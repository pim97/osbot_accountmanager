package osbot.settings;

import java.io.IOException;
import java.util.List;

import osbot.bot.BotController;
import osbot.tables.AccountTable;

public class OsbotController {

	private AccountTable account;

	public OsbotController(int id, AccountTable account) {
		setId(id);
		setAccount(account);

	}

	private int id, pidId;


	/**
	 * 
	 */
	public void runBot() {
		new Thread(() -> {
			try {
				List<Integer> pids = BotController.getJavaPIDsWindows();
				Process p = Runtime.getRuntime().exec(getCliArgs().toString());
				System.out.println("Waiting for OSBot to launch..");
				p.waitFor();
				System.out.println(getCliArgs().toString());
				List<Integer> pidsAfter = BotController.getJavaPIDsWindows();
				pidsAfter.removeAll(pids);

				if (pidsAfter.size() == 1) {
					setPidId(pidsAfter.get(0));
					System.out.println("Pid set to: " + pidsAfter.get(0));
				}
				setCliArgs(new StringBuilder());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

	}

	/**
	 * 
	 * @param args
	 * @param value
	 */
	public void addArguments(CliArgs args, boolean addDoublePoint, Object... value) {
		if (getCliArgs().length() == 0) {
			getCliArgs().append("java -jar osbot.jar");
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

}
