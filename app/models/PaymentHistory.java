package models;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class PaymentHistory extends Model {

	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	public String action_method;
	public String txn_type;
	public String recurring_payment_id;
	public String payment_status;
	
	public Date created;
	
	public PaymentHistory(
			HaUser ha_user,
			String action_method,
			String txn_type,
			String recurring_payment_id,
			String payment_status,
			Date created
			) {
		this.ha_user = ha_user;
		this.action_method = action_method;
		this.txn_type = txn_type;
		this.recurring_payment_id = recurring_payment_id;
		this.payment_status = payment_status;
		this.created = created;
	}
}
