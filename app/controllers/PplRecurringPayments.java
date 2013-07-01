package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import models.HaUser;
import models.PaymentHistory;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;


@With(Secure.class)
public class PplRecurringPayments extends Controller {
	
	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			Logger.info("haUser byEmail Done.");
			renderArgs.put("haUser", haUser);
		}
	}
	
	public static void oneMonthLater() {
		render();
	}
	
	public static void reqRedirSetExpressCheckout() {
		Logger.info("PplRecurringPayments_reqRedirSetExpressCheckout");
		String str = "" +
				"METHOD=SetExpressCheckout" +
				"&VERSION=95.0" +
				"&USER=" + Play.configuration.getProperty("paypal.API_username") +
				"&PWD=" + Play.configuration.getProperty("paypal.API_password") +
				"&SIGNATURE=" + Play.configuration.getProperty("paypal.API_signature") +
				"&PAYMENTREQUEST_0_AMT=100" +
				"&PAYMENTREQUEST_0_CURRENCYCODE=JPY" +
				"&PAYMENTREQUEST_0_PAYMENTACTION=Sale" +
				"&NOSHIPPING=1" +
				"&RETURNURL=" + Play.configuration.getProperty("paypal.API_returnurl") +
				"&CANCELURL=" + Play.configuration.getProperty("paypal.API_cancelurl") +
				"&L_BILLINGTYPE0=RecurringPayments" +
				"&L_BILLINGAGREEMENTDESCRIPTION0=" + Play.configuration.getProperty("paypal.API_l_billingagreementdescription0") +
				"";
		try {
			URL url = new URL("https://api-3t.paypal.com/nvp");
			URLConnection connection;
			try {
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
				if (!map.get("ACK").equals("Success")) {
					validation.addError("ackError", Messages.get("validation.anErrorOccured"));
					Logger.info(strDecodeRslt);
					render("@receipt");
				}
				String strToken = map.get("TOKEN");
				session.put("paypalToken", strToken);
				redirect("https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+strToken);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void receipt() {
		Logger.info("PplRecurringPayments_receipt");
		
		String strToken = params.get("token");
		validation.clear();
		if (strToken==null || !strToken.equals(session.get("paypalToken"))) {
			validation.addError("callError", Messages.get("validation.badCall"));
			render();
		}
		String str = "" +
				"METHOD=GetExpressCheckoutDetails" +
				"&VERSION=95.0" +
				"&USER=" + Play.configuration.getProperty("paypal.API_username") +
				"&PWD=" + Play.configuration.getProperty("paypal.API_password") +
				"&SIGNATURE=" + Play.configuration.getProperty("paypal.API_signature") +
				"&TOKEN=" + strToken +
				"";
		try {
			URL url = new URL("https://api-3t.paypal.com/nvp");
			URLConnection connection;
			try {
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
				if (!map.get("ACK").equals("Success"))
					validation.addError("ackError", Messages.get("validation.anErrorOccured"));
				HaUser hu = (HaUser)renderArgs.get("haUser");
				hu.pplPayerId = map.get("PAYERID");
				// Validate
				validation.valid(hu);
				if (validation.hasErrors())
					render();
				// 保存
				hu.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				validation.addError("ioError", e.getMessage());
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			validation.addError("malformedUrlError", e.getMessage());
		}
		render();
	}
	
	public static void confirm() {
		String strActionMethod = "PplRecurringPayments_confirm";
		Logger.info(strActionMethod);
		Calendar calendar = Calendar.getInstance();
//		int intDate = calendar.get(Calendar.DATE);
//		if (intDate > 27)
//			calendar.add(Calendar.DATE, 27-intDate);
//		calendar.add(Calendar.MONTH, 1);
		String str = "" +
				"METHOD=CreateRecurringPaymentsProfile" +
				"&VERSION=95.0" +
				"&USER=" + Play.configuration.getProperty("paypal.API_username") +
				"&PWD=" + Play.configuration.getProperty("paypal.API_password") +
				"&SIGNATURE=" + Play.configuration.getProperty("paypal.API_signature") +
				"&TOKEN=" + session.get("paypalToken") +
				"&PROFILESTARTDATE=" + String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS", calendar.getTime()) +
				"&DESC=" + Play.configuration.getProperty("paypal.API_l_billingagreementdescription0") +
				"&BILLINGPERIOD=Month" +
				"&BILLINGFREQUENCY=1" +
				"&AMT=100" +
				"&CURRENCYCODE=JPY" +
				"";
		try {
			URL url = new URL("https://api-3t.paypal.com/nvp");
			URLConnection connection;
			try {
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
				Logger.info(strDecodeRslt);
				StringTokenizer st = new StringTokenizer(strDecodeRslt, "&");
				Map<String,String> map = new HashMap<String,String>();
				while(st.hasMoreTokens()) {
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
					map.put(st2.nextToken(), st2.nextToken());
				}
				String strSuccess = "Success";
				if (!map.get("ACK").equals(strSuccess)) {
					validation.addError("ackError", Messages.get("validation.anErrorOccured"));
					Logger.info(strDecodeRslt);
					render();
				}
				//支払履歴
				HaUser hu = (HaUser)renderArgs.get("haUser");
				PaymentHistory ph = new PaymentHistory(
						hu,
						strActionMethod,
						strSuccess,
						Calendar.getInstance().getTime()
						);
				// Validate
				validation.valid(ph);
				if (validation.hasErrors())
					render();
				// 保存
				ph.save();
				
				Date dteNow = Calendar.getInstance().getTime();
				hu.pplStatus = 1;
				hu.modified = dteNow;
				// Validate
				validation.valid(hu);
				if (validation.hasErrors())
					render();
				// 保存
				hu.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		render();
	}
	
}
