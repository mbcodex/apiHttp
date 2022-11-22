package com.powin.modbusfiles.cycling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.cycling.AppController.AppControl;

/**
 * Cycle requested by Blake. Charge to 100% SoC using BasicOps. Follow the
 * cycle.
 *
 * @author powin
 *
 */
public class MidCycleCycler extends SyntheticCycleCycler {
	private final static Logger LOG = LogManager.getLogger();

	@Override
	public void cycle() {
		movePower(100, 61);
		movePower(-100, 15);
		movePower(100, 30);
		movePower(-100, 15);
		rest(29);
		movePower(100, 90);
	}

	@Override
	public void run(boolean startWithCharge) {
		AppController.controlApps(AppControl.START_SUNSPEC);
		chargeToTop(-100);
		cycle();
		AppController.controlApps(AppControl.STOP_SUNSPEC);
	}

	public static void main(String[] args) {
//		MidCycleCycler mMidCycleCycler = new MidCycleCycler();
//		mMidCycleCycler.getReporter().createNewReportFile("MidCycleCyler");
//		mMidCycleCycler.run(mMidCycleCycler.getOnlineSoC() < 100);
	}
}
