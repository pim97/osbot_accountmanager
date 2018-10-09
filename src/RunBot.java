import java.io.IOException;

public class RunBot {
	
	private int id;
	
	/**
	 * 
	 */
	public RunBot() {
		getCliArgs().append("java -jar osbot.jar");
	}
	
	/**
	 * 
	 */
	public void runBot() {
		try {
			Runtime.getRuntime().exec(getCliArgs().toString());
			System.out.println(getCliArgs().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param args
	 * @param value
	 */
	public void addArguments(CliArgs args, String value) {
		getCliArgs().append(" ");
		getCliArgs().append(args);
		getCliArgs().append(" ");
		getCliArgs().append(value);
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
	 * @param cliArgs the cliArgs to set
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
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	
	
}
