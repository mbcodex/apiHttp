package mbTest.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.ByteString;
import com.powin.goblin.commands.GoblinCommandDepot;
import com.powin.modbusfiles.reports.SystemInfo;
import com.powin.redis.JedisPoolWrapper;
import com.powin.tongue.fourba.command.Command;
import com.powin.tongue.fourba.command.CommandPayload;
import com.powin.tongue.fourba.command.Endpoint;
import com.powin.tongue.fourba.command.EndpointType;
import com.powin.tongue.fourba.command.SetEMSApplicationConfiguration;

import redis.clients.jedis.JedisPool;

public class AppInjectionCommon {
	private final static Logger LOG = LogManager.getLogger();
	private final static String APP_CONFIG_URL = Constants.POWIN_APP_DIR;
	private final static String SH_FILE_URL = "/home/powin/";
	

	public static Command buildCommand(boolean isEnable, ByteString byteString, String appConfigName, int appPriority,
			String appCode) {
		Command mCommand = Command.newBuilder().setCommandId(UUID.randomUUID().toString())
				.setCommandSource(Endpoint.newBuilder().setEndpointType(EndpointType.GOBLIN))
				.setCommandTarget(Endpoint.newBuilder().setEndpointType(EndpointType.BLOCK)
						.setStationCode(SystemInfo.getStationCode()).setBlockIndex(SystemInfo.getBlockIndex()))
				.setCommandPayload(CommandPayload.newBuilder().setSetEMSApplicationConfiguration(
						buildEMSAppConfig(isEnable, byteString, appConfigName, appPriority, appCode)))
				.build();
		return mCommand;
	}

	private static SetEMSApplicationConfiguration buildEMSAppConfig(boolean isEnabled, ByteString byteString,
			String appConfigName, int appPriority, String appCode) {
		SetEMSApplicationConfiguration mSetEMSApplicationConfiguration = SetEMSApplicationConfiguration.newBuilder()
				.setApplicationConfigurationName(appConfigName).setApplicationConfigurationVersionid(0)
				.setApplicationTypeCode(appCode).setApplicationPriority(appPriority).setEnabled(isEnabled)
				.setRawApplicationConfiguration(byteString).build();
		return mSetEMSApplicationConfiguration;
	}

	public static GoblinCommandDepot getGoblinCommandDepot() {
		GenericObjectPoolConfig mGenericObjectPoolConfig = new GenericObjectPoolConfig();
		mGenericObjectPoolConfig.setMinEvictableIdleTimeMillis(Constants.SIXTY_SECONDS);
		mGenericObjectPoolConfig.setTestWhileIdle(Constants.ENABLE);
		mGenericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(Constants.THIRTY_SECONDS);
		mGenericObjectPoolConfig.setNumTestsPerEvictionRun(1);
		JedisPool cJedisPool = new JedisPool(mGenericObjectPoolConfig, PowinProperty.CLOUDHOST.toString(),
				Constants.REDIS_PORT, Constants.TEN_SECONDS, Constants.NO_SSL);
		JedisPoolWrapper cJedisPoolWrapper = new JedisPoolWrapper();
		cJedisPoolWrapper.setJedisPool(cJedisPool);
		GoblinCommandDepot cGoblinCommandDepot = new GoblinCommandDepot();
		cGoblinCommandDepot.setRedisEnabled(Constants.ENABLE);
		cGoblinCommandDepot.setRedisPool(cJedisPoolWrapper);
		cGoblinCommandDepot.setRedisKeyPrefix("gcd2");
		cGoblinCommandDepot.init();
		// cJedisPool.close();
		return cGoblinCommandDepot;
	}

	/**
	 * Checks the file system for the configuration file.
	 *
	 * @param turtleHost
	 * @param fileLocation TODO
	 * @return
	 */
	public static boolean isAppConfigPresent(String turtleHost, String fileLocation) {
		boolean fileExists = false;
		if ("localhost".equals(turtleHost)) {
			fileExists = (new File(fileLocation).exists());
		} else {
			fileExists = FileHelper.remoteFileExists(turtleHost, Constants.USER, Constants.PW, fileLocation);
		}

		return fileExists;
	}

	public static String setEnabledValue(String fileContents, boolean isEnable) {
		return fileContents.replace("${}", String.valueOf(isEnable));
	}

	public static File createAppConfigRemovalFile(Set<Integer> appPriorities) {
		StringBuilder mFileName = new StringBuilder("remove");
		for (Integer i : appPriorities) {
			mFileName.append("_P" + i);
		}
		mFileName.append(".sh");
		File mFile = new File(SH_FILE_URL + mFileName.toString());

		try (FileWriter mWriter = new FileWriter(mFile, true)) {
			String tomcatServiceName = CommonHelper.isSimulator() ? "tomcat" : "tomcat8";
			String catalinaDir = CommonHelper.isSimulator() ? "/opt/tomcat/logs" : "/var/log/tomcat8";
			StringBuilder mFileContent = new StringBuilder("echo \"Removing redundant app configs......\"\n");

			for (Integer i : appPriorities) {
				mFileContent.append("sudo rm -rf " + APP_CONFIG_URL + "app-" + i + "-*\n");
			}

			mFileContent.append("sudo rm -rf " + catalinaDir + "/catalina.out " + catalinaDir + "/catalina_old1.out\n");
			mFileContent.append("sudo service " + tomcatServiceName + " restart \n");
			mFileContent.append("while true ; do \n");
			mFileContent.append("echo \"Waiting for tomcat to restart...\"\n");
			mFileContent.append(
					"result=$(sudo grep -i \"Catalina.start Server startup in\" " + catalinaDir + "/catalina.out)\n");
			mFileContent.append("if [ \"$result\" ] ; then\n");
			mFileContent.append("echo \"COMPLETE!\"\n");
			mFileContent.append("echo \"Result found is $result\"\n");
			mFileContent.append("break\n");
			mFileContent.append("fi\n");
			mFileContent.append("sleep 2\n");
			mFileContent.append("done\n");

			mWriter.write(mFileContent.toString());
			mWriter.flush();
			System.out.print("Writing successfully!");
		} catch (IOException e) {
			System.out.print("Writing failed" + e.getLocalizedMessage());
		}
		return mFile;
	}

	public static void deleteAppConfigsAndRestartTomcat(Set<Integer> appPriorities) {
		StringBuilder mFileName = new StringBuilder("remove");
		for (Integer i : appPriorities) {
			mFileName.append("_P" + i);
		}
		mFileName.append(".sh");
		FileHelper.deleteExistingFile(SH_FILE_URL + mFileName.toString());
		File mfile = createAppConfigRemovalFile(appPriorities);
		String command = "sudo sh ";
		if (!Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			command += SH_FILE_URL + mFileName.toString();
			String filePath = SH_FILE_URL + mFileName.toString();
			// copyAppConfigRemovalFilToRemote(mFileName.toString());
			FileHelper.copyFileToRemote(filePath, filePath, PowinProperty.TURTLEHOST.toString(),
					PowinProperty.TURTLEPASSWORD.toString());
		} else if (null != mfile) {
			command = mFileName.toString();
			mfile.setExecutable(true, false);
		}
		ScriptHelper.runScriptRemotelyOnTurtle(command);
		CommonHelper.quietSleep(Constants.FIVE_SECONDS);
		CommonHelper.restartTurtleTomcat();
	}

//	private static void copyAppConfigRemovalFilToRemote(String fileName) {
//		String user = PowinProperty.TURTLEUSER.toString();
//		String host = PowinProperty.TURTLEHOST.toString();
//		int port = 22;
//		String password = PowinProperty.TURTLEPASSWORD.toString();
//		CommonHelper.copyLocalFiletoRemoteLocation(user, host, port, password, SH_FILE_URL, SH_FILE_URL, fileName);
//	}

