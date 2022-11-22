package com.powin.modbusfiles.apps.scheduler;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.DateTimeHelper;

public class SchedulerAppHelper {
	private static final int WAIT_AFTER_TIME_INDEPENDANT_TESTS_SECONDS = 2 * 60;
	private static final int WAIT_MESSAGE_APPEAR_SECONDS = 0;
	private static final int WAIT_MESSAGE_DISAPPEAR_SECONDS = 0;
	public static final int IGNORE_BEFORE_EVENT_SECONDS = 25;
	public static final int IGNORE_AFTER_EVENT_SECONDS = 25;

	public static String sortRuleListByEndTime(String strRuleList) {
		List<String[]> ruleList = SchedulerAppHelper.splitRuleListStringIntoRuleArray(strRuleList);
		List<String[]> sortedList = new ArrayList<>();
		sortedList.addAll(SchedulerAppHelper.getTimeIndependantRules(ruleList));
		sortedList.addAll(SchedulerAppHelper.getTimeDependantRules(ruleList, 6));
		return CommonHelper.convertListOfStringArraysToString(sortedList, "|");
	}

	public static String sortRuleListByStartTime(String strRuleList) {
		List<String[]> ruleList = SchedulerAppHelper.splitRuleListStringIntoRuleArray(strRuleList);
		List<String[]> sortedList = new ArrayList<>();
		sortedList.addAll(SchedulerAppHelper.getTimeIndependantRules(ruleList));
		sortedList.addAll(SchedulerAppHelper.getTimeDependantRules(ruleList, 5));
		return CommonHelper.convertListOfStringArraysToString(sortedList, "|");
	}

	public static List<String[]> splitRuleListStringIntoRuleArray(String strRuleList) {
		List<String[]> ruleList = new ArrayList<>();
		for (String rule : strRuleList.split("\\|")) {
			String[] ruleParameters = rule.split(",", -8);
			ruleParameters = Arrays.stream(ruleParameters).map(String::trim).toArray(String[]::new);
			ruleList.add(ruleParameters);
		}
		return ruleList;
	}

	public static boolean isEffectivePriorityOfActiveSchedulesSOC(List<String[]> activeScheduleList) {
		return activeScheduleList.get(0)[1].toUpperCase().contentEquals("SOC");
	}

	public static List<String[]> getTimeIndependantRules(List<String[]> ruleList) {
		List<String[]> tempList = new ArrayList<>();
		tempList = ruleList.stream().filter(o -> o[4].contains("ALWAYS_TRUE") || o[4].contains("SCHEDULER_FLAG"))
				.collect(Collectors.toList());
		return tempList;
	}

	public static List<String[]> getTimeDependantRules(List<String[]> ruleList, int sortFieldIndex) {
		List<String[]> tempList = new ArrayList<>();
		tempList = ruleList.stream()
				// .filter(o -> o[4].equals("ABSOLUTE_TIME") || o[4].equals("DAY_AND_TIME"))
				.filter(o -> o[4].contains("ABSOLUTE_TIME") || o[4].contains("DAY_AND_TIME"))
				.map(x -> 	{
								return SchedulerAppHelper.convertDynamicStringsToZdtStrings(x);
							}
				)
				.sorted((d1, d2) -> ZonedDateTime.parse(d1[sortFieldIndex])
									.compareTo(ZonedDateTime.parse(d2[sortFieldIndex]))
						)
				.collect(Collectors.toList());
		return tempList;
	}

	public static List<ZonedDateTime> getMileStones(List<String[]> ruleList) {
		List<String[]> timeDependenttRules = getTimeDependantRules(ruleList, 5);
		List<ZonedDateTime> retList = new ArrayList<>();
		for (String[] rule : timeDependenttRules) {
			retList.add(ZonedDateTime.parse(rule[5]).truncatedTo(ChronoUnit.MINUTES));//Why truncating
			retList.add(ZonedDateTime.parse(rule[6]).truncatedTo(ChronoUnit.MINUTES));
		}
		return retList;
	}

	public static List<ZonedDateTime[]> getBlackoutPeriodList(List<String[]> ruleList) {
		List<ZonedDateTime> mileStoneList = new ArrayList<>();
		mileStoneList = getMileStones(ruleList);
		List<ZonedDateTime[]> retList = new ArrayList<>();
		for (ZonedDateTime mileStone : mileStoneList) {
			retList.add(DateTimeHelper.getBlackOutPeriod(mileStone, IGNORE_BEFORE_EVENT_SECONDS,
					IGNORE_AFTER_EVENT_SECONDS));
		}
		return retList;
	}

	public static int getTargetSocStackedPower(List<String[]> activeScheduleList) {
		int stackedPower;
		List<String[]> targetSocScheduleList = SchedulerAppHelper.getTargetSocScheduleList(activeScheduleList);
		stackedPower = Integer.parseInt(targetSocScheduleList.get(0)[2]);
		return stackedPower;
	}

	public static int getExpectedStackedPower(List<String[]> activeScheduleList) {
		int stackedPower;
		if (isEffectivePriorityOfActiveSchedulesSOC(activeScheduleList)) {
			stackedPower = getTargetSocStackedPower(activeScheduleList);
		} else {
			stackedPower = SchedulerAppHelper.getTargetPStackedPower(activeScheduleList);
		}
		return stackedPower;
	}

