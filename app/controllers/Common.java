package controllers;

import java.text.SimpleDateFormat;
import java.text.DateFormat;

import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;

import models.BalanceTypeMst;
import models.HaUser;
import models.HandlingMst;
import models.HandlingTypeMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.WkCmMkHdlgRslt;
import models.WkCmMkIdepoRslt;
import models.WkCmMkItemRslt;
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
	
	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("haUser", haUser);
		}
	}
	
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
	
	/**
	 * HandlingMstの保存メソッド
	 * @param id
	 * @param handling_name
	 * @param zero_hidden
	 * @param refHandlingMst
	 * @param sHandlingType
	 * @return
	 */
	public static Integer handling_mst_save(
			Long id,
			String handling_name,
			Boolean zero_hidden,
			Boolean invalidity_flg,
			RefHandlingMst refHandlingMst,
			String sHandlingType
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		HandlingTypeMst handlingTypeMst = HandlingTypeMst.find("byHandling_type_name", sHandlingType).first();
		Integer iCutoffDay = null;
		Integer iDebitDay = null;
		if(id == null) {
			// 取扱データの作成
			String sql = "SELECT COALESCE(MAX(order_seq), 0) FROM HandlingMst WHERE ha_user_id = ?1 AND handling_type_mst_id = ?2";
			Integer intMaxOrderSeq = (Integer)JPA.em().createNativeQuery(sql).setParameter(1, haUser.id).setParameter(2, handlingTypeMst.id).getSingleResult() + 1;
			refHandlingMst.handlingMst = new HandlingMst(
					haUser,
					handlingTypeMst,
					handling_name,
					null,
					iCutoffDay,
					null,
					iDebitDay,
					zero_hidden==null ? false : (zero_hidden==true ? true : false),
					invalidity_flg==null ? false : (invalidity_flg==true ? true : false),
					intMaxOrderSeq
			);
		} else {
			// 取扱データの読み出し
			refHandlingMst.handlingMst = HandlingMst.findById(id);
			// 編集
			refHandlingMst.handlingMst.handling_name = handling_name;
			refHandlingMst.handlingMst.zero_hidden = zero_hidden==null ? false : (zero_hidden==true ? true : false);
			refHandlingMst.handlingMst.invalidity_flg = invalidity_flg==null ? false : (invalidity_flg==true ? true : false);
		}
		// Validate
		validation.valid(refHandlingMst.handlingMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refHandlingMst.handlingMst.save();
		
		return 0;
	}
	
	/**
	 * HandlingMstの保存メソッド（クレジットカード用）
	 * @param id
	 * @param handling_name
	 * @param zero_hidden
	 * @param refHandlingMst
	 * @param sHandlingType
	 * @return
	 */
	public static Integer handling_mst_save(
			Long id,
			String handling_name,
			Boolean zero_hidden,
			Boolean invalidity_flg,
			RefHandlingMst refHandlingMst,
			String sHandlingType,
			Long debit_bank,
			Integer cutoff_day,
			String debit_month,
			Integer debit_day
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		HandlingTypeMst handlingTypeMst = HandlingTypeMst.find("byHandling_type_name", sHandlingType).first();
		HandlingMst debitBank = null;
		if(debit_bank!=null) {
			debitBank = HandlingMst.findById(debit_bank);
		}
		String sql = " SELECT COALESCE(MAX(order_seq), 0) FROM HandlingMst WHERE ha_user_id = " + haUser.id + " AND handling_type_mst_id = " + handlingTypeMst.id + " ";
		Integer intMaxOrderSeq = (Integer)JPA.em().createNativeQuery(sql).getSingleResult() + 1;
		if(id == null) {
			// 取扱データの作成
			refHandlingMst.handlingMst = new HandlingMst(
					haUser,
					handlingTypeMst,
					handling_name,
					debitBank,
					cutoff_day,
					debit_month,
					debit_day,
					zero_hidden,
					invalidity_flg==null ? false : (invalidity_flg==true ? true : false),
					intMaxOrderSeq
			);
		} else {
			// 取扱データの読み出し
			refHandlingMst.handlingMst = HandlingMst.findById(id);
			// 編集
			refHandlingMst.handlingMst.handling_name = handling_name;
			refHandlingMst.handlingMst.debit_bank = debitBank;
			refHandlingMst.handlingMst.cutoff_day = cutoff_day;
			refHandlingMst.handlingMst.debit_month = debit_month;
			refHandlingMst.handlingMst.debit_day = debit_day;
			refHandlingMst.handlingMst.zero_hidden = zero_hidden;
			refHandlingMst.handlingMst.invalidity_flg = invalidity_flg==null ? false : (invalidity_flg==true ? true : false);
		}
		// Validate
		validation.valid(refHandlingMst.handlingMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refHandlingMst.handlingMst.save();
		
		return 0;
	}
	
	/**
	 * ItemMstの保存メソッド
	 * @param id
	 * @param item_name
	 * @param refItemMst
	 * @param sBalanceType
	 * @return
	 */
	public static Integer item_mst_save(
			Long id,
			String item_name,
			RefItemMst refItemMst,
			String sBalanceType
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		BalanceTypeMst balanceTypeMst = BalanceTypeMst.find("byBalance_type_name", sBalanceType).first();
		String sql = " SELECT COALESCE(MAX(order_seq), 0) FROM ItemMst WHERE ha_user_id = " + haUser.id + " AND balance_type_mst_id = " + balanceTypeMst.id + " ";
		Integer intMaxOrderSeq = (Integer)JPA.em().createNativeQuery(sql).getSingleResult() + 1;
		if(id == null) {
			// 取扱データの作成
			refItemMst.itemMst = new ItemMst(
					haUser,
					balanceTypeMst,
					item_name,
					intMaxOrderSeq
			);
		} else {
			// 取扱データの読み出し
			refItemMst.itemMst = ItemMst.findById(id);
			// 編集
			refItemMst.itemMst.item_name = item_name;
		}
		// Validate
		validation.valid(refItemMst.itemMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refItemMst.itemMst.save();
		
		return 0;
	}
	
	/**
	 * IdealDepositMstの保存メソッド
	 * @param id
	 * @param ideal_deposit_name
	 * @param zero_hidden
	 * @param refIdealDepositMst
	 * @return
	 */
	public static Integer ideal_deposit_mst_save(
			Long id,
			String ideal_deposit_name,
			Boolean zero_hidden,
			RefIdealDepositMst refIdealDepositMst
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		String sql = " SELECT COALESCE(MAX(order_seq), 0) FROM IdealDepositMst WHERE ha_user_id = " + haUser.id + " ";
		Integer intMaxOrderSeq = (Integer)JPA.em().createNativeQuery(sql).getSingleResult() + 1;
		if(id == null) {
			// My貯金データの作成
			refIdealDepositMst.idealDepositMst = new IdealDepositMst(
					haUser,
					ideal_deposit_name,
					zero_hidden==null ? false : (zero_hidden==true ? true : false),
					intMaxOrderSeq
			);
		} else {
			// My貯金データの読み出し
			refIdealDepositMst.idealDepositMst = IdealDepositMst.findById(id);
			// 編集
			refIdealDepositMst.idealDepositMst.ideal_deposit_name = ideal_deposit_name;
			refIdealDepositMst.idealDepositMst.zero_hidden = zero_hidden==null ? false : (zero_hidden==true ? true : false);
		}
		// Validate
		validation.valid(refIdealDepositMst.idealDepositMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refIdealDepositMst.idealDepositMst.save();
		
		return 0;
	}
	
	/**
	 * HandlingMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	public static class RefHandlingMst {
		HandlingMst handlingMst;
	}

	/**
	 * ItemMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	public static class RefItemMst {
		ItemMst itemMst;
	}
	
	/**
	 * IdealDepositMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	public static class RefIdealDepositMst {
		IdealDepositMst idealDepositMst;
	}
	
	/**
	 * ユーザー設定初期化
	 * @param hu
	 * @throws Exception
	 */
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
	
	/**
	 * ダイアログフォームからHandlingMstの作成
	 * @param strType
	 * @param strName
	 * @param bolZeroHddn
	 */
	public static void makeHdlg(String strType, String strName, boolean bolZeroHddn) {
		WkCmMkHdlgRslt wr = new WkCmMkHdlgRslt();
		String strHandlingType = "";
		if(strType.equals(Messages.get("views.config.cf_bank"))) {
			strHandlingType = HANDLING_TYPE_BANK;
		}
		if(strType.equals(Messages.get("views.config.cf_emoney"))) {
			strHandlingType = HANDLING_TYPE_EMONEY;
		}
		RefHandlingMst refHandlingMst = new RefHandlingMst();
		Common cmn = new Common();
		Integer iRtn = cmn.handling_mst_save(null, strName, bolZeroHddn, false, refHandlingMst, strHandlingType);
		HandlingMst hM = refHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(hM);
			wr.setIntRslt(99);
			wr.setStrErr(Messages.get(validation.errors().get(0).message()));
		} else {
			wr.setIntRslt(0);
		}
		wr.setHlMst(hM);
		renderJSON(wr);
	}
	
	/**
	 * ダイアログフォームからIdealDepositMstの作成
	 * @param strType
	 * @param strName
	 * @param bolZeroHddn
	 */
	public static void makeIdepo(String strName, boolean bolZeroHddn) {
		WkCmMkIdepoRslt wr = new WkCmMkIdepoRslt();
		RefIdealDepositMst refIdealDepositMst = new RefIdealDepositMst();
		Common cmn = new Common();
		Integer iRtn = cmn.ideal_deposit_mst_save(null, strName, bolZeroHddn, refIdealDepositMst);
		IdealDepositMst iDM = refIdealDepositMst.idealDepositMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(iDM);
			wr.setIntRslt(99);
			wr.setStrErr(Messages.get(validation.errors().get(0).message()));
		} else {
			wr.setIntRslt(0);
		}
		wr.setIdMst(iDM);
		renderJSON(wr);
	}
	
	public static void makeItem(String strBalanceType, String strName) {
		WkCmMkItemRslt wr = new WkCmMkItemRslt();
		RefItemMst refItemMst = new RefItemMst();
		Common cmn = new Common();
		Integer iRtn = cmn.item_mst_save(null, strName, refItemMst, strBalanceType);
		ItemMst iM = refItemMst.itemMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(iM);
			wr.setIntRslt(99);
			wr.setStrErr(Messages.get(validation.errors().get(0).message()));
		} else {
			wr.setIntRslt(0);
		}
		wr.setItMst(iM);
		renderJSON(wr);
	}
}