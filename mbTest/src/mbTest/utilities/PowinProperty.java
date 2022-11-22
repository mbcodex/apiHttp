package mbTest.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum PowinProperty {
	//@formatter:off
	ARCHIVA_PASSWORD("archiva_password"),
	ARCHIVA_USER("archiva_user"),
	ARRAY_INDEX("array_index"),
	ARRAY_INDEXES("array_indexes"),
	BATTERY_PACK_COUNT("battery_pack_count"),
	BC_ARRAY_REPORT_CSV_FILE_PATH("bc_array_report_csv_file_path"),
	BC_CELL_GROUP_REPORT_MINUTE_CSV_FILE_PATH("bc_cell_group_report_minute_csv_file_path"),
	BC_CELL_GROUP_REPORT_SECOND_CSV_FILE_PATH("bc_cell_group_report_second_csv_file_path"),
	BC_CHARGINGPOWERASPCT("bc_chargingPowerAsPct"),
	BC_CHARGINGPOWERW("bc_chargingPowerW"),
	BC_DISCHARGINGPOWERASPCT("bc_dischargingPowerAsPct"),
	BC_DISCHARGINGPOWERW("bc_dischargingPowerW"),
	BC_LOGINTERVAL("bc_logInterval"),
	BC_MAXCYCLES("bc_maxCycles"),
	BC_POWERCYCLEPERIODSECONDS("bc_powerCyclePeriodSeconds"),
	BC_RESTPERIODSECONDS("bc_restPeriodSeconds"),
	BC_SEL735("bc_sel735"),
	BC_STRING_REPORT_CSV_FILE_PATH("bc_string_report_csv_file_path"),
	BC_TARGETCHARGESOC("bc_targetChargeSOC"),
	BC_TARGETCHARGEVOLTAGE("bc_targetChargeVolresourcestage"),
	BC_TARGETDISCHARGESOC("bc_targetDischargeSOC"),
	BC_TARGETDISCHARGEVOLTAGE("bc_targetDischargeVoltage"),
	CELL_GROUP_COUNT("cell_group_count"),
	CLOUDHOST("cloudHost"),
	DATABASE_PASSWORD("database_password"),
	DATABASE_URL("database_url"),
	DATABASE_USER("database_user"),
	DEFAULT_APP_CHECKSUMS("default_app_checksums"),
	DEFAULT_CONFIGURATION_CHECKSUM("default_configuration_checksum"),
	DEFAULT_DEVICE_CHECKSUMS("default_device_checksums"),
	DRAGON_ID("dragon_id"),
	FIRMWARE_REPORTS_URL("firmware_reports_url"),
	INITIAL_APPS("initial_apps"),
	INITIAL_DEVICES("initial_devices"),
	KOBOLD_URL("kobold_url"),
	MARVAIR_COMM_STAT_HOST("marvair_comm_stat_host"),
	MAXPERCENTAGEOFPOWER("maxPercentageOfPower"),
	METER_INDEX("meter_index"),
	NOTIFICATIONS_BASE_URL("notifications_base_url"),
	NOTIFICATION_REPORT_PATH("notification_report_path"),
	OPEN_ALL_CONTACTORS_WITH("open_all_contactors_with"),
	OPEN_CONTACTORS_WITH("open_contactors_with"),
	PCS_INDEX("pcs_index"),
	PHOENIX_COMMAND_INJECT_URL("phoenix_command_inject_url"),
	REDIS_HOST("redis_host"),
	REDIS_PORT("redis_port"),
	REPORTFOLDER("reportFolder"),
	REPORT_BASE_URL("report_base_url"),
	RTE_CHARGINGPOWERASPCT("rte_chargingPowerAsPct"),
	RTE_CSV_FILE_PATH("rte_csv_file_path"),
	RTE_DISCHARGINGPOWERASPCT("rte_dischargingPowerAsPct"),
	RTE_MAXCYCLES("rte_maxCycles"),
	RTE_RESTPERIODSECONDS("rte_restPeriodSeconds"),
	RTE_TARGETCHARGEVOLTAGE("rte_targetChargeVoltage"),
	RTE_TARGETDISCHARGEVOLTAGE("rte_targetDischargeVoltage"),
	SLACK_CHANNEL_ID("slack_channel_id"),
	SLACK_CHANNEL_NAME("slack_channel_name"),
	STANDARD_REPORT_PATH("standard_report_path"),
	STATION_ID("stationID"),
	STRING_INDEX("string_index"),
	STRING_INDEXES("string_indexes"),
	SUPPORTED_VERSIONS("supported_versions"),
	TOKEN_STRING("token_string"),
	TOOLS_URL("tools_url"),
	TURTLEHOST("turtleHost"),
	TURTLEPASSWORD("turtlePassword"),
	TURTLEUSER("turtleUser"),
	TURTLE_URL("turtle_url"),
	UL_CHARGINGDURATION("ul_chargingDuration"),
	UL_CSV_FILE_PATH("ul_csv_file_path"),
	UL_DISCHARGINGDURATION("ul_dischargingDuration"),
	UL_LOGINTERVAL("ul_logInterval"),
	UL_MAXCYCLES("ul_maxCycles"),
	UL_RESTPERIODSECONDS("ul_restPeriodSeconds"),
	UPDATE_RUNTIME_DB("update_runtime_db"),
	WEBHOOK_URL("webhook_url");
	//@formatter:on
	
	private static final String RESOURCE_NAME = "default.properties";

	private final static Logger LOG = LogManager.getLogger();
	private static Properties mProperty = new Properties();
	final String propertyFileKey;

	static {
		loadProperties();
	}

	/**
	 * Reload the properties from disk.
	 */
	public static void loadProperties() {
		getProperties(RESOURCE_NAME);
	}

	/**
	 * Constructor initializes the source flags.
	 * PowinProperty.STRING_INDEX.setValue(3);
	 *
	 * @param value - the 'key' in the property file.
	 */
	PowinProperty(final String value) {
		propertyFileKey = value;
	}

	@Override
	public String toString() {
		return mProperty.getProperty(propertyFileKey);
	}

	public int intValue() {
		return Integer.parseInt(this.toString());
	}

	public boolean booleanValue() {
		return Boolean.parseBoolean(this.toString());
	}

	/**
	 * Returns true if the property contains a list of values separated by the
	 * delimiter.
	 *
	 * @return
	 */
	public boolean isList() {
		return this.toString().contains(Constants.PROPERTY_LIST_DELIMITER);
	}

	/**
	 * Factory method to return a list of the properties coerced to the type
	 * specified. Example Usage: given a list of integer string_indexes=1|2|3|4
	 * List<Integer> arrayIndexes = (List<Integer>)
	 * PowinProperty.ARRAY_INDEXES.listOf(Integer.class);
	 *
	 * @param clazz
	 * @return
	 */
	public List<?> listOf(Class<?> clazz) {
		List<?> ret = null;
		String[] elements = this.toString().split("\\" + Constants.PROPERTY_LIST_DELIMITER); // convert to regex with
																								// "\\"
		if (clazz == Integer.class) {
			try {
				ret = Stream.of(elements).map(Integer::valueOf).collect(Collectors.toList());
			} catch (NumberFormatException e) {
				LOG.error("Error converting to an Integer.", e);
				throw new RuntimeException(e.getMessage());
			}
		}
		if (clazz == String.class) {
			ret = Stream.of(elements).collect(Collectors.toList());
		}
		return ret;
	}

	public static List<String> getNames() {
		Class<?> c = PowinProperty.class;
		Field[] flds = c.getDeclaredFields();
		List<String> names = new ArrayList<>();
		for (Field f : flds) {
			if (f.isEnumConstant()) {
				names.add(f.getName());
			}
		}
		return names;
	}

	/**
	 * This is a direct replacement for getDefaultProperty Use this when the key is
	 * constructed at runtime.
	 *
	 * @param key
	 * @return
	 */
	public static String fromPropertyFileKey(String key) {
		return mProperty.getProperty(key);
	}

	/**
	 * When running as unit test we want to force the property file in the classes
	 * folder to be loaded as we don't want to maintain two files.
	 * 
	 * @param propertyFileName
	 * @return
	 */
	private static String getProperties(String propertyFileName) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream("../classes/" + propertyFileName);
		// InputStream input = loader.getResourceAsStream(propertyFileName);
		try {
//				if (input == null)
//					input = loader.getResourceAsStream(propertyFileName);
			if (input == null)
				input = loader.getResourceAsStream("resources/" + propertyFileName);
			mProperty.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getKey() {
		return this.propertyFileKey;
	}

	/**
	 * Override the value of a property on the fly. Usage: Override the String
	 * Index, set value is a static member of the enum class so pass the
	 * specialization getKey() and the new value.
	 * PowinProperty.setValue(PowinProperty.STRING_INDEX.getKey(), "3");
	 *
	 * @param key
	 * @param string
	 */
	public static void setValue(String key, String string) {
		mProperty.setProperty(key, string);
	}

	public static void main(String[] args) {
		final PowinProperty[] values = PowinProperty.values();
		Arrays.asList(values).stream().forEach(LOG::info);
	}
}
