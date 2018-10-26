package osbot.account.creator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class HttpRequests {

	private static final String API_KEY = "SDFJNLKDASNFJK798283423NJASKF";

	private static final String LINK = "http://localhost:8000/osbot/api";

	/**
	 * Updates the account status (banned, locked etc) in the database
	 * 
	 * @param newPassword
	 * @param accountId
	 * @return
	 */
	public static boolean updateAccountStatusInDatabase(String status, String email) {
		try {

			String urlParameters = "?status=" + URLEncoder.encode(status, "UTF-8") + "&email="
					+ URLEncoder.encode(email, "UTF-8");

			sendGet(LINK + "" + urlParameters);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Updates stage progress for quests etc.
	 * 
	 * @param prov
	 * @param accountStatus
	 * @param number
	 * @param email
	 * @return
	 */
	public static boolean updateStageProgress(String accountStatus, int number, String email) {
		try {

			String urlParameters = "?accountStage=" + URLEncoder.encode(accountStatus, "UTF-8") + "&email="
					+ URLEncoder.encode(email, "UTF-8") + "&number=" + URLEncoder.encode("" + number, "UTF-8");

			sendGet(LINK + "" + urlParameters);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void sendGet(String url) throws Exception {
		String USER_AGENT = "Mozilla/5.0";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

	}
}
