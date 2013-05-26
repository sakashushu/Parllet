package models;

import java.math.BigInteger;
import java.util.List;

public class WkDailyAccount {
	private String sLargeCategory;		/* 大分類 */
	private String sItem;				/* 項目 */
	private boolean bolLastItemFlg;	/* 大分類毎の項目の最終行フラグ */
	private boolean bBudgetFlg;		/* 予算有無フラグ */
	private long lBudgetId;			/* 予算ID */
	private Long lBudgetAmount;		/* 予算金額(数値) */
	private long lSumMonth;			/* 月計 */
	private long lIdepoLink;			/* My貯金連結 */
	private long[] lAryDays;			/* 日付毎 */
	private List<WkDaToDl> lstWdtd;	/* 日計表から明細表へのリンク時の引渡し項目 */
	private String strTypeNm;			/* 種類名 */
	private Boolean bolInvFlg;			/* 無効フラグ */
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
	public boolean isBolLastItemFlg() {
		return bolLastItemFlg;
	}
	public void setBolLastItemFlg(boolean bolLastItemFlg) {
		this.bolLastItemFlg = bolLastItemFlg;
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
	public Long getlBudgetAmount() {
		return lBudgetAmount;
	}
	public void setlBudgetAmount(Long lBudgetAmount) {
		this.lBudgetAmount = lBudgetAmount;
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
	public long getlIdepoLink() {
		return lIdepoLink;
	}
	public void setlIdepoLink(long lIdepoLink) {
		this.lIdepoLink = lIdepoLink;
	}
	public long[] getLAryDays() {
		return lAryDays;
	}
	public void setLAryDays(long[] lAryDays) {
		this.lAryDays = lAryDays;
	}
	public List<WkDaToDl> getLstWdtd() {
		return lstWdtd;
	}
	public void setLstWdtd(List<WkDaToDl> lstWdtd) {
		this.lstWdtd = lstWdtd;
	}
	public String getStrTypeNm() {
		return strTypeNm;
	}
	public void setStrTypeNm(String strTypeNm) {
		this.strTypeNm = strTypeNm;
	}
	public Boolean getBolInvFlg() {
		return bolInvFlg;
	}
	public void setBolInvFlg(Boolean bolInvFlg) {
		this.bolInvFlg = bolInvFlg;
	}
}