	public static ZonedDateTime getExpiredTime() {
		ZonedDateTime ExpiredTime = SchedulerAppHelper.getLatestEndTime();
		if (ExpiredTime != null)
			ExpiredTime = ExpiredTime.plusSeconds(0);
		else
			ExpiredTime = ZonedDateTime.now().plusSeconds(WAIT_AFTER_TIME_INDEPENDANT_TESTS_SECONDS);
		return ExpiredTime;
	}
//@formatter:off
	public static int getTargetPStackedPower(List<String[]> activeScheduleList) {
		int stackedPower;
		final List<String[]> list = activeScheduleList;
		stackedPower = list.stream()
				.filter(o -> o[1].toUpperCase().contentEquals("POWER"))
				.filter(o -> Integer.parseInt(list.get(0)[2]) < 0 ? Integer.parseInt(o[2]) < 0 : Integer.parseInt(o[2]) > 0)
				.collect(Collectors.summingInt(p -> Integer.parseInt(p[2])));
		return stackedPower;
	}

	public static List<String[]> getTargetSocScheduleList(List<String[]> activeScheduleList) {
		List<String[]> targetSocScheduleList = new ArrayList<>();
		targetSocScheduleList.addAll(activeScheduleList);
		targetSocScheduleList = targetSocScheduleList.stream()
				.filter(o -> o[1].toUpperCase().contentEquals("SOC"))
				.collect(Collectors.toList());
		return targetSocScheduleList;
	}
	//@formatter:on
	public static String[] convertDynamicStringsToZdtStringsFormatted(String[] ruleList) {
		String[] ruleList1 = ruleList;
		if (ruleList.length > 5) {
			String startTime = ruleList1[5];
			if (!startTime.contentEquals("")) {
				ZonedDateTime zdt_startTime = DateTimeHelper.getDynamicZonedDateTime(startTime);
				ruleList1[5] = DateTimeHelper.getFormattedTime(zdt_startTime, "HH:mm");
			}
			String endTime = ruleList1[6];
			if (!endTime.contentEquals("")) {
				ZonedDateTime zdt_endTime = DateTimeHelper.getDynamicZonedDateTime(endTime);
				ruleList1[6] = DateTimeHelper.getFormattedTime(zdt_endTime, "HH:mm");
			}
		}
		return ruleList1;
	}

	public static String[] convertDynamicStringsToZdtStrings(String[] ruleList) {
		String startTime = ruleList[5];
		if (!startTime.contentEquals("")) {
			ZonedDateTime zdt_startTime = DateTimeHelper.getDynamicZonedDateTime(startTime);
			ruleList[5] = zdt_startTime.toString();
		}
		String endTime = ruleList[6];
		if (!startTime.contentEquals("")) {
			ZonedDateTime zdt_endTime = DateTimeHelper.getDynamicZonedDateTime(endTime);
			ruleList[6] = zdt_endTime.toString();
		}
		return ruleList;
	}

	static ZonedDateTime getLatestEndTime() {
		List<String[]> ruleListSortedByEndTime1 = splitRuleListStringIntoRuleArray(
				SchedulerApp.ruleListSortedByEndTime);
		String strLastEndTime = "";
		strLastEndTime = (ruleListSortedByEndTime1.get(ruleListSortedByEndTime1.size() - 1))[6];
		ZonedDateTime endTimeZDT = null;
		if (!strLastEndTime.contentEquals(""))
			endTimeZDT = DateTimeHelper.getDynamicZonedDateTime(strLastEndTime);
		return endTimeZDT;
	}

	static String getScheduleDescriptor(String[] schedule) {
		return String.join("_", schedule[0], schedule[1], schedule[2]);
	}

	static ZonedDateTime[] getZonedDateTimeArray(String startTime, String endTime) {
		ZonedDateTime startTimeZDT = ZonedDateTime.parse(startTime);
		ZonedDateTime endTimeZDT = ZonedDateTime.parse(endTime);
		ZonedDateTime[] zonedDateTimeArray = { startTimeZDT, endTimeZDT };
		return zonedDateTimeArray;
	}

	static boolean isScheduleActive(ZonedDateTime startZDT, ZonedDateTime endZDT, String scheduleDescriptor) {
		return SchedulerAppHelper.getScheduleStatus(startZDT, endZDT) == 0;
	}

	public static int getScheduleStatus(ZonedDateTime startZDT, ZonedDateTime endZDT) {
		int status = 0;
		startZDT = startZDT.truncatedTo(ChronoUnit.MINUTES).plusSeconds(WAIT_MESSAGE_APPEAR_SECONDS);
		endZDT = endZDT.truncatedTo(ChronoUnit.MINUTES).plusSeconds(WAIT_MESSAGE_DISAPPEAR_SECONDS);
		ZonedDateTime currentTime = ZonedDateTime.now();
		if (currentTime.isBefore(startZDT)) {
			status = -1;
		} else if (currentTime.isAfter(endZDT) || currentTime.isEqual(endZDT)) {
			status = 1;
		}
		return status;
	}

	static String printableSchedules(String strRuleList) {
		List<String[]> ruleList = splitRuleListStringIntoRuleArray(strRuleList);
		ruleList = ruleList.stream().map(x -> {
			return convertDynamicStringsToZdtStringsFormatted(x);
		}).collect(Collectors.toList());
		return CommonHelper.convertListOfStringArraysToString(ruleList, "\n");
	}

	static String sortRuleListByPriority(String strRuleList) {
		List<String[]> ruleList = splitRuleListStringIntoRuleArray(strRuleList);
		ruleList = ruleList.stream().map(x -> {
			return convertDynamicStringsToZdtStrings(x);
		}).collect(Collectors.toList());
		return CommonHelper.convertListOfStringArraysToString(ruleList, "|");
	}

}
