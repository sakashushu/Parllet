package models;

import java.math.BigInteger;

public class WkDaToDl {
	private long lAmount;				/* 金額 */
	private String sAmount;			/* 金額(表示用) */
	private String sPaymentDateFr;		/* 絞込支払日範囲（開始） */
	private String sPaymentDateTo;		/* 絞込支払日範囲（終了） */
	private String sDebitDateFr;		/* 絞込引落日範囲（開始） */
	private String sDebitDateTo;		/* 絞込引落日範囲（終了） */
	private Long lBalanceTypeId;  		/* 絞込収支種類ID */
	private Long lHandlingId;			/* 絞込取扱(実際)ID */
	private Long lParlletId;			/* 絞込取扱(Parllet)ID */
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
	public String getsDebitDateFr() {
		return sDebitDateFr;
	}
	public void setsDebitDateFr(String sDebitDateFr) {
		this.sDebitDateFr = sDebitDateFr;
	}
	public String getsDebitDateTo() {
		return sDebitDateTo;
	}
	public void setsDebitDateTo(String sDebitDateTo) {
		this.sDebitDateTo = sDebitDateTo;
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
	public Long getlHandlingId() {
		return lHandlingId;
	}
	public void setlHandlingId(Long lHandlingId) {
		this.lHandlingId = lHandlingId;
	}
	public Long getlParlletId() {
		return lParlletId;
	}
	public void setlParlletId(Long lParlleId) {
		this.lParlletId = lParlleId;
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
