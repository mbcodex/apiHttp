package com.powin.modbusfiles.apps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.powin.dragon.app.powercommand.PowerCommandAppFactory;
import com.powin.modbusfiles.testbase.BaseIntegrationTest;
import com.powin.modbusfiles.utilities.AppInjectionCommon;
import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;

class BackstopAppIntegrationTest extends BaseIntegrationTest {
	static {
		BaseIntegrationTest.LOG = LogManager.getLogger();
	}
	
	@BeforeAll
	static void setupBeforeClass() throws Exception {
		initSystemForRegression();
		CommonHelper.quietSleep(2 * Constants.ONE_SECOND);
		BackstopApp.disablePowerApps();
	}
	
	@Override
	@BeforeEach
	public void setup(TestInfo testInfo) {
		super.setup(testInfo);
		CommonHelper.quietSleep(2 * Constants.ONE_SECOND);
		BackstopApp.enable();
	}
	
	@Override
	@AfterEach
	public void tearDown(TestInfo testInfo) {
		super.tearDown(testInfo);
		CommonHelper.quietSleep(2 * Constants.ONE_SECOND);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		BackstopApp.disablePowerApps();
	}

	//@Disabled
	@Tag("simulator")
	@Tag("backstop")
	@Tag("c3182_BackstopIsAlwaysOnTest")
	@Test
	void c3182_BackstopIsAlwaysOnTest() {
		boolean isTestPass = true;
		BackstopApp.enable();
		boolean statePersistsWhenEnabled = BackstopApp.verifyAppEnabled();
		BackstopApp.disable();
		boolean statePersistsWhenDisabled = BackstopApp.verifyAppEnabled();
		isTestPass = statePersistsWhenEnabled & statePersistsWhenDisabled;
		assertTrue(isTestPass);
	}
	
	//@Disabled
	@Tag("simulator")
	@Tag("backstop")
	@Tag("c3185_EnableAnyPowerMovingAppWillCauseAppstatusChangeTest")
	@Test
	void c3185_EnableAnyPowerMovingAppWillCauseAppstatusChangeTest() {
		BackstopApp.disablePowerApps();
		boolean isTestPass = true;
		if (BackstopApp.verifyBackStopAppStatusMessage(BackstopApp.SET_TO_STANDBY, BackstopApp.STATUS_MESSAGE_EXPECTED)) {
			PowerCommandApp.enablePowerCommandAppWithZeroPower();
			isTestPass &= BackstopApp.verifyWithPowerOff();
		} else {
			LOG.error("Failed to make Backstop status to standby, test abort.");
			isTestPass &= false;
		}
		assertTrue (isTestPass);
	}
	
	//@Disabled
	@Tag("simulator")
	@Tag("backstop")
	@Tag("c3188_StatusGoes2StandbyWhenNoPowerMovingAppIsOn")
	@Test
	void c3188_StatusGoes2StandbyWhenNoPowerMovingAppIsOn() {
		BackstopApp.disablePowerApps();
		boolean isTestPass = BackstopApp.verifyBackStopAppStatusMessage(BackstopApp.SET_TO_STANDBY, BackstopApp.STATUS_MESSAGE_EXPECTED);
		assertTrue (isTestPass);
	}
	
	//@Disabled
	@Tag("simulator")
	@Tag("backstop")
	@Tag("c3189_MovePowerAndVerifyAppstatusTest")
	@Test
	void c3189_MovePowerAndVerifyAppstatusTest() {
		BackstopApp.disablePowerApps();
		PowerCommandApp.sendCommandToPowerCommandApp(50, 0, Constants.ENABLE);
		AppInjectionCommon.verifyAppEnabledStatus(PowerCommandAppFactory.APPCODE, 1);
		boolean isTestPass = BackstopApp.verifyWithPowerOn();
		assertTrue( isTestPass);
	}
}
