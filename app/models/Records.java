package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Records extends Model {

	public Date payment_date;
	public String payment_time;
	public Integer item_id;
	public String item_name;
	
	public Records(
			Date payment_date,
			String payment_time,
			Integer item_id,
			String item_name) {
		this.payment_date = payment_date;
		this.payment_time = payment_time;
		this.item_id = item_id;
		this.item_name = item_name;
	}
}
