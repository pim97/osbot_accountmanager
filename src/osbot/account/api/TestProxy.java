package osbot.account.api;

import osbot.account.global.Config;

public class TestProxy {

	public static void main(String[] args) {
		Config.DATABASE_NAME = args[2];
		Config.DATABASE_USER_NAME = args[3];

		if (args[4].equalsIgnoreCase("null")) {
			Config.DATABASE_PASSWORD = "";
		} else {
			Config.DATABASE_PASSWORD = args[4];
		}

		Config.DATABASE_IP = args[5];
		Config.MACHINE_ID = Integer.parseInt(args[6]);

		if (Config.MACHINE_ID == -1) {
			System.out.println("Must have set a machine id!");
			System.exit(1);
		}

		System.out.println("DATABASE SETTIGNS: ");
		System.out.println("Database username: " + Config.DATABASE_USER_NAME);
		System.out.println("Database name: " + Config.DATABASE_NAME);
		System.out.println("Database password: " + Config.DATABASE_PASSWORD);
		System.out.println("Database IP: " + Config.DATABASE_IP);
		System.out.println("Machine id: " + Config.MACHINE_ID);

		// TODO Auto-generated method stub
		Proxy6 p = Proxy6.getSingleton();
		p.resetDescription();
		// p.loop();
	}

}
