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
import models.LevelMst;
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
		if (Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("haUser", haUser);
		}
	}
	
	public static void oneMonthLater() {
		render();
	}
	
	public static void reqWebPay() {
		Logger.info("PplRecurringPayments_reqWebPay");
		String str = "" +
				"amount=103" +
				"&currency=jpy" +
				"&description=buy_aitem" +
				"&card[number]=5555-5555-5555-4444" +
				"&card[exp_month]=8" +
				"&card[exp_year]=2016" +
				"&card[cvc]=650" +
				"&card[name]=SHU SAKA" +
				"";
		try {
			URL url = new URL("https://api.webpay.jp/v1/charges");
			URLConnection connection;
			try {
				connection = url.openConnection();
				connection.setDoOutput(true);	//POST可能にする
				connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				connection.setRequestProperty("Authorization","Bearer test_secret_6WKbum4Lug7HfGn1qygzP5bE");
				
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * paypal SetExpressCheckoutへのリダイレクト
	 * @param intAmt
	 */
	public static void reqRedirSetExpressCheckout(
			int intAmt,
			int intLevelNew
			) {
		HaUser hU = (HaUser)renderArgs.get("haUser");
		//無料プランに戻す時
		if (intLevelNew==0) {
			//既存支払をキャンセル
			try {
				new pojo.PjCommon().pplCancel(hU.pplProfileId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Config.cf_hauser_edit();
			}
			
			//アカウントの種類を変更
			Date dteNow = new Common().locDate();
			hU.level_mst = LevelMst.find("byLevel", intLevelNew).first();
			hU.pplPayerId = null;
			hU.pplProfileId = null;
			hU.pplStatus = null;
			hU.modified = dteNow;
			validation.valid(hU);
			if (validation.hasErrors())
				Config.cf_hauser_lv_edit(true, "haUserValidError", validation.errors().get(0).message());
			// 保存
			hU.save();
			
			Config.cf_hauser_edit();
		}
		
		StackTraceElement ste = Thread.currentThread().getStackTrace()[1];
		Logger.info(ste.getClassName() + "." + ste.getMethodName());
		String str = "" +
				"METHOD=SetExpressCheckout" +
				"&VERSION=95.0" +
				"&USER=" + Play.configuration.getProperty("paypal.API_username") +
				"&PWD=" + Play.configuration.getProperty("paypal.API_password") +
				"&SIGNATURE=" + Play.configuration.getProperty("paypal.API_signature") +
				"&PAYMENTREQUEST_0_AMT=" + intAmt +
				"&PAYMENTREQUEST_0_CURRENCYCODE=JPY" +
				"&PAYMENTREQUEST_0_PAYMENTACTION=Sale" +
				"&NOSHIPPING=1" +
				"&RETURNURL=" + Play.configuration.getProperty("paypal.API_returnurl") +
				"&CANCELURL=" + Play.configuration.getProperty("paypal.API_cancelurl") +
				"&L_BILLINGTYPE0=RecurringPayments" +
				"&L_BILLINGAGREEMENTDESCRIPTION0=" + Play.configuration.getProperty("paypal.API_l_billingagreementdescription0_pre") + " " + intAmt + Play.configuration.getProperty("paypal.API_l_billingagreementdescription0_suf") +
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
//				validation.addError("ioError", e.getMessage());
				Config.cf_hauser_lv_edit(true, "ioError", Messages.get("views.pplRecurringPayments.reqRedirSetExpressCheckout.ioErr"));
//				Config.cf_hauser_lv_edit(validation);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			validation.addError("malformedUrlError", e.getMessage());
			Config.cf_hauser_lv_edit(true, "malformedUrlError", e.getMessage());
//			Config.cf_hauser_lv_edit(validation);
		}
	}
	
	/**
	 * paypalからの回帰と詳細表示
	 */
	public static void receipt() {
//		Logger.info("PplRecurringPayments_receipt");
		StackTraceElement ste = Thread.currentThread().getStackTrace()[1];
		Logger.info(ste.getClassName() + "." + ste.getMethodName());
		
		String strPayerId = "";
		Integer intAmt = null;
		String strToken = params.get("token");
		validation.clear();
		HaUser haUser = (HaUser)renderArgs.get("haUser");
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
				
				strPayerId = map.get("PAYERID");
				try {
					intAmt = Integer.parseInt(map.get("AMT"));
				} catch (NumberFormatException e) {
					// TODO: handle exception
					e.printStackTrace();
					validation.addError("numberFormatError", e.getMessage());
				}
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
		render(haUser, strPayerId, intAmt);
	}
	
	/**
	 * 定期支払いの確定
	 * @param payer_id
	 * @param intAmt
	 */
	public static void confirm(
			String payer_id,
			Integer intAmt
			) {
		if (payer_id==null ||
				payer_id.equals("") ||
				intAmt==null) {
			validation.addError("callError", Messages.get("validation.badCall"));
			render();
		}
		
//		String strActionMethod = "PplRecurringPayments_confirm";
		StackTraceElement ste = Thread.currentThread().getStackTrace()[1];
		String strActionMethod = ste.getClassName() + "." + ste.getMethodName();
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		String strProfileId = "";
		Date dteNow = new Common().locDate();
		Logger.info(strActionMethod);
		
		Calendar calendar = Calendar.getInstance();
		int intDate = calendar.get(Calendar.DATE);
		if (intDate > 27)
			calendar.add(Calendar.DATE, 27-intDate);
		calendar.add(Calendar.MONTH, 1);
		String str = "" +
				"METHOD=CreateRecurringPaymentsProfile" +
				"&VERSION=95.0" +
				"&USER=" + Play.configuration.getProperty("paypal.API_username") +
				"&PWD=" + Play.configuration.getProperty("paypal.API_password") +
				"&SIGNATURE=" + Play.configuration.getProperty("paypal.API_signature") +
				"&TOKEN=" + session.get("paypalToken") +
				"&PROFILESTARTDATE=" + String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS", calendar.getTime()) +
				"&DESC=" + Play.configuration.getProperty("paypal.API_l_billingagreementdescription0_pre") + " " + intAmt + Play.configuration.getProperty("paypal.API_l_billingagreementdescription0_suf") +
				"&BILLINGPERIOD=Month" +
				"&BILLINGFREQUENCY=1" +
				"&AMT=" + intAmt +
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
					render(haUser);
				}
				strProfileId = map.get("PROFILEID");
				
				//支払履歴
				PaymentHistory pH = new PaymentHistory(
						haUser,
						strActionMethod,
						null,
						null,
						null,
						dteNow
						);
				// Validate
				validation.valid(pH);
				if (validation.hasErrors())
					render(haUser);
				// 保存
				pH.save();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				validation.addError("ioError", e.getMessage());
				render(haUser);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			validation.addError("malformedURLError", e.getMessage());
			render(haUser);
		}
		//既存支払がある場合、キャンセル
		if (haUser.level_mst.level!=0) {
			try {
				new pojo.PjCommon().pplCancel(haUser.pplProfileId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				render(haUser);
			}
		}
		
		//ユーザー情報変更
		haUser.level_mst = LevelMst.find("byMonth_amount", intAmt).first();
		haUser.pplPayerId = payer_id;
		haUser.pplStatus = 1;
		haUser.pplProfileId = strProfileId;
		haUser.modified = dteNow;
		// Validate
		validation.valid(haUser);
		if (validation.hasErrors())
			render(haUser);
		// 保存
		haUser.save();
		
		render(haUser);
	}
	
	public static void test() {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		render("@confirm", haUser);
	}
}