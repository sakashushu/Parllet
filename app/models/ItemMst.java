package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class ItemMst extends Model {

	@ManyToOne
	public ActualTypeMst actual_type_mst;
	
	public String item_name;
	
	public ItemMst(
			ActualTypeMst actual_type_mst,
			String item_name
			) {
		this.actual_type_mst = actual_type_mst;
		this.item_name = item_name;
	}
	
	public String toString() {
        return item_name;
	}
}
