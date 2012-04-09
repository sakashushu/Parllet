package models;

import java.util.Date;

public class RecPaymentDate {
	public Long id;
	public String payment_date;
	
	public RecPaymentDate() {}
	
	public RecPaymentDate(Long id, String payment_date) {
		this.id = id;
		this.payment_date= payment_date;
	}
}
