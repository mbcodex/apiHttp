package com.powin.modbusfiles.modbus;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbus.ModbusException;
import com.powin.modbus.sunspec.SunspecMasterEndpoint;
import com.powin.modbus.sunspec.SunspecTcpMasterEndpointFactory;
import com.powin.modbus.sunspec.blocks.InverterThreePhaseBlockMaster;
import com.powin.modbus.sunspec.blocks.WyeConnectThreePhaseabcnMeterBlockCommon.Events;
import com.powin.modbus.sunspec.blocks.WyeConnectThreePhaseabcnMeterBlockMaster;
import com.powin.modbusfiles.utilities.PowinProperty;;

public class Modbus203 {
	private final static Logger LOG = LogManager.getLogger();

	private final String cModbusHostName;
	private final int cModbusPort;
	private final int cModbusUnitId;
	private final boolean cEnableModbusLogging;

	private SunspecMasterEndpoint cSunspecMasterEndpoint;
	private WyeConnectThreePhaseabcnMeterBlockMaster cWyeConnectThreePhaseabcnMeterBlockMaster;
	private InverterThreePhaseBlockMaster cInverterThreePhaseBlockMaster;

	public Modbus203(String modbusHostName, int modbusPort, int modbusUnitId, boolean enableModbusLogging) {
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

		cInverterThreePhaseBlockMaster = cSunspecMasterEndpoint.getBlock(103, 0);
		if (cInverterThreePhaseBlockMaster == null) {
			throw new IllegalStateException("No inverter103!");
		}
		cWyeConnectThreePhaseabcnMeterBlockMaster = cSunspecMasterEndpoint.getBlock(203, 0);
		if (cWyeConnectThreePhaseabcnMeterBlockMaster == null) {
			throw new IllegalStateException("No Wye Meter!");
		}
	}

	public int get103Data() throws ModbusException {
		return cInverterThreePhaseBlockMaster.getWatts().intValue();
	}

	// Getters
	public int getID() {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getId();
	}

