
package com.powin.modbusfiles.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationType {
	private String notificationCategory;
	private String notificationId;

	public void setNotificationCategory(String notificationCategory) {
		this.notificationCategory = notificationCategory;
	}

	public String getNotificationCategory() {
		return notificationCategory;
	}

	@Override
	public String toString() {
		return "NotificationType [notificationCategory=" + notificationCategory + ", notificationId=" + notificationId
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((notificationCategory == null) ? 0 : notificationCategory.hashCode());
		result = prime * result + ((notificationId == null) ? 0 : notificationId.hashCode());
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
		NotificationType other = (NotificationType) obj;
		if (notificationCategory == null) {
			if (other.notificationCategory != null)
				return false;
		} else if (!notificationCategory.equals(other.notificationCategory))
			return false;
		if (notificationId == null) {
			if (other.notificationId != null)
				return false;
		} else if (!notificationId.equals(other.notificationId))
			return false;
		return true;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public String getNotificationId() {
		return notificationId;
	}

}