	public static void main(String[] args) {
		Set<Integer> appSet = new HashSet<Integer>();
		appSet.add(10);
		appSet.add(10);
		appSet.add(8);
		appSet.add(9);

		deleteAppConfigsAndRestartTomcat(appSet);

		appSet.clear();

	}

	public static String editTimeParameter(String commandText, String timeParameterName, String timeParameterValue) {
		if (!timeParameterValue.contentEquals("")) {
			ZonedDateTime dynamicTime = DateTimeHelper.getDynamicZonedDateTime(timeParameterValue);
			long timeMilliseconds = DateTimeHelper.convertZonedDateTimeToTimestampMilliseconds(dynamicTime);
			commandText = FileHelper.editConfigFileContent(commandText, timeParameterName,
					String.valueOf(timeMilliseconds));
		}
		return commandText;
	}

	public static String editParameter(String commandText, String parameterName, String parameterValue) {
		if (!parameterValue.contentEquals("")) {
			commandText = FileHelper.editConfigFileContent(commandText, parameterName, String.valueOf(parameterValue));
		}
		return commandText;
	}
	//

	/**
	 * 
	 * @param appCode
	 * @param expectedEnabledStatus, 1 for enabled, 0 for disabled
	 */
	public static boolean verifyAppEnabledStatus(String appCode, int expectedEnabledStatus) {
		String timer = TimeOut.create(Constants.ONE_MINUTE_SECONDS);
		int appEnabledStatus = -1;
		while (appEnabledStatus != expectedEnabledStatus && !TimeOut.isExpired(timer)) {
			CommonHelper.quietSleep(4 * Constants.ONE_SECOND);
			appEnabledStatus = SystemInfo.getAppEnabledStatus(appCode);
		}
		TimeOut.remove(timer);
		return appEnabledStatus == expectedEnabledStatus;
	}
	
	public static boolean verifyAppEnabledStatus(String appCode, boolean expectedEnabledStatus) {
		int enabledStatusInt = expectedEnabledStatus?1:0;
		return verifyAppEnabledStatus( appCode, enabledStatusInt);
	}
	
	/**
	 * 
	 * @param appCode
	 * @param expectedEnabledStatus, 1 for enabled, 0 for disabled
	 */
	public static boolean verifyAppStatusMessage(String appCode, String expectedStatusMessage) {
		LOG.info("verifyAppStatusMessage:{}, {}",appCode,expectedStatusMessage);
		String timer = TimeOut.create(Constants.ONE_MINUTE_SECONDS);
		String actualStatusMessage = "One ring to rule them all";
		boolean statusMessageMatches=false;
		while (!statusMessageMatches&& !TimeOut.isExpired(timer)) {
			CommonHelper.quietSleep(4 * Constants.ONE_SECOND);
			actualStatusMessage = SystemInfo.getAppStatus(appCode);
			statusMessageMatches = actualStatusMessage.toLowerCase().contains (expectedStatusMessage);
		}
		TimeOut.remove(timer);
		
		return statusMessageMatches;
	}
	public static boolean verifyAppStatusMessage(String appCode, String expectedStatusMessage,int timeoutSeconds) {
		LOG.info("verifyAppStatusMessage:{}, {}",appCode,expectedStatusMessage);
		String timer = TimeOut.create(timeoutSeconds);
		String actualStatusMessage = "One ring to rule them all";
		boolean statusMessageMatches=false;
		while (!statusMessageMatches && !TimeOut.isExpired(timer)) {
			CommonHelper.quietSleep(4 * Constants.ONE_SECOND);
			actualStatusMessage = SystemInfo.getAppStatus(appCode);
			statusMessageMatches = actualStatusMessage.toLowerCase().contains (expectedStatusMessage);
		}
		TimeOut.remove(timer);
		
		return statusMessageMatches;
	}

}
