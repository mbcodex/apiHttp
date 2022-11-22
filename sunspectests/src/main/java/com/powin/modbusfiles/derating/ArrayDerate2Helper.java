package com.powin.modbusfiles.derating;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.reports.SystemInfo;
import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;
import com.powin.modbusfiles.utilities.DateTimeHelper;
import com.powin.modbusfiles.utilities.FileHelper;
import com.powin.modbusfiles.utilities.Result;
import com.powin.modbusfiles.utilities.StringUtils;

public class ArrayDerate2Helper {
	public final static Logger LOG = LogManager.getLogger();
	static String timerStartedArray = "false,false,false,false";
	static String timerStopInstantArray = ",,,";
	static String timer2StartedArray = "false,false,false,false";
	static String timer2StopInstantArray = ",,,";
	
	public static int RESULT_FILE_ALLOWED_CURRENT_INDEX=1;
	public static int RESULT_FILE_MAX_VOLTAGE_INDEX=2;
	public static int RESULT_FILE_AVG_VOLTAGE_INDEX=3;
	public static int RESULT_FILE_ARRAY_CURRENT_INDEX=4;
	public static int RESULT_FILE_AC_POWER_INDEX=5;
	public static int RESULT_FILE_DC_BUS_VOLTAGE_INDEX=6;
	public static int ENTRY_VOLTAGE_INDEX=0;
	public static int EXIT_CURRENT_INDEX=1;
	public static int EXIT_ENERGY_INDEX=2;
	public static int DERATE_FACTOR_INDEX=3;
	
	static final boolean POWER_IS_MOVING = true;
	public static int RESULT_FILE_TIME_INDEX=0;
	static double accumulatedEnergy=0;

	public static void getDerateTestDataFromDb(int arrayIndex, String testStart, String testEnd,
			String testresultFilepath) {
		String sqlQuery = "\"select to_char(reporttime at time zone 'utc','YYYY-MM-DD HH24:MI:SS'),maxallowedchargecurrent,connectedstackmaxcellvoltage,connectedstackavgcellvoltage,amps,kw,dcbusvoltage "
				+ "from kobold2.fullreportheader r " + "inner join " + "kobold2.fullarrayreport a "
				+ "on a.reportid=r.reportid " + "where reporttime at time zone 'utc' between '" + testStart + "' and '"
				+ testEnd + "'" + " and arrayid in" + " (select arrayid from kobold2.arrays arr "
				+ " inner join kobold2.blocks b on arr.blockid = b.blockid "
				+ " inner join kobold2.stations s on s.stationid=b.stationid " + " where s.stationcode ='"
				+ ArrayDerate2.cStationCode + "'" + " and arr.arrayindex=" + arrayIndex + ") " +

				" order by reporttime asc;\"";
		CommonHelper.getPostgresQueryResults(sqlQuery, testresultFilepath);
	}

	public static boolean timerExpired(int arrayIndex, ZonedDateTime presentTime) {
		if (timerStopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			ArrayDerate2Helper.editEnergyCounterArray(ZonedDateTime.now(), arrayIndex);
		}
		ZonedDateTime newTimerStopInstant = ArrayDerate2Helper.getEnergyCounterStopInstant(arrayIndex);
		return presentTime.isAfter(newTimerStopInstant);
	}
	
	public static boolean timer2Expired(int arrayIndex, ZonedDateTime presentTime) {
		if (timer2StopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			editEnergyCounter2Array(ZonedDateTime.now(), arrayIndex);
		}
		ZonedDateTime newTimerStopInstant = ArrayDerate2Helper.getEnergyCounter2StopInstant(arrayIndex);
		return presentTime.isAfter(newTimerStopInstant);
	}

	public static int compareStrategies(String currentStrategy, String referenceStrategy) {
		return getEntryVoltageFromStrategy(currentStrategy)
				- getEntryVoltageFromStrategy(referenceStrategy);
	}

	public static boolean hasExitVoltageBeenReached(int presentVoltage, String strategy) {
//		int exitVoltage = ArrayDerate2Helper.getExitEnergyFromStrategy(strategy);
//		return presentVoltage <= exitVoltage;
		return false;
	}

	public static int getEntryVoltageFromStrategy(String strategy) {
		return Integer.parseInt(strategy.split(",")[ENTRY_VOLTAGE_INDEX]);
	}

	public static int getExitCurrentFromStrategy(String strategy) {
		return Integer.parseInt(strategy.split(",")[EXIT_ENERGY_INDEX]);
	}

	public static int getExitEnergyFromStrategy(String strategy) {
		return Integer.parseInt(strategy.split(",")[EXIT_ENERGY_INDEX]);
	}

	public static double getDerateFactorFromStrategy(String strategy) {
		return Double.parseDouble(strategy.split(",")[DERATE_FACTOR_INDEX]);
	}
	public static void startEnergyCounter2(String strategy, int arrayIndex) {
		editEnergyCounter2Status(arrayIndex, true);
		int timerDurationSeconds = getExitEnergyFromStrategy(strategy) / 1000;
		ZonedDateTime currentTime = ZonedDateTime.now();
		ZonedDateTime endTime = currentTime.plusSeconds(timerDurationSeconds);
		editEnergyCounter2Array(endTime, arrayIndex);
	}

	public static void editEnergyCounter2Status(int arrayIndex, boolean start) {
		String[] currentTimerStatusArray = timer2StartedArray.split(",", -8);
		currentTimerStatusArray[arrayIndex - 1] = String.valueOf(start);
		timer2StartedArray = String.join(",", currentTimerStatusArray);
	}

	public static boolean getEnergyCounter2Status(int arrayIndex) {
		String[] currentTimerStatusArray = timer2StartedArray.split(",", -8);
		String status = currentTimerStatusArray[arrayIndex - 1];
		return Boolean.parseBoolean(status);
	}

	public static void editEnergyCounter2Array(ZonedDateTime newTime, int arrayIndex) {
		String[] currentTimerStopInstantArray = timer2StopInstantArray.split(",", -8);
		currentTimerStopInstantArray[arrayIndex - 1] = newTime.toString();
		timer2StopInstantArray = String.join(",", currentTimerStopInstantArray);
	}

	public static ZonedDateTime getEnergyCounter2StopInstant(int arrayIndex) {
		if (timer2StopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			editEnergyCounter2Array(ZonedDateTime.now(), arrayIndex);
		}
		String[] currentTimerStopInstantArray = timer2StopInstantArray.split(",", -8);
		return ZonedDateTime.parse(currentTimerStopInstantArray[arrayIndex - 1]);
	}
	public static void startTimer2(int arrayIndex, ZonedDateTime currentTime) {
		editTimer2Status(arrayIndex, true);
		int timerDurationSeconds =ArrayDerate2.PAUSE_MILLISECONDS_AFTER_SECOND_DERATE /1000;//getExitTimeoutMsFromStrategy(strategy) / 1000;
		ZonedDateTime endTime = currentTime.plusSeconds(timerDurationSeconds);
		editTimer2StopInstantArray(endTime, arrayIndex);
	}

	public static void editTimer2Status(int arrayIndex, boolean start) {
		String[] currentTimerStatusArray = timer2StartedArray.split(",", -8);
		currentTimerStatusArray[arrayIndex - 1] = String.valueOf(start);
		timer2StartedArray = String.join(",", currentTimerStatusArray);
	}

	public static boolean getTimer2Status(int arrayIndex) {
		String[] currentTimerStatusArray = timerStartedArray.split(",", -8);
		String status = currentTimerStatusArray[arrayIndex - 1];
		return Boolean.parseBoolean(status);
	}

	public static void editTimer2StopInstantArray(ZonedDateTime newTime, int arrayIndex) {
		String[] currentTimerStopInstantArray = timer2StopInstantArray.split(",", -8);
		currentTimerStopInstantArray[arrayIndex - 1] = newTime.toString();
		timer2StopInstantArray = String.join(",", currentTimerStopInstantArray);
	}

	public static ZonedDateTime getTimer2StopInstant(int arrayIndex) {
		if (timer2StopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			editTimerStopInstantArray(ZonedDateTime.now(), arrayIndex);
		}
		String[] currentTimerStopInstantArray = timer2StopInstantArray.split(",", -8);
		return ZonedDateTime.parse(currentTimerStopInstantArray[arrayIndex - 1]);
	}
	
	

	public static void startEnergyCounter(String strategy, int arrayIndex) {
		ArrayDerate2Helper.editEnergyCounterStatus(arrayIndex, true);
		int timerDurationSeconds = getExitEnergyFromStrategy(strategy) / 1000;
		ZonedDateTime currentTime = ZonedDateTime.now();
		ZonedDateTime endTime = currentTime.plusSeconds(timerDurationSeconds);
		ArrayDerate2Helper.editEnergyCounterArray(endTime, arrayIndex);
	}

	public static void editEnergyCounterStatus(int arrayIndex, boolean start) {
		String[] currentTimerStatusArray = timerStartedArray.split(",", -8);
		currentTimerStatusArray[arrayIndex - 1] = String.valueOf(start);
		timerStartedArray = String.join(",", currentTimerStatusArray);
	}

	public static boolean getEnergyCounterStatus(int arrayIndex) {
		String[] currentTimerStatusArray = timerStartedArray.split(",", -8);
		String status = currentTimerStatusArray[arrayIndex - 1];
		return Boolean.parseBoolean(status);
	}

	public static void editEnergyCounterArray(ZonedDateTime newTime, int arrayIndex) {
		String[] currentTimerStopInstantArray = timerStopInstantArray.split(",", -8);
		currentTimerStopInstantArray[arrayIndex - 1] = newTime.toString();
		timerStopInstantArray = String.join(",", currentTimerStopInstantArray);
	}

	public static ZonedDateTime getEnergyCounterStopInstant(int arrayIndex) {
		if (timerStopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			editEnergyCounterArray(ZonedDateTime.now(), arrayIndex);
		}
		String[] currentTimerStopInstantArray = timerStopInstantArray.split(",", -8);
		return ZonedDateTime.parse(currentTimerStopInstantArray[arrayIndex - 1]);
	}
	public static void startTimer(int arrayIndex, ZonedDateTime currentTime) {
		editTimerStatus(arrayIndex, true);
		int timerDurationSeconds = 300;//getExitTimeoutMsFromStrategy(strategy) / 1000;
		ZonedDateTime endTime = currentTime.plusSeconds(timerDurationSeconds);
		editTimerStopInstantArray(endTime, arrayIndex);
	}

	public static void editTimerStatus(int arrayIndex, boolean start) {
		String[] currentTimerStatusArray = timerStartedArray.split(",", -8);
		currentTimerStatusArray[arrayIndex - 1] = String.valueOf(start);
		timerStartedArray = String.join(",", currentTimerStatusArray);
	}

	public static boolean getTimerStatus(int arrayIndex) {
		String[] currentTimerStatusArray = timerStartedArray.split(",", -8);
		String status = currentTimerStatusArray[arrayIndex - 1];
		return Boolean.parseBoolean(status);
	}

	public static void editTimerStopInstantArray(ZonedDateTime newTime, int arrayIndex) {
		String[] currentTimerStopInstantArray = timerStopInstantArray.split(",", -8);
		currentTimerStopInstantArray[arrayIndex - 1] = newTime.toString();
		timerStopInstantArray = String.join(",", currentTimerStopInstantArray);
	}

	public static ZonedDateTime getTimerStopInstant(int arrayIndex) {
		if (timerStopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			editTimerStopInstantArray(ZonedDateTime.now(), arrayIndex);
		}
		String[] currentTimerStopInstantArray = timerStopInstantArray.split(",", -8);
		return ZonedDateTime.parse(currentTimerStopInstantArray[arrayIndex - 1]);
	}


	public static String getApplicableDerateStrategy(String deratingStrategySet, int presentVoltage) {
		ArrayList<String> deratingStrategyList = ArrayDerate2Helper.getStrategyList(deratingStrategySet);
		int findDerateStrategy = ArrayDerate2Helper.searchArrayList(deratingStrategyList, presentVoltage, "");
		if (findDerateStrategy == -1) {
			ArrayDerate2.LOG.info(presentVoltage + " is not governed by strategy");
			return ArrayDerate2.NO_TRIGGER;
		} else {
			ArrayDerate2.LOG
					.info(presentVoltage + " is governed by strategy: " + deratingStrategyList.get(findDerateStrategy));
			return deratingStrategyList.get(findDerateStrategy);
		}
	}

