package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class HandlingTypeMst extends Model {

	public String handling_type_name;
	public Integer handling_type_order;
	
	public HandlingTypeMst(
			String handling_type_name,
			Integer handling_type_order
			) {
		this.handling_type_name = handling_type_name;
		this.handling_type_order = handling_type_order;
	}

	public String toString() {
        return handling_type_name;
	}
}
