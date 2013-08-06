package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class LevelMst extends Model {
	
	public Integer level;
	public String level_name;
	public Integer month_amount;
	public Integer rec_size	;

	public LevelMst(
			Integer level,
			String level_name,
			Integer month_amount,
			Integer rec_size
			) {
		this.level = level;
		this.level_name = level_name;
		this.month_amount = month_amount;
		this.rec_size = rec_size;
	}
	
	public String toString() {
        return level_name;
	}
}
