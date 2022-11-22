package com.powin.modbusfiles.cycling;

import com.powin.modbusfiles.modbus.Modbus103;
import com.powin.modbusfiles.modbus.Modbus802;
import com.powin.modbusfiles.modbus.ModbusPowinBlock;
import com.powin.modbusfiles.utilities.PowinProperty;

public class CyclingTestBase {
	// Modbus parameters
	protected static String cModbusHostName;
	protected static int cModbusPort;
	protected static int cModbusUnitId;
	protected static boolean cEnableModbusLogging;
	// Devices
	protected static Modbus103 cInverterThreePhaseBlockMaster;
	protected static Modbus802 cBatteryBaseModelBlockMaster;
	protected static ModbusPowinBlock cModbusPowinBlock;

	protected static int chargingPowerPercent;
	protected static int dischargingPowerPercent;
	protected static int restDurationSeconds;
	protected static int logIntervalSeconds = 2;
	protected static int targetChargeVoltage;
	protected static int targetDischargeVoltage;
	protected static int maxCycles;

	protected void initParameters(String[] args) {
		int i = 0;
		chargingPowerPercent = args.length > i ? Integer.parseInt(args[i++])
				: PowinProperty.BC_CHARGINGPOWERW.intValue();
		dischargingPowerPercent = args.length > i ? Integer.parseInt(args[i++])
				: PowinProperty.BC_DISCHARGINGPOWERW.intValue();
		restDurationSeconds = args.length > i ? Integer.parseInt(args[i++])
				: PowinProperty.BC_RESTPERIODSECONDS.intValue();
		targetChargeVoltage = args.length > i ? Integer.parseInt(args[i++])
				: PowinProperty.BC_TARGETCHARGEVOLTAGE.intValue();
		targetDischargeVoltage = args.length > i ? Integer.parseInt(args[i++])
				: PowinProperty.BC_TARGETDISCHARGEVOLTAGE.intValue();
		maxCycles = args.length > i ? Integer.parseInt(args[i++]) : PowinProperty.BC_MAXCYCLES.intValue();
		logIntervalSeconds = args.length > i ? Integer.parseInt(args[i++]) : PowinProperty.BC_LOGINTERVAL.intValue();
	}

//	public static void resetDevices() {
//		cInverterThreePhaseBlockMaster = Modbus103.Instance;
//		cBatteryBaseModelBlockMaster = Modbus802.Instance;
//	}

}
