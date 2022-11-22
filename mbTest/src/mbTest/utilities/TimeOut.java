package mbTest.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple retry/wait timer
 * This class can also be use for duration timings.
 * Call create with a duration in seconds or with a string tag for an elapsed timer.
 * The duration will be added to the current system time.
 * 
 * 
 * @author RAF
 *
 */
class Timer {
	public long startTime;
	public long endTime;
	Timer() {
		startTime = System.currentTimeMillis();
	}
	Timer(int durationSeconds) {
		this();
		endTime = startTime + Constants.MILLIS_PER_SECOND * durationSeconds;
	}
}

public class TimeOut {
	static Map<String, Timer> timers = new HashMap<>();

	/**
	 * There can be only one! (Highlander)
	 */
	private TimeOut() {
	}

	public static String create(String tag) {
		if (null == timers.get(tag) ) {
		  Timer timer = new Timer();
		  timers.put(tag, timer);
		}
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
		Timer timer = new Timer(secondsDuration);
		timers.put(tag, timer);
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
		ret = timers.isEmpty() 
				|| null == timers.get(tag) 
				|| System.currentTimeMillis() > timers.get(tag).endTime;
		if (ret) {
			remove(tag);
		}
		return ret;
	}

	public static long elapsed(String tag) {
		long elapsedTime = System.currentTimeMillis() - timers.getOrDefault(tag, new Timer(0)).startTime;
		return elapsedTime;
	}
	
	public static long getElapsedAndRemove(String tag) {
		long elapsedTime = elapsed(tag);
		remove(tag);
		return elapsedTime;
	}

	public static boolean remove(String tag) {
		return null != timers.remove(tag);
	}
	
}
