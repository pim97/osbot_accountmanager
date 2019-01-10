package osbot.settings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;

import osbot.account.global.Config;
import osbot.account.handler.BotHandler;
import osbot.bot.BotController;
import osbot.tables.AccountTable;

public class OsbotController {

	private AccountTable account;

	public OsbotController(int id, AccountTable account) {
		setId(id);
		setAccount(account);
	}

	private int id, pidId = -1;

	private long startTime = -1;

	private boolean startingUp = false;

	private ProcessExecutor zz = null;

	public static synchronized long getPidOfProcess(Process p) {
		long pid = -1;

		try {
			if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getLong(p);
				f.setAccessible(false);
			}
		} catch (Exception e) {
			pid = -1;
		}
		return pid;
	}

	/**
	 * @param isMule
	 *            TODO
	 * 
	 */
	public void runBot(boolean isMule) {

//		new Thread(() -> {

			try {
				List<Integer> pids = BotController.getJavaPIDsWindows();
				Process p = Runtime.getRuntime().exec(getCliArgs().toString());
				System.out.println("Waiting for OSBot to launch..");

				// System.out.println("6");
				if (!p.waitFor(10, TimeUnit.SECONDS)) {
					System.out.println("Destroyed, couldn't start up in time");
					p.destroy();
					setStartingUp(false);
					return;
				}

				System.out.println(getCliArgs().toString());
				List<Integer> pidsAfter = BotController.getJavaPIDsWindows();
				pidsAfter.removeAll(pids);

				if (!Config.TESTING) {
					if (pidsAfter.size() == 1) {
						setPidId(pidsAfter.get(0));
						System.out.println("Pid set to: " + pidsAfter.get(0));
					} else {
						p.destroy();
						System.out.println("Destroyed, couldn't set pid, too many");
						setStartingUp(false);
						return;
					}
				}
				setCliArgs(new StringBuilder());

				if (isMule) {
					OsbotController partner = BotHandler.getMulePartner(this);
					if (partner != null && partner.getPidId() > 0
							&& BotHandler.isProcessIdRunningOnWindows(partner.getPidId())) {
						System.out.println("Both mules are running, others may start again!");
					}
				}
				setStartingUp(false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//		}).start();

		// try {
		// List<Integer> pids = BotController.getJavaPIDsWindows();
		//
		// ArrayList<String> args = new ArrayList<String>();
		// args.add("java");
		// args.add("-cp");
		// args.add("lib/*");
		// args.add("org.osbot.Boot");
		//
		// String[] arg = getCliArgs().toString().split(" ");
		// for (String abc : arg) {
		// args.add(abc);
		// }
		//
		//// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//// String logFileOutput = ("logs/" + getAccount().getUsername() + "/output_" +
		// getAccount().getUsername() + "_"
		//// + getId() + "_" + timestamp + ".txt").replace(" ", "_").replaceAll(":",
		// "_");
		//// String logFileOutputError = ("logs/" + getAccount().getUsername() +
		// "/error_" + getAccount().getUsername()
		//// + "_" + getId() + "_" + timestamp + ".txt").replace(" ",
		// "_").replaceAll(":", "_");
		////
		//// File f1 = new File("logs/" + getAccount().getUsername());
		//// f1.mkdir();
		////
		//// File logFileOutputFile = new File(logFileOutput);
		//// if (!logFileOutputFile.exists()) {
		//// logFileOutputFile.createNewFile();
		//// }
		//// File logFileOutputFileError = new File(logFileOutputError);
		//// if (!logFileOutputFileError.exists()) {
		//// logFileOutputFileError.createNewFile();
		//// }
		//
		// // java -cp \"lib/*\" org.osbot.Boot
		// ProcessBuilder processBuilder = new ProcessBuilder(args);
		//// .redirectError(new File(logFileOutputError))
		//// .redirectOutput(new File(logFileOutput));
		// Process p = processBuilder.start();
		//
		// // Process p = Runtime.getRuntime().exec(getCliArgs().toString());
		// System.out.println("Waiting for OSBot to launch..");
		//
		// if (!p.waitFor(5, TimeUnit.SECONDS)) {
		// System.out.println("Destroyed, couldn't start up in time");
		// p.destroy();
		// }
		//
		//// try {
		//// System.out.println(FileUtils.readFileToString(new File("output")));
		//// System.out.println(FileUtils.readFileToString(new File("error")));
		//// } catch (IOException e) {
		//// // TODO Auto-generated catch block
		//// e.printStackTrace();
		//// }
		//
		// System.out.println(getCliArgs().toString());
		// List<Integer> pidsAfter = BotController.getJavaPIDsWindows();
		// pidsAfter.removeAll(pids);
		//
		// if (pidsAfter.size() == 1) {
		// setPidId(pidsAfter.get(0));
		// System.out.println("Pid set to: " + pidsAfter.get(0));
		// } else {
		// p.destroy();
		// System.out.println("Destroyed, couldn't set pid, too many");
		// }
		// setCliArgs(new StringBuilder());
		//
		// if (isMule) {
		// OsbotController partner = BotHandler.getMulePartner(this);
		// if (partner != null && partner.getPidId() > 0
		// && BotHandler.isProcessIdRunningOnWindows(partner.getPidId())) {
		// System.out.println("Both mules are running, others may start again!");
		// }
		// }
		// setStartingUp(false);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * 
	 * @param args
	 * @param value
	 */
	public void addArguments(CliArgs args, boolean addDoublePoint, Object... value) {
		if (getCliArgs().length() == 0) {
			// getCliArgs().append("java -cp \"lib/*\" org.osbot.Boot -debug");
			getCliArgs().append("java -XX:ErrorFile=nul -cp \"lib/*\" org.osbot.Boot");
		}
		getCliArgs().append(" ");
		getCliArgs().append("-" + args.name().toLowerCase());
		getCliArgs().append(" ");

		if (value.length == 1) {
			getCliArgs().append(value[0]);
		} else {
			if (addDoublePoint) {
				for (int i = 0; i < value.length; i++) {
					getCliArgs().append(value[i] + ((value.length - 1) != i ? ":" : ""));
				}
			} else {
				for (int i = 0; i < value.length; i++) {
					getCliArgs().append(value[i] + ((value.length - 1) != i ? " " : ""));
				}
			}
		}
	}

	/**
	 * 
	 */
	private StringBuilder cliArgs = new StringBuilder();

	/**
	 * @return the cliArgs
	 */
	public StringBuilder getCliArgs() {
		return cliArgs;
	}

	/**
	 * @param cliArgs
	 *            the cliArgs to set
	 */
	public void setCliArgs(StringBuilder cliArgs) {
		this.cliArgs = cliArgs;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the pidId
	 */
	public int getPidId() {
		return pidId;
	}

	/**
	 * @param pidId
	 *            the pidId to set
	 */
	public void setPidId(int pidId) {
		this.pidId = pidId;
	}

	/**
	 * @return the account
	 */
	public AccountTable getAccount() {
		return account;
	}

	/**
	 * @param account
	 *            the account to set
	 */
	public void setAccount(AccountTable account) {
		this.account = account;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the startingUp
	 */
	public boolean isStartingUp() {
		return startingUp;
	}

	/**
	 * @param startingUp
	 *            the startingUp to set
	 */
	public void setStartingUp(boolean startingUp) {
		this.startingUp = startingUp;
	}

}
