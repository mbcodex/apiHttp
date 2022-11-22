package mbTest.utilities;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.ByteString;
import com.powin.goblin.commands.GoblinCommandDepot;
import com.powin.modbusfiles.awe.AweCommon;
import com.powin.modbusfiles.stackoperations.Balancing;
import com.powin.redis.JedisPoolWrapper;
import com.powin.tongue.fourba.command.Command;
import com.powin.tongue.fourba.command.CommandPayload;
import com.powin.tongue.fourba.command.Endpoint;
import com.powin.tongue.fourba.command.EndpointType;
import com.powin.tongue.fourba.command.SetEMSApplicationConfiguration;
import com.powin.tongue.fourba.command.SetStringVoltageNotificationConfiguration;
import com.powin.tongue.fourba.command.VoltageTrigger;

import redis.clients.jedis.JedisPool;

public class CommandHelper {

	protected final static Logger LOG = LogManager.getLogger();
	private static String cloudHost = PowinProperty.CLOUDHOST.toString();
	private String cRemoteUrl;
	private String cStationCode;
	private int cArrayIndex;
	private int cStringIndex;
	private CommonHelper commonHelper = new CommonHelper();

	// These values are from the recommended settings document created by Dylan
	// Vance:
	// https://powin365.sharepoint.com/Project%20Success/Forms/AllItems.aspx?FolderCTID=0x012000145D0C9521F3D0459B3D7A629243BE33&View=%7B5ED3F878%2D3088%2D433A%2DA325%2D287DD1CDF95F%7D&InitialTabId=Ribbon%2ERead&VisibilityContext=WSSTabPersistence&CT=1584566630014&OR=OWA%2DNT&CID=b10ae0ab%2Df606%2Dac45%2D65a4%2Dd1762957860a&id=%2FProject%20Success%2FPowin%20Energy%20Client%20Documentation%20Suite%2FProduct%20Client%20Documentation%2FProduct%20Guides%2FPowin%20Default%20System%20Configurations%20V1%2E0%2Epdf&parent=%2FProject%20Success%2FPowin%20Energy%20Client%20Documentation%20Suite%2FProduct%20Client%20Documentation%2FProduct%20Guides
	// DEFAULTS: Battery Pack Voltage Notifications
	// #region
	private static final int CELL_HIGH_VOLTAGE_ALARM_SET = 3650;// Notification code: 1001
	private static final int CELL_HIGH_VOLTAGE_ALARM_CLEAR = 3600;
	private static final int CELL_HIGH_VOLTAGE_WARNING_SET = 3600;// Notification code: 2001
	private static final int CELL_HIGH_VOLTAGE_WARNING_CLEAR = 3525;
	private static final int CELL_HIGH_VOLTAGE_WARRANTY_ALARM_SET = 3650;// Notification code: 8001
	private static final int CELL_HIGH_VOLTAGE_WARRANTY_ALARM_CLEAR = 3600;
	private static final int CELL_HIGH_VOLTAGE_WARRANTY_WARNING_SET = 3600;// Notification code: 9001
	private static final int CELL_HIGH_VOLTAGE_WARRANTY_WARNING_CLEAR = 3500;
	private static final int CELL_LOW_VOLTAGE_ALARM_SET = 2500;// Notification code: 1004
	private static final int CELL_LOW_VOLTAGE_ALARM_CLEAR = 2800;
	private static final int CELL_LOW_VOLTAGE_WARNING_SET = 2800;// Notification code: 2004
	private static final int CELL_LOW_VOLTAGE_WARNING_CLEAR = 2900;
	private static final int CELL_LOW_VOLTAGE_WARRANTY_ALARM_SET = 2500;// Notification code: 8004
	private static final int CELL_LOW_VOLTAGE_WARRANTY_ALARM_CLEAR = 2700;
	private static final int CELL_LOW_VOLTAGE_WARRANTY_WARNING_SET = 2700;// Notification code: 9004
	private static final int CELL_LOW_VOLTAGE_WARRANTY_WARNING_CLEAR = 2800;
	private static final int CELL_HIGH_VOLTAGE_DELTA_ALARM_SET = 1000;// Notification code: 1007
	private static final int CELL_HIGH_VOLTAGE_DELTA_ALARM_CLEAR = 800;
	private static final int CELL_HIGH_VOLTAGE_DELTA_WARNING_SET = 800;// Notification code: 2007
	private static final int CELL_HIGH_VOLTAGE_DELTA_WARNING_CLEAR = 600;
	// #endregion
	// #region
	// DEFAULTS: Battery Pack Temperature Notifications
	private static final int CELL_HIGH_TEMPERATURE_ALARM_SET = 47;// Notification code: 1010
	private static final int CELL_HIGH_TEMPERATURE_ALARM_CLEAR = 42;
	private static final int CELL_HIGH_TEMPERATURE_WARNING_SET = 42;// Notification code: 2010
	private static final int CELL_HIGH_TEMPERATURE_WARNING_CLEAR = 37;
	private static final int CELL_HIGH_TEMPERATURE_WARRANTY_ALARM_SET = 38;// Notification code: 8010
	private static final int CELL_HIGH_TEMPERATURE_WARRANTY_ALARM_CLEAR = 33;
	private static final int CELL_HIGH_TEMPERATURE_WARRANTY_WARNING_SET = 33;// Notification code: 9010
	private static final int CELL_HIGH_TEMPERATURE_WARRANTY_WARNING_CLEAR = 28;
	private static final int CELL_LOW_TEMPERATURE_ALARM_SET = 4;// Notification code: 1014
	private static final int CELL_LOW_TEMPERATURE_ALARM_CLEAR = 9;
	private static final int CELL_LOW_TEMPERATURE_WARNING_SET = 9;// Notification code: 2014
	private static final int CELL_LOW_TEMPERATURE_WARNING_CLEAR = 14;
	private static final int CELL_LOW_TEMPERATURE_WARRANTY_ALARM_SET = 4;// Notification code: 8014
	private static final int CELL_LOW_TEMPERATURE_WARRANTY_ALARM_CLEAR = 9;
	private static final int CELL_LOW_TEMPERATURE_WARRANTY_WARNING_SET = 9;// Notification code: 9014
	private static final int CELL_LOW_TEMPERATURE_WARRANTY_WARNING_CLEAR = 14;
	private static final int CELL_HIGH_TEMPERATURE_DELTA_ALARM_SET = 43;// Notification code: 1018
	private static final int CELL_HIGH_TEMPERATURE_DELTA_ALARM_CLEAR = 33;
	private static final int CELL_HIGH_TEMPERATURE_DELTA_WARNING_SET = 33;// Notification code: 2018
	private static final int CELL_HIGH_TEMPERATURE_DELTA_WARNING_CLEAR = 23;
	// #endregion
	// DEFAULTS: String Voltage Notifications
	private static final int STRING_HIGH_VOLTAGE_ALARM_SET = 963;// Notification code: 1003
	private static final int STRING_HIGH_VOLTAGE_ALARM_CLEAR = 953;
	private static final int STRING_HIGH_VOLTAGE_WARNING_SET = 953;// Notification code: 2003
	private static final int STRING_HIGH_VOLTAGE_WARNING_CLEAR = 943;
	private static final int STRING_LOW_VOLTAGE_ALARM_SET = 660;// Notification code: 1006
	private static final int STRING_LOW_VOLTAGE_ALARM_CLEAR = 670;
	private static final int STRING_LOW_VOLTAGE_WARNING_SET = 670;// Notification code: 2006
	private static final int STRING_LOW_VOLTAGE_WARNING_CLEAR = 680;
	private static final int STRING_HIGH_VOLTAGE_DELTA_ALARM_SET = 12650;// Notification code: 1008
	private static final int STRING_HIGH_VOLTAGE_DELTA_ALARM_CLEAR = 9650;
	private static final int STRING_HIGH_VOLTAGE_DELTA_WARNING_SET = 9650;// Notification code: 2008
	private static final int STRING_HIGH_VOLTAGE_DELTA_WARNING_CLEAR = 6650;

