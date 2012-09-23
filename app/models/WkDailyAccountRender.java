package models;

import java.math.BigInteger;
import java.util.List;

public class WkDailyAccountRender {
	private int intMonth;   		  
	private String strBasisDate;	  
	private String strTableType;	  
	private String[] strAryDays;	  
	private List<WkDailyAccount> lWDA;
	private int intWidth;
	public int getIntMonth() {
		return intMonth;
	}
	public void setIntMonth(int intMonth) {
		this.intMonth = intMonth;
	}
	public String getStrBasisDate() {
		return strBasisDate;
	}
	public void setStrBasisDate(String strBasisDate) {
		this.strBasisDate = strBasisDate;
	}
	public String getStrTableType() {
		return strTableType;
	}
	public void setStrTableType(String strTableType) {
		this.strTableType = strTableType;
	}
	public String[] getStrAryDays() {
		return strAryDays;
	}
	public void setStrAryDays(String[] strAryDays) {
		this.strAryDays = strAryDays;
	}
	public List<WkDailyAccount> getlWDA() {
		return lWDA;
	}
	public void setlWDA(List<WkDailyAccount> lWDA) {
		this.lWDA = lWDA;
	}
	public int getIntWidth() {
		return intWidth;
	}
	public void setIntWidth(int intWidth) {
		this.intWidth = intWidth;
	}
}
