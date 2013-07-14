package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.i18n.Messages;

@Entity
public class ParlletMst extends Model {

	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	@Required
	@MaxSize(value=80)
	@CheckWith(ParlletNameMultipleCheck.class)
	public String parllet_name;
	
	public Boolean zero_hidden;					//残高ゼロの時には残高表の対象外とする
	
	public Integer order_seq;
	
	public ParlletMst(
			HaUser ha_user,
			String parllet_name,
			Boolean zero_hidden,
			Integer order_seq
			) {
		this.ha_user = ha_user;
		this.parllet_name = parllet_name;
		this.zero_hidden = zero_hidden;
		this.order_seq = order_seq;
	}

	public String toString() {
        return parllet_name;
	}
	
	/**
	 * 「Parllet名」は重複登録不可
	 * @author sakashushu
	 *
	 */
	static class ParlletNameMultipleCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			ParlletMst parlletMst = (ParlletMst)validatedObject;
			ParlletMst idmExist = null;
			idmExist = ParlletMst.find(
					"ha_user = ? and parllet_name = ?",
					parlletMst.ha_user,
					parlletMst.parllet_name).first();
			if(idmExist!=null && idmExist.id!=parlletMst.id) {
				setMessage(Messages.get("validation.multipleName"));
				return false;
			}

			return true;
		}
	}
}
