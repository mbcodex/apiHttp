package mbTest.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DateTimeHelper {
	public static final String HH_MM_SS = "HH:mm:ss";
	public static final String MM = "mm";
	public static final String MM_SS = "mm:ss";
	public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd' 'HH:mm:ss.SSS";
	public static final String HH_MM_SS_SSS = "HH:mm:ss.SSS";
	public static final String MM_SS_SSS = "mm:ss.SSS";
	public static final String SS_SSS = "ss.SSS";
	public static final String DD_MM_YYYY_HH_MM_SS = "dd-MM-YYYY HH:mm:ss";
	public static final String YYYY_MM_DD_HH_MM = "yyyy/MM/dd HH:mm";
	public static final String YY_MM_DD_HH_MM = "yy/MM/dd HH:mm";
	public static final String HH_MM = "HH:mm";
	public static final Logger LOG = LogManager.getLogger();

	public DateTimeHelper() {
		// TODO Auto-generated constructor stub
	}

	public static String getDayFromDateTime(ZonedDateTime dateTime) {
		return dateTime.getDayOfWeek().toString();
	}

	public static String getCurrentTimeHHmm() {
		return DateTimeHelper.getFormattedTime(ZonedDateTime.now(), HH_MM);
	}

	public static String getTimeZone(String zoneNamePartial) {
		Set<String> zoneIds = ZoneId.getAvailableZoneIds();
		String zoneFullName = "NotFound";
		zoneNamePartial = zoneNamePartial.replaceAll("\\s", "_");// Zone ids replace spaces with underscore, for
																	// example, Los_Angeles
		for (String zone : zoneIds) {
			if (zone.contains(zoneNamePartial)) {
				// System.out.print("found" + zone);
				zoneFullName = zone;
			}
		}
		return zoneFullName;
	}

	public static String getFormattedTime(ZonedDateTime timeAtZone, String pattern) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
		return dateFormatter.format(timeAtZone);
	}

	public static ZonedDateTime getZonedDateTime(String strTime, String strPattern) {
		// Returns ZonedDateTime for the string/pattern
		// If there is no pattern, it is inferred
		// If pattern cannot be inferred it is assumed the string is in a format that
		// can be parsed by ZonedDateTime.parse()
		ZonedDateTime retDateTime = null;
		strPattern = strPattern.contentEquals("") ? DateTimeHelper.inferDateTimePattern(strTime) : strPattern;
		if (strPattern.contentEquals(""))// No pattern can be inferred
			retDateTime = ZonedDateTime.parse(strTime);
		else if (strPattern.contentEquals(HH_MM)) {// HH:mm pattern will assume current day
			retDateTime = ZonedDateTime.now();
			int hour = Integer.parseInt(strTime.split(":")[0]);
			int minute = Integer.parseInt(strTime.split(":")[1]);
			retDateTime = retDateTime.withHour(hour).withMinute(minute);
		} else// Process date time string and pattern
			retDateTime = ZonedDateTime.parse(strTime,
					DateTimeFormatter.ofPattern(strPattern).withZone(ZoneId.systemDefault()));
		return retDateTime;

	}

	public static String inferDateTimePattern(String strDateTime) {
		String strPattern = "";
		if (DateTimeHelper.isDateTime_HH_colon_mm(strDateTime)) {
			strPattern = HH_MM;
		}
		else if (DateTimeHelper.isDateTime_yyyy_fs_MM_fs_dd_space_HH_colon_mm(strDateTime)) {
			strPattern = YYYY_MM_DD_HH_MM;
		}
		return strPattern;
	}

	public static boolean isDateTime_HH_colon_mm(String strDateTime) {
		String strPattern = "^\\d{1,2}:\\d{1,2}$";// HH:mm
		Pattern pattern = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(strDateTime);
		return matcher.find();
	}

	public static boolean isDateTime_yyyy_fs_MM_fs_dd_space_HH_colon_mm(String strDateTime) {
		String strPattern = "^\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}$";//
		Pattern pattern = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(strDateTime);
		return matcher.find();
	}

	public static ZonedDateTime getDynamicZonedDateTime(String zonedDateTime) {
		// returns a ZonedDateTime object corresponding to the zonedDateTime pattern
		// The pattern matches
		// 1. strSourceDateTime - "now" or the zonedDateTime in this format
		// 2021-03-29T12:59:48.856Z[UTC]
		// 2. incrementOperator - "plus" or "minus". This is an optional match. If it
		// does not match the pattern at 1, i.e, strSourceDateTime is returned
		// 3. increment - the amount of time to increment: can be a positive or negative
		// number
		// 4. time unit of the increment/decrement: ms(milliseconds), s (seconds), m
		// (minutes), h (hours)
		Pattern pattern = Pattern.compile("(.*)(plus|minus)([\\+|\\-]*\\d+)(.*)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(zonedDateTime);
		ZonedDateTime retDateTime = null;
		if (matcher.find()) {
			String strSourceDateTime = matcher.group(1);
			String incrementOperator = matcher.group(2);
			String increment = matcher.group(3);
			String timeUnit = matcher.group(4);
			// System.out.println(String.join(",", strSourceDateTime, incrementOperator,
			// increment, timeUnit));
			ZonedDateTime sourceDateTime = null;
			// Gets the source date time
			if (strSourceDateTime.toUpperCase().contentEquals("NOW")) {
				sourceDateTime = DateTimeHelper.getTime("UTC", false);
				// System.out.println("currentDateTime: " + sourceDateTime);
			} else {
				sourceDateTime = getZonedDateTime(strSourceDateTime, "");
			}
			// Increment or decrement
			Long incrementLong;
			incrementLong = Long.valueOf(increment);
			if (incrementOperator.toUpperCase().contains("PLUS"))
				incrementLong = Long.valueOf(increment);
			else
				incrementLong = -Long.valueOf(increment);
			// Get the incremented/decremented date time
			retDateTime = DateTimeHelper.addTimeToZonedDateTime(sourceDateTime, timeUnit, incrementLong);

		} else {
			// There was no increment or decrement
			if (zonedDateTime.toUpperCase().contentEquals("NOW")) {
				retDateTime = ZonedDateTime.now();
			} else
				retDateTime = getZonedDateTime(zonedDateTime, "");
		}
		// System.out.println("incrementedTime: " + retDateTime);
		return retDateTime;
	}

	public static ZonedDateTime addTimeToZonedDateTime(ZonedDateTime sourceDateTime, String timeUnit,
			Long incrementLong) {
		ZonedDateTime retDateTime = null;
		switch (timeUnit.toUpperCase()) {
		case "MS":
		case "MILISECOND":
		case "MILLISECOND":
		case "MILISECONDS":
		case "MILLISECONDS":
			retDateTime = sourceDateTime.plus(incrementLong, ChronoField.MILLI_OF_DAY.getBaseUnit());
			break;
		case "M":
		case "MIN":
		case "MINS":
		case "MINUTES":
		case "MINUTE":
			retDateTime = sourceDateTime.plusMinutes(incrementLong);
			break;
		case "S":
		case "SEC":
		case "SECOND":
		case "SECONDS":
		case "SECS":
			retDateTime = sourceDateTime.plusSeconds(incrementLong);
			break;
		case "H":
		case "HOUR":
		case "HOURS":
			retDateTime = sourceDateTime.plusHours(incrementLong);
			break;
		}
		return retDateTime;
	}

	public static void waitTillNthSecond(int nth_second) {
		ZonedDateTime zdt_startTime = ZonedDateTime.now();
		int second = zdt_startTime.getSecond();
		if (second < nth_second) {
			CommonHelper.quietSleep(1000 * (nth_second - second));
		}
	}

	public static ZonedDateTime getTime(String zoneName, boolean partialZoneName) {
		ZonedDateTime retTime = null;
		Instant nowUtc = Instant.now();
		// System.out.println("UTC: " + nowUtc);
		if (partialZoneName) {
			zoneName = getTimeZone(zoneName);
		}
		if (zoneName != "NotFound") {
			ZoneId newZoneId = ZoneId.of(zoneName);
			// System.out.println("newZoneId: " + newZoneId);
			retTime = ZonedDateTime.ofInstant(nowUtc, newZoneId);
			// System.out.println("Time at zone:" + nowAtZone);
		}
		return retTime;
	}

	public static String getTimePST() {// TO DO - Use ZonedDateTime
		// TODO Auto-generated method stub
		// System.out.println(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		// System.out.println(DateTimeFormatter.ISO_OFFSET_DATE.format(LocalDate.of(2018,
		// 3, 9).atStartOfDay(ZoneId.of("UTC-8"))));
		//
		// LocalDate anotherSummerDay = LocalDate.now();
		// LocalTime anotherTime = LocalTime.now();
		// ZonedDateTime zonedDateTime = ZonedDateTime.of(anotherSummerDay, anotherTime,
		// ZoneId.of("America/New_York"));
		// System.out.println(
		// DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
		// .format(zonedDateTime));
		// System.out.println(
		// DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
		// .format(zonedDateTime));
		// System.out.println(
		// DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
		// .format(zonedDateTime));
		// System.out.println(
		// DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
		// .format(zonedDateTime));
		// return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		String pstPattern = "MM-dd-yyyy hh:mm:ss a";
		DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern(pstPattern);
		System.out.println(europeanDateFormatter.format(LocalDateTime.now().minusHours(8)));
		return europeanDateFormatter.format(LocalDateTime.now().minusHours(8)).toString();

	}

	public static double getTimeDiffHours(String date1, String date2, String sformat) {
		SimpleDateFormat format = new SimpleDateFormat(sformat);
		Date d1;
		Date d2;
		long diffInMillies = 0;
		try {
			d1 = format.parse(date1);
			d2 = format.parse(date2);
			diffInMillies = Math.abs(d2.getTime() - d1.getTime());
		} catch (java.text.ParseException e) {
			LOG.error("Date parse error:", e);
			throw new RuntimeException(e);
		}

		return diffInMillies / 1000 / 60 / 60.0;
	}

	public static int getClosestDate(String date1, String date2, String date3)
			throws java.text.ParseException {
		SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
		Date d1 = format.parse(date1);
		Date d2 = format.parse(date2);
		Date d3 = format.parse(date3);

		long lower_limit_diffInMillies = Math.abs(d3.getTime() - d1.getTime());
		long upper_limitdiffInMillies = Math.abs(d2.getTime() - d3.getTime());

		if (lower_limit_diffInMillies < upper_limitdiffInMillies) {
			System.out.println("Closest to lower limit");
			return -1;
		} else if (lower_limit_diffInMillies > upper_limitdiffInMillies) {
			System.out.println("Closest to upper limit");
			return 1;
		} else {
			System.out.println("Equidistant to both limits");
			return 0;
		}
	}
	
    public static String formatAsLogTimestamp(long milliseconds) {
		Date date = new Date(milliseconds);
		DateFormat format = new SimpleDateFormat(HH_MM_SS_SSS);
		return format.format(date);
    }
    
	public static String convertUtcToLocalDatetime(long utcTime) {
		Date date = new Date(utcTime);
		DateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
		format.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
		return format.format(date);
	}

	public static long convertZonedDateTimeToTimestampMilliseconds(ZonedDateTime zdt) {
		return 1000 * zdt.toEpochSecond();
	}

	public static void main(String arg[]) {
		System.out.println(convertZonedDateTimeToTimestampMilliseconds(ZonedDateTime.now()));
	}

	public static ZonedDateTime[] getBlackOutPeriod(ZonedDateTime eventTime, int blackOutPeriodBeforeEventSeconds,
			int blackOutPeriodAfterEventSeconds) {
		ZonedDateTime blackOutStart = eventTime.minusSeconds(blackOutPeriodBeforeEventSeconds);
		ZonedDateTime blackOutEnd = eventTime.plusSeconds(blackOutPeriodAfterEventSeconds);
		ZonedDateTime[] blackOutPeriod = { blackOutStart, blackOutEnd };
		return blackOutPeriod;
	}

	public static boolean isWithinTimePeriod(ZonedDateTime testTime, ZonedDateTime startTime, ZonedDateTime endTime) {
		return testTime.isAfter(startTime) && testTime.isBefore(endTime);
	}

//	public static boolean isWithinBlackoutPeriod(ZonedDateTime testTime, List<ZonedDateTime[]> blackOutPeriodsList) {
//		// checks to see if the testTime is within any of the black out periods. If it
//		// is in any, return true.
//		boolean isWithin = false;
//		for (ZonedDateTime[] bo : blackOutPeriodsList) {
//			isWithin |= DateTimeHelper.isWithinTimePeriod(testTime, bo[0], bo[1]);
//		}
//		return isWithin;
//	}
	
	public static boolean isWithinBlackoutPeriod(ZonedDateTime testTime, List<ZonedDateTime[]> blackOutPeriodsList) {
		// checks to see if the testTime is within any of the black out periods. If it
		// is in any, return true.
		boolean isWithin = false;
		for (ZonedDateTime[] bo : blackOutPeriodsList) {
			isWithin |= DateTimeHelper.isWithinTimePeriod(testTime, bo[0], bo[1]);
		}
		return isWithin;
	}

	/**
	 * This is specifically for the regression runtime partitioning. Use the Date
	 * class and DateFormat to convert to milliseconds.
	 *
	 * @param time
	 * @return
	 */
	public static long convertToMillis(String time) {
		DateFormat[] dateFormats = { new SimpleDateFormat(MM), new SimpleDateFormat(MM_SS),
				new SimpleDateFormat(HH_MM_SS) };
		int dateformatIndex = time.split(":").length - 1;
		Date reference = new Date();
		Date date = reference;
		try {
			reference = dateFormats[1].parse("00:00:00");
			date = dateFormats[dateformatIndex].parse(time);
		} catch (java.text.ParseException e) {
			throw new RuntimeException(e);
		}
		return (date.getTime() - reference.getTime());
	}
	/**
	 * Parses a Date from a list of acceptable formats.
	 * @param dtclient
	 * @return
	 */
	public static Date parseDate(final String dtclient, final String [] dateFormats) {
		try {
			final Date ret = org.apache.commons.lang3.time.DateUtils.parseDate(dtclient, Locale.US, dateFormats);
			return ret;
		} catch (final Exception e) {
			throw new RuntimeException(String.format("%s is not parsable", dtclient), e);
		}
	}
	
	/**
	 * Convert a time duration in HH:mm:ss.SSS or mm:ss.SSS to seconds. The .SSS is
	 * ignored and may be not be present
	 *
	 * @param time
	 * @return
	 */
	public static long convertToSeconds(String time) {
		return convertToMillis(time) / 1000L;
	}
	
	public static int getZoneOffset() {
		int offset=0;
		String timezone=CommonHelper.getPostgresQueryResults("\"show timezone;\" " ,"").get(2);
		timezone=timezone.trim();
		return offset;
	}
}
