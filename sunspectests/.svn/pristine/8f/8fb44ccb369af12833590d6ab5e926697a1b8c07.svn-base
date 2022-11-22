package com.powin.modbusfiles.reports;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.powin.modbusfiles.stackoperations.RotationControl;
import com.powin.modbusfiles.utilities.CommonHelper;
import com.powin.modbusfiles.utilities.Constants;
import com.powin.modbusfiles.utilities.FileHelper;
import com.powin.modbusfiles.utilities.PowinProperty;
import com.powin.modbusfiles.utilities.TimeOut;

import ch.ethz.ssh2.SFTPv3DirectoryEntry;

public class GoblinResend {
	private final static Logger LOG = LogManager.getLogger();

	public GoblinResend() {
		FileHelper.loginRemoteMachine(PowinProperty.TURTLEHOST.toString(), PowinProperty.TURTLEUSER.toString(), PowinProperty.TURTLEPASSWORD.toString());
	}

	public boolean qaaut596_597_VerifyStandardReportIsSavedAndDeleted() {
		int amountOrigin = FileHelper.getFileAmount(PowinProperty.STANDARD_REPORT_PATH.toString());
		if (amountOrigin == Integer.MAX_VALUE)
			return false;
		else
			LOG.info("The original amount of files in {} is {}", PowinProperty.STANDARD_REPORT_PATH.toString(), amountOrigin);

		CommonHelper.stopLocalTomcat();
		String timer = TimeOut.create(150);
		int amountAfterTomcatStopped = 0;
		boolean reportSaved = false;
		while (!TimeOut.isExpired(timer)) {
			amountAfterTomcatStopped = FileHelper.getFileAmount(PowinProperty.STANDARD_REPORT_PATH.toString());
			if (amountAfterTomcatStopped > amountOrigin) {
				reportSaved = true;
				TimeOut.remove(timer);
				break;
			}
			CommonHelper.quietSleep(1000);
		}
		if (reportSaved == false) {
			LOG.error("Fail: No report file is saved in hard drive before timeout.");
			return false;
		}else {
			LOG.info("Pass: Report files are saved, the amount is {} now.", amountAfterTomcatStopped);
		}

		CommonHelper.restartLocalTomcat();
		timer = TimeOut.create(300);
		int amountAfterTomcatRestarted = 0;
		boolean reportDeleted = false;
		while (!TimeOut.isExpired(timer)) {
			amountAfterTomcatRestarted = FileHelper.getFileAmount(PowinProperty.STANDARD_REPORT_PATH.toString());
			if (amountAfterTomcatRestarted < amountAfterTomcatStopped) {
				reportDeleted = true;
				break;
			}
			CommonHelper.quietSleep(1000);
		}

		if (reportDeleted == false) {
			LOG.error("Fail: Report files are not deleted before timeout.");
			return false;
		} else {
			LOG.info("Pass: Report files are deleted, the amount is {} now.", amountAfterTomcatRestarted);
		}
		return true;
	}

	public boolean qaaut599_600_VerifyNotificationReportIsSavedAndResent() {
		RotationControl.moveOutOfRotation(PowinProperty.ARRAY_INDEX.intValue());
		if (RotationControl.verifyOutRotationStatus(60, PowinProperty.ARRAY_INDEX.intValue()) == false) {
			LOG.error("Failed to move stack out of rotation, test abort.");
			return false;
		}
		if (verifyStringOutOfRotationWarningStatus(60, true) == false) {
			return false;
		}
		LOG.info("Pass: Stacks are taken out of rotation.");
		int amountOrigin = FileHelper.getFileAmount(PowinProperty.NOTIFICATION_REPORT_PATH.toString());
		CommonHelper.stopLocalTomcat();
		try {
			CommonHelper.restartTurtleTomcat();
		} catch (Exception e) {
			LOG.error("", e);
			RotationControl.moveIntoRotation(PowinProperty.ARRAY_INDEX.intValue());
		}

		int amountAfterTurtleTomcatRestarted = 0;
		String timer = TimeOut.create(90);
		boolean notificationFileCreated = false;
		while (!TimeOut.isExpired(timer)) {
			amountAfterTurtleTomcatRestarted = FileHelper.getFileAmount(PowinProperty.NOTIFICATION_REPORT_PATH.toString());
			if (amountAfterTurtleTomcatRestarted > amountOrigin) {
				notificationFileCreated = true;
				TimeOut.remove(timer);
				break;
			}
			CommonHelper.quietSleep(1000);
		}

		if (notificationFileCreated == false) {
			LOG.error("Fail: No notification file is saved in hard drive before timeout.");
			RotationControl.moveIntoRotation(PowinProperty.ARRAY_INDEX.intValue());
			return false;
		}

		LOG.info("Pass: Notification file is saved in hard drive.");

		CommonHelper.restartLocalTomcat();

		if (verifyStringOutOfRotationWarningStatus(60, true) == false) {
			LOG.error("Fail: Notifications are not resent before timeout.");
			RotationControl.moveIntoRotation(PowinProperty.ARRAY_INDEX.intValue());
			return false;
		}

		boolean notificationFileDeleted = false;
		int amountAfterLocalTomcatRestarted = 0;
		timer = TimeOut.create(400);
		while (!TimeOut.isExpired(timer)) {
			amountAfterLocalTomcatRestarted = FileHelper.getFileAmount(PowinProperty.NOTIFICATION_REPORT_PATH.toString());
			if (amountAfterLocalTomcatRestarted < amountAfterTurtleTomcatRestarted) {
				notificationFileDeleted = true;
				break;
			}
			CommonHelper.quietSleep(Constants.TEN_SECONDS);
			LOG.info("Waiting for the notification files being deleted......");
		}

		if (notificationFileDeleted == false) {
			LOG.error("Fail: Notification files are not deleted before timeout.");
			RotationControl.moveIntoRotation(PowinProperty.ARRAY_INDEX.intValue());
			return false;
		} else {
			LOG.info("Pass: Notification files are deleted, the amount is {} now.", amountAfterLocalTomcatRestarted);
		}
		RotationControl.moveIntoRotation(PowinProperty.ARRAY_INDEX.intValue());
		return true;
	}

