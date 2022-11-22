package mbTest.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

import com.jcraft.jsch.JSchException;
import com.powin.modbusfiles.configuration.StackType;
import com.powin.modbusfiles.derating.ArrayDerate2Helper;
import com.powin.modbusfiles.derating.DerateCommon;
import com.powin.modbusfiles.reports.DragonAppSlotData;
import com.powin.modbusfiles.reports.Lastcall;
import com.powin.modbusfiles.reports.SystemInfo;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;

public class FileHelper extends FileUtils {
	private static final String ALL = "";
	private final static Logger LOG = LogManager.getLogger();
	public static String REMOTE_IP = Constants.TURTLE_HOST;

	public static String appConfigFilenameBuilder(String appcode, int preferredPriority) {
		String priority = String.valueOf(FileHelper.getPriority(appcode, preferredPriority, Constants.HEED));
		return Constants.POWIN_APP_DIR + "app-" + priority + "-" + appcode + ".json";
	}

	public static boolean compareContents(String file1Path, String file2Path) {
		LOG.trace("Comparing contents of {} and {}", file1Path, file2Path);
		String left = readFileAsString(file1Path).replaceAll("\r|\n", ALL);
		String right = readFileAsString(file2Path).replaceAll("\r|\n", ALL);
		boolean isContentsEqual = left.equals(right);
		if (!isContentsEqual) {
			LOG.error("LEFT: {}\n{}\nRIGHT: {}\n{}", file1Path, left, file2Path, right);
		}
		return isContentsEqual;
	}

	public static void kopyFilesWithinLocalFilesystem(String sourceFilePath, String destinationFilePath) {
		if (SystemInfo.isTurtleLocal()) {

		} else {

		}
	}

	public static void kopyFilesWithinTurtleFilesystem(String sourceFilePath, String destinationFilePath) {

	}

	public static void kopyFilesToTurtle(String sourceFilePath, String destinationFilePath) {

	}

	public static void kopyFilesFromTurtle(String sourceFilePath, String destinationFilePath) {

	}

	/**
	 * Copies the default apps and devices from resources to powin directory.
	 *
	 * @param powinEntityList:   i.e.
	 *                           initial_apps=app-0-ES00001|app-10-BOP0001|app-1-BSF0001|app-4-PC00001|app-5-SSPC001|app-9999-BS00001
	 *                           i.e.
	 *                           initial_devices=device-10-PhoenixDcBattery|device-20-PcsSimulator|device-50-AcBattery|device-60-AcBatteryBlock
	 * @param entityType:        i.e. Constants.APP_TYPE or Constants.DEVICE_TYPE
	 * @param entityDestination: i.e. "/etc/powin/app/" or "/etc/powin/device"
	 */
	static void copyConfigFiles(PowinProperty powinEntityList, String entityType, String entityDestination) {
		deleteJsonConfigFiles(ALL, entityDestination);
		List<String> powinEntities = Arrays.asList(powinEntityList.toString().split("\\|"));
		powinEntities.forEach(
				entityFile -> copyTestResourceFileToDir(entityType + "/" + entityFile + ".json", entityDestination));
		setFullPermissions(entityDestination);
	}

	public static void copyFile(String source, String destination) {
		if (StringUtils.isAllBlank(source) || StringUtils.isAllBlank(destination)) {
			throw new InvalidParameterException("Source or destination is blank");
		}
		LOG.trace("Copying file(s) from {}, to {}", source, destination);
		File destinationFile = new File(destination);
		File sourceFile = FileHelper.getFilesMatchingPattern(source)[0];
		FileHelper.setFullPermissions(sourceFile.getParent().toString());
		if (sourceFile.isFile()) {
			ScriptHelper.executeProcess("sh", "-c",
					"echo " + Constants.PW + " | sudo -S cp " + source + " " + destination);
			FileHelper.setFullPermissions(destinationFile);
		} else if (sourceFile.isDirectory()) {
			FileHelper.setFullPermissions(sourceFile.toString());
			if (!destinationFile.isDirectory()) {
				throw new RuntimeException("Both source and destination must be directories.");
			}
			try {
				copyDirectory(sourceFile, destinationFile);
			} catch (IOException e) {
				LOG.error("Error copying directory", e);
				throw new RuntimeException(e.getMessage());
			}
		} else {
			throw new RuntimeException("There is no path forward. Check the path: " + source);
		}
	}

	/**
	 * 
	 * @param localFilename
	 * @param turtleFilename
	 * @throws IOException
	 */
	public static void copyFileFromLocalHomeToEtcPowinHome(String localFilename, String turtleFilename)
			throws IOException {
		if (!Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			String localScriptFile = FileHelper.copyScriptFileToLocalHome("copyFileFromLocalHomeToTurtleHome.sh");
			ScriptHelper.executeProcess(localScriptFile, localFilename, turtleFilename);
		} else {
			ScriptHelper.executeProcess("sh", "-c", "cp ", "/home/powin/" + localFilename,
					"/etc/powin/" + turtleFilename);
		}
	}

