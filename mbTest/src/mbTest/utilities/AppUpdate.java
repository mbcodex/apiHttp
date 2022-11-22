package mbTest.utilities;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.reports.SystemInfo;

public class AppUpdate {
	private static final int TOMCAT_SERVICE_DIR_IDX = 4;
	private static final int CATALINA_IDX = 3;
	private static final String CLOUD_CATALINA_PATH = "/opt/tomcat/logs";
	private static final String TURTLE_CATALINA_PATH = "/var/log/tomcat8";
	private static final String TURTLE_WEBAPPS = "/var/lib/tomcat8/webapps";
	private static final String TURTLE_TOMCAT_SERVICE = "tomcat8";
	private static final String CLOUD_TOMCAT_SERVICE = "tomcat";
	private static final String CLOUD_WEBAPPS = "/opt/tomcat/webapps";
	private final static Logger LOG = LogManager.getLogger();
	// local info
	public static String CLOUDAPPS_UPDATEDIR = "/home/powin/";
	public static String CLOUDAPPS_UPDATEFILE = "cloudUpdate.sh";
	public static String TURTLEAPPUPDATEDIR = "/home/powin/";
	public static String TURTLEAPPUPDATEFILE = "turtleUpdate.sh";
	// kobold server (usually same as local machine
	private static String cRemoteCloudAppsUpdateDirectory = "/home/powin/";
	private static String cKoboldServerUser = "powin";
	private static String cKoboldServerPassword = "powin";
	// turtle location
	private static String cRemoteTurtleUpdateDirectory = "/home/powin/";

	public static Map<String, String> installedAppVersions;
	public static Map<String, String> latestAppVersions;

	private static StringBuilder coblynauVersion = new StringBuilder();
	private static StringBuilder knockerVersion = new StringBuilder();
	private static StringBuilder koboldVersion = new StringBuilder();
	private static StringBuilder primroseVersion = new StringBuilder();
	private static StringBuilder turtleVersion = new StringBuilder();

	public static int KOBOLD_MASK = 0x01;
	public static int COBLYNAU_MASK = 0x02;
	public static int PRIMROSE_MASK = 0x04;
	public static int KNOCKER_MASK = 0x08;
	public static int TURTLE_MASK = 0x10;
	public static int ALL_MASK = 0x1F;
	
	public AppUpdate(String koboldServerUser, String koboldServerPassword) {
		cKoboldServerUser = koboldServerUser;
		cKoboldServerPassword = koboldServerPassword;
		init("");
	}

	public AppUpdate(String source) {
		init(source);
		installedAppVersions = SystemInfo.getInstalledAppVersions();
		latestAppVersions = CommonHelper.getLatestAppVersions();
	}

	public AppUpdate() {
		init("");
		installedAppVersions = SystemInfo.getInstalledAppVersions();
		latestAppVersions = CommonHelper.getLatestAppVersions();
	}

	private void init(String source) {
		CLOUDAPPS_UPDATEDIR = CommonHelper.getLocalHome();
		TURTLEAPPUPDATEDIR = CommonHelper.getLocalHome();
		cRemoteCloudAppsUpdateDirectory = "/home/" + cKoboldServerUser;
		if (!"smoke".equals(source)) {
			updateArchivaCredentials();
		}
	}
	/**
	 * Returns true is the bit corresponding to the mask is set.
	 * @param apps
	 * @param mask
	 * @return
	 */
	public boolean isBitSet(int apps, int mask) {
		return (apps & mask)==mask;
	}

	static void updateArchivaCredentials() {
		replaceArchivaUsername(PowinProperty.ARCHIVA_USER.toString());
		replaceArchivaPassword(PowinProperty.ARCHIVA_PASSWORD.toString());
	}

	public static void replaceArchivaPassword(String pw) {
		FileHelper.replaceXmlFieldValue("password", pw,
				"/home/powin/.m2/settings.xml");
	}

	public static void replaceArchivaUsername(String user) {
		FileHelper.replaceXmlFieldValue("username", user,
				"/home/powin/.m2/settings.xml");
	}

