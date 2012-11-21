package models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class Record extends Model {
	
	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	@Required
	public Date payment_date;					//日付
	@Required
	@ManyToOne
	public BalanceTypeMst balance_type_mst;		//収支種類
	@ManyToOne
	@CheckWith(HandlingConditionallyRequiredCheck.class)
	public HandlingMst handling_mst;			//取扱(実際)
	@ManyToOne
	@CheckWith(IdealDepositConditionallyRequiredCheck.class)
	public IdealDepositMst ideal_deposit_mst;	//取扱(My貯金)
	@ManyToOne
	public ItemMst item_mst;					//項目
	@MaxSize(40)
	public String detail_mst;					//項目詳細
	@Required
	public Integer amount;						//金額
	public Integer price;						//単価
	public Integer quantity;					//数量
	@CheckWith(DebitDateConditionallyRequiredCheck.class)
	public Date debit_date;						//引落日
	@MaxSize(100)
	public String content;						//内容
	@MaxSize(40)
	public String store;						//お店
	@MaxSize(10000)
	public String remarks;						//備考
	@MaxSize(10000)
	public String secret_remarks;				//備考（非公開）
	public Boolean secret_rec_flg;				//非公開レコードフラグ
	
	public Integer remainder;
	
	public Record(
			HaUser ha_user,
			Date payment_date,
			BalanceTypeMst balance_type,
			HandlingMst handling_mst,
			IdealDepositMst ideal_deposit_mst,
			ItemMst item,
			String detail_mst,
			Integer amount,
			Integer price,
			Integer quantity,
			Date debit_date,
			String content,
			String store,
			String remarks,
			String secret_remarks,
			Boolean secret_rec_flg,
			Integer remainder
			) {
		this.ha_user = ha_user;
		this.payment_date = payment_date;
		this.balance_type_mst = balance_type;
		this.handling_mst = handling_mst;
		this.ideal_deposit_mst = ideal_deposit_mst;
		this.item_mst = item;
		this.detail_mst = detail_mst;
		this.amount = amount;
		this.price = price;
		this.quantity = quantity;
		this.debit_date = debit_date;
		this.content = content;
		this.store = store;
		this.remarks = remarks;
		this.secret_remarks = secret_remarks;
		this.secret_rec_flg = secret_rec_flg;
		this.remainder = remainder;
	}
	
	public String toString() {
		String sRtn = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		try {
			sRtn = sdf.format(payment_date) + " " + balance_type_mst.balance_type_name;
		} catch (Exception e) {
			// TODO: handle exception
		}
        return sRtn;
	}
	
	/**
	 * 「収支種類が」収入・支出・口座預入・口座引出の時は「取扱(実際)」は必須
	 * @author sakashushu
	 *
	 */
	static class HandlingConditionallyRequiredCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			Record record = (Record)validatedObject;
			if(record.balance_type_mst!=null) {
				if((record.balance_type_mst.balance_type_name.equals(Messages.get("BalanceType.in")) ||
						record.balance_type_mst.balance_type_name.equals(Messages.get("BalanceType.out")) ||
						record.balance_type_mst.balance_type_name.equals(Messages.get("BalanceType.bank_in")) ||
						record.balance_type_mst.balance_type_name.equals(Messages.get("BalanceType.bank_out"))) &&
						(record.handling_mst==null)) {
					setMessage(Messages.get("validation.required"));
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * 「収支種類」がMy貯金預入・My貯金引出の時は「取扱(My貯金)」は必須
	 * @author sakashushu
	 *
	 */
	static class IdealDepositConditionallyRequiredCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			Record record = (Record)validatedObject;
			if(record.balance_type_mst!=null) {
				if((record.balance_type_mst.balance_type_name.equals(Messages.get("BalanceType.ideal_deposit_in")) ||
						record.balance_type_mst.balance_type_name.equals(Messages.get("BalanceType.ideal_deposit_out"))) &&
						(record.ideal_deposit_mst==null)) {
					setMessage(Messages.get("validation.required"));
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * 「取扱(実際)」が現金以外の時は「引落日」は必須
	 * @author sakashushu
	 *
	 */
	static class DebitDateConditionallyRequiredCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			Record record = (Record)validatedObject;
			if(record.handling_mst!=null) {
				if(!record.handling_mst.handling_type_mst.handling_type_name.equals(Messages.get("HandlingType.cash")) &&
						(record.debit_date==null)) {
					setMessage(Messages.get("validation.required"));
					return false;
				}
			}
			return true;
		}
	}
}
