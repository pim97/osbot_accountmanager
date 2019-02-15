package osbot.threads.test;

public class test {

	public static void main(String[] args) {
		double perc = 2 / 5 * 100;
		
		System.out.println("123");
	}
//
//		new Thread(() -> {
//			
//			String timeToStart = "07:30:00";
//			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
//			SimpleDateFormat formatOnlyDay = new SimpleDateFormat("yyyy-MM-dd");
//			Date now = new Date();
//			Date dateToStart = null;
//			
//			Date tomorrow = new Date();
//			Calendar c = Calendar.getInstance();
//			
//			try {
//				dateToStart = format.parse(formatOnlyDay.format(now) + " at " + timeToStart);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			while (true) {
//				Date atTheMoment = new Date();
//				long diff = dateToStart.getTime() - atTheMoment.getTime();
//				
//				System.out.println("diff 1: " + diff);
//				if (diff < 0 && diff > -600_000) {
//					
//					DatabaseUtilities.updateAtASpecificTimeToMule();
//					c.setTime(tomorrow);
//					c.add(Calendar.DATE, 1);
//					tomorrow = c.getTime();
//					try {
//						dateToStart = format.parse(formatOnlyDay.format(tomorrow) + " at " + timeToStart);
//					} catch (ParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//					System.out.println("end: " + diff);
//				}
//
//				try {
//					Thread.sleep(1500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}).start();
//		
//		new Thread(() -> {
//			String timeToStart = "15:30:00";
//			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
//			SimpleDateFormat formatOnlyDay = new SimpleDateFormat("yyyy-MM-dd");
//			Date now = new Date();
//			Date dateToStart = null;
//			
//			Date tomorrow = new Date();
//			Calendar c = Calendar.getInstance();
//			
//			try {
//				dateToStart = format.parse(formatOnlyDay.format(now) + " at " + timeToStart);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			while (true) {
//				Date atTheMoment = new Date();
//				long diff = dateToStart.getTime() - atTheMoment.getTime();
//				
//				System.out.println("diff 1: " + diff);
//				if (diff < 0 && diff > -600_000) {
//
//					DatabaseUtilities.updateAtASpecificTimeToMule();
//					c.setTime(tomorrow);
//					c.add(Calendar.DATE, 1);
//					tomorrow = c.getTime();
//					try {
//						dateToStart = format.parse(formatOnlyDay.format(tomorrow) + " at " + timeToStart);
//					} catch (ParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//					System.out.println("end: " + diff);
//				}
//
//				try {
//					Thread.sleep(1500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}).start();
//
//	}

	// public static void main(String[] args) {
	//
	// System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
	// // System.getProperty("user.home") + "/toplistbot/driver/geckodriver.exe");
	// System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,
	// "true");
	// System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,
	// "/dev/null");
	//
	// ProfilesIni profile2 = new ProfilesIni();
	// FirefoxProfile profile = profile2.getProfile("bot");// new FirefoxProfile();
	//
	// FirefoxBinary firefoxBinary = new FirefoxBinary();
	// // firefoxBinary.addCommandLineOptions("--headless");
	// DesiredCapabilities dc = new DesiredCapabilities();
	// FirefoxOptions option = new FirefoxOptions();
	//
	// option.setBinary(firefoxBinary);
	// option.setProfile(profile);
	// int pidId = -1;
	//
	// // PidDriver driver = new PidDriver();
	// List<Integer> pids = GeckoHandler.getGeckodriverExeWindows();
	// List<Integer> pidsAfter = null;
	//
	// WebDriver driver = new FirefoxDriver(option);
	//
	// int tries = 0;
	// boolean searching = true;
	//
	// PidDriver pidDriver = new PidDriver(driver, pidId);
	//
	// Dimension n = new Dimension(1000, 700);
	// driver.manage().window().setSize(n);
	//
	// driver.get("moz-extension://49aecb7d-8e81-4baf-8d90-d5e138cc07fd/add-edit-proxy.html");
	// // old
	//
	// // Selecting socks 5
	// Select select = new Select(driver.findElement(By.id("newProxyType")));
	// select.selectByIndex(1);
	//
	// driver.findElement(By.id("newProxyAddress")).sendKeys("1118.139.176.242");
	// driver.findElement(By.id("newProxyPort")).sendKeys("14619");
	// driver.findElement(By.id("newProxyUsername")).sendKeys("");
	// driver.findElement(By.id("newProxyPassword")).sendKeys("");
	// driver.findElement(By.id("newProxySave")).click();
	//
	// driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
	//
	// boolean onWebsite = false;
	//
	// while (!onWebsite) {
	// try {
	// driver.navigate().to(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);
	// } catch (Exception e) {
	// System.out.println("Page did not load within 40 seconds!");
	// System.out.println("Restarting driver and trying again");
	// e.printStackTrace();
	// driver.navigate().to(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);
	// }
	// onWebsite = true;
	// try {
	// Thread.sleep(1500);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// System.out.println("Successfully navigateed!");
	// }

}
