package models;

import java.math.BigInteger;
import java.util.List;

public class WorkDailyAccount {
	private String sLargeCategory;		/* 大分類 */
	private String sItem;				/* 項目 */
	private boolean bBudgetFlg;		/* 予算有無フラグ */
	private long lBudgetId;			/* 予算ID */
	private String sBudgetAmount;		/* 予算金額(表示用) */
	private long lSumMonth;			/* 月計(数値) */
	private String sSumMonth;			/* 月計(表示用) */
	private long[] lAryDays;			/* 日付毎(数値) */
	private String[] sAryDays;			/* 日付毎(表示用) */
	private List<WorkDaToDl> lstWdtd;	/* 日計表から明細表へのリンク時の引渡し項目 */
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
	public long getlBudgetId() {
		return lBudgetId;
	}
	public void setlBudgetId(long lBudgetId) {
		this.lBudgetId = lBudgetId;
	}
	public String getsBudgetAmount() {
		return sBudgetAmount;
	}
	public void setsBudgetAmount(String sBudgetAmount) {
		this.sBudgetAmount = sBudgetAmount;
	}
	public long getlSumMonth() {
		return lSumMonth;
	}
	public void setlSumMonth(long lSumMonth) {
		this.lSumMonth = lSumMonth;
	}
	public long[] getlAryDays() {
		return lAryDays;
	}
	public void setlAryDays(long[] lAryDays) {
		this.lAryDays = lAryDays;
	}
	public long getLSumMonth() {
		return lSumMonth;
	}
	public void setLSumMonth(long lSumMonth) {
		this.lSumMonth = lSumMonth;
	}
	public String getsSumMonth() {
		return sSumMonth;
	}
	public void setsSumMonth(String sSumMonth) {
		this.sSumMonth = sSumMonth;
	}
	public long[] getLAryDays() {
		return lAryDays;
	}
	public void setLAryDays(long[] lAryDays) {
		this.lAryDays = lAryDays;
	}
	public String[] getsAryDays() {
		return sAryDays;
	}
	public void setsAryDays(String[] sAryDays) {
		this.sAryDays = sAryDays;
	}
	public List<WorkDaToDl> getLstWdtd() {
		return lstWdtd;
	}
	public void setLstWdtd(List<WorkDaToDl> lstWdtd) {
		this.lstWdtd = lstWdtd;
	}
}