	public CommandHelper(String url, String stationCode, int arrayIndex, int stringIndex) {
		if (url.contains("/")) {
			cRemoteUrl = url;
		} else {
			String tmpUrl = PowinProperty.PHOENIX_COMMAND_INJECT_URL.toString();
			tmpUrl = tmpUrl.replace("array_index/string_index", "");
			tmpUrl += arrayIndex + "/" + stringIndex;
			cRemoteUrl = tmpUrl;
		}
		cStationCode = stationCode;
		cArrayIndex = arrayIndex;
		cStringIndex = stringIndex;
	}

	// String Low voltage warning
	public void setLowStringVoltageWarning(int set, int clear) {
		if (set < clear) {
			SetStringVoltageNotificationConfiguration mConfig = getDefaultStringVoltageNotificationConfiguration();
			mConfig = SetStringVoltageNotificationConfiguration.newBuilder(mConfig).setLowStringVoltageWarning(
					VoltageTrigger.newBuilder().setEnabled(true).setSet(set).setClear(clear)).build();
			Command mCommand = getCommand(mConfig);
			AweCommon.postCommand(mCommand);
		}
	}

	// String High voltage warning
	public void setHighStringVoltageWarning(int set, int clear) {
		if (set > clear) {
			SetStringVoltageNotificationConfiguration mConfig = getDefaultStringVoltageNotificationConfiguration();
			mConfig = SetStringVoltageNotificationConfiguration.newBuilder(mConfig).setHighStringVoltageWarning(
					VoltageTrigger.newBuilder().setEnabled(true).setSet(set).setClear(clear)).build();
			Command mCommand = getCommand(mConfig);
			AweCommon.postCommand(mCommand);
		}
	}

