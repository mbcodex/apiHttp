package com.powin.modbusfiles.db;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseConnections {

	private static CoblynauDatamanager dataManager;

	private DatabaseConnections() {

	}

	public static CoblynauDatamanager getKoboldConnection() {
		if (dataManager == null) {
			ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/sunspectests-main.xml");
			dataManager = (CoblynauDatamanager) ctx.getBean("coblynauDatamanager");
		}
		return dataManager;
	}
}
