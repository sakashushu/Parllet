package models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

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
	@Required
	@ManyToOne
	public ItemMst item_mst;					//項目
	@MaxSize(40)
	public String detail_mst;					//項目詳細
	@Required
	public int amount;							//金額
	public int price;							//単価
	public int quantity;						//数量
	@ManyToOne
	@Required
	public HandlingMst handling_mst;			//取扱
	public Date debit_date;						//引落日
	@MaxSize(100)
	public String content;						//内容
	@MaxSize(40)
	public String store;						//お店
	@MaxSize(10000)
	public String remarks;						//備考
	@MaxSize(10000)
	public String secret_remarks;				//備考（非公開）
	@ManyToOne
	public IdealDepositMst ideal_deposit_mst;	//マイ貯金
	
	public Record(
			HaUser ha_user,
			Date payment_date,
			BalanceTypeMst balance_type,
			ItemMst item,
			String detail_mst,
			int amount,
			int price,
			int quantity,
			HandlingMst handling_mst,
			Date debit_date,
			String content,
			String store,
			String remarks,
			String secret_remarks,
			IdealDepositMst ideal_deposit_mst
			) {
		this.ha_user = ha_user;
		this.payment_date = payment_date;
		this.balance_type_mst = balance_type;
		this.item_mst = item;
		this.detail_mst = detail_mst;
		this.amount = amount;
		this.price = price;
		this.quantity = quantity;
		this.handling_mst = handling_mst;
		this.debit_date = debit_date;
		this.content = content;
		this.store = store;
		this.remarks = remarks;
		this.secret_remarks = secret_remarks;
		this.ideal_deposit_mst = ideal_deposit_mst;
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
}
