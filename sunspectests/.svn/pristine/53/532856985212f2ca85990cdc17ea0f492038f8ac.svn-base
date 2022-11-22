package com.powin.modbusfiles.configuration;

import java.util.ArrayList;
import java.util.List;

public class DcTopology {
	private List<ArrayChildNode> childNodes;

	private String dcTopologyNodeType;

	private String dcTopologyNodeName;

	private boolean hasCurrentLimit;

	private int chargeCurrentLimitAmps;

	private int dischargeCurrentLimitAmps;

	public void setChildNodes(List<ArrayChildNode> childNodes) {
		this.childNodes = childNodes;
	}

	public List<ArrayChildNode> getChildNodes() {
		return this.childNodes;
	}

	public void setDcTopologyNodeType(String dcTopologyNodeType) {
		this.dcTopologyNodeType = dcTopologyNodeType;
	}

	public String getDcTopologyNodeType() {
		return this.dcTopologyNodeType;
	}

	public void setDcTopologyNodeName(String dcTopologyNodeName) {
		this.dcTopologyNodeName = dcTopologyNodeName;
	}

	public String getDcTopologyNodeName() {
		return this.dcTopologyNodeName;
	}

	public void setHasCurrentLimit(boolean hasCurrentLimit) {
		this.hasCurrentLimit = hasCurrentLimit;
	}

	public boolean getHasCurrentLimit() {
		return this.hasCurrentLimit;
	}

	public void setChargeCurrentLimitAmps(int chargeCurrentLimitAmps) {
		this.chargeCurrentLimitAmps = chargeCurrentLimitAmps;
	}

	public int getChargeCurrentLimitAmps() {
		return this.chargeCurrentLimitAmps;
	}

	public void setDischargeCurrentLimitAmps(int dischargeCurrentLimitAmps) {
		this.dischargeCurrentLimitAmps = dischargeCurrentLimitAmps;
	}

	public int getDischargeCurrentLimitAmps() {
		return this.dischargeCurrentLimitAmps;
	}

	public void init(int stringCount, int containerIndex) {
		List<ArrayChildNode> lst = new ArrayList<ArrayChildNode>();
		ArrayChildNode arrNode = new ArrayChildNode();
		arrNode.init(stringCount, containerIndex);
		lst.add(arrNode);
		setChildNodes(lst);
		setDcTopologyNodeType("PowerElectronics");
		setDcTopologyNodeName("PCS");
		setHasCurrentLimit(true);
		setChargeCurrentLimitAmps(99999);
		setDischargeCurrentLimitAmps(99999);
	}
}
