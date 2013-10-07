package models;

import java.math.BigInteger;
import java.util.Date;

public class WkDlRmRec {
	private Long lngId;
	private Boolean bolSecretRecFlg;
	private String strDebitDate;
	private String strPaymentDate;
	private String strBalanceTypeName;
	private String strHandlingName;
	private String strParlletName;
	private Long lngAmount;
	private String strStore;
	private Long lngRemainder;
	private String strPaymentDateOrder;
	private Long lngBalanceTypeId;  		/* 絞込収支種類ID */
	private Long lngHandlingId;			/* 絞込取扱(実際)ID */
	private Long lngParlletId;				/* 絞込取扱(Parllet)ID */
	public Long getLngId() {
		return lngId;
	}
	public void setLngId(Long lngId) {
		this.lngId = lngId;
	}
	public Boolean getBolSecretRecFlg() {
		return bolSecretRecFlg;
	}
	public void setBolSecretRecFlg(Boolean bolSecretRecFlg) {
		this.bolSecretRecFlg = bolSecretRecFlg;
	}
	public String getStrDebitDate() {
		return strDebitDate;
	}
	public void setStrDebitDate(String strDebitDate) {
		this.strDebitDate = strDebitDate;
	}
	public String getStrPaymentDate() {
		return strPaymentDate;
	}
	public void setStrPaymentDate(String strPaymentDate) {
		this.strPaymentDate = strPaymentDate;
	}
	public String getStrBalanceTypeName() {
		return strBalanceTypeName;
	}
	public void setStrBalanceTypeName(String strBalanceTypeName) {
		this.strBalanceTypeName = strBalanceTypeName;
	}
	public String getStrHandlingName() {
		return strHandlingName;
	}
	public void setStrHandlingName(String strHandlingName) {
		this.strHandlingName = strHandlingName;
	}
	public String getStrParlletName() {
		return strParlletName;
	}
	public void setStrParlletName(String strParlletName) {
		this.strParlletName = strParlletName;
	}
	public Long getLngAmount() {
		return lngAmount;
	}
	public void setLngAmount(Long lngAmount) {
		this.lngAmount = lngAmount;
	}
	public String getStrStore() {
		return strStore;
	}
	public void setStrStore(String strStore) {
		this.strStore = strStore;
	}
	public Long getLngRemainder() {
		return lngRemainder;
	}
	public void setLngRemainder(Long lngRemainder) {
		this.lngRemainder = lngRemainder;
	}
	public String getStrPaymentDateOrder() {
		return strPaymentDateOrder;
	}
	public void setStrPaymentDateOrder(String strPaymentDateOrder) {
		this.strPaymentDateOrder = strPaymentDateOrder;
	}
	public Long getLngBalanceTypeId() {
		return lngBalanceTypeId;
	}
	public void setLngBalanceTypeId(Long lngBalanceTypeId) {
		this.lngBalanceTypeId = lngBalanceTypeId;
	}
	public Long getLngHandlingId() {
		return lngHandlingId;
	}
	public void setLngHandlingId(Long lngHandlingId) {
		this.lngHandlingId = lngHandlingId;
	}
	public Long getLngParlletId() {
		return lngParlletId;
	}
	public void setLngParlletId(Long lngParlletId) {
		this.lngParlletId = lngParlletId;
	}
}
