package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class HandlingTypeMst extends Model {

	public String handling_type_name;
	
	public HandlingTypeMst(
			String handling_type_name
			) {
		this.handling_type_name = handling_type_name;
	}

	public String toString() {
        return handling_type_name;
	}
}
