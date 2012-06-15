package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class BalanceTypeMst extends Model {

	public String balance_type_name;

	public BalanceTypeMst(
			String balance_type_name
			) {
		this.balance_type_name = balance_type_name;
	}
	
	public String toString() {
        return balance_type_name;
	}
}
