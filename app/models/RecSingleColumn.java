package models;

import java.util.Date;

public class RecSingleColumn {
	public String act_type;
	public String edited_col;
	public Long id;
	public String col_val;
	
	public RecSingleColumn() {}
	
	public RecSingleColumn(String act_type, String edited_col, Long id, String col_val) {
		this.act_type = act_type;
		this.edited_col = edited_col;
		this.id = id;
		this.col_val= col_val;
	}
}
