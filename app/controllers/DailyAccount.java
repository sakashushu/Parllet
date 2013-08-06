package controllers;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import models.BalanceTypeMst;
import models.Budget;
import models.HaUser;
import models.LevelMst;
import models.ParlletMst;
import models.ItemMst;
import models.WkDaToDl;
import models.WkDailyAccount;
import models.WkDailyAccountRender;

import play.Logger;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class DailyAccount extends Controller {

	@Before
	static void setConnectedUser() {
		if(!Security.isConnected())
			return;
		
		HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
		renderArgs.put("haUser", haUser);
	}
	
	
	/**
	 * 日計表
	 * @param strBasisDate
	 */
	public static void dailyAccount(
			String strBasisDate
			) {
		DailyAccount da = new DailyAccount();
		//単純に呼ばれた時の基準日のセット
		if(strBasisDate==null)
			strBasisDate = da.setBasisDate();
		
		Integer intBasisDate = null; 
		if(strBasisDate!=null)
			intBasisDate = Integer.parseInt(strBasisDate.replace("/", ""));
		dailyAccountDisp(intBasisDate);
	}
	
	/**
	 * 残高表
	 * @param strBasisDate
	 */
	public static void balanceTable(
			String strBasisDate
			) {
		DailyAccount da = new DailyAccount();
   		//単純に呼ばれた時の基準日のセット(残高表)
		if(strBasisDate==null)
			strBasisDate = da.setBasisDateBt();

		Integer intBasisDate = null;
		if(strBasisDate!=null)
			intBasisDate = Integer.parseInt(strBasisDate.replace("/", ""));
		balanceTableDisp(intBasisDate);
	}
	
	/**
	 * 日計表の表示
	 * @param intBasisDate
	 */
	public static void dailyAccountDisp(
			Integer intBasisDate
			) {
		String sBasisDate = null;
		DailyAccount da = new DailyAccount();
		Common cm = new Common();
		//単純に呼ばれた時の基準日のセット
		if(intBasisDate==null) {
			sBasisDate = da.setBasisDate();
		} else {
			if(cm.checkIntDate(intBasisDate)) {
				String strTmp = intBasisDate.toString();
				sBasisDate = strTmp.substring(0, 4) + "/" + strTmp.substring(4, 6) + "/" + strTmp.substring(6, 8);
			} else {
				validation.addError("dateError", Messages.get("validation.dateError"));
				sBasisDate = da.setBasisDate();
			}
				
		}
//		日計表で1ヶ月分表示していた時のコード
//		String strDispDate = sBasisDate;
//		if (!sBasisDate.substring(sBasisDate.length()-2).equals("01")) {
//			sBasisDate = sBasisDate.substring(0, sBasisDate.length()-2) + "01";
//		}
		
		String strTableType = Common.VIEWS_DAILY_ACCOUNT;

		//日計表・残高表の表示用ワーク作成
		WkDailyAccountRender wkDAR = makeWkDAR(sBasisDate, strTableType);
		
		int month = wkDAR.getIntMonth();
//		sBasisDate = wkDAR.getStrBasisDate();
		String[] sAryDays = wkDAR.getStrAryDays();
		List<WkDailyAccount> lWDA = wkDAR.getlWDA();
		int iWidth = wkDAR.getIntWidth();
		
//		render("@dailyAccount",  month, sBasisDate, strDispDate, strTableType, sAryDays, lWDA, iWidth);
		render("@dailyAccount",  month, sBasisDate, strTableType, sAryDays, lWDA, iWidth);
	}
	
	/**
	 * 残高表の表示
	 * @param sBasisDate
	 */
	public static void balanceTableDisp(
			Integer intBasisDate
			) {
		String sBasisDate = null;
		DailyAccount da = new DailyAccount();
		Common cm = new Common();
		//単純に呼ばれた時の基準日のセット(残高表)
		if(intBasisDate==null) {
			sBasisDate = da.setBasisDateBt();
		} else {
			if(cm.checkIntDate(intBasisDate)) {
				String strTmp = intBasisDate.toString();
				sBasisDate = strTmp.substring(0, 4) + "/" + strTmp.substring(4, 6) + "/" + strTmp.substring(6, 8);
			} else {
				validation.addError("dateError", Messages.get("validation.dateError"));
				sBasisDate = da.setBasisDateBt();
			}
				
		}
		
		String strTableType = Common.VIEWS_BALANCE_TABLE;

		//日計表・残高表の表示用ワーク作成
		WkDailyAccountRender wkDAR = makeWkDAR(sBasisDate, strTableType);
		
		int month = wkDAR.getIntMonth();
		sBasisDate = wkDAR.getStrBasisDate();
		String[] sAryDays = wkDAR.getStrAryDays();
		List<WkDailyAccount> lWDA = wkDAR.getlWDA();
		int iWidth = wkDAR.getIntWidth();
		
		render("@balanceTable",  month, sBasisDate, strTableType, sAryDays, lWDA, iWidth);
	}
	
	/**
	 * 日計表・残高表の表示用ワーク作成
	 * @param strBasisDate
	 * @param strTableType
	 * @return
	 */
	public static WkDailyAccountRender makeWkDAR(
			String strBasisDate,
			String strTableType
			) {

		WkDailyAccountRender wkDAR = new WkDailyAccountRender();
   		Calendar calendar = Calendar.getInstance();
   		
   		wkDAR.setStrBasisDate(strBasisDate);
   		
		//検索条件をセッションに保存
		if(strTableType.equals(Common.VIEWS_DAILY_ACCOUNT)) {
			session.put("daFilExistFlg", "true");
			session.put("daStrBasisDate", strBasisDate);
		}
		if(strTableType.equals(Common.VIEWS_BALANCE_TABLE)) {
			session.put("btFilExistFlg", "true");
			session.put("btStrBasisDate", strBasisDate);
		}
		
		Date dBasis = null;
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		try {
			dBasis = df.parse(strBasisDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calendar.setTime(dBasis);
		int year = calendar.get(Calendar.YEAR);
		wkDAR.setIntMonth(calendar.get(Calendar.MONTH) + 1);

		int iDaysCnt = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		if(strTableType.equals(Common.VIEWS_DAILY_ACCOUNT)) {
			//日計表の表示日数
			iDaysCnt = 5;
			calendar.add(Calendar.DATE, -2);
		}
		if(strTableType.equals(Common.VIEWS_BALANCE_TABLE)) {
			iDaysCnt = 1;
		}
		
   		
		//日計表のヘッダーの日付の配列
		String[] strAryDays = new String[iDaysCnt];
		Date dStartDay = calendar.getTime();
		SimpleDateFormat sdf1 = new SimpleDateFormat("M/d");
		
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			strAryDays[iDay] = sdf1.format(calendar.getTime());
			calendar.add(Calendar.DATE, 1);
		}
		wkDAR.setStrAryDays(strAryDays);
		

		DailyAccount da = new DailyAccount();
		
		//日計表の行に相当するリストの作成
		wkDAR.setlWDA(da.makeWorkList(year, wkDAR.getIntMonth(), dStartDay, iDaysCnt, strTableType));
   		
   		//日計表の日付ごとの部分のスクロール内の幅の設定
		wkDAR.setIntWidth(iDaysCnt * 101);
   		
		return wkDAR;
	}

	
	/**
	 * 基準日変更による移動
	 * @param e_basis_date
	 * @param strTableType
	 * @param move
	 */
	public static void jump(
			String e_basis_date,
			String strTableType,
			String strMoveType,
			Integer intMoveNum,
			String move			/* 「移動」ボタン */
			) {
		
		// 「移動」ボタンが押されていない時は処理を抜ける
		if(move==null)
			return;
		
		//ジャンプ後の日付を取得
		DailyAccount da = new DailyAccount();
		if(e_basis_date!=null && strMoveType!=null && intMoveNum!=null )
			e_basis_date = da.dteAfterJump(e_basis_date, strMoveType, intMoveNum);
		
		if(strTableType.equals(Common.VIEWS_DAILY_ACCOUNT)) {
			dailyAccount(e_basis_date);
			return;
		} 
		if(strTableType.equals(Common.VIEWS_BALANCE_TABLE)) {
			balanceTable(e_basis_date);
			return;
		}
	}
	
	/**
	 * ジャンプ後の日付を取得
	 * @param e_basis_date
	 * @param strMoveType
	 * @param intMoveNum
	 * @return
	 */
	private String dteAfterJump(
			String e_basis_date,
			String strMoveType,
			Integer intMoveNum
			) {
		Calendar calendar = Calendar.getInstance();
		Date dBasis = null;
		try {
			dBasis = DateFormat.getDateInstance().parse(e_basis_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calendar.setTime(dBasis);
		if(strMoveType.equals(Messages.get("views.dailyaccount.movetype.year"))) {
			calendar.add(Calendar.YEAR, intMoveNum);
		}
		if(strMoveType.equals(Messages.get("views.dailyaccount.movetype.month"))) {
			calendar.add(Calendar.MONTH, intMoveNum);
		}
		if(strMoveType.equals(Messages.get("views.dailyaccount.movetype.days"))) {
			calendar.add(Calendar.DATE, intMoveNum);
		}
		e_basis_date = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
		
		return e_basis_date;
	}
	
	/**
	 * 日計表・残高表の行に相当するリストの作成
	 * @param year
	 * @param month
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param strTableType
	 * @return
	 */
	private List<WkDailyAccount> makeWorkList(
			Integer year,
			Integer month,
			Date dteStartDay,
			int intDaysCnt,
			String strTableType
			) {
		
		//日計表・残高表の行に相当するリスト
   		List<WkDailyAccount> lWDA = new ArrayList<WkDailyAccount>();
   		
   		Calendar calendar = Calendar.getInstance();
   		
		calendar.set(year, month - 1, 1);
		String sFirstDay =  String.format("%1$tY%1$tm%1$td", calendar.getTime());
   		calendar.add(Calendar.MONTH, 1);
   		String sNextFirst = String.format("%1$tY%1$tm%1$td", calendar.getTime());
   		
		HaUser haUser = (HaUser)renderArgs.get("haUser");

		if(strTableType.equals(Common.VIEWS_BALANCE_TABLE))
			sFirstDay =  String.format("%1$tY%1$tm%1$td", dteStartDay);
		
		//全行取得用SQL作成
		String sql = makeSql(year, month, dteStartDay, intDaysCnt, strTableType, haUser, sFirstDay, sNextFirst);
		
		List<Object[]> lstObjEach = JPA.em().createNativeQuery(sql).getResultList();
		
//		long[] lAryDaysRlAll = new long[iDaysCnt];		//合計行の日毎金額
		
		int intCnt = 0;
		int intLwdaCnt = lWDA.size();
		String strOldLargeCategoryName = "";	//大分類ブレイク用の旧大分類名
		for(Object[] objEach : lstObjEach) {
			String strLargeCategoryName = String.valueOf(objEach[4]);
			
			if(intCnt==0)
				strOldLargeCategoryName = strLargeCategoryName;
			
			//大分類が変わる時
			if(!strOldLargeCategoryName.equals(strLargeCategoryName)) {
				lWDA.get(intLwdaCnt+intCnt-1).setBolLastItemFlg(true);	//大分類ブレイク時に、一つ前の行に最終行フラグを立てる
				strOldLargeCategoryName = strLargeCategoryName;
			}
			intCnt++;
			
			
			WkDailyAccount wDaEach = new WkDailyAccount();
			
			wDaEach.setsLargeCategory(strLargeCategoryName);
			wDaEach.setsItem(String.valueOf(objEach[2]));
			wDaEach.setbBudgetFlg(false);
	
			//「収入」・「支出」・「Parllet預入・引出」の場合、予算有フラグを立てる
			if(strLargeCategoryName.equals(Common.BALANCE_TYPE_IN) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_OUT) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_PARLLET_INOUT)) {
				wDaEach.setbBudgetFlg(true);
			}
			
			// ある時だけ予算をセット
			if(objEach[6]!=null) {
				Long lngBgId = Long.parseLong(String.valueOf(objEach[5]));
				Long lngBgAmount = Long.parseLong(String.valueOf(objEach[6]));
//				String sBudgetAmount = String.format("%1$,3d", lngBgAmount).trim();
				wDaEach.setlBudgetId(lngBgId);
				wDaEach.setlBudgetAmount(lngBgAmount);
			}
			
			// 「実残高」の時は種類名に取扱種類名をセット
			if(strLargeCategoryName.equals(Common.REMAINDER_TYPE_REAL))
				wDaEach.setStrTypeNm(String.valueOf(objEach[intDaysCnt+7]));
			
			// 「実残高」・「Parllet残高」の時は無効フラグの中身をセット
			if(strLargeCategoryName.equals(Common.REMAINDER_TYPE_REAL) ||
					strLargeCategoryName.equals(Common.REMAINDER_TYPE_PARLLET))
				wDaEach.setBolInvFlg(Boolean.valueOf(String.valueOf(objEach[intDaysCnt+8])));
			
			// 日毎
			calendar.setTime(dteStartDay);
			long lngEach = 0L;
//			long lngSum = 0L;
			List<WkDaToDl> lstWdtd = new ArrayList<WkDaToDl>();
			for(int iDay = 0; iDay < intDaysCnt; iDay++) {
				//「実残高」・「Parlletに入れてないお金」・「Parllet残高」の時は2日目以降は加算
				if(strLargeCategoryName.equals(Common.REMAINDER_TYPE_REAL) ||
						strLargeCategoryName.equals(Common.REMAINDER_TYPE_NOT_PARLLET) ||
						strLargeCategoryName.equals(Common.REMAINDER_TYPE_PARLLET)) {
					lngEach += Long.parseLong(String.valueOf(objEach[iDay+7]));	//日毎金額
//					lngSum = lngEach;											//月計
				} else {
					lngEach = Long.parseLong(String.valueOf(objEach[iDay+7]));	//日毎金額
//					lngSum += lngEach;											//月計
				}
//				lAryDaysRlAll[iDay] += lngEach;	//合計行の日毎金額
				
				//日計表から明細表へのリンクのための引数をセット
				WkDaToDl wkDaToDl = makeWkDaToDl(
						calendar,
						strLargeCategoryName,
						lngEach,
						Long.parseLong(String.valueOf(objEach[0]))
						);

				lstWdtd.add(wkDaToDl);
				
				calendar.add(Calendar.DATE, 1);
			}
			wDaEach.setLstWdtd(lstWdtd);
			//「収入」・「支出」・「Parllet預入・引出」・「口座預入」・「口座引出」の場合、月計をセット
			if(strLargeCategoryName.equals(Common.BALANCE_TYPE_IN) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_OUT) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_PARLLET_INOUT) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_BANK_IN) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_BANK_OUT)) {
				wDaEach.setLSumMonth(Long.parseLong(String.valueOf(objEach[intDaysCnt+7])));
			}
			//「収入」・「支出」の場合、Parllet連結をセット
			if(strLargeCategoryName.equals(Common.BALANCE_TYPE_IN) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_OUT)) {
				wDaEach.setLSumMonth(Long.parseLong(String.valueOf(objEach[intDaysCnt+7])));
				wDaEach.setlPrltLink(Long.parseLong(String.valueOf(objEach[intDaysCnt+8])));
			}
			
			lWDA.add(wDaEach);
		}
		
		return lWDA;
	}

	/**
	 * 全行取得用SQL作成
	 * @param year
	 * @param month
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param strTableType
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @return
	 */
	private String makeSql(
			Integer year,
			Integer month,
			Date dteStartDay,
			int intDaysCnt,
			String strTableType,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst
			) {
		
		String sql = "";
		if(strTableType.equals(Common.VIEWS_DAILY_ACCOUNT)) {
			//収支取得用SQL作成(収入)(合計)
			String sqlBalInAll = makeSqlBalInOut(
					false,
					Common.BALANCE_TYPE_IN,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
			//収支取得用SQL作成(収入)(項目毎)
			String sqlBalIn = makeSqlBalInOut(
					true,
					Common.BALANCE_TYPE_IN,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
			//収支取得用SQL作成(支出)(合計)
			String sqlBalOutAll = makeSqlBalInOut(
					false,
					Common.BALANCE_TYPE_OUT,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
			//収支取得用SQL作成(支出)(項目毎)
			String sqlBalOut = makeSqlBalInOut(
					true,
					Common.BALANCE_TYPE_OUT,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
			//収支取得用SQL作成(Parllet預入・引出)(合計)
			String sqlBalPrltInOutAll = makeSqlBalPrlt(
					false,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
			//収支取得用SQL作成(Parllet預入・引出)(項目毎)
			String sqlBalPrltInOut = makeSqlBalPrlt(
					true,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
//			//収支取得用SQL作成(Parlletから支払)(合計)
//			String sqlBalOutPrltAll = makeSqlBalPrlt(
//					false,
//					Common.BALANCE_TYPE_OUT_PARLLET,
//					year, month, dStartDay, iDaysCnt, haUser, strFirstDay, strNextFirst
//					);
//			//収支取得用SQL作成(Parlletから支払)(項目毎)
//			String sqlBalOutPrlt = makeSqlBalPrlt(
//					true,
//					Common.BALANCE_TYPE_OUT_PARLLET,
//					year, month, dStartDay, iDaysCnt, haUser, strFirstDay, strNextFirst
//					);
			//収支取得用SQL作成(口座預入)(合計)
			String sqlBalBankInAll = makeSqlBalBank(
					Common.BALANCE_TYPE_BANK_IN,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
			//収支取得用SQL作成(口座引出)(合計)
			String sqlBalBankOutAll = makeSqlBalBank(
					Common.BALANCE_TYPE_BANK_OUT,
					year, month, dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
					);
			
			sql = "" +
					" ( " + sqlBalInAll + " ) " +
					" UNION ALL " +
					" ( " + sqlBalIn + " ) " +
					" UNION ALL " +
					" ( " + sqlBalOutAll + " ) " +
					" UNION ALL " +
					" ( " + sqlBalOut + " ) " +
					" UNION ALL " +
					" ( " + sqlBalPrltInOutAll + " ) " +
					" UNION ALL " +
					" ( " + sqlBalPrltInOut + " ) " +
//					" UNION ALL " +
//					" ( " + sqlBalOutPrltAll + " ) " +
//					" UNION ALL " +
//					" ( " + sqlBalOutPrlt + " ) " +
					" UNION ALL " +
					" ( " + sqlBalBankInAll + " ) " +
					" UNION ALL " +
					" ( " + sqlBalBankOutAll + " ) " +
					" ORDER BY cate_order, item_order " +
					"";
			
			while(!(sql.equals(sql.replaceAll("  ", " "))))
				sql = sql.replaceAll("  ", " ");
			
			return sql;
			
		}
		//残高取得用SQL作成(実残高)(合計)
		String sqlRemRealAll = makeSqlRemReal(
						false,
						dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
				);
		//残高取得用SQL作成(実残高)(取扱毎)
		String sqlRemReal = makeSqlRemReal(
						true,
						dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
				);
		//残高取得用SQL作成(Parlletに入れてないお金)
		String sqlRemNotParllet = makeSqlRemNotParllet(
						dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
				);
//		//残高取得用SQL作成(Parllet残高)(合計)
//		String sqlRemParlletAll = makeSqlRemParllet(
//						false,
//						dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
//				);
		//残高取得用SQL作成(Parllet残高)(取扱毎)
		String sqlRemParllet = makeSqlRemParllet(
						true,
						dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
				);
//		//残高取得用SQL作成(Parllet残高)(合計)
//		String sqlRemParlletAll2 = makeSqlRemParllet2(
//						false,
//						dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
//				);
//		//残高取得用SQL作成(Parllet残高)(取扱毎)
//		String sqlRemParllet2 = makeSqlRemParllet2(
//						true,
//						dteStartDay, intDaysCnt, haUser, strFirstDay, strNextFirst
//				);
		
		sql += "" +
				" ( " + sqlRemRealAll + " ) " +
				" UNION ALL " +
				" ( " + sqlRemReal + " ) " +
				" UNION ALL " +
				" ( " + sqlRemNotParllet + " ) " +
//				" UNION ALL " +
//				" ( " + sqlRemParlletAll + " ) " +
				" UNION ALL " +
				" ( " + sqlRemParllet + " ) " +
//				" UNION ALL " +
//				" ( " + sqlRemParlletAll2 + " ) " +
//				" UNION ALL " +
//				" ( " + sqlRemParllet2 + " ) " +
				" ORDER BY cate_order, item_order " +
				"";
		
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 残高取得用SQL作成(実残高)
	 * @param bolEach
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @return
	 */
	private String makeSqlRemReal(
			boolean bolEach,
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst
			) {
		
		String sql = "";
		
		/** 
   		 * SQL固定部分作成
   		 */
		
		//  FROM句
		String sqlFromPhrase = "" +
				" FROM Record r " +
				" LEFT JOIN ItemMst i " +
				"   ON r.item_mst_id = i.id " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" LEFT JOIN HandlingMst h " +
				"   ON r.handling_mst_id = h.id " +
				" LEFT JOIN HandlingTypeMst ht " +
				"   ON h.handling_type_mst_id = ht.id " +
				" LEFT JOIN HandlingMst hb " +
				"   ON h.debit_bank_id = hb.id " +
				"";
		
		
		/* CASE文内の加減算の条件 */
		
		// 収入：加算、支出：減算
		String sqlSumAllCaseInOut = "" +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_IN + "' THEN r.amount " +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_OUT + "' THEN -r.amount " +
				"";
		// 口座引出：加算、口座預入：減算
		String sqlSumCashCaseBankInOut = "" +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_BANK_OUT + "' THEN r.amount " +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_BANK_IN + "' THEN -r.amount " +
				"";
		// 口座引出：減算、口座預入：加算
		String sqlSumNotCashCaseBankInOut = "" +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_BANK_OUT + "' THEN -r.amount" +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_BANK_IN + "' THEN r.amount" +
				"";
		
		if(!bolEach) {
			//残高取得用SQL作成(実残高)(取扱合計)
			sql = makeSqlRemRealAll(
					dteStartDay,
					intDaysCnt,
					haUser,
					strFirstDay,
					strNextFirst,
					sqlFromPhrase,
					sqlSumAllCaseInOut
					);
			
			return sql;
		}
		
		
		String sqlDailyLater = "";
		String sqlDailyZero = "";
   		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dteStartDay);
		for(int iDay = 1; iDay <= intDaysCnt; iDay++) {
			sqlDailyZero += "" +
					" OR  sum_day_" + iDay + " <> 0 ";
			calendar.add(Calendar.DATE, 1);
			if(iDay > 1)
				sqlDailyLater += "" +
						" ,COALESCE(rem_later.sum_day_" + iDay + ", 0) as sum_day_" + iDay + " " +
						"";
		}
		
		//残高取得用SQL作成(実残高)(取扱毎)
		sql = makeSqlRemRealEach(
				dteStartDay,
				intDaysCnt,
				haUser,
				strFirstDay,
				strNextFirst,
				sqlFromPhrase,
				sqlSumAllCaseInOut,
				sqlSumCashCaseBankInOut,
				sqlSumNotCashCaseBankInOut,
				sqlDailyLater,
				sqlDailyZero);
		
		return sql;
	}
				
	/**
	 * 残高取得用SQL作成(実残高)(取扱合計)
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @return
	 */
	private String makeSqlRemRealAll(
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut
			) {
	
		String sql = "";
		
		//初日の残高取得用SQL作成（取扱合計）
		String sqlAllFirstDay = makeSqlRemRealAllFirstDay(
				haUser,
				strFirstDay,
				sqlFromPhrase,
				sqlSumAllCaseInOut
				);
		sql = "" +
				" SELECT " +
				"   *" +
				" FROM ( SELECT 0 as item_id, 0 as item_order, cast('' as character varying(255)) as item_name, 50 as cate_order " +
				"  ,cast('" + Common.REMAINDER_TYPE_REAL + "' as character varying(255)) as cate_name " +
				"  ,0 as bg_id " +
				"  ,0 as bg_amount " +
				" ) rem_item " +
				" CROSS JOIN ( " + sqlAllFirstDay + " ) rem_firstday " +
				"";
		//2日目以降の残高取得用SQL作成（取扱合計）
		if(intDaysCnt>=2) {
			String sqlAllLater = makeSqlRemRealAllLater(
					dteStartDay,
					intDaysCnt,
					haUser,
					strFirstDay,
					strNextFirst,
					sqlFromPhrase,
					sqlSumAllCaseInOut
					);
			sql += " CROSS JOIN (" + sqlAllLater + " ) rem_later ";
		}
		
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		
		return sql;
	}
	
	/**
	 * 初日の残高取得用SQL作成(実残高)(取扱合計)
	 * @param haUser
	 * @param strFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @return
	 */
	private String makeSqlRemRealAllFirstDay(
			HaUser haUser,
			String strFirstDay,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut
			) {

   		String sqlDaily = "";

		//日付毎の合計取得部分のSQL
		sqlDaily += "" +
				" COALESCE(SUM(" +
				"   CASE " +
				sqlSumAllCaseInOut +	//収入加算・支出減算
				"   END " +
				" ), 0) as sum_day_1";
   		
		String sqlFirstDay = "" +
   				" SELECT " +
   				sqlDaily +
				"  ,cast('' as character varying(255)) as item_type_name " +
				"  ,cast(NULL as boolean) as inv_flg " +
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"";
   		
		while(!(sqlFirstDay.equals(sqlFirstDay.replaceAll("  ", " "))))
			sqlFirstDay = sqlFirstDay.replaceAll("  ", " ");
		
		return sqlFirstDay;
	}
	
	/**
	 * 2日目以降の残高取得用SQL作成(実残高)(取扱合計)
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @return
	 */
	private String makeSqlRemRealAllLater(
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut
			) {
		
   		String sqlDaily = "";
   		
   		Calendar calendar = Calendar.getInstance();

   		calendar.setTime(dteStartDay);
   		
		//日付毎の合計取得部分のSQL(現金)
		calendar.setTime(dteStartDay);
		for(int iDay = 2; iDay <= intDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
						" WHEN cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
						"   CASE " +
					sqlSumAllCaseInOut +	//収入加算・支出減算
						"   END " +
						" ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}

		String sqlLater = "" +
   				" SELECT " +
   				sqlDaily +
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) >= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + strNextFirst + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"";
   		
		while(!(sqlLater.equals(sqlLater.replaceAll("  ", " "))))
			sqlLater = sqlLater.replaceAll("  ", " ");
		
		return sqlLater;
	}
	
	
	/**
	 * 残高取得用SQL作成(実残高)(取扱毎)
	 * @param bolEach
	 * @param haUser
	 * @param strFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @param sqlSumCashCaseBankInOut
	 * @param sqlSumNotCashCaseBankInOut
	 * @return
	 */
	private String makeSqlRemRealEach(
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut,
			String sqlSumCashCaseBankInOut,
			String sqlSumNotCashCaseBankInOut,
			String sqlDailyLater,
			String sqlDailyZero
			) {
		
		String sql = "";
		
		//初日の残高取得用SQL作成（取扱(実際)毎）
		String sqlEachFirstDay = makeSqlRemRealEachFirstDay(
				haUser,
				strFirstDay,
				sqlFromPhrase,
				sqlSumAllCaseInOut,
				sqlSumCashCaseBankInOut,
				sqlSumNotCashCaseBankInOut
				);
		sql = "" +
				" SELECT " +
				"   hm.id as item_id " +
				"  ,(htm.handling_type_order*10000 + hm.order_seq) as item_order " +
				"  ,hm.handling_name as item_name " +
				"  ,50 as cate_order " +
				"  ,cast('" + Common.REMAINDER_TYPE_REAL + "' as character varying(255)) as cate_name " +
				"  ,0 as bg_id " +
				"  ,0 as bg_amount " +
				"  ,COALESCE(rem_firstday.sum_day_1, 0) as sum_day_1 " +
				"  ,cast(htm.handling_type_name as character varying(255)) as item_type_name " +
				"  ,hm.invalidity_flg as inv_flg " +
				" FROM HandlingMst hm " +
				" LEFT JOIN HandlingTypeMst htm " +
				"   ON hm.handling_type_mst_id = htm.id " +
				" LEFT JOIN ( " + sqlEachFirstDay + " ) rem_firstday " +
				"   ON hm.id = rem_firstday.hd_id " +
				"";
		//2日目以降の残高取得用SQL作成（取扱(実際)毎）
		if(intDaysCnt>=2) {
			String sqlEachLater = makeSqlRemRealEachLater(
					dteStartDay,
					intDaysCnt,
					haUser,
					strFirstDay,
					strNextFirst,
					sqlFromPhrase,
					sqlSumAllCaseInOut,
					sqlSumCashCaseBankInOut,
					sqlSumNotCashCaseBankInOut
					);
			sql += "" +
					" LEFT JOIN (" + sqlEachLater + " ) rem_later " +
					"   ON hm.id = rem_later.hd_id " +
					"";
		}
		sql += "" +
				" WHERE hm.ha_user_id = " + haUser.id +
				"   AND htm.handling_type_name != '" + Common.HANDLING_TYPE_CRECA + "' " +
				"   AND (hm.zero_hidden = false " + sqlDailyZero + ") " +
				"";
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 初日の残高取得用SQL作成(実残高)(取扱毎)
	 * @param bolEach
	 * @param haUser
	 * @param strFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @param sqlSumCashCaseBankInOut
	 * @param sqlSumNotCashCaseBankInOut
	 * @return
	 */
	private String makeSqlRemRealEachFirstDay(
			HaUser haUser,
			String strFirstDay,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut,
			String sqlSumCashCaseBankInOut,
			String sqlSumNotCashCaseBankInOut
			) {
		
		String sqlCashDaily = "";
		String sqlNotCashDaily = "";
		
		//日付毎の合計取得部分のSQL(現金)
		sqlCashDaily += "" +
				" COALESCE(SUM(" +
				"   CASE " +
				sqlSumAllCaseInOut +		//収入加算・支出減算
				sqlSumCashCaseBankInOut +	//口座引出加算・口座預入減算
				"   END " +
				" ), 0) as sum_day_1";
		
		//日付毎の合計取得部分のSQL(現金以外)
		sqlNotCashDaily += "" +
				" COALESCE(SUM(" +
				"   CASE " +
				sqlSumAllCaseInOut +			//収入加算・支出減算
				sqlSumNotCashCaseBankInOut +	//口座引出減算・口座預入加算
				"   END " +
				" ), 0) as sum_day_1";

		
		//現金取得用SQL
		String sqlCash = "" +
				" SELECT " +
				sqlCashDaily +		//日付毎の合計取得部分(現金)
				"  ,hcash.handling_name as hd_handling_name " +
				"  ,hcash.id as hd_id" +
				"  ,hcash.zero_hidden as hd_zero_hidden " +
				sqlFromPhrase +		//FROM句
				" CROSS JOIN HandlingMst hcash " +
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    ht.handling_type_name = '" + Common.HANDLING_TYPE_CASH + "' " +
							"AND b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							") " +
						"OR (    ht.handling_type_name in('" + Common.HANDLING_TYPE_BANK + "','" + Common.HANDLING_TYPE_EMONEY + "') " +
							"AND b.balance_type_name in('" + Common.BALANCE_TYPE_BANK_OUT + "','" + Common.BALANCE_TYPE_BANK_IN + "') " +
							") " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"   AND hcash.handling_name = '" + Common.HANDLING_TYPE_CASH + "' " +
				" GROUP BY hd_id, hd_handling_name, hd_zero_hidden " +
				"";
		
		//現金以外取得用SQL
		String sqlNotCash = "" +
				" SELECT " +
				sqlNotCashDaily +		//日付毎の合計取得部分(現金以外)
				"  ,CASE " +
					" WHEN ht.handling_type_name = '" + Common.HANDLING_TYPE_CRECA + "' THEN " +
					"   hb.handling_name " +
					" ELSE " +
					"   h.handling_name " +
					" END as hd_handling_name " +
				"  ,CASE " +
					" WHEN ht.handling_type_name = '" + Common.HANDLING_TYPE_CRECA + "' THEN " +
					"   hb.id " +
					" ELSE " +
					"   h.id " +
				"   END as hd_id " +
				"  ,CASE " +
					" WHEN ht.handling_type_name = '" + Common.HANDLING_TYPE_CRECA + "' THEN " +
					"   hb.zero_hidden " +
					" ELSE " +
					"   h.zero_hidden " +
				"   END as hd_zero_hidden " +
				sqlFromPhrase +			//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "','" + Common.BALANCE_TYPE_BANK_OUT + "','" + Common.BALANCE_TYPE_BANK_IN + "') " +
				"   AND h.handling_name <> '" + Common.HANDLING_TYPE_CASH + "' " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				" GROUP BY hd_id, hd_handling_name, hd_zero_hidden " +
				"";
		
		
		//現金と現金以外を合わせて取得用SQL
		String sqlFirstDay = "" +
				" ( " + sqlCash + " ) " +
				"  UNION ALL " +
				" ( " + sqlNotCash + " ) " +
				"";
		
		while(!(sqlFirstDay.equals(sqlFirstDay.replaceAll("  ", " "))))
			sqlFirstDay = sqlFirstDay.replaceAll("  ", " ");
		
		return sqlFirstDay;
	}
	
	/**
	 * 2日目以降の残高取得用SQL作成(実残高)(取扱毎)
	 * @param bolEach
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @param sqlSumCashCaseBankInOut
	 * @param sqlSumNotCashCaseBankInOut
	 * @return
	 */
	private String makeSqlRemRealEachLater(
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut,
			String sqlSumCashCaseBankInOut,
			String sqlSumNotCashCaseBankInOut
			) {
		
		Calendar calendar = Calendar.getInstance();
		
		String sqlCashDaily = "";
		String sqlNotCashDaily = "";
		
		//日付毎の合計取得部分のSQL(現金)
		calendar.setTime(dteStartDay);
		for(int iDay = 2; iDay <= intDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlCashDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
						" WHEN cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
						"   CASE " +
					sqlSumAllCaseInOut +		//収入加算・支出減算
					sqlSumCashCaseBankInOut +	//口座引出加算・口座預入減算
						"   END " +
						" ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		
		//日付毎の合計取得部分のSQL(現金以外)
		calendar.setTime(dteStartDay);
		for(int iDay = 2; iDay <= intDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlNotCashDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
						" WHEN cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
						"   CASE " +
					sqlSumAllCaseInOut +			//収入加算・支出減算
					sqlSumNotCashCaseBankInOut +	//口座引出減算・口座預入加算
						"   END " +
						" ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		

		//現金取得用SQL
		String sqlCash = "" +
				" SELECT " +
				sqlCashDaily +		//日付毎の合計取得部分(現金)
				"  ,hcash.handling_name as hd_handling_name " +
				"  ,hcash.id as hd_id" +
				"  ,hcash.zero_hidden as hd_zero_hidden " +
				sqlFromPhrase +		//FROM句
				" CROSS JOIN HandlingMst hcash " +
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    ht.handling_type_name = '" + Common.HANDLING_TYPE_CASH + "' " +
							"AND b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							") " +
						"OR (    ht.handling_type_name in('" + Common.HANDLING_TYPE_BANK + "','" + Common.HANDLING_TYPE_EMONEY + "') " +
							"AND b.balance_type_name in('" + Common.BALANCE_TYPE_BANK_OUT + "','" + Common.BALANCE_TYPE_BANK_IN + "') " +
							") " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) >= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + strNextFirst + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"   AND hcash.handling_name = '" + Common.HANDLING_TYPE_CASH + "' " +
				" GROUP BY hd_id, hd_handling_name, hd_zero_hidden " +
				"";
		
		//現金以外取得用SQL
		String sqlNotCash = "" +
				" SELECT " +
				sqlNotCashDaily +		//日付毎の合計取得部分(現金以外)
				"  ,CASE " +
					" WHEN ht.handling_type_name = '" + Common.HANDLING_TYPE_CRECA + "' THEN " +
					"   hb.handling_name " +
					"  ELSE " +
					"   h.handling_name " +
					" END as hd_handling_name " +
				"  ,CASE " +
					" WHEN ht.handling_type_name = '" + Common.HANDLING_TYPE_CRECA + "' THEN " +
					"   hb.id " +
					" ELSE " +
					"   h.id " +
				"   END as hd_id " +
				"  ,CASE " +
					" WHEN ht.handling_type_name = '" + Common.HANDLING_TYPE_CRECA + "' THEN " +
					"   hb.zero_hidden " +
					" ELSE " +
					"   h.zero_hidden " +
				"   END as hd_zero_hidden " +
				sqlFromPhrase +			//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "','" + Common.BALANCE_TYPE_BANK_OUT + "','" + Common.BALANCE_TYPE_BANK_IN + "') " +
				"   AND h.handling_name <> '" + Common.HANDLING_TYPE_CASH + "' " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + strNextFirst + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				" GROUP BY hd_id, hd_handling_name, hd_zero_hidden " +
				"";

		//現金と現金以外を合わせて取得用SQL
		String sqlLater = "" +
				" ( " + sqlCash + " ) " +
				"  UNION ALL " +
				" ( " + sqlNotCash + " ) " +
				"";
		
		while(!(sqlLater.equals(sqlLater.replaceAll("  ", " "))))
			sqlLater = sqlLater.replaceAll("  ", " ");
		
		return sqlLater;
	}
	
	/**
	 * 残高取得用SQL作成(Parllet残高)
	 * @param bolEach
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @return
	 */
	private String makeSqlRemParllet(
			boolean bolEach,
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst
			) {
		
		String sql = "";

   		/** 
   		 * SQL固定部分作成
   		 */
		
		//  FROM句
		String sqlFromPhrase = "" +
   				" FROM Record r " +
				" LEFT JOIN ItemMst i " +
				"   ON r.item_mst_id = i.id " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" LEFT JOIN ParlletMst pm " +
				"   ON r.parllet_mst_id = pm.id " +
				" LEFT JOIN HandlingMst h " +
				"   ON r.handling_mst_id = h.id " +
				" LEFT JOIN HandlingTypeMst ht " +
				"   ON h.handling_type_mst_id = ht.id" +
				"";
		
		/* CASE文内の加減算の条件 */
		// Parlletから直接支払：減算、Parlletに直接入金：加算、Parllet預入：加算、Parllet引出：減算
		String sqlSumCasePrltInOut = "" +
   				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_OUT + "' " +
   					"   AND r.parllet_mst_id IS NOT NULL " +
   					"   ) THEN -r.amount " +
   				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_IN + "' " +
   					"   AND r.parllet_mst_id IS NOT NULL " +
   					"   ) THEN r.amount " +
   				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_IN + "' THEN r.amount" +
   				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_OUT + "' THEN -r.amount" +
				"";

		if(!bolEach) {
			//初日の残高取得用SQL作成（Parllet合計）
			String sqlAllFirstDay = makeSqlRemPrltFirstDay(
					false,
					haUser,
					strFirstDay,
					sqlFromPhrase,
					sqlSumCasePrltInOut
					);
			sql = "" +
					" SELECT " +
					"   *" +
					" FROM ( SELECT 0 as item_id, 0 as item_order, cast('' as character varying(255)) as item_name, 70 as cate_order" +
					"  ,cast('" + Common.REMAINDER_TYPE_PARLLET + "' as character varying(255)) as cate_name " +
					"  ,0 as bg_id " +
					"  ,0 as bg_amount " +
					" ) rem_item " +
					" CROSS JOIN ( " + sqlAllFirstDay + " ) rem_firstday " +
					"";
			//2日目以降の残高取得用SQL作成（Parllet合計）
			if(intDaysCnt>=2) {
				String sqlAllLater = makeSqlRemPrltLater(
						false,
						dteStartDay,
						intDaysCnt,
						haUser,
						strFirstDay,
						strNextFirst,
						sqlFromPhrase,
						sqlSumCasePrltInOut
						);
				sql += " CROSS JOIN (" + sqlAllLater + " ) rem_later ";
			}
			sql += "" +
					" CROSS JOIN ( " +
					" SELECT " +
					"   cast('' as character varying(255)) as item_type_name " +
					"  ,cast(NULL as boolean) as inv_flg " +
					" ) sel_item_type_name " +
					"";
			while(!(sql.equals(sql.replaceAll("  ", " "))))
				sql = sql.replaceAll("  ", " ");
			
			return sql;
		}

		
		
		
		String sqlDailyLater = "";
		String sqlDailyZero = "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dteStartDay);
		for(int iDay = 1; iDay <= intDaysCnt; iDay++) {
			sqlDailyZero += "" +
					" OR  sum_day_" + iDay + " <> 0 ";
			calendar.add(Calendar.DATE, 1);
			if(iDay>=2)
				sqlDailyLater += "" +
						" ,COALESCE(rem_later.sum_day_" + iDay + ", 0) as sum_day_" + iDay + " ";
		}
		
		//初日の残高取得用SQL作成（取扱(Parllet)毎）
		String sqlEachFirstDay = makeSqlRemPrltFirstDay(
				true,
				haUser,
				strFirstDay,
				sqlFromPhrase,
				sqlSumCasePrltInOut
				);
		//2日目以降の残高取得用SQL作成（取扱(Parllet)毎）
		String sqlJoinPhrase = "";
		String sqlEachLater = "";
		if(intDaysCnt>=2) {
			sqlEachLater = makeSqlRemPrltLater(
					true,
					dteStartDay,
					intDaysCnt,
					haUser,
					strFirstDay,
					strNextFirst,
					sqlFromPhrase,
					sqlSumCasePrltInOut
					);
			sqlJoinPhrase = "" +
					" LEFT JOIN (" + sqlEachLater + " ) rem_later " +
					"   ON rem_firstday.pm_id = rem_later.pm_id " +
					"";
		}
		sql = "" +
				" SELECT " +
				"   pm.id as item_id " +
				"  ,pm.order_seq as item_order " +
				"  ,pm.parllet_name as item_name " +
				"  ,70 as cate_order " +
				"  ,cast('" + Common.REMAINDER_TYPE_PARLLET + "' as character varying(255)) as cate_name " +
				"  ,0 as bg_id " +
				"  ,0 as bg_amount " +
				"  ,COALESCE(rem_firstday.sum_day_1, 0) as sum_day_1 " + sqlDailyLater +
				"  ,cast('' as character varying(255)) as item_type_name " +
				"  ,cast(NULL as boolean) as inv_flg " +
				" FROM ParlletMst pm " +
				" LEFT JOIN ( " + sqlEachFirstDay + " ) rem_firstday " +
				"   ON pm.id = rem_firstday.pm_id" +
				sqlJoinPhrase +
				" WHERE pm.ha_user_id = " + haUser.id +
				"   AND (pm.zero_hidden = false " + sqlDailyZero + ") " +
				"";
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 初日の残高取得用SQL作成(Parllet残高)
	 * @param bolEach
	 * @param haUser
	 * @param strFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumCasePrltInOut
	 * @return
	 */
	private String makeSqlRemPrltFirstDay(
			boolean bolEach,
			HaUser haUser,
			String strFirstDay,
			String sqlFromPhrase,
			String sqlSumCasePrltInOut
			) {
		
		//日付毎の合計取得部分のSQL
		String sqlDaily = "" +
				" COALESCE(SUM(" +
				"   CASE " +
				sqlSumCasePrltInOut +		//Parlletから直接支払減算・Parlletに直接入金加算・Parllet預入加算・Parllet引出減算
				"   END " +
				" ), 0) as sum_day_1";
		
		//SQL
		String sqlFirstDay = "" +
				" SELECT " +
   				(bolEach ?
   				"   pm.parllet_name as pm_parllet_name " +
   				"  ,pm.id as pm_id " +
   				"  ,pm.zero_hidden as pm_zero_hidden , "
   				: "") +
				sqlDaily +		//日付毎の合計取得部分
				sqlFromPhrase +	//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							"AND r.parllet_mst_id IS NOT NULL " +
							") " +
						"OR b.balance_type_name in('" + Common.BALANCE_TYPE_PARLLET_IN + "','" + Common.BALANCE_TYPE_PARLLET_OUT + "') " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
   				(bolEach ?
				" GROUP BY pm.id, pm.parllet_name, pm.zero_hidden "
   				: "") +
				"";

		while(!(sqlFirstDay.equals(sqlFirstDay.replaceAll("  ", " "))))
			sqlFirstDay = sqlFirstDay.replaceAll("  ", " ");
		
		return sqlFirstDay;
	}
	
	/**
	 * 2日目以降の残高取得用SQL作成(Parllet残高)
	 * @param bolEach
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumCasePrltInOut
	 * @return
	 */
	private String makeSqlRemPrltLater(
			boolean bolEach,
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst,
			String sqlFromPhrase,
			String sqlSumCasePrltInOut
			) {
		
		Calendar calendar = Calendar.getInstance();
		
		String sqlDaily = "";
		
		//日付毎の合計取得部分のSQL
		calendar.setTime(dteStartDay);
		for(int iDay = 2; iDay <= intDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
						" WHEN cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
						"   CASE " +
					sqlSumCasePrltInOut +		//Parlletから直接支払減算・Parlletに直接入金加算・Parllet預入加算・Parllet引出減算
						"   END " +
						" ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		
		//SQL
		String sqlLater = "" +
				" SELECT " +
   				(bolEach ?
   				"   pm.parllet_name as pm_parllet_name " +
   				"  ,pm.id as pm_id " +
   				"  ,pm.zero_hidden as pm_zero_hidden , "
   				: "") +
				sqlDaily +		//日付毎の合計取得部分
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							"AND r.parllet_mst_id IS NOT NULL " +
							") " +
						"OR b.balance_type_name in('" + Common.BALANCE_TYPE_PARLLET_IN + "','" + Common.BALANCE_TYPE_PARLLET_OUT + "') " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) >= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + strNextFirst + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
   				(bolEach ?
				" GROUP BY pm.id, pm.parllet_name, pm.zero_hidden "
   				: "") +
				"";
		
		while(!(sqlLater.equals(sqlLater.replaceAll("  ", " "))))
			sqlLater = sqlLater.replaceAll("  ", " ");
		
		return sqlLater;
	}
	
	/**
	 * 残高取得用SQL作成(Parlletに入れてないお金)
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @return
	 */
	private String makeSqlRemNotParllet(
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst
			) {
		
		String sql = "";
		
		/** 
		 * SQL固定部分作成
		 */
		
		//  FROM句
		String sqlFromPhrase = "" +
				" FROM Record r " +
				" LEFT JOIN ItemMst i " +
				"   ON r.item_mst_id = i.id " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" LEFT JOIN ParlletMst pm " +
				"   ON r.parllet_mst_id = pm.id " +
				" LEFT JOIN HandlingMst h " +
				"   ON r.handling_mst_id = h.id " +
				" LEFT JOIN HandlingTypeMst ht " +
				"   ON h.handling_type_mst_id = ht.id" +
				"";
		
		/* CASE文内の加減算の条件 */
		// 収入でParllet未選択：加算、支出でParllet未選択：減算、Parllet引出：加算、Parllet預入：減算
		String sqlSumCaseNotPrltInOut = "" +
				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_IN + "' " +
					"   AND r.parllet_mst_id IS NULL " +
					"   ) THEN r.amount " +
				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_OUT + "' " +
					"   AND r.parllet_mst_id IS NULL " +
					"   ) THEN -r.amount " +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_OUT + "' THEN r.amount" +
				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_IN + "' THEN -r.amount" +
				"";
		
		//初日の残高取得用SQL作成（Parlletに入れてないお金合計）
		String sqlAllFirstDay = makeSqlRemNotPrltFirstDay(
				haUser,
				strFirstDay,
				sqlFromPhrase,
				sqlSumCaseNotPrltInOut
				);
		sql = "" +
				" SELECT " +
				"   *" +
//				" FROM ( SELECT 0 as item_id, 0 as item_order, cast('' as character varying(255)) as item_name, 60 as cate_order " +
//				"  ,cast('" + Common.REMAINDER_TYPE_NOT_PARLLET + "' as character varying(255)) as cate_name " +
				" FROM ( SELECT 0 as item_id, 0 as item_order," +
						"  cast('" + Common.REMAINDER_TYPE_NOT_PARLLET + "' as character varying(255)) as item_name," +
						"  60 as cate_order " +
				"  ,cast('" + Common.REMAINDER_TYPE_PARLLET + "' as character varying(255)) as cate_name " +
				"  ,0 as bg_id " +
				"  ,0 as bg_amount " +
				" ) rem_item " +
				" CROSS JOIN ( " + sqlAllFirstDay + " ) rem_firstday " +
				"";
		//2日目以降の残高取得用SQL作成（Parlletに入れてないお金合計）
		if(intDaysCnt>=2) {
			String sqlAllLater = makeSqlRemNotPrltLater(
					dteStartDay,
					intDaysCnt,
					haUser,
					strFirstDay,
					strNextFirst,
					sqlFromPhrase,
					sqlSumCaseNotPrltInOut
					);
			sql += " CROSS JOIN (" + sqlAllLater + " ) rem_later ";
		}
		sql += "" +
				" CROSS JOIN ( " +
				" SELECT " +
				"   cast('' as character varying(255)) as item_type_name " +
				"  ,cast(NULL as boolean) as inv_flg " +
				" ) sel_item_type_name " +
				"";
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 初日の残高取得用SQL作成(Parlletに入れてないお金)
	 * @param haUser
	 * @param strFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumCaseNotPrltInOut
	 * @return
	 */
	private String makeSqlRemNotPrltFirstDay(
			HaUser haUser,
			String strFirstDay,
			String sqlFromPhrase,
			String sqlSumCaseNotPrltInOut
			) {
		
		//日付毎の合計取得部分のSQL
		String sqlDaily = "" +
				" COALESCE(SUM(" +
				"   CASE " +
				sqlSumCaseNotPrltInOut +		//収入でParllet未選択：加算、支出でParllet未選択：減算、Parllet引出：加算、Parllet預入：減算
				"   END " +
				" ), 0) as sum_day_1";
		
		//SQL
		String sqlFirstDay = "" +
				" SELECT " +
				sqlDaily +		//日付毎の合計取得部分
				sqlFromPhrase +	//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							"AND r.parllet_mst_id IS NULL " +
							") " +
						"OR b.balance_type_name in('" + Common.BALANCE_TYPE_PARLLET_IN + "','" + Common.BALANCE_TYPE_PARLLET_OUT + "') " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"";

		while(!(sqlFirstDay.equals(sqlFirstDay.replaceAll("  ", " "))))
			sqlFirstDay = sqlFirstDay.replaceAll("  ", " ");
		
		return sqlFirstDay;
	}
	
	/**
	 * 2日目以降の残高取得用SQL作成(Parllet残高)
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumCaseNotPrltInOut
	 * @return
	 */
	private String makeSqlRemNotPrltLater(
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst,
			String sqlFromPhrase,
			String sqlSumCaseNotPrltInOut
			) {
		
		Calendar calendar = Calendar.getInstance();
		
		String sqlDaily = "";
		
		//日付毎の合計取得部分のSQL
		calendar.setTime(dteStartDay);
		for(int iDay = 2; iDay <= intDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
						" WHEN cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
						"   CASE " +
					sqlSumCaseNotPrltInOut +		//Parlletから直接支払減算・Parlletに直接入金加算・Parllet預入加算・Parllet引出減算
						"   END " +
						" ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		
		//SQL
		String sqlLater = "" +
				" SELECT " +
				sqlDaily +		//日付毎の合計取得部分
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							"AND r.parllet_mst_id IS NULL " +
							") " +
						"OR b.balance_type_name in('" + Common.BALANCE_TYPE_PARLLET_IN + "','" + Common.BALANCE_TYPE_PARLLET_OUT + "') " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) >= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + strNextFirst + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"";
		
		while(!(sqlLater.equals(sqlLater.replaceAll("  ", " "))))
			sqlLater = sqlLater.replaceAll("  ", " ");
		
		return sqlLater;
	}
	
	/**
	 * 残高取得用SQL作成(Parllet残高)
	 * @param bolEach
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @return
	 */
	private String makeSqlRemParllet2(
			boolean bolEach,
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst
			) {
		
		String sql = "";

   		/** 
   		 * SQL固定部分作成
   		 */
		
		//  FROM句
		String sqlFromPhrase = "" +
   				" FROM Record r " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				"";
		
		/* CASE文内の加減算の条件 */
		// Parlletから直接支払：減算、Parlletに直接入金：加算、単純支出：減算、単純収入：加算
		String sqlSumCasePrltInOut = "" +
   				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_OUT + "' " +
   					"   AND r.parllet_mst_id IS NOT NULL " +
   					"   ) THEN -r.amount " +
   				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_IN + "' " +
   					"   AND r.parllet_mst_id IS NOT NULL " +
   					"   ) THEN r.amount " +
   				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_IN + "' THEN r.amount" +
   				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_OUT + "' THEN -r.amount" +
				"";

		if(!bolEach) {
			//初日の残高取得用SQL作成（Parllet合計）
			String sqlAllFirstDay = makeSqlRemPrlt2FirstDay(
					false,
					haUser,
					strFirstDay,
					sqlFromPhrase,
					sqlSumCasePrltInOut
					);
			sql = "" +
					" SELECT " +
					"   *" +
					" FROM ( SELECT 0 as item_id, 0 as item_order, cast('' as character varying(255)) as item_name, 70 as cate_order" +
					"  ,cast('" + Common.REMAINDER_TYPE_PARLLET + "' as character varying(255)) as cate_name " +
					"  ,0 as bg_id " +
					"  ,0 as bg_amount " +
					" ) rem_item " +
					" CROSS JOIN ( " + sqlAllFirstDay + " ) rem_firstday " +
					"";
			//2日目以降の残高取得用SQL作成（Parllet合計）
			if(intDaysCnt>=2) {
				String sqlAllLater = makeSqlRemPrlt2Later(
						false,
						dteStartDay,
						intDaysCnt,
						haUser,
						strFirstDay,
						strNextFirst,
						sqlFromPhrase,
						sqlSumCasePrltInOut
						);
				sql += " CROSS JOIN (" + sqlAllLater + " ) rem_later ";
			}
			sql += "" +
					" CROSS JOIN ( " +
					" SELECT " +
					"   cast('' as character varying(255)) as item_type_name " +
					"  ,cast(NULL as boolean) as inv_flg " +
					" ) sel_item_type_name " +
					"";
			while(!(sql.equals(sql.replaceAll("  ", " "))))
				sql = sql.replaceAll("  ", " ");
			
			return sql;
		}

		
		
		
		
		//  FROM句
		sqlFromPhrase = "" +
   				" FROM Record r " +
				" LEFT JOIN ItemMst i " +
				"   ON r.item_mst_id = i.id " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" LEFT JOIN ParlletMst pm " +
				"   ON r.parllet_mst_id = pm.id " +
				" LEFT JOIN HandlingMst h " +
				"   ON r.handling_mst_id = h.id " +
				" LEFT JOIN HandlingTypeMst ht " +
				"   ON h.handling_type_mst_id = ht.id" +
				"";
		
		/* CASE文内の加減算の条件 */
		// Parlletから直接支払：減算、Parlletに直接入金：加算、Parllet預入：加算、Parllet引出：減算
		sqlSumCasePrltInOut = "" +
   				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_OUT + "' " +
   					"   AND r.parllet_mst_id IS NOT NULL " +
   					"   ) THEN -r.amount " +
   				" WHEN (    b.balance_type_name = '" + Common.BALANCE_TYPE_IN + "' " +
   					"   AND r.parllet_mst_id IS NOT NULL " +
   					"   ) THEN r.amount " +
   				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_IN + "' THEN r.amount" +
   				" WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_OUT + "' THEN -r.amount" +
				"";

		
		
		
		
		
		String sqlDailyLater = "";
		String sqlDailyZero = "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dteStartDay);
		for(int iDay = 1; iDay <= intDaysCnt; iDay++) {
			sqlDailyZero += "" +
					" OR  sum_day_" + iDay + " <> 0 ";
			calendar.add(Calendar.DATE, 1);
			if(iDay>=2)
				sqlDailyLater += "" +
						" ,COALESCE(rem_later.sum_day_" + iDay + ", 0) as sum_day_" + iDay + " ";
		}
		
		//初日の残高取得用SQL作成（取扱(Parllet)毎）
		String sqlEachFirstDay = makeSqlRemPrlt2FirstDay(
				true,
				haUser,
				strFirstDay,
				sqlFromPhrase,
				sqlSumCasePrltInOut
				);
		//2日目以降の残高取得用SQL作成（取扱(Parllet)毎）
		String sqlJoinPhrase = "";
		String sqlEachLater = "";
		if(intDaysCnt>=2) {
			sqlEachLater = makeSqlRemPrlt2Later(
					true,
					dteStartDay,
					intDaysCnt,
					haUser,
					strFirstDay,
					strNextFirst,
					sqlFromPhrase,
					sqlSumCasePrltInOut
					);
			sqlJoinPhrase = "" +
					" LEFT JOIN (" + sqlEachLater + " ) rem_later " +
					"   ON rem_firstday.pm_id = rem_later.pm_id " +
					"";
		}
		sql = "" +
				" SELECT " +
				"   pm.id as item_id " +
				"  ,pm.order_seq as item_order " +
				"  ,pm.parllet_name as item_name " +
				"  ,70 as cate_order " +
				"  ,cast('" + Common.REMAINDER_TYPE_PARLLET + "' as character varying(255)) as cate_name " +
				"  ,0 as bg_id " +
				"  ,0 as bg_amount " +
				"  ,COALESCE(rem_firstday.sum_day_1, 0) as sum_day_1 " + sqlDailyLater +
				"  ,cast('' as character varying(255)) as item_type_name " +
				"  ,cast(NULL as boolean) as inv_flg " +
				" FROM ParlletMst pm " +
				" LEFT JOIN ( " + sqlEachFirstDay + " ) rem_firstday " +
				"   ON pm.id = rem_firstday.pm_id" +
				sqlJoinPhrase +
				" WHERE pm.ha_user_id = " + haUser.id +
				"   AND (pm.zero_hidden = false " + sqlDailyZero + ") " +
				"";
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 初日の残高取得用SQL作成(Parllet残高)
	 * @param bolEach
	 * @param haUser
	 * @param strFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumCasePrltInOut
	 * @return
	 */
	private String makeSqlRemPrlt2FirstDay(
			boolean bolEach,
			HaUser haUser,
			String strFirstDay,
			String sqlFromPhrase,
			String sqlSumCasePrltInOut
			) {
		
		//日付毎の合計取得部分のSQL
		String sqlDaily = "" +
				" COALESCE(SUM(" +
				"   CASE " +
				sqlSumCasePrltInOut +		//Parlletから直接支払減算・Parlletに直接入金加算・Parllet預入加算・Parllet引出減算
				"   END " +
				" ), 0) as sum_day_1";
		
		//SQL
		String sqlFirstDay = "" +
				" SELECT " +
   				(bolEach ?
   				"   pm.parllet_name as pm_parllet_name " +
   				"  ,pm.id as pm_id " +
   				"  ,pm.zero_hidden as pm_zero_hidden , "
   				: "") +
				sqlDaily +		//日付毎の合計取得部分
				sqlFromPhrase +	//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							"AND r.parllet_mst_id IS NOT NULL " +
							") " +
						"OR b.balance_type_name in('" + Common.BALANCE_TYPE_PARLLET_IN + "','" + Common.BALANCE_TYPE_PARLLET_OUT + "') " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
   				(bolEach ?
				" GROUP BY pm.id, pm.parllet_name, pm.zero_hidden "
   				: "") +
				"";

		while(!(sqlFirstDay.equals(sqlFirstDay.replaceAll("  ", " "))))
			sqlFirstDay = sqlFirstDay.replaceAll("  ", " ");
		
		return sqlFirstDay;
	}
	
	/**
	 * 2日目以降の残高取得用SQL作成(Parllet残高)
	 * @param bolEach
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumCasePrltInOut
	 * @return
	 */
	private String makeSqlRemPrlt2Later(
			boolean bolEach,
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst,
			String sqlFromPhrase,
			String sqlSumCasePrltInOut
			) {
		
		Calendar calendar = Calendar.getInstance();
		
		String sqlDaily = "";
		
		//日付毎の合計取得部分のSQL
		calendar.setTime(dteStartDay);
		for(int iDay = 2; iDay <= intDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
						" WHEN cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
						"   CASE " +
					sqlSumCasePrltInOut +		//Parlletから直接支払減算・Parlletに直接入金加算・Parllet預入加算・Parllet引出減算
						"   END " +
						" ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		
		//SQL
		String sqlLater = "" +
				" SELECT " +
   				(bolEach ?
   				"   pm.parllet_name as pm_parllet_name " +
   				"  ,pm.id as pm_id " +
   				"  ,pm.zero_hidden as pm_zero_hidden , "
   				: "") +
				sqlDaily +		//日付毎の合計取得部分
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + Common.BALANCE_TYPE_OUT + "','" + Common.BALANCE_TYPE_IN + "') " +
							"AND r.parllet_mst_id IS NOT NULL " +
							") " +
						"OR b.balance_type_name in('" + Common.BALANCE_TYPE_PARLLET_IN + "','" + Common.BALANCE_TYPE_PARLLET_OUT + "') " +
						") " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) >= to_date('" + strFirstDay + "', 'YYYYMMDD') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + strNextFirst + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
   				(bolEach ?
				" GROUP BY pm.id, pm.parllet_name, pm.zero_hidden "
   				: "") +
				"";
		
		while(!(sqlLater.equals(sqlLater.replaceAll("  ", " "))))
			sqlLater = sqlLater.replaceAll("  ", " ");
		
		return sqlLater;
	}
	
	/**
	 * 日毎合計取得用SQL作成
	 * @param dStartDay
	 * @param iDaysCnt
	 * @return
	 */
	private String makeSqlBalDaily(
			Date dStartDay,
			int iDaysCnt,
			String strLargeCategoryName
			) {
   		String sqlDaily = "";
   		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dStartDay);
		String strAmount = "";
		if(strLargeCategoryName.equals(Common.BALANCE_TYPE_PARLLET_INOUT)) {
			strAmount = " CASE WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_IN + "' THEN r.amount ELSE -r.amount END ";
		} else {
			strAmount = " r.amount ";
		}
		for(int iDay = 1; iDay <= iDaysCnt; iDay++) {
			sqlDaily += "" +
					",COALESCE(SUM(" +
					"  CASE " +
						"WHEN cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " + strAmount +
						"ELSE 0 " +
					"  END" +
					"  ), 0) as sum_day_" + iDay + "";
			calendar.add(Calendar.DATE, 1);
		}
		return sqlDaily;
	}
	
	/**
	 * 日毎合計取得用SQL作成(合計行)
	 * @param dStartDay
	 * @param iDaysCnt
	 * @return
	 */
	private String makeSqlBalDailyAll(
			Date dStartDay,
			int iDaysCnt
			) {
   		String sqlDaily = "";
   		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dStartDay);
		for(int iDay = 1; iDay <= iDaysCnt; iDay++) {
			sqlDaily += ",sum_day_" + iDay + "";
			calendar.add(Calendar.DATE, 1);
		}
		return sqlDaily;
	}
	
	/**
	 * 収支取得用SQL作成(収入・支出)
	 * @param bolEach
	 * @param strLargeCategoryName
	 * @param year
	 * @param month
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @return
	 */
	private String makeSqlBalInOut(
			boolean bolEach,
			String strLargeCategoryName,	// 大分類行の名称「収入」・「支出」
			Integer year,
			Integer month,
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst
			) {
		
		String sqlDaily = makeSqlBalDaily(dteStartDay, intDaysCnt, strLargeCategoryName);
		String sqlDailyAll = bolEach ? "" : makeSqlBalDailyAll(dteStartDay, intDaysCnt);
		
		String sqlSelGroupby = "" +
   				"   0 as item_id, 0 as item_order" +
   				"  ,cast('' as character varying(255)) as item_name" +
   				"  ," + (strLargeCategoryName.equals(Common.BALANCE_TYPE_IN) ? 10 : 20) + " as cate_order " +
				"  ,cast('" + strLargeCategoryName + "' as character varying(255)) as cate_name " +
   				"  ,0 as bg_id " +
				"";
		String sqlGroupby = "" +
				" ) a " +
				" CROSS JOIN " +
				" (" +
				" SELECT " +
				"   COALESCE(SUM(bg.amount), NULL) as bg_amount " +
				" FROM Budget bg " +
				" LEFT JOIN ItemMst i " +
				"   ON i.id = bg.item_mst_id" +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON i.balance_type_mst_id = b.id " +
				" WHERE b.balance_type_name = '" + strLargeCategoryName + "' " +
				"   AND bg.ha_user_id = " + haUser.id +
				"   AND bg.year = " + year +
				"   AND bg.month = " + month +
				" ) b ) " +
				"";
		if(bolEach) {
			sqlSelGroupby = "" +
	   				"   i.id as item_id " +
	   				"  ,i.order_seq as item_order" +
	   				"  ,i.item_name as item_name" +
	   				"  ," + (strLargeCategoryName.equals(Common.BALANCE_TYPE_IN) ? 10 : 20) + " as cate_order " +
					"  ,cast('" + strLargeCategoryName + "' as character varying(255)) as cate_name " +
	   				"  ,bg.id as bg_id " +
	   				"  ,bg.amount as bg_amount " +
					"";
			sqlGroupby = "" +
					" GROUP BY item_id, item_order, item_name, bg_id, bg_amount " +
					"";
		}
		
		String sql = "" +
				(!bolEach ? 
				" SELECT " +
				"   item_id, " +
				"   item_order, " +
				"   item_name, " +
				"   cate_order, " +
				"   cate_name, " +
				"   bg_id, " +
				"   bg_amount " +
				sqlDailyAll +
				"  ,sum_month " +
				"  ,prlt_link " +
				" FROM ( ( "
						: "") +
   				" SELECT " +
   				sqlSelGroupby +
   				sqlDaily +
				"  ,COALESCE(SUM(CASE WHEN (r.parllet_mst_id IS NULL) THEN r.amount ELSE 0 END), 0) as sum_month " +
				"  ,COALESCE(SUM(CASE WHEN (r.parllet_mst_id IS NOT NULL) THEN r.amount ELSE 0 END), 0) as prlt_link " +
   				" FROM ItemMst i " +
				" LEFT JOIN Record r " +
				"   ON r.item_mst_id = i.id " +
				"   AND r.ha_user_id = " + haUser.id +
				"   AND cast(r.payment_date as date) >= to_date('" + strFirstDay + "', 'YYYYMMDD')" +
				"   AND cast(r.payment_date as date) < to_date('" + strNextFirst + "', 'YYYYMMDD')" +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON i.balance_type_mst_id = b.id " +
				" LEFT JOIN ParlletMst pm " +
				"   ON r.parllet_mst_id = pm.id " +
				(bolEach ?
				" LEFT JOIN Budget bg " +
				"   ON i.id = bg.item_mst_id" +
				"   AND bg.ha_user_id = " + haUser.id +
				"   AND bg.year = " + year +
				"   AND bg.month = " + month 
				: "") +
				" WHERE i.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name = '" + strLargeCategoryName + "' " +
				sqlGroupby +
				"";
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 収支取得用SQL作成(Parllet預入・引出)
	 * @param bolEach
	 * @param year
	 * @param month
	 * @param dteStartDay
	 * @param intDaysCnt
	 * @param haUser
	 * @param strFirstDay
	 * @param strNextFirst
	 * @return
	 */
	private String makeSqlBalPrlt(
			boolean bolEach,
			Integer year,
			Integer month,
			Date dteStartDay,
			int intDaysCnt,
			HaUser haUser,
			String strFirstDay,
			String strNextFirst
			) {
		
		String sqlDaily = makeSqlBalDaily(dteStartDay, intDaysCnt, Common.BALANCE_TYPE_PARLLET_INOUT);
		String sqlDailyAll = bolEach ? "" : makeSqlBalDailyAll(dteStartDay, intDaysCnt);
		
		String sqlSelGroupby = "" +
   				"   0 as item_id, 0 as item_order " +
   				"  ,cast('' as character varying(255)) as item_name" +
   				"  ," + 30 + " as cate_order " +
				"  ,cast('" + Common.BALANCE_TYPE_PARLLET_INOUT + "' as character varying(255)) as cate_name " +
   				"  ,0 as bg_id " +
				"";
		String sqlGroupby = "" +
				" ) a " +
				" CROSS JOIN " +
				" (" +
				" SELECT " +
				"   COALESCE(SUM(bg.amount), NULL) as bg_amount " +
				" FROM Budget bg " +
				" WHERE bg.parllet_mst_id IS NOT NULL " +
				"   AND bg.ha_user_id = " + haUser.id +
				"   AND bg.year = " + year +
				"   AND bg.month = " + month +
				" ) b ) " +
				"";
		if(bolEach) {
			sqlSelGroupby = "" +
	   				"   pm.id as item_id " +
	   				"  ,pm.order_seq as item_order" +
	   				"  ,pm.parllet_name as item_name" +
	   				"  ," + 30 + " as cate_order " +
					"  ,cast('" + Common.BALANCE_TYPE_PARLLET_INOUT + "' as character varying(255)) as cate_name " +
	   				"  ,bg.id as bg_id " +
	   				"  ,bg.amount as bg_amount " +
					"";
			sqlGroupby = "" +
					" GROUP BY item_id, item_order, item_name, bg_id, bg_amount " +
					"";
		}
		
		String sql = "" +
				(!bolEach ? 
				" SELECT " +
				"   item_id, " +
				"   item_order, " +
				"   item_name, " +
				"   cate_order, " +
				"   cate_name, " +
				"   bg_id, " +
				"   bg_amount " +
   				sqlDailyAll +
				"  ,sum_month " +
				"  ,prlt_link " +
				" FROM ( ( "
						: "") +
   				" SELECT " +
   				sqlSelGroupby +
   				sqlDaily +
   				"  ,COALESCE(SUM(CASE WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_IN + "' THEN r.amount ELSE -r.amount END), 0) as sum_month" +
   				"  ,0 as prlt_link " +
   				" FROM Record r " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" LEFT JOIN ParlletMst pm " +
				"   ON r.parllet_mst_id = pm.id " +
				" LEFT JOIN Budget bg " +
				"   ON pm.id = bg.parllet_mst_id" +
				"   AND bg.ha_user_id = " + haUser.id +
				"   AND bg.year = " + year +
				"   AND bg.month = " + month +
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND cast(r.payment_date as date) >= to_date('" + strFirstDay + "', 'YYYYMMDD')" +
				"   AND cast(r.payment_date as date) < to_date('" + strNextFirst + "', 'YYYYMMDD')" +
				"   AND (b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_IN + "' OR b.balance_type_name = '" + Common.BALANCE_TYPE_PARLLET_OUT + "') " +
				"   AND r.parllet_mst_id IS NOT NULL "
				 + ((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				sqlGroupby +
				"";
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 収支取得用SQL作成(口座預入・口座引出)
	 * @param sLargeCategoryName
	 * @param year
	 * @param month
	 * @param dStartDay
	 * @param iDaysCnt
	 * @param haUser
	 * @param sFirstDay
	 * @param sNextFirst
	 * @return
	 */
	private String makeSqlBalBank(
			String strLargeCategoryName,	// 大分類行の名称「口座預入・口座引出」
			Integer year,
			Integer month,
			Date dStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst
			) {
		
		String sqlDaily = makeSqlBalDaily(dStartDay, iDaysCnt, strLargeCategoryName);
		
		String sqlSelGroupby = "" +
   				"   0 as item_id, 0 as item_order " +
   				"  ,cast('' as character varying(255)) as item_name" +
   				"  ," + (strLargeCategoryName.equals(Common.BALANCE_TYPE_BANK_IN) ? 50 : 60) + " as cate_order " +
				"  ,cast('" + strLargeCategoryName + "' as character varying(255)) as cate_name " +
   				"  ,0 as bg_id " +
				"";
		String sqlGroupby = "";
		
		String sql = "" +
				" SELECT " +
				sqlSelGroupby +
				"  ,NULL as bg_amount " +
				sqlDaily +
//				"  ,COALESCE(SUM(CASE WHEN b.balance_type_name = '" + Common.BALANCE_TYPE_BANK_IN + "' THEN r.amount ELSE -r.amount END), 0) as sum_month" +
				"  ,COALESCE(SUM(r.amount), 0) as sum_month" +
   				"  ,0 as prlt_link " +
   				" FROM Record r " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND cast(r.payment_date as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD')" +
				"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')" +
				"   AND (b.balance_type_name = '" + strLargeCategoryName + "') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				sqlGroupby +
				"";
		while(!(sql.equals(sql.replaceAll("  ", " "))))
			sql = sql.replaceAll("  ", " ");
		
		return sql;
	}
	
	/**
	 * 日計表から明細表へのリンクのための引数をセット
	 * @param calendar
	 * @param lngEach
	 * @param lngItemId
	 * @return
	 */
	private WkDaToDl makeWkDaToDl(
			Calendar calendar,
			String strLargeCategoryName,
			long lngEach,
			Long lngItemId
			) {
		
		WkDaToDl wkDaToDl = new WkDaToDl();
		wkDaToDl.setlAmount(lngEach);
		String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
		wkDaToDl.setsPaymentDateFr(sDate);
		wkDaToDl.setsPaymentDateTo(sDate);
		wkDaToDl.setlHandlingId(null);

		
		/** 合計行の場合 **/
		if(lngItemId==0) {
			// 「収入」・「支出」・「口座預入」・「口座引出」
			if(strLargeCategoryName.equals(Common.BALANCE_TYPE_IN) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_OUT) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_BANK_IN) ||
					strLargeCategoryName.equals(Common.BALANCE_TYPE_BANK_OUT)) {
				wkDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", strLargeCategoryName)).first()).id);
			}
			// 「Parllet預入・引出」
			if(strLargeCategoryName.equals(Common.BALANCE_TYPE_PARLLET_INOUT)) {
				wkDaToDl.setlParlletId((long) -2);		//Parllet＝NULLでない
			}
//			// 「Parlletから支払」
//			if(strLargeCategoryName.equals(Common.BALANCE_TYPE_OUT_PARLLET)) {
//				wkDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT)).first()).id);
//				wkDaToDl.setlParlletId((long) -2);		//Parllet＝NULLでない
//			}
			
			return wkDaToDl;
		}
		
		
		/** 明細行の場合 **/
		
		// 「収入」・「支出」
		if(strLargeCategoryName.equals(Common.BALANCE_TYPE_IN) ||
				strLargeCategoryName.equals(Common.BALANCE_TYPE_OUT)) {
			wkDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", strLargeCategoryName)).first()).id);
			wkDaToDl.setiItemId(lngItemId);
		}
		// 「Parllet預入・引出」
		if(strLargeCategoryName.equals(Common.BALANCE_TYPE_PARLLET_INOUT))
			wkDaToDl.setlParlletId(lngItemId);
//		// 「Parlletから支払」
//		if(strLargeCategoryName.equals(Common.BALANCE_TYPE_OUT_PARLLET)) {
//			wkDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT)).first()).id);
//			wkDaToDl.setlParlletId(lngItemId);
//		}
		// 「実残高」・「Parllet」
		if(strLargeCategoryName.equals(Common.REMAINDER_TYPE_REAL) ||
				strLargeCategoryName.equals(Common.REMAINDER_TYPE_PARLLET)) {
			calendar.add(Calendar.MONTH, -1);
			String sDateFr = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
			wkDaToDl.setsDebitDateFr(sDateFr);
			calendar.add(Calendar.MONTH, 7);
			String sDateTo = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
			wkDaToDl.setsDebitDateTo(sDateTo);
			// 「実残高」
			if(strLargeCategoryName.equals(Common.REMAINDER_TYPE_REAL)) {
				wkDaToDl.setlHandlingId(lngItemId);
			}
			// 「Parllet」
			if(strLargeCategoryName.equals(Common.REMAINDER_TYPE_PARLLET)) {
				wkDaToDl.setlParlletId(lngItemId);
			}
		}
		
		return wkDaToDl;
	}
	
	
	/**
	 * 単純に呼ばれた時の基準日のセット
	 * @param calendar
	 * @return
	 */
	private String setBasisDate() {
		//セッションに絞込条件が入っている時はセット
		if((session.get("daFilExistFlg") != null) &&
				(session.get("daFilExistFlg").equals("true")))
			return session.get("daStrBasisDate");
   		Calendar calendar = Calendar.getInstance();
//		//単純に呼ばれた時（初回等）は、今月１日をセット
//		return  String.format("%1$tY/%1$tm/01", calendar.getTime());
		//単純に呼ばれた時（初回等）は、本日をセット
		return  String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
	}
	
	/**
	 * 単純に呼ばれた時の基準日のセット(残高表)
	 * @param calendar
	 * @return
	 */
	private String setBasisDateBt() {
		//セッションに絞込条件が入っている時はセット
		if((session.get("btFilExistFlg") != null) &&
				(session.get("btFilExistFlg").equals("true")))
			return session.get("btStrBasisDate");
   		//単純に呼ばれた時（初回等）は、今日をセット
   		Calendar calendar = Calendar.getInstance();
		return  String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
	}
	
	/**
	 * 予算更新
	 * @param bg_basis_date
	 * @param e_budget_id
	 * @param e_budget_amount
	 */
	public static void updBudget(
			String bg_basis_date,
			List<Long> e_budget_id,			/* 変更行のID */
			List<String> e_large_category,	/* 変更行の大分類行の名称「収入」・「支出」・「Parllet預入」 */
			List<String> e_item,			/* 変更行の項目 */
			List<String> e_budget_amount	/* 変更行の金額 */
			) {
		
		Iterator<String> sELargeCategory = e_large_category.iterator();
		Iterator<String> sEItem = e_item.iterator();
   		Iterator<String> sEBudgetAmount = e_budget_amount.iterator();
		for (Long lngId : e_budget_id) {
			String strEBudgetAmountVal = sEBudgetAmount.next(); 
			String strELargeCategoryVal = sELargeCategory.next();
			String strEItemVal = sEItem.next();

			//予算が空白にされた時
			if(strEBudgetAmountVal.equals("")) {
				//既存レコードが無ければなにもしない
				if(lngId==0L)
					continue;
				//既存レコードがある場合レコード削除
				Budget budget = Budget.findById(lngId);
				budget.delete();
				
				continue;
			}
			//予算が入力された時
			//予算更新実処理
			actUpdBudget(
					bg_basis_date,
					lngId,
					strELargeCategoryVal,
					strEItemVal,
					strEBudgetAmountVal
					);
		}
		
		dailyAccount(bg_basis_date);
	}
	
	/**
	 * 予算更新実処理
	 * @param bg_basis_date
	 * @param lngId
	 * @param strELargeCategoryVal
	 * @param strEItemVal
	 * @param strEBudgetAmountVal
	 */
	private static void actUpdBudget(
			String bg_basis_date,
			Long lngId,
			String strELargeCategoryVal,
			String strEItemVal,
			String strEBudgetAmountVal
			) {
		//カンマ区切りの数値文字列を数値型に変換するNumberFormatクラスのインスタンスを取得する
		NumberFormat nf = NumberFormat.getInstance();

		try {
			//数値文字列をNumber型のオブジェクトに変換する
			Number nEBudgetAmount = nf.parse(strEBudgetAmountVal);
			//Number型のオブジェクトからInteger値を取得する
			Integer iEBudgetAmount = nEBudgetAmount.intValue();
			
			//既存レコードがある場合は更新
			if(lngId!=0L) {
				Budget budget = Budget.findById(lngId);
				// 変更有無チェック用のレコードにセット
				Budget eBudget = new Budget(
						budget.ha_user,
						budget.year,
						budget.month,
						iEBudgetAmount,
						budget.item_mst,
						budget.parllet_mst
						);
				
				// Validate
				validation.valid(eBudget);
				if(validation.hasErrors()) {
					dailyAccount(bg_basis_date);
				}
				// 項目が変更されていた行だけ更新
				if (budget.amount != eBudget.amount) {
					budget.amount = eBudget.amount;
					
					// 保存
					budget.save();
				}
				
				return;
			}
			//既存レコードが無い場合は新規登録
			HaUser haUser = (HaUser)renderArgs.get("haUser");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateFormat.getDateInstance().parse(bg_basis_date));
			ItemMst itemMst = null;
			ParlletMst parlletMst = null;
			//大分類が「Parllet預入・引出」の場合は「取扱(Parllet)」ごとの登録
			if(strELargeCategoryVal.equals(Common.BALANCE_TYPE_PARLLET_INOUT)) {
				parlletMst = ParlletMst.find("ha_user = " + haUser.id + " and parllet_name = '" + strEItemVal + "'").first();
			//大分類が「Parllet預入・引出」でない場合は「項目」ごとの登録
			} else {
				itemMst = ItemMst.find("ha_user = " + haUser.id + " and item_name = '" + strEItemVal + "'").first();
			}
			Budget budget = new Budget(
					haUser,
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH) + 1,
					iEBudgetAmount,
					itemMst,
					parlletMst
					);
			
			// Validate
			validation.valid(budget);
			if(validation.hasErrors()) {
				dailyAccount(bg_basis_date);
			}
			// 保存
			budget.save();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
