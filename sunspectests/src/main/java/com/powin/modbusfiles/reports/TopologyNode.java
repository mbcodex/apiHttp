package com.powin.modbusfiles.reports;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopologyNode {

	private String entityKey;
	private boolean enabled;
	private boolean ready;
	private String entityType;
	private String entitySubType;
	private boolean communicating;
	private String statusMessage;
	private boolean allowFaultReset;
	private List<String> childEntityKeys;

	public void setEntityKey(String entityKey) {
		this.entityKey = entityKey;
	}

	public String getEntityKey() {
		return this.entityKey;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean getReady() {
		return this.ready;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getEntityType() {
		return this.entityType;
	}

	public void setEntitySubType(String entitySubType) {
		this.entitySubType = entitySubType;
	}

	public String getEntitySubType() {
		return this.entitySubType;
	}

	public void setCommunicating(boolean communicating) {
		this.communicating = communicating;
	}

	public boolean getCommunicating() {
		return this.communicating;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getStatusMessage() {
		return this.statusMessage;
	}

	public void setAllowFaultReset(boolean allowFaultReset) {
		this.allowFaultReset = allowFaultReset;
	}

	public boolean getAllowFaultReset() {
		return this.allowFaultReset;
	}

	public void setChildEntityKeys(List<String> childEntityKeys) {
		this.childEntityKeys = childEntityKeys;
	}

	public List<String> getChildEntityKeys() {
		return this.childEntityKeys;
	}

	@Override
	public String toString() {
		return "TopologyNode [getEntityKey()=" + getEntityKey() + ", getEnabled()=" + getEnabled() + ", getReady()="
				+ getReady() + ", getEntityType()=" + getEntityType() + ", getEntitySubType()=" + getEntitySubType()
				+ ", getCommunicating()=" + getCommunicating() + ", getStatusMessage()=" + getStatusMessage()
				+ ", getAllowFaultReset()=" + getAllowFaultReset() + ", getChildEntityKeys()=" + getChildEntityKeys()
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (allowFaultReset ? 1231 : 1237);
		result = prime * result + ((childEntityKeys == null) ? 0 : childEntityKeys.hashCode());
		result = prime * result + (communicating ? 1231 : 1237);
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((entityKey == null) ? 0 : entityKey.hashCode());
		result = prime * result + ((entitySubType == null) ? 0 : entitySubType.hashCode());
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + (ready ? 1231 : 1237);
		result = prime * result + ((statusMessage == null) ? 0 : statusMessage.hashCode());
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
		TopologyNode other = (TopologyNode) obj;
		if (allowFaultReset != other.allowFaultReset)
			return false;
		if (childEntityKeys == null) {
			if (other.childEntityKeys != null)
				return false;
		} else if (!childEntityKeys.equals(other.childEntityKeys))
			return false;
		if (communicating != other.communicating)
			return false;
		if (enabled != other.enabled)
			return false;
		if (entityKey == null) {
			if (other.entityKey != null)
				return false;
		} else if (!entityKey.equals(other.entityKey))
			return false;
		if (entitySubType == null) {
			if (other.entitySubType != null)
				return false;
		} else if (!entitySubType.equals(other.entitySubType))
			return false;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		if (ready != other.ready)
			return false;
		if (statusMessage == null) {
			if (other.statusMessage != null)
				return false;
		} else if (!statusMessage.equals(other.statusMessage))
			return false;
		return true;
	}
}
