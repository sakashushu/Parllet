package controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import models.PaypalTransaction;
import play.Logger;
import play.Play;
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
		String itemName = params.get("item_name");
		String itemNumber = params.get("item_number");
		String paymentStatus = params.get("payment_status");
		String pendingReason = params.get("pending_reason");
		String paymentAmount = params.get("mc_gross");
		String paymentCurrency = params.get("mc_currency");
		String txnId = params.get("txn_id");
		String receiverEmail = params.get("receiver_email");
		String payerEmail = params.get("payer_email");
		
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

