package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class DetailMst extends Model {

	@ManyToOne
	public ItemMst item_id;

	public int detail_id;
	public String detail_name;
	
	public DetailMst(
			ItemMst item_id,
			int detail_id,
			String detail_name
			) {
		this.item_id = item_id;
		this.detail_id = detail_id;
		this.detail_name = detail_name;
	}
}
