package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class ItemMst extends Model {

	@ManyToOne
	public ActualTypeMst actual_type_id;
	
	public int item_id;
	public String item_name;
	
	public ItemMst(
			ActualTypeMst actual_type_id,
			int item_id,
			String item_name
			) {
		this.actual_type_id = actual_type_id;
		this.item_id = item_id;
		this.item_name = item_name;
	}
}
