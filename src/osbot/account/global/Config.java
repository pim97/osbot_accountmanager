package osbot.account.global;

import osbot.account.creator.queue.CaptchaQueue;

public class Config {
	
	/**
	 * Osbot configs
	 */

	public static final String OSBOT_USERNAME = "dormic";
	
	public static final String OSBOT_PASSWORD = "DuTIbljNuXHDF4T0e7Bk";
	
	public static final CaptchaQueue QUEUE = new CaptchaQueue();

	
	/**
	 * Bots settings
	 */
	public static int MAX_BOTS_OPEN = 14;

	/**
	 * Configs for threads
	 */

	public static final boolean CAPTCHA = true;
	
	public static final boolean CREATING_ACCOUNTS_THREAD_ACTIVE = false;
	
	public static final boolean RECOVERING_ACCOUNTS_THREAD_ACTIVE = false;
	
	public static final boolean BOT_HANDLER_THREAD_ACTIVE = true;
	
	public static final boolean MULES_TRADING = true;
}
