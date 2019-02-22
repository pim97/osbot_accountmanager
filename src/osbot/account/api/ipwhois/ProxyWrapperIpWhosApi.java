package osbot.account.api.ipwhois;

import java.util.HashMap;

public class ProxyWrapperIpWhosApi implements ProxyInformation {

	private IPWhoisApi api = new IPWhoisApi();

	private HashMap<String, WhoIsIp> cachedProxyInformation = new HashMap<String, WhoIsIp>();

	@Override
	public WhoIsIp getProxyInformation(String proxyIp, String port) {
		WhoIsIp ip = cachedProxyInformation.get(proxyIp);

		if (ip == null) {
			ip = api.getProxyInformation(proxyIp, port);
			cachedProxyInformation.put(proxyIp, ip);
		} else {
			System.out.println("Retrieved IP " + proxyIp + " from cache");
		}
		return ip;
	}

}
