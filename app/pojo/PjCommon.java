package pojo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import play.Logger;
import play.Play;

public class PjCommon {
	/**
	 * 定期支払いのキャンセル
	 * @param strProfileId
	 */
	public static void pplCancel(String strProfileId) throws Exception {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[1];
		String strActionMethod = ste.getClassName() + "." + ste.getMethodName();
		Logger.info(strActionMethod);
		
		String str = "" +
				"METHOD=ManageRecurringPaymentsProfileStatus" +
				"&VERSION=95.0" +
				"&USER=" + Play.configuration.getProperty("paypal.API_username") +
				"&PWD=" + Play.configuration.getProperty("paypal.API_password") +
				"&SIGNATURE=" + Play.configuration.getProperty("paypal.API_signature") +
				"&PROFILEID=" + strProfileId +
				"&ACTION=Cancel" +
				"&NOTE=Cancellation for the change of type of user" +
				"";
		URL url = new URL("https://api-3t.paypal.com/nvp");
		URLConnection connection;
		connection = url.openConnection();
		connection.setDoOutput(true);	//POST可能にする
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		
		//sending the request
		PrintWriter out = new PrintWriter(connection.getOutputStream());
		out.println(str);
		out.close();
		
		//reading the response
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String result = in.readLine();
		in.close();
		
		String strDecodeRslt = URLDecoder.decode(result, "utf-8");
		StringTokenizer st = new StringTokenizer(strDecodeRslt, "&");
		Map<String,String> map = new HashMap<String,String>();
		while(st.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
			map.put(st2.nextToken(), st2.nextToken());
		}
		Logger.info(strDecodeRslt);
	}

}
