package com.powin.modbusfiles.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BatteryPackBalancingConfiguration {
	private String balancingMode;

	private int providedVoltageTarget;

	private boolean chargeBalancingPermitted;

	private boolean dischargeBalancingPermitted;

	private int chargeDeadband;

	private int dischargeDeadband;

	public void setBalancingMode(String balancingMode) {
		this.balancingMode = balancingMode;
	}

	public String getBalancingMode() {
		return this.balancingMode;
	}

	public void setProvidedVoltageTarget(int providedVoltageTarget) {
		this.providedVoltageTarget = providedVoltageTarget;
	}

	public int getProvidedVoltageTarget() {
		return this.providedVoltageTarget;
	}

	public void setChargeBalancingPermitted(boolean chargeBalancingPermitted) {
		this.chargeBalancingPermitted = chargeBalancingPermitted;
	}

	public boolean getChargeBalancingPermitted() {
		return this.chargeBalancingPermitted;
	}

	public void setDischargeBalancingPermitted(boolean dischargeBalancingPermitted) {
		this.dischargeBalancingPermitted = dischargeBalancingPermitted;
	}

	public boolean getDischargeBalancingPermitted() {
		return this.dischargeBalancingPermitted;
	}

	public void setChargeDeadband(int chargeDeadband) {
		this.chargeDeadband = chargeDeadband;
	}

	public int getChargeDeadband() {
		return this.chargeDeadband;
	}

	public void setDischargeDeadband(int dischargeDeadband) {
		this.dischargeDeadband = dischargeDeadband;
	}

	public int getDischargeDeadband() {
		return this.dischargeDeadband;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((balancingMode == null) ? 0 : balancingMode.hashCode());
		result = prime * result + (chargeBalancingPermitted ? 1231 : 1237);
		result = prime * result + chargeDeadband;
		result = prime * result + (dischargeBalancingPermitted ? 1231 : 1237);
		result = prime * result + dischargeDeadband;
		result = prime * result + providedVoltageTarget;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BatteryPackBalancingConfiguration other = (BatteryPackBalancingConfiguration) obj;
		if (balancingMode == null) {
			if (other.balancingMode != null)
				return false;
		} else if (!balancingMode.equals(other.balancingMode))
			return false;
		if (chargeBalancingPermitted != other.chargeBalancingPermitted)
			return false;
		if (chargeDeadband != other.chargeDeadband)
			return false;
		if (dischargeBalancingPermitted != other.dischargeBalancingPermitted)
			return false;
		if (dischargeDeadband != other.dischargeDeadband)
			return false;
		if (providedVoltageTarget != other.providedVoltageTarget)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BatteryPackBalancingConfiguration [balancingMode=" + balancingMode + ", providedVoltageTarget="
				+ providedVoltageTarget + ", chargeBalancingPermitted=" + chargeBalancingPermitted
				+ ", dischargeBalancingPermitted=" + dischargeBalancingPermitted + ", chargeDeadband=" + chargeDeadband
				+ ", dischargeDeadband=" + dischargeDeadband + "]";
	}
}
