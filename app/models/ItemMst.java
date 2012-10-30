package models;


import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class ItemMst extends Model {

	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	@ManyToOne
	public BalanceTypeMst balance_type_mst;
	
	public String item_name;
	
	public Integer order_seq;
	
	public ItemMst(
			HaUser ha_user,
			BalanceTypeMst balance_type_mst,
			String item_name,
			Integer order_seq
			) {
		this.ha_user = ha_user;
		this.balance_type_mst = balance_type_mst;
		this.item_name = item_name;
		this.order_seq = order_seq;
	}
	
	public String toString() {
        return item_name;
	}
}
