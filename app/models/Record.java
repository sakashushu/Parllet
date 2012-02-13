package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Record extends Model {

	public Date payment_date;
	public int item_id;
	public String item_name;
	
	public Record(
			Date payment_date,
			int item_id,
			String item_name) {
		this.payment_date = payment_date;
		this.item_id = item_id;
		this.item_name = item_name;
	}
}