	// Battery pack high voltage delta warning
	public void setHighStringVoltageDeltaWarning(int set, int clear) {
		if (set > clear) {
			SetStringVoltageNotificationConfiguration mConfig = getDefaultStringVoltageNotificationConfiguration();
			mConfig = SetStringVoltageNotificationConfiguration.newBuilder(mConfig)
					.setHighBatteryPackVoltageDeltaWarning(
							VoltageTrigger.newBuilder().setEnabled(true).setSet(set).setClear(clear))
					.build();
			Command mCommand = getCommand(mConfig);
			AweCommon.postCommand(mCommand);
		}
	}

	// String Low voltage alarm
	public void setLowStringVoltageAlarm(int set, int clear) {
		if (set < clear) {
			SetStringVoltageNotificationConfiguration mConfig = getDefaultStringVoltageNotificationConfiguration();
			mConfig = SetStringVoltageNotificationConfiguration.newBuilder(mConfig)
					.setLowStringVoltageAlarm(VoltageTrigger.newBuilder().setEnabled(true).setSet(set).setClear(clear))
					.build();
			Command mCommand = getCommand(mConfig);
			AweCommon.postCommand(mCommand);
		}
	}

	// String High voltage alarm
	public void setHighStringVoltageAlarm(int set, int clear) {
		if (set > clear) {
			SetStringVoltageNotificationConfiguration mConfig = getDefaultStringVoltageNotificationConfiguration();
			mConfig = SetStringVoltageNotificationConfiguration.newBuilder(mConfig)
					.setHighStringVoltageAlarm(VoltageTrigger.newBuilder().setEnabled(true).setSet(set).setClear(clear))
					.build();
			Command mCommand = getCommand(mConfig);
			AweCommon.postCommand(mCommand);
		}
	}

	// Battery pack high voltage delta alarm
	public void setHighStringVoltageDeltaAlarm(int set, int clear) {
		if (set > clear) {
			SetStringVoltageNotificationConfiguration mConfig = getDefaultStringVoltageNotificationConfiguration();
			mConfig = SetStringVoltageNotificationConfiguration.newBuilder(mConfig).setHighBatteryPackVoltageDeltaAlarm(
					VoltageTrigger.newBuilder().setEnabled(true).setSet(set).setClear(clear)).build();
			Command mCommand = getCommand(mConfig);
			AweCommon.postCommand(mCommand);
		}
	}

	public void resetDefaultsStringVoltageNotificationConfiguration() {
		SetStringVoltageNotificationConfiguration mConfig = getDefaultStringVoltageNotificationConfiguration();
		Command mCommand = getCommand(mConfig);
		AweCommon.postCommand(mCommand);
	}

	public void disableStringVoltageNotificationConfiguration() {
		SetStringVoltageNotificationConfiguration mConfig = getDisabledStringVoltageNotificationConfiguration();
		Command mCommand = getCommand(mConfig);
		AweCommon.postCommand(mCommand);
	}

