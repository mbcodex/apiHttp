package mbTest.utilities;

public class Constants {

	public static final String USER = PowinProperty.TURTLEUSER.toString(); // Username for ssh
	public static final String PW = PowinProperty.TURTLEPASSWORD.toString(); // Password for ssh
	public static final String TURTLE_HOST = PowinProperty.TURTLEHOST.toString();
	public static final String TURTLE_URL = PowinProperty.TURTLE_URL.toString();
	public static final String LOCAL_HOST = "localhost";
	public static final boolean NO_SSL = false;
	public static final int REDIS_PORT = 6379;
	public static final String SIMULATED_VALUES_INJECT_URL = "/turtle/dragon/qa/inject/sim/";
	public static final String REMOTE_TURTLE_XML = "/etc/tomcat8/Catalina/localhost/turtle.xml";
	public static final String LOCALHOST_TURTLE_XML = "/opt/tomcat/conf/Catalina/localhost/turtle.xml";

	// APPS
	public static final String POWIN_APP_DIR = "/etc/powin/app/";
	public static final String POWIN_DEVICE_DIR = "/etc/powin/device/";
	public static final String POWIN_SOC_DIR = "/etc/powin/soc/";
	public static final String HCP_CONFIG_FILE = "/etc/powin/app/app-3-HCP0001.json"; // EStop App
	public static final String ES_CONFIG_FILE = "/etc/powin/app/app-0-ES00001.json"; // EStop App
	public static final String DMG_CONFIG_FILE = "/etc/powin/app/app-40-DMG00001.json"; // Demand Management App
	public static final String EXPORT_MANAGEMENT_CONFIG_FILE = "/etc/powin/app/app-40-EM00001.json"; // Demand Management App
	
	public static final String SCHED_CONFIG_FILE = "/etc/powin/app/app-44-SCHED001.json"; // Scheduler App
	public static final String CONFIGURATION_FILE = "/etc/powin/configuration.json"; // Configuration json
	public static final String CONFIGURATION_SAVE_FILE = "/etc/powin/configuration_save.json"; // Configuration json
	public static final String METER_SIM_CONFIG_FILE = "/etc/powin/device/device-20-MeterSimulator.json"; // Meter
																											// Simulator
	public static final String VOLTVARCURVE_PARAMETERS_FILE = "/etc/powin/voltvarcurve.parameters.json"; // VoltVarCurve
																											// app
	public static final String MARVAIR_COMM_STAT_CONFIG = "/etc/powin/device/device-50-MarvairCommstat4.json"; // MARVAIR_COMM_STAT_CONFIG
	public static final String BASICOP_PARAMETERS_CONFIG = "/etc/powin/basicop.parameters.json"; // basicops parameters
	public static final String METER_BACHMAN_CONFIG_FILE = "/etc/powin/device/device-60-MeterBachmann.json"; // MeterBachmann
	// json
	public static final String BASICOP_CONFIG_FILE = "/etc/powin/app/app-10-BOP0001.json"; // basicops parameters json
	public static final String DRAGON_KEY_FILE = "/etc/powin/dragonkey.json";

	public static final boolean NOTIFICATION_NOT_EXPECTED = false;

	// Application Priorities
	public static final boolean APP_DISABLED = false;
	public static final boolean APP_ENABLED = true;
	public static final int HIGH_CURRENT_PROTECTION_APP_PRIORITY = 3;
	public static final int FREQUENCY_DROOP_PRIORITY = 3;
	public static final int RAMP_RATE_PRIORITY = 3;
	public static final int RECHARGER_APP_PRIORITY = 20;
	public static final int ERCOT_FFR_PRIORITY = 2;
	public static final int SCHEDULER_APP_PRIORITY = 44;
	public static final int FREQ_WATT_PRIORITY = 45;
	public static final int SOC_CALIBRATOR_PRIORITY = 45;
	public static final int VOLT_VAR_APP_PRIORITY = 44;
	public static final int VOLT_VAR_CURVE_APP_PRIORITY = 50;
	public static final int FREQUENCY_CONTAINMENT_RESERVES_PRIORITY = 45;
	public static final int REALPOWER_APP_PRIORITY = 4;
	public static final int SUNSPEC_DISCONNECT_APP_PRIORITY = 3;
	public static final int SLOW_CHARGE_APP_PRIORITY = 6;

