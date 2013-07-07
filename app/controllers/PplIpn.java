package controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import models.HaUser;
import models.PaymentHistory;
import models.PaypalTransaction;
import play.Logger;
import play.Play;
import play.mvc.Controller;


public class PplIpn extends Controller {
	
	public static void validation() throws Exception {
		String strActionMethod = "PplIpn_validation";
		Logger.info(strActionMethod);
		
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
		String paymentAmount = params.get("mc_gross");
		String paymentCurrency = params.get("mc_currency");
		String txnId = params.get("txn_id");
		String receiverEmail = params.get("receiver_email");
		String payerEmail = params.get("payer_email");
		String payerId = params.get("payer_id");
		String recurringPaymentId = params.get("recurring_payment_id");
		
		//check notification validation
		if ("VERIFIED".equals(result)) {
			HaUser hu = HaUser.find("byPplPayerId", payerId).first();
			if (hu==null) {
				Logger.info("hu==null");
			} else {
				Common cm = new Common();
				Date dteNow = cm.locDate();
				
				//支払履歴
				PaymentHistory ph = new PaymentHistory(
						hu,
						strActionMethod,
						txnType,
						recurringPaymentId,
						paymentStatus,
						dteNow
						);
				// Validate
				validation.valid(ph);
				if (validation.hasErrors())
					render();
				// 保存
				ph.save();
				//定期支払（契約締結・決済）
				if (txnType!=null) {
					if (txnType.equals("recurring_payment_profile_created") ||
							txnType.equals("recurring_payment")) {
						hu.pplStatus = 1;
						hu.modified = dteNow;
						// Validate
						validation.valid(hu);
						if (validation.hasErrors())
							render();
						// 保存
						hu.save();
					} else if (txnType.equals("recurring_payment_profile_cancel")) {
						hu.pplStatus = 9;
						hu.modified = dteNow;
						// Validate
						validation.valid(hu);
						if (validation.hasErrors())
							render();
						// 保存
						hu.save();
					}
				}
			}
			
			
			
			//定期支払（契約締結・決済）
			if (txnType!=null &&
					(txnType.equals("recurring_payment_profile_created") ||
							txnType.equals("recurring_payment"))) {
				
			} else if (txnType!=null &&
					(txnType.equals("recurring_payment_failed") ||
							txnType.equals("recurring_payment_profile_cancel"))) {
				//管理者に自動メール送信予定
				
			} else {
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
			}
		} else if ("INVALID".equals(result)) {
			Logger.info("Invalide transaction");
			new PaypalTransaction(itemName, itemNumber, paymentStatus, paymentAmount, paymentCurrency, txnId, receiverEmail, payerEmail,PaypalTransaction.TrxStatusEnum.INVALID).save();
		} else {
			Logger.info("エラー処理");
		}
    }
	
}

