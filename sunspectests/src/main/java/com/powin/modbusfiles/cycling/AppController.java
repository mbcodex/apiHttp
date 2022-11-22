package com.powin.modbusfiles.cycling;

import static com.powin.modbusfiles.utilities.Constants.FIVE_SECONDS;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.apps.SlowChargeApp;
import com.powin.modbusfiles.modbus.ModbusPowinBlock;
import com.powin.modbusfiles.power.MovePower;
import com.powin.modbusfiles.utilities.CommonHelper;

/**
 * Centralized App Control, start, stop, enable, disable.
 *
 * @author RAF
 *
 */
public class AppController {
	private final static Logger LOG = LogManager.getLogger();
	private static Map<String, Exception> forceExceptions = new HashMap<>();
	public static SlowChargeApp scApp = new SlowChargeApp();
	private static final int APPCONTROL_START_INDEX = 2;

	public enum AppControl {
		STOP_SUNSPEC, STOP_BASICOP, STOP_SLOWCHARGE, START_SUNSPEC, START_BASICOP, START_SLOWCHARGE
	}

	/**
	 * Start or stops and app, retrying on failure.
	 *
	 * @param action
	 */
	public static void controlApps(AppControl action) {
		boolean mSucceededInControllingApps = false;

		while (!mSucceededInControllingApps) {
			try {
				throwExceptionIfSet("CONTROL_APPS1");
				switch (action) {
				case STOP_BASICOP:
					ModbusPowinBlock.getModbusPowinBlock().disableBasicOp();
					break;
				case STOP_SUNSPEC:
					MovePower.disableSunspecPower();
					break;
				case STOP_SLOWCHARGE:
					scApp.disable();
					break;
				case START_BASICOP:
					ModbusPowinBlock.getModbusPowinBlock().enableBasicOp();
					break;
				case START_SUNSPEC:
					ModbusPowinBlock.getModbusPowinBlock().enableSunspec();
					break;
				case START_SLOWCHARGE:
					scApp.enable();
					break;
				}
				mSucceededInControllingApps = true;
				CommonHelper.quietSleep(FIVE_SECONDS);
			} catch (Exception e) {
				String startstop = action.ordinal() > APPCONTROL_START_INDEX ? "start" : "stop";
				LOG.error("Failed to {} apps. Will retry in {} milliseconds.", startstop, FIVE_SECONDS, e);
				CommonHelper.quietSleep(FIVE_SECONDS);
			}
		}
	}

	protected static void setExceptionDriver(String key, Exception value) {
		forceExceptions.put(key, value);
	}

	/**
	 * 
	 * @param key
	 * @throws Exception
	 */
	private static void throwExceptionIfSet(String key) throws Exception {
		Exception e = forceExceptions.get(key);
		if (null != e) {
			forceExceptions.put(key, null);
			throw e;
		}
	}

}
