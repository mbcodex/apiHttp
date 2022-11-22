package com.powin.modbusfiles.configuration;

import java.util.ArrayList;
import java.util.List;

public class StringChildNode {
	private List<Integer> stringIndexes;

	private String dcTopologyNodeType;

	private String dcTopologyNodeName;

	public void setStringIndexes(List<Integer> stringIndexes) {
		this.stringIndexes = stringIndexes;
	}

	public List<Integer> getStringIndexes() {
		return this.stringIndexes;
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

	public void init(int stringCount) {
		setDcTopologyNodeType("StackList");
		setDcTopologyNodeName("Stack1-" + stringCount);
		List<Integer> lst = new ArrayList<Integer>();
		for (int i = 0; i < stringCount; i++) {
			lst.add(i + 1);
		}
		setStringIndexes(lst);
	}
}
