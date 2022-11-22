package mbTest.utilities;

import java.util.HashMap;
import java.util.Map;

public class OverrideableTimer {
	static Map<String, Long> translateTo = new HashMap<>();

	public static void sleep(String label, long ms) {
		if (null == translateTo.get(label)) {
			CommonHelper.sleep(ms);
		} else {
			CommonHelper.sleep(translateTo.get(label));
		}
	}

	public static void setOverride(String label, long ms) {
		translateTo.computeIfPresent(label, (key, val) -> ms);
	}
}
