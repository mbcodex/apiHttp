package com.powin.modbusfiles.derating;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;
import com.powin.modbusfiles.utilities.DateTimeHelper;
import com.powin.modbusfiles.utilities.FileHelper;

public class CellVoltageDerateHelper {
	private final static Logger LOG = LogManager.getLogger();
	static String timerStartedArray = "false,false,false,false";;
	static String timerStopInstantArray = ",,,";

	public static int RESULT_FILE_ARRAY_CURRENT_INDEX=2;
	public static int RESULT_FILE_DC_BUS_VOLTAGE_INDEX=4;

	
	public static void getDerateTestDataFromDb(int arrayIndex, String testStart, String testEnd, String testresultFilepath) {
		String sqlQuery="\"select to_char(reporttime at time zone 'utc','YYYY-MM-DD HH24:MI:SS'),connectedstackmaxcellvoltage,amps,kw,dcbusvoltage "+
				 "from kobold2.fullreportheader r "+
				 "inner join "+
				 "kobold2.fullarrayreport a "+
				 "on a.reportid=r.reportid "+
				 "where reporttime at time zone 'utc' between '"+testStart+"' and '"+testEnd+"'" +
				 " and arrayid in"+
				 " (select arrayid from kobold2.arrays arr "+
				 " inner join kobold2.blocks b on arr.blockid = b.blockid "+
				 " inner join kobold2.stations s on s.stationid=b.stationid "+
				 " where s.stationcode ='"+CellVoltageDerate.cStationCode+"'"+
				 " and arr.arrayindex="+arrayIndex+") "+
				 
				 " order by reporttime asc;\"";
		CommonHelper.getPostgresQueryResults(sqlQuery,testresultFilepath);
	}

	public static boolean timerExpired(int arrayIndex, ZonedDateTime presentTime) {
		if (timerStopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			CellVoltageDerateHelper.editTimerStopInstantArray(ZonedDateTime.now(), arrayIndex);
		}
		ZonedDateTime newTimerStopInstant = CellVoltageDerateHelper.getTimerStopInstant(arrayIndex);
		return presentTime.isAfter(newTimerStopInstant);
	}

	public static int compareStrategies(String currentStrategy, String referenceStrategy) {
		return CellVoltageDerateHelper.getEntryVoltageFromStrategy(currentStrategy) - CellVoltageDerateHelper.getEntryVoltageFromStrategy(referenceStrategy);
	}

	public static boolean hasExitVoltageBeenReached(int presentVoltage, String strategy) {
		int exitVoltage = CellVoltageDerateHelper.getExitVoltageFromStrategy(strategy);
		return presentVoltage <= exitVoltage;
	}

	public static int getEntryVoltageFromStrategy(String strategy) {
		return Integer.parseInt(strategy.split(",")[0]);
	}

	public static int getExitVoltageFromStrategy(String strategy) {
		return Integer.parseInt(strategy.split(",")[1]);
	}

	public static int getExitTimeoutMsFromStrategy(String strategy) {
		return Integer.parseInt(strategy.split(",")[2]);
	}

	public static double getDerateFactorFromStrategy(String strategy) {
		return Double.parseDouble(strategy.split(",")[3]);
	}

	public static void startTimer(String strategy, int arrayIndex) {
		CellVoltageDerateHelper.editTimerStatus(arrayIndex, true);
		int timerDurationSeconds = getExitTimeoutMsFromStrategy(strategy) / 1000;
		ZonedDateTime currentTime = ZonedDateTime.now();
		ZonedDateTime endTime = currentTime.plusSeconds(timerDurationSeconds);
		CellVoltageDerateHelper.editTimerStopInstantArray(endTime, arrayIndex);
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
		ArrayList<String> deratingStrategyList = CellVoltageDerateHelper.getStrategyList(deratingStrategySet);
		int findDerateStrategy = CellVoltageDerateHelper.searchArrayList(deratingStrategyList, presentVoltage, "");
		if (findDerateStrategy == -1) {
			CellVoltageDerate.LOG.info(presentVoltage + " is not governed by strategy");
			return CellVoltageDerate.NO_TRIGGER;
		} else {
			CellVoltageDerate.LOG.info(presentVoltage + " is governed by strategy: " + deratingStrategyList.get(findDerateStrategy));
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
			if (searchTerm <= entryVoltage) {
				break;
			}
		}
		return currentIndex - 1;
	}

