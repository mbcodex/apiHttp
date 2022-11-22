package com.powin.modbusfiles.utilities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonParserHelperIntegrationTest {
	private final static Logger LOG = LogManager.getLogger();

	@BeforeClass
	public static void setupBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {// for (Map.Entry<String, Number> entry : m.entrySet()) {
//		System.out.println(entry);
//	}

	}

	@Test
	public void testGetJSONFromFile() {
		String path = "src/test/resources/" + "Stack225Gen2_2SafetyAndNotificationConfig.json";
		JSONObject jsonFromFile = JsonParserHelper.getJSONFromFile(path);
		assertThat(jsonFromFile.get("highCellGroupTemperatureWarningClear"), is(44.0));
	}
	
	@Test
	public void testGetFieldsFromJSON() {
		//String path = "src/test/resources/" + "Stack225Gen2_2SafetyAndNotificationConfig.json";
		File testFile = FileHelper.getFileFromResources("toolsPermissions.json",FileHelper.class);
		JSONObject jsonFromFile =JsonParserHelper.getJSONFromFile(testFile.getAbsolutePath());
		Set<String > fieldsSet=jsonFromFile.keySet();
		List<String> fieldsList=new ArrayList<>(fieldsSet);
		LOG.info(CommonHelper.convertArrayListToString(fieldsList));
		//Collection values=jsonFromFile.values();
		//[reportEnabled, ptcEnabled, simulatedValueInjectionEnabled, monitorEnabled, controlEnabled, uiEnabled, diagnosticEnabled]
		//assertThat(jsonFromFile.get("highCellGroupTemperatureWarningClear"), is(44.0));
	}
	
	/**
	 * Read the Json object from the resource.
	 * In this example the resource is in the qilan jar.
	 * @throws Exception
	 */
	@Test
	public void getJsonFromResources() throws Exception {
		JSONObject json = JsonParserHelper.parseJsonFromResource("/com/powin/qilin/configuration/Stack225Gen2_2SafetyAndNotificationConfig.json");
		LOG.info("lowStringVoltageAlarmSet = {}", json.get("lowStringVoltageAlarmSet"));
		LOG.info("lowStringVoltageAlarmClear = {}", json.get("lowStringVoltageAlarmClear"));

		json = JsonParserHelper.parseJsonFromResource("/com/powin/qilin/configuration/Stack140Gen2_0SafetyAndNotificationConfig.json");
		LOG.info("lowStringVoltageAlarmSet = {}", json.get("lowStringVoltageAlarmSet"));
		LOG.info("lowStringVoltageAlarmClear = {}", json.get("lowStringVoltageAlarmClear"));
		
		json = JsonParserHelper.parseJsonFromResource("/com/powin/qilin/configuration/Stack230Gen2_2SafetyAndNotificationConfig.json");
		LOG.info("lowStringVoltageAlarmSet = {}", json.get("lowStringVoltageAlarmSet"));
		LOG.info("lowStringVoltageAlarmClear = {}", json.get("lowStringVoltageAlarmClear"));
		
	}
	
	@Test
	public void testToSnakeCase() {
		assertThat(JsonParserHelper.toSnakeCase("lowStringVoltageWarningEnabled"),
				is("LOW_STRING_VOLTAGE_WARNING_ENABLED"));
	}

	@Test
	public void testToSnakeCaseKeys2() throws Exception {
		String path = "src/test/resources/" + "Stack225Gen2_2SafetyAndNotificationConfig.json";
		JSONObject jsonFromFile = JsonParserHelper.getJSONFromFile(path);
		@SuppressWarnings("unchecked")
		Map<String, Number> m = JsonParserHelper.snakeCaseKeys(jsonFromFile);
		for (Map.Entry<String, Number> entry : m.entrySet()) {
			System.out.println(entry);
			assertThat(entry.getKey().contains("_"), is(true));

		}
	}

}
