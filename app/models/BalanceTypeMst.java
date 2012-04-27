package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class BalanceTypeMst extends Model {

	public String balance_type_name;

	@ManyToOne
	public ActualTypeMst actual_type_mst;
	
	public BalanceTypeMst(
			String balance_type_name,
			ActualTypeMst actual_type_mst
			) {
		this.balance_type_name = balance_type_name;
		this.actual_type_mst = actual_type_mst;
	}
	
	public String toString() {
        return balance_type_name;
	}
}
