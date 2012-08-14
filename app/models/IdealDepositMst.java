package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class IdealDepositMst extends Model {

	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	public String ideal_deposit_name;
	
	public Boolean zero_hidden;					//残高ゼロの時には残高表の対象外とする
	
	public IdealDepositMst(
			HaUser ha_user,
			String ideal_deposit_name,
			Boolean zero_hidden
			) {
		this.ha_user = ha_user;
		this.ideal_deposit_name = ideal_deposit_name;
		this.zero_hidden = zero_hidden;
	}

	public String toString() {
        return ideal_deposit_name;
	}
}
