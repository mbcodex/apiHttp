
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

import com.powin.modbusfiles.apps.SunspecPowerAppIntegrationTest;
import com.powin.modbusfiles.awe.ZeroConfigFromStringIntegrationTest;
import com.powin.modbusfiles.awe.ZeroConfigTurtleToStringIntegrationTest;
import com.powin.modbusfiles.modbus.ModBusEndpointsSuite;

@RunWith(JUnitPlatform.class)
@SelectClasses({ SunspecPowerAppIntegrationTest.class, ZeroConfigFromStringIntegrationTest.class,
		ZeroConfigTurtleToStringIntegrationTest.class, ModBusEndpointsSuite.class })
public class AllSimulatorTests {

}
