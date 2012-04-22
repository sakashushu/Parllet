package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Record extends Model {

	public Date payment_date;
	public int item_id;
	public String item_name;
	public int detail_id;
	public String detail_name;
	public int amount;
	public String goods;
	public int price;
	public int quantity;
	public int handling_id;
	public String handling_name;
	public Date debit_date;
	public String store;
	public String remarks;
	public String secret_remarks;
	public int balance_type_id;
	public String balance_type_name;
	public int ideal_deposit_id;
	public String ideal_deposit_name;
	
	public Record(
			Date payment_date,
			int item_id,
			String item_name,
			int detail_id,
			String detail_name,
			int amount,
			String goods,
			int price,
			int quantity,
			int handling_id,
			String handling_name,
			Date debit_date,
			String store,
			String remarks,
			String secret_remarks,
			int balance_type_id,
			String balance_type_name,
			int ideal_deposit_id,
			String ideal_deposit_name
			) {
		this.payment_date = payment_date;
		this.item_id = item_id;
		this.item_name = item_name;
		this.detail_id = detail_id;
		this.detail_name = detail_name;
		this.amount = amount;
		this.goods = goods;
		this.price = price;
		this.quantity = quantity;
		this.handling_id = handling_id;
		this.handling_name = handling_name;
		this.debit_date = debit_date;
		this.store = store;
		this.remarks = remarks;
		this.secret_remarks = secret_remarks;
		this.balance_type_id = balance_type_id;
		this.balance_type_name = balance_type_name;
		this.ideal_deposit_id = ideal_deposit_id;
		this.ideal_deposit_name = ideal_deposit_name;
	}
}
