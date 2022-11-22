package com.powin.modbusfiles.configuration;

import java.util.List;

public class DeviceAcBatteryBlock {
	public DeviceAcBatteryBlock(List<Integer> arrayIndexes, boolean distributePowerEvenly, boolean enabled,
			int stopPriority, int startPriority) {
		super();
		this.arrayIndexes = arrayIndexes;
		this.distributePowerEvenly = distributePowerEvenly;
		this.enabled = enabled;
		this.stopPriority = stopPriority;
		this.startPriority = startPriority;
	}

	private List<Integer> arrayIndexes;
	boolean distributePowerEvenly;
	private boolean enabled;
	private int stopPriority;
	private int startPriority;

	public List<Integer> getArrayIndexes() {
		return arrayIndexes;
	}

	public void setArrayIndexes(List<Integer> arrayIndexes) {
		this.arrayIndexes = arrayIndexes;
	}

	public boolean isDistributePowerEvenly() {
		return distributePowerEvenly;
	}

	public void setDistributePowerEvenly(boolean distributePowerEvenly) {
		this.distributePowerEvenly = distributePowerEvenly;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getStopPriority() {
		return stopPriority;
	}

	public void setStopPriority(int stopPriority) {
		this.stopPriority = stopPriority;
	}

	public int getStartPriority() {
		return startPriority;
	}

	public void setStartPriority(int startPriority) {
		this.startPriority = startPriority;
	}
}