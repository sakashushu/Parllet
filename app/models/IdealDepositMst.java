package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.i18n.Messages;

@Entity
public class IdealDepositMst extends Model {

	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	@Required
	@CheckWith(IdealDepositNameMultipleCheck.class)
	public String ideal_deposit_name;
	
	public Boolean zero_hidden;					//残高ゼロの時には残高表の対象外とする
	
	public Integer order_seq;
	
	public IdealDepositMst(
			HaUser ha_user,
			String ideal_deposit_name,
			Boolean zero_hidden,
			Integer order_seq
			) {
		this.ha_user = ha_user;
		this.ideal_deposit_name = ideal_deposit_name;
		this.zero_hidden = zero_hidden;
		this.order_seq = order_seq;
	}

	public String toString() {
        return ideal_deposit_name;
	}
	
	/**
	 * 「My貯金名」は重複登録不可
	 * @author sakashushu
	 *
	 */
	static class IdealDepositNameMultipleCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			IdealDepositMst idealDepositMst = (IdealDepositMst)validatedObject;
			IdealDepositMst idmExist = null;
			idmExist = IdealDepositMst.find(
					"ha_user = ? and ideal_deposit_name = ?",
					idealDepositMst.ha_user,
					idealDepositMst.ideal_deposit_name).first();
			if(idmExist!=null && idmExist.id!=idealDepositMst.id) {
				setMessage(Messages.get("validation.multipleName"));
				return false;
			}

			return true;
		}
	}
}
