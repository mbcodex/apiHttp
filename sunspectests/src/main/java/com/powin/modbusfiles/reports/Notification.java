package com.powin.modbusfiles.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification {

	private String triggerMessage;
	private NotificationSource notificationSource;
	private NotificationType notificationType;
	private String timestamp;

	public void setTriggerMessage(String triggerMessage) {
		this.triggerMessage = triggerMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((notificationSource == null) ? 0 : notificationSource.hashCode());
		result = prime * result + ((notificationType == null) ? 0 : notificationType.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((triggerMessage == null) ? 0 : triggerMessage.hashCode());
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
		Notification other = (Notification) obj;
		if (notificationSource == null) {
			if (other.notificationSource != null)
				return false;
		} else if (!notificationSource.equals(other.notificationSource))
			return false;
		if (notificationType == null) {
			if (other.notificationType != null)
				return false;
		} else if (!notificationType.equals(other.notificationType))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (triggerMessage == null) {
			if (other.triggerMessage != null)
				return false;
		} else if (!triggerMessage.equals(other.triggerMessage))
			return false;
		return true;
	}

	public String getTriggerMessage() {
		return triggerMessage;
	}

	public void setNotificationSource(NotificationSource notificationSource) {
		this.notificationSource = notificationSource;
	}

	public NotificationSource getNotificationSource() {
		return notificationSource;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Notification [triggerMessage=" + triggerMessage + ", notificationSource=" + notificationSource
				+ ", notificationType=" + notificationType + ", timestamp=" + timestamp + "]";
	}

}