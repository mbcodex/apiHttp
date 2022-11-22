package mbTest.utilities;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PowinSystemHelper {
	private final static Logger LOG = LogManager.getLogger();

	public static String getAppStatusRaw(String app) {
		String ret = "";
		List<String> results = ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c",
				"curl -k https://localhost:8443/" + app + "/status");
		if (!results.isEmpty()) {
			ret = results.get(0);
		}
		return ret;
	}

	public static boolean getAppStatus(String app) {
		return getAppStatusRaw(app).contains("OK");
	}

	public static void logAppStatus() {
		String [] apps = {"turtle", "kobold", "coblynau", "knocker", "primrose"};
		for (String app : apps) {
			LOG.info("{} status is {} ", StringUtils.capitalize(app),  PowinSystemHelper.getAppStatus(app) ? "OK" : "not OK");	
		}
	}

	public static void logAppFolderContents() {
		File[] filesMatchingPattern = FileHelper.getFilesMatchingPattern("/etc/powin/app/.*json");
		LOG.info(Arrays.asList(filesMatchingPattern));
	}

}
