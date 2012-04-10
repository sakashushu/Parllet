package models;

import java.util.Date;

public class RecPaymentDate {
	public String act_type;
	public Long id;
	public String payment_date;
	
	public RecPaymentDate() {}
	
	public RecPaymentDate(String act_type, Long id, String payment_date) {
		this.act_type = act_type;
		this.id = id;
		this.payment_date= payment_date;
	}
}
