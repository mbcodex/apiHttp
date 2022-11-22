package com.powin.modbusfiles;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class sohIntegrationTest {
	//Not to be run as stand-alone
	private static Stream<Arguments> pgParametersValid() {
		return Stream.of(
				Arguments.of(8.63,189.6),
				Arguments.of(8.00,189.6),
				Arguments.of(8.00,173.0),
				Arguments.of(8.00,230),
				Arguments.of(8.63,173),
				Arguments.of(8.63,230)
		);
	}
	
	private static Stream<Arguments> pgParametersInvalid() {
		return Stream.of(
				Arguments.of(0,2), //pg not defined
				Arguments.of(21,58), //cycle out of range
				Arguments.of(5,3000), //cycle out of range
				Arguments.of(21,3000) //cycle out of range
		);
	}

	@ParameterizedTest
	@MethodSource("pgParametersValid")
	final void testGetInterpolatedSohValid(double inputCycle,double inputEnergy) {
		Assertions.assertTrue( soh.getInterpolatedSoh(inputCycle,inputEnergy));
	}
	@ParameterizedTest
	@MethodSource("pgParametersInvalid")
	final void testGetInterpolatedSohInvalid(double inputCycle,double inputEnergy) {
		Assertions.assertFalse( soh.getInterpolatedSoh(inputCycle,inputEnergy));
	}

	@Test
	final void testGetStringTimestamp() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetStringMeasuredVoltage() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetStringCalculatedVoltage() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetStringSoc() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetSunspecSoc() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetLastCallsSoc() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetSocDriftData() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetSocDataFromTurtleLog() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testProcessTurtleData() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testStitchFiles() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testConsolidateFile() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetCombinedTurtleAndStringReportData() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testTestSocCalculation() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testValidateReportedSoc() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testMain() {
		fail("Not yet implemented"); // TODO
	}

}