	public BigDecimal getTotalAcCurrent() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getAmps();
	}

	public BigDecimal getCurrentPhaseA() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getAmpsPhaseA();
	}

	public BigDecimal getCurrentPhaseB() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getAmpsPhaseB();
	}

	public BigDecimal getCurrentPhaseC() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getAmpsPhaseC();
	}

	public int getCurrentSf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getA_SF();
	}

	public BigDecimal getPhaseVoltageLN() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVoltageLN();
	}

	public BigDecimal getPhaseVoltageLL() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVoltageLL();
	}

	public BigDecimal getPhaseVoltageAB() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPhVphAB();
	}

	public BigDecimal getPhaseVoltageBC() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPhVphBC();
	}

	public BigDecimal getPhaseVoltageCA() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPhVphCA();
	}

	public BigDecimal getPhaseVoltageAN() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPhaseVoltageAN();
	}

	public BigDecimal getPhaseVoltageBN() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPhaseVoltageBN();
	}

	public BigDecimal getPhaseVoltageCN() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPhaseVoltageCN();
	}

	public int getVoltageSf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getV_SF();
	}

	public BigDecimal getHz() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getHz();
	}

	public int getHzSf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getHz_SF();
	}

	public int getWatts() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getWatts().intValue();
	}

	public int getWattsInverter() throws ModbusException {
		return cInverterThreePhaseBlockMaster.getWatts().intValue();
	}

	public BigDecimal getWattsPhaseA() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getWattsphaseA();
	}

	public BigDecimal getWattsPhaseB() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getWattsphaseB();
	}

	public BigDecimal getWattsPhaseC() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getWattsphaseC();
	}

	public int getWattsSf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getW_SF();
	}

	public BigDecimal getVAPhaseA() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVAphaseA();
	}

	public BigDecimal getVAPhaseB() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVAphaseB();
	}

	public BigDecimal getVAPhaseC() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVAphaseC();
	}

	public int getVASf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVA_SF();
	}

	public BigDecimal getVARPhaseA() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVARphaseA();
	}

	public BigDecimal getVARPhaseB() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVARphaseB();
	}

	public BigDecimal getVARPhaseC() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVARphaseC();
	}

	public int getVARSf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getVAR_SF();
	}

	public BigDecimal getPF() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPF();
	}

	public BigDecimal getPFphA() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPFphaseA();
	}

	public BigDecimal getPFphB() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPFphaseB();
	}

	public BigDecimal getPFphC() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPFphaseC();
	}

	public int getPfSf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getPF_SF();
	}

	public BigDecimal getTotalWhExported() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getTotalWatthoursExported();
	}

	public BigDecimal getTotalWhImported() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getTotalWatthoursImported();
	}

	public int getTotalWhSf() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getTotWh_SF();
	}

	public Events getEvents() throws ModbusException {
		return cWyeConnectThreePhaseabcnMeterBlockMaster.getEvents();
	}

	public final static void main(String[] args) {

		try {
			Modbus203 modBusTest = new Modbus203(PowinProperty.TURTLEHOST.toString(), 4502, 255, false);
			modBusTest.init();
			// Tests
			// LOG.info("getCurrentPhaseA()={}", modBusTest.getCurrentPhaseA());
			// LOG.info("getCurrentPhaseB()={}", modBusTest.getCurrentPhaseB());
			// LOG.info("getCurrentPhaseC()={}", modBusTest.getCurrentPhaseC());
			// LOG.info("getHz()={}", modBusTest.getHz());
			// LOG.info("getPF()={}", modBusTest.getPF());
			// LOG.info("getPFphA()={}", modBusTest.getPFphA());
			// LOG.info("getPFphB()={}", modBusTest.getPFphB());
			// LOG.info("getPFphC()={}", modBusTest.getPFphC());
			// LOG.info("getPhaseVoltageAB()={}", modBusTest.getPhaseVoltageAB());
			// LOG.info("getPhaseVoltageAN()={}", modBusTest.getPhaseVoltageAN());
			// LOG.info("getPhaseVoltageBC()={}", modBusTest.getPhaseVoltageBC());
			// LOG.info("getPhaseVoltageBN()={}", modBusTest.getPhaseVoltageBN());
			// LOG.info("getPhaseVoltageCA()={}", modBusTest.getPhaseVoltageCA());
			// LOG.info("getPhaseVoltageCN()={}", modBusTest.getPhaseVoltageCN());
			// LOG.info("getPhaseVoltageLL()={}", modBusTest.getPhaseVoltageLL());
			// LOG.info("getPhaseVoltageLN()={}", modBusTest.getPhaseVoltageLN());
			// LOG.info("getTotalAcCurrent()={}", modBusTest.getTotalAcCurrent());
			// LOG.info("getTotalWhExported()={}", modBusTest.getTotalWhExported());
			// LOG.info("getTotalWhImported()={}", modBusTest.getTotalWhImported());
			// LOG.info("getVAPhaseA()={}", modBusTest.getVAPhaseA());
			// LOG.info("getVAPhaseB()={}", modBusTest.getVAPhaseB());
			// LOG.info("getVAPhaseC()={}", modBusTest.getVAPhaseC());
			// LOG.info("getVARPhaseA()={}", modBusTest.getVARPhaseA());
			// LOG.info("getVARPhaseB()={}", modBusTest.getVARPhaseB());
			// LOG.info("getVARPhaseC()={}", modBusTest.getVARPhaseC());
			// LOG.info("getWattsPhaseA()={}", modBusTest.getWattsPhaseA());
			// LOG.info("getWattsPhaseB()={}", modBusTest.getWattsPhaseB());
			// LOG.info("getWattsPhaseC()={}", modBusTest.getWattsPhaseC());
			// LOG.info("getEvents()={}", modBusTest.getEvents());
			// LOG.info("getCurrentSf()={}", modBusTest.getCurrentSf());
			// LOG.info("getHzSf()={}", modBusTest.getHzSf());
			// LOG.info("getID()={}", modBusTest.getID());
			// LOG.info("getPfSf()={}", modBusTest.getPfSf());
			// LOG.info("getTotalWhSf()={}", modBusTest.getTotalWhSf());
			// LOG.info("getVARSf()={}", modBusTest.getVARSf());
			// LOG.info("getVASf()={}", modBusTest.getVASf());
			// LOG.info("getVoltageSf()={}", modBusTest.getVoltageSf());
			LOG.info("getWatts()={}", modBusTest.getWatts());
			// LOG.info("getWattsSf()={}", modBusTest.getWattsSf());
			LOG.info("getWattsInverter()={}", modBusTest.getWattsInverter());

		} catch (

		Exception e) {
			LOG.error("Exception in go.", e);
		}
	}
}
