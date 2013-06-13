package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import models.PaypalTransaction;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.mvc.Controller;

/**
*
* Paypal controller
*
* @author Original Author guillaumeleone
* @author Another Author sakashushu
* 
*/
public class PaypalController extends Controller {
	
	public static void reqRedirSetExpressCheckout() {
		Logger.info("PaypalController_reqRedirSetExpressCheckout");
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
		Logger.info("PaypalController_receipt");
		
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
				if (!map.get("ACK").equals("Success")) {
					validation.addError("ackError", Messages.get("validation.anErrorOccured"));
					Logger.info(strDecodeRslt);
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
		render();
	}
	
	public static void confirm() {
		Logger.info("PaypalController_confirm");
   		Calendar calendar = Calendar.getInstance();
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
//&EMAIL=payername@bbb.net  
//&L_PAYMENTREQUEST_0_ITEMCATEGORY0=Digital  
//&L_PAYMENTREQUEST_0_NAME0=Kitty Antics  
//&L_PAYMENTREQUEST_0_AMT0=1.00  
//&L_PAYMENTREQUEST_0_QTY0=1
//
//以下は、支払方法によって異なる必須パラメータ
//
//    TOKEN:PayPal支払いの場合、Express CheckoutのTOKENを指定します。また、TOKENを指定する場合は以下のパラメータも必須になります。
//        SHIPTONAME
//        SHIPTOSTREET
//        SHIPTOCITY
//        SHIPTOSTATE
//        SHIPTOZIP
//        SHIPTOCOUNTRY
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
				}
				
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
	
	/**
	*
	* PayPal sends your IPN listener a message that notifies you of the event
	*
	* Your listener sends the complete unaltered message back to PayPal; the message must contain
	* the same fields in the same order and be encoded in the same way as the original message
	*
	* PayPal sends a single word back, which is either VERIFIED if the message originated with PayPal or
	* INVALID if there is any discrepancy with what was originally sent
	*
	* @throws Exception
	*/
	public static void validation() throws Exception {
		
		Logger.info("validation");
		
		// creation of the url sent to paypal to check the
		//parameters of POST requests is recovered
		String str = "cmd=_notify-validate&" + params.get("body");
		Logger.info(str);
		
		//creating a connection to the paypal
		URL url = new URL("https://www.paypal.com/cgi-bin/webscr");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		
		//sending the request
		PrintWriter out = new PrintWriter(connection.getOutputStream());
		out.println(str);
		out.close();
		
		//reading the response
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String result = in.readLine();
		in.close();
		
		//assign posted variables to local variables
		String txnType = params.get("txn_type");
		String itemName = params.get("item_name");
		String itemNumber = params.get("item_number");
		String paymentStatus = params.get("payment_status");
		String pendingReason = params.get("pending_reason");
		String paymentAmount = params.get("mc_gross");
		String paymentCurrency = params.get("mc_currency");
		String txnId = params.get("txn_id");
		String receiverEmail = params.get("receiver_email");
		String payerEmail = params.get("payer_email");
		
		Logger.info("txn_type = '"+txnType+"'");
		Logger.info("item_name = '"+itemName+"'");
		Logger.info("item_number = '"+itemNumber+"'");
		Logger.info("payment_status = '"+paymentStatus+"'");
		Logger.info("pending_reason = '"+pendingReason+"'");
		Logger.info("mc_gross = '"+paymentAmount+"'");
		Logger.info("mc_currency = '"+paymentCurrency+"'");
		Logger.info("txn_id = '"+txnId+"'");
		Logger.info("receiver_email = '"+receiverEmail+"'");
		Logger.info("payer_email = '"+payerEmail+"'");
		
		//check notification validation
		if ("VERIFIED".equals(result)) {
			if ("Completed".equals(paymentStatus)) {
				// verife that txn_id has not been previously treated
				PaypalTransaction paypalTransaction = PaypalTransaction.findByTrxId(txnId);
				// if no transaction or transaction basis but invalid status request is processed
				if (paypalTransaction == null
						|| (paypalTransaction != null && PaypalTransaction.TrxStatusEnum.INVALID.equals(paypalTransaction.status))) {
					// check that your email address is receiver_email to replace the email address of the seller
					if (Play.configuration.getProperty("paypal.receiver_email").equals(receiverEmail)) {
						// check paymentAmount (EUR) and paymentCurrency (product price) are correct
						Logger.info("Transaction OK");
						// backup track of the transaction based paypal
						new PaypalTransaction(itemName, itemNumber, paymentStatus, paymentAmount, paymentCurrency, txnId, receiverEmail, payerEmail,PaypalTransaction.TrxStatusEnum.VALID).save();
					} else {
						// Wrong paypal email address
						Logger.info("Mauvaise adresse email paypal");
					}
				} else {
					// Transaction ID already used
					Logger.info("La transaction a déjà été traité");
				}
			} else {
				// Payment Status: Failed
				Logger.info("Payment Status: Failed");
			}
		} else if ("INVALID".equals(result)) {
			Logger.info("Invalide transaction");
			new PaypalTransaction(itemName, itemNumber, paymentStatus, paymentAmount, paymentCurrency, txnId, receiverEmail, payerEmail,PaypalTransaction.TrxStatusEnum.INVALID).save();
		} else {
			Logger.info("Erreur lors du traitement");
		}
    }
	
	/** Success payment */
	public static void success(){
		Logger.info("success");
		render();
	}
	
	/** home page */
	public static void buy(){
		Logger.info("buy");
		render();
	}
	
	/** fail payment */
	public static void fail(){
		Logger.info("fail");
		render();
	}
}

