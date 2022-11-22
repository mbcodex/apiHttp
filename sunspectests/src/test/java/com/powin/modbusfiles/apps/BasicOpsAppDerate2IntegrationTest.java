package com.powin.modbusfiles.apps;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import com.powin.modbusfiles.testbase.BaseIntegrationTest;

// @Ignore
@TestMethodOrder(MethodOrderer.MethodName.class)
class BasicOpsAppDerate2IntegrationTest extends BaseIntegrationTest {

	public static BasicOpsApp basicOps;
	static {
		BaseIntegrationTest.LOG = LogManager.getLogger();
	}
	
//	@BeforeAll
//	public static void setupBeforeClass() throws Exception {
//		LOG.info("setupBeforeClass");
//
//		// initSystemForRegression();
//		ArrayDerate2.setupArrayDerate2();
//		RotationControl.moveIntoRotation(ARRAY_INDEX);
//		Contactors.turtleToolCloseContactors(ARRAY_INDEX);
//		DeviceInstaller.removeBasicopParametersJson();
//		DeviceInstaller.installBasicopParametersJson(true);
//		MovePower.disableSunspecPower();
//
//		CommonHelper.restartTurtleTomcat();
//
//		basicOps = new BasicOpsApp(ARRAY_INDEX, STRING_INDEX);
//		LOG.info("init finished.");
//	}
//
//	@AfterAll
//	static void tearDownAfterClass() throws Exception {
//		DeviceInstaller.removeBasicopParametersJson();
//		DeviceInstaller.installBasicopParametersJson(false);
//		MovePower.disableSunspecPower();
//		CommonHelper.restartTurtleTomcat();
//		BasicOpsApp.disable();
//	}
//
//	@Override
//	@BeforeEach
//	public void setup(TestInfo testinfo) {
//		super.setup(testinfo);
//		CommonHelper.resetFaults();
//		CommonHelper.waitForSystemReady();
//		// MovePower.disableSunspecPower();
//		// CommonHelper.quietSleep(Constants.FIVE_SECONDS);
//	}
//
//	@Override
//	@AfterEach
//	public void tearDown(TestInfo testInfo) {
//		BasicOpsApp.disable();
//		super.tearDown(testInfo);
//	}
//
//	// @Test
//	// @Tag ("live")
//	// @Tag ("basicopDerate2")
//	// public void C531_BulkChargeTo100Test() {
//	// assertTrue(basicOps.C531_BulkChargeTo100());
//	// }
//	// @Test
//	// @Tag ("live")
//	// @Tag ("basicopDerate2")
//	// public void C521_BulkDischargeTo0Test() {
//	// assertTrue(basicOps.C521_BulkDischargeTo0());
//	// }
//	@Test
//	@Tag("basicopDerate2")
//	public void C524_EnableBasicOpsTest() {
//		assertTrue(basicOps.C524_EnableBasicOps());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	public void C526_PriorityPowerDischargeTest() {
//		assertTrue(basicOps.C526_PriorityPowerDischarge());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	public void C527_PrioritySocChargeTest() {
//		assertTrue(basicOps.C527_PrioritySocCharge());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	public void C528_PriorityPowerSocChargeTest() {
//		assertTrue(basicOps.C528_PriorityPowerSocCharge());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	@Tag("c529_PrioritySocPowerDischargeTest")
//	public void c529_PrioritySocPowerDischargeTest() {
//		assertTrue(basicOps.C529_PrioritySocPowerDischarge());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	@Tag("c530_DisableBasicOpsTest")
//	public void c530_DisableBasicOpsTest() {
//		assertTrue(basicOps.C530_DisableBasicOps());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	@Tag("c537_PriorityPowerChargeTest")
//	public void c537_PriorityPowerChargeTest() {
//		assertTrue(basicOps.C537_PriorityPowerCharge());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	@Tag("c538_PrioritySocDischargeTest")
//	public void c538_PrioritySocDischargeTest() {
//		assertTrue(basicOps.C538_PrioritySocDischarge());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	@Tag("c539_PriorityPowerSocDischargeTest")
//	public void c539_PriorityPowerSocDischargeTest() {
//		assertTrue(basicOps.C539_PriorityPowerSocDischarge());
//	}
//
//	@Test
//	@Tag("basicopDerate2")
//	@Tag("c540_PrioritySocPowerChargeTest")
//	public void c540_PrioritySocPowerChargeTest() {
//		assertTrue(basicOps.C540_PrioritySocPowerCharge());
//	}
//
//	/**
//	 * This test requires at least two stacks to be in rotation.
//	 */
//	@Test
//	@Tag("stacksweep")
//	public void C677_StackSweepTest() {
//		List<Integer> numberOfStacks = new ArrayList<Integer>();
//		List<StringReport> stringList = Lastcall.getStringReportList(PowinProperty.ARRAY_INDEX.intValue());
//		for (StringReport report : stringList)
//			numberOfStacks.add(report.getStringIndex());
//		if (numberOfStacks.size() > 1) {
//			LOG.info("There are {} stacks", numberOfStacks);
//			RotationControl.moveIntoRotation(PowinProperty.ARRAY_INDEX.intValue());
//			if (RotationControl.verifyInRotationStatus(20, PowinProperty.ARRAY_INDEX.intValue()) == false) {
//				fail("Failed to move stacks into rotation.");
//			}
//			boolean c677_StackSweep_result = basicOps.C677_StackSweep(99, numberOfStacks);
//
//			assertTrue(c677_StackSweep_result);
//		} else {
//			fail("There we're not enough stacks in rotation to run this test.");
//		}
//		Contactors.turtleToolCloseContactors(ARRAY_INDEX);
//	}
//
//	@Test
//	public void C678_InitialRelaxTest() {
//		List<StringReport> stringList = Lastcall.getStringReportList(PowinProperty.ARRAY_INDEX.intValue());
//		if (stringList.size() > 1) {
//			LOG.info("There are {} stacks", stringList.size());
//			assertTrue(basicOps.C678_InitialRelax(99));
//		} else {
//			fail("There are not enough stacks in rotation to run this test.");
//		}
//	}
//
//
//	// @Test
//	// @Tag("live")
//	// @Tag("basicopDerate2")
//	// public void C683_TopOffToTargetTest() {
//	// assertTrue(basicOps.C683_TopOffToTarget(50));
//	// }
//
//	@Test
//	@Tag("basicopDerate2")
//	@Tag("slow")
//	@Tag("c695_PrioritySocDoneTest")
//	public void c695_PrioritySocDoneTest() {
//		assertTrue(basicOps.C695_PrioritySocDone(Constants.CHARGING));
//		assertTrue(basicOps.C695_PrioritySocDone(Constants.DISCHARGING));
//	}
//
//	private static Stream<Arguments> targetPowerValues_UnitWatt() {
//		return Stream.of(arguments(1234, 1000, "1.0 kW"), arguments(12345, 12000, "12.0 kW"),
//				arguments(123456, 123000, "123.0 kW"), arguments(1234567, 1234000, "1234.0 kW"),
//				arguments(-1234, -1000, "-1.0 kW"), arguments(-12345, -12000, "-12.0 kW"),
//				arguments(-123456, -123000, "-123.0 kW"), arguments(-1234567, -1234000, "-1234.0 kW"));
//	}
//
//	@Tag("basicopDerate2")
//	@Tag("slow")
//	@ParameterizedTest
//	@Tag("setBopPriorityPowerTest")
//	@MethodSource("targetPowerValues_UnitWatt")
//	public void setBopPriorityPowerTest(int targetP, int expectedPowCmd, String expectedStatus) throws ModbusException,
//			InterruptedException, KeyManagementException, NoSuchAlgorithmException, IOException, ParseException {
//		int targetSoc;
//		targetSoc = targetP > 0 ? 5 : 95;
//		ModbusPowinBlock.getModbusPowinBlock().setBasicOpPriorityPower(targetP, targetSoc);
//		Thread.sleep(20000);
//		String actualBopStatus = SystemInfo.getAppStatus(BasicOpAppFactory.APPCODE);
//
//		int actualTargetPowerCmd = Integer
//				.parseInt(ModbusPowinBlock.getModbusPowinBlock().getBasicOpTargetPowerCommand());
//
//		assert (actualBopStatus.contains(expectedStatus));
//		assertEquals(expectedPowCmd, actualTargetPowerCmd);
//	}
//
//	@Test
//	void c4036_PrioritySocEnableWithStackInNearlineTest() {
//		assertTrue(basicOps.c4036_PrioritySocEnableWithStackInNearline());
//	}
//
//	@Test
//	void c4037_PrioritySocEnableWithAllContactorsClosedTest() {
//		assertTrue(basicOps.c4037_PrioritySocEnableWithAllContactorsClosed());
//	}
//
//	@Test
//	void c4038_PrioritySocEnableWithAllContactorsOpenTest() {
//		assertTrue(basicOps.c4038_PrioritySocEnableWithAllContactorsOpen());
//	}
//
//	@Test
//	void c4039_PrioritySocEnableWithContactorsOpenAndClosedTest() {
//		assertTrue(basicOps.c4039_PrioritySocEnableWithContactorsOpenAndClosed());
//	}
//
//	@Test
//	void C525_StartUpDelayTest() {
//		assertTrue(basicOps.C525_StartUpDelay());
//	}
//
//	@Test
//	void closeMultipleStackContactorsTestTest() {
//		assertTrue(basicOps.closeMultipleStackContactorsTest(50));
//	}
}