	public void clearVersions() {
		coblynauVersion.delete(0, coblynauVersion.length());
		turtleVersion.delete(0, turtleVersion.length());
		knockerVersion.delete(0, knockerVersion.length());
		koboldVersion.delete(0, koboldVersion.length());
		primroseVersion.delete(0, primroseVersion.length());
	}

	public void copyCloudAppsUpdateFileToRemote() {
		String remote = cRemoteCloudAppsUpdateDirectory;
		String local = CLOUDAPPS_UPDATEDIR;
		String fileName = CLOUDAPPS_UPDATEFILE;
		String user = cKoboldServerUser;
		String host = PowinProperty.CLOUDHOST.toString();
		int port = 22;
		String password = cKoboldServerPassword;

		FileHelper.copyLocalFiletoRemoteLocation(user, host, port, password, local, remote, fileName);
	}

	public void copyTurtleAppUpdateFileToRemote() {
		String remote = cRemoteTurtleUpdateDirectory;
		String local = TURTLEAPPUPDATEDIR;
		String fileName = TURTLEAPPUPDATEFILE;
		String user = "powin";
		String host = PowinProperty.TURTLEHOST.toString();
		int port = 22;
		String password = "powin";
		FileHelper.copyLocalFiletoRemoteLocation(user, host, port, password, local, remote, fileName);
	}

	public File createCloudAppUpdateFile(int appMask) {
		File mFile = new File(CLOUDAPPS_UPDATEDIR + CLOUDAPPS_UPDATEFILE);
		String archivaUser = PowinProperty.ARCHIVA_USER.toString();
		String archivaPassword = PowinProperty.ARCHIVA_PASSWORD.toString();
		if (archivaUser.isEmpty() || archivaPassword.isEmpty()) {
			LOG.error("Enter your archiva username and password into default.properties.");
			return null;
		}
		writeCloudUpdateScript(mFile, archivaUser, archivaPassword, appMask);
		return mFile;
	}

	/**
	 * Refactored for GUI
	 * @param mFile
	 * @param archivaUser
	 * @param archivaPassword
	 * @param apps
	 */
	public File writeCloudUpdateScript(File mFile, String archivaUser, String archivaPassword, int apps) {
		String scriptText = getAppUpdateScriptText(archivaUser, archivaPassword, apps);
		if (archivaUser.isEmpty() || archivaPassword.isEmpty()) {
			LOG.error("Enter your archiva username and password.");
			mFile = null;
		} else {
			FileHelper.writeStringToFile(mFile, scriptText);
		}
		return mFile;
	}
	
	
	private static Map<Integer, List<String>> appNames=new HashMap<>();
	static {
	  appNames.put(KOBOLD_MASK, Arrays.asList("kobold", "${KOBOLD_VERSION}", CLOUD_WEBAPPS, CLOUD_CATALINA_PATH, CLOUD_TOMCAT_SERVICE)); 
	  appNames.put(COBLYNAU_MASK, Arrays.asList("coblynau", "${COB_VERSION}", CLOUD_WEBAPPS, CLOUD_CATALINA_PATH, CLOUD_TOMCAT_SERVICE)); 
	  appNames.put(PRIMROSE_MASK, Arrays.asList("primrose", "${PRIMROSE_VERSION}", CLOUD_WEBAPPS, CLOUD_CATALINA_PATH, CLOUD_TOMCAT_SERVICE));
	  appNames.put(KNOCKER_MASK, Arrays.asList("knocker", "${KNOCKER_VERSION}", CLOUD_WEBAPPS, CLOUD_CATALINA_PATH, CLOUD_TOMCAT_SERVICE));
	  // Load the turtle info once turtle location is determined.
	}

