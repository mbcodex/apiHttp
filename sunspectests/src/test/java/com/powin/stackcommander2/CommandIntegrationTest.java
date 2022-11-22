package com.powin.stackcommander2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandIntegrationTest {

	@BeforeAll
	static void setupBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testInit() {
		String s = "SET_POWER_SUNSPEC, 50, 0";
		String[] temp = s.split(",");
		Command cmd = Command.fromName(temp[0]);
		assertEquals(Command.SET_POWER_SUNSPEC, cmd);
		cmd.setParameters(Arrays.copyOfRange(temp, 1, temp.length));
		assertEquals(2, cmd.getParameters().size());
	}

}
