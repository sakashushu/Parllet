package models;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import play.db.jpa.Model;

/**
*
* @author guillaumeleone
*
*/
@Entity
public class PaypalTransaction extends Model{

	//fields
	public String itemName;
	public String itemNumber;
	public String paymentStatus;
	public String paymentAmount;
	public String paymentCurrency;
	public String txnId;
	public String receiverEmail;
	public String payerEmail;
	
	@Enumerated(EnumType.STRING)
	public TrxStatusEnum status;
	
	/**
	* gestion du status de la transaction
	*/
	public enum TrxStatusEnum{
		VALID, INVALID;
	}
	
	//constructor
	public PaypalTransaction(String itemName, String itemNumber, String paymentStatus, String paymentAmount,
			String paymentCurrency, String txnId, String receiverEmail, String payerEmail, TrxStatusEnum status) {
		this.itemName = itemName;
		this.itemNumber = itemNumber;
		this.paymentStatus = paymentStatus;
		this.paymentAmount = paymentAmount;
		this.paymentCurrency = paymentCurrency;
		this.txnId = txnId;
		this.receiverEmail = receiverEmail;
		this.payerEmail = payerEmail;
		this.status = status;
	}
	
	/**
	*
	* Recherche en base une transaction paypal en fonction de la transactionId
	*
	* @param id
	* de la transaction
	* @return PaypalTransaction
	*
	*/
	public static PaypalTransaction findByTrxId(String id){
		return PaypalTransaction.find("byTxnId", id).first();
	}
	
	//toString
	public String toString(){
		return this.txnId;
	}
}