	private SetStringVoltageNotificationConfiguration getDisabledStringVoltageNotificationConfiguration() {
		SetStringVoltageNotificationConfiguration mConfig = SetStringVoltageNotificationConfiguration.newBuilder()
				.setHighStringVoltageAlarm(VoltageTrigger.newBuilder().setEnabled(false))
				.setHighStringVoltageWarning(VoltageTrigger.newBuilder().setEnabled(false))
				.setLowStringVoltageAlarm(VoltageTrigger.newBuilder().setEnabled(false))
				.setLowStringVoltageWarning(VoltageTrigger.newBuilder().setEnabled(false))
				.setHighBatteryPackVoltageDeltaAlarm(VoltageTrigger.newBuilder().setEnabled(false))
				.setHighBatteryPackVoltageDeltaWarning(VoltageTrigger.newBuilder().setEnabled(false)).build();
		return mConfig;
	}

	private SetStringVoltageNotificationConfiguration getDefaultStringVoltageNotificationConfiguration() {
		SetStringVoltageNotificationConfiguration mConfig = SetStringVoltageNotificationConfiguration.newBuilder()
				.setHighStringVoltageAlarm(VoltageTrigger.newBuilder().setEnabled(true)
						.setSet(STRING_HIGH_VOLTAGE_ALARM_SET).setClear(STRING_HIGH_VOLTAGE_ALARM_CLEAR))
				.setHighStringVoltageWarning(VoltageTrigger.newBuilder().setEnabled(true)
						.setSet(STRING_HIGH_VOLTAGE_WARNING_SET).setClear(STRING_HIGH_VOLTAGE_WARNING_CLEAR))
				.setLowStringVoltageAlarm(VoltageTrigger.newBuilder().setEnabled(true)
						.setSet(STRING_LOW_VOLTAGE_ALARM_SET).setClear(STRING_LOW_VOLTAGE_ALARM_CLEAR))
				.setLowStringVoltageWarning(VoltageTrigger.newBuilder().setEnabled(true)
						.setSet(STRING_LOW_VOLTAGE_WARNING_SET).setClear(STRING_LOW_VOLTAGE_WARNING_CLEAR))
				.setHighBatteryPackVoltageDeltaAlarm(VoltageTrigger.newBuilder().setEnabled(true)
						.setSet(STRING_HIGH_VOLTAGE_DELTA_ALARM_SET).setClear(STRING_HIGH_VOLTAGE_DELTA_ALARM_CLEAR))
				.setHighBatteryPackVoltageDeltaWarning(
						VoltageTrigger.newBuilder().setEnabled(true).setSet(STRING_HIGH_VOLTAGE_DELTA_WARNING_SET)
								.setClear(STRING_HIGH_VOLTAGE_DELTA_WARNING_CLEAR))
				.build();
		return mConfig;
	}

	private Command getCommand(SetStringVoltageNotificationConfiguration mConfig) {
		Command mCommand = Command.newBuilder().setCommandId(UUID.randomUUID().toString())
				.setCommandPayload(CommandPayload.newBuilder().setSetStringVoltageNotificationConfiguration(mConfig))
				.setCommandSource(Endpoint.newBuilder().setEndpointType(EndpointType.GOBLIN))
				.setCommandTarget(Endpoint.newBuilder().setEndpointType(EndpointType.ARRAY).setStationCode(cStationCode)
						.setStringIndex(cStringIndex).setArrayIndex(cArrayIndex))
				.build();
		return mCommand;
	}

	public static void executeCommand(String URL) {
		try {
			LOG.debug("Executing request to URL: {}", URL);
			HttpHelper cHttpHelper = new HttpHelper(URL);
			cHttpHelper.getConnection();
		} catch (KeyManagementException | NoSuchAlgorithmException | IOException e) {
			Balancing.LOG.error(e.getMessage());
			throw new RuntimeException("Error executing command.");
		}
	}

