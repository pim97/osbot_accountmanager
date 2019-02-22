package anti_captcha;

import java.net.MalformedURLException;
import java.net.URL;

import anti_captcha.Api.NoCaptchaProxyless;
import anti_captcha.Helper.DebugHelper;

public class AntiCaptcha {

	/**
	 * 
	 * @param args
	 */
	// public static void main(String args[]) {
	// try {
	// exampleNoCaptchaProxyless();
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public static String exampleNoCaptchaProxyless(String captchaKey, String websiteLink, String websiteKey)
			throws MalformedURLException, InterruptedException {
		String result = "";
		DebugHelper.setVerboseMode(true);

		NoCaptchaProxyless api = new NoCaptchaProxyless();
		api.setClientKey(captchaKey);
		api.setWebsiteUrl(new URL(websiteLink));
		api.setWebsiteKey(websiteKey);

		if (!api.createTask()) {
			DebugHelper.out("API v2 send failed. " + api.getErrorMessage(), DebugHelper.Type.ERROR);
		} else if (!api.waitForResult()) {
			DebugHelper.out("Could not solve the captcha.", DebugHelper.Type.ERROR);
		} else {
			result = api.getTaskSolution().getGRecaptchaResponse();
			DebugHelper.out("Result: " + api.getTaskSolution().getGRecaptchaResponse(), DebugHelper.Type.SUCCESS);
		}
		return result;
	}

	// public static String exampleNoCaptcha(String captchaKey, String websiteLink,
	// String websiteKey, NoCaptcha.ProxyTypeOption proxyType, String proxyAddress,
	// String proxyPort) throws MalformedURLException, InterruptedException {
	// String result = "";
	// DebugHelper.setVerboseMode(true);
	//
	// NoCaptcha api = new NoCaptcha();
	// api.setClientKey(captchaKey);
	// api.setWebsiteUrl(new URL( websiteLink
	//// java.net.URLDecoder.decode(websiteLink, "UTF-8")
	// ));
	// api.setWebsiteKey(websiteKey);
	// api.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6)
	// AppleWebKit/537.36 " +
	// "(KHTML, like Gecko) Chrome/52.0.2743.116");
	//
	// api.setProxyType(proxyType);
	// api.setProxyAddress(proxyAddress);
	// api.setProxyPort(Integer.parseInt(proxyPort));
	// api.setProxyLogin(Config.proxyUsername);
	// api.setProxyPassword(Config.proxyPassword);
	//
	// // proxy access parameters
	//// api.setProxyType(proxyType);
	//// api.setProxyAddress(proxyAddress);
	//// api.setProxyPort(proxyPort);
	//// api.setProxyLogin(Config.proxyUsername);
	//// api.setProxyPassword(Config.proxyPassword);
	//
	// if (!api.createTask()) {
	// DebugHelper.out(
	// "API v2 send failed. " + api.getErrorMessage(),
	// DebugHelper.Type.ERROR
	// );
	// } else if (!api.waitForResult()) {
	// DebugHelper.out("Could not solve the captcha.", DebugHelper.Type.ERROR);
	// } else {
	// result = api.getTaskSolution().getGRecaptchaResponse();
	// DebugHelper.out("Result: " + api.getTaskSolution().getGRecaptchaResponse(),
	// DebugHelper.Type.SUCCESS);
	// }
	// return result;
	// }
}
