package mbTest.utilities;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.powin.modbusfiles.derating.ArrayDerate2Helper;

/**
 * Simple retry/wait timer
 *
 * String timer = TimeOutX.create(seconds); while(!TimeOutX.isExpired(timer)) { //
 * do work break on condition. CommonHelper.quietSleep(1000); }
 *
 * @author RAF
 *
 */
public class TimeOutX {
	static Map<String, Long> timers = new HashMap<>();
	static Map<String, Long> durations = new HashMap<>();
	
	static String timerStartedArray = "false,false,false,false";
	static String timerStopInstantArray = ",,,";

	/**
	 * There can be only one!
	 */
	private TimeOutX() {
	}

	public static String create(String tag) {
		timers.put(tag, System.currentTimeMillis());
		return tag;
	}

	/**
	 * Creates the timer entry.
	 *
	 * @param secondsDuration
	 * @return id of the timer.
	 */
	public static String create(int secondsDuration) {
		String tag = UUID.randomUUID().toString();
		int millisecondsDuration = Constants.MILLIS_PER_SECOND * secondsDuration;
		long endTime = 0 == millisecondsDuration ? 0 : System.currentTimeMillis() + millisecondsDuration;
		timers.put(tag, endTime);
		return tag;
	}

	/**
	 * Check for expiration.
	 *
	 * @param tag
	 * @return true if expired.
	 */
	public static boolean isExpired(String tag) {
		boolean ret = false;
		if ( 0 != timers.getOrDefault(tag, 0L) ) {
			ret = timers.isEmpty() || null == timers.get(tag) || System.currentTimeMillis() > timers.get(tag);
			if (ret) {
				timers.remove(tag);
			}
		}
		return ret;
	}

	public static long elapsed(String tag) {
		return System.currentTimeMillis() - timers.getOrDefault(tag, 0L);
	}
	
	public static long elapsedOnce(String tag) {
		long elapsedTime = elapsed(tag);
		//durations.put(tag, elapsedTime);
		timers.remove(tag);
		return elapsedTime;
	}
	
	//Copied from AD2
	public static boolean timerExpired(int arrayIndex, ZonedDateTime presentTime) {
		if (timerStopInstantArray.split(",", -8)[arrayIndex - 1].contentEquals("")) {
			ArrayDerate2Helper.editEnergyCounterArray(ZonedDateTime.now(), arrayIndex);
		}
		ZonedDateTime newTimerStopInstant = ArrayDerate2Helper.getEnergyCounterStopInstant(arrayIndex);
		return presentTime.isAfter(newTimerStopInstant);
	}
	
	public static void startEnergyCounter(String strategy, int arrayIndex) {
		ArrayDerate2Helper.editEnergyCounterStatus(arrayIndex, true);
		int timerDurationSeconds = getExitEnergyFromStrategy(strategy) / 1000;
		ZonedDateTime currentTime = ZonedDateTime.now();
		ZonedDateTime endTime = currentTime.plusSeconds(timerDurationSeconds);
		ArrayDerate2Helper.editEnergyCounterArray(endTime, arrayIndex);
	}

	private static int getExitEnergyFromStrategy(String strategy) {
		// TODO Auto-generated method stub
		return 0;
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

}
