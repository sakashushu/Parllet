package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Budget extends Model {

	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	@Required
	public Integer year;						//年
	@Required
	public Integer month;						//月
	@Required
	public Integer amount;						//金額
	@ManyToOne
	public ItemMst item_mst;					//項目
	@ManyToOne
	public ParlletMst parllet_mst;				//取扱(Parllet)
	
	public Budget(
			HaUser ha_user,
			Integer year,
			Integer month,
			Integer amount,
			ItemMst item_mst,
			ParlletMst parllet_mst
			) {
		this.ha_user = ha_user;
		this.year = year;
		this.month = month;
		this.amount = amount;
		this.item_mst = item_mst;
		this.parllet_mst = parllet_mst;
	}
	public String toString() {
		String sRtn = "";
		if (item_mst==null) {
			sRtn = year + "/" + month + " " + parllet_mst.parllet_name;
		} else {
			sRtn = year + "/" + month + " " + item_mst.item_name;
		}
        return sRtn;
	}
}
