package models;

import java.math.BigInteger;

public class WorkDailyAccount {
	private String sActualType;
	private String sItem;
	private String sSumMonth;
	private String[] sAryDays;
	public String getsActualType() {
		return sActualType;
	}
	public void setsActualType(String sActualType) {
		this.sActualType = sActualType;
	}
	public String getsItem() {
		return sItem;
	}
	public void setsItem(String sItem) {
		this.sItem = sItem;
	}
	public String getsSumMonth() {
		return sSumMonth;
	}
	public void setsSumMonth(String sSumMonth) {
		this.sSumMonth = sSumMonth;
	}
	public String[] getsAryDays() {
		return sAryDays;
	}
	public void setsAryDays(String[] sAryDays) {
		this.sAryDays = sAryDays;
	}
}
