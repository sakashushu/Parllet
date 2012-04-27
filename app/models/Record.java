package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Record extends Model {

	public Date payment_date;
	@ManyToOne
	public ItemMst item_mst;
	public String item_name;  //不要？
	public int detail_id;
	public String detail_name;  //不要？
	public int amount;
	public String content;
	public int price;
	public int quantity;
	public int handling_id;
	public String handling_name;
	public Date debit_date;
	public String store;
	public String remarks;
	public String secret_remarks;
	@ManyToOne
	public BalanceTypeMst balance_type_mst;
	public String balance_type_name;  //不要？
	public int ideal_deposit_id;
	public String ideal_deposit_name;
	
	public Record(
			Date payment_date,
			ItemMst item,
			String item_name,
			int detail_id,
			String detail_name,
			int amount,
			String content,
			int price,
			int quantity,
			int handling_id,
			String handling_name,
			Date debit_date,
			String store,
			String remarks,
			String secret_remarks,
			BalanceTypeMst balance_type,
			String balance_type_name,
			int ideal_deposit_id,
			String ideal_deposit_name
			) {
		this.payment_date = payment_date;
		this.item_mst = item;
		this.item_name = item_name;
		this.detail_id = detail_id;
		this.detail_name = detail_name;
		this.amount = amount;
		this.content = content;
		this.price = price;
		this.quantity = quantity;
		this.handling_id = handling_id;
		this.handling_name = handling_name;
		this.debit_date = debit_date;
		this.store = store;
		this.remarks = remarks;
		this.secret_remarks = secret_remarks;
		this.balance_type_mst = balance_type;
		this.balance_type_name = balance_type_name;
		this.ideal_deposit_id = ideal_deposit_id;
		this.ideal_deposit_name = ideal_deposit_name;
	}
}
