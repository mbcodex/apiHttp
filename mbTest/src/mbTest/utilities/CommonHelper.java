package mbTest.utilities;

//import static com.powin.modbusfiles.utilities.Constants.COBLYNAU;
//import static com.powin.modbusfiles.utilities.Constants.KNOCKER;
//import static com.powin.modbusfiles.utilities.Constants.KOBOLD;
//import static com.powin.modbusfiles.utilities.Constants.LATEST;
//import static com.powin.modbusfiles.utilities.Constants.PRIMROSE;
//import static com.powin.modbusfiles.utilities.Constants.TURTLE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.XML;
import org.json.simple.JSONObject;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.powin.dragon.app.estop.EStopAppFactory;
import com.powin.dragon.app.invertersafety.InverterSafetyAppFactory;
import com.powin.modbus.ModbusException;
import com.powin.modbusfiles.awe.InverterSafetyCommands;
import com.powin.modbusfiles.awe.NotificationCodes;
import com.powin.modbusfiles.configuration.ConfigurationFileCreator;
import com.powin.modbusfiles.configuration.DeviceAcBatteryBlockConfigFileCreator;
import com.powin.modbusfiles.configuration.DeviceAcBatteryConfigFileCreator;
import com.powin.modbusfiles.configuration.DevicePcsSimulatorConfigFileCreator;
import com.powin.modbusfiles.configuration.DevicePhoenixDcBatteryConfigFileCreator;
import com.powin.modbusfiles.configuration.StackType;
import com.powin.modbusfiles.modbus.ModbusPowinBlock;
import com.powin.modbusfiles.reports.Lastcall;
import com.powin.modbusfiles.reports.Notification;
import com.powin.modbusfiles.reports.Notifications;
import com.powin.modbusfiles.reports.SystemInfo;
import com.powin.powinwebappbase.HttpHelper;
import com.powin.tongue.fourba.command.Command;
import com.powin.tongue.fourba.command.CommandPayload;
import com.powin.tongue.fourba.command.Endpoint;
import com.powin.tongue.fourba.command.EndpointType;
import com.powin.tongue.fourba.command.ManualClearDeviceFault;

public class CommonHelper {
	private static final String FAULT = "FAULT";
	private static final String ETC_POWIN_TOOLS_PERMISSIONS_JSON = "/etc/powin/toolsPermissions.json";
	private static final String METADATA_VERSIONING_VERSIONS_VERSION = "metadata|versioning|versions|version";
	private static final String METADATA_VERSIONING_LATEST = "metadata|versioning|latest";
	private static final String OPT_TOMCAT_LOGS_CATALINA_OUT = "/opt/tomcat/logs/catalina.out";
	final static Logger LOG = LogManager.getLogger();
	private static final String RESOURCE_NAME = "default.properties";
	static String host;
	static String user;
	static String password;
	static int port = 22;

	static {
		try {
			host = PowinProperty.TURTLEHOST.toString();
			user = PowinProperty.TURTLEUSER.toString();
			password = PowinProperty.TURTLEPASSWORD.toString();
		} catch (Exception e) {
			LOG.error(String.format("%s not found", RESOURCE_NAME));
		}
	}

	public static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public static ArrayList<String> combineArrayListsElementwise(List<String> a, List<String> b, String delimiter) {
		return (ArrayList<String>) IntStream.range(0, a.size()).mapToObj(
				i -> String.join(",", Arrays.copyOfRange(a.get(i).split(","), 0, a.get(i).split(",").length - 1)) + ","
						+ a.get(i).split(",")[a.get(i).split(",").length - 1] + delimiter
						+ b.get(i).split(",")[b.get(i).split(",").length - 1])
				.collect(Collectors.toList());
	}

