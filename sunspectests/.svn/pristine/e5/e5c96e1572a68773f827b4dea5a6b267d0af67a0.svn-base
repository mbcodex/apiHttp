package com.powin.modbusfiles.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.powin.modbusfiles.utilities.PowinProperty;

class JdbcConnectionIntegrationTest {
	private final static Logger LOG = LogManager.getLogger();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
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
	void testGetConnection() {
		PowinProperty.setValue(PowinProperty.DATABASE_URL.getKey(), "jdbc:postgresql://localhost:5432/coblynaudb");
		PowinProperty.setValue(PowinProperty.DATABASE_USER.getKey(), "coblynaudbuser");
		PowinProperty.setValue(PowinProperty.DATABASE_PASSWORD.getKey(), "coblynaudbuser");
		Optional<Connection> connection = JdbcConnection.getConnection();
		LOG.info("Database connected.");
		Connection conn = connection.get();
		LOG.info("Got the connection to the db.");
		String sql = "select * from kobold2.dragonkey";
		try {
			PreparedStatement prepareStatement = conn.prepareStatement(sql);
			ResultSet rs = prepareStatement.executeQuery();

			while (rs.next()) {
				System.out.println("DragonId：" + rs.getString("dragonid"));
				System.out.println("StationCode：" + rs.getString("stationcode"));
				System.out.println("DragonKey：" + rs.getString("dragonkey"));
			}
			int k = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = 0;
	}

}
