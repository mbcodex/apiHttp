package com.powin.modbusfiles.apps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.powin.dragon.app.recharger.RechargerAppFactory;
import com.powin.modbusfiles.power.MovePower;
import com.powin.modbusfiles.reports.SystemInfo;
import com.powin.modbusfiles.testbase.BaseIntegrationTest;
import com.powin.modbusfiles.utilities.AppInjectionCommon;
import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;
import com.powin.modbusfiles.utilities.FileHelper;

public class RechargerAppIntegrationTest extends BaseIntegrationTest {
	private static final int RETRY_COUNT = 10;
	public static RechargerApp rechargerApp;
	static {
		BaseIntegrationTest.LOG = LogManager.getLogger();
	}
	
	@BeforeAll
	public static void setupBeforeClass() throws Exception {
		boolean tomcatRestartNeeded = FileHelper.resetDefaults();
		tomcatRestartNeeded |= CommonHelper.setInverterDefaults();
		// disableZeroConfig();
		if (SystemInfo.getSoc() > 70) {
			CommonHelper.setSoC(50);
			tomcatRestartNeeded = true;
			CommonHelper.waitForSystemReady();
		}

		if (tomcatRestartNeeded) {
			CommonHelper.restartTurtleTomcat();
		}
		rechargerApp = new RechargerApp();
	}

	@AfterAll
	public static void tearDownAfterClass() throws Exception {
		cleanup();
		LOG.info("Finish {}", RechargerAppIntegrationTest.class.getName());
	}

	@Override
	@BeforeEach
	protected void setup(TestInfo testInfo) {
		super.setup(testInfo);
		try {
			disableRechargerApp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void disableRechargerApp() throws Exception {
		RechargerApp.disableRechargerApp();
		boolean isAppStatusEqual = AppInjectionCommon.verifyAppEnabledStatus(RechargerAppFactory.APPCODE, Constants.DISABLED);
		assertTrue(isAppStatusEqual);
	}

	@Tag("simulator")
	@Tag("recharger")
	@Tag("enableRechargerAppTest")
	@Test
	public void enableRechargerAppTest() throws Exception {
		RechargerApp.enableRechargerApp();
		boolean isAppStatusEqual = AppInjectionCommon.verifyAppEnabledStatus(RechargerAppFactory.APPCODE, Constants.ENABLED);
		assertTrue(isAppStatusEqual);
	}

	/**
	 * Test c3267, c3193 Move Power when no other apps are moving power and that we
	 * are below the trigger point and will charge up to the target.
	 *
	 * @throws Exception
	 */
	@Tag("simulator")
	@Tag("recharger")
	@Tag("movePowerWhenSoCLowerThanTriggerAndStopsAtTarget")
	@Test
	public void movePowerWhenSoCLowerThanTriggerAndStopsAtTarget() throws Exception {
		int soc = SystemInfo.getSoc();
		LOG.info("The soc is:{}", soc);
		int socTrigger = soc + 3;
		int socTarget = soc + 5;
		RechargerApp.enableRechargerApp(socTrigger, socTarget);
		assertTrue(MovePower.isPowerFlowing(RETRY_COUNT), "Power is not flowing!");
		LOG.info("Power is moving.");
		String appStatus = SystemInfo.getAppStatus(RechargerAppFactory.APPCODE);
		LOG.info("appStatus:[{}]", appStatus);
		assertTrue(appStatus.contains("Recharging all AC Batteries to " + String.valueOf(socTarget)
		+ "% SOC. BasicOp Status : TopOff  "));
		long endtime = DateTimeUtils.currentTimeMillis() + Constants.FIVE_MINUTES_MS;
		while (DateTimeUtils.currentTimeMillis() < endtime && compareAppStatus(appStatus)) {
			CommonHelper.quietSleep(Constants.TEN_SECONDS);
		}
		assertEquals("--- (All AC batteries above " + String.valueOf(socTrigger) + "% SOC",
				SystemInfo.getAppStatus(RechargerAppFactory.APPCODE));
		assertEquals(socTarget, SystemInfo.getSoc(), 2);
	}

	/**
	 * Helper to monitor the status.
	 *
	 * @param appStatus
	 * @return
	 */
	private boolean compareAppStatus(String appStatus) {
		String temp = SystemInfo.getAppStatus(RechargerAppFactory.APPCODE);
		LOG.info("SoC:{}, {}", SystemInfo.getSoc(), temp);
		return temp.startsWith("Recharging");
	}

	/**
	 * test c3268
	 *
	 * @throws Exception
	 */
	@Tag("simulator2")
	@Tag("recharger")
	@Tag("noPowerMovesWhenSoCHigherThanTrigger")
	@Test
	public void noPowerMovesWhenSoCHigherThanTrigger() throws Exception {
		int soc = SystemInfo.getSoc();
		LOG.info("The soc is:{}", soc);
		RechargerApp.enableRechargerApp(soc - 10, soc + 10);
		assertEquals("--- (All AC batteries above " + String.valueOf(soc - 10) + "% SOC",
				SystemInfo.getAppStatus(RechargerAppFactory.APPCODE));

	}

}
