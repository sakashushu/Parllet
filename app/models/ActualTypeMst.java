package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class ActualTypeMst extends Model {

	public int actual_type_id;
	public String actual_type_name;
	
	public ActualTypeMst(
			int actual_type_id,
			String actual_type_name
			) {
		this.actual_type_id = actual_type_id;
		this.actual_type_name = actual_type_name;
	}
}
