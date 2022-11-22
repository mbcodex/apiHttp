package mbTest.utilities;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeviceInstaller {
	private final static Logger LOG = LogManager.getLogger();
	protected static final String tempBlockHvacSimulatorConfigContents = "{\n"
			+ "  \"blockHvacIndex\" : {blockHvacIndex},\n" 
			+ "  \"combineSignals\" : true,\n"
			+ "  \"safeTempDuration\" : 30000,\n" 
			+ "  \"highTempDuration\" : 30000,\n"
			+ "  \"lowTempDuration\" : 30000,\n" 
			+ "  \"noFaultDuration\" : 30000,\n"
			+ "  \"lockoutDuration\" : 30000,\n" 
			+ "  \"communicatingDuration\" : 30000,\n"
			+ "  \"notCommunicatingDuration\" : 30000,\n" 
			+ "  \"enabled\" : ${},\n" 
			+ "  \"startPriority\" : 50,\n"
			+ "  \"stopPriority\" : 50\n" + "}\n";

	protected static final String modifiedBasicopsConfigContents = "{\n"
			+ " \"topOffToTarget_DisableBalancing\" : false,\n" 
			+ " \"adaptivePowerManager_PowerDivisor\" : 32,\n"
			+ " \"adaptivePowerManager_MinPowerNumerator\" : 4,\n" 
			+ " \"sweep_ContactorCommandDelay\" : 30000,\n"
			+ " \"adaptivePowerManager_ExitFromUnexpiringWarns\" : 10000,\n"
			+ " \"adaptivePowerManager_ShortPowerIncreasePeriod\" : 10000,\n"
			+ " \"adaptivePowerManager_PowerIncreasePeriod\" : 10000,\n"
			+ " \"basicOpBalancer_BalancesRulesTTL\" : 10000,\n" 
			+ " \"pcsCommandPeriod\" : 500,\n"
			+ " \"bulkAtPower_ExitDelay\" : 10000,\n" 
			+ " \"settleAfterTarget_SettlePeriod\" : 10000,\n"
			+ " \"sweep_AllowedStackDcBusDelta\" : 5,\n" 
			+ " \"sweep_PowerDivisor\" : 32,\n"
			+ " \"sweep_ShortPowerIncreaseTime\" : 10000,\n" 
			+ " \"sweep_NormalPowerIncreaseTime\" : 10000,\n"
			+ " \"sweep_RelaxPeriod\" : 20000\n" + "}\n";

//	protected static final String modifiedBasicopsConfigContents = "{\n"
//			+ "  \"topOffToTarget_DisableBalancing\" : false,\n" 
//			+ "  \"adaptivePowerManager_PowerDivisor\" : 32,\n"
//			+ "  \"adaptivePowerManager_MinPowerNumerator\" : 4,\n" 
//			+ "  \"sweep_ContactorCommandDelay\" : 300000,\n"
//			+ "  \"adaptivePowerManager_ExitFromUnexpiringWarns\" : 180000,\n"
//			+ "  \"adaptivePowerManager_ShortPowerIncreasePeriod\" : 10000,\n"
//			+ "  \"adaptivePowerManager_PowerIncreasePeriod\" : 60000,\n"
//			+ "  \"basicOpBalancer_BalancesRulesTTL\" : 300000,\n" 
//			+ "  \"pcsCommandPeriod\" : 500,\n"
//			+ "  \"bulkAtPower_ExitDelay\" : 60000,\n" 
//			+ "  \"settleAfterTarget_SettlePeriod\" : 180000,\n"
//			+ "  \"sweep_AllowedStackDcBusDelta\" : 5,\n" 
//			+ "  \"sweep_PowerDivisor\" : 32,\n"
//			+ "  \"sweep_ShortPowerIncreaseTime\" : 10000,\n" 
//			+ "  \"sweep_NormalPowerIncreaseTime\" : 60000,\n"
//			+ "  \"sweep_RelaxPeriod\" : 20000\n" + "}\n";

	protected static final String originalBasicopsConfigContents = "{\n"
			+ "  \"topOffToTarget_DisableBalancing\" : false,\n" 
			+ "  \"adaptivePowerManager_PowerDivisor\" : 32,\n"
			+ "  \"adaptivePowerManager_MinPowerNumerator\" : 4,\n" 
			+ "  \"sweep_ContactorCommandDelay\" : 300000,\n"
			+ "  \"adaptivePowerManager_ExitFromUnexpiringWarns\" : 180000,\n"
			+ "  \"adaptivePowerManager_ShortPowerIncreasePeriod\" : 10000,\n"
			+ "  \"adaptivePowerManager_PowerIncreasePeriod\" : 60000,\n"
			+ "  \"basicOpBalancer_BalancesRulesTTL\" : 300000,\n" 
			+ "  \"pcsCommandPeriod\" : 500,\n"
			+ "  \"bulkAtPower_ExitDelay\" : 60000,\n" 
			+ "  \"settleAfterTarget_SettlePeriod\" : 180000,\n"
			+ "  \"sweep_AllowedStackDcBusDelta\" : 5,\n" 
			+ "  \"sweep_PowerDivisor\" : 32,\n"
			+ "  \"sweep_ShortPowerIncreaseTime\" : 10000,\n" 
			+ "  \"sweep_NormalPowerIncreaseTime\" : 60000,\n"
			+ "  \"sweep_RelaxPeriod\" : 180000\n" + "}\n";

	protected static final String basicOpsAppConfigContents = "{\n" 
			+ "	\"startupDelay\" :60000, \n"
			+ "	\"basicOp\" :\n" + "		{\n" 
			+ "			\"priority\" :\"SOC\" , \n"
			+ "			\"targetSOC\" :99\n" 
			+ "			}, \n" + "	\"enabled\" :true, \n"
			+ "	\"appConfigName\" :\"default\", \n" 
			+ "	\"appConfigVersion\" :0\n" + "} ";

	public static void installBlockHvacSimulator(boolean isEnable, int blockHvacIndex) {
		String fileContent = tempBlockHvacSimulatorConfigContents.replace("${}", String.valueOf(isEnable))
				.replace("{blockHvacIndex}", Integer.toString(blockHvacIndex));

		FileHelper.writeConfigFile(PowinProperty.TURTLEHOST.toString(), Constants.BLOCK_HVAC_SIMULATOR_CONFIG,
				fileContent);
		CommonHelper.setSystemFilePermissons("777", Constants.BLOCK_HVAC_SIMULATOR_CONFIG);
	}

	public static void removeBlockHvacSimulatorConfigFile() {
		if (PowinProperty.TURTLEHOST.toString().equals(Constants.LOCAL_HOST)) {
			FileHelper.setFullPermissions((new File(Constants.BLOCK_HVAC_SIMULATOR_CONFIG)).getParent());
			FileUtils.deleteQuietly(new File(Constants.BLOCK_HVAC_SIMULATOR_CONFIG));
		} else {
			FileHelper.removeRemoteFile(Constants.BLOCK_HVAC_SIMULATOR_CONFIG, PowinProperty.TURTLEHOST.toString(),
					PowinProperty.TURTLEUSER.toString(), PowinProperty.TURTLEPASSWORD.toString());
		}
	}

	public static void installBasicopParametersJson(boolean isModifiedFile) {
		if (isModifiedFile)
			FileHelper.writeConfigFile(PowinProperty.TURTLEHOST.toString(), Constants.BASICOP_PARAMETERS_CONFIG,
					modifiedBasicopsConfigContents);
		else
			FileHelper.writeConfigFile(PowinProperty.TURTLEHOST.toString(), Constants.BASICOP_PARAMETERS_CONFIG,
					originalBasicopsConfigContents);
		CommonHelper.setSystemFilePermissons("777", Constants.BASICOP_PARAMETERS_CONFIG);
	}

	public static void removeBasicopParametersJson() {
		if (PowinProperty.TURTLEHOST.toString().equals(Constants.LOCAL_HOST)) {
			FileHelper.setFullPermissions((new File(Constants.BASICOP_PARAMETERS_CONFIG)).getParent());
			FileUtils.deleteQuietly(new File(Constants.BASICOP_PARAMETERS_CONFIG));
		} else {
			FileHelper.removeRemoteFile(Constants.BASICOP_PARAMETERS_CONFIG, PowinProperty.TURTLEHOST.toString(),
					PowinProperty.TURTLEUSER.toString(), PowinProperty.TURTLEPASSWORD.toString());
		}
	}

	public static void removeBasicopConfigFile() {
		if (PowinProperty.TURTLEHOST.toString().equals(Constants.LOCAL_HOST)) {
			FileHelper.setFullPermissions((new File(Constants.BASICOP_CONFIG_FILE)).getParent());
			FileUtils.deleteQuietly(new File(Constants.BASICOP_CONFIG_FILE));
		} else {
			FileHelper.removeRemoteFile(Constants.BASICOP_CONFIG_FILE, PowinProperty.TURTLEHOST.toString(),
					PowinProperty.TURTLEUSER.toString(), PowinProperty.TURTLEPASSWORD.toString());
		}
	}

	public static void installBasicopConfigFile() {
		FileHelper.writeConfigFile(PowinProperty.TURTLEHOST.toString(), Constants.BASICOP_CONFIG_FILE,
				basicOpsAppConfigContents);
		CommonHelper.setSystemFilePermissons("777", Constants.BASICOP_CONFIG_FILE);
	}
}