	public static int compareDates(String date1, String date2) throws java.text.ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");
		Date d1 = format.parse(date1);
		Date d2 = format.parse(date2);
		return d1.compareTo(d2);
	}

	/**
	 * 
	 * @param testValue			:
	 * @param referenceValue	:
	 * @param percentTolerance	:
	 * @param tolerance			:range that the values are considered to be equal 
	 * @return
	 */
	public static boolean compareIntegers(int testValue, int referenceValue, int percentTolerance,
			double tolerance) {
		//LOG.error("compareIntegers: testValue:{}, referenceValue:{}, percentTolerance:{}, tolerance:{}", testValue, referenceValue, percentTolerance,tolerance);
		boolean isIntegersMatch = false;
		int absoluteDifference = Math.abs(referenceValue - testValue);
		int percentageDifference = 0;
		if (referenceValue == 0) {
			isIntegersMatch = testValue == 0 || absoluteDifference < tolerance;
		} else {
			percentageDifference = 100 * absoluteDifference / referenceValue;
			isIntegersMatch = Math.abs(percentageDifference) <= percentTolerance;
		}
		//LOG.error("compareIntegers: isIntegersMatch:{}", isIntegersMatch );
        return isIntegersMatch;
	}

	public static boolean compareIntegers(int testValue, int referenceValue, int percentTolerance) {
		double zeroDifference = 0.01;// to compare when the reference value is 0
		return compareIntegers(testValue, referenceValue, percentTolerance, zeroDifference);

	}

	public static String convertArrayListToString(List<String> arrayList) {
		String[] arr = arrayList.toArray(new String[arrayList.size()]);
		return String.join(",", arr);
	}

	public static String convertArrayListToString(List<String> arrayList, String delimiter) {
		String[] arr = arrayList.toArray(new String[arrayList.size()]);
		return String.join(delimiter, arr);
	}

	public static String convertListToString(List<String> list) {
		String[] arr = list.toArray(new String[list.size()]);
		return String.join(",", arr);
	}

	public static String convertListOfStringsToString(List<String[]> list) {
		String[][] arrArr = list.toArray(new String[list.size()][]);
		String temp = "";
		for (String[] arr : arrArr) {
			temp += String.join(",", arr);
			temp += "\n";
		}
		return temp;
	}

	public static String convertListOfStringArraysToString(List<String[]> list, String delimiter) {
		String[][] arrArr = list.toArray(new String[list.size()][]);
		String temp = "";
		for (String[] arr : arrArr) {
			temp += String.join(",", arr);
			temp += delimiter;
		}
		return temp;
	}

	public static String convertListOfStringsToString(List<String[]> list, int onlyThisIndex) {
		String[][] arrArr = list.toArray(new String[list.size()][]);
		String temp = "";
		for (String[] arr : arrArr) {
			temp += String.join(",", arr[onlyThisIndex]);
			temp += ",";
		}
		return temp;
	}

	public static ArrayList<Integer> convertStringArrayListToIntegerArrayList(ArrayList<String> strArrayList) {
		ArrayList<Integer> tmpComputeList = new ArrayList<>();
		for (String s : strArrayList)
			tmpComputeList.add(Integer.parseInt(s));
		return tmpComputeList;
	}

	public static org.json.JSONObject convertXmlToJson(String archivaDataFile) {
		try {
			File xmlFile = new File(archivaDataFile);
			InputStream inputStream = new FileInputStream(xmlFile);
			StringBuilder builder = new StringBuilder();
			int ptr;
			while ((ptr = inputStream.read()) != -1) {
				builder.append((char) ptr);
			}
			inputStream.close();
			org.json.JSONObject jsonObj = XML.toJSONObject(builder.toString().replaceAll("vversion", "version"));// to
																													// clean
																													// up
																													// the
																													// errant"vversion"
			return jsonObj;
		} catch (IOException ex) {

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Session createSession(String user, String host, int port, String password) {
		try {
			JSch jsch = new JSch();
			Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			Session session = jsch.getSession(user, host, port);
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			return session;
		} catch (JSchException e) {
			System.out.println(e);
			return null;
		}
	}

	public static int getCollectionAverage(ArrayList<Integer> intList) {
		int sum = 0;
		for (int i : intList) {
			sum += i;
		}
		int average = sum / intList.size();
		return average;
	}

	public static double getCollectionAverage1(ArrayList<Double> list) {
		double sum = 0;
		for (double i : list) {
			sum += i;
		}
		double average = sum / list.size();
		BigDecimal bd = new BigDecimal(Double.toString(average));
		bd = bd.setScale(1, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String getLocalHome() {
		return System.getProperty("user.home") + "/";
	}

	public static String getLocalUser() {
		return System.getProperty("user.name");
	}

	public static String getMaxVersionFromVersionList(List<String> l_filter1) {
		List<Integer> l_filter_modified = new ArrayList<>();
		String listItemModified = "";
		try {
			for (String s : l_filter1) {
				listItemModified = s.split("\\.")[2].replaceAll("[^\\d.]", "");
				l_filter_modified.add(Integer.parseInt(listItemModified));
			}
		} catch (Exception e) {
			System.out.println("Error processing version list");
		}
		int maxValue = Collections.max(l_filter_modified);
		int maxIndex = l_filter_modified.indexOf(maxValue);
		return l_filter1.get(maxIndex).split("\\.")[2];
	}

	public static void getProps() {
		Thread.currentThread().getContextClassLoader().getResource("").getPath();
		System.out.println();
	}

	public static ArrayList<String> modifyTemperature(List<String> a) {
		return (ArrayList<String>) IntStream.range(0, a.size()).mapToObj(
				i -> String.join(",", Arrays.copyOfRange(a.get(i).split(","), 0, a.get(i).split(",").length - 1)) + ","
						+ modifyTemperatureString(a.get(i).split(",")[a.get(i).split(",").length - 1]))
				.collect(Collectors.toList());
	}

	public static String modifyTemperatureString(String t) {
		double d = Double.parseDouble(t) / 10;
		return Double.toString(d);

	}

	/**
	 * Assigns the permissions to a file or directory tree.
	 *
	 * @param filename
	 * @param permissions
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void setSystemFilePermissons(String filename, String permissions) {
		// executeProcess("/bin/chmod", permissions, "-R", filename);
		ScriptHelper.callScriptSudo("powin", "chmod -R " + permissions + " " + filename);
	}

	public static void setTurtleFilePermissons(String filename, String permissions) {
		ScriptHelper.runScriptRemotelyOnTurtle("sudo chmod -R " + permissions + " " + filename);
	}

	public static void enableZeroConfig() {
		ScriptHelper.executeRemoteSSHCommand(PowinProperty.TURTLEHOST.toString(), "powin", "powin",
				"echo powin | sudo -S  sed -i 's/\\(\"simplifiedSafetyConfig\".*false\\)/\"simplifiedSafetyConfig\":true/g' "
						+ Constants.CONFIGURATION_FILE);
	}

	public static void disableZeroConfig() {
		ScriptHelper.executeRemoteSSHCommand(PowinProperty.TURTLEHOST.toString(), "powin", "powin",
				"echo powin | sudo -S  sed -i 's/\\(\"simplifiedSafetyConfig\".*true\\)/\"simplifiedSafetyConfig\":false/g' "
						+ Constants.CONFIGURATION_FILE);
	}

	/**
	 * Check to see if the stack simulator is enabled by examining turtle.xml.
	 *
	 * @param host
	 * @return
	 */
	public static boolean isSimulator(String host) {
		boolean isSimulator = false;
		String simulatorString = "<Parameter name=\"phoenix.stacksimulator.enabled\" value=\"true\" />";
		if (Constants.LOCAL_HOST.equals(host)) {
			File f = new File("/opt/tomcat/conf/Catalina/localhost/turtle.xml");
			if (f.exists()) {
				String s = FileHelper.readFileAsString(f.getPath());
				isSimulator = s.contains(simulatorString);
			}
		} else {
			simulatorString = "<Parameter name=\\\"phoenix.stacksimulator.enabled\\\" value=\\\"true\\\" />";
			String command = "grep \"" + simulatorString + "\" /etc/tomcat8/Catalina/localhost/turtle.xml";
			LOG.info(command);
			isSimulator = Boolean.valueOf(
					!ScriptHelper.executeRemoteSSHCommand(host, Constants.USER, Constants.PW, command).isEmpty());
		}
		return isSimulator;
	}

	/**
	 * restart tomcat on localhost. Wait for startup to be logged in catalina.out,
	 * then wait for the start-up delay.
	 */
	public static void restartLocalTomcat() {
		LOG.info("Restarting local tomcat");
		FileUtils.deleteQuietly(new File(OPT_TOMCAT_LOGS_CATALINA_OUT));
		ScriptHelper.executeProcess("sh", "-c", "echo " + Constants.USER + " | sudo -S service tomcat restart");
		List<String> grepResults = new ArrayList<String>();
		while (grepResults.isEmpty()) {
			LOG.info("Waiting for Tomcat to restart...");
			grepResults = ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c",
					"echo " + Constants.USER
							+ " | sudo -S grep -i \"Catalina.start Server startup in\" /opt/tomcat/logs/catalina.out");
			sleep(Constants.TEN_SECONDS);
		}
		LOG.info(grepResults);
		sleep(Constants.TEN_SECONDS);
	}

	public static void stopLocalTomcat() {
		LOG.info("Stopping local tomcat");
		ScriptHelper.executeProcess("sh", "-c", "echo " + Constants.USER + " | sudo -S service tomcat stop");
		sleep(Constants.TEN_SECONDS);
	}

	public static boolean setInverterDefaults() {
		boolean isTomcatRestartNeeded = false;
		if (!"AUTOSTANDBY".contains(SystemInfo.getPCSState())) {
			InverterSafetyCommands.resetInverterSafetyLimits(Constants.TWO_MINUTES);
			LOG.info("PCS is {}", SystemInfo.getPCSState());
		}
		return isTomcatRestartNeeded;
	}

	public static void stopTomcat() {
		ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c",
				"echo powin | sudo -S service tomcat stop");
	}

	public static void restartTurtleTomcat() {
		Lastcall.stopLastCallTimer();
		if (isSimulator()) {
			restartLocalTomcat();
		} else {
			LOG.info("Restarting Tomcat on Turtle.");
			String scriptFile = "/home/powin/restartTomcat.sh";
			FileHelper.deleteExistingFile(scriptFile);
			FileHelper.createTomcatRestartFile(scriptFile);
			// Copy script app_update.sh to turtle home
			String remote = "/home/powin/";
			String fileName = "restartTomcat.sh";
			PowinProperty.TURTLEUSER.toString();
			String host = PowinProperty.TURTLEHOST.toString();
			String password = PowinProperty.TURTLEPASSWORD.toString();
			FileHelper.copyFileToRemote(scriptFile, remote, host, password);
			// Run script remotely
			String command = "sudo sh " + fileName;
			ScriptHelper.runScriptRemotelyOnTurtle(command);
		}
		try {
			Lastcall.init();
		} catch (Exception e) {
			;
		}

	}

	public static String getArchivaInfo(String app, String majorVersion, String minorVersion) {
		String username = PowinProperty.ARCHIVA_USER.toString();
		String password = PowinProperty.ARCHIVA_PASSWORD.toString();
		if (username.isEmpty() || password.toString().isEmpty()) {
			LOG.error("Enter your archiva username and password into default.properties.");
			throw new RuntimeException("Invalid Username or Password");
		}
		String maxVersion = getMaxVersion(app, majorVersion, minorVersion, username, password);
		return validateVersion(app, majorVersion, minorVersion, maxVersion, username, password);
	}

	/**
	 * The inputs to this method can vary. If the majorVersion == "LATEST" then the full version string is returned 
	 * from the xml.
	 * Otherwise the version is being built and we only want the revision number.
	 * @param app
	 * @param majorVersion
	 * @param minorVersion
	 * @param username
	 * @param password
	 * @return
	 */
	public static String getMaxVersion(String app, String majorVersion, String minorVersion, String username,
			String password) {
		String maxVersion = "";
		List<String> results = new ArrayList<>();
		JSONObject json = getAppVersionJson(app, username, password);
		if (null != json) {
			if (majorVersion.toUpperCase().equals(Constants.LATEST)) {
				results = JsonParserHelper.getFieldJSONObject(json, METADATA_VERSIONING_LATEST, "", results);
				maxVersion = results.get(0);
			} else {
				results = JsonParserHelper.getFieldJSONObject(json, METADATA_VERSIONING_VERSIONS_VERSION, "",
						results);
				List<String> l_filter = new ArrayList<>();
				results.replaceAll(s -> s.concat(".0.0"));
				l_filter = results.stream().filter(o -> o.split("\\.")[0].equals(majorVersion))
						.filter(o -> o.split("\\.")[1].equals(minorVersion)).collect(Collectors.toList());
				maxVersion = getMaxVersionFromVersionList(l_filter);
			}
		}
		return maxVersion;
	}
   
	/* 
	 * Verifiy that the max version in the xml is valid.
	 * Try to pull the headers down for the xml file that is one greater than the reported version.
	 */
	public static String validateVersion(String app, String majorVersion, String minorVersion, String maxVersion,
			String username, String password) {
		String ret = maxVersion;
		if (majorVersion.toUpperCase().equals(Constants.LATEST)) {
			ret = validateLatestVersion(app, maxVersion, username, password);
		} else {
			final String nextVersion = String.join(".", majorVersion, minorVersion, "" + (Integer.parseInt(maxVersion) + 1));
			if (checkNextVersion(app, username, password, ret, nextVersion).contains("OK")) {
				ret = nextVersion; 
			}
		}
		LOG.info(ret);
		return ret;
	}
	
	private static String validateLatestVersion(String app, String maxVersion, String username, String password) {
		String ret = maxVersion;
		String[] splitVersion = maxVersion.split("\\.");
		String nextVersion = String.join(".", splitVersion[0], splitVersion[1],
				String.valueOf(Integer.parseInt(splitVersion[2]) + 1));
		if ( checkNextVersion(app, username, password, ret, nextVersion).contains("OK") ) {
			ret = nextVersion;
		}
		return ret;
	}

	
	private static String checkNextVersion(String app, String username, String password, String ret,
			final String nextVersion) {
		List<String> responseStatusNext = ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT,
				Constants.CAPTURE, "sh", "-c",
				"curl -k --silent -I -u " + username + ":" + password
						+ " https://archiva.powindev.com/repository/internal/com/powin/" + app + "/" + nextVersion
						+ "/" + app + "-" + nextVersion + ".war");
		return responseStatusNext.get(0);
	}

	public static List<String> getAppVersionList(String app, String username, String password) {
		List<String> results = new ArrayList<>();
		JSONObject json = getAppVersionJson(app, username, password);
		if (null != json) {
		    JsonParserHelper.getFieldJSONObject(json, METADATA_VERSIONING_VERSIONS_VERSION, "",
				results);
		}
		return results;
	}
	
    /**
     * Build a map of versions to use in the UI.
     * @param app
     * @param user
     * @param password
     * @return
     */
	public static Map<String, Map<String, List<String>>> getAppVersionsMap(String app, String user, String password) {
		List<String> result = CommonHelper.getAppVersionList(app, user, password);
		List<String> majors = result.stream().map(version -> { return version.split("\\.")[0]; }).distinct().collect(Collectors.toList());
		Map<String, List<String>> major_VersionMap = new HashMap<>();
		Map<String, List<String>> major_MinorMap = new HashMap<>();
		Map<String, List<String>> minor_versionsMap = new HashMap<>();
		Map<String, Map<String, List<String>>> versionsMap = new HashMap<>();

		majors.forEach( major -> {
			List<String> versions = result.stream().filter(version -> { return
			    version.split("\\.")[0].equals(major);
			}).collect(Collectors.toList());
			major_VersionMap.put(major, versions);
		});
		
		major_VersionMap.forEach((k, v) -> { 
			 List<String> minor = v.stream().map(e -> e.split("\\.").length > 1 ? e.split("\\.")[1] : "0").distinct().collect(Collectors.toList());
			 major_MinorMap.put(k, minor);
		});
		
		majors.forEach( major -> {
			List<String> versions = major_VersionMap.get(major);
			List<String> lminors = major_MinorMap.get(major);
			lminors.forEach(m -> {
				List<String> revs = versions.stream().filter(v -> m.equals(v.split("\\.").length > 1 ? v.split("\\.")[1] : "0"))
                        .map(v -> v.split("\\.").length > 2 ? v.split("\\.")[2] : "0").collect(Collectors.toList());
			    minor_versionsMap.put(m, revs);	
			});
			versionsMap.put(major, minor_versionsMap);
		});
		return versionsMap;
	}
	
	public static JSONObject getAppVersionJson(String app, String username, String password) {
		String archivaDataFile = getLocalHome() + "archivaData.xml";
		String cmd = "wget  --no-check-certificate --user=" + username + " --password "
				+ password + " -O /" + archivaDataFile
				+ " https://archiva.powindev.com/repository/internal/com/powin/" + app + "/maven-metadata.xml";
		int exitVal=1;
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
			exitVal = process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (exitVal == 0) {
			String jsonText = convertXmlToJson(archivaDataFile).toString();
			return JsonParserHelper.parseJsonFromString(jsonText);
		} else {
			return null;
		}
	}

	/**
	 * Get the latest app version for the app from archiva.
	 *
	 * @param app
	 * @return
	 */
	public static String getLatestAppVersion(String app) {
		return CommonHelper.getArchivaInfo(app, LATEST, null);
	}

	public static void getTurtleLog(String file) throws IOException {
		// Runs this command in turtle (10.0.0.3)
		String command = "sudo scp /var/log/tomcat8/turtle.log /home/powin/";
		ScriptHelper.runScriptRemotelyOnTurtle(command);
		sleep(Constants.TEN_SECONDS);
		String command2 = "sudo chmod 777 /home/powin/turtle.log";
		ScriptHelper.runScriptRemotelyOnTurtle(command2);
		sleep(3000);
		FileHelper.copyFileFromTurtleHomeToLocalHome("turtle.log", file);
	}

	/**
	 * Wait for file to appear on the file system.
	 *
	 * @param localScriptFile
	 * @param seconds         - The number of seconds to wait for the file.
	 * @throws InterruptedException
	 */
	public static void waitForFile(String localScriptFile, int seconds) {
		File f = new File(localScriptFile);
		int i = 0;
		while (!f.exists() && i++ < seconds) {
			quietSleep(1000);
		}
	}

	public static void quietSleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage());
		}
	}

	public static void sleep(long ms) {
		if (ms > 10000) {
			LOG.info("Delay for {} seconds", ms / 1000);
		}
		quietSleep(ms);
	}

	public static void sleep(long ms, String msg) {
		if (ms > 10000) {
			LOG.info("{}\nDelay for {} seconds", msg, ms / 1000);
		}
		quietSleep(ms);
	}

	/**
	 * @param limit
	 * @return a random integer between 0 and limit
	 */
	public static int getRandomNumber(int limit) {
		return (int) Math.round(limit * Math.random());
	}

	public static boolean checkForContactorStatusViaReport(boolean closedContactorsExpected, int timeoutSeconds,
			String customizeLog) {
		boolean statusPassed = false;
		String timer = TimeOut.create(timeoutSeconds);
		while (!(statusPassed || TimeOut.isExpired(timer))) {
			sleep(4*Constants.ONE_SECOND);
			LOG.info("statusPassed={}, elapsed={}", statusPassed, TimeOut.elapsed(timer));
			if (closedContactorsExpected) {
				statusPassed = SystemInfo.getStringContactorStatus();
			} else {
				statusPassed = !SystemInfo.getStringContactorStatus();
			}
		}
		String resultStatus = closedContactorsExpected ? "closed" : "open";
		if (statusPassed) {
			TimeOut.remove(timer);
			LOG.info("PASS: contactors {}... " + customizeLog, resultStatus);
		} else {
			LOG.info("FAIL: contactors are not {}... " + customizeLog, resultStatus);
		}
		return statusPassed;
	}

	/**
	 *
	 * @param expected
	 * @param errorCodes
	 * @param arrayIndex
	 * @param stringIndex
	 * @return
	 * @throws IOException
	 * @throws ModbusException
	 */
	public static boolean didNotificationAppear(NotificationCodes expected, List<NotificationCodes> errorCodes,
			int arrayIndex, int stringIndex) {
		List<String> notificationList = getNotifications(arrayIndex);
		// List<String> errorCodeStrings = errorCodes.stream().map(e ->
		// e.toString()).collect(Collectors.toList());
		boolean ret = notificationList.contains(expected.toString());
		// is error code present
		// notificationList.retainAll(errorCodeStrings);
		// if (!notificationList.isEmpty()) {
		// throw new ModbusException(String.format("%s was detected abort run.",
		// errorCodeStrings.get(0)));
		// }
		return ret;
	}

	public static List<String> getNotifications(int arrayIndex) {
		Notifications notifications = new Notifications(Integer.toString(arrayIndex));
		List<String> notificationList = notifications.getNotificationsInfo();
		return notificationList;
	}

	/**
	 * Sort a list of comma separated strings by the sub-string (field) and  sub-string data type.
	 *
	 * @param rawList
	 * @param ascendingOrder
	 * @param zeroBasedIndex
	 * @param substringType
	 * @return
	 */
	public static List<String> sortListBySubstring(List<String> rawList, boolean ascendingOrder, int zeroBasedIndex,
			String substringType) {
		List<String> sortedList = new ArrayList<String>();
		sortedList.addAll(rawList);
		if (substringType.toUpperCase().contains("INT")) {
			if (ascendingOrder)
				sortedList.sort(Comparator.comparing(s -> Integer.parseInt(s.split(",")[zeroBasedIndex])));
			else
				sortedList.sort(Comparator.comparing(s -> Integer.parseInt(s.split(",")[zeroBasedIndex]),
						Comparator.reverseOrder()));

		} else if (substringType.toUpperCase().contains("DOUBLE")) {
			if (ascendingOrder)
				sortedList.sort(Comparator.comparing(s -> Double.parseDouble(s.split(",")[zeroBasedIndex])));
			else
				sortedList.sort(Comparator.comparing(s -> Double.parseDouble(s.split(",")[zeroBasedIndex]),
						Comparator.reverseOrder()));

		} else if (substringType.toUpperCase().contains("STR")) {
			if (ascendingOrder)
				sortedList.sort(Comparator.comparing(s -> s.split(",")[zeroBasedIndex]));
			else
				sortedList.sort(Comparator.comparing(s -> s.split(",")[zeroBasedIndex], Comparator.reverseOrder()));

		}

		return sortedList;

	}

	@SuppressWarnings("resource")
	public static String getKoboldVersion(String koblodLocation) {
		String mVersion = "unknown";
		HttpGet httpGet = new HttpGet(koblodLocation);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2500).setConnectionRequestTimeout(2500)
				.setSocketTimeout(2500).build();
		httpGet.setConfig(requestConfig);
		HttpClient mHttpClient = null;
		try {
			mHttpClient = HttpHelper.buildHttpClient(true);
			HttpResponse response = mHttpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity, "UTF-8");
				int beginIndex = result.indexOf("Version") + 8;
				int endIndex = result.length();
				if (beginIndex != -1 && endIndex != -1 && endIndex > beginIndex) {
					mVersion = result.substring(beginIndex, endIndex);
				}
				if (mVersion.length() > 30) {
					mVersion = "unknown";
				}
			}

		} catch (Exception e) {
			mVersion = "unknown";
		}
		finally {
			try {
				((CloseableHttpClient)mHttpClient).close();
			} catch (IOException e) {
				;
			}
		}
		return mVersion.replaceAll("\r|\n", "");
	}

	public static String getTurtleVersion(String turtleLocation) {
		String mVersion = "unknown";
		HttpGet httpGet = new HttpGet(turtleLocation);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2500).setConnectionRequestTimeout(2500)
				.setSocketTimeout(2500).build();
		httpGet.setConfig(requestConfig);
		try {
			HttpClient mHttpClient = HttpHelper.buildHttpClient(true);
			HttpResponse response = mHttpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity, "UTF-8");
				int beginIndex = result.indexOf("Version") + 8;
				int endIndex = result.indexOf("\n\r");
				if (beginIndex != -1 && endIndex != -1 && endIndex > beginIndex) {
					mVersion = result.substring(beginIndex, endIndex);
				}
				if (mVersion.length() > 30) {
					mVersion = "unknown";
				}
			}

		} catch (Exception e) {
			mVersion = "unknown";
		}
		return mVersion.replaceAll("\r|\n", "");
	}

	public static String getHttpGetResponseString(String requestUrl) {
		LOG.info("getHttpGetResponseString {}", requestUrl);
		String mResponseString = "unknown";
		HttpGet httpGet = new HttpGet(requestUrl);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2500).setConnectionRequestTimeout(2500)
				.setSocketTimeout(2500).build();
		httpGet.setConfig(requestConfig);
		try {
			HttpClient mHttpClient = HttpHelper.buildHttpClient(true);
			HttpResponse response = mHttpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				mResponseString = EntityUtils.toString(entity, "UTF-8");
			}
		} catch (Exception e) {
			mResponseString = "unknown";
		}
		return mResponseString.replaceAll("\r|\n", "");
	}

	public static void createDeviceConfigurtionFiles(String deviceFolderLocation, int arrayCount, int pcsMaxPower,
			int pcsMaxCurrent) {
		// PhoenixDcBattery: device-10-PhoenixDcBattery.json
		DevicePhoenixDcBatteryConfigFileCreator PhoenixDcBattery = new DevicePhoenixDcBatteryConfigFileCreator();
		PhoenixDcBattery.createPhoenixDcBatteryConfigFiles(deviceFolderLocation, arrayCount, 10000, true, 10, 10);
		// PcsSimulator : device-20-PcsSimulator.json
		DevicePcsSimulatorConfigFileCreator PcsSimulator = new DevicePcsSimulatorConfigFileCreator();
		PcsSimulator.createPcsSimulatorConfigFiles(deviceFolderLocation, arrayCount, "GRID_FOLLOWING", false,
				pcsMaxPower, pcsMaxPower, -pcsMaxPower, -pcsMaxPower, pcsMaxCurrent, pcsMaxPower, pcsMaxPower, true, 20,
				20);
		// AcBattery: device-50-AcBattery.json
		DeviceAcBatteryConfigFileCreator AcBattery = new DeviceAcBatteryConfigFileCreator();
		AcBattery.createAcBatteryConfigFiles(deviceFolderLocation, arrayCount, true, 50, 50);
		// AcBatteryBlock: device-60-AcBatteryBlock.json
		DeviceAcBatteryBlockConfigFileCreator AcBatteryBlock = new DeviceAcBatteryBlockConfigFileCreator();
		AcBatteryBlock.createAcBatteryBlockConfigFiles(deviceFolderLocation, arrayCount, true, true, 60, 60);
	}

	public static void createAcDevicesConfigurtionFiles(String deviceFolderLocation, int arrayCount, int pcsMaxPower,
			int pcsMaxCurrent) {
		// PhoenixDcBattery: device-10-PhoenixDcBattery.json
		DevicePhoenixDcBatteryConfigFileCreator PhoenixDcBattery = new DevicePhoenixDcBatteryConfigFileCreator();
		PhoenixDcBattery.createPhoenixDcBatteryConfigFiles(deviceFolderLocation, arrayCount, 10000, true, 10, 10);
		// PcsSimulator : device-20-PcsSimulator.json
		DevicePcsSimulatorConfigFileCreator PcsSimulator = new DevicePcsSimulatorConfigFileCreator();
		PcsSimulator.createPcsSimulatorConfigFiles(deviceFolderLocation, arrayCount, "GRID_FOLLOWING", false,
				pcsMaxPower, pcsMaxPower, -pcsMaxPower, -pcsMaxPower, pcsMaxCurrent, pcsMaxPower, pcsMaxPower, true, 20,
				20);
		// AcBattery: device-50-AcBattery.json
		DeviceAcBatteryConfigFileCreator AcBattery = new DeviceAcBatteryConfigFileCreator();
		AcBattery.createAcBatteryConfigFiles(deviceFolderLocation, arrayCount, true, 50, 50);
		// AcBatteryBlock: device-60-AcBatteryBlock.json
		DeviceAcBatteryBlockConfigFileCreator AcBatteryBlock = new DeviceAcBatteryBlockConfigFileCreator();
		AcBatteryBlock.createAcBatteryBlockConfigFiles(deviceFolderLocation, arrayCount, true, true, 60, 60);
	}

	public static void upDateDeviceFolder(int arrayCount, int pcsMaxPower, int pcsMaxCurrent)
			throws IOException, InterruptedException {
		// Create random folder in local home folder
		String randomFolderName = "foo" + Math.round(100000 * Math.random());
		String makeDirectoryCommand = "sudo mkdir ~/" + randomFolderName + " && sudo chmod -R 777 ~/"
				+ randomFolderName;
		ScriptHelper.callScriptSudo("powin", makeDirectoryCommand);
		// Create device files and place in the random folder
		createDeviceConfigurtionFiles("/home/powin/" + randomFolderName + "/", arrayCount, pcsMaxPower, pcsMaxCurrent);
		// Create folder with same name as the random one in turtle home
		ScriptHelper.runScriptRemotelyOnTurtle(makeDirectoryCommand);
		Thread.sleep(2000);
		// Copy random folder contents to the random folder created in turtle home
		FileHelper.copyFileFromLocalHomeToEtcPowinHome(randomFolderName + "/device-*.json", randomFolderName + "/");
		// Back up the existing files in turtle device folder
		String backupDeviceFolderFilesCommand = "sudo cp -a /etc/powin/device/. /etc/powin/deviceBackup/ && sleep 5";
		ScriptHelper.runScriptRemotelyOnTurtle(backupDeviceFolderFilesCommand);
		// Change permissions of device folder
		String changeFolderPermissionsDeleteDeviceFolderFilesCommand = "sudo chmod -R 777 /etc/powin/device/ && sleep 5";
		ScriptHelper.runScriptRemotelyOnTurtle(changeFolderPermissionsDeleteDeviceFolderFilesCommand);
		// Change owners of files
		String changeOwnersFolderFilesCommand = "sudo chown -R powin:powin /etc/powin/device/ && sleep 5";
		ScriptHelper.runScriptRemotelyOnTurtle(changeOwnersFolderFilesCommand);
		// delete all files in device folder
		FileHelper.removeFiles("/etc/powin/device/*.json");// TO DO: for remote files the pattern should be "*.json"
		// Copy the .json files in the random folder in turtle home to the device folder
		FileHelper.copyFilesToTurtle("/home/powin/" + randomFolderName + "/device-*.json", "/etc/powin/device/");
		FileHelper.setFullPermissions("/etc/powin/device/");
		Thread.sleep(2000);
	}

	/**
	 * Creates a configuration file for Turtle.
	 *
	 * @param stationName - StationName "QAWARN" for regression tests for example.
	 * @param stackType   - Stack type for stack specific zero config
	 * @param stringCount - Number of strings desired
	 * @param arrayCount  - Number of arrays / string
	 * @throws IOException
	 */
	public static void createConfigurationJson(String stationName, StackType stackType, int stringCount, int arrayCount)
			throws IOException {
		String modifiedStationName = SystemInfo.getStationNameWithRetry(stationName);
		// Create a random folder in local home directory
		String randomFolderName = FileHelper.createDynamicFolder("foo", "/home/powin/");
		FileHelper.setFullPermissions(randomFolderName);
		// Create configuration.json and place within the random folder
		ConfigurationFileCreator mCreator = new ConfigurationFileCreator();
		mCreator.CreateConfigurationFile(randomFolderName, modifiedStationName, stackType, stringCount, arrayCount);
		// Backup the existing configuration.json and delete the existing one
		FileHelper.backupAndDeleteFile("/etc/powin/configuration.json");
		// Copy configuration.json into turtle folder
		FileHelper.copyFilesToTurtle(randomFolderName + "configuration.json", "/etc/powin/");
		CommonHelper.quietSleep(2000);
	}

	public static boolean setupSystem(String stationCode, StackType stackType, int arrayCount, int stringCount,
			int pcsNameplatePower, int pcsNameplateCurrent) throws IOException, InterruptedException {
		boolean restartTomcatNeeded = true;//checkCurrentConfiguration(stationCode, stackType, arrayCount, stringCount, arrayCount*pcsNameplatePower, 0);
		if (restartTomcatNeeded) {
			createConfigurationJson(stationCode, stackType, stringCount, arrayCount);
			upDateDeviceFolder(arrayCount, pcsNameplatePower, pcsNameplateCurrent);
			LOG.info("stationCode={}, restartTomcat={}", stationCode, restartTomcatNeeded);
		} else {
			LOG.info("Current configuration is correct.");
		}
		return restartTomcatNeeded;
	}

	public static boolean checkCurrentConfiguration(String stationCode, StackType stackType, int arrayCount,
			int stringCount, int pcsNameplatePower, int pcsNameplateCurrent) {
		boolean isRestartRequired = false;
		isRestartRequired |= !SystemInfo.getStationCode().equals(stationCode);
		isRestartRequired |= SystemInfo.getNumberOfArrays() != arrayCount;
		isRestartRequired |= SystemInfo.getNumberOfStacks() != stringCount;
		isRestartRequired |= SystemInfo.getStackType() != stackType;
		isRestartRequired |= SystemInfo.getNameplateKw() != pcsNameplatePower;
		isRestartRequired |= SystemInfo.getNameplateCurrent() != pcsNameplateCurrent;
		return isRestartRequired;
	}

	public static Map<String, String> getLatestAppVersions() {
		String knockerVersion = CommonHelper.getLatestAppVersion(KNOCKER);
		String coblynauVersion = CommonHelper.getLatestAppVersion(COBLYNAU);
		String turtleVersion = CommonHelper.getLatestAppVersion(TURTLE);
		String primroseVersion = CommonHelper.getLatestAppVersion(PRIMROSE);
		String koboldVersion = CommonHelper.getLatestAppVersion(KOBOLD);
		Map<String, String> appVersions = StringUtils.splitToMap(
				new String[] { Constants.KOBOLD, koboldVersion, Constants.KNOCKER, knockerVersion, Constants.COBLYNAU,
						coblynauVersion, Constants.TURTLE, turtleVersion, Constants.PRIMROSE, primroseVersion });
		return appVersions;
	}

	/**
	 * Set the initial
	 *
	 * @param soc
	 */
	public static void setSoC(int soc) {
		if (CommonHelper.isSimulator()) {
			String socFiles = "/etc/powin/soc";
			File dir = new File(socFiles);
			String[] socfiles = dir.list();
			for (String f : socfiles) {
				FileHelper.writeStringToFile(socFiles + "/" + f, String.valueOf(soc / 100.0));
			}

		} else {
			String cSoCUpdateDirectory = "/home/powin/";
			String cSoCUpdateFile = "socUpdate.sh";
			String catalinaDir = AppUpdate.getCatalinaPath();
			String tomcatService = AppUpdate.getTomcatServiceName();
			File mFile = new File(cSoCUpdateDirectory + cSoCUpdateFile);
			mFile.setExecutable(true, false);

			try (FileWriter mWriter = new FileWriter(mFile, false)) {
				mWriter.write("echo #!/bin/bash\n" + "echo \"Halting Tomcat\"\n" + "sudo service " + tomcatService
						+ " stop \n" + "sleep 15 \n" + "echo \"Setting SoC,..\"\n" + "cd /etc/powin/soc \n"
						+ "for file in $(ls); do \n" + "sudo echo " + soc / 100.0 + " > $file \n"
						+ "echo $file now contains $(cat $file); echo \n" + "done \n" + "cd /home/powin \n"
						+ "sleep 5 \n" + "echo \"Restarting Tomcat...\"\n" + "sudo rm -rf " + catalinaDir
						+ "/catalina.out " + catalinaDir + "/catalina_old1.out\n" + "sudo service " + tomcatService
						+ " restart \n" + "while true ; do \n" + "echo \"Waiting for tomcat to restart...\"\n"
						+ "result=$(grep -i \"Catalina.start Server startup in\" " + catalinaDir
						+ "/catalina.out) # -n shows line number \n" + "if [ \"$result\" ] ; then \n"
						+ "echo \"COMPLETE!\" \n" + "echo \"Result found is $result\"\n" + "break \n" + "fi \n"
						+ "sleep 2\n" + "done" + "");
				mWriter.flush();
				System.out.print("Writing SoC successful!");
				FileHelper.copyFileToRemote(cSoCUpdateDirectory + cSoCUpdateFile, cSoCUpdateDirectory,
						PowinProperty.TURTLEHOST.toString(), Constants.PW);
				String command = "echo " + Constants.PW + " | sudo -S " + cSoCUpdateDirectory + cSoCUpdateFile;
				ScriptHelper.runScriptRemotelyOnTurtle(command);
			} catch (IOException e) {
				System.out.print("Writing failed" + e.getLocalizedMessage());
			}
		}
	}
	
	public static void setSocStackSimulator(int soc) {
		if (CommonHelper.isSimulator()) {
			FileHelper.copyTestResourceFileToDir("stacksimulator.json", "/etc/powin/");
			FileHelper.setFullPermissions("/etc/powin/stacksimulator.json");
			//FileHelper.setFileOwner("/etc/powin/derate/derate-*.json","tomcat","tomcat");
			FileHelper.editConfigurationFile("/etc/powin/stacksimulator.json", "initialSoc",  String.valueOf(soc),"",false);
		} 
	}

	public static boolean isSimulator() {
		return Constants.LOCAL_HOST.equals(PowinProperty.TURTLEHOST.toString());
	}

	public static boolean resetFaults() {
		if (FAULT.equalsIgnoreCase(SystemInfo.getPCSState())) {
			ManualClearDeviceFault mManualClearDeviceFault = ManualClearDeviceFault.newBuilder()
					.setEntityKey(SystemInfo.getEntityKey(SystemInfo.EntityType.PCS)).build();

			Command mCommand = Command.newBuilder().setCommandId(UUID.randomUUID().toString())
					.setCommandSource(Endpoint.newBuilder().setEndpointType(EndpointType.GOBLIN))
					.setCommandTarget(Endpoint.newBuilder().setEndpointType(EndpointType.BLOCK)
							.setStationCode(SystemInfo.getStationCode()).setBlockIndex(1))
					.setCommandPayload(CommandPayload.newBuilder().setManualClearDeviceFault(mManualClearDeviceFault))
					.build();

			CommandHelper.getGoblinCommandDepot().addCommand("admin", mCommand);
			CommonHelper.quietSleep(Constants.FIVE_SECONDS);
		}
		return !FAULT.equalsIgnoreCase(SystemInfo.getPCSState());
	}

	/**
	 * Wait for system to got back to ready status after an alarm clears or the
	 * turtle is restarted. Note: don't call this until the system is up.
	 *
	 * @return
	 */
	public static boolean waitForSystemReady() {
		LOG.info("Waiting for System to be ready.");
		String timer = TimeOut.create(Constants.ONE_MINUTE_SECONDS * 2);
		while (!TimeOut.isExpired(timer)) {
			if (SystemInfo.getAppStatus(EStopAppFactory.APPCODE).toLowerCase().contains("no trips")
					&& SystemInfo.getAppStatus(InverterSafetyAppFactory.APPCODE).contains("GOOD")) {
				TimeOut.remove(timer);
				return true;
			}
			resetFaults();
			ModbusPowinBlock.getModbusPowinBlock().disableBasicOp();
			quietSleep(Constants.TEN_SECONDS);
		}
		LOG.info("System not ready within two minute limit.");
		return false;
	}

	/**
	 * Prompt user to press enter. Use this to pause an integration test. Useful for
	 * tracking down timing issues.
	 * To use set the LocalDebug=true environment variable.
	 */
	public static void pressEnterToContinue() {
		if (Boolean.getBoolean("LocalDebug")) {
			System.out.println("Press enter to  continue...");
			try {
				System.in.read();
			} catch (IOException e) {
				;
			}
		}
	}

	public static boolean isZeroConfigEnabled() {
		List<String> response = ScriptHelper.executeRemoteSSHCommand(PowinProperty.TURTLEHOST.toString(), "powin",
				"powin", "echo powin | sudo -S grep simplifiedSafetyConfig /etc/powin/configuration.json");
		LOG.info(Arrays.asList(response.get(0).split("[:,]")));
		String trueOrFalse = response.get(0).split("[:,]")[1].trim();
		return Boolean.parseBoolean(trueOrFalse);
	}

	public static String convertIntegerListToString(List<Integer> integerList) {
		int[] arr = integerList.stream().mapToInt(Integer::intValue).toArray();
		return Arrays.toString(arr);
	}

	public static double stdDeviation(List<Integer> a) {
		int sum = 0;
		double mean = a.stream().mapToDouble(d -> d).average().orElse(0.0);

		for (Integer i : a)
			sum += Math.pow((i - mean), 2);
		return Math.sqrt(sum / (a.size())); // sample
	}

	public static int getSumListSection(List<String> inputList, int sampleIndex) {
		int sum = inputList.stream().map(d -> Integer.parseInt(d.split(",")[sampleIndex])).mapToInt(Integer::valueOf)
				.sum();
		return sum;
	}

	public static double roundDouble(double doubleValue, int roundingPlaces) {
		BigDecimal bd = BigDecimal.valueOf(doubleValue);
		bd = bd.setScale(roundingPlaces, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static List<String> getListSection(List<String> inputList, int extractIndex) {
		List<String> outputList = inputList.stream().map(d -> d.split(",")[extractIndex]).collect(Collectors.toList());
		return outputList;
	}

	public static boolean isOdd(int number) {
		return number % 2 == 1;
	}
	
    /**
     * TODO: This will not be necessary when we set the system from a tgz file.
     * Check that flags in tools permissions file are enabled, if not then set them and return
     * that a tomcat restart is needed or not.
     * @return
     */
	public static boolean enableTurtleTools() {
		boolean turtleToolsAlreadyEnabled = isTurtleToolsEnabled();
		boolean restartTomcatNeeded = !turtleToolsAlreadyEnabled;
		if(!turtleToolsAlreadyEnabled) {
			FileHelper.copyTestResourceFileToDir("toolsPermissions.json", "/etc/powin");
			List<String> fields = JsonParserHelper.getFieldsFromJsonFile(ETC_POWIN_TOOLS_PERMISSIONS_JSON);
			String fileContent = FileHelper.getConfigFileContents(ETC_POWIN_TOOLS_PERMISSIONS_JSON,"");
			for (String flag : fields) {
				fileContent = FileHelper.editConfigFileContent(fileContent, flag,String.valueOf(true));
			}
			FileHelper.writeConfigFile(Constants.TURTLE_HOST, ETC_POWIN_TOOLS_PERMISSIONS_JSON,fileContent.toString());
		}
		return restartTomcatNeeded;
	}

	public static boolean isTurtleToolsEnabled() {
		List<String> fields = JsonParserHelper.getFieldsFromJsonFile(ETC_POWIN_TOOLS_PERMISSIONS_JSON);
		String [] enableFlags = {"uiEnabled", "reportEnabled", "monitorEnabled", "controlEnabled", "isTurtleToolsEnabled"};
		boolean turtleToolsEnabled=true;
		for (String flag : fields) {
			try {
				turtleToolsEnabled &= "true".contentEquals(FileHelper.getFieldValueFromConfigFilePath( flag ,ETC_POWIN_TOOLS_PERMISSIONS_JSON));
			} catch (Exception e) {
                 turtleToolsEnabled=false;
			}
		}
		return turtleToolsEnabled;
	}

	public static List<String> getPostgresQueryResults(String sqlQuery, String outputFilepath) {
		String compositeCommand="psql -U coblynaudbuser -d coblynaudb -c ";
		compositeCommand += sqlQuery;
		if(!outputFilepath.isEmpty()) {
			compositeCommand +=" > "+outputFilepath;
		}
		List<String> results=ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sh","-c",compositeCommand);
		return results;
	}
	
	public static void logCaller() {
		StackTraceElement[] stackTrace = (new Exception()).getStackTrace();
		for (int i = 0; i < 10; ++i) {
			LOG.info(stackTrace[i]);
		}
	}

	// TODO multiple returns
	public static boolean verifyNotificationExistence(int expireTimeSeconds, int notificationCode, boolean existExpected) {
		String timer = TimeOut.create(expireTimeSeconds);
		while (!TimeOut.isExpired(timer)) {
			List<Notification> notifications = Lastcall.getNotificationList();
			if (notifications != null) {
				if (existExpected) {
					if (notifications.size() > 0 && notifications.stream().anyMatch(notification -> notification.getNotificationType().getNotificationId().contains(String.valueOf(notificationCode)))) {
						TimeOut.remove(timer);
						return true;
					}
				} else {
					if (notifications.size() == 0 || notifications.stream().anyMatch(notification -> notification.getNotificationType().getNotificationId().contains(String.valueOf(notificationCode))) == false) {
						TimeOut.remove(timer);
						return true;
					}
				}
				break;
			}
			CommonHelper.quietSleep(1000);
		}
		return false;
	}
// Maybe this can be generic
//	public static <T extends TimerTask> void scheduleTimer(Timer timer, T timerTask, Class<T> clazz, int delay, int period) {
//		if (null != timerTask) {
//		   timerTask.cancel();
//		}
//		if (null != timer) {
//			timer.purge();
//		}
//		try {
//			timerTask = clazz.newInstance();
//		} catch (InstantiationException | IllegalAccessException e) {
//		}
//		timer.schedule(timerTask, delay, period);
//	}

	public static List<List<Long>> convertDelimitedStringListToListOfLongLists(List<String> sourceData){
		List<String[]> stringArrayList=sourceData.stream()
							.map(x->x.split("\\|"))
							.collect(Collectors.toList());
		List<List<String>> listOfStringLists= stringArrayList.stream()	
							.map(x->Arrays.asList(x))
							.collect(Collectors.toList());
		List<List<Long>> listOfLongLists= listOfStringLists
						.stream()
						.map(s -> convertStringListToLongList(s))
						.collect(Collectors.toList())	;
		return listOfLongLists;
	}
	
	public static List<List<Double>> convertDelimitedStringListToListOfDoubleLists(List<String> sourceData){
		List<String[]> stringArrayList=sourceData.stream()
							.map(x->x.split("\\|"))
							.collect(Collectors.toList());
		List<List<String>> listOfStringLists= stringArrayList.stream()	
							.map(x->Arrays.asList(x))
							.collect(Collectors.toList());
		return convertListListStringToListListDouble(listOfStringLists);
	}

	public static List<List<Double>> convertListListStringToListListDouble(List<List<String>> listOfStringLists) {
		List<List<Double>> listOfDoubleLists= listOfStringLists
						.stream()
						.map(s -> convertStringListToDoubleList(s))
						.collect(Collectors.toList())	;
		return listOfDoubleLists;
	}
	
	public static List<Long> convertStringListToLongList(List<String> strList){
		//return strList.stream().map(Long::valueOf).collect(Collectors.toList());
		return strList.stream()
				.map(s -> Long.parseLong(s.trim()))
				.collect(Collectors.toList());	
	}
	
	public static List<Double> convertStringListToDoubleList(List<String> strList){
		//return strList.stream().map(Long::valueOf).collect(Collectors.toList());
		return strList.stream()
				.map(s -> Double.parseDouble(s.trim()))
				.collect(Collectors.toList());	
	}
	
	public static List<String> replaceTextInStringList(List<String> strList, String textToReplace, String replacementText){
		return strList.stream()
				.map(s -> s.replaceAll(textToReplace,replacementText))
				.collect(Collectors.toList());	
	}
	
	public static  double getBiLinearInterpolatedValue(double inputX, double inputY,double x1, double x2, double y1, double y2, double Q11, double Q21, double Q12, double Q22) {
		double soh1 = CommonHelper.getInterpolatedValue(inputX, x1,x2, Q11,Q21);
		double soh2 = CommonHelper.getInterpolatedValue(inputX, x1,x2, Q12,Q22);
		double soh3 = CommonHelper.getInterpolatedValue(inputY, y1,y2, soh1,soh2);
		return soh3;
	}

	public static double getInterpolatedValue(ArrayList<String> targetArrayList, double setTemperature, String mode) {
		double interpolatedPRate=0.0;
		int lowerKey=0;
		int upperKey=0;
		double lowerValue=0;
		double upperValue=0;
		int currentIndex = 0;
		int strategyListTemperature=0;
		
		int strategyListTemperatureLowLimit = Integer.parseInt(targetArrayList.get(0).split(",")[0]);
		int strategyListTemperatureHighLimit = Integer.parseInt(targetArrayList.get(targetArrayList.size()-1).split(",")[0]);
		double strategyListPrateLowLimit = Double.parseDouble(targetArrayList.get(0).split(",")[1]);
		double strategyListPrateHighLimit = Double.parseDouble(targetArrayList.get(targetArrayList.size()-1).split(",")[1]);
		if(setTemperature <strategyListTemperatureLowLimit) {//below range
			interpolatedPRate=strategyListPrateLowLimit;
		}
		else if(setTemperature >strategyListTemperatureHighLimit) {//above range
			interpolatedPRate=strategyListPrateHighLimit;
		}
		else {//within range
			for (currentIndex = 0; currentIndex < targetArrayList.size(); currentIndex++) {
				strategyListTemperature = Integer.parseInt(targetArrayList.get(currentIndex).split(",")[0]);
				if (setTemperature <= strategyListTemperature) {
					break;
				}
			}
			if(setTemperature == strategyListTemperature) {
				interpolatedPRate=Double.parseDouble(targetArrayList.get(currentIndex).split(",")[1]);
			}else {
				upperKey=Integer.parseInt(targetArrayList.get(currentIndex).split(",")[0]);;
				lowerKey=Integer.parseInt(targetArrayList.get(currentIndex-1).split(",")[0]);;
				upperValue=Double.parseDouble(targetArrayList.get(currentIndex).split(",")[1]);
				lowerValue=Double.parseDouble(targetArrayList.get(currentIndex-1).split(",")[1]);
				double slope=(upperValue-lowerValue)/(upperKey-lowerKey);
				interpolatedPRate=lowerValue+ slope*(setTemperature-lowerKey);
			}
		}
		return interpolatedPRate ;
	}
	public static double getInterpolatedValue(double inputKey,double lowerKey, double upperKey, double lowerValue,double upperValue) {
		double interpolatedValue=0.0;
		double slope=(upperValue-lowerValue)/(upperKey-lowerKey);
		interpolatedValue=lowerValue + slope*(inputKey-lowerKey);
		return interpolatedValue ;
	}
	public static double getInterpolatedValueWeights(double inputKey,double lowerKey, double upperKey, double lowerValue,double upperValue) {
		double interpolatedValue=0.0;
		double upperWeight=(upperKey-inputKey)/(upperKey-lowerKey);
		double lowerWeight=(inputKey-lowerKey)/(upperKey-lowerKey);
		interpolatedValue=lowerValue*upperWeight + upperValue*lowerWeight;
		return interpolatedValue ;
	}
	
	public static int searchArrayList(List<Double> targetArrayList, double searchTerm) {
		int currentIndex = 0;
		boolean found =false;
		for (currentIndex = 0; currentIndex < targetArrayList.size(); currentIndex++) {
			if (searchTerm < targetArrayList.get(currentIndex)) {
				found =true;
				break;
			}
		}
		return found ? currentIndex:-1 ;
	}

	// Clean up file data
	public static List<List<Double>> removeLeadingZeroValues(List<List<Double>> listOfDoubleLists, int itemIndex) {
		int lastZero = 0;
		for (int idx = 0; idx < listOfDoubleLists.size(); idx++) {
			List<Double> curr = listOfDoubleLists.get(idx);
			if (curr.get(itemIndex) != 0) {
				lastZero = idx - 1;
				break;
			}
		}
		int numRemovals = lastZero;
		for (int idx = 0; idx < numRemovals; idx++) {
			listOfDoubleLists.remove(0);
		}
		return listOfDoubleLists;
	}

	public static List<List<Double>> removeTrailingZeroValues(List<List<Double>> listOfDoubleLists) {
		int firstZero = 0;
		for (int idx = 0; idx < listOfDoubleLists.size(); idx++) {
			List<Double> curr = listOfDoubleLists.get(idx);
			if (curr.get(1) == 0) {
				firstZero = idx;
				break;
			}
		}
		int numRemovals = listOfDoubleLists.size() - firstZero - 1;
		for (int idx = 0; idx < numRemovals; idx++) {
			listOfDoubleLists.remove(listOfDoubleLists.size() - 1);
		}
		return listOfDoubleLists;
	}

	public static List<List<Double>> removeTrailingMaxValues(List<List<Double>> listOfDoubleLists, int maxPower,boolean charge) {
		int firstMaxCurrent = listOfDoubleLists.size() - 1;
		for (int idx = listOfDoubleLists.size() - 1; idx >= 0; idx--) {
			List<Double> curr = listOfDoubleLists.get(idx);
			if (charge) {
				if (curr.get(1) > 0.98 * maxPower) {
					firstMaxCurrent = idx;
					break;
				}
			} else {
				if (curr.get(1) < 0.98 * maxPower) {
					firstMaxCurrent = idx;
					break;
				}
			}
		}
		int numRemovals = listOfDoubleLists.size() - firstMaxCurrent - 2;
		for (int idx = 0; idx < numRemovals; idx++) {
			listOfDoubleLists.remove(listOfDoubleLists.size() - 1);
		}
		return listOfDoubleLists;
	}

	public static List<List<Double>> normalizeTimestamps(List<String> fileContents) {
		List<List<Double>> listOfDoubleLists = convertDelimitedStringListToListOfDoubleLists(fileContents);
		double baseTimeStamp = listOfDoubleLists.get(0).get(0);
		for (int idx = 0; idx < listOfDoubleLists.size(); idx++) {
			List<Double> curr = listOfDoubleLists.get(idx);
			curr.set(0, curr.get(0) - baseTimeStamp);
			listOfDoubleLists.set(idx, curr);
		}
		return listOfDoubleLists;
	}

	public static List<String> removePsqlArtefacts(List<String> testDataFile) {
		testDataFile.remove(0);
		testDataFile.remove(0);
		testDataFile.remove(testDataFile.size() - 1);
		testDataFile.remove(testDataFile.size() - 1);
		return testDataFile;
	}
	
}
