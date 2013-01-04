package models;


import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.i18n.Messages;

@Entity
public class ItemMst extends Model {

	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	@ManyToOne
	public BalanceTypeMst balance_type_mst;
	
	@Required
	@CheckWith(ItemNameMultipleCheck.class)
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
	
	/**
	 * 「項目名」は同じ取扱種類内で重複登録不可
	 * @author sakashushu
	 *
	 */
	static class ItemNameMultipleCheck extends Check {
		public boolean isSatisfied(Object validatedObject, Object value) {
			ItemMst itemMst = (ItemMst)validatedObject;
			ItemMst imExist = null;
			imExist = ItemMst.find(
					"ha_user = ? and balance_type_mst = ? and item_name = ?",
					itemMst.ha_user,
					itemMst.balance_type_mst,
					itemMst.item_name).first();
			if(imExist!=null && imExist.id!=itemMst.id) {
				setMessage(Messages.get("validation.multipleName"));
				return false;
			}

			return true;
		}
	}

}
