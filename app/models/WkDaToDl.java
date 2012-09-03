package models;

import java.math.BigInteger;

public class WkDaToDl {
	private long lAmount;				/* 金額 */
	private String sAmount;			/* 金額(表示用) */
	private String sPaymentDateFr;		/* 絞込日時範囲（開始） */
	private String sPaymentDateTo;		/* 絞込日時範囲（終了） */
	private Long lBalanceTypeId;  		/* 絞込収支種類ID */
	private Long lIdealDepositId;		/* 絞込取扱(My貯金)ID */
	private Long lItemId;				/* 絞込項目ID */
	public long getlAmount() {
		return lAmount;
	}
	public void setlAmount(long lAmount) {
		this.lAmount = lAmount;
	}
	public String getsAmount() {
		return sAmount;
	}
	public void setsAmount(String sAmount) {
		this.sAmount = sAmount;
	}
	public String getsPaymentDateFr() {
		return sPaymentDateFr;
	}
	public void setsPaymentDateFr(String sPaymentDateFr) {
		this.sPaymentDateFr = sPaymentDateFr;
	}
	public String getsPaymentDateTo() {
		return sPaymentDateTo;
	}
	public void setsPaymentDateTo(String sPaymentDateTo) {
		this.sPaymentDateTo = sPaymentDateTo;
	}
	public Long getlBalanceTypeId() {
		return lBalanceTypeId;
	}
	public void setlBalanceTypeId(Long lBalanceTypeId) {
		this.lBalanceTypeId = lBalanceTypeId;
	}
	public Long getlIdealDepositId() {
		return lIdealDepositId;
	}
	public void setlIdealDepositId(Long lIdealDepositId) {
		this.lIdealDepositId = lIdealDepositId;
	}
	public Long getlItemId() {
		return lItemId;
	}
	public void setlItemId(Long lItemId) {
		this.lItemId = lItemId;
	}
	public Long getiItemId() {
		return lItemId;
	}
	public void setiItemId(Long lItemId) {
		this.lItemId = lItemId;
	}
	
}
