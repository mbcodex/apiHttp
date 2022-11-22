package com.powin.modbusfiles.modbus;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbus.ModbusException;
import com.powin.modbus.sunspec.SunspecMasterEndpoint;
import com.powin.modbus.sunspec.SunspecTcpMasterEndpointFactory;
import com.powin.modbus.sunspec.blocks.DeltaConnectThreePhaseabcMeterBlockCommon.Events;
import com.powin.modbus.sunspec.blocks.DeltaConnectThreePhaseabcMeterBlockMaster;
import com.powin.modbusfiles.utilities.PowinProperty;;

public class Modbus204 {
	private final static Logger LOG = LogManager.getLogger();

	private final String cModbusHostName;
	private final int cModbusPort;
	private final int cModbusUnitId;
	private final boolean cEnableModbusLogging;

	private SunspecMasterEndpoint cSunspecMasterEndpoint;
	private DeltaConnectThreePhaseabcMeterBlockMaster cDeltaConnectThreePhaseabcMeterBlockMaster;

	public Modbus204(String modbusHostName, int modbusPort, int modbusUnitId, boolean enableModbusLogging) {
		cModbusHostName = modbusHostName;
		cModbusPort = modbusPort;
		cModbusUnitId = modbusUnitId;
		cEnableModbusLogging = enableModbusLogging;
		init();
	}

	public void init() {
		SunspecTcpMasterEndpointFactory mSunspecMasterEndpointFactory = new SunspecTcpMasterEndpointFactory(
				cModbusHostName, cModbusPort, cModbusUnitId, cEnableModbusLogging, 10000);
		cSunspecMasterEndpoint = mSunspecMasterEndpointFactory.getSunspecMasterEndpoint(null);

		cDeltaConnectThreePhaseabcMeterBlockMaster = cSunspecMasterEndpoint.getBlock(203, 0);
		if (cDeltaConnectThreePhaseabcMeterBlockMaster == null) {
			throw new IllegalStateException("No Delta Meter!");
		}
	}

	// Getters
	public int getID() {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getId();
	}