	/**
	 * Copies a file from a remote IP using SSH or from localhost. Source and
	 * destination may be a file or a directory file -> file file -> dir dir -> dir
	 *
	 * @param sourcefile
	 * @param destination
	 * @param host
	 * @param username
	 * @param password
	 */
	public static void copyFileFromRemote(String source, String destination, String host, String username,
			String password) {
		LOG.trace("Copying file(s) from {}, to {}", source, destination);
		File destinationFile = new File(destination);
		createDestinationFolder(destination);
		if (Constants.LOCAL_HOST.equals(host)) {
			FileHelper.copyFile(source, destination);
		} else {
			// This will work with the target as a dir only or as a full path.

			String sourceSSH = username + "@" + host + ":" + source;
			if (destinationFile.isDirectory()) {
				sourceSSH = StringUtils.appendIfMissing(sourceSSH, "/");
				// ScriptHelper.runScriptRemotelyOnTurtle(
				// "sudo chmod -R 777 " + StringUtils.appendIfMissing(destination, "/"));
				sourceSSH += "*.*";
				ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sshpass", "-p",
						password, " scp -r -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ", sourceSSH,
						destination);
				// sourceSSH += "*.*";
			} else {
				// FileHelper.setFullPermissions(sourceFile.getParent().toString());
				ScriptHelper.runScriptRemotelyOnTurtle("sudo chmod -R 777 " + destinationFile.getParent().toString());
				ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sshpass", "-p",
						password, "scp -r  -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ", sourceSSH,
						destination);
			}
		}
		// LOG.trace("Copied {} files", destinationFile.listFiles().length);
	}

	/**
	 * Copies a file from turtle home to local home using the script
	 * 'copyFileFromTurtleHomeToLocalHome.sh'
	 *
	 * @param turtleFilename
	 * @param localFilename
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void copyFileFromTurtleHomeToLocalHome(String turtleFilename, String localFilename)
			throws IOException {
		String localScriptFile = copyScriptFileToLocalHome("copyFileFromTurtleHomeToLocalHome.sh");
		ScriptHelper.executeProcess(localScriptFile, turtleFilename, localFilename);
	}

	public static void copyFileFromTurtleToLocal(String turtleFilename, String localFilename)
			throws IOException {
		String localScriptFile = copyScriptFileToLocalHome("copyFileFromTurtleToLocal.sh");
		ScriptHelper.executeProcess(localScriptFile, turtleFilename, localFilename);
	}

	public static void copyFilesToRemote(String fullSourceFileNamePattern, String destinationFolderPath, String host,
			String password) {

		File[] files = getFilesMatchingPattern(fullSourceFileNamePattern);
		for (File f : files) {
			FileHelper.copyFileToRemote(f.getAbsolutePath(), destinationFolderPath, host, password);
		}
	}

	public static void copyFilesToTurtle(String fullSourceFileNamePattern, String destinationFolderPath) {
		if (SystemInfo.isTurtleLocal())
			copyFile(fullSourceFileNamePattern, destinationFolderPath);
		else
			copyFilesToRemote(fullSourceFileNamePattern, destinationFolderPath, SystemInfo.getTurtleHost(),
					SystemInfo.getTurtlePassword());
	}

	public static void copyFileToRemote(String sourcefile, String destination, String host, String password) {
		FileHelper.setFullPermissions(FileHelper.getDirectoryFromFilepath(sourcefile));
		FileHelper.setFullPermissions(destination);
		destination = PowinProperty.TURTLEUSER.toString() + "@" + host + ":" + destination;
		ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sshpass", "-p", password,
				"scp", sourcefile, destination);
	}

	public static void copyFileToTurtle(String sourcefile, String destination) {
		copyFileToRemote(sourcefile, destination, SystemInfo.getTurtleHost(), SystemInfo.getTurtlePassword());
	}

	static void copyGeneratedConfigurationToPowinDir(String randomFolderName) {
		String fullPathToConfigurationJson = randomFolderName + "configuration.json";
		if (Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			copyFile(fullPathToConfigurationJson, "/etc/powin/");
		} else {
			String copyCommand = "sudo scp " + fullPathToConfigurationJson + " /etc/powin/";
			ScriptHelper.runScriptRemotelyOnTurtle(copyCommand);
		}
	}

	/**
	 * @deprecated use FileHelper.copyFileToRemote()
	 * @param user
	 * @param host
	 * @param port
	 * @param password
	 * @param localPath
	 * @param remotePath
	 * @param fileName
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws JSchException
	 */
	@Deprecated
	public static void copyLocalFiletoRemoteLocation(String user, String host, int port, String password,
			String localPath, String remotePath, String fileName) {
		//Session session = CommonHelper.createSession(user, host, port, password);
		//FileHelper.copyLocalToRemote(session, localPath, remotePath, fileName);
		copyFileToRemote(localPath+fileName, remotePath, host, password);
		
	}


	public static void copyRemoteFile(String host, String user, String pw, String oldname, String newname) {
		String command = " echo " + pw + " | sudo -S cp " + oldname + " " + newname;
		if (Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			ScriptHelper.executeProcess("sh", "-c", command);
		} else {

			ScriptHelper.executeRemoteSSHCommand(host, user, pw, command);
		}
	}

	/**
	 * Move the script file from the resources to the Home directory. The script is
	 * then given execute permissions.
	 *
	 * @param scriptFile
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static String copyScriptFileToLocalHome(String scriptFile) throws IOException {
		String localScriptFile = CommonHelper.getLocalHome() + scriptFile;
		String resourceFileLocation = getFilepathFromResources(scriptFile, FileHelper.class);
		copyFile(resourceFileLocation, localScriptFile);
		CommonHelper.setSystemFilePermissons(localScriptFile, "777");
		return localScriptFile;
	}

	public static void copyTestResourceFileToDir(String filename, String destDir) {
		try {
			if (Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
				copyFileToDirectory(FileHelper.getFileFromResources(filename, FileHelper.class), new File(destDir),
						Constants.NO_CREATEDIR);
			} else {
				// ScriptHelper.executeRemoteSSHCommand(PowinProperty.TURTLEHOST.toString(),
				// PowinProperty.TURTLEUSER.toString(), PowinProperty.TURTLEPASSWORD.toString(),
				// "scp src/test/resources/" + filename + " powin@" + Constants.TURTLE_HOST +
				// ":" + destDir);
				String resourceFilePath = getFilepathFromResources(filename, FileHelper.class);
				copyFileToTurtle(resourceFilePath, destDir);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void createDestinationFolder(String destination) {
		if (destination.endsWith("/") && !new File(destination).exists()) {
			try {
				Files.createDirectories(Paths.get(destination));
			} catch (IOException e) {
				LOG.error("Error reading file", e);
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	public static String createDynamicFolder(String folderNamePrefix, String folderLocation) {
		String randomFolderName = folderNamePrefix + Math.round(100000 * Math.random());
		String newFolderPath = folderLocation + randomFolderName;
		String command = "sudo mkdir " + newFolderPath;
		ScriptHelper.callScriptSudo(Constants.PW, command);
		setFullPermissions(newFolderPath);
		
		return newFolderPath + "/";
	}

	/**
	 * Create an empty file
	 *
	 * @param fileName
	 */
	public static void createEmptyFile(String fileName) {
		try {
			File file = new File(fileName);
			file.createNewFile();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static FileHelper createTimeStampedFile(String folder, String filename, String ext) {
		try {
			return new FileHelper(
					folder + "/" + filename + DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS).replace(" ", "_") + ext);
		} catch (IOException e) {
			LOG.error("Can't create file {}.", filename, e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void createTomcatRestartFile(String scriptFile) {
		File mFile = new File(scriptFile);
		try (FileWriter mWriter = new FileWriter(mFile, true)) {
			// TODO:Use StringBuilder to make more readable
			mWriter.write(

					"echo \"Restarting Tomcat...\"\n" + "sudo service tomcat8 stop \n" + "sleep 2\n"
							+ "sudo rm -rf /var/log/tomcat8/catalina.out /var/log/tomcat8/catalina_old1.out\n"
							+ "sudo service tomcat8 restart \n" + "while true ; do \n"
							+ "echo \"Waiting for tomcat to restart...\"\n"
							+ "result=$(grep -i \"Catalina.start Server startup in\" /var/log/tomcat8/catalina.out) # -n shows line number \n"
							+ "if [ \"$result\" ] ; then \n" + "echo \"COMPLETE!\" \n"
							+ "echo \"Result found is $result\"\n" + "break \n" + "fi \n" + "sleep 2\n" + "done" + "");
			mWriter.flush();
			System.out.print("Writing successful!");
		} catch (IOException e) {
			System.out.print("Writing failed" + e.getLocalizedMessage());
		}
	}

	public static void deleteExistingFile(String flePath) {
		File f = new File(flePath);
		if (f.exists()) {
			if (f.delete()) {
				System.out.println(f.getName() + " deleted");
			} else {
				System.out.println("failed");
			}
		}
	}
    
	public static boolean deleteFolderContents(String folderPath) {
		setFullPermissions(folderPath);
		File f = new File(folderPath);
		File[] folder = f.listFiles();
		if (folder != null) {
			for (File f2 : folder)
				deleteFolderContents(f2.getAbsolutePath());
		}
		return new File(folderPath).delete();
	}

	public static void deleteJsonConfigFiles(String tag, String path) {
		LOG.trace("Deleting {}", tag.isEmpty() ? "all config files" : tag);
		final File appPath = new File(path);
		File[] files = appPath.listFiles((dir, name) -> name.matches(".*" + tag + ".json"));
		setFullPermissions(path);
		Arrays.asList(files).stream().forEach(File::delete);
	}
	
//	public static void deleteAppConfigsAndRestartTomcat(Set<Integer> appPriorities) {
//		StringBuilder mFileName = new StringBuilder("remove");
//		for (Integer i : appPriorities) {
//			mFileName.append("_P" + i);
//		}
//		mFileName.append(".sh");
//		FileHelper.deleteExistingFile(SH_FILE_URL + mFileName.toString());
//		File mfile = createAppConfigRemovalFile(appPriorities);
//		String command = "sudo sh ";
//		if (!Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
//			command += SH_FILE_URL + mFileName.toString();
//			String filePath = SH_FILE_URL + mFileName.toString();
//			// copyAppConfigRemovalFilToRemote(mFileName.toString());
//			FileHelper.copyFileToRemote(filePath, filePath, PowinProperty.TURTLEHOST.toString(),
//					PowinProperty.TURTLEPASSWORD.toString());
//		} else if (null != mfile) {
//			command = mFileName.toString();
//			mfile.setExecutable(true, false);
//		}
//		ScriptHelper.runScriptRemotelyOnTurtle(command);
//	}

	/**
	 * Updates or adds a field Value.
	 *
	 * @param fileContent
	 * @param fieldName
	 * @param newFieldValue
	 * @return
	 */

	public static String editConfigFileContent(String fileContent, String fieldName, String newFieldValue) {
		String insertionPattern=".*(})";;
		return editConfigFileContent( fileContent,  insertionPattern,  false, fieldName, newFieldValue) ;
	}
	
	public static String editConfigFileContent(String fileContent, String insertionPattern, boolean insertAfter,String fieldName,String newFieldValue) {
		String toReplace = getFieldValueFromConfigFile(fileContent, fieldName);
		if (null != toReplace) {
			fileContent = fileContent.replace(toReplace, newFieldValue);
		} else {
			// append new field
			insertionPattern = insertionPattern + ".*";
			List<String> allMatches = StringUtils.getAllMatches(insertionPattern, fileContent, Pattern.MULTILINE,StringUtils.ReturnGroup.NONE, null);
			String replaceWith = "";
			for(String match:allMatches) {
				replaceWith = ",\n\"" + fieldName + "\" : " + newFieldValue;
				if(insertAfter)
					replaceWith = match +replaceWith;
				else
					replaceWith = replaceWith + match;
				fileContent = fileContent.replace(match, replaceWith);
			}
		}
		return fileContent;
	}

	public static void editConfigurationFile(String fullFilePath, String fieldName, String fieldValue,String defaultContents) {
		editConfigurationFile( fullFilePath,  ".*(})", false, fieldName,  fieldValue, defaultContents,true) ;
	}
	
	public static void editConfigurationFile(String fullFilePath, String fieldName, String fieldValue,String defaultContents,boolean restartTomcat) {
		editConfigurationFile( fullFilePath,  ".*(})", false, fieldName,  fieldValue, defaultContents,restartTomcat) ;
	}
	
	public static void editConfigurationFile(String fullFilePath, String insertionPoint,boolean insertAfter,String fieldName, String fieldValue,String defaultContents,boolean restartTomcat) {
		String fileContent = "";
		fileContent = FileHelper.getConfigFileContents(fullFilePath, defaultContents);
		fileContent = FileHelper.editConfigFileContent(fileContent, insertionPoint,insertAfter,fieldName, fieldValue);
		FileHelper.writeConfigFile(PowinProperty.TURTLEHOST.toString(), fullFilePath, fileContent);
		if (restartTomcat) {
			CommonHelper.restartTurtleTomcat();
		}
	}

	public static void editConfigurationJson(String scriptFile, String parameterName, String enabledStatus,String pathToConfigFile) {
		String command = "sudo sh " + scriptFile + " " + parameterName + " " + enabledStatus + " " + pathToConfigFile;
		ScriptHelper.runScriptRemotelyOnTurtle(command);
	}

	public static List<String> eGrepJsonFile(String host, String regex, String pathToFile) {
		return ScriptHelper.executeSimpleRemote(host, Constants.USER, Constants.PW,
				"egrep \"" + regex + "\" " + pathToFile);
	}

	public static String getConfigFileContents(String configFileLocation, String defaultFileContents) {
		String fileContents;
		if (AppInjectionCommon.isAppConfigPresent(PowinProperty.TURTLEHOST.toString(), configFileLocation)) {
			fileContents = FileHelper.readConfigFile(PowinProperty.TURTLEHOST.toString(), configFileLocation);
		} else {
			fileContents = defaultFileContents;
		}
		return fileContents;
	}

	public static String getDirectoryFromFilepath(String filepath) {
		return StringUtils.getMatchString(filepath, "^(.*\\/)", Pattern.CASE_INSENSITIVE, StringUtils.ReturnGroup.FIRST,
				"");
	}

	public static String getFieldValueFromConfigFile(String fieldName, File filePath) {
		String configFileContents = FileHelper.getConfigFileContents(filePath.toString(), "");
		String fieldValue = FileHelper.getFieldValueFromConfigFile(configFileContents, fieldName);
		return fieldValue;
	}

	public static String getFieldValueFromConfigFilePath(String fieldName, String filePath) {
		String configFileContents = FileHelper.getConfigFileContents(filePath, "");
		String fieldValue = "null";
		if (!configFileContents.isEmpty())
			fieldValue = FileHelper.getFieldValueFromConfigFile(configFileContents, fieldName);
		return fieldValue;
	}

	/**
	 * Returns the field value of fieldName
	 *
	 * @param fileContent
	 * @param fieldName
	 * @return null if field doesn't exist or has no value.
	 */
	public static String getFieldValueFromConfigFile(String fileContent, String fieldName) {
		String searchPattern = fieldName + "\"\\s*:\\s*(.*?)\\s*[\\,|\\}]";
		String toReplace = StringUtils.getMatchStringRaw(fileContent, searchPattern, Pattern.MULTILINE,
				StringUtils.ReturnGroup.FIRST, null);

		// // Hack to fix mysterious disappearing Station Name
		// if ("\"\"".equals(toReplace)) {
		// toReplace.replaceAll("\"", "");
		// }

		return toReplace;
	}

	public static String getFileContentDifference(File f1, File f2) throws FileNotFoundException {
		// Assumption: One file's content includes all of the other's content but has
		// some extra content
		File smallerFile = f1.length() >= f2.length() ? f2 : f1;
		File biggerFile = f1.length() >= f2.length() ? f1 : f2;
		Scanner input1 = new Scanner(smallerFile);// read first file
		Scanner input2 = new Scanner(biggerFile);// read second file
		String second = "";
		String diff = "";

		while (input2.hasNextLine()) {
			if (input1.hasNextLine()) {
				input1.nextLine();
				second = input2.nextLine();
			} else {
				second = input2.nextLine() + "\n";
				diff += second;
			}
		}
		input1.close();
		input2.close();
		return diff;
	}

//	// TODO Replace this with readFileAsString?
//	public static String getFileContents(File f1) throws FileNotFoundException {
//		// Returns file contents with each line having \n except the last line
//		Scanner input = new Scanner(f1);// read first file
//		String first = "";
//		while (input.hasNextLine()) {
//			first += input.nextLine() + "\n";
//		}
//		input.close();
//		return org.apache.commons.lang3.StringUtils.chomp(first);
//	}

	public static File getFileFromResources(String fileName, Class<?> clazz) {
		return new File(getFilepathFromResources(fileName, clazz));
	}
	
	public static Path getPathFromResources(String fileName, Class<?> clazz) {
		return (getFileFromResources(fileName, clazz)).toPath();
	}

	public static String getFilepathFromResources(String fileName, Class<?> clazz) {
		ClassLoader classLoader = clazz.getClassLoader();
		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("file %s is not found!", fileName));
		} else {
			return resource.getFile();
		}
	}

	public static String getFilepathFromResources1(String fileName, Class<?> clazz) throws URISyntaxException {
		return Paths.get(clazz.getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
				.resolve(Paths.get(fileName)).toFile().toString();
	}

	public static File[] getFilesMatchingPattern(String fullSourceFileNamePattern) {
		fullSourceFileNamePattern = StringUtils.addDotBeforeAsterisk(fullSourceFileNamePattern);
		File matchingFiles = new File(fullSourceFileNamePattern);
		File parentDirectory = new File(matchingFiles.getParent());
		FileHelper.setFullPermissions(parentDirectory.toString());
		Path path = Paths.get(fullSourceFileNamePattern);
		Path fileName = path.getFileName();
		String strFileName = fileName.toString();
		File[] files = parentDirectory.listFiles((dir, name) -> name.matches(strFileName));
		return files;
	}

	/**
	 * Given a json formatted string "key" : true|false, returns the boolean.
	 *
	 * @param line
	 * @return
	 */
	public static boolean getJSONBooleanValue(String line) {
		return Boolean.valueOf(line.split(":")[1].replaceAll(",", ALL).trim());
	}

	/**
	 * Checks to see if the priority requested is already being used and increments
	 * until one is available.
	 *
	 * @param appcode
	 * @param preferredPriority
	 * @param ignoreExisting    - If another instance of an app is required pass in
	 *                          true.
	 * @return
	 */
	public static int getPriority(String appcode, int preferredPriority, boolean ignoreExisting) {
		// Map<String, String> map =
		// Arrays.asList(Lastcall.getDragonAppList()).stream().collect(Collectors.toMap(DragonAppSlotData::getAppCode,
		// DragonAppSlotData::getPriority));
		Map<String, String> map = new HashMap<>();
		for (DragonAppSlotData dsd : Lastcall.getDragonAppList()) {
			map.put(dsd.getAppCode(), dsd.getPriority());
		}

		Collection<String> priorities = map.values();
		int priority = preferredPriority;
		Set<String> appCodes = map.keySet();
		if (!ignoreExisting && appCodes.contains(appcode)) {
			priority = Integer.valueOf(map.get(appcode));
		} else {
			while (priorities.contains(String.valueOf(priority))) {
				priority++;
			}
		}
		return priority;
	}

	public static boolean localFileExists(String filepath) {
		return new File(filepath).exists();
	}

	public static boolean matchesDefaultConfigurationJson(int arrayCount, int stringCount) {
		String oneArrayOneString = "(1,{1=1})";// TO DO: This function is currently applicable only to a 1 array-1
												// string setup. Needs to handle any combination of array=-string
		boolean matches = false;
		if (arrayCount == 1 && stringCount == 1) {
			matches = SystemInfo.getPhysicalConfiguration().toString().equals(oneArrayOneString);
			LOG.trace("SystemInfo.getPhysicalConfiguration().toString()={}",
					SystemInfo.getPhysicalConfiguration().toString());
			matches &= !SystemInfo.getStationCode().isEmpty();
		}
		return matches;
	}

	/**
	 * TODO add support for remote turtle. Compare the default configuration files
	 * (apps and devices) with what is on the system in /etc/powin. Note: We short
	 * circuit the return if the count and filenames don't match.
	 *
	 * @param folder
	 * @return
	 */
	public static boolean matchesDefaultFolder(String folder) {
		String configFolder = "";
		String powinEntities = "";
		if (folder.equals(Constants.POWIN_ENTITY_TYPE_APP)) {
			configFolder = Constants.POWIN_APP_DIR;
			powinEntities = PowinProperty.INITIAL_APPS.toString();
		} else {
			configFolder = Constants.POWIN_DEVICE_DIR;
			powinEntities = PowinProperty.INITIAL_DEVICES.toString();
		}
//Copy folder if it exists remotely
		String localConfigFolder="";
		List<File> powinEntityFiles =new ArrayList<>();
		if(!PowinProperty.TURTLEHOST.toString().contains("localhost")) {
			localConfigFolder=copyRemoteFolderToLocalDynamicFolder(configFolder) ;
			powinEntityFiles = getFilesbyPattern(localConfigFolder, ".*json");
		}else {
			powinEntityFiles = getFilesbyPattern(configFolder, ".*json");
		}
		List<File> defaultEntityFiles = getDefaultEntityFileList(folder, powinEntities);
		return isFileContentsMatch(powinEntityFiles, defaultEntityFiles);
	}
	
	public static String copyRemoteFolderToLocalDynamicFolder(String remoteFolderPath) {
		String localTempDestinationFolder = FileHelper.createDynamicFolder("tempConfig", "/home/powin/");
		FileHelper.setFullPermissions(localTempDestinationFolder);
		String changePermissionsOfRemoteFolderCommand = "sudo chmod -R 777 "+remoteFolderPath;
		ScriptHelper.executeRemoteSSHCommand(PowinProperty.TURTLEHOST.toString(), "powin", "powin",
				"echo powin | sudo -S  " + changePermissionsOfRemoteFolderCommand);
		FileHelper.copyFileFromRemote(remoteFolderPath,
				localTempDestinationFolder , Constants.TURTLE_HOST, "powin",
				"powin");
		return localTempDestinationFolder;
	}
	
	


	/**
	 * Compares the contents of two lists of File objects
	 * First check the filenames, if that list doesn't match we're done.
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean isFileContentsMatch(List<File> left, List<File> right) {
		boolean allTestsPass = isFileListsEqual(left, right);
		for (int i = 0; allTestsPass && i < left.size(); ++i) {
			allTestsPass &= compareContents(left.get(i).getAbsolutePath(),
					right.get(i).getAbsolutePath());
		}
		if (!allTestsPass) {
			LOG.trace("oops the file contents do not match");
		}
		return allTestsPass;
	}
	
	/**
	 * Checks two lists of filenames for equality.
	 * @param powinEntityFiles
	 * @param defaultEntityFiles
	 * @return
	 */
	public static boolean isFileListsEqual( List<File> powinEntityFiles,
			List<File> defaultEntityFiles) {
		List<String> powinAppNames = powinEntityFiles.stream().map(file -> file.getName()).sorted().collect(Collectors.toList());
		List<String> defaultAppNames = defaultEntityFiles.stream().map(file -> file.getName()).sorted()
				.collect(Collectors.toList());

		boolean allTestsPass = powinAppNames.equals(defaultAppNames);
		if (!allTestsPass) {
			LOG.trace("The names of files don't match.");
		}
		return allTestsPass;
	}

	public static List<File> getDefaultEntityFileList(String folder, String powinEntities) {
		List<String> defaultEntities = Arrays.asList(powinEntities.split("\\|"));
		defaultEntities = defaultEntities.stream()
				.map(entity -> "src/test/resources/" + folder + "/" + entity + ".json").collect(Collectors.toList());
		List<File> defaultEntityFiles = defaultEntities.stream().map(File::new).sorted().collect(Collectors.toList());
		return defaultEntityFiles;
	}

	public static List<File> getFilesbyPattern(String folder	, String pattern) {
		List<File> powinEntityFiles = Arrays.asList(new File(folder).listFiles((dir, name) -> name.matches(pattern)));
		powinEntityFiles = powinEntityFiles.stream().sorted().collect(Collectors.toList());
		return powinEntityFiles;
	}

	public static boolean matchesPhysicalConfigurationJson(int arrayCount, int stringCount) {
		String oneArrayOneString = "(1,{1=1})";// TO DO: This function is currently applicable only to a 1 array-1
		SystemInfo.getExpectedPhysicalConfiguration(arrayCount, stringCount);
		boolean matches = false;
		if (arrayCount == 1 && stringCount == 1) {
			matches = SystemInfo.getPhysicalConfiguration().toString().equals(oneArrayOneString);
			LOG.trace("SystemInfo.getPhysicalConfiguration().toString()={}",
					SystemInfo.getPhysicalConfiguration().toString());
			matches &= !SystemInfo.getStationCode().isEmpty();
		}
		return matches;
	}

	public static String parseXmlFile(String filePath, String field) {
		String jsonText = CommonHelper.convertXmlToJson(filePath).toString();
		JSONObject json = JsonParserHelper.parseJsonFromString(jsonText);
//		new JsonParserHelper(jsonText);
		List<String> results = new ArrayList<>();
		results = JsonParserHelper.getFieldJSONObject(json, field, "", results);
		return results.get(0);
	}

	public static void printFile(File file) throws IOException {
		if (file == null)
			return;
		try (FileReader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		}
	}
	
	public static List<String> getFileListRemote(String fileLocation) {
		String command = "ls -l "+fileLocation;
		List<String> result = ScriptHelper.executeRemoteSSHCommand(PowinProperty.TURTLEHOST.toString(), "powin", "powin",
				command);
		return result;
	}

	static File[]  getFileListLocal(String fileLocation) {
		File[] files=getFilesMatchingPattern(fileLocation);
		return null;
	}

	/**
	 * Reads the configuration file from /etc/powin/app
	 *
	 * @param turtleHost
	 * @param fileLocation TODO
	 * @return
	 */
	public static String readConfigFile(String turtleHost, String fileLocation) {
		String filecontents = "";
		if ("localhost".equals(turtleHost)) {
			filecontents = readFileAsString(fileLocation);
		} else {
			filecontents = readRemoteFileAsString(fileLocation, turtleHost, Constants.USER, Constants.PW);
		}
		return filecontents;
	}

	public static String readFileAsString(Path pathToFile) {
		String ret = ALL;
		setFullPermissions(pathToFile);
		try {
			ret = new String(Files.readAllBytes(pathToFile));
		} catch (IOException e) {
			LOG.error("Unable to read file {}.", pathToFile);
			throw new RuntimeException();
		}
		return ret;
	}
	/**
	 * Reads an input stream as a string. The input stream is created by passing
	 * the fully qualified path to a file that is located on the classpath.
	 * this.getClass().getResourceAsStream(fullyQualifiedName)
	 * 
	 * @param is
	 * @return
	 */
	public static String readInputStreamAsString(InputStream is) {
		 return new BufferedReader(
			      new InputStreamReader(is, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\n"));
	}
	
	public static String readFileAsString(String pathToFile) {
		return readFileAsString(Paths.get(pathToFile));
	}

	/**
	 * Read a text file into a List<String>
	 *
	 * @param filename
	 * @return
	 */
	public static List<String> readFileToList(final String filename) {
		return readFileToStream(filename).collect(Collectors.toList());
	}

	public static void writeListToFile(final String filename, List<String> list) {
		try {
			FileWriter writer = new FileWriter(filename);
			for (String str : list) {
				writer.write(str + System.lineSeparator());
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the lines of a text file as a Stream.
	 *
	 * @param filename
	 * @return
	 */
	public static Stream<String> readFileToStream(final String filename) {
		try {
			return Files.lines(Paths.get(filename));
		} catch (IOException e) {
			LOG.error("Can't read file {}", filename, e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String readJsonLine(String keyValue, String filePath) {
		return (ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c",
				"grep " + keyValue + " " + filePath)).get(0);
	}

	/**
	 * Reads a remote file via ssh, requires that sshpass is installed on the
	 * system.
	 *
	 * @param filename
	 * @param host
	 * @param username
	 * @param pw       TODO
	 * @return
	 */
	public static String readRemoteFileAsString(String filename, String host, String username, String pw) {
		String filecontents;
		String command = " cat " + "\"" + filename + "\"";
		List<String> result = ScriptHelper.executeRemoteSSHCommand(host, username, pw, command);
		filecontents = String.join("\n", result);
		return filecontents;
	}

	/**
	 * Grep the line from the json file containing the key. Note: key must be
	 * unique.
	 *
	 * @param host
	 * @param key
	 * @param user
	 * @param pw
	 * @param filePath
	 * @return
	 */
	public static String readRemoteJsonLine(String host, String key, String user, String pw, String filePath) {
		String command = "grep " + key + " " + Constants.CONFIGURATION_FILE;
		List<String> result = ScriptHelper.executeRemoteSSHCommand(host, user, pw, command);
		return result.get(0);
	}

	// ----------------------------------------------------------------------
	// Remote file methods
	// ----------------------------------------------------------------------
	/**
	 * Check that at a file exists on a remote machine via ssh.
	 *
	 * @param host
	 * @param user
	 * @param pw
	 * @param filepath
	 * @return
	 */
	public static boolean remoteFileExists(String host, String user, String pw, String filepath) {
		boolean fileExists;
		String command = " [ -f  \"" + filepath + "\"  ] && echo true  || echo false";
		List<String> result = ScriptHelper.executeRemoteSSHCommand(host, user, pw, command);
		// LOG.debug(result.get(0));
		fileExists = Boolean.valueOf(result.get(0));
		return fileExists;
	}

	/**
	 * Removes derate from configurationName and returns the modified line.
	 *
	 * @return
	 */
	public static String removeDerateFromConfigurationName() {
		ScriptHelper.executeRemoteSSHCommand(REMOTE_IP, PowinProperty.TURTLEUSER.toString(),
				PowinProperty.TURTLEPASSWORD.toString(),
				"echo powin | sudo -S sed -i 's/-derate01//g' /etc/powin/configuration.json");
		return FileHelper.readRemoteJsonLine(Constants.TURTLE_HOST, "configurationName", Constants.USER, Constants.PW,
				Constants.CONFIGURATION_FILE);
	}

	/**
	 * * Remove file or files from local or remote given the full filepath and
	 * filename. Wildcards allowed
	 *
	 * Usage : FileHelper.removeFiles("/etc/powin/device/device-*.json") will remove
	 * all files matching the pattern "/etc/powin/device/device-*.json")
	 *
	 * @param fullFileNamePattern *
	 *
	 */
	public static boolean removeFiles(String fullFileNamePattern) {
		boolean operationStatus = false;
		if (PowinProperty.TURTLEHOST.toString().equals(Constants.LOCAL_HOST)) {
			File file = new File(fullFileNamePattern);
			// If its a directory
			try {
				if (file.isDirectory()) {
					cleanDirectory(file);
				}
			} catch (final Exception ignored) {
			}
			// If its a file(s)
			File parentDirectory = new File(file.getParent());
			FileHelper.setFullPermissions(parentDirectory.toString());
			File[] files = FileHelper.getFilesMatchingPattern(fullFileNamePattern);
			Arrays.asList(files).stream().forEach(File::delete);
		} else {
			FileHelper.removeRemoteFile(fullFileNamePattern, PowinProperty.TURTLEHOST.toString(),
					PowinProperty.TURTLEUSER.toString(), PowinProperty.TURTLEPASSWORD.toString());
		}
		return operationStatus;
	}

	/**
	 * Remove file from remote host.
	 *
	 * @param filename
	 * @param host
	 * @param username
	 */
	public static void removeRemoteFile(String filename, String host, String username, String pw) {
		String command = " rm " + filename;
		ScriptHelper.executeRemoteSSHCommand(host, username, pw, command);
	}
	
	/**
	 * Remove directory from remote host.
	 *
	 * @param filename
	 * @param host
	 * @param username
	 */
	public static void removeRemoteDir(String filename, String host, String username, String pw) {
		String command = " rm -drf " + filename;
		ScriptHelper.executeRemoteSSHCommand(host, username, pw, command);
	}

	/**
	 * Rename a remote file via ssh
	 *
	 * @param host
	 * @param user
	 * @param pw
	 * @param oldname
	 * @param newname
	 */
	public static void renameRemoteFile(String host, String user, String pw, String oldname, String newname) {
		String command = " echo " + pw + " | sudo -S mv " + oldname + " " + newname;
		ScriptHelper.executeRemoteSSHCommand(host, user, pw, command);
	}

	/**
	 * This method will replace a Json string value in a file on a remote server
	 * viaimport ch.ethz.ssh2.Connection; import ch.ethz.ssh2.SCPClient; import
	 * ch.ethz.ssh2.SFTPv3Client; import ch.ethz.ssh2.SFTPv3DirectoryEntry; ssh.
	 * Using 'configurationName' as the field, the regex match pattern is:
	 * (\"configurationName\"[ :]*")(.*)"
	 *
	 * @param repl
	 * @param fieldname
	 * @param filepath
	 * @param remoteIP
	 */
	public static void replaceRemoteJsonStringField(String repl, String fieldname, String filepath, String remoteIP) {
		String cmd = "echo powin | sudo -S sed -i  's/\\(\"" + fieldname + "\"[ :]*\"\\)\\(.*,\\)/\\1" + repl
				+ "\",/g' " + filepath;
		ScriptHelper.executeSimpleRemote(remoteIP, Constants.USER, Constants.PW, cmd);
	}

	public static void replaceXmlFieldValue(String fieldName, String fieldValue, String filename) {
		String command = "echo powin | sudo -S  sed -i";
		String searchFieldName = "<" + fieldName + ">.*</" + fieldName + ">";
		String replaceFieldName = "<" + fieldName + ">" + fieldValue + "</" + fieldName + ">";
		ScriptHelper.executeRemoteSSHCommand(PowinProperty.TURTLEHOST.toString(), "powin", "powin",
				command + " 's|" + searchFieldName + "|" + replaceFieldName + "|g' " + filename);
	}

	public static boolean resetConfigurationJson(int arrayCount, int stringCount) {
		boolean restartTomcatNeeded = false;
		if (!matchesDefaultConfigurationJson(arrayCount, stringCount)) {
			try {
				CommonHelper.createConfigurationJson(SystemInfo.getStationCode(), StackType.STACK_225_GEN22,
						stringCount, arrayCount);
			} catch (IOException e) {
				e.printStackTrace();
			}
			restartTomcatNeeded = true;
		}
		return restartTomcatNeeded;
	}

	public static boolean resetDefaults() {
		boolean restartTomcatNeeded = false;
		restartTomcatNeeded |= resetPowinEntitiesToDefault(Constants.POWIN_ENTITY_TYPE_APP);
		restartTomcatNeeded |= resetPowinEntitiesToDefault(Constants.POWIN_ENTITY_TYPE_DEVICE);
		restartTomcatNeeded |= resetConfigurationJson(1, 1);
		return restartTomcatNeeded;
	}

	public static boolean resetLocalDevicesToDefault(String destDir) {
		boolean restartTomcatNeeded = false;
		if (!matchesDefaultFolder(Constants.POWIN_ENTITY_TYPE_DEVICE)) {
			copyConfigFiles(PowinProperty.INITIAL_DEVICES, Constants.POWIN_ENTITY_TYPE_DEVICE, destDir);
		} else {
			restartTomcatNeeded = true;
		}
		return restartTomcatNeeded;
	}

	/**
	 * Copy the default powin entities if needed
	 *
	 * @param entityType: Constants.POWIN_ENTITY_TYPE_APP / DEVICE
	 * @return true if restart of tomcat is required
	 */
	public static boolean resetPowinEntitiesToDefault(String entityType) {
		boolean restartTomcatNeeded = false;
		// Compare powin entities with default
		if (!matchesDefaultFolder(entityType)) {
			// they don't match so copy default
			if (Constants.POWIN_ENTITY_TYPE_APP.equals(entityType)) {
				String fileNamePattern=Constants.POWIN_APP_DIR+"*.json";
				FileHelper.backupFiles(fileNamePattern);
				copyConfigFiles(PowinProperty.INITIAL_APPS, Constants.POWIN_ENTITY_TYPE_APP, Constants.POWIN_APP_DIR);
			} else {
				String fileNamePattern=Constants.POWIN_DEVICE_DIR+"*.json";
				FileHelper.backupFiles(fileNamePattern);
				copyConfigFiles(PowinProperty.INITIAL_DEVICES, Constants.POWIN_ENTITY_TYPE_DEVICE,
						Constants.POWIN_DEVICE_DIR);
			}
			restartTomcatNeeded = true;
		}
		return restartTomcatNeeded;
	}

	/**
	 * Restore derate to the configurationName given the line to modify.
	 *
	 * @param line
	 */
	public static void restoreDerateToConfigurationName(String line) {
		String value = line.split(":|\\\",")[1];
		ScriptHelper.executeRemoteSSHCommand(REMOTE_IP, PowinProperty.TURTLEUSER.toString(),
				PowinProperty.TURTLEPASSWORD.toString(),
				"echo powin | sudo -S sed -i 's/" + value + "/" + value + "-derate01/g' /etc/powin/configuration.json");
	}

	public static void setFullPermissions(Path folderPath) {
		setFullPermissions(folderPath.toString());
	}
	public static void setFullPermissions(File folderPath) {
		setFullPermissions(folderPath.toString());
	}
	/**
	 * Set full read, write and execute permissions
	 * @param folderPath
	 */
	public static void setFullPermissions(String folderPath) {
		LOG.trace("Granting full permissions to {}", folderPath);
		setFilePermissions(folderPath, "777");
	}

	public static void setFilePermissions(Path folderPath, String permissions) {
		setFilePermissions(folderPath.toString(), permissions);
	}
	public static void setFilePermissions(File folderPath, String permissions) {
		setFilePermissions(folderPath.toString(), permissions);
	}
	public static void setFilePermissions(String folderPath, String permissions) {
		setFileOwner(folderPath, "powin:powin");
		String command = "echo " + Constants.PW + " | sudo -S chmod  -R " + permissions + " " + folderPath;
		if (Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			ScriptHelper.executeProcess("sh", "-c", command);
		} else {
			ScriptHelper.callScriptSudo(Constants.PW, "sudo chmod -R " + permissions + folderPath);
		}
	}

	public static void setFileOwner(String folderPath, String owner) {
		String command = "echo " + Constants.PW + " | sudo -S chown -R " + owner + " " + folderPath;
		if (Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			ScriptHelper.executeProcess("sh", "-c", command);
		} else {
			ScriptHelper.callScriptSudo(Constants.PW, "sudo chmod -R " + owner + " " + folderPath);
		}
	}
	public static String getFilePermissions(Path folderPath) {
		return getFilePermissions(folderPath.toString());
	}
	public static String getFilePermissions(File folderPath) {
		return getFilePermissions(folderPath.toString());
	}
    /**
     * Get the file permissions
     * @param path
     * @return
     */
	public static String getFilePermissions(String path) {
		String ret = "";
		List<String> permissions = ScriptHelper.executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", "ls -ld " + path + " | cut -c 2-10");
		if (!permissions.isEmpty()) {
			ret = permissions.get(0);
		}
		return ret;
	}
	
	/**
	 * For the local file system we can simply write new values.
	 *
	 * @param soc
	 */
	public static void setSoCSimulator(String soc) {
		if (Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
			final File directory = new File(Constants.POWIN_SOC_DIR);
			final File[] files = directory.listFiles();
			Arrays.stream(files).forEach(e -> {
				try {
					FileUtils.write(e, soc, Charset.defaultCharset());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
		} else {
			writeSoCToTurtle(soc);
		}
	}

	/**
	 * Toggle the property value from true to false and vice-versa.
	 *
	 * @param filePath TODO
	 */
	public static void toggleBooleanJSONLocal(String keyValue, String filePath) {
		boolean value = getJSONBooleanValue(readJsonLine(keyValue, filePath));
		String key = "\"" + keyValue + "\".*\\)";
		ScriptHelper.executeProcess("sh", "-c", "echo powin | sudo -S sed -i 's/\\(" + key + String.valueOf(value)
				+ "/\\1" + String.valueOf(!value) + "/g' " + filePath);
	}

	/**
	 * Toggle the property value from true to false and vice-versa.
	 *
	 * @param filePath TODO
	 */
	public static void toggleBooleanJSONRemote(String host, String keyValue, String user, String pw, String filePath) {
		String line = readRemoteJsonLine(host, keyValue, user, pw, filePath);
		boolean value = getJSONBooleanValue(line);
		String key = "\"" + keyValue + "\".*\\)";
		String command = "echo powin | sudo -S sed -i 's/\\(" + key + String.valueOf(value) + "/\\1"
				+ String.valueOf(!value) + "/g' " + filePath;
		ScriptHelper.executeRemoteSSHCommand(host, user, pw, command);
	}

	public static boolean turtleFileExists(String filepath) {
		boolean fileExists;
		if (SystemInfo.isTurtleLocal())
			fileExists = localFileExists(filepath);
		else
			fileExists = remoteFileExists(SystemInfo.getTurtleHost(), PowinProperty.TURTLEUSER.toString(),
					SystemInfo.getTurtlePassword(), filepath);
		return fileExists;
	}

	/*
	 * Given a list of fields and string values and the path to the json file
	 * compares the values of the fields with the given list of expected values. Use
	 * localhost if pathToFile is on the same machine.
	 */
	public static boolean verifyJsonStringValues(List<String> fields, List<String> expectedValues, String pathToFile,
			String hostIP) {
		boolean ret = true;
		String regex = org.apache.commons.lang3.StringUtils.join(fields, "|");
		List<String> verifyResults = eGrepJsonFile(hostIP, regex, pathToFile);
		for (int i = 0; i < verifyResults.size(); ++i) {
			ret &= verifyResults.get(i).contains(expectedValues.get(i));
		}
		return ret;
	}

	/**
	 * Give the system time to write the config file and display the app on the
	 * screen.
	 *
	 * @param appCode
	 */
	public static void waitForAppConfig(String appCode) {
		final File appPath = new File(Constants.POWIN_SOC_DIR);
		File[] files = appPath.listFiles((dir, name) -> name.matches(".*" + appCode + ".json"));
		for (int i = 0; i < 30; ++i) {
			if (files.length > 0) {
				break;
			}
			if (i % 10 == 0) {
				LOG.trace("Waiting for app config file");
			}
			CommonHelper.quietSleep(Constants.ONE_SECOND);
			files = appPath.listFiles((dir, name) -> name.matches(".*" + appCode + ".json"));
		}
		CommonHelper.quietSleep(Constants.FIVE_SECONDS);
	}

	/**
	 * Restore app config file to the state before running the test.
	 *
	 * @param turtleHost
	 * @param fileNameIncludingPath TODO
	 * @return
	 * @throws IOException
	 */
	public static String writeConfigFile(String turtleHost, String fileNameIncludingPath, String fileContents) {
		String filecontents = "";
		if ("localhost".equals(turtleHost)) {
			writeStringToFile(fileNameIncludingPath, fileContents);
			FileHelper.setFullPermissions(fileNameIncludingPath);
		} else {
			writeStringToRemoteFile(fileContents, fileNameIncludingPath, turtleHost, Constants.PW);
		}
		return filecontents;
	}

	public static void writeSoCToTurtle(String soc) {
		String remoteIP = "10.0.0.3";
		String cmd = "echo powin | sudo -S chmod 777 -R /etc/powin/soc";
		String cmd1 = "for f in /etc/powin/soc/*soc; do echo '" + soc + "' > $f; done";
		ScriptHelper.executeSimpleRemote(remoteIP, Constants.USER, Constants.PW, cmd);
		ScriptHelper.executeSimpleRemote(remoteIP, Constants.USER, Constants.PW, cmd1);
	}

	/**
	 * Write a string to a file.
	 *
	 * @param pathToFile
	 * @param contents
	 * @throws IOException
	 */
	public static void writeStringToFile(String pathToFile, String contents) {
		String fileDirectory = FileHelper.getDirectoryFromFilepath(pathToFile);
		CommonHelper.setSystemFilePermissons(fileDirectory, "777");
		byte[] strToBytes = contents.getBytes();
		try {
			Files.write(Paths.get(pathToFile), strToBytes);
		} catch (IOException e) {
			LOG.error("Unable to write to file", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void writeStringToFile(File mFile, String scriptText) {
		try (FileWriter mWriter = new FileWriter(mFile, true)) {
			mWriter.write(scriptText);
			mWriter.flush();
			System.out.print("Writing successfully!");
		} catch (IOException e) {
			System.out.print("Writing failed" + e.getLocalizedMessage());
		}
	}

	/**
	 * Writes the source string to the remote machine via scp.
	 *
	 * @param source
	 * @param destination
	 * @param host
	 * @param password
	 * @throws IOException
	 */
	public static void writeStringToRemoteFile(String source, String destination, String host, String password) {
		String tempFile = "/home/powin/tempfile";
		writeStringToFile(tempFile, source);
		copyFileToRemote(tempFile, destination, host, password);
	}

	/*
	 * FILE COPY
	 */

	private String csvFilePath = ALL;

	private Writer mFileWriter;

	private CSVPrinter mCsvWriter;

	public FileHelper() {

	}

	public FileHelper(String path) throws IOException {
		if (path.contains("/")) {
			csvFilePath = path;
		} else {
			csvFilePath = getCsvPath(path);
		}
		this.init();
	}

	public void addHeader(String commaSeparatedHeader) {
		writeToCSV(commaSeparatedHeader);
	}

	private void closeFile() {
		try {
			mCsvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteFileContents() throws IOException {
		new FileWriter(csvFilePath, false).close();
	}

	private String getCsvPath(String fileResourceName) {
		return PowinProperty.fromPropertyFileKey(fileResourceName);
	}

	private void init() throws IOException {
		try {
			this.mFileWriter = new OutputStreamWriter(new FileOutputStream(csvFilePath, true), StandardCharsets.UTF_8);
			// new FileWriter(csvFilePath, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.mCsvWriter = new CSVPrinter(this.mFileWriter, CSVFormat.EXCEL);
	}

	public String reportPath() {
		return csvFilePath;
	}

	public void writeToCSV(ArrayList<String> recordList) {
		for (String record : recordList) {
			writeToCSV(record);
		}
	}

	public void writeToCSV(String commaSeparatedString) {
		try {
			this.init();
			String[] x = commaSeparatedString.split(",");
			ArrayList<String> a = new ArrayList<>(Arrays.asList(x));
			mCsvWriter.printRecord(a);
		} catch (IOException e) {
			LOG.error("Unable to write to File", e);
			throw new RuntimeException(e.getMessage());
		}
		closeFile();
	}

	public void writeToCSV(String prefix, List<String> recordList) {
		for (String record : recordList) {
			writeToCSV(prefix + record);
		}
	}


	public static void createFolderWithFullPermissions(String randomFolderName) {
		String command = "sudo mkdir " + randomFolderName + " && sudo chmod -R 777 " + randomFolderName;
		ScriptHelper.runScriptRemotelyOnTurtle(command);
		CommonHelper.sleep(2000);
	}
	
	public static void setFileOwner(String filePath, String group, String user) {
		//sudo chown tomcat:tomcat *.*
		String command = "sudo chown " + group + ":"+user+ " " + filePath;
		ScriptHelper.runScriptRemotelyOnTurtle(command);
		CommonHelper.sleep(4000);
	}

	public static void backupAndDeleteFile(String fileName) {
		backupFiles(fileName);
	}

	public static void backupFiles(String fileNamePattern) {
		String backupFileCommand = "echo " + Constants.PW + " | for file in "+fileNamePattern+"; do mv -- \"$file\" \"$file\".bak;done" ;
		ScriptHelper.runScriptRemotelyOnTurtle(backupFileCommand);
		CommonHelper.quietSleep(Constants.FIVE_SECONDS);
	}



		//	public static boolean isInflectionPoint(List<Integer> movingVoltages) {
	//		//Check if the sum of the previous 3 entries is greater than the next 3 entries
	//		//First half
	//		int maxIndex=movingVoltages.size()/2 ;
	//		int cumVoltageFirstHalf=0;
	//		for (int idx=0;idx<maxIndex;idx++) {
	//				int voltage=movingVoltages.get(idx);
	//				cumVoltageFirstHalf += voltage ;
	//		}
	//		//Second half
	//		int cumVoltageSecondHalf=0;
	//		for (int idx=maxIndex+1;idx<movingVoltages.size();idx++) {
	//				int voltage=movingVoltages.get(idx);
	//				cumVoltageSecondHalf += voltage ;
	//		}
	//		
	//		boolean movingVoltageDrop= cumVoltageFirstHalf - cumVoltageSecondHalf > 50 ;
	//		boolean voltageDrops=movingVoltages.get(3) > movingVoltages.get(4);
	//		boolean nearKnee=true;
	//		
	//		
	////		if(ArrayDerate2.prevailingKnee <2)
	////			nearKnee= Math.abs(movingVoltages.get(4)-3450) < 10;
	////		else
	////			nearKnee= Math.abs(movingVoltages.get(4)-3550) < 10;
	//		return movingVoltageDrop && voltageDrops && nearKnee;
	//	}
		public static boolean isInflectionPoint(List<Integer> movingVoltages) {
		//Check if the sum of the previous 3 entries is greater than the next 3 entries
		//First half
		int maxIndex=movingVoltages.size()/2 ;
		int cumVoltageFirstHalf=0;
		for (int idx=0;idx<maxIndex;idx++) {
				int voltage=movingVoltages.get(idx);
				cumVoltageFirstHalf += voltage ;
		}
		//Second half
		int cumVoltageSecondHalf=0;
		for (int idx=maxIndex+1;idx<movingVoltages.size();idx++) {
				int voltage=movingVoltages.get(idx);
				cumVoltageSecondHalf += voltage ;
		}
		
		boolean movingVoltageDrop= cumVoltageFirstHalf - cumVoltageSecondHalf > 40 ;
		boolean voltageDrops=movingVoltages.get(3) - movingVoltages.get(4) > 30;
		boolean nearKnee=true;
		
		
	//	if(ArrayDerate2.prevailingKnee <2)
	//		nearKnee= Math.abs(movingVoltages.get(4)-3450) < 10;
	//	else
	//		nearKnee= Math.abs(movingVoltages.get(4)-3550) < 10;
		return voltageDrops && nearKnee;
	}

	public static List<Integer> getMovingVoltages(List<String> sampleData, int lineNumber) {
		List<Integer> movingVoltages= new ArrayList<Integer>();
		int sampleSize=7;
		int maxIndex=sampleSize/2 +1;
		for (int idx=1;idx<maxIndex;idx++) {
				int voltage=FileHelper.getAvgVoltageFromDataLine(sampleData,lineNumber - maxIndex +idx);
				movingVoltages.add(voltage);
		}
		//add the pivot term
		int pivotTerm=FileHelper.getMaxVoltageFromDataLine(sampleData,lineNumber );
		movingVoltages.add(pivotTerm);
		//Add the second half of the terms
		for (int idx=1;idx <maxIndex;idx++) {
				int voltage=FileHelper.getAvgVoltageFromDataLine(sampleData,lineNumber +idx);
				movingVoltages.add(voltage);
		}
		return movingVoltages;
	}

//	public static List<String> fileInterpolation(List<String> sampleData) {
//		List<List<String>> insertionDataList = new ArrayList<>();
//		int ascent = 0;
//		for (int lineNumber = 5; lineNumber < sampleData.size() - 3; lineNumber++) {
//			// Check for downward inflection --> get the adjacent pair of lines
//			List<Integer> movingVoltages= FileHelper.getMovingVoltages(sampleData, lineNumber);
//			boolean isInflectionPoint=	FileHelper.isInflectionPoint(movingVoltages);						 
//			if (isInflectionPoint) {
//				// Get ascent just before inflection
//				ascent = movingVoltages.get(3) - movingVoltages.get(0) ;
//				//apply ascent
//				int testDataVoltageMiddle = movingVoltages.get(3) + ascent;
//				ArrayDerate2Helper.LOG.trace("Inflection: testDataVoltage:{} testDataVoltageNext:{}", movingVoltages.get(3), movingVoltages.get(4));
//				// Get mid time
//				ZonedDateTime firstDate = DerateCommon.getPresentTime(sampleData.get(lineNumber), ArrayDerate2Helper.RESULT_FILE_TIME_INDEX);
//				ZonedDateTime midDate = firstDate.plusSeconds(2);
//				String midDateStr = DateTimeHelper.getFormattedTime(midDate, Constants.YYYY_MM_DD_HH_MM_SS);
//				String[] midData = sampleData.get(lineNumber).split("\\|");
//				midData[ArrayDerate2Helper.RESULT_FILE_TIME_INDEX] = midDateStr;
//				midData[ArrayDerate2Helper.RESULT_FILE_MAX_VOLTAGE_INDEX] = String.valueOf(testDataVoltageMiddle);
//				String testDataMiddle = String.join("|", midData);
//				ArrayDerate2Helper.LOG.trace("Inflection: midPointVoltage={}, midPointTime= {}, midData:{}", testDataVoltageMiddle,
//						midDateStr, testDataMiddle);
//				List<String> insertionData = new ArrayList<>();
//				insertionData.add(String.valueOf(lineNumber + 1));
//				insertionData.add(testDataMiddle);
//				insertionDataList.add(insertionData);
//			}
//		}
//		// Insert a line corresponding to the mid-point
//		for (int idx = 0, offset = 0; idx < insertionDataList.size(); idx++, offset++) {
//			int lineNumber = Integer.parseInt(insertionDataList.get(idx).get(0));
//			lineNumber += offset;
//			sampleData.add(lineNumber, insertionDataList.get(idx).get(1));
//		}
//		return sampleData;
//	}

	private static Connection remoteConn = null;

	public static boolean loginRemoteMachine(String ip, String user, String pw) {
		if (remoteConn == null)
			remoteConn = new Connection(ip);
		try {
			remoteConn.connect();
			return remoteConn.authenticateWithPassword(user, pw);
		} catch (IOException e) {
			LOG.error("Failed to logon to {}.", ip);
			e.printStackTrace();
		}
		return false;
	}

	public static int getFileAmount(String remotePath) {
		try {
			SFTPv3Client sft = new SFTPv3Client(remoteConn);
			List<?> v = sft.ls(remotePath);
			sft.close();
			return v.size();
		} catch (Exception e) {
			LOG.error("Failed to get the amount of files from remote location.", e);
			return Integer.MAX_VALUE;
		}
	}

	public static List<SFTPv3DirectoryEntry> getFileList(String remotePath) {
		try {
			SFTPv3Client sft = new SFTPv3Client(remoteConn);
			List<SFTPv3DirectoryEntry> v = sft.ls(remotePath);
			sft.close();
			return v;
		} catch (Exception e) {
			LOG.error("Failed to get the amount of files from remote location.", e);
			return null;
		}
	}

	public static void logoutRemoteMachine() {
		remoteConn.close();
	}
	

	
}