	public String addRestartTomcat(int apps) {
		String catalinaDir = isBitSet(apps, TURTLE_MASK) ? appNames.get(TURTLE_MASK).get(CATALINA_IDX) : appNames.get(KOBOLD_MASK).get(CATALINA_IDX);
		String tomcatService = isBitSet(apps, TURTLE_MASK) ? appNames.get(TURTLE_MASK).get(TOMCAT_SERVICE_DIR_IDX) : appNames.get(KOBOLD_MASK).get(TOMCAT_SERVICE_DIR_IDX);
		return "echo \"Restarting Tomcat...\"\n"
				+ "echo powin | sudo -S rm -rf "+catalinaDir+"/catalina.out "+catalinaDir+"/catalina_old1.out\n"
				+ "echo powin | sudo -S service " + tomcatService + " start \n" + "while true ; do \n"
				+ "echo \"Waiting for tomcat to restart...\"\n"
				+ "result=$(echo powin | sudo -S grep -i \"Catalina.start Server startup in\" "+catalinaDir+"/catalina.out) # -n shows line number \n"
				+ "if [ \"$result\" ] ; then \n" + "echo \"COMPLETE!\" \n" + "echo \"Result found is $result\"\n"
				+ "break \n" + "fi \n" + "sleep 2\n" + "done" + "";
	}
	
	public String addStopTomcat(int apps) {
		String tomcatService = isBitSet(apps, TURTLE_MASK) ? appNames.get(TURTLE_MASK).get(4) : appNames.get(KOBOLD_MASK).get(4);
		return "echo \"Halting Tomcat\"\n" + "echo powin | sudo -S service " + tomcatService + " stop \n"
				+ "echo powin | sudo -S rm /opt/tomcat/temp/tomcat.pid \n"
		        + "sleep 30 \n"
				+ "echo \"Moving apps into place,..\"\n";
	}


	public String deployWar(int apps, int mask) {
	    String appName=appNames.get(mask).get(0);
	    String appVersion=appNames.get(mask).get(1);
	    String webappsDir=appNames.get(mask).get(2);
		return isBitSet(apps, mask) ? "echo powin | sudo -S rm -rf "+webappsDir+"/"+appName+"* && echo powin | sudo -S mv "+appName+"-"+appVersion+".war "+webappsDir+"/"+appName+".war \n" : "";
	}
	
	public String addAppWar(int apps, int mask) {
	    String appName=appNames.get(mask).get(0);
	    String appVersion=appNames.get(mask).get(1);

		return isBitSet(apps, mask) ? "echo 'Downloading "+appName+"' "+appVersion+"\n"
				   + "wget --user=${ARCHIVA_LOGON} --password ${ARCHIVA_PASSWORD} https://archiva.powindev.com/repository/internal/com/powin/"+appName+"/"+appVersion+"/"+appName+"-"+appVersion+".war -O "+appName+"-"+appVersion+".war\n" : "";
	}

	/**
	 * Refactored for GUI
	 * @param scriptFilename
	 * @param archivaUser
	 * @param archivaPassword
	 * @param catalinaPath
	 * @param webAppsPath
	 * @param tomcatServiceName
	 * @return
	 */
	public File createTurtleUpdateFile(File scriptFilename, String archivaUser, String archivaPassword,
			                           String catalinaPath, String webAppsPath, String tomcatServiceName) {
		File mFile = scriptFilename;
		if (archivaUser.isEmpty() || archivaPassword.isEmpty()) {
			LOG.error("Enter your archiva username and password into default.properties.");
			mFile = null;
		}
		appNames.put(TURTLE_MASK, Arrays.asList("turtle", "${TURTLE_VERSION}",webAppsPath, catalinaPath, tomcatServiceName));
		String turtleUpdateScriptText = getAppUpdateScriptText(archivaUser, archivaPassword, TURTLE_MASK);
		FileHelper.writeStringToFile(mFile, turtleUpdateScriptText);
		return mFile;
	}
	
