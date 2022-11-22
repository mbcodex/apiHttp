package mbTest.utilities;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.reports.SystemInfo;

public class SimulatedValuesInjector {
	private final static Logger LOG = LogManager.getLogger();
	private final static String simInjectionUrl = getSimInjectionUrl();

	public static String getSimInjectionUrl() {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(Constants.TURTLE_URL).append(Constants.SIMULATED_VALUES_INJECT_URL);
		return urlBuilder.toString().replaceAll("//turtle", "/turtle");
	}

	public static boolean setValue(String componentName, String target, String name, String value) {
		String injectUrl = String.join("/",
				simInjectionUrl + componentName + "%20" + SystemInfo.getStationCode() + target, name, value) + "/";
		return injectCommand(injectUrl);
	}

	private static boolean setValue(String componentName, String name, String value) {
		String injectUrl = String.join("/", simInjectionUrl + componentName, name, value) + "/";
		return injectCommand(injectUrl);
	}

	public static boolean resetToDefault(String componentName, String target, String name) {
		String injectUrl = String.join("/",
				simInjectionUrl + componentName + "%20" + SystemInfo.getStationCode() + target, name) + "/";
		return injectCommand(injectUrl);
	}

	private static boolean resetToDefault(String componentName, String name) {
		String injectUrl = String.join("/", simInjectionUrl + componentName, name) + "/";
		return injectCommand(injectUrl);
	}

	private static boolean injectCommand(String injectUrl) {
		try {
			String result = CommonHelper.getHttpGetResponseString(injectUrl);
			return result.contains("OK");
		} catch (Exception e) {
			LOG.error("", e);
			return false;
		}
	}

	public static boolean setMeterFrequency(BigDecimal frequency, String meterIndex) {
		String target = ":1:" + meterIndex;
		return setValue("BlockMeter", target, "MeterFrequencyHz", frequency.setScale(1).toString());
	}

	public static boolean resetDefaultMeterFrequency(int meterIndex) {
		String target = ":1:" + meterIndex;
		return resetToDefault("BlockMeter", target, "MeterFrequencyHz");
	}

	public static boolean setFD00001Frequency(BigDecimal frequency) {
		LOG.info("Setting FrequencyDroop meter frequency: {}", frequency);
		return setValue("AppFD00001", "FrequencyHz", frequency.setScale(2).toString());
	}

	public static boolean resetDefaultFD00001Frequency() {
		return resetToDefault("AppFD00001", "FrequencyHz");
	}

	public static void main(String[] args) {
		SimulatedValuesInjector.resetDefaultMeterFrequency(2);
	}
}
