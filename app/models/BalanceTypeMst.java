package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class BalanceTypeMst extends Model {

	public int balance_type_id;  //不要？
	public String balance_type_name;

	@ManyToOne
	public ActualTypeMst actual_type_id;
	
	public BalanceTypeMst(
			int balance_type_id,
			String balance_type_name,
			ActualTypeMst actual_type_id
			) {
		this.balance_type_id = balance_type_id;
		this.balance_type_name = balance_type_name;
		this.actual_type_id = actual_type_id;
	}
}
