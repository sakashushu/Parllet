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
public class HandlingMst extends Model {
	
	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	@Required
	@ManyToOne
	public HandlingTypeMst handling_type_mst;	//取扱種類

	@Required
	public String handling_name;				//取扱名
	
	@ManyToOne
	@CheckWith(CutoffDebitConditionallyRequiredCheck.class)
	public HandlingMst debit_bank;				//引落口座

	@CheckWith(CutoffDebitConditionallyRequiredCheck.class)
	public Integer cutoff_day;					//締日
	@CheckWith(CutoffDebitConditionallyRequiredCheck.class)
	public String debit_month;					//引落月
	@CheckWith(CutoffDebitConditionallyRequiredCheck.class)
	public Integer debit_day;					//引落日

	public HandlingMst(
			HaUser ha_user,
			HandlingTypeMst handling_type_mst,
			String handling_name,
			HandlingMst debit_bank,
			Integer cutoff_day,
			String debit_month,
			Integer debit_day
			) {
		this.ha_user = ha_user;
		this.handling_type_mst = handling_type_mst;
		this.handling_name = handling_name;
		this.debit_bank = debit_bank;
		this.cutoff_day = cutoff_day;
		this.debit_month = debit_month;
		this.debit_day = debit_day;
	}

	public String toString() {
        return handling_name;
	}

	/**
	 * 「取扱種類」がクレジットカードの時は「取扱口座」・「締日」・「引落月」・「引落日」は必須
	 * @author sakashushu
	 *
	 */
	static class CutoffDebitConditionallyRequiredCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			HandlingMst handlingMst = (HandlingMst)validatedObject;
			if(handlingMst.handling_type_mst.handling_type_name.equals(Messages.get("views.config.cf_creca")) &&
					(handlingMst.debit_bank==null ||
					handlingMst.cutoff_day==null ||
					handlingMst.debit_month==null ||
					handlingMst.debit_day==null)) {
				setMessage(Messages.get("validation.required"));
				return false;
			}
			return true;
		}
	}
}
