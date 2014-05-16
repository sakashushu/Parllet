package models;

import play.data.validation.Validation;

public class WkCmRecRslt {
	private int intRslt;
	private String strErr;
	private Record rec;
	private Validation validation;
	public int getIntRslt() {
		return intRslt;
	}
	public void setIntRslt(int intRslt) {
		this.intRslt = intRslt;
	}
	public String getStrErr() {
		return strErr;
	}
	public void setStrErr(String strErr) {
		this.strErr = strErr;
	}
	public Record getRec() {
		return rec;
	}
	public void setRec(Record rec) {
		this.rec = rec;
	}
	public Validation getValidation() {
		return validation;
	}
	public void setValidation(Validation validation) {
		this.validation = validation;
	}
}
