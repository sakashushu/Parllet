package models;

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
	@CheckWith(DebitBankConditionallyCheck.class)
	public HandlingMst debit_bank;				//引落口座

	@CheckWith(CutoffConditionallyCheck.class)
	public Integer cutoff_day;					//締日
	@CheckWith(DebitMonthConditionallyCheck.class)
	public String debit_month;					//引落月
	@CheckWith(CutoffDebitConditionallyCheck.class)
	public Integer debit_day;					//引落日

	public Boolean zero_hidden;					//残高ゼロの時には残高表の対象外とする
	public Boolean invalidity_flg;				//無効フラグ(解約した口座等)
	public Integer order_seq;					//表示順序
	
	public HandlingMst(
			HaUser ha_user,
			HandlingTypeMst handling_type_mst,
			String handling_name,
			HandlingMst debit_bank,
			Integer cutoff_day,
			String debit_month,
			Integer debit_day,
			Boolean zero_hidden,
			Boolean invalidity_flg,
			Integer order_seq
			) {
		this.ha_user = ha_user;
		this.handling_type_mst = handling_type_mst;
		this.handling_name = handling_name;
		this.debit_bank = debit_bank;
		this.cutoff_day = cutoff_day;
		this.debit_month = debit_month;
		this.debit_day = debit_day;
		this.zero_hidden = zero_hidden;
		this.invalidity_flg = invalidity_flg;
		this.order_seq = order_seq;
	}

	public String toString() {
        return handling_name;
	}

	/**
	 * 「取扱種類」がクレジットカードの時は「取扱口座」は必須
	 * @author sakashushu
	 *
	 */
	static class DebitBankConditionallyCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			HandlingMst handlingMst = (HandlingMst)validatedObject;
			if(handlingMst.handling_type_mst.handling_type_name.equals(Messages.get("HandlingType.creca")) &&
					handlingMst.debit_bank==null) {
				setMessage(Messages.get("validation.required"));
				return false;
			}

			return true;
		}
	}

	/**
	 * 「取扱種類」がクレジットカードの時は「締日」は必須
	 * @author sakashushu
	 *
	 */
	static class CutoffConditionallyCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			HandlingMst handlingMst = (HandlingMst)validatedObject;
			if(handlingMst.handling_type_mst.handling_type_name.equals(Messages.get("HandlingType.creca")) &&
					handlingMst.cutoff_day==null) {
				setMessage(Messages.get("validation.required"));
				return false;
			}

			return true;
		}
	}

	/**
	 * 「取扱種類」がクレジットカードの時は「引落月」は必須
	 * @author sakashushu
	 *
	 */
	static class DebitMonthConditionallyCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			HandlingMst handlingMst = (HandlingMst)validatedObject;
			if(handlingMst.handling_type_mst.handling_type_name.equals(Messages.get("HandlingType.creca")) &&
					handlingMst.debit_month==null) {
				setMessage(Messages.get("validation.required"));
				return false;
			}

			return true;
		}
	}

	/**
	 * 「取扱種類」がクレジットカードの時は「引落日」は必須
	 * 「取扱種類」がクレジットカードで締日より引落日が過去はエラー
	 * @author sakashushu
	 *
	 */
	static class CutoffDebitConditionallyCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			HandlingMst handlingMst = (HandlingMst)validatedObject;
			if(handlingMst.handling_type_mst.handling_type_name.equals(Messages.get("HandlingType.creca"))) {
				if(handlingMst.debit_day==null) {
					setMessage(Messages.get("validation.required"));
					return false;
				} else {
					if(handlingMst.cutoff_day!=null &&
							handlingMst.debit_month!=null &&
							handlingMst.debit_day!=null) {
						if(handlingMst.debit_month.equals(Messages.get("DebitMonth.this")) &&
								handlingMst.cutoff_day > handlingMst.debit_day) {
							setMessage(Messages.get("validation.cutoff_debit"));
							return false;
						}
					}
				}
			}
			return true;
		}
	}

}
