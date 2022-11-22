package com.powin.modbusfiles.derating;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.configuration.StackType;
import com.powin.modbusfiles.power.MovePower;
import com.powin.modbusfiles.reports.SystemInfo;
import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;
import com.powin.modbusfiles.utilities.Result;

//public class CellVoltageDerate extends CyclingTestBase {
	public class CellVoltageDerate  {
	// Constants
	public static final int MAX_CELL_VOLTAGE = 3442;// TO DO : Should be stack/config dependant
	public static final int ONE_SECOND = 1000;
	public static final String NO_TRIGGER = "0,5000,0,1";
	//S20-140-200-1000-E40-derate01
	private static final String deratingStrategySet = 
					"3400,3350,60000,0.5|" 		+ 
					"3410,3370,80000,0.25|"		+ 
					"3420,3380,100000,0.13|" 	+ 
					"3430,3390,120000,0.06|" 	+ 
					"3440,3410,140000,0.03";
	
	// Test utilities
	public final static Logger LOG = LogManager.getLogger();
	// Set up
	public static int cArrayCount=1;
	public static int cStackCountPerArray=1;
	public final static int maxChargeCurrentPerStackAmps = 200;
	public final static int maxChargePowerPerStackKw = 450;
	private static double commandedPowerPercentage= 0.6;
	static int consecutiveFailureToleranceSeconds=40;
	//Fields that are used to store runtime state
	public static String prevailingDeratingStrategy = NO_TRIGGER;
	static String testresultFolderpath="/home/powin/";
	static String cStationCode;
	private static int cBlockIndex;
	public static List<String> resultFilePaths=new ArrayList<String>();
	
	static {
		cStationCode = SystemInfo.getStationCode();
		cBlockIndex = SystemInfo.getBlockIndex();
	}
	
	
	public void init(int arrayCount, int stackCountPerArray) throws IOException, InterruptedException {
		cArrayCount = arrayCount;
		cStackCountPerArray = stackCountPerArray;
		LOG.info("init - creating system with {} arrays and {} strings", arrayCount, stackCountPerArray);
		boolean isTomcatRestartRequired = CommonHelper.setupSystem(	SystemInfo.getStationCode(),
																	StackType.STACK_140_GEN2, 
																	arrayCount, 
																	stackCountPerArray, 
																	stackCountPerArray*maxChargePowerPerStackKw,
																	stackCountPerArray * maxChargeCurrentPerStackAmps);
		if (isTomcatRestartRequired) {
			CommonHelper.restartTurtleTomcat();
			CommonHelper.quietSleep(Constants.TEN_SECONDS);
		}
	}

	public static void triggerDerate(boolean isCharging, int testDurationSeconds) {
		// charge or discharge
		int totalCommandedPower = (int) (commandedPowerPercentage*maxChargePowerPerStackKw*cArrayCount*cStackCountPerArray);
		if (isCharging) {
			MovePower.setPowerPowerCommand(-totalCommandedPower, 0);
		} else {
			MovePower.setPowerPowerCommand(totalCommandedPower, 0);
		}
		//Wait till cycleDurationMinutes. Test variables startTime and endTime are set
		boolean timerRunning = true;
		ZonedDateTime startTime = ZonedDateTime.now();
		ZonedDateTime endTime = startTime.plusMinutes(testDurationSeconds);
		while (timerRunning) {
			timerRunning = ZonedDateTime.now().isBefore(endTime);
			CommonHelper.sleep(Constants.ONE_MINUTE_MS);
		}
		//collects data from the database pertaining to this run
		CellVoltageDerateHelper.getTestDataFromDatabaseToFiles(startTime, endTime) ;
	}
	
	public static double getExpectedDerateFactor(int arrayIndex, String testData) {
		double derateFactor = 0.0;
		int presentVoltage = CellVoltageDerateHelper.getPresentVoltage(testData);
		ZonedDateTime presentTime = DerateCommon.getPresentTime(testData, ArrayDerate2Helper.RESULT_FILE_TIME_INDEX);
		String presentDerateStrategy = CellVoltageDerateHelper.getApplicableDerateStrategy(deratingStrategySet, presentVoltage);
		LOG.info("prevailing derate strategy {} ; presentDerateStrategy:{}; present voltage :{} mV.",prevailingDeratingStrategy, presentDerateStrategy, presentVoltage);
		// If the prevailing strategy needs to be raised
		if (CellVoltageDerateHelper.compareStrategies(presentDerateStrategy, prevailingDeratingStrategy) > 0) { 
			CellVoltageDerateHelper.editTimerStatus(arrayIndex, false);
			prevailingDeratingStrategy = presentDerateStrategy;// Uprate strategy
			LOG.info("derate strategy {} has been uprated by {}. Present voltage: {} mV.", prevailingDeratingStrategy,presentDerateStrategy, presentVoltage);
		} 
		else {// Check if derate can be exited
			if (CellVoltageDerateHelper.hasExitVoltageBeenReached(presentVoltage, prevailingDeratingStrategy)) {
				LOG.info("Exit voltage reached for prevailing derate strategy: {}. Current voltage:{}",prevailingDeratingStrategy, presentVoltage);
				if (!CellVoltageDerateHelper.getTimerStatus(arrayIndex))
					CellVoltageDerateHelper.startTimer(prevailingDeratingStrategy, arrayIndex);
				if (CellVoltageDerateHelper.timerExpired(arrayIndex, presentTime)) {
					// Downrate prevailing strategy
					prevailingDeratingStrategy = presentDerateStrategy;
					LOG.info("derate strategy {} has been downrated by {}. Present voltage: {} mV.",prevailingDeratingStrategy, presentDerateStrategy, presentVoltage);
					derateFactor = CellVoltageDerateHelper.getDerateFactorFromStrategy(prevailingDeratingStrategy);
				}
			}
		}
		derateFactor = CellVoltageDerateHelper.getDerateFactorFromStrategy(prevailingDeratingStrategy);
		return derateFactor;
	}

	static int getExpectedDeratedCurrent(int arrayIndex, String testData,double expectedDerateFactor ) {
		int dcBusVoltage = DerateCommon.getDcBusVoltage(testData, CellVoltageDerateHelper.RESULT_FILE_DC_BUS_VOLTAGE_INDEX);
		int namePlateCurrent=maxChargeCurrentPerStackAmps * cStackCountPerArray;
		int expectedCurrent = (int) (expectedDerateFactor *namePlateCurrent );
		int arrayNameplatePower=1000*maxChargePowerPerStackKw*cStackCountPerArray;
		int arrayCommandedPower = (int) (commandedPowerPercentage*arrayNameplatePower);
		int currentCorrespondingToCommandedPower=arrayCommandedPower/ dcBusVoltage;
		int currentCorrespondingToNameplatePower=arrayNameplatePower / dcBusVoltage;
		int maxChargeCurrentNormalized = -Math.min(expectedCurrent,currentCorrespondingToCommandedPower);
		maxChargeCurrentNormalized= -Math.min(maxChargeCurrentNormalized,currentCorrespondingToNameplatePower);
		return maxChargeCurrentNormalized;
	}
	public static Result validateCvdArray(int tolerance, int arrayIndex) {
		Result isTestPass = Result.getInstance(true);
//		ZonedDateTime currentErrorTime = ZonedDateTime.now();
//		ZonedDateTime endErrorTime = ZonedDateTime.now();
//		boolean errorPhaseHasStarted = false;
//		int errorCycle = 0;
//		int errorsInCycle = 0;
//		int failingErrorCycle = 0;
		List<String> testDataFile = CellVoltageDerateHelper.getTestDataFromFile(arrayIndex) ;
		for (int lineNumber = 2; lineNumber < testDataFile.size()-2; lineNumber++) {
			String testData = testDataFile.get(lineNumber);
			Result tempTestPass = Result.getInstance(true);
			String message = "testData:"+testData+" at line number:"+lineNumber;
			tempTestPass.add( validateArrayDerateStrategy(arrayIndex, true, tolerance, testData),message);
//			// Will ignore errors upto a consecutive window of consecutiveFailureToleranceSeconds seconds
//			if (!isTestPass) {
//				LOG.info("testData: failed - {}", testData);
//				// Start a new error cycle if one is not already started
//				currentErrorTime = DerateHelper.getPresentTime(testData, 0);
//				if (!errorPhaseHasStarted) {
//					errorPhaseHasStarted = true;
//					endErrorTime = currentErrorTime.plusSeconds(consecutiveFailureToleranceSeconds);
//					errorCycle++;
//					errorsInCycle = 1;
//					LOG.info("Error cycle number: {}", errorCycle);
//				} else {// in error cycle
//					errorsInCycle++;
//					if (currentErrorTime.isAfter(endErrorTime)) {
//						failingErrorCycle++;
//						LOG.info("FAIL:Consecutive errors:{} in Error cycle number: {}", errorsInCycle, errorCycle);
//						errorPhaseHasStarted = false;
//					}
//				}
//			} else {// if test passes cancel the error cycle
//				errorPhaseHasStarted = false;
//				endErrorTime = ZonedDateTime.now();
//				CommonHelper.quietSleep(250);
//				isTestPass = true;
//			}
//		}
//		LOG.info("num fails in power cycle:{}", failingErrorCycle);
//		isTestPass = failingErrorCycle == 0 ? true : false;
//		if(isTestPass) {
//			LOG.info("Validation passed for Array#{}",arrayIndex);
//		}
//		else {
//			LOG.info("Validation failed for Array#{}",arrayIndex);
//			LOG.info("num fails in power cycle:{}", failingErrorCycle);
//		}
		isTestPass.add(tempTestPass);
		}
		return isTestPass;
	}
	
	public static boolean validateArrayDerateStrategy(int arrayIndex, boolean isMovingPower, int tolerance, String testData) {
		boolean isTestPass = true;
		double expectedDerateFactor = getExpectedDerateFactor(arrayIndex,testData);
		if (isMovingPower) {
			int expectedDeratedCurrent = getExpectedDeratedCurrent(arrayIndex, testData,expectedDerateFactor);
			int actualCurrent = DerateCommon.getArrayCurrent(testData,2);//(int) SystemInfo.getArrayCurrent(arrayIndex);
			isTestPass = CommonHelper.compareIntegers(Math.abs(actualCurrent), Math.abs(expectedDeratedCurrent),tolerance, .01);
			if (isTestPass)
				LOG.info("PASS: Array#{} - Actual current: {}, Expected current: {}", arrayIndex, actualCurrent,expectedDeratedCurrent);
			else
				LOG.info("FAIL: Array#{} - Actual current: {}, Expected current: {}", arrayIndex, actualCurrent,expectedDeratedCurrent);
		} else {
			double actualDerate = (double) SystemInfo.getMaxAllowableChargeCurrent(arrayIndex)
					/ (maxChargeCurrentPerStackAmps* cStackCountPerArray);
			isTestPass = CommonHelper.compareIntegers((int) (100 * expectedDerateFactor), (int) (100 * actualDerate),tolerance, .01);
			if (isTestPass)
				LOG.info("PASS: Array#{} - Actual derate: {},  Expected derate: {}", arrayIndex, actualDerate,expectedDerateFactor);
			else
				LOG.info("FAIL: Array#{} - Actual derate: {},  Expected derate: {}", arrayIndex, actualDerate,expectedDerateFactor);
		}
		return isTestPass;
	}

}