	// TODO: multiple returns
	public boolean qaaut_594_VerifyLishuReportIsSaved() {
		String remotePath = "/etc/powin/lishureports";
		List<SFTPv3DirectoryEntry> originList = FileHelper.getFileList(remotePath);
		if (originList == null) {
			LOG.error("Error: Failed to get file information from {}, test abort", remotePath);
			return false;
		}

		String timer = TimeOut.create(180);
		while (!TimeOut.isExpired(timer)) {
			List<SFTPv3DirectoryEntry> compareList = FileHelper.getFileList(remotePath);
			if (compareList != null) {
				if (compareList.size() > originList.size()) {
					LOG.info("PASS: New files are saved.");
					TimeOut.remove(timer);
					return true;
				} else {
					for (SFTPv3DirectoryEntry entry : compareList) {
						if (originList.stream().anyMatch(f -> f.filename.equals(entry.filename) && f.attributes.size < entry.attributes.size)) {
							LOG.info("PASS: The size of {} increased.", entry.filename);
							TimeOut.remove(timer);
							return true;
						}
					}
				}
			}
			CommonHelper.quietSleep(Constants.TEN_SECONDS);
		}
		LOG.error("FAIL: No records are saved before timeout.");
		return false;
	}

	public boolean qaaut_598_VerifyStandardReportIsSentAfterReconnection() {
		int amountOrigin = FileHelper.getFileAmount(PowinProperty.STANDARD_REPORT_PATH.toString());
		if (amountOrigin == Integer.MAX_VALUE)
			return false;
		else
			LOG.info("The original amount of files in {} is {}", PowinProperty.STANDARD_REPORT_PATH.toString(), amountOrigin);

		CommonHelper.stopLocalTomcat();
		String timer = TimeOut.create(150);
		int amountAfterTomcatStopped = 0;
		boolean reportSaved = false;
		while (!TimeOut.isExpired(timer)) {
			amountAfterTomcatStopped = FileHelper.getFileAmount(PowinProperty.STANDARD_REPORT_PATH.toString());
			if (amountAfterTomcatStopped > amountOrigin && amountAfterTomcatStopped > 10) {
				reportSaved = true;
				TimeOut.remove(timer);
				break;
			}
			CommonHelper.quietSleep(1000);
		}
		if (reportSaved == false) {
			LOG.error("Fail: No report file is saved in hard drive before timeout.");
			return false;
		} else {
			LOG.info("Pass: Report files are saved, the amount is {} now.", amountAfterTomcatStopped);
		}

		List<SFTPv3DirectoryEntry> fileList = FileHelper.getFileList(PowinProperty.STANDARD_REPORT_PATH.toString());

		CommonHelper.restartLocalTomcat();
		timer = TimeOut.create(300);
		// int amountAfterTomcatRestarted = 0;
		boolean reportSent = false;
		while (!TimeOut.isExpired(timer)) {
			if (fileList.stream().anyMatch(file -> file.filename.contains(Lastcall.getRequestTimeStamp()))) {
				reportSent = true;
				TimeOut.remove(timer);
				break;
			}
			CommonHelper.quietSleep(1000);
		}

		if (reportSent == false) {
			LOG.error("Fail: Reports are not sent before timeout.");
			return false;
		} else {
			LOG.info("Pass: Reports are sent through lastcall after re-connection to coblynau.");
		}
		return true;
	}

	//TODO multiple returns
	private boolean verifyStringOutOfRotationWarningStatus(int seconds, boolean isWarningExpected) {
		String timer = TimeOut.create(seconds);
		while (!TimeOut.isExpired(timer)) {
			List<Notification> notifications = Lastcall.getNotificationList();

			if (isWarningExpected) {
				if (notifications.stream().anyMatch(notification -> notification.getNotificationType().getNotificationId().equals("2561"))) {
					TimeOut.remove(timer);
					return true;
				}
			} else {
				if (notifications.stream().anyMatch(notification -> notification.getNotificationType().getNotificationId().equals("2561")) == false) {
					TimeOut.remove(timer);
					return true;
				}
			}
			CommonHelper.quietSleep(1000);
		}
		if (isWarningExpected)
			LOG.error("Error: Failed to get StringOutOfRotationWarning.");
		else
			LOG.error("Error: Failed to clear all StringOutOfRotationWarning.");
		return false;
	}
}
