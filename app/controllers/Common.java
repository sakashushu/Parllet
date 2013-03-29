package controllers;

import java.text.SimpleDateFormat;
import java.text.DateFormat;

import play.i18n.Messages;
import play.mvc.Controller;

import models.BalanceTypeMst;
import models.HaUser;
import models.HandlingMst;
import models.HandlingTypeMst;
import models.ItemMst;
import models.WkSyEsFbUsRslt;

public class Common extends Controller {
	static final String BALANCE_TYPE_IN = Messages.get("BalanceType.in");
	static final String BALANCE_TYPE_OUT = Messages.get("BalanceType.out");
	static final String BALANCE_TYPE_BANK_IN = Messages.get("BalanceType.bank_in");
	static final String BALANCE_TYPE_BANK_OUT = Messages.get("BalanceType.bank_out");
	static final String BALANCE_TYPE_IDEAL_DEPOSIT_IN = Messages.get("BalanceType.ideal_deposit_in");
	static final String BALANCE_TYPE_IDEAL_DEPOSIT_OUT = Messages.get("BalanceType.ideal_deposit_out");
	static final String BALANCE_TYPE_OUT_IDEAL_DEPOSIT = Messages.get("BalanceType.out_ideal_deposit");
	static final String REMAINDER_TYPE_REAL = Messages.get("RemainderType.real");
	static final String REMAINDER_TYPE_IDEAL_DEPOSIT = Messages.get("RemainderType.ideal_deposit");
	static final String REMAINDER_TYPE_NOT_IDEAL_DEPOSIT = Messages.get("RemainderType.not_ideal_deposit");
	static final String HANDLING_TYPE_CASH = Messages.get("HandlingType.cash");
	static final String HANDLING_TYPE_BANK = Messages.get("HandlingType.bank");
	static final String HANDLING_TYPE_EMONEY = Messages.get("HandlingType.emoney");
	static final String HANDLING_TYPE_CRECA = Messages.get("HandlingType.creca");
	static final String ITEM_IN_SALARY = Messages.get("Item.in.salary");
	static final String ITEM_IN_OTHER = Messages.get("Item.in.other");
	static final String ITEM_OUT_FOOD = Messages.get("Item.out.food");
	static final String ITEM_OUT_DAILYRESIDENCE = Messages.get("Item.out.dailyResidence");
	static final String ITEM_OUT_UTILITIES = Messages.get("Item.out.utilities");
	static final String ITEM_OUT_CLOTHING = Messages.get("Item.out.clothing");
	static final String ITEM_OUT_HEALTH = Messages.get("Item.out.health");
	static final String ITEM_OUT_EDUCATION = Messages.get("Item.out.education");
	static final String ITEM_OUT_RECREATION = Messages.get("Item.out.recreation");
	static final String ITEM_OUT_TRANSPORTATIONANDCOMMUNICATION = Messages.get("Item.out.transportationAndCommunication");
	static final String ITEM_OUT_OTHER = Messages.get("Item.out.other");
	static final String VIEWS_DAILY_ACCOUNT = Messages.get("views.dailyaccount.dailyaccount");
	static final String VIEWS_BALANCE_TABLE = Messages.get("views.dailyaccount.balancetable");
	
	/**
	 * 日付数値の妥当性チェック
	 * @param intDate
	 * @return 存在する日付の場合true
	 */
	public boolean checkIntDate(Integer intDate) {
			if(intDate<10000101 ||
					intDate>99991231)
				return false;
				
			String strTmp = intDate.toString();
			String sBasisDate = strTmp.substring(0, 4) + "/" + strTmp.substring(4, 6) + "/" + strTmp.substring(6);
			//日付の妥当性チェック
			return checkDate(sBasisDate);
	}
	
	/**
	 * 日付の妥当性チェックを行います。
	 * 指定した日付文字列（yyyy/MM/dd or yyyy-MM-dd）が
	 * カレンダーに存在するかどうかを返します。
	 * @param strDate チェック対象の文字列
	 * @return 存在する日付の場合true
	 */
	public boolean checkDate(String strDate) {
		if (strDate == null || strDate.length() != 10)
			return false;
		strDate = strDate.replace('-', '/');
//		DateFormat format = DateFormat.getDateInstance();
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		// 日付/時刻解析を厳密に行うかどうかを設定する。
		format.setLenient(false);
		try {
			format.parse(strDate);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void initUserConf(HaUser hu) throws Exception {
		HandlingMst hm;
		BalanceTypeMst bm;
		HandlingTypeMst htm;
		ItemMst im;
		
		/** 取扱（実際） **/
		
		//現金
		htm = HandlingTypeMst.find("byHandling_type_name", HANDLING_TYPE_CASH).first();
		hm = new HandlingMst(hu, htm, HANDLING_TYPE_CASH, null, null, null, null, false, false, 1);
		hm.save();
		
		
		/** 項目 **/
		
		/* 収入 */
		bm = BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_IN).first();
		//給与
		im = new ItemMst(hu, bm, ITEM_IN_SALARY, 1);
		im.save();
		//その他収入
		im = new ItemMst(hu, bm, ITEM_IN_OTHER, 2);
		im.save();
		
		/* 支出 */
		bm = BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT).first();
		//食費
		im = new ItemMst(hu, bm, ITEM_OUT_FOOD, 1);
		im.save();
		//居住日用
		im = new ItemMst(hu, bm, ITEM_OUT_DAILYRESIDENCE, 3);
		im.save();
		//水道光熱
		im = new ItemMst(hu, bm, ITEM_OUT_UTILITIES, 4);
		im.save();
		//衣類
		im = new ItemMst(hu, bm, ITEM_OUT_CLOTHING, 5);
		im.save();
		//健康
		im = new ItemMst(hu, bm, ITEM_OUT_HEALTH, 6);
		im.save();
		//教育
		im = new ItemMst(hu, bm, ITEM_OUT_EDUCATION, 7);
		im.save();
		//娯楽
		im = new ItemMst(hu, bm, ITEM_OUT_RECREATION, 8);
		im.save();
		//交通通信
		im = new ItemMst(hu, bm, ITEM_OUT_TRANSPORTATIONANDCOMMUNICATION, 9);
		im.save();
		//その他
		im = new ItemMst(hu, bm, ITEM_OUT_OTHER, 10);
		im.save();
	}
	
	/**
	 * Facebookと連携する
	 * @param id
	 */
	public static void linkFacebook(Long id, String name, String link) {
		WkSyEsFbUsRslt wr = new WkSyEsFbUsRslt();
		HaUser hU = HaUser.find("byEmail", Security.connected()).first();
		hU.fbId = id;
		hU.fbName = name;
		hU.fbLink = link;
		validation.valid(hU);
		if(validation.hasErrors()) {
			wr.setIntRslt(99);
			wr.setStrErr(Messages.get(validation.errors().get(0).message()));
			renderJSON(wr);
		}
		hU.save();
		wr.setIntRslt(0);
		renderJSON(wr);
	}
	
	/**
	 * Facebookとの連携を解除する
	 * @param id
	 */
	public static void breakLinkFacebook() {
		WkSyEsFbUsRslt wr = new WkSyEsFbUsRslt();
		HaUser hU = HaUser.find("byEmail", Security.connected()).first();
		hU.fbId = null;
		hU.fbName = null;
		hU.fbLink = null;
		validation.valid(hU);
		if(validation.hasErrors()) {
			wr.setIntRslt(99);
			wr.setStrErr(Messages.get(validation.errors().get(0).message()));
			renderJSON(wr);
		}
		hU.save();
		wr.setIntRslt(0);
		renderJSON(wr);
	}
	
}