	public static int getPresentVoltage(String testData) {
		try{
			String strVoltage=testData.split("\\|")[1].trim();
			CellVoltageDerate.LOG.info("strVoltage:{} testData:{}",strVoltage,testData);
			return Integer.parseInt(strVoltage);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return -1;		
	}

	public static void getTestDataFromDatabaseToFiles(ZonedDateTime startTime, ZonedDateTime endTime) {
		String startTimeString=DateTimeHelper.getFormattedTime(startTime.truncatedTo(ChronoUnit.SECONDS), Constants.YYYY_MM_DD_HH_MM_SS);
		String endTimeString=DateTimeHelper.getFormattedTime(endTime.truncatedTo(ChronoUnit.SECONDS), Constants.YYYY_MM_DD_HH_MM_SS);
		for (int arrayIndex = 1; arrayIndex <= CellVoltageDerate.cArrayCount; arrayIndex++) {
			//String testFilePath=CellVoltageDerate.testresultFolderpath+"cvd_"+arrayIndex+".txt";
			String testFilePath = "" ;
			testFilePath = CellVoltageDerate.testresultFolderpath + "cvd_" +arrayIndex +".txt";
			CellVoltageDerate.resultFilePaths.add(arrayIndex -1,testFilePath);
			getDerateTestDataFromDb(arrayIndex,startTimeString,  endTimeString, testFilePath) ;
		}
	}
	
	public static String setResultFileFullPath(int arrayIndex) {
		String testFilePath = "" ;
		testFilePath = CellVoltageDerate.testresultFolderpath + "cvd_" +arrayIndex +".txt";
		CellVoltageDerate.resultFilePaths.add(arrayIndex -1,testFilePath);
		return testFilePath;
	}

	public static List<String> getTestDataFromFile(int arrayIndex) {
		//String testFilePath=CellVoltageDerate.testresultFolderpath+"cvd_"+arrayIndex+".txt";
		String testFilePath=CellVoltageDerate.resultFilePaths.get(arrayIndex-1);
		List<String> testDataFile = FileHelper.readFileToList(testFilePath) ;
		//testDataFile=fileInterpolation(testDataFile);
		return testDataFile;
	}
	
	
	public static List<String> fileInterpolation(List<String> sampleData)  {
		List<List<String>> insertionDataList = new ArrayList<>();
		int testDataVoltage = 0;
		int testDataVoltageNext = 0;
		int testDataVoltageMiddle = 0;
		int testDataVoltagePrev2 = 0;
		String testData = "";
		String testDataNext = "";
		String testDataMiddle = "";
		String testDataPrev2 = "";

		int ascent = 0;
		int numSteps = 3;
		int timeStepSeconds = 4;
		for (int lineNumber = 3; lineNumber < sampleData.size() - 3; lineNumber++) {
			// Check for downward inflection --> get the adjacent pair of lines
			testData = sampleData.get(lineNumber);
			testDataNext = sampleData.get(lineNumber + 1);
			testDataVoltage = getPresentVoltage(testData);
			testDataVoltageNext = getPresentVoltage(testDataNext);

			if ((testDataVoltageNext - testDataVoltage < -5) &&(testDataVoltage<3400)){
				testDataPrev2 = sampleData.get(lineNumber - numSteps);
				testDataVoltagePrev2 =getPresentVoltage(testDataPrev2);// Integer.parseInt(strVoltagePrev2);
				// Get rate of ascent just before inflection
				ascent = testDataVoltage - testDataVoltagePrev2;
				//double slope = (double) ascent / (numSteps * timeStepSeconds);
				// Calculate the ascent that would have happened at the midpoint of the selected
				// lines
				//int ascentThreeQuartersStep = (int) (slope * 6);
				testDataVoltageMiddle = testDataVoltage + ascent;
				LOG.info("Inflection: testDataVoltage:{} testDataVoltageNext:{}", testDataVoltage, testDataVoltageNext);
				// Get mid time
				ZonedDateTime firstDate = DerateCommon.getPresentTime(testData, ArrayDerate2Helper.RESULT_FILE_TIME_INDEX);
				ZonedDateTime midDate = firstDate.plusSeconds(2);
				String midDateStr = DateTimeHelper.getFormattedTime(midDate, Constants.YYYY_MM_DD_HH_MM_SS);
				String[] midData = testData.split("\\|");
				midData[0] = midDateStr;
				midData[1] = String.valueOf(testDataVoltageMiddle);
				testDataMiddle = String.join("|", midData);
				LOG.info("Inflection: midPointVoltage={}, midPointTime= {}, midData:{}", testDataVoltageMiddle,
						midDateStr, testDataMiddle);
				List<String> insertionData = new ArrayList<>();
				insertionData.add(String.valueOf(lineNumber + 1));
				insertionData.add(testDataMiddle);
				insertionDataList.add(insertionData);
			}
		}
		// Insert a line corresponding to the mid-point
		for (int idx = 0, offset = 0; idx < insertionDataList.size(); idx++, offset++) {
			int lineNumber = Integer.parseInt(insertionDataList.get(idx).get(0));
			lineNumber += offset;
			sampleData.add(lineNumber, insertionDataList.get(idx).get(1));
		}
		return sampleData;
	}
}
