package com.powin.modbusfiles.apps.scheduler;

import static com.powin.modbusfiles.utilities.DateTimeHelper.HH_MM;
import static com.powin.modbusfiles.utilities.DateTimeHelper.YY_MM_DD_HH_MM;
import static com.powin.modbusfiles.utilities.StringUtils.passFail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.dragon.app.scheduler.SchedulerAppFactory;
import com.powin.modbusfiles.configuration.DeviceFileCreator;
import com.powin.modbusfiles.reports.SystemInfo;
import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;
import com.powin.modbusfiles.utilities.DateTimeHelper;
import com.powin.modbusfiles.utilities.FileHelper;
import com.powin.modbusfiles.utilities.Result;



public class SchedulerApp {
	/**
	 * @deprecated Use {@link Constants#YY_MM_DD_HH_MM} instead
	 */
	private final static Logger LOG = LogManager.getLogger();
	final static String cStationCode = SystemInfo.getStationCode();;
	final static int cBlockIndex = SystemInfo.getBlockIndex();

	private static String ruleListSortedByStartTime;
	static String ruleListSortedByEndTime;
	private static String ruleListInPriorityOrder;
	private static String koboldMessageContains = "koboldMessage: %s contains: %s";
	public static String resultFilePaths="/home/powin/";

	// TO DO: message validation and power validation to be consolidated

	public static List<String[]> getActiveSchedules() {
		List<String[]> fullList = new ArrayList<String[]>();
		fullList = SchedulerAppHelper.splitRuleListStringIntoRuleArray(ruleListInPriorityOrder);
		Predicate<String[]> timeDependant = o -> o[4].contentEquals("ABSOLUTE_TIME") == true
				|| o[4].contentEquals("DAY_AND_TIME") == true;
		Predicate<String[]> isInactive = x -> SchedulerAppHelper.isScheduleActive(ZonedDateTime.parse(x[5]),
				ZonedDateTime.parse(x[6]), SchedulerAppHelper.getScheduleDescriptor(x)) == false;
		List<String[]> currentlyInactiveScheduleList = fullList.stream().filter(timeDependant)
				.collect(Collectors.toList());
		currentlyInactiveScheduleList = currentlyInactiveScheduleList.stream().filter(isInactive)
				.collect(Collectors.toList());
		fullList.removeAll(currentlyInactiveScheduleList);
		return fullList;
	}