	public BigDecimal getTotalAcCurrent() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getAmps();
	}

	public BigDecimal getCurrentPhaseA() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getAmpsPhaseA();
	}

	public BigDecimal getCurrentPhaseB() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getAmpsPhaseB();
	}

	public BigDecimal getCurrentPhaseC() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getAmpsPhaseC();
	}

	public int getCurrentSf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getA_SF();
	}

	public BigDecimal getPhaseVoltageLN() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVoltageLN();
	}

	public BigDecimal getPhaseVoltageLL() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVoltageLL();
	}

	public BigDecimal getPhaseVoltageAB() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPhVphAB();
	}

	public BigDecimal getPhaseVoltageBC() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPhVphBC();
	}

	public BigDecimal getPhaseVoltageCA() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPhVphCA();
	}

	public BigDecimal getPhaseVoltageAN() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPhaseVoltageAN();
	}

	public BigDecimal getPhaseVoltageBN() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPhaseVoltageBN();
	}

	public BigDecimal getPhaseVoltageCN() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPhaseVoltageCN();
	}

	public int getVoltageSf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getV_SF();
	}

	public BigDecimal getHz() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getHz();
	}

	public int getHzSf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getHz_SF();
	}

	public int getWatts() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getWatts().intValue();
	}

	public BigDecimal getWattsPhaseA() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getWattsphaseA();
	}

	public BigDecimal getWattsPhaseB() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getWattsphaseB();
	}

	public BigDecimal getWattsPhaseC() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getWattsphaseC();
	}

	public int getWattsSf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getW_SF();
	}

	public BigDecimal getVAPhaseA() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVAphaseA();
	}

	public BigDecimal getVAPhaseB() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVAphaseB();
	}

	public BigDecimal getVAPhaseC() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVAphaseC();
	}

	public int getVASf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVA_SF();
	}

	public BigDecimal getVARPhaseA() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVARphaseA();
	}

	public BigDecimal getVARPhaseB() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVARphaseB();
	}

	public BigDecimal getVARPhaseC() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVARphaseC();
	}

	public int getVARSf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getVAR_SF();
	}

	public BigDecimal getPF() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPF();
	}

	public BigDecimal getPFphA() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPFphaseA();
	}

	public BigDecimal getPFphB() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPFphaseB();
	}

	public BigDecimal getPFphC() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPFphaseC();
	}

	public int getPfSf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getPF_SF();
	}

	public BigDecimal getTotalWhExported() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getTotalWatthoursExported();
	}

	public BigDecimal getTotalWhImported() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getTotalWatthoursImported();
	}

	public int getTotalWhSf() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getTotWh_SF();
	}

	public Events getEvents() throws ModbusException {
		return cDeltaConnectThreePhaseabcMeterBlockMaster.getEvents();
	}

	public final static void main(String[] args) {

		try {
			Modbus204 modBusTest = new Modbus204(PowinProperty.TURTLEHOST.toString(), 4502, 255, true);
			modBusTest.init();
			// Tests
			LOG.info("getCurrentPhaseA()={}", modBusTest.getCurrentPhaseA());
			LOG.info("getCurrentPhaseB()={}", modBusTest.getCurrentPhaseB());
			LOG.info("getCurrentPhaseC()={}", modBusTest.getCurrentPhaseC());
			LOG.info("getHz()={}", modBusTest.getHz());
			LOG.info("getPF()={}", modBusTest.getPF());
			LOG.info("getPFphA()={}", modBusTest.getPFphA());
			LOG.info("getPFphB()={}", modBusTest.getPFphB());
			LOG.info("getPFphC()={}", modBusTest.getPFphC());
			LOG.info("getPhaseVoltageAB()={}", modBusTest.getPhaseVoltageAB());
			LOG.info("getPhaseVoltageAN()={}", modBusTest.getPhaseVoltageAN());
			LOG.info("getPhaseVoltageBC()={}", modBusTest.getPhaseVoltageBC());
			LOG.info("getPhaseVoltageBN()={}", modBusTest.getPhaseVoltageBN());
			LOG.info("getPhaseVoltageCA()={}", modBusTest.getPhaseVoltageCA());
			LOG.info("getPhaseVoltageCN()={}", modBusTest.getPhaseVoltageCN());
			LOG.info("getPhaseVoltageLL()={}", modBusTest.getPhaseVoltageLL());
			LOG.info("getPhaseVoltageLN()={}", modBusTest.getPhaseVoltageLN());
			LOG.info("getTotalAcCurrent()={}", modBusTest.getTotalAcCurrent());
			LOG.info("getTotalWhExported()={}", modBusTest.getTotalWhExported());
			LOG.info("getTotalWhImported()={}", modBusTest.getTotalWhImported());
			LOG.info("getVAPhaseA()={}", modBusTest.getVAPhaseA());
			LOG.info("getVAPhaseB()={}", modBusTest.getVAPhaseB());
			LOG.info("getVAPhaseC()={}", modBusTest.getVAPhaseC());
			LOG.info("getVARPhaseA()={}", modBusTest.getVARPhaseA());
			LOG.info("getVARPhaseB()={}", modBusTest.getVARPhaseB());
			LOG.info("getVARPhaseC()={}", modBusTest.getVARPhaseC());
			LOG.info("getWattsPhaseA()={}", modBusTest.getWattsPhaseA());
			LOG.info("getWattsPhaseB()={}", modBusTest.getWattsPhaseB());
			LOG.info("getWattsPhaseC()={}", modBusTest.getWattsPhaseC());
			LOG.info("getEvents()={}", modBusTest.getEvents());
			LOG.info("getCurrentSf()={}", modBusTest.getCurrentSf());
			LOG.info("getHzSf()={}", modBusTest.getHzSf());
			LOG.info("getID()={}", modBusTest.getID());
			LOG.info("getPfSf()={}", modBusTest.getPfSf());
			LOG.info("getTotalWhSf()={}", modBusTest.getTotalWhSf());
			LOG.info("getVARSf()={}", modBusTest.getVARSf());
			LOG.info("getVASf()={}", modBusTest.getVASf());
			LOG.info("getVoltageSf()={}", modBusTest.getVoltageSf());
			LOG.info("getWatts()={}", modBusTest.getWatts());
			LOG.info("getWattsSf()={}", modBusTest.getWattsSf());

		} catch (

		Exception e) {
			LOG.error("Exception in go.", e);
		}
	}
}
