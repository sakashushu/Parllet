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
	public String result;
	
	public Date created;
	
	public PaymentHistory(
			HaUser ha_user,
			String action_method,
			String result,
			Date created
			) {
		this.ha_user = ha_user;
		this.action_method = action_method;
		this.result = result;
		this.created = created;
	}
}
