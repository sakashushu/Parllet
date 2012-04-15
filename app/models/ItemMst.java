package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class ItemMst extends Model {

	public int actual_type_id;
	public int item_id;
	public String item_name;
	
	public ItemMst(
			int actual_type_id,
			int item_id,
			String item_name
			) {
		this.actual_type_id = actual_type_id;
		this.item_id = item_id;
		this.item_name = item_name;
	}
}