	public static Command getGoblinCommand(String appCode, int appPriority, boolean mEnabled, ByteString mByteString,
			String stationCode, int blockIndex) {
		SetEMSApplicationConfiguration mSetEMSApplicationConfiguration = SetEMSApplicationConfiguration.newBuilder()
				.setApplicationConfigurationName("instant").setApplicationConfigurationVersionid(0)
				.setApplicationTypeCode(appCode).setApplicationPriority(appPriority).setEnabled(mEnabled)
				.setRawApplicationConfiguration(mByteString).build();

		Command mCommand = Command.newBuilder().setCommandId(UUID.randomUUID().toString())
				.setCommandSource(Endpoint.newBuilder().setEndpointType(EndpointType.GOBLIN))
				.setCommandTarget(Endpoint.newBuilder().setEndpointType(EndpointType.BLOCK).setStationCode(stationCode)
						.setBlockIndex(blockIndex))
				.setCommandPayload(
						CommandPayload.newBuilder().setSetEMSApplicationConfiguration(mSetEMSApplicationConfiguration))
				.build();
		return mCommand;
	}

	public static GoblinCommandDepot getGoblinCommandDepot() {
		GenericObjectPoolConfig cGenericObjectPoolConfig = new GenericObjectPoolConfig();
		cGenericObjectPoolConfig.setMinEvictableIdleTimeMillis(Constants.SIXTY_SECONDS_L);
		cGenericObjectPoolConfig.setTestWhileIdle(Constants.ENABLE);
		cGenericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(Constants.THIRTY_SECONDS_L);
		cGenericObjectPoolConfig.setNumTestsPerEvictionRun(1);
		JedisPool cJedisPool = new JedisPool(cGenericObjectPoolConfig, cloudHost, 6379, 10000, false);
		JedisPoolWrapper cJedisPoolWrapper = new JedisPoolWrapper();
		cJedisPoolWrapper.setJedisPool(cJedisPool);
		GoblinCommandDepot cGoblinCommandDepot = new GoblinCommandDepot();
		cGoblinCommandDepot.setRedisEnabled(Constants.ENABLE);
		cGoblinCommandDepot.setRedisPool(cJedisPoolWrapper);
		cGoblinCommandDepot.setRedisKeyPrefix("gcd2");
		cGoblinCommandDepot.init();
		return cGoblinCommandDepot;
	}

	public static void main(String[] args) {
		CommandHelper mCommands = new CommandHelper("", "QATEST7", PowinProperty.ARRAY_INDEX.intValue(),
				PowinProperty.STRING_INDEX.intValue());
		// mCommands.setHighCellGroupVoltageAlarm(3250, 3200);
		// mCommands.setHighCellGroupVoltageAlarm(CELL_HIGH_VOLTAGE_ALARM_SET,
		// CELL_HIGH_VOLTAGE_ALARM_CLEAR);
		// mCommands.setHighCellGroupVoltageWarning(3250, 3200);
		// mCommands.setHighCellGroupVoltageWarning(CELL_HIGH_VOLTAGE_WARNING_SET,
		// CELL_HIGH_VOLTAGE_WARNING_CLEAR);
		// mCommands.setLowCellGroupVoltageAlarm(3300, 3330);//did not raise
		// notification
		// mCommands.setLowCellGroupVoltageAlarm(CELL_LOW_VOLTAGE_ALARM_SET,
		// CELL_LOW_VOLTAGE_ALARM_CLEAR);
		// mCommands.setLowCellGroupVoltageWarning(3280, 3330);
		// mCommands.setLowCellGroupVoltageWarning(CELL_LOW_VOLTAGE_WARNING_SET,
		// CELL_LOW_VOLTAGE_WARNING_CLEAR);
		// mCommands.setHighCellGroupVoltageDeltaAlarm(10, 5);
		// mCommands.setHighCellGroupVoltageDeltaAlarm(CELL_HIGH_VOLTAGE_DELTA_ALARM_SET,
		// CELL_HIGH_VOLTAGE_DELTA_WARNING_CLEAR);
		// mCommands.setHighCellGroupVoltageDeltaWarning(10, 5);
		// mCommands.setHighCellGroupVoltageDeltaWarning(CELL_HIGH_VOLTAGE_DELTA_WARNING_SET,
		// CELL_HIGH_VOLTAGE_DELTA_WARNING_CLEAR);

		// mCommands.setHighStringVoltageAlarm(800, 750);
		// mCommands.setHighStringVoltageAlarm(800, 750);
		mCommands.resetDefaultsStringVoltageNotificationConfiguration();

	}
}