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
	public HandlingTypeMst handling_type_mst;

	@Required
	public String handling_name;
	
	public HandlingMst(
			HaUser ha_user,
			HandlingTypeMst handling_type_mst,
			String handling_name
			) {
		this.ha_user = ha_user;
		this.handling_type_mst = handling_type_mst;
		this.handling_name = handling_name;
	}

	public String toString() {
        return handling_name;
	}
}
