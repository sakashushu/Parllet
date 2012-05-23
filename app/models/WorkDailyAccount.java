package models;

import java.math.BigInteger;

public class WorkDailyAccount {
	private String sActualType;
	private String sItem;
	private BigInteger biSumMonth;
	private BigInteger[] biAryDays;
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
	public BigInteger getBiSumMonth() {
		return biSumMonth;
	}
	public void setBiSumMonth(BigInteger biSumMonth) {
		this.biSumMonth = biSumMonth;
	}
	public BigInteger[] getBiAryDays() {
		return biAryDays;
	}
	public void setBiAryDays(BigInteger[] biAryDays) {
		this.biAryDays = biAryDays;
	}
}