	/**
	 * Create the appUpdate script
	 * The apps included are controlled by the bit-field apps.
	 * Defined as:
	 * 				KOBOLD_MASK = 0x01;
	 *  			COBLYNAU_MASK = 0x02;
	 *  			PRIMROSE_MASK = 0x04;
	 *  			KNOCKER_MASK = 0x08;
	 *  			TURTLE_MASK = 0x10;
	 *              ALL_MASK = 0x1F;
	 *   use the bitwise or ('|') operator to select multiple apps.
	 * @param archivaUser
	 * @param archivaPassword
	 * @param apps
	 * @return
	 */
	public String getAppUpdateScriptText(String archivaUser, String archivaPassword, int apps) {
		// TODO:Use StringBuilder to make more readable
		// TO DO: create a template in default.properties and edit at runtime
		String scriptText = "ARCHIVA_LOGON='" + archivaUser + "'\n" + "ARCHIVA_PASSWORD='" + archivaPassword + "'\n";
		   scriptText += isBitSet(apps, KOBOLD_MASK) ? "KOBOLD_VERSION='" + koboldVersion.toString() + "'\n" : "";  
		   scriptText += isBitSet(apps, COBLYNAU_MASK) ? "COB_VERSION='" + coblynauVersion.toString() + "'\n" : ""; 
		   scriptText += isBitSet(apps, PRIMROSE_MASK) ? "PRIMROSE_VERSION='" + primroseVersion.toString() + "'\n" : "";
		   scriptText += isBitSet(apps, KNOCKER_MASK) ? "KNOCKER_VERSION='" + knockerVersion.toString() + "'\n" : "";
        scriptText += isBitSet(apps, TURTLE_MASK) ? "TURTLE_VERSION='" + turtleVersion.toString() + "'\n" : "";
		for (Integer key : appNames.keySet()) {
			scriptText += addAppWar(apps, key);
		}
		scriptText += addStopTomcat(apps);
		for (Integer key : appNames.keySet()) {
			scriptText += deployWar(apps, key);
		}
		scriptText += addRestartTomcat(apps);
		return scriptText;
	}


	public static String getTomcatServiceName() {
		return getCorrectPath(TURTLE_TOMCAT_SERVICE, CLOUD_TOMCAT_SERVICE);
	}

	public static String getWebAppsPath() {
		return getCorrectPath(TURTLE_WEBAPPS, CLOUD_WEBAPPS);
	}

	public static String getCatalinaPath() {
		return getCorrectPath(TURTLE_CATALINA_PATH, CLOUD_CATALINA_PATH);
	}
	
    // Adjust folders for single machine environment.
	private static String getCorrectPath(String turtlePath, String cloudPath) {
		String catalinaDir = turtlePath;
		if (CommonHelper.isSimulator()) { 
			catalinaDir = cloudPath;
		}
		return catalinaDir;
	}

	/**
	 * Get the latest versions for the apps from Archiva
	 *
	 * @param majorVersion
	 * @param minorVersion
	 */
	public void getLatestCloudAppRevision(String majorVersion, String minorVersion) {
		String partialVersion = majorVersion + "." + minorVersion + ".";
		coblynauVersion.append(partialVersion)
				.append(CommonHelper.getArchivaInfo("coblynau", majorVersion, minorVersion));
		knockerVersion.append(partialVersion)
				.append(CommonHelper.getArchivaInfo("knocker", majorVersion, minorVersion));
		koboldVersion.append(partialVersion).append(CommonHelper.getArchivaInfo("kobold", majorVersion, minorVersion));
		primroseVersion.append(partialVersion)
				.append(CommonHelper.getArchivaInfo("primrose", majorVersion, minorVersion));
		
	}

	public void getLastestTurtleAppRevision(String majorVersion, String minorVersion) {
		String partialVersion = majorVersion + "." + minorVersion + ".";
		LOG.info("majorVersion {}, minorVersion {}", majorVersion, minorVersion);
		turtleVersion.append(partialVersion).append(CommonHelper.getArchivaInfo("turtle", majorVersion, minorVersion));
	}

	/*
	 * Sets the member StringBuilders with the app versions to install.
	 */
	public static void setAppVersionsForUpdate(Map<String, String> appVersions) {
		setAppVersion(coblynauVersion, appVersions, Constants.COBLYNAU);
		setAppVersion(knockerVersion, appVersions, Constants.KNOCKER);
		setAppVersion(koboldVersion, appVersions, Constants.KOBOLD);
		setAppVersion(primroseVersion, appVersions, Constants.PRIMROSE);
		setAppVersion(turtleVersion, appVersions, Constants.TURTLE);
	}

	static void setAppVersion(StringBuilder currentVersion, Map<String, String> appVersions, String appName) {
		String appVersion = appVersions.get(appName);
		if (null != appVersion) {
			LOG.info("Updating {} from {} to {}.", appName, currentVersion, appVersion);
			currentVersion.delete(0, currentVersion.length());
			currentVersion.append(appVersion);
		} else {
			LOG.info("There is no change in {}, skipping.", appName);
		}
	}

