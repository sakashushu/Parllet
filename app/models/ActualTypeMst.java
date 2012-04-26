package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class ActualTypeMst extends Model {

	public String actual_type_name;
	
	public ActualTypeMst(
			String actual_type_name
			) {
		this.actual_type_name = actual_type_name;
	}
}