	public static ArrayList<String> getStrategyList(String deratingStrategySet) {
		String[] deratingStrategyArray = deratingStrategySet.split("\\|");
		ArrayList<String> deratingStrategyList = new ArrayList<String>();
		for (String deratingStrategy : deratingStrategyArray) {
			deratingStrategyList.add(deratingStrategy);
		}
		CommonHelper.sortListBySubstring(deratingStrategyList, Constants.ASCENDING_ORDER, 0, "INT");
		return deratingStrategyList;
	}

	public static int searchArrayList(ArrayList<String> targetArrayList, int searchTerm, String mode) {
		int currentIndex = 0;
		for (currentIndex = 0; currentIndex < targetArrayList.size(); currentIndex++) {
			int entryVoltage = Integer.parseInt(targetArrayList.get(currentIndex).split(",")[0]);
			if (searchTerm < entryVoltage) {
				break;
			}
		}
		return currentIndex - 1;
	}

	public static int getPresentMaxChargeCapacity(String testData, int fieldIndex) {
		try {
			String maxChargeCapacity = StringUtils.getFieldFromDelimitedString(testData,fieldIndex, Constants.DELIMITER_PIPE);
			return Integer.parseInt(maxChargeCapacity);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return -1;
	}

	public static int getPresentMaxVoltage(String testData) {
		try {
			String cellMaxVoltage = StringUtils.getFieldFromDelimitedString(testData,RESULT_FILE_MAX_VOLTAGE_INDEX, Constants.DELIMITER_PIPE);
			return Integer.parseInt(cellMaxVoltage);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return -1;
	}
	
	public static int getPresentAvgVoltage(String testData) {
		try {
			String cellAvgVoltage = StringUtils.getFieldFromDelimitedString(testData,RESULT_FILE_AVG_VOLTAGE_INDEX, Constants.DELIMITER_PIPE);
			return Integer.parseInt(cellAvgVoltage);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return -1;
	}
	
	public static int getPresentCurrent(String testData) {
		try {
			String arrayCurrent = StringUtils.getFieldFromDelimitedString(testData,RESULT_FILE_ARRAY_CURRENT_INDEX, Constants.DELIMITER_PIPE);
			return Integer.parseInt(arrayCurrent);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return -1;	
	}

	public static void getTestDataFromDatabaseToFiles(ZonedDateTime startTime, ZonedDateTime endTime, String testMode) {
		String startTimeString = DateTimeHelper.getFormattedTime(startTime.truncatedTo(ChronoUnit.SECONDS),
				Constants.YYYY_MM_DD_HH_MM_SS);
		String endTimeString = DateTimeHelper.getFormattedTime(endTime.truncatedTo(ChronoUnit.SECONDS),
				Constants.YYYY_MM_DD_HH_MM_SS);
		for (int arrayIndex = 1; arrayIndex <= ArrayDerate2.cArrayCount; arrayIndex++) {
			String testFilePath = setResultFileFullPath(arrayIndex, testMode);
			getDerateTestDataFromDb(arrayIndex, startTimeString, endTimeString, testFilePath);
		}
	}

	public static String setResultFileFullPath(int arrayIndex, String testMode) {
		String testFilePath = "" ;
		switch(testMode.toUpperCase()) {
			case "KNEES":
				testFilePath = ArrayDerate2.testresultFolderpath + "ad2_knees_" +arrayIndex +"_"+ 1000*Math.random() +".txt";
				ArrayDerate2.resultFilePathsKnees.add(arrayIndex -1,testFilePath);
			break;
			case "EXIT":
				testFilePath = ArrayDerate2.testresultFolderpath + "ad2_exit_" +arrayIndex +"_"+ 1000*Math.random() +".txt";
				ArrayDerate2.resultFilePathsExit.add(arrayIndex -1,testFilePath);
			break;
		}
		return testFilePath;
	}
	
	public static String getResultFileFullPath(int arrayIndex, String testMode) {
		String testFilePath ="";
		switch(testMode.toUpperCase()) {
			case "KNEES":
				testFilePath = ArrayDerate2.resultFilePathsKnees.get(arrayIndex-1);
			break;
			case "EXIT":
				testFilePath = ArrayDerate2.resultFilePathsExit.get(arrayIndex-1);
			break;
		}
		return testFilePath;
	}

	public static List<String> getTestDataFromFile(int arrayIndex, String testMode) {
		String testFilePath = getResultFileFullPath(arrayIndex, testMode);
		List<String> testDataFile = FileHelper.readFileToList(testFilePath);
		return testDataFile;
	}

	public static Result validateArrayDerateStrategy(int arrayIndex, boolean isMovingPower, int tolerance, String testData, int currentIndex) {
		Result isTestPass;
		double expectedDerateFactor = ArrayDerate2.getExpectedDerateFactor(arrayIndex,testData);
		if (isMovingPower) {
			isTestPass = ArrayDerate2Helper.validateDeratedCurrent(arrayIndex, tolerance, testData, currentIndex, expectedDerateFactor);
		} else {
			isTestPass = ArrayDerate2Helper.validateDerateFactor(arrayIndex, tolerance, expectedDerateFactor);
		}
		return isTestPass;
	}

	public static Result validateDerateFactor(int arrayIndex, int tolerance, double expectedDerateFactor) {
		double actualDerate = (double) SystemInfo.getMaxAllowableChargeCurrent(arrayIndex)
				/ (ArrayDerate2.maxChargeCurrentPerStackAmps* ArrayDerate2.cStackCountPerArray);
		boolean isActualDerateAsExpected = CommonHelper.compareIntegers((int) (100 * expectedDerateFactor), (int) (100 * actualDerate),tolerance, .01);
		Result isTestPass = Result.getInstance(isActualDerateAsExpected, "Comparing Derate Percentage.");
		String passfail = isTestPass.isPass() ? "PASS:" : "FAIL:";
		ArrayDerate2.LOG.info("{} Array#{} - Actual derate: {},  Expected derate: {}",passfail, arrayIndex, actualDerate,expectedDerateFactor);
		return isTestPass;
	}

	public static Result validateDeratedCurrent(int arrayIndex, int tolerance, String testData, int currentIndex,
			double expectedDerateFactor) {
		int expectedDeratedCurrent = ArrayDerate2.getExpectedDeratedCurrent(arrayIndex, testData,expectedDerateFactor);
		int actualCurrent = DerateCommon.getArrayCurrent(testData, currentIndex);
		boolean isActualCurrentAsExpected = CommonHelper.compareIntegers(Math.abs(actualCurrent), Math.abs(expectedDeratedCurrent),tolerance, .01);
		Result isTestPass = Result.getInstance(isActualCurrentAsExpected,"Comparing Derate Current.");
		String passfail = isTestPass.isPass() ? "PASS:" : "FAIL:";
		ArrayDerate2.LOG.info("{} Array#{} - Actual current: {}, Expected current: {}", passfail, arrayIndex, actualCurrent,expectedDeratedCurrent);
		if (!isTestPass.isPass()) {
			isTestPass.setComment(isTestPass.getComment() + " Actual Current: " + actualCurrent);
		}
		return isTestPass;
	}

	public static Result validateAd2Knees(int expectedVsActualTolerance, int arrayIndex, List<String> testDataFromFile, int currentIndex) {
		return ArrayDerate2Helper.validateArrayDerate2Common(new ArrayDerate2TestParameters(expectedVsActualTolerance, arrayIndex, testDataFromFile, currentIndex));
	}

	public static Result validateAd2ExitCriteria(int arrayIndex, List<String> testDataFromFile) {
		return ArrayDerate2Helper.validateArrayDerate2Common(new ArrayDerate2TestParameters(arrayIndex, testDataFromFile));
	}

	/** Derate validation all follows the same process **/
	public static Result validateArrayDerate2Common(ArrayDerate2TestParameters ad2TestParameters) {
		Result isTestPass = Result.getInstance(true);
		ZonedDateTime currentErrorTime = ZonedDateTime.now();
		ZonedDateTime endErrorTime = currentErrorTime;
		boolean errorPhaseHasStarted = false;
		int errorCycle = 0;
		int errorsInCycle = 0;
		int failingErrorCycle = 0;
		for (int lineNumber = 2; lineNumber < ad2TestParameters.testData.size()-2; lineNumber++) {
			String testData = ad2TestParameters.testData.get(lineNumber);
			Result tempTestPass = Result.getInstance(true);
			if (ad2TestParameters.call == 0) {
			  tempTestPass = ArrayDerate2Helper.validateArrayDerateExitStrategy(testData);
			} else if (ad2TestParameters.call == 1) {
			  tempTestPass = validateArrayDerateStrategy(ad2TestParameters.arrayIndex, ArrayDerate2Helper.POWER_IS_MOVING, ad2TestParameters.expectedVsActualTolerance, testData, ad2TestParameters.currentIndex);
			}
			// Will ignore errors upto a consecutive window of consecutiveFailureToleranceSeconds seconds
			if (!tempTestPass.isPass()) {
				ArrayDerate2Helper.LOG.info("testData: failed - {}", testData);
				// Start a new error cycle if one is not already started
				currentErrorTime = DerateCommon.getPresentTime(testData, ArrayDerate2Helper.RESULT_FILE_TIME_INDEX);
				if (!errorPhaseHasStarted) {
					errorPhaseHasStarted = true;
					endErrorTime = currentErrorTime.plusSeconds(ArrayDerate2.consecutiveFailureToleranceSeconds);
					errorCycle++;
					errorsInCycle = 1;
					ArrayDerate2Helper.LOG.info("Error cycle number: {}", errorCycle);
				} else {// in error cycle
					errorsInCycle++;
					if (currentErrorTime.isAfter(endErrorTime)) {
						failingErrorCycle++;
						ArrayDerate2Helper.LOG.info("FAIL:Consecutive errors:{} in Error cycle number: {}", errorsInCycle, errorCycle);
						errorPhaseHasStarted = false;
					}
				}
			} else {// if test passes cancel the error cycle
				errorPhaseHasStarted = false;
				endErrorTime = ZonedDateTime.now();
				CommonHelper.quietSleep(250);
			}
			isTestPass.add(tempTestPass);
		}
		ArrayDerate2Helper.LOG.info("num fails in power cycle:{}", failingErrorCycle);
		boolean isErrorCyclesPresent = failingErrorCycle != 0;
		if(!isErrorCyclesPresent) {
			ArrayDerate2Helper.LOG.info("Validation passed for Array#{}",ad2TestParameters.arrayIndex);
		}
		else {
			ArrayDerate2Helper.LOG.info("Validation failed for Array#{}",ad2TestParameters.arrayIndex);
			ArrayDerate2Helper.LOG.info("num fails in power cycle:{}", failingErrorCycle);
		}
		return isTestPass;
	}

	public static Result validateArrayDerateExitStrategy(String testData) {
		Result isTestPass= Result.getInstance(true);
		ZonedDateTime presentTime = DerateCommon.getPresentTime(testData, ArrayDerate2Helper.RESULT_FILE_TIME_INDEX);
		int presentCurrent=getPresentCurrent(testData);
		int presentMaxChargeCapacity= getPresentMaxChargeCapacity(testData, RESULT_FILE_ALLOWED_CURRENT_INDEX);
		//Get derate break point
		if(presentCurrent > 0) {
		//Start accumulating energy
			double incrementalEnergy = (double) presentCurrent*1/3600;
			ArrayDerate2Helper.accumulatedEnergy += incrementalEnergy;
			int level=0;
			//Verify the accumulated energy is appropriate for the derate break point
			if (ArrayDerate2Helper.accumulatedEnergy<10) {
				//isTestPass=CommonHelper.compareIntegers(presentMaxChargeCapacity, 0, 1, 0.01);
				level=0;
			}
			if (ArrayDerate2Helper.accumulatedEnergy>10.1 && ArrayDerate2Helper.accumulatedEnergy<20) {
				boolean isPresentChargeCapacityAtLevel1 = CommonHelper.compareIntegers(presentMaxChargeCapacity, 75, 1, 0.01);
				isTestPass.add(isPresentChargeCapacityAtLevel1, "Charge at level 1.");
				level=1;
			}
			if (ArrayDerate2Helper.accumulatedEnergy>20.1) {
				boolean isPresntChargeCapacityAtLevel2 = CommonHelper.compareIntegers(presentMaxChargeCapacity, 150, 1, 0.01);
				isTestPass.add(isPresntChargeCapacityAtLevel2, "Charge at level 2");
				level=2;
			}
			ArrayDerate2.LOG.info("level:{},accumulatedEnergy={} at time={} and capacity={}",level,ArrayDerate2Helper.accumulatedEnergy,presentTime,presentMaxChargeCapacity);
		}
		return isTestPass;
	}
}