	// STATE
	public static final boolean ENABLE = true;
	public static final boolean DISABLE = false;
	public static final int DISABLED = 0;
	public static final int ENABLED = 1;
	// process execution flags
	public static final boolean WAIT = true; // Wait for results
	public static final boolean CAPTURE = true; // Capture output
	public static final boolean NOWAIT = false; // Wait for results
	public static final boolean NOCAPTURE = false; // Capture output
	public static final int MODBUS_PORT = 4502;
	public static final int MODBUS_UNIT_ID = 255;
	public static final int SECONDS_PER_MINUTE = 60; // Time conversion for ms
	public static final int MILLIS_PER_SECOND = 1000;
	public static final int ONE_SECOND = MILLIS_PER_SECOND;
	public static final int ONE_MINUTE_SECONDS = 60;
	public static final int ONE_MINUTE_MS = SECONDS_PER_MINUTE * MILLIS_PER_SECOND;
	public static final int SIXTY_SECONDS = ONE_MINUTE_MS;
	public final static int FIVE_SECONDS = 5 * MILLIS_PER_SECOND;
	public static final int TEN_SECONDS = 2 * FIVE_SECONDS;
	public static final int TWENTY_SECONDS = 4 * FIVE_SECONDS;
	public static final int THIRTY_SECONDS = 3 * TEN_SECONDS;
	public final static int FIVE_MINUTES_MS = 5 * ONE_MINUTE_MS;
	public final static int FIVE_MINUTES_SECONDS = 5 * ONE_MINUTE_SECONDS;	
	public static final long SIXTY_SECONDS_L = 60000L;
	public static final long THIRTY_SECONDS_L = SIXTY_SECONDS_L / 2;
	public static final int SOC_BOTTOM = 0; // Discharge to 0 SoC
	public static final int NO_TARGET_SOC = -1; // No target SoC is specified
	public static final boolean MOVING_POWER_ALLOWED = true;
	public static final boolean MOVING_POWER_NOT_ALLOWED = false;
	public static final boolean DISCHARGING = false;
	public static final boolean CHARGING = true;
	public static final int SOC_TOP = 100;
	public static final String KOBOLD = "kobold";
	public static final String PRIMROSE = "primrose";
	public static final String TURTLE = "turtle";
	public static final String COBLYNAU = "coblynau";
	public static final String KNOCKER = "knocker";
	static final String LATEST = "LATEST";
	public static final int MAX_BALANCE_MINUTES = 60;
	public static final String BALANCE_TO_AVERAGE = "BALANCE_TO_AVERAGE";
	public static final String BALANCE_TO_PROVIDED = "BALANCE_TO_PROVIDED";
	public static final String BALANCE_TO_HIGHEST = "BALANCE_TO_HIGHEST";
	public static final String BALANCE_TO_LOWEST = "BALANCE_TO_LOWEST";
	public static final String BALANCING_OFF = "BALANCING_OFF";
	public static final String BATTERY_PACK_CHARGE_BALANCING_ON = "BATTERY_PACK_CHARGE_BALANCING_ON";
	public static final String BATTERY_PACK_DISCHARGE_BALANCING_ON = "BATTERY_PACK_DISCHARGE_BALANCING_ON";
	public static final double REPORTS_TEMP_SCALE_FACTOR = 10.0;
	public static final double ROOM_TEMP_MAX = 26.0;
	public static final double ROOM_TEMP_MIN = 19.0;
	public static final int ONE_HOUR = 60;
	public static final boolean DISABLE_SSL_CHECKING = true;
	public static final int PRIORITY_POWER = 0;
	public static final int PRIORITY_SOC = 1;
	public static final int TWO_MINUTES = 120;
	public static final String PROPERTY_LIST_DELIMITER = "|";
	public static final int INVALID_SOC = -1;
	public static final int INVALID_STRING_CURRENT = 9999;
	public static final long RECHARGER_APP_STARTUP_DELAY = 4 * FIVE_SECONDS;
	public static final boolean POWER_BLOCKED = false;
	public static final String PIPE_DELIMITER = "\\|";
	public static final int INITIAL_CONTAINER_CAPACITY = 256;
	public static final boolean PRIORITY_SOC_BOOLEAN = false;
	public static final int CURRENT_TOLERANCE = 5;
	public static final boolean NOTIFICATION_EXPECTED = true;
	public static final boolean NOLOG = false;
	public static final boolean LOG = true;
	public static final boolean IGNORE = true;
	public static final boolean HEED = false;
	public static final String BLOCK_HVAC_SIMULATOR_CONFIG = "/etc/powin/device/device-50-BlockHvacSimulator.json";
	public static final int ARRAYINDEX = PowinProperty.ARRAY_INDEX.intValue();
	public static final int STRINGINDEX = PowinProperty.STRING_INDEX.intValue();
	public static final int TEN_MINUTES_SEC = 600;
	public static final int TEN_MINUTES_MIN = 10;
	public static final boolean CONTACTORS_CLOSED = true;
	public static final boolean CONTACTORS_OPEN = false;
	public static final boolean NO_CREATEDIR = false;
	public static final boolean RESTART_TOMCAT = true;
	public static final boolean ASCENDING_ORDER = true;
	public static final boolean DESCENDING_ORDER = false;
	public static final String POWIN_ENTITY_TYPE_APP = "app";
	public static final String POWIN_ENTITY_TYPE_DEVICE = "device";
	public static final boolean IN_ROTATION = true;
	public static final boolean OUT_OF_ROTATION = false;
	public static final String BALANCE_MECHANISM_PARAMETER = "phoenix.handlesBalancingCommand";
	public static final int NO_TARGET_STRING_VOLTAGE = 0;
	public static final long NO_TIME_LIMIT = 0;
	public static final boolean INCLUDE_TIMESTAMP = true;
	public static final long MAX_TEST_TIME_SECONDS = 2 * ONE_HOUR * SECONDS_PER_MINUTE;
	public static final boolean ENABLE_MODBUS_LOGGING = false;
	public static final int SUNSPEC_ENDPOINT_TIMEOUT = TEN_SECONDS;
	public static final boolean INITIAL_PASS_STATE_TRUE = true;
	public static final String DELIMITER_PIPE = "|";
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	

}