	public static String getCoblynauVersion() {
		return coblynauVersion.toString();
	}

	public static String getKnockerVersion() {
		return knockerVersion.toString();
	}

	public static String getKoboldVersion() {
		return koboldVersion.toString();
	}

	public static String getPrimroseVersion() {
		return primroseVersion.toString();
	}

	public static String getTurtleVersion() {
		return turtleVersion.toString();
	}

	public void getTurtleAppVersion(String majorVersion, String minorVersion) {
		String partialVersion = majorVersion + "." + minorVersion + ".";
		turtleVersion.append(partialVersion).append(CommonHelper.getArchivaInfo("turtle", majorVersion, minorVersion));
	}

	/**
	 * Pull the latest revision of the app for the requested major and minor
	 * versions.
	 *
	 * @param majorVersion
	 * @param minorVersioncoblynauVersion
	 */
	public void updateCloudApps(String majorVersion, String minorVersion) {
		getLatestCloudAppRevision(majorVersion, minorVersion);
		updateCloudApps(KOBOLD_MASK|COBLYNAU_MASK|PRIMROSE_MASK|KNOCKER_MASK);
	}

	public void updateSmokeApps(String majorVersion, String minorVersion) {
		getLatestCloudAppRevision(majorVersion, minorVersion);
		getLastestTurtleAppRevision(majorVersion, minorVersion);
		updateCloudApps(TURTLE_MASK|KOBOLD_MASK|COBLYNAU_MASK|PRIMROSE_MASK|KNOCKER_MASK);
		CommonHelper.sleep(Constants.ONE_MINUTE_MS);
		LOG.info("Apps have been updated.");
	}
	
	/**
	 * Uses the versions supplied in the static member app versions.
	 * @param appMask TODO
	 */
	public void updateCloudApps(int appMask) {
		if (!isCloudAppVersionsSpecified()) {
			throw new RuntimeException("Don't forget to specify the app versions.");
		}
		FileHelper.deleteExistingFile(CLOUDAPPS_UPDATEDIR + CLOUDAPPS_UPDATEFILE);
		if (isBitSet(appMask, TURTLE_MASK)) {
			appNames.put(TURTLE_MASK, Arrays.asList("turtle", "${TURTLE_VERSION}", getWebAppsPath(), getCatalinaPath(), getTomcatServiceName()));
		}
		createCloudAppUpdateFile(appMask);
		// Copy script app_update.sh to turtle home
		copyCloudAppsUpdateFileToRemote();
		// Run script remotely
		String command = "echo powin | sudo -S sh " + CLOUDAPPS_UPDATEFILE;
		ScriptHelper.runScriptRemotelyOnKobold(cKoboldServerUser, cKoboldServerPassword, command);
	}

	private boolean isCloudAppVersionsSpecified() {
		return !(getCoblynauVersion().isEmpty() || getKnockerVersion().isEmpty() || getKoboldVersion().isEmpty()
				|| getPrimroseVersion().isEmpty());
	}

	private boolean isTurtleAppVersionsSpecified() {
		return !getTurtleVersion().isEmpty();
	}

	public void updateTurtleApp(String majorVersion, String minorVersion) {
		LOG.info("Updating turtle{}.{}", majorVersion, minorVersion);
		getTurtleAppVersion(majorVersion, minorVersion);
		updateTurtleApp();
	}

	public void updateTurtleApp(String version) {
		turtleVersion.delete(0, turtleVersion.length());
		turtleVersion.append(version);
		updateTurtleApp();
	}

	public void updateTurtleApp() {
		if (!isTurtleAppVersionsSpecified()) {
			throw new RuntimeException("Don't forget to specify the app versions.");
		}
		String filePath = TURTLEAPPUPDATEDIR + TURTLEAPPUPDATEFILE;
		FileHelper.deleteExistingFile(filePath);
		File mfile = createTurtleUpdateFile(new File(TURTLEAPPUPDATEDIR + TURTLEAPPUPDATEFILE), PowinProperty.ARCHIVA_USER.toString(), PowinProperty.ARCHIVA_PASSWORD.toString(), 
				                            getCatalinaPath(), getWebAppsPath(), getTomcatServiceName());
		String command = "echo powin | sudo -S sh ";
		if (!Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			// Copy script app_update.sh to turtle home
			command += TURTLEAPPUPDATEFILE;
			copyTurtleAppUpdateFileToRemote();
		} else if (null != mfile) {
			command = filePath;
			mfile.setExecutable(true, false);
		}
		// Run script remotely
		ScriptHelper.runScriptRemotelyOnTurtle(command);
	}
	
