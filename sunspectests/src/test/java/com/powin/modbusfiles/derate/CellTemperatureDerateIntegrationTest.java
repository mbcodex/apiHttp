package com.powin.modbusfiles.derate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.powin.modbusfiles.derating.CellTemperatureDerate;
import com.powin.modbusfiles.derating.DerateCommon;
import com.powin.modbusfiles.derating.DerateCommon.DerateParameters;
import com.powin.modbusfiles.testbase.BaseIntegrationTest;
import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Result;

public class CellTemperatureDerateIntegrationTest extends BaseIntegrationTest {
	static {
		BaseIntegrationTest.LOG = LogManager.getLogger();
	}

	@BeforeAll
	public static void setupBeforeClass() throws Exception {
		initSystemForRegression();
		BaseIntegrationTest.moveAllStacksInAllArraysIntoRotationAndCloseContactors();
		DerateCommon.setTemperatureDerate(DerateParameters.ENABLE,DerateParameters.DERATE_FOLDER_DOES_NOT_EXIST);
		CommonHelper.restartTurtleTomcat();
	}

	@AfterAll
	public static void tearDownAfterClass() {
		DerateCommon.resetDerateFolder();
		CommonHelper.restartTurtleTomcat();
	}

	@Override
	@BeforeEach
	public void setup(TestInfo testInfo) {
		super.setup(testInfo);
		boolean tomcatRestartNeeded = false;
		tomcatRestartNeeded = BaseIntegrationTest.setSocStackSimulator(50, 5, 5);
		if (tomcatRestartNeeded) {
			CommonHelper.restartTurtleTomcat();
			CommonHelper.sleep(3000);
		}
	}

	@Override
	@AfterEach
	public void tearDown(TestInfo testInfo) {
		super.tearDown(testInfo);
		LOG.info("Test {} Complete", testInfo.getDisplayName());
	}

	@Tag("simulator")
	@Tag("celltemperaturederate")
	@Tag("cellTemperatureDerateTest_11")
	@Test
	public void cellTemperatureDerateTest_11() throws Exception {
		boolean isTestPass = cellTemperatureDerateTest(true,1, 1);
		assertTrue(isTestPass, Result.getResults().toString());
	}

	@Tag("simulator")
	@Tag("celltemperaturederate")
	@Tag("cellTemperatureDerateTest_34")
	@Test
	public void cellTemperatureDerateTest_34() throws Exception {
		boolean isTestPass = cellTemperatureDerateTest(true,3, 4);
		assertTrue(isTestPass, Result.getResults().toString());
	}

	private boolean cellTemperatureDerateTest(boolean charge,int arrayCount, int stringsPerArray) throws Exception {
		CellTemperatureDerate temperatureDerate = new CellTemperatureDerate();
		temperatureDerate.init(arrayCount, stringsPerArray);
		BaseIntegrationTest.moveAllStacksInAllArraysIntoRotationAndCloseContactors() ;
		//TODO: Move the temperature baseline setting to main test once we have the tests have variable temp array inices
		//CellTemperatureDerate.setSystemTemperature(25.0, 1, 1, 11, 24);
		return CellTemperatureDerate.triggerDerate(1);
	}
}
