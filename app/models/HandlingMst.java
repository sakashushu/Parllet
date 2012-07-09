package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class HandlingMst extends Model {
	
	@Required
	@ManyToOne
	public HaUser ha_user;						//家計簿ユーザー
	
	@Required
	@ManyToOne
	public HandlingTypeMst handling_type_mst;	//取扱種類

	@Required
	public String handling_name;				//取扱名
	
	@ManyToOne
	public HandlingMst handling_bank;			//取扱(口座)

	public String cutoff_date;					//締日
	public String debit_month;					//引落月
	public String debit_day;					//引落日

	public HandlingMst(
			HaUser ha_user,
			HandlingTypeMst handling_type_mst,
			String handling_name,
			HandlingMst handling_bank,
			String cutoff_date,
			String debit_month,
			String debit_day
			) {
		this.ha_user = ha_user;
		this.handling_type_mst = handling_type_mst;
		this.handling_name = handling_name;
		this.handling_bank = handling_bank;
		this.cutoff_date = cutoff_date;
		this.debit_month = debit_month;
		this.debit_day = debit_day;
	}

	public String toString() {
        return handling_name;
	}
}