	/**
	 * Factored for GUI use.
	 * @param filePath
	 * @param archivaUser
	 * @param archivaPW
	 * @param catalinaPath
	 * @param webappsPath
	 * @param tomcatServiceName
	 */
	public void updateClientTurtleApp(String filePath, String archivaUser, 
									  String archivaPW, String catalinaPath, 
									  String webappsPath, String tomcatServiceName,
									  String host) {
		LOG.info("updateClientTurtleApp {}, {}, {}, {}, {}, {}, {}", filePath, archivaUser, archivaPW, catalinaPath, webappsPath, tomcatServiceName, host);
		
		FileHelper.deleteExistingFile(filePath);
		appNames.put(TURTLE_MASK, Arrays.asList("turtle", "${TURTLE_VERSION}",webappsPath, catalinaPath, tomcatServiceName));
		File mfile = createTurtleUpdateFile(new File(filePath), archivaUser, archivaPW, catalinaPath, webappsPath, tomcatServiceName);
        if (null != mfile) {
        	executeScriptOnClient(filePath, host);
        }
		//FileHelper.deleteExistingFile(filePath);
	}

	/**
	 * Factored for GUI use.
	 * @param filePath
	 * @param archivaUser
	 * @param archivaPW
	 * @param catalinaPath
	 * @param webappsPath
	 * @param tomcatServiceName
	 */
	public void updateClientCloudApps(String filePath, String archivaUser, String archivaPW, int appsMask, String host) {
		LOG.info("updatingClientCloudApps {}, {}, {}, {}, {}", filePath, archivaUser, archivaPW, appsMask, host);
		FileHelper.deleteExistingFile(filePath);

		if (isBitSet(appsMask, TURTLE_MASK)) {
			appNames.put(TURTLE_MASK, Arrays.asList("turtle", "${TURTLE_VERSION}", getWebAppsPath(), getCatalinaPath(), getTomcatServiceName()));
		}

		File mFile = writeCloudUpdateScript(new File(filePath), archivaUser, archivaPW, appsMask);
        if (null != mFile) {
        	executeScriptOnClient(filePath, host);
        }
		//FileHelper.deleteExistingFile(filePath);
	}

	/**
	 * Execute shell script on a remote host
	 * @param filePath - path to shell script
	 * @param host - IP of host or "localhost"
	 */
	private void executeScriptOnClient(String filePath, String host) {
		boolean isLocal = "localhost".equals(host);
		if (isLocal) {
			FileHelper.setFullPermissions(filePath);
		} else {
		   ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", "sshpass -p powin scp "+ filePath+ " powin@"+host+":"+filePath);
		   ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", "sshpass -p powin ssh powin@"+host+ " 'chmod 755 "+filePath+"'");
		}
		ScriptHelper.runScriptOnClient(filePath, isLocal, host , "powin", "powin");
		ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", "sshpass -p powin ssh powin@"+host+ " 'echo powin | sudo -S rm "+filePath+"'");
	}

//	/**
//	 * Utility method to get the latest versions.
//	 *
//	 * @throws Exception
//	 */
//	public static List<Map<String, String>> getAllLatestVersions() throws Exception {
//		String[] versions = org.apache.commons.lang3.StringUtils.split(PowinProperty.SUPPORTED_VERSIONS.toString(),
//				"|");
//		AppUpdate cut = new AppUpdate();
//		List<Map<String, String>> ret = new ArrayList<>();
//		Arrays.stream(versions).forEach(version -> {
//			Map<String, String> m = new HashMap<>();
//			String versionSeparator = "\\.";
//			String majorVersion = version.split(versionSeparator)[0];
//			String minorVersion = version.split(versionSeparator)[1];
//			cut.getLatestCloudAppRevision(majorVersion, minorVersion);
//			cut.getLastestTurtleAppRevision(majorVersion, minorVersion);
//			m.put(COBLYNAU, AppUpdate.getCoblynauVersion());
//			m.put(TURTLE, AppUpdate.getTurtleVersion());
//			m.put(KNOCKER, AppUpdate.getKnockerVersion());
//			m.put(KOBOLD, AppUpdate.getKoboldVersion());
//			m.put(PRIMROSE, AppUpdate.getPrimroseVersion());
//			ret.add(m);
//			LOG.info("\nCoblynau: {}\nTurtle: {}\nKnocker: {}\nKobold: {}\nPrimrose: {}\n",
//					AppUpdate.getCoblynauVersion(), AppUpdate.getTurtleVersion(), AppUpdate.getKnockerVersion(),
//					AppUpdate.getKoboldVersion(), AppUpdate.getPrimroseVersion());
//			cut.clearVersions();
//		});
//		return ret;
//	}

