package com.powin.modbusfiles.configuration;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;
import com.powin.modbusfiles.utilities.FileHelper;

public class DragonKey {
	private final static Logger LOG = LogManager.getLogger();

	public boolean regenerateDragonKey() {
		LOG.info("Removing dragonkey.json.");
		FileHelper.removeFiles(Constants.DRAGON_KEY_FILE);
		if ((new File(Constants.DRAGON_KEY_FILE)).exists()) {
			LOG.error("Failed to remove dragonkey.json, test abort.");
			return false;
		} else {
			LOG.info("dragonkey.json is deleted, restarting Tomcat.");
			CommonHelper.restartTurtleTomcat();
			if ((new File(Constants.DRAGON_KEY_FILE)).exists()) {
				LOG.info("PASS: dragonkey.json is re-generated.");
				return true;
			} else {
				LOG.error("FAIL: dragonkey.json is not re-generated.");
				return false;
			}
		}
	}

	public static void main(String[] args) {
		DragonKey dragon = new DragonKey();
		dragon.regenerateDragonKey();
	}
}