	public static void setScheduleRules(String strRuleList) {
		// init
		ruleListSortedByStartTime = SchedulerAppHelper.sortRuleListByStartTime(strRuleList);
		ruleListSortedByEndTime = SchedulerAppHelper.sortRuleListByEndTime(strRuleList);
		ruleListInPriorityOrder = SchedulerAppHelper.sortRuleListByPriority(strRuleList);
		String modifiedCommand = SchedulerAppConfig.composeCommand(strRuleList);
		SchedulerAppConfig.sendRulesCommandToschedulerApp(modifiedCommand);
		LOG.info("Sending the following scheduler rules to the system: \n{}",
				SchedulerAppHelper.printableSchedules(ruleListInPriorityOrder));
	}

//	public static Result validateScheduleRules() {
//		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
//		boolean timeNotExpired = true;
//		ZonedDateTime ExpiredTime = SchedulerAppHelper.getExpiredTime();
//		int cycle = 1;
//		List<String[]> ruleList = SchedulerAppHelper.splitRuleListStringIntoRuleArray(ruleListSortedByStartTime);
//		try {
//		while (timeNotExpired) {
//			List<ZonedDateTime[]> blackOutPeriodList = SchedulerAppHelper.getBlackoutPeriodList(ruleList);
//			boolean isWithinBlackoutPeriod = DateTimeHelper.isWithinBlackoutPeriod(ZonedDateTime.now(),
//					blackOutPeriodList);
//			if (!isWithinBlackoutPeriod) {
//				for (String[] rule : ruleList) {
//					rule = Arrays.stream(rule).map(String::trim).toArray(String[]::new);
//					isTestPass.add(validateSchedulesRuleDispatcher(rule));
//				}
//			}
//			if (!isTestPass.isAllTestsPass()) {
//				LOG.info("Cycle# {} , test status={}", cycle, isTestPass);
//			}
//			CommonHelper.quietSleep(15000);
//			timeNotExpired = ZonedDateTime.now().isBefore(ExpiredTime);
//			cycle++;
//		}
//		}
//		catch (Exception e) {
//		  LOG.error("", e);	
//		}
//		return isTestPass;
//	}
	public static Result validateScheduleRules(List<String> testDataFileContents) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		List<String[]> ruleList = SchedulerAppHelper.splitRuleListStringIntoRuleArray(ruleListSortedByStartTime);
		for (int lineNumber = 1; lineNumber < testDataFileContents.size(); lineNumber++) {
			String testData = testDataFileContents.get(lineNumber);
			String koboldMessage=getKoboldMessageFromFile( testData) ;
			for (String[] rule : ruleList) {
				rule = Arrays.stream(rule).map(String::trim).toArray(String[]::new);
				isTestPass.add(validateSchedulesRuleDispatcher(koboldMessage,rule));
			}
		}
		return isTestPass;
	}
	
	public static List<String> triggerScheduleRulesPower() throws IOException {
		boolean timeNotExpired = true;
		ZonedDateTime ExpiredTime = SchedulerAppHelper.getExpiredTime();
		String filePath=resultFilePaths + "scheduler_tests__" + 1000*Math.random() +".txt";
		FileHelper.writeStringToFile(filePath, "");
		//Files.write(Paths.get("/home/powin/schedulerResults.txt"), "".getBytes());
		while (timeNotExpired) {
			List<String[]> currentlyActiveScheduleList = new ArrayList<String[]>();
			currentlyActiveScheduleList = getActiveSchedules();
			String currentTime = DateTimeHelper.getFormattedTime(ZonedDateTime.now(), Constants.YYYY_MM_DD_HH_MM_SS);
			String currentlyActiveScheduleListStr = CommonHelper.convertListOfStringArraysToString(currentlyActiveScheduleList, "|");
			String actualAcPower = String.valueOf(SystemInfo.getActualAcPower());
			String koboldMessage = SystemInfo.getAppStatus(SchedulerAppFactory.APPCODE);
			String maxAllowedChargePower = String.valueOf(SystemInfo.getMaxAllowableChargePower(1));
			String maxAllowedDischargePower = String.valueOf(SystemInfo.getMaxAllowableDischargePower(1));
			String log = String.join(";", currentTime, currentlyActiveScheduleListStr, actualAcPower, koboldMessage,
					maxAllowedChargePower, maxAllowedDischargePower);
			log += "\n";
			if(currentlyActiveScheduleList.size()>0)
			Files.write(Paths.get(filePath), log.getBytes(), StandardOpenOption.APPEND);
			CommonHelper.quietSleep(600);
			timeNotExpired = ZonedDateTime.now().isBefore(ExpiredTime);
		}
		return FileHelper.readFileToList(filePath);
	}

	public static Result validateScheduleRulesPower(List<String> testDataFileContents) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		for (int lineNumber = 1; lineNumber < testDataFileContents.size(); lineNumber++) {
			String testData = testDataFileContents.get(lineNumber);
			List<String[]> currentlyActiveScheduleList = new ArrayList<String[]>();
			currentlyActiveScheduleList = getActiveSchedulesFromFile(testData);
			isTestPass.add(validatePowerBlockingAndStacking(currentlyActiveScheduleList, testData));
		}
		return isTestPass;
	}
	
	public static ZonedDateTime getPresentTime(String testData) {
		String strTime=testData.split("\\;")[0].trim();
		strTime=strTime.replaceAll("\"", "");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.YYYY_MM_DD_HH_MM_SS).withZone(ZoneId.systemDefault());
		ZonedDateTime dateTime = ZonedDateTime.parse(strTime, formatter);
		return dateTime;
}

	private static List<String[]> getActiveSchedulesFromFile(String testData) {
		String strScheduleList=testData.split("\\;")[1].trim();
		strScheduleList=strScheduleList.replaceAll("\"", "");
		return SchedulerAppHelper.splitRuleListStringIntoRuleArray( strScheduleList);
	}
	
	private static int getActualAcPowerFromFile(String testData) {
		String strAcPower=testData.split("\\;")[2].trim();
		strAcPower=strAcPower.replaceAll("\"", "");
		return Integer.parseInt(strAcPower);
	}
	
	private static String getKoboldMessageFromFile(String testData) {
		String strKoboldMessage=testData.split("\\;")[3].trim();
		return strKoboldMessage.replaceAll("\"", "");
	}
	
	private static int getMaxAllowableDischargePowerFromFile(String testData) {
		String strMaxAllowableDischargePower=testData.split("\\;")[5].trim();
		return Integer.parseInt(strMaxAllowableDischargePower.replaceAll("\"", ""));
	}

	private static int getMaxAllowableChargePowerFromFile(String testData) {
		String strMaxAllowableChargePower=testData.split("\\;")[4].trim();
		return Integer.parseInt(strMaxAllowableChargePower.replaceAll("\"", ""));
	}

	private static Result validatePowerBlockingAndStacking(List<String[]> activeScheduleList, String testData) {
		int stackedPower = 0;
		int powerTolerance = 10;
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		stackedPower = SchedulerAppHelper.getExpectedStackedPower(activeScheduleList);
		int actualPower = getActualAcPowerFromFile(testData);
		String koboldMessage = getKoboldMessageFromFile(testData);
		int maxAllowableChargePower = getMaxAllowableChargePowerFromFile(testData);
		int maxAllowableDischargePower = getMaxAllowableDischargePowerFromFile(testData);
		isTestPass.add(validateAppPower(stackedPower, maxAllowableChargePower, maxAllowableDischargePower, actualPower,
				powerTolerance, koboldMessage, "Stacking "));
		if (!isTestPass.isAllTestsPass()) {
			LOG.info("koboldMessage prior to verification:{}", koboldMessage);
		}
		return isTestPass;
	}
	
	

	public static Result validateAppPower(int expectedPower, int maxAllowableChargePower, int maxAllowableDischargePower,int actualPower,int powerTolerance, String koboldMessage,String appendLog) {
		double powerFractionDueToBasicOps = SystemInfo.getPowerFractionFromBasicOpsMessage(koboldMessage);
		expectedPower = (int) (expectedPower * powerFractionDueToBasicOps);
		int maxPermissiblePower = expectedPower < 0 ? -maxAllowableChargePower: maxAllowableDischargePower; 
		int normalizedExpectedPower = 0;
		if (Math.abs(expectedPower) < Math.abs(maxPermissiblePower)) {
			normalizedExpectedPower = expectedPower;
		} else {
			normalizedExpectedPower = maxPermissiblePower;
		}
		// Compare
		Result isTestPass = Result.getInstance(CommonHelper.compareIntegers(actualPower, normalizedExpectedPower, powerTolerance, 0.01), 
				String.format("Comparing %d with expected %d and a tolerance of %d", actualPower, normalizedExpectedPower, powerTolerance));
		if (isTestPass.isPass())
			LOG.info("PASS: {} Expected Power: {}, Actual Power: {},", appendLog, normalizedExpectedPower, actualPower);
		else {
			LOG.info("FAIL: {} Expected Power: {}, Actual Power: {},", appendLog, normalizedExpectedPower, actualPower);
			LOG.info("FAIL: kobold message {} powerFractionDueToBasicOps Power: {}", koboldMessage,powerFractionDueToBasicOps);
		}
		return isTestPass;
	}



	private static Result validateSchedulesAbsoluteTime(String koboldMessage,String actionApp, String priority, String targetP,
			String targetSOC, ZonedDateTime[] startAndEndTimes) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		ZonedDateTime startZDT = startAndEndTimes[0];
		ZonedDateTime endZDT = startAndEndTimes[1];
		String ruleDescription = String.join(":", "ABSOLUTE_TIME", actionApp, priority, targetP, targetSOC,
				startZDT.toString(), endZDT.toString());
		int scheduleStatus = 0;
		scheduleStatus = SchedulerAppHelper.getScheduleStatus(startZDT, endZDT);
		if (scheduleStatus < 0) {
			LOG.info("Schedule {} not yet activated. Current time: {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
		} else if (scheduleStatus > 0) {
			LOG.info("Schedule {} has expired at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
		} else {
		//	String koboldMessage = logAppStatus();
			LOG.info("Schedule {} is running at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
			String formattedStartTime = DateTimeHelper.getFormattedTime(startZDT, YY_MM_DD_HH_MM);
			String formattedEndTime = DateTimeHelper.getFormattedTime(endZDT, YY_MM_DD_HH_MM);
			isTestPass.add(koboldMessage.contains(targetP), "message doesn't contain targetP");
			isTestPass.add(koboldMessage.contains(targetSOC), "message doesn't contain targetSOC");
			isTestPass.add(koboldMessage.contains("Between " + formattedStartTime + " and " + formattedEndTime), "The dates don't have the same dates.");
			LOG.info("{}: targetP={} , targetSOC={} , Between {} and {},", passFail(isTestPass),
					 targetP, targetSOC, formattedStartTime, formattedEndTime);
		}
		return isTestPass;
	}

	private static Result validateSchedulesAbsoluteTimeDmg(String koboldMessage,String actionApp, String dct,
			ZonedDateTime[] startAndEndTimes) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		ZonedDateTime startZDT = startAndEndTimes[0];
		ZonedDateTime endZDT = startAndEndTimes[1];
		String ruleDescription = String.join(":", "ABSOLUTE_TIME", actionApp, dct, startZDT.toString(),
				endZDT.toString());
		int scheduleStatus = 0;
		scheduleStatus = SchedulerAppHelper.getScheduleStatus(startZDT, endZDT);
		if (scheduleStatus < 0) {
			LOG.info("Schedule {} not yet activated. Current time: {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
		} else if (scheduleStatus > 0) {
			LOG.info("Schedule {} has expired at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
		} else {
			//String koboldMessage = logAppStatus();
			LOG.info("Schedule {} is running at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
			String formattedStartTime = DateTimeHelper.getFormattedTime(startZDT, YY_MM_DD_HH_MM);
			String formattedEndTime = DateTimeHelper.getFormattedTime(endZDT, YY_MM_DD_HH_MM);
			isTestPass.add(koboldMessage.contains("Demand Charge Threshold set to"), koboldMessage);
			isTestPass.add(koboldMessage.contains("Between " + formattedStartTime + " and " + formattedEndTime), koboldMessage);
			LOG.info("{}: Between {} and {},", passFail(isTestPass), formattedStartTime, formattedEndTime);
		}
		return isTestPass;
	}

	private static Result validateSchedulesAlwaysTrue(String koboldMessage, String... params) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE); 
		String actionApp = params[0];
		if (actionApp.toUpperCase().contentEquals("BASICOP")) {
			String priority = params[1];
			String targetP = params[2];
			String targetSOC = params[3];
			String ruleDescription = String.join(":", "ALWAYS_TRUE", actionApp, priority, targetP, targetSOC);
			CommonHelper.quietSleep(Constants.TEN_SECONDS);
			//String koboldMessage = logAppStatus();
			boolean verifyKoboldMessage = koboldMessage.contains(targetP);
			String logMessage = String.format(koboldMessageContains, koboldMessage, targetP);
			isTestPass.add(verifyKoboldMessage, logMessage);
			verifyKoboldMessage = koboldMessage.contains(targetSOC);
			isTestPass.add(verifyKoboldMessage, String.format(koboldMessageContains, koboldMessage, targetSOC));
			isTestPass.add(koboldMessage.contains("Always"), "Always is present in the app status.");
			LOG.info("Schedule {} is running at {}", ruleDescription, ZonedDateTime.now().toString());
			LOG.info("{}: targetP={} , targetSOC={} , Always={},", passFail(isTestPass),targetP, targetSOC,
						koboldMessage.contains("Always"));
		} else if (actionApp.toUpperCase().contentEquals("DEMANDMGMT")) {
			String dct = params[1];
			String ruleDescription = String.join(":", "ALWAYS_TRUE", actionApp, dct);
			CommonHelper.quietSleep(Constants.TEN_SECONDS);
			//String koboldMessage = logAppStatus();
			isTestPass.add(koboldMessage.contains("Always"), "Always is present in the app status.");
			LOG.info("Schedule {} is running at {}", ruleDescription, ZonedDateTime.now().toString());
			LOG.info("{}:Always={},", passFail(isTestPass), koboldMessage.contains("Always"));
		}
		return isTestPass;
	}

	private static Result validateSchedulesDayAndTime(String koboldMessage,String actionApp, String priority, String targetP,
			String targetSOC, ZonedDateTime[] startAndEndTimes) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		ZonedDateTime startZDT = startAndEndTimes[0];
		ZonedDateTime endZDT = startAndEndTimes[1];
		String ruleDescription = String.join(":", "DAY_AND_TIME", actionApp, priority, targetP, targetSOC,
				DateTimeHelper.getFormattedTime(startZDT, HH_MM), DateTimeHelper.getFormattedTime(endZDT, HH_MM));
		int scheduleStatus = 0;
		scheduleStatus = SchedulerAppHelper.getScheduleStatus(startZDT, endZDT);
		if (scheduleStatus < 0) {
			LOG.info("Schedule {} not yet activated. Current time: {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
		} else if (scheduleStatus > 0) {
			LOG.info("Schedule {} has expired at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
		} else {
			//String koboldMessage = logAppStatus();
			LOG.info("Schedule {} is running at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM));
			String formattedStartTime = DateTimeHelper.getFormattedTime(startZDT, HH_MM);
			String formattedEndTime = DateTimeHelper.getFormattedTime(endZDT, HH_MM);
			String day = DateTimeHelper.getDayFromDateTime(startZDT);
			day = com.powin.modbusfiles.utilities.StringUtils.stringToTitleCase(day);
			isTestPass.add(koboldMessage.contains(targetP), String.format(koboldMessageContains, koboldMessage, targetP));
			isTestPass.add(koboldMessage.contains(targetSOC), String.format(koboldMessageContains, koboldMessage, targetSOC));
			isTestPass.add(koboldMessage.contains("On " + day), String.format(koboldMessageContains, koboldMessage, "On " + day));
			isTestPass.add(koboldMessage.contains("Between " + formattedStartTime + " and " + formattedEndTime),
					       String.format(koboldMessageContains, koboldMessage, "Between " + formattedStartTime + " and " + formattedEndTime));
			LOG.info("{}: targetP={} , targetSOC={} , On ={}, Between {} and {},", passFail(isTestPass), targetP, targetSOC, day,
						formattedStartTime, formattedEndTime);
		}
		return isTestPass;
	}

	private static Result validateSchedulesDayAndTimeDmg(String koboldMessage,String actionApp, ZonedDateTime[] startAndEndTimes) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		ZonedDateTime startZDT = startAndEndTimes[0];
		ZonedDateTime endZDT = startAndEndTimes[1];
		String timeFormat = HH_MM;
		String ruleDescription = String.join(":", "DAY_AND_TIME", actionApp,
				DateTimeHelper.getFormattedTime(startZDT, timeFormat), DateTimeHelper.getFormattedTime(endZDT, timeFormat));
		int scheduleStatus = 0;
		scheduleStatus = SchedulerAppHelper.getScheduleStatus(startZDT, endZDT);
		if (scheduleStatus < 0) {
			LOG.info("Schedule {} not yet activated. Current time: {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), timeFormat));
		} else if (scheduleStatus > 0) {
			LOG.info("Schedule {} has expired at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), timeFormat));
		} else {
			//String koboldMessage = logAppStatus();
			LOG.info("Schedule {} is running at {}", ruleDescription,
					DateTimeHelper.getFormattedTime(ZonedDateTime.now(), timeFormat));
			String formattedStartTime = DateTimeHelper.getFormattedTime(startZDT, timeFormat);
			String formattedEndTime = DateTimeHelper.getFormattedTime(endZDT, timeFormat);
			String day = DateTimeHelper.getDayFromDateTime(startZDT);
			day = com.powin.modbusfiles.utilities.StringUtils.stringToTitleCase(day);
			isTestPass.add(koboldMessage.contains("On " + day), String.format(koboldMessageContains, koboldMessage, "On " + day));
			isTestPass.add(koboldMessage.contains("Between " + formattedStartTime + " and " + formattedEndTime),
				       String.format(koboldMessageContains, koboldMessage, "Between " + formattedStartTime + " and " + formattedEndTime));
			isTestPass.add(koboldMessage.contains("Demand Charge Threshold set to"), String.format(koboldMessageContains, koboldMessage, "Demand Charge Threshold set to"));

			LOG.info("{}: On ={}, Between {} and {}", passFail(isTestPass), day, formattedStartTime, formattedEndTime);
		}
		return isTestPass;
	}

	private static String logAppStatus() {
		String koboldMessage = SystemInfo.getAppStatus(SchedulerAppFactory.APPCODE);
		LOG.info("koboldMessage prior to verification:{}", koboldMessage);
		return koboldMessage;
	}

	private static Result validateSchedulesRuleDispatcher(String koboldMessage,String[] params) {
		String actionApp = params[0];
		String priority = "";
		String dct = "";
		String targetP = params[2];
		String targetSOC = params[3];
		String conditionType = params[4];
		String startTime = params[5];
		String endTime = params[6];
		String flagLabel = params[8];
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);

		ZonedDateTime[] startAndEndTime = null;
		if (actionApp.toUpperCase().contentEquals("BASICOP")) {
			priority = params[1];
			switch (conditionType.toUpperCase()) {
			case "ALWAYS_TRUE":
				isTestPass.add(validateSchedulesAlwaysTrue(koboldMessage,"", actionApp, priority, targetP, targetSOC));
				break;
			case "ABSOLUTE_TIME":
				startAndEndTime = SchedulerAppHelper.getZonedDateTimeArray(startTime, endTime);
				isTestPass.add(validateSchedulesAbsoluteTime(koboldMessage,actionApp, priority, targetP, targetSOC, startAndEndTime));
				break;
			case "DAY_AND_TIME":
				startAndEndTime = SchedulerAppHelper.getZonedDateTimeArray(startTime, endTime);
				isTestPass.add(validateSchedulesDayAndTime(koboldMessage,actionApp, priority, targetP, targetSOC, startAndEndTime));
				break;
			case "SCHEDULER_FLAG":
				isTestPass.add(validateSchedulesSchedulerFlag(koboldMessage,actionApp, priority, targetP, targetSOC, flagLabel));
				break;
			}
		} else if (actionApp.toUpperCase().contentEquals("DEMANDMGMT")) {
			dct = params[1];
			switch (conditionType.toUpperCase()) {
			case "ALWAYS_TRUE":
				isTestPass = validateSchedulesAlwaysTrue(koboldMessage,"", actionApp, dct);
				break;
			case "ABSOLUTE_TIME":
				startAndEndTime = SchedulerAppHelper.getZonedDateTimeArray(startTime, endTime);
				isTestPass = validateSchedulesAbsoluteTimeDmg(koboldMessage,actionApp, dct, startAndEndTime);
				break;
			case "DAY_AND_TIME":
				startAndEndTime = SchedulerAppHelper.getZonedDateTimeArray(startTime, endTime);
				isTestPass = validateSchedulesDayAndTimeDmg(koboldMessage,actionApp, startAndEndTime);
				break;
			case "SCHEDULER_FLAG":
				isTestPass = validateSchedulesSchedulerFlagDmg(koboldMessage,actionApp, flagLabel);
				break;
			}
		}
		return isTestPass;
	}

	private static Result validateSchedulesSchedulerFlagDmg(String koboldMessage,String actionApp, String flagLabel) {
		LOG.info("validateScheduleSchedulerFlagDmg - actionApp: {}, flagLabel: {}", actionApp, flagLabel);
		CommonHelper.quietSleep(Constants.TEN_SECONDS);
		String ruleDescription = String.join(":", "SCHEDULER_FLAG", actionApp);
		LOG.info("Schedule {} is running at {}", ruleDescription, ZonedDateTime.now().toString());
		return validateKoboldMessage(koboldMessage,flagLabel);		
	}

	private static Result validateSchedulesSchedulerFlag(String koboldMessage,String actionApp, String priority, String targetP,
			String targetSOC, String flagLabel) {
		LOG.info("validateScheduleSchedulerFlagDmg - actionApp: {}, priority: {}, targetP: {}, targetSOC: {}, flagLabel: {}", actionApp, priority, targetP, targetSOC, flagLabel);
		CommonHelper.quietSleep(Constants.TEN_SECONDS);
		String ruleDescription = String.join(":", "SCHEDULER_FLAG", actionApp, priority, targetP, targetSOC);
		LOG.info("Schedule {} is running at {}", ruleDescription, ZonedDateTime.now().toString());
        return validateKoboldMessage(koboldMessage,targetP, targetSOC, flagLabel); 
	}

	private static Result validateKoboldMessage(String koboldMessage ,String flagLabel) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		//String koboldMessage = logAppStatus();
		LOG.info("Scheduler app message in Kobold: {}", koboldMessage);
		boolean isFlagLabelPresent = koboldMessage.contains(flagLabel);
		boolean isDemandChargeThresholdPresent = koboldMessage.contains("Demand Charge Threshold set to");
		isTestPass.add(isFlagLabelPresent, "");
		isTestPass.add(isDemandChargeThresholdPresent, "");
		LOG.info("{} SCHEDULER_FLAG: label ={}", passFail(isTestPass), isFlagLabelPresent);
        return isTestPass;
	}

    private static Result validateKoboldMessage(String koboldMessage ,String targetP, String targetSOC, String flagLabel) {
		Result isTestPass = Result.getInstance(Constants.INITIAL_PASS_STATE_TRUE);
		//String koboldMessage = logAppStatus();
		LOG.info("Scheduler app message in Kobold: {}", koboldMessage);
		boolean isTargetPPresent = koboldMessage.contains(targetP);
		boolean isTargetSOCPresent = koboldMessage.contains(targetSOC);
		boolean isFlagLabelPresent = koboldMessage.contains(flagLabel);
		isTestPass.add(isTargetPPresent, koboldMessage);
		isTestPass.add(isTargetSOCPresent, koboldMessage);
		isTestPass.add(isFlagLabelPresent, koboldMessage);
		LOG.info("{} SCHEDULER_FLAG:targetP={} , targetSOC={} , label ={}", passFail(isTestPass), isTargetPPresent,
				isTargetSOCPresent, isFlagLabelPresent);
		return isTestPass; 
    }

	
 
	public void disable() {
		setAppStatus(false);
	}

	public void enable() {
		setAppStatus(true);
	}

	public void setAppStatus(boolean status) {
		List<Rules> ruleList = new ArrayList<Rules>();
		SchedulerAppConfigRules schedulerAppConfigRules1 = new SchedulerAppConfigRules();
		schedulerAppConfigRules1.init(ruleList);
		SchedulerAppConfig schedAppConfig = new SchedulerAppConfig();
		schedAppConfig.init("default", 0, status, schedulerAppConfigRules1, "UTC");
		DeviceFileCreator creator = new DeviceFileCreator();
		String command = creator.createDeviceJsonFile("/home/powin/scheduleAppConfig.json", schedAppConfig);
		String modifiedCommand = SchedulerAppConfig.modifyRulesCommand(command);
		SchedulerAppConfig.sendRulesCommandToschedulerApp(modifiedCommand);
	}

}