	/**
	 * update the apps, first the cloud then the turtle
	 *
	 * @param appUpdate
	 */
	public void updateApps() {
		// TODO remove extra tomcat restart
		updateCloudApps(KOBOLD_MASK|COBLYNAU_MASK|PRIMROSE_MASK|KNOCKER_MASK);
		// CommonHelper.sleep(Constants.ONE_MINUTE_MS);
		updateTurtleApp();
		// CommonHelper.sleep(Constants.ONE_MINUTE_MS);
	}

	/**
	 * Compares the list of requested versions with the installed versions.
	 * @param appVersions
	 * @return
	 */
	public static boolean verifyAppsUpdatedToRequestedVersion(Map<String, String> appVersions) {
		boolean result = true;
        LOG.info("appVersions:{}, installed:{}", appVersions, AppUpdate.installedAppVersions);
		for (Map.Entry<String, String> entry : appVersions.entrySet()) {
			result &= StringUtils.removeAll(entry.getValue(),"'").equals(AppUpdate.installedAppVersions.get(entry.getKey()));
		}
		return result;
	}

	/**
	 * Fill out specific versions here.
	 * @formatter:off
	 * @param mtest
	 */
	public static void specifyVersionsManually(AppUpdate mtest) {
		Map<String, String> appVersions = StringUtils
				.splitToMap(new String[] { 
						Constants.TURTLE,  "2.39.129", 
						Constants.KOBOLD,  "2.39.56", 
						Constants.KNOCKER, "2.39.11", 
						Constants.COBLYNAU, "2.39.3", 
						Constants.PRIMROSE, "2.39.4" 
			     });
		setAppVersionsForUpdate(appVersions);
		mtest.updateApps();
		installedAppVersions = SystemInfo.getInstalledAppVersions();
		if (!verifyAppsUpdatedToRequestedVersion(appVersions)) {
			LOG.error("App versions don't match.");
		}

	}

	/*
	 * @formatter:on
	 */
	public static void main(String[] args) {
		AppUpdate mTest = new AppUpdate();

		String major = "2";
		String minor = "43";
//		mTest.getLatestCloudAppRevision(major, minor);
//		mTest.updateTurtleApp(major, minor);
//		CommonHelper.deleteExistingFile(cCloudAppsUpdateDirectory + cCloudAppsUpdateFile);
//		CommonHelper.deleteExistingFile(cTurtleUpdateDirectory + cTurtleUpdateFile);
//		mTest.createCloudAppUpdateFile();
//		mTest.createTurtleUpdateFile();
//        mTest.updateSmokeApps(major, minor);
		// System.out.println(cCloudAppsUpdateDirectory);
//		 specifyVersionsManually(mTest);
        mTest.updateCloudApps(major, minor); 
		mTest.updateTurtleApp(major, minor); 
		
		// StringBuilder turtleVersion = CommonHelper.executeProcess(Constants.NOLOG,
		// Constants.WAIT, Constants.CAPTURE, "sh", "-c",
		// SystemInfo.buildRequest(Constants.TURTLE,
		// PowinProperty.TURTLEHOST.toString())).get(0);
		// mTest.clearVersions();
	}

}
