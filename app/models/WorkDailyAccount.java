package models;

import java.math.BigInteger;

public class WorkDailyAccount {
	private String sLargeCategory;	//大分類
	private String sItem;			//項目
	private boolean bBudgetFlg;	//予算有無フラグ
//	private BigInteger biSumMonth;	//月計(数値)
	private long lSumMonth;		//月計(数値)
	private String sSumMonth;		//月計(表示用)
//	private BigInteger[] biAryDays;	//日付毎(数値)
	private long[] lAryDays;		//日付毎(数値)
	private String[] sAryDays;		//日付毎(表示用)
	public String getsLargeCategory() {
		return sLargeCategory;
	}
	public void setsLargeCategory(String sLargeCategory) {
		this.sLargeCategory = sLargeCategory;
	}
	public String getsItem() {
		return sItem;
	}
	public void setsItem(String sItem) {
		this.sItem = sItem;
	}
	public boolean isbBudgetFlg() {
		return bBudgetFlg;
	}
	public void setbBudgetFlg(boolean bBudgetFlg) {
		this.bBudgetFlg = bBudgetFlg;
	}
//	public BigInteger getBiSumMonth() {
//		return biSumMonth;
//	}
	public long getLSumMonth() {
		return lSumMonth;
	}
//	public void setBiSumMonth(BigInteger biSumMonth) {
//		this.biSumMonth = biSumMonth;
//	}
	public void setLSumMonth(long lSumMonth) {
		this.lSumMonth = lSumMonth;
	}
	public String getsSumMonth() {
		return sSumMonth;
	}
	public void setsSumMonth(String sSumMonth) {
		this.sSumMonth = sSumMonth;
	}
//	public BigInteger[] getBiAryDays() {
//		return biAryDays;
//	}
	public long[] getLAryDays() {
		return lAryDays;
	}
//	public void setBiAryDays(BigInteger[] biAryDays) {
//		this.biAryDays = biAryDays;
//	}
	public void setLAryDays(long[] lAryDays) {
		this.lAryDays = lAryDays;
	}
	public String[] getsAryDays() {
		return sAryDays;
	}
	public void setsAryDays(String[] sAryDays) {
		this.sAryDays = sAryDays;
	}
}
