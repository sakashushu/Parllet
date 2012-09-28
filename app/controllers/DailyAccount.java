package controllers;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.BalanceTypeMst;
import models.Budget;
import models.HaUser;
import models.HandlingMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.Record;
import models.WkDaToDl;
import models.WkDailyAccount;
import models.WkDailyAccountRender;

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
	static final String VIEWS_DAILY_ACCOUNT = Messages.get("views.dailyaccount.dailyaccount");
	static final String VIEWS_BALANCE_TABLE = Messages.get("views.dailyaccount.balancetable");
	
	
	/**
	 * 日計表
	 * @param sBasisDate
	 */
	public static void dailyAccount(
			String sBasisDate
			) {
   		//単純に呼ばれた時の基準日のセット
		if(sBasisDate==null)
			sBasisDate = setBasisDate();

		Integer intBasisDate = null;
		if(sBasisDate!=null)
			intBasisDate = Integer.parseInt(sBasisDate.replace("/", ""));
		dailyAccountDisp(intBasisDate);
	}
	
	/**
	 * 残高表
	 * @param sBasisDate
	 */
	public static void balanceTable(
			String sBasisDate
			) {
   		//単純に呼ばれた時の基準日のセット
		if(sBasisDate==null)
			sBasisDate = setBasisDate();

		Integer intBasisDate = null;
		if(sBasisDate!=null)
			intBasisDate = Integer.parseInt(sBasisDate.replace("/", ""));
		balanceTableDisp(intBasisDate);
	}
	
	/**
	 * 日計表の表示
	 * @param intBasisDate
	 */
	public static void dailyAccountDisp(
			Integer intBasisDate
			) {
		String strTableType = VIEWS_DAILY_ACCOUNT;

		String sBasisDate = null;
		if(intBasisDate!=null) {
			String strTmp = intBasisDate.toString();
			sBasisDate = strTmp.substring(0, 4) + "/" + strTmp.substring(4, 6) + "/" + strTmp.substring(6, 8);
		}
		
		//日計表・残高表の表示用ワーク作成
		WkDailyAccountRender wkDAR = makeWkDAR(sBasisDate, strTableType);
		
		int month = wkDAR.getIntMonth();
		sBasisDate = wkDAR.getStrBasisDate();
		String[] sAryDays = wkDAR.getStrAryDays();
		List<WkDailyAccount> lWDA = wkDAR.getlWDA();
		int iWidth = wkDAR.getIntWidth();
		
		render("@dailyAccount",  month, sBasisDate, strTableType, sAryDays, lWDA, iWidth);
	}
	
	/**
	 * 残高表の表示
	 * @param sBasisDate
	 */
	public static void balanceTableDisp(
			Integer intBasisDate
			) {
		String strTableType = VIEWS_BALANCE_TABLE;

		String sBasisDate = null;
		if(intBasisDate!=null) {
			String strTmp = intBasisDate.toString();
			sBasisDate = strTmp.substring(0, 4) + "/" + strTmp.substring(4, 6) + "/" + strTmp.substring(6, 8);
		}
		
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
		session.put("daFilExistFlg", "true");
		session.put("daStrBasisDate", wkDAR.getStrBasisDate());
		
		//セッションのactionModeが空の時は閲覧モードをセット
		if(session.get("actionMode")==null)
			session.put("actionMode", "View");
		
		Date dBasis = null;
		try {
			dBasis = DateFormat.getDateInstance().parse(wkDAR.getStrBasisDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calendar.setTime(dBasis);
		int year = calendar.get(Calendar.YEAR);
		wkDAR.setIntMonth(calendar.get(Calendar.MONTH) + 1);

		int iDaysCnt = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		
		if(strTableType.equals(VIEWS_DAILY_ACCOUNT)) {
//			iDaysCnt = 3;
//			calendar.add(Calendar.DATE, -1);
		} else if(strTableType.equals(VIEWS_BALANCE_TABLE)) {
//			iDaysCnt = 1;
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
		

		//日計表の行に相当するリストの作成
		wkDAR.setlWDA(makeWorkList(year, wkDAR.getIntMonth(), dStartDay, iDaysCnt, strTableType));
   		
   		//日計表の日付ごとの部分のスクロール内の幅の設定
		wkDAR.setIntWidth(iDaysCnt * 100);
   		
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
    		String move			/* 「移動」ボタン */
			) {
		
		// 「移動」ボタンが押されていない時は処理を抜ける
		if(move==null)
			return;
		
		Calendar calendar = Calendar.getInstance();
		Date dBasis = null;
		try {
			dBasis = DateFormat.getDateInstance().parse(e_basis_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calendar.setTime(dBasis);
		if(strTableType.equals(VIEWS_DAILY_ACCOUNT)) {
			dailyAccount(e_basis_date);
			return;
		} 
		if(strTableType.equals(VIEWS_BALANCE_TABLE)) {
			balanceTable(e_basis_date);
			return;
		}
	}
	
	
	/**
	 * 日計表・残高表の行に相当するリストの作成
	 * @param year
	 * @param month
	 * @param dStartDay
	 * @param iDaysCnt
	 * @param strTableType
	 * @return
	 */
	private static List<WkDailyAccount> makeWorkList(
			Integer year,
			Integer month,
			Date dStartDay,
			int iDaysCnt,
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

		DailyAccount da = new DailyAccount();
		if(strTableType.equals(VIEWS_DAILY_ACCOUNT)) {
			//「収入」
			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_IN, lWDA);
			
			//「支出」
			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_OUT, lWDA);
			
			//「My貯金預入」
			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_IDEAL_DEPOSIT_IN, lWDA);
			
			
			//「My貯金から支払」
			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_OUT_IDEAL_DEPOSIT, lWDA);
			
			
			//「実残高」
			lWDA = da.addWorkListRemReal(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst, lWDA);
			
			//「My貯金残高」(My貯金してないお金を含む)
			lWDA = da.addWorkListRemIdealDepo(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst, lWDA);
			
			
		} else if(strTableType.equals(VIEWS_BALANCE_TABLE)) {
			//「実残高」
//			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					REMAINDER_TYPE_REAL, lWDA);
			lWDA = da.addWorkListRemReal(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst, lWDA);
			
//			//「My貯金してないお金」
//			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					REMAINDER_TYPE_NOT_IDEAL_DEPOSIT, lWDA);
//			
//			//「My貯金残高」
//			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					REMAINDER_TYPE_IDEAL_DEPOSIT, lWDA);
  	  	
  	  	
		}

		return lWDA;
	}
	

	
	/**
	 * 初日の残高取得用SQL作成（取扱合計）
	 * @param haUser
	 * @param sFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @return
	 */
	private String makeSqlRemAllFirstDay(
			HaUser haUser,
			String sFirstDay,
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
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + sFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"";
   		
		while(!(sqlFirstDay.equals(sqlFirstDay.replaceAll("  ", " "))))
			sqlFirstDay = sqlFirstDay.replaceAll("  ", " ");
		
		return sqlFirstDay;
	}
	
	/**
	 * 2日目以降の残高取得用SQL作成（取扱合計）
	 * @param dStartDay
	 * @param iDaysCnt
	 * @param haUser
	 * @param sFirstDay
	 * @param sNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @return
	 */
	private String makeSqlRemAllLater(
			Date dStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut
			) {
		
   		String sqlDaily = "";
   		
   		Calendar calendar = Calendar.getInstance();

   		calendar.setTime(dStartDay);
   		
		//日付毎の合計取得部分のSQL(現金)
		calendar.setTime(dStartDay);
		for(int iDay = 2; iDay <= iDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
					"     WHEN cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
					"       CASE " +
					sqlSumAllCaseInOut +	//収入加算・支出減算
					"       END " +
					"     ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}

		String sqlLater = "" +
   				" SELECT " +
   				sqlDaily +
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				"";
   		
		while(!(sqlLater.equals(sqlLater.replaceAll("  ", " "))))
			sqlLater = sqlLater.replaceAll("  ", " ");
		
		return sqlLater;
	}
	
	/**
	 * 初日の残高取得用SQL作成（取扱毎）
	 * @param bolEach
	 * @param haUser
	 * @param sFirstDay
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @param sqlSumCashCaseBankInOut
	 * @param sqlSumNotCashCaseBankInOut
	 * @return
	 */
	private String makeSqlRemEachFirstDay(
			HaUser haUser,
			String sFirstDay,
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
				"  ,'" + HANDLING_TYPE_CASH + "' as hd_handling_name " +
				"  ,0 as hd_id " +
				"  ,false as hd_zero_hidden " +
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    ht.handling_type_name = '" + HANDLING_TYPE_CASH + "' " +
				"            AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') " +
				"            ) " +
				"        OR (    ht.handling_type_name in('" + HANDLING_TYPE_BANK + "','" + HANDLING_TYPE_EMONEY + "') " +
				"            AND b.balance_type_name in('" + BALANCE_TYPE_BANK_OUT + "','" + BALANCE_TYPE_BANK_IN + "') " +
				"            ) " +
				"        ) " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + sFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				" GROUP BY hd_id, hd_handling_name " +
				"";
		
		//現金以外取得用SQL
		String sqlNotCash = "" +
				" SELECT " +
				sqlNotCashDaily +		//日付毎の合計取得部分(現金以外)
				"  ,CASE " +
				"     WHEN ht.handling_type_name = '" + HANDLING_TYPE_CRECA + "' THEN " +
				"       hb.handling_name " +
				"     ELSE " +
				"       h.handling_name " +
				"     END as hd_handling_name " +
				"  ,CASE " +
				"     WHEN ht.handling_type_name = '" + HANDLING_TYPE_CRECA + "' THEN " +
				"       hb.id " +
				"     ELSE " +
				"       h.id " +
				"   END as hd_id " +
				"  ,CASE " +
				"     WHEN ht.handling_type_name = '" + HANDLING_TYPE_CRECA + "' THEN " +
				"       hb.zero_hidden " +
				"     ELSE " +
				"       h.zero_hidden " +
				"   END as hd_zero_hidden " +
				sqlFromPhrase +			//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "','" + BALANCE_TYPE_BANK_OUT + "','" + BALANCE_TYPE_BANK_IN + "') " +
				"   AND h.handling_name <> '" + HANDLING_TYPE_CASH + "' " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + sFirstDay + "', 'YYYYMMDD') " +
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
	 * 2日目以降の残高取得用SQL作成（取扱毎）
	 * @param bolEach
	 * @param dStartDay
	 * @param iDaysCnt
	 * @param haUser
	 * @param sFirstDay
	 * @param sNextFirst
	 * @param sqlFromPhrase
	 * @param sqlSumAllCaseInOut
	 * @param sqlSumCashCaseBankInOut
	 * @param sqlSumNotCashCaseBankInOut
	 * @return
	 */
	private String makeSqlRemEachLater(
			Date dStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst,
			String sqlFromPhrase,
			String sqlSumAllCaseInOut,
			String sqlSumCashCaseBankInOut,
			String sqlSumNotCashCaseBankInOut
			) {
		
		Calendar calendar = Calendar.getInstance();
		
		String sqlCashDaily = "";
		String sqlNotCashDaily = "";
		
		//日付毎の合計取得部分のSQL(現金)
		calendar.setTime(dStartDay);
		for(int iDay = 2; iDay <= iDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlCashDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
					"     WHEN cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
					"       CASE " +
					sqlSumAllCaseInOut +		//収入加算・支出減算
					sqlSumCashCaseBankInOut +	//口座引出加算・口座預入減算
					"       END " +
					"     ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		
		//日付毎の合計取得部分のSQL(現金以外)
		calendar.setTime(dStartDay);
		for(int iDay = 2; iDay <= iDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlNotCashDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
					"     WHEN cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
					"       CASE " +
					sqlSumAllCaseInOut +			//収入加算・支出減算
					sqlSumNotCashCaseBankInOut +	//口座引出減算・口座預入加算
					"       END " +
					"     ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		

		//現金取得用SQL
		String sqlCash = "" +
				" SELECT " +
				sqlCashDaily +		//日付毎の合計取得部分(現金)
				"  ,'" + HANDLING_TYPE_CASH + "' as hd_handling_name " +
				"  ,0 as hd_id " +
				"  ,false as hd_zero_hidden " +
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    ht.handling_type_name = '" + HANDLING_TYPE_CASH + "' " +
				"            AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') " +
				"            ) " +
				"        OR (    ht.handling_type_name in('" + HANDLING_TYPE_BANK + "','" + HANDLING_TYPE_EMONEY + "') " +
				"            AND b.balance_type_name in('" + BALANCE_TYPE_BANK_OUT + "','" + BALANCE_TYPE_BANK_IN + "') " +
				"            ) " +
				"        ) " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD') " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD') " +
				" GROUP BY hd_id, hd_handling_name " +
				"";
		
		//現金以外取得用SQL
		String sqlNotCash = "" +
				" SELECT " +
				sqlNotCashDaily +		//日付毎の合計取得部分(現金以外)
				"  ,CASE " +
				"     WHEN ht.handling_type_name = '" + HANDLING_TYPE_CRECA + "' THEN " +
				"       hb.handling_name " +
				"     ELSE " +
				"       h.handling_name " +
				"     END as hd_handling_name " +
				"  ,CASE " +
				"     WHEN ht.handling_type_name = '" + HANDLING_TYPE_CRECA + "' THEN " +
				"       hb.id " +
				"     ELSE " +
				"       h.id " +
				"   END as hd_id " +
				"  ,CASE " +
				"     WHEN ht.handling_type_name = '" + HANDLING_TYPE_CRECA + "' THEN " +
				"       hb.zero_hidden " +
				"     ELSE " +
				"       h.zero_hidden " +
				"   END as hd_zero_hidden " +
				sqlFromPhrase +			//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "','" + BALANCE_TYPE_BANK_OUT + "','" + BALANCE_TYPE_BANK_IN + "') " +
				"   AND h.handling_name <> '" + HANDLING_TYPE_CASH + "' " +
				"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD') " +
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
	 * 日計表の行に相当するリストの作成（実残高）
	 * @param year
	 * @param month
	 * @param dStartDay
	 * @param iDaysCnt
	 * @param haUser
	 * @param sFirstDay
	 * @param sNextFirst
	 * @param lWDA
	 * @return
	 */
	private List<WkDailyAccount> addWorkListRemReal(
			Integer year,
			Integer month,
			Date dStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst,
			List<WkDailyAccount> lWDA
			) {
		String strSql = "";
		Calendar calendar = Calendar.getInstance();
		
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
		
		//CASE文内 収入加算・支出減算
		String sqlSumAllCaseInOut = "" +
				" WHEN b.balance_type_name = '" + BALANCE_TYPE_IN + "' THEN r.amount " +
				" WHEN b.balance_type_name = '" + BALANCE_TYPE_OUT + "' THEN -r.amount " +
				"";
		
		//CASE文内 口座引出加算・口座預入減算
		String sqlSumCashCaseBankInOut = "" +
				" WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_OUT + "' THEN r.amount " +
				" WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_IN + "' THEN -r.amount " +
				"";
		
		//CASE文内 口座引出減算・口座預入加算
		String sqlSumNotCashCaseBankInOut = "" +
				" WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_OUT + "' THEN -r.amount" +
				" WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_IN + "' THEN r.amount" +
				"";
		
		String sqlDailyLater = "";
		String sqlDailyZero = "";
		calendar.setTime(dStartDay);
		for(int iDay = 1; iDay <= iDaysCnt; iDay++) {
			sqlDailyZero += "" +
					" OR  sum_day_" + iDay + " <> 0 ";
			calendar.add(Calendar.DATE, 1);
			if(iDay > 1)
				sqlDailyLater += "" +
						" ,COALESCE(rem_later.sum_day_" + iDay + ", 0) as sum_day_" + iDay + " " +
						"";
		}
		
   		/** 
   		 * 合計行
   		 */
   		WkDailyAccount wDA = new WkDailyAccount();
   		long lSumMonthG = 0L;
		List<WkDaToDl> lstWdtdG = new ArrayList<WkDaToDl>();
		if(Calendar.getInstance().get(Calendar.DATE)==32) {	//有り得ない日付(32)で条件付けし、if文内を通らないようにしている。日計表画面でアコーディオンで明細を開く機能を追加したら、その条件で通るようにする予定。
			//初日の残高取得用SQL作成（取扱合計）
			String sqlAllFirstDay = makeSqlRemAllFirstDay(
					haUser,
					sFirstDay,
					sqlFromPhrase,
					sqlSumAllCaseInOut
					);
			//2日目以降の残高取得用SQL作成（取扱合計）
			String sqlAllLater = makeSqlRemAllLater(
					dStartDay,
					iDaysCnt,
					haUser,
					sFirstDay,
					sNextFirst,
					sqlFromPhrase,
					sqlSumAllCaseInOut
					);
			
			strSql = "" +
					" SELECT " +
					"   *" +
					" FROM ( " + sqlAllFirstDay + " ) rem_firstday " +
					" CROSS JOIN (" + sqlAllLater + " ) rem_later " +
					"";
			while(!(strSql.equals(strSql.replaceAll("  ", " "))))
				strSql = strSql.replaceAll("  ", " ");
	   		
	   		Object[] obj = null;
			obj = (Object[])JPA.em().createNativeQuery(
					strSql).getSingleResult();
			lSumMonthG = obj[0]==null ? 0L : Long.parseLong(String.valueOf(obj[0]));
			wDA.setLSumMonth(lSumMonthG);
			wDA.setsLargeCategory(REMAINDER_TYPE_REAL);
			wDA.setsItem("");
			wDA.setbBudgetFlg(false);
			
			// 日毎
			calendar.setTime(dStartDay);
			long lAmount = 0L;
			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
				lAmount += Long.parseLong(String.valueOf(obj[iDay]));
				WkDaToDl workDaToDl = new WkDaToDl();
				workDaToDl.setlAmount(lAmount);
				String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
				workDaToDl.setsPaymentDateFr(sDate);
				workDaToDl.setsPaymentDateTo(sDate);
				workDaToDl.setlHandlingId(null);
				lstWdtdG.add(workDaToDl);
				calendar.add(Calendar.DATE, 1);
			}
			wDA.setLstWdtd(lstWdtdG);
			
			lWDA.add(wDA);
			
			return lWDA;
		}
		
		
		/**
		 *  取扱(実際)毎の行
		 * （クレジットカードは除いて、引落口座に集約）
		 */
		//合計行は先にリストに追加し、最後に中身に明細行からの加算合計をセット
		lWDA.add(wDA);
		long[] lAryDaysRlAll = new long[iDaysCnt];
		
		//初日の残高取得用SQL作成（取扱(実際)毎）
		String sqlEachFirstDay = makeSqlRemEachFirstDay(
				haUser,
				sFirstDay,
				sqlFromPhrase,
				sqlSumAllCaseInOut,
				sqlSumCashCaseBankInOut,
				sqlSumNotCashCaseBankInOut
				);
		//2日目以降の残高取得用SQL作成（取扱(実際)毎）
		String sqlEachLater = makeSqlRemEachLater(
				dStartDay,
				iDaysCnt,
				haUser,
				sFirstDay,
				sNextFirst,
				sqlFromPhrase,
				sqlSumAllCaseInOut,
				sqlSumCashCaseBankInOut,
				sqlSumNotCashCaseBankInOut
				);
		
		strSql = "" +
				" SELECT " +
				"   rem_firstday.hd_id " +
				"  ,rem_firstday.hd_handling_name" +
				"  ,rem_firstday.sum_day_1" + sqlDailyLater +
				" FROM ( " + sqlEachFirstDay + " ) rem_firstday " +
				" LEFT JOIN (" + sqlEachLater + " ) rem_later " +
				" ON rem_firstday.hd_id = rem_later.hd_id " +
				" WHERE rem_firstday.hd_zero_hidden = false " + sqlDailyZero +
				" ORDER by rem_firstday.hd_id" +
				"";
		while(!(strSql.equals(strSql.replaceAll("  ", " "))))
			strSql = strSql.replaceAll("  ", " ");
		
		List<Object[]> lstObjEach = JPA.em().createNativeQuery(
				strSql
				).getResultList();
		
		int intItemCnt = lstObjEach.size();
		int intCnt = 0;
		for(Object[] objEach : lstObjEach) {
			WkDailyAccount wDaHandling = new WkDailyAccount();
			
			wDaHandling.setsLargeCategory(REMAINDER_TYPE_REAL);
			wDaHandling.setsItem(String.valueOf(objEach[1]));
			wDaHandling.setbBudgetFlg(false);
	
			//項目の最終行フラグ
			intCnt++;
			if(intItemCnt==intCnt) {
				wDaHandling.setBolLastItemFlg(true);
			}
					
			// 日毎
			calendar.setTime(dStartDay);
			long lngSum = 0L;
			List<WkDaToDl> lstWdtd = new ArrayList<WkDaToDl>();
			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
				//初日のみそのままの値。2日目以降は加算。
				lngSum += Long.parseLong(String.valueOf(objEach[iDay+2]));
				lAryDaysRlAll[iDay] += lngSum;
				
				WkDaToDl workDaToDl = new WkDaToDl();
				workDaToDl.setlAmount(lngSum);
				String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
				workDaToDl.setsPaymentDateFr(sDate);
				workDaToDl.setsPaymentDateTo(sDate);
//				workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
				workDaToDl.setlHandlingId(null);
//				//取扱(My貯金)＝NULL
//				workDaToDl.setlIdealDepositId((long) -1);
////						workDaToDl.setiItemId(((ItemMst)(ItemMst.find("byItem_name", itemMst.item_name)).first()).id);
//				workDaToDl.setiItemId(Long.parseLong(String.valueOf(objEach[iDaysCnt+2])));
				lstWdtd.add(workDaToDl);
				
				calendar.add(Calendar.DATE, 1);
			}
			wDaHandling.setLstWdtd(lstWdtd);
			
			lWDA.add(wDaHandling);
			
		}
		
		//合計行の中身に中身に明細行からの加算合計をセット
//		wDA.setLSumMonth(lSumMonthG);
		wDA.setsLargeCategory(REMAINDER_TYPE_REAL);
		wDA.setsItem("");
		wDA.setbBudgetFlg(false);
		calendar.setTime(dStartDay);
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			WkDaToDl wkDaToDlG = new WkDaToDl();
			wkDaToDlG.setlAmount(lAryDaysRlAll[iDay]);
			String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
			wkDaToDlG.setsPaymentDateFr(sDate);
			wkDaToDlG.setsPaymentDateTo(sDate);
			wkDaToDlG.setlHandlingId(null);
			lstWdtdG.add(wkDaToDlG);
			calendar.add(Calendar.DATE, 1);
		}
		wDA.setLstWdtd(lstWdtdG);
		
		
		return lWDA;
	}
	
	
	
	
	
	
	
	
	
	
	private String makeSqlRemIdealEachFirstDay(
			HaUser haUser,
			String sFirstDay,
			String sqlFromPhrase,
			String sqlSumCaseIdealDepoInOut
			) {
		
		//日付毎の合計取得部分のSQL
		String sqlDaily = "" +
				" COALESCE(SUM(" +
				"   CASE " +
				sqlSumCaseIdealDepoInOut +		//My貯金から直接支払減算・My貯金に直接入金加算・My貯金預入加算・My貯金引出減算
				"   END " +
				" ), 0) as sum_day_1";
		
		//SQL
		String sqlFirstDay = "" +
				" SELECT " +
				sqlDaily +		//日付毎の合計取得部分
   				"   ,id.ideal_deposit_name as id_ideal_deposit_name " +
   				"   ,id.id as id_id " +
   				"   ,id.zero_hidden as id_zero_hidden " +
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') " +
				"            AND r.ideal_deposit_mst_id IS NOT NULL " +
				"            ) " +
   				"        OR b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "') " +
   				"        ) " +
				"   AND cast(r.payment_date as date) <= to_date('" + sFirstDay + "', 'YYYYMMDD') " +
				((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
				" GROUP BY id.id, id.ideal_deposit_name, id.zero_hidden" +
				"";

		while(!(sqlFirstDay.equals(sqlFirstDay.replaceAll("  ", " "))))
			sqlFirstDay = sqlFirstDay.replaceAll("  ", " ");
		
		return sqlFirstDay;
	}
	
	private String makeSqlRemIdealEachLater(
			Date dStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst,
			String sqlFromPhrase,
			String sqlSumCaseIdealDepoInOut
			) {
		
		Calendar calendar = Calendar.getInstance();
		
		String sqlDaily = "";
		
		//日付毎の合計取得部分のSQL
		calendar.setTime(dStartDay);
		for(int iDay = 2; iDay <= iDaysCnt; iDay++) {
			calendar.add(Calendar.DATE, 1);
			sqlDaily += "" +
					(iDay==2 ? " " : ",") +
					" COALESCE(SUM(" +
					"   CASE " +
					"     WHEN cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
					"       CASE " +
					sqlSumCaseIdealDepoInOut +		//My貯金から直接支払減算・My貯金に直接入金加算・My貯金預入加算・My貯金引出減算
					"       END " +
					"     ELSE 0 " +
					"   END" +
					" ), 0) as sum_day_" + iDay + " ";
		}
		
		//SQL
		String sqlLater = "" +
				" SELECT " +
				sqlDaily +		//日付毎の合計取得部分
   				"   ,id.ideal_deposit_name as id_ideal_deposit_name " +
   				"   ,id.id as id_id " +
   				"   ,id.zero_hidden as id_zero_hidden " +
				sqlFromPhrase +		//FROM句
				" WHERE r.ha_user_id = " + haUser.id +
				"   AND (   (    b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') " +
				"            AND r.ideal_deposit_mst_id IS NOT NULL " +
				"            ) " +
   				"        OR b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "') " +
   				"        ) " +
				"   AND cast(r.payment_date as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD') " +
				"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD') " +
				" GROUP BY id.id, id.ideal_deposit_name, id.zero_hidden" +
				"";
		
		while(!(sqlLater.equals(sqlLater.replaceAll("  ", " "))))
			sqlLater = sqlLater.replaceAll("  ", " ");
		
		return sqlLater;
	}
	
	
	
	private List<WkDailyAccount> addWorkListRemIdealDepo(
			Integer year,
			Integer month,
			Date dStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst,
			List<WkDailyAccount> lWDA
			) {
		String strSql = "";
		Calendar calendar = Calendar.getInstance();
		
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
				" LEFT JOIN IdealDepositMst id " +
				"   ON r.ideal_deposit_mst_id = id.id " +
				" LEFT JOIN HandlingMst h " +
				"   ON r.handling_mst_id = h.id " +
				" LEFT JOIN HandlingTypeMst ht " +
				"   ON h.handling_type_mst_id = ht.id" +
				"";
		
		//CASE文内 My貯金から直接支払減算・My貯金に直接入金加算・My貯金預入加算・My貯金引出減算
		String sqlSumCaseIdealDepoInOut = "" +
   				"        WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
   				"              r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
   				"        WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND " +
   				"              r.ideal_deposit_mst_id IS NOT NULL) THEN r.amount" +
   				"        WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN r.amount" +
   				"        WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN -r.amount" +
				"";

		String sqlDailyLater = "";
		String sqlDailyZero = "";
		calendar.setTime(dStartDay);
		for(int iDay = 1; iDay <= iDaysCnt; iDay++) {
			sqlDailyZero += "" +
					" OR  sum_day_" + iDay + " <> 0 ";
			calendar.add(Calendar.DATE, 1);
			if(iDay > 1)
				sqlDailyLater += "" +
						" ,COALESCE(rem_later.sum_day_" + iDay + ", 0) as sum_day_" + iDay + " ";
		}
		
   		/** 
   		 * 合計行
   		 */
   		WkDailyAccount wDA = new WkDailyAccount();
   		long lSumMonthG = 0L;
		List<WkDaToDl> lstWdtdG = new ArrayList<WkDaToDl>();
		if(Calendar.getInstance().get(Calendar.DATE)==32) {	//有り得ない日付(32)で条件付けし、if文内を通らないようにしている。日計表画面でアコーディオンで明細を開く機能を追加したら、その条件で通るようにする予定。
			//初日の残高取得用SQL作成（取扱合計）
			String sqlAllFirstDay = makeSqlRemAllFirstDay(
					haUser,
					sFirstDay,
					sqlFromPhrase,
					sqlSumCaseIdealDepoInOut
					);
			//2日目以降の残高取得用SQL作成（取扱合計）
			String sqlAllLater = makeSqlRemAllLater(
					dStartDay,
					iDaysCnt,
					haUser,
					sFirstDay,
					sNextFirst,
					sqlFromPhrase,
					sqlSumCaseIdealDepoInOut
					);
			
			strSql = "" +
					" SELECT " +
					"   *" +
					" FROM ( " + sqlAllFirstDay + " ) rem_firstday " +
					" CROSS JOIN (" + sqlAllLater + " ) rem_later " +
					"";
			while(!(strSql.equals(strSql.replaceAll("  ", " "))))
				strSql = strSql.replaceAll("  ", " ");
	   		
	   		Object[] obj = null;
			obj = (Object[])JPA.em().createNativeQuery(
					strSql).getSingleResult();
			lSumMonthG = obj[0]==null ? 0L : Long.parseLong(String.valueOf(obj[0]));
			wDA.setLSumMonth(lSumMonthG);
			wDA.setsLargeCategory(REMAINDER_TYPE_REAL);
			wDA.setsItem("");
			wDA.setbBudgetFlg(false);
			
			// 日毎
			calendar.setTime(dStartDay);
			long lAmount = 0L;
			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
				lAmount += Long.parseLong(String.valueOf(obj[iDay]));
				WkDaToDl workDaToDl = new WkDaToDl();
				workDaToDl.setlAmount(lAmount);
				String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
				workDaToDl.setsPaymentDateFr(sDate);
				workDaToDl.setsPaymentDateTo(sDate);
				workDaToDl.setlHandlingId(null);
				lstWdtdG.add(workDaToDl);
				calendar.add(Calendar.DATE, 1);
			}
			wDA.setLstWdtd(lstWdtdG);
			
			lWDA.add(wDA);
			
			return lWDA;
		}
		
		
		/**
		 *  取扱(My貯金)毎の行
		 */
		//合計行は先にリストに追加し、最後に中身に明細行からの加算合計をセット
		lWDA.add(wDA);
		long[] lAryDaysIdealAll = new long[iDaysCnt];
		
		//初日の残高取得用SQL作成（取扱(My貯金)毎）
		String sqlEachFirstDay = makeSqlRemIdealEachFirstDay(
				haUser,
				sFirstDay,
				sqlFromPhrase,
				sqlSumCaseIdealDepoInOut
				);
		//2日目以降の残高取得用SQL作成（取扱(My貯金)毎）
		String sqlEachLater = makeSqlRemIdealEachLater(
				dStartDay,
				iDaysCnt,
				haUser,
				sFirstDay,
				sNextFirst,
				sqlFromPhrase,
				sqlSumCaseIdealDepoInOut
				);
		
		
		strSql = "" +
				" SELECT " +
				"   rem_firstday.id_id " +
				"  ,rem_firstday.id_ideal_deposit_name" +
				"  ,rem_firstday.sum_day_1" + sqlDailyLater +
				" FROM ( " + sqlEachFirstDay + " ) rem_firstday " +
				" LEFT JOIN (" + sqlEachLater + " ) rem_later " +
				" ON rem_firstday.id_id = rem_later.id_id " +
				" WHERE rem_firstday.id_zero_hidden = false " + sqlDailyZero +
				" ORDER by rem_firstday.id_id" +
				"";
		while(!(strSql.equals(strSql.replaceAll("  ", " "))))
			strSql = strSql.replaceAll("  ", " ");
		
		List<Object[]> lstObjEach = JPA.em().createNativeQuery(
				strSql
				).getResultList();
		
		int intItemCnt = lstObjEach.size();
		int intCnt = 0;
		for(Object[] objEach : lstObjEach) {
			WkDailyAccount wDaHandling = new WkDailyAccount();
			
			wDaHandling.setsLargeCategory(REMAINDER_TYPE_IDEAL_DEPOSIT);
			wDaHandling.setsItem(String.valueOf(objEach[1]));
			wDaHandling.setbBudgetFlg(false);
	
			//項目の最終行フラグ
			intCnt++;
			if(intItemCnt==intCnt) {
				wDaHandling.setBolLastItemFlg(true);
			}
					
			// 日毎
			calendar.setTime(dStartDay);
			long lngSum = 0L;
			List<WkDaToDl> lstWdtd = new ArrayList<WkDaToDl>();
			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
				//初日のみそのままの値。2日目以降は加算。
				lngSum += Long.parseLong(String.valueOf(objEach[iDay+2]));
				lAryDaysIdealAll[iDay] += lngSum;
				
				WkDaToDl workDaToDl = new WkDaToDl();
				workDaToDl.setlAmount(lngSum);
				String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
				workDaToDl.setsPaymentDateFr(sDate);
				workDaToDl.setsPaymentDateTo(sDate);
//				workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
				workDaToDl.setlHandlingId(null);
//				//取扱(My貯金)＝NULL
//				workDaToDl.setlIdealDepositId((long) -1);
////						workDaToDl.setiItemId(((ItemMst)(ItemMst.find("byItem_name", itemMst.item_name)).first()).id);
//				workDaToDl.setiItemId(Long.parseLong(String.valueOf(objEach[iDaysCnt+2])));
				lstWdtd.add(workDaToDl);
				
				calendar.add(Calendar.DATE, 1);
			}
			wDaHandling.setLstWdtd(lstWdtd);
			
			lWDA.add(wDaHandling);
			
		}
		
		//合計行の中身に中身に明細行からの加算合計をセット
//		wDA.setLSumMonth(lSumMonthG);
		wDA.setsLargeCategory(REMAINDER_TYPE_IDEAL_DEPOSIT);
		wDA.setsItem("");
		wDA.setbBudgetFlg(false);
		calendar.setTime(dStartDay);
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			WkDaToDl wkDaToDlG = new WkDaToDl();
			wkDaToDlG.setlAmount(lAryDaysIdealAll[iDay]);
			String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
			wkDaToDlG.setsPaymentDateFr(sDate);
			wkDaToDlG.setsPaymentDateTo(sDate);
			wkDaToDlG.setlHandlingId(null);
			lstWdtdG.add(wkDaToDlG);
			calendar.add(Calendar.DATE, 1);
		}
		wDA.setLstWdtd(lstWdtdG);
		
		
		return lWDA;

		
		
		
		
		

//	   	//「My貯金残高」
//   		if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
//   			
//			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
//				strSqlDaily += "" +
//						",COALESCE(SUM(" +
//						"  CASE " +
//						"    WHEN cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
//		   				"      CASE " +
//		   				"        WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
//		   				"              r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
//		   				"        WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND " +
//		   				"              r.ideal_deposit_mst_id IS NOT NULL) THEN r.amount" +
//		   				"        WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN r.amount" +
//		   				"        WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN -r.amount" +
//		   				"      END" +
//						"    ELSE 0 " +
//						"  END" +
//		   				" ), 0) as rem_day_" + iDay + "";
//				calendar.add(Calendar.DATE, 1);
//			}
//   			
//	   		sSqlBase = "" +
//	   				" SELECT COALESCE(SUM(" +
//	   				"   CASE " +
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
//	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND " +
//	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN r.amount" +
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN r.amount" +
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN -r.amount" +
//	   				"   END" +
//	   				"   ), 0) " +
/////	   				" FROM Record r " +
/////					" LEFT JOIN ItemMst i " +
/////					"   ON r.item_mst_id = i.id " +
/////					" LEFT JOIN BalanceTypeMst b " +
/////					"   ON r.balance_type_mst_id = b.id " +
/////					" LEFT JOIN IdealDepositMst id " +
/////					"   ON r.ideal_deposit_mst_id = id.id " +
/////					" LEFT JOIN HandlingMst h " +
/////					"   ON r.handling_mst_id = h.id " +
/////					" LEFT JOIN HandlingTypeMst ht " +
/////					"   ON h.handling_type_mst_id = ht.id" +
//					" WHERE r.ha_user_id = " + haUser.id;
//	   		sSqlBaseG = "" +
//					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
//
//   			sSqlBaseEach = "" +
//	   				" SELECT COALESCE(SUM(" +
//	   				"   CASE " +
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
//	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND " +
//	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN r.amount" +
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN r.amount" +
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN -r.amount" +
//	   				"   END" +
//	   				"   ), 0) as rem_month" +
//	   				strSqlDaily +
//	   				"   ,id.ideal_deposit_name " +
//	   				"   ,id.id as ideal_deposit_id " +
//	   				"   ,id.zero_hidden " +
/////	   				" FROM Record r " +
/////					" LEFT JOIN ItemMst i " +
/////					"   ON r.item_mst_id = i.id " +
/////					" LEFT JOIN BalanceTypeMst b " +
/////					"   ON r.balance_type_mst_id = b.id " +
/////					" LEFT JOIN IdealDepositMst id " +
/////					"   ON r.ideal_deposit_mst_id = id.id " +
/////					" LEFT JOIN HandlingMst h " +
/////					"   ON r.handling_mst_id = h.id " +
/////					" LEFT JOIN HandlingTypeMst ht " +
/////					"   ON h.handling_type_mst_id = ht.id" +
//					" WHERE r.ha_user_id = " + haUser.id;
//	   		
//	   		
//	   		
//	   	//「My貯金してないお金」
//   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
//   			
//			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
//				strSqlDaily += "" +
//						",COALESCE(SUM(" +
//						"  CASE " +
//						"    WHEN cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN " +
//		   				"      CASE " +
//		   				//「収入」で「取扱(My貯金)」未選択は加算
//		   				"        WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND" +
//		   				"              r.ideal_deposit_mst_id IS NULL) THEN r.amount " +
//		   				//「支出」で「取扱(My貯金)」未選択は減算
//		   				"        WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
//		   				"              r.ideal_deposit_mst_id IS NULL) THEN -r.amount " +
//		   				//「My貯金預入」は減算
//		   				"        WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN -r.amount" +
//		   				//「My貯金引出」は加算
//		   				"        WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN r.amount" +
//		   				"      END" +
//						"    ELSE 0 " +
//						"  END" +
//		   				" ), 0) as rem_day_" + iDay + "";
//				calendar.add(Calendar.DATE, 1);
//			}
//   			
//	   		sSqlBase = "" +
//	   				" SELECT COALESCE(SUM(" +
//	   				"   CASE " +
//	   				//「収入」で「取扱(My貯金)」未選択は加算
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND" +
//	   				"           r.ideal_deposit_mst_id IS NULL) THEN r.amount " +
//	   				//「支出」で「取扱(My貯金)」未選択は減算
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
//	   				"           r.ideal_deposit_mst_id IS NULL) THEN -r.amount " +
//	   				//「My貯金預入」は減算
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN -r.amount" +
//	   				//「My貯金引出」は加算
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN r.amount" +
//	   				"   END" +
//	   				"   ), 0) " +
/////	   				" FROM Record r " +
/////					" LEFT JOIN ItemMst i " +
/////					"   ON r.item_mst_id = i.id " +
/////					" LEFT JOIN BalanceTypeMst b " +
/////					"   ON r.balance_type_mst_id = b.id " +
/////					" LEFT JOIN IdealDepositMst id " +
/////					"   ON r.ideal_deposit_mst_id = id.id " +
/////					" LEFT JOIN HandlingMst h " +
/////					"   ON r.handling_mst_id = h.id " +
/////					" LEFT JOIN HandlingTypeMst ht " +
/////					"   ON h.handling_type_mst_id = ht.id" +
//					" WHERE r.ha_user_id = " + haUser.id +
//					""
//					;
//	   		sSqlBaseG = "" +
//					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
//
//   			sSqlBaseEach = "" +
//	   				" SELECT COALESCE(SUM(" +
//	   				"   CASE " +
//	   				//「収入」で「取扱(My貯金)」未選択は加算
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND" +
//	   				"           r.ideal_deposit_mst_id IS NULL) THEN r.amount " +
//	   				//「支出」で「取扱(My貯金)」未選択は減算
//	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
//	   				"           r.ideal_deposit_mst_id IS NULL) THEN -r.amount " +
//	   				//「My貯金預入」は減算
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN -r.amount" +
//	   				//「My貯金引出」は加算
//	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN r.amount" +
//	   				"   END" +
//	   				"   ), 0) " +
//	   				strSqlDaily +
/////	   				" FROM Record r " +
/////					" LEFT JOIN ItemMst i " +
/////					"   ON r.item_mst_id = i.id " +
/////					" LEFT JOIN BalanceTypeMst b " +
/////					"   ON r.balance_type_mst_id = b.id " +
/////					" LEFT JOIN IdealDepositMst id " +
/////					"   ON r.ideal_deposit_mst_id = id.id " +
/////					" LEFT JOIN HandlingMst h " +
/////					"   ON r.handling_mst_id = h.id " +
/////					" LEFT JOIN HandlingTypeMst ht " +
/////					"   ON h.handling_type_mst_id = ht.id" +
//					" WHERE r.ha_user_id = " + haUser.id;
//   		
//   		
//   		}
//
//   		
//   		
//   		
//   		//「My貯金残高」
//   		if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
//   			sSql = sSqlBase + sSqlBaseG +
//					"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
//	   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
//	   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
//	   				"        )";
//   		//「My貯金してないお金」
//   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
//   			sSql = sSqlBase + sSqlBaseG +
//					"   AND ((b.balance_type_name in('" + BALANCE_TYPE_IN + "','" + BALANCE_TYPE_OUT + "') AND " +
//					"         r.ideal_deposit_mst_id IS NULL) OR " +
//   				    "        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "') " +
//	   				"        )";
//   		}   		
//   		
//   		
//   		
//		//「My貯金残高」
//		if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
//			
//			sSql = sSqlBase + sSqlBaseG +
//					"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
//	   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
//	   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
//	   				"        )" +
////					"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'" +
//					((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
//					" GROUP BY id.id, id.ideal_deposit_name, id.zero_hidden" +
//					" ORDER BY id.id" +
//					"";
//		}
//   		
//		
//		List<Object[]> lstObjEach = null;
//		lstObjEach = JPA.em().createNativeQuery(
//				strSqlEach).getResultList();
//
//		int intIdealDepoCnt = lstObjEach.size();
//		for(Object[] objEach : lstObjEach) {
//			
//			WkDailyAccount wDaIdealDepo = new WkDailyAccount();
//			
//			//残高表示フラグ
//			boolean bRemainderDispFlg = true;
//			
//			//「My貯金残高」
//			if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
//				bRemainderDispFlg = false;
//				if(Boolean.valueOf(String.valueOf(objEach[iDaysCnt+3]))==false)
//					bRemainderDispFlg = true;
//			}
//				
//			
//			
//			// 月計をセット
////			long lSumMonthMyDp = objEach[0] == null ? 0L : Long.parseLong(String.valueOf(objEach[0]));
//			
//			
//			wDaIdealDepo.setsLargeCategory(sLargeCategoryName);
//			wDaIdealDepo.setsItem(String.valueOf(objEach[iDaysCnt+1]));
//			if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
//				wDaIdealDepo.setbBudgetFlg(true);
//			} else {
//				wDaIdealDepo.setbBudgetFlg(false);
//			}
//
//			//項目の最終行フラグ
//			intCnt++;
//			if(intIdealDepoCnt==intCnt) {
//				wDaIdealDepo.setBolLastItemFlg(true);
//			}
//			
//			// 日毎
//			long[] lAryDaysMyDp = new long[iDaysCnt];
//			String[] sAryDaysMyDp = new String[iDaysCnt];
//			calendar.setTime(dStartDay);
//			List<WkDaToDl> lstWdtd = new ArrayList<WkDaToDl>();
//			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
//
//				lAryDaysMyDp[iDay] = objEach[iDay+1] == null ? 0L : Long.parseLong(String.valueOf(objEach[iDay+1]));
//				if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT) &&
//						lAryDaysMyDp[iDay]!=0L) {
//					bRemainderDispFlg = true;
//				}
//				sAryDaysMyDp[iDay] = df.format(lAryDaysMyDp[iDay]);
//				
//				WkDaToDl workDaToDl = new WkDaToDl();
//				long lAmount = objEach[iDay+1] == null ? 0L : Long.parseLong(String.valueOf(objEach[iDay+1]));
//				workDaToDl.setlAmount(lAmount);
//				workDaToDl.setsAmount(df.format(lAmount));
//				String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
//				workDaToDl.setsPaymentDateFr(sDate);
//				workDaToDl.setsPaymentDateTo(sDate);
//				workDaToDl.setlHandlingId(null);
////				// 「My貯金預入」
////				if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
////					workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
////					
////				// 「My貯金から支払」
////				} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
////					workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT)).first()).id);
////					workDaToDl.setlIdealDepositId(Long.parseLong(String.valueOf(objEach[iDaysCnt+2])));
////				}
//				lstWdtd.add(workDaToDl);
//
//				calendar.add(Calendar.DATE, 1);
//			}
//			//残高有りか、0でも表示設定の場合、列を作成する
//			if(bRemainderDispFlg) {
//				wDaIdealDepo.setLAryDays(lAryDaysMyDp);
//	
//				wDaIdealDepo.setLstWdtd(lstWdtd);
//				
//				lWDA.add(wDaIdealDepo);
//			}
//			
//		}
		
		
		
		
		
	}
	
	
	
	
	
	/**
	 * 日計表の行に相当するリストの作成（「収入」・「支出」・「My貯金預入」・「My貯金から支払」毎に作成する）
	 * @param year
	 * @param month
	 * @param dStartDay
	 * @param iDaysCnt
	 * @param sSqlBase
	 * @param sSqlBaseG
	 * @param haUser
	 * @param wDaDiff
	 * @param sLargeCategoryName
	 * @param lWDA
	 */
	private static void makeWorkListEach(
			Integer year,
			Integer month,
			Date dStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst,
			String sLargeCategoryName,	// 大分類行の名称「収入」・「支出」・「My貯金預入」・「My貯金から支払」
			List<WkDailyAccount> lWDA
			) {
		//カンマ区切りの数値文字列を数値型に変換するNumberFormatクラスのインスタンスを取得する
		NumberFormat nf = NumberFormat.getInstance();

		//数値をカンマ区切りの数値文字列に変換するフォーマットを定義する
		DecimalFormat df = new DecimalFormat("###,###");
		
   		Calendar calendar = Calendar.getInstance();
   		String sSql = "";
		
   		/**
   		 * 合計行
   		 */
   		WkDailyAccount wDA = new WkDailyAccount();

   		String sSqlBase = "";
   		String sSqlBaseG = "";
   		
   		String sSqlBaseEach = "";
   		
   		String strSqlDaily = "";
   		
		calendar.setTime(dStartDay);
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			strSqlDaily += "" +
					",COALESCE(SUM(" +
					"  CASE " +
					"    WHEN cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD') THEN r.amount " +
					"    ELSE 0 " +
					"  END" +
					"  ), 0) as sum_day_" + iDay + "";
			calendar.add(Calendar.DATE, 1);
		}
		
		sSqlBase = "" +
//   				" SELECT COALESCE(SUM(r.amount), 0)" +
   				" SELECT COALESCE(SUM(r.amount), 0) as sum_month" + strSqlDaily +
   				" FROM Record r " +
				" LEFT JOIN ItemMst i " +
				"   ON r.item_mst_id = i.id " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" LEFT JOIN IdealDepositMst id " +
				"   ON r.ideal_deposit_mst_id = id.id " +
				" WHERE r.ha_user_id = " + haUser.id +
				"";
   		sSqlBaseG = "" +
				"   AND cast(r.payment_date as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD')" +
				"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";

   		
   		//項目毎の行取得用（「収入」・「支出」）
   		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_OUT))
   			sSqlBaseEach = "" +
	   				" SELECT COALESCE(SUM(r.amount), 0) as sum_month" + strSqlDaily +
	   				"   ,i.item_name " +
	   				"   ,i.id as item_id " +
	   				"   ,bg.id as bg_id " +
	   				"   ,bg.amount as bg_amount " +
	   				" FROM Record r " +
					" LEFT JOIN ItemMst i " +
					"   ON r.item_mst_id = i.id " +
					" LEFT JOIN BalanceTypeMst b " +
					"   ON r.balance_type_mst_id = b.id " +
					" LEFT JOIN IdealDepositMst id " +
					"   ON r.ideal_deposit_mst_id = id.id " +
					" LEFT JOIN Budget bg " +
					"   ON i.id = bg.item_mst_id" +
					"   AND bg.ha_user_id = " + haUser.id +
					"   AND bg.year = " + year +
					"   AND bg.month = " + month +
					" WHERE r.ha_user_id = " + haUser.id +
					"";
   		//項目毎の行取得用（「My貯金預入」・「My貯金から支払」）
   		if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT))
   			sSqlBaseEach = "" +
	   				" SELECT COALESCE(SUM(r.amount), 0) as sum_month" + strSqlDaily +
	   				"   ,id.ideal_deposit_name " +
	   				"   ,id.id as ideal_deposit_id " +
	   				"   ,bg.id as bg_id " +
	   				"   ,bg.amount as bg_amount " +
	   				" FROM Record r " +
					" LEFT JOIN ItemMst i " +
					"   ON r.item_mst_id = i.id " +
					" LEFT JOIN BalanceTypeMst b " +
					"   ON r.balance_type_mst_id = b.id " +
					" LEFT JOIN IdealDepositMst id " +
					"   ON r.ideal_deposit_mst_id = id.id " +
					" LEFT JOIN Budget bg " +
					"   ON id.id = bg.ideal_deposit_mst_id" +
					"   AND bg.ha_user_id = " + haUser.id +
					"   AND bg.year = " + year +
					"   AND bg.month = " + month +
					" WHERE r.ha_user_id = " + haUser.id +
					"";
   		
   		
   		
   		
   		
   		
   		//「収入」・「支出」
   		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) || sLargeCategoryName.equals(BALANCE_TYPE_OUT)) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
					"   AND r.ideal_deposit_mst_id IS NULL ";
   		//「My貯金預入」
   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		//「My貯金から支払」
   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		}
   		
		//月計をセット
   		Object[] obj = null;
		obj = (Object[])JPA.em().createNativeQuery(
				sSql + ((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "")).getSingleResult();
		long lSumMonthG = obj[0] == null ? 0L : Long.parseLong(String.valueOf(obj[0]));
		wDA.setLSumMonth(lSumMonthG);
		
		wDA.setsLargeCategory(sLargeCategoryName);
		wDA.setsItem("");
		wDA.setbBudgetFlg(false);
		
		//  「収入」・「支出」・「My貯金預入」の場合、予算有無フラグを立てる
		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
				sLargeCategoryName.equals(BALANCE_TYPE_OUT) ||
				sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
			wDA.setbBudgetFlg(true);
		}
		
		
		// 日毎
		long[] lAryDaysG = new long[iDaysCnt];
		String[] sAryDaysG = new String[iDaysCnt];
		calendar.setTime(dStartDay);
		List<WkDaToDl> lstWdtdG = new ArrayList<WkDaToDl>();
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			
			long lAmount = Long.parseLong(String.valueOf(obj[iDay+1]));
			
			sAryDaysG[iDay] = df.format(lAryDaysG[iDay]);
			
			WkDaToDl workDaToDl = new WkDaToDl();
			workDaToDl.setlAmount(lAmount);
			workDaToDl.setsAmount(df.format(lAmount));
			String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
			workDaToDl.setsPaymentDateFr(sDate);
			workDaToDl.setsPaymentDateTo(sDate);
			workDaToDl.setlHandlingId(null);
			// 「収入」・「支出」・「My貯金預入」
			if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
					sLargeCategoryName.equals(BALANCE_TYPE_OUT) ||
					sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
				workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
				
				//「収入」・「支出」の時は、My貯金＝NULL
				if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
						sLargeCategoryName.equals(BALANCE_TYPE_OUT)) {
					workDaToDl.setlIdealDepositId((long) -1);
				}
				
			// 「My貯金から支払」
			} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
				workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT)).first()).id);
				
				//My貯金＝NULLでない
				workDaToDl.setlIdealDepositId((long) -2);
			}
			lstWdtdG.add(workDaToDl);

			calendar.add(Calendar.DATE, 1);
		}
		wDA.setLAryDays(lAryDaysG);
		
		wDA.setLstWdtd(lstWdtdG);
		
		lWDA.add(wDA);

		
		//「収入」・「支出」
		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) || sLargeCategoryName.equals(BALANCE_TYPE_OUT)) {
			//項目ごとのループ
//			List<ItemMst> itemMsts = ItemMst.find("ha_user = " + haUser.id + " and balance_type_mst.balance_type_name = '" + sLargeCategoryName + "' order by id").fetch();
//			int intItemCnt = itemMsts.size();
			int intCnt = 0;
//			for(Iterator<ItemMst> itrItem = itemMsts.iterator(); itrItem.hasNext();) {
//				ItemMst itemMst = itrItem.next();
				
	
				
//				BigInteger biSumMonth = (BigInteger)JPA.em().createNativeQuery(
//						sSqlBase + sSqlBaseG +
//						"   AND i.item_name = '" + itemMst.item_name + "' " +
//						"   AND r.ideal_deposit_mst_id IS NULL "
//						 + ((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "")).getSingleResult();;
//				long lSumMonth = biSumMonth == null ? 0L : biSumMonth.longValue();
				String strSqlEach = sSqlBaseEach + sSqlBaseG +
//						"   AND i.item_name = '" + itemMst.item_name + "' " +
						"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL "
						 + ((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
						" GROUP BY i.id, i.item_name, bg.id, bg.amount " +
						" ORDER BY i.id " +
						"";
				List<Object[]> lstObjEach = JPA.em().createNativeQuery(
						strSqlEach
						).getResultList();
				
				int intItemCnt = lstObjEach.size();
				for(Object[] objEach : lstObjEach) {
					
					
					
					
					WkDailyAccount wDaItem = new WkDailyAccount();
					
					
					
					long lSumMonth = objEach[0] == null ? 0L : Long.parseLong(String.valueOf(objEach[0]));
					
					wDaItem.setsLargeCategory(sLargeCategoryName);
//					wDaItem.setsItem(itemMst.item_name);
					wDaItem.setsItem(String.valueOf(objEach[iDaysCnt+1]));
					wDaItem.setbBudgetFlg(true);
					wDaItem.setLSumMonth(lSumMonth);
					
					//項目の最終行フラグ
					intCnt++;
					if(intItemCnt==intCnt) {
						wDaItem.setBolLastItemFlg(true);
					}

					
					
					
					
					
					
					//ある時だけ予算をセット
//					Budget budget = Budget.find("ha_user = " + haUser.id + "" +
//							" and year = " + year + "" +
//							" and month = " + month + "" +
//							" and item_mst = " + Long.parseLong(String.valueOf(objEach[iDaysCnt+2]))
//							).first();
					
					if(objEach[iDaysCnt+3]!=null) {
						Long lngBgId = Long.parseLong(String.valueOf(objEach[iDaysCnt+3]));
//						Integer intBgAmount = Integer.parseInt(String.valueOf(objEach[iDaysCnt+4]));
						Long lngBgAmount = Long.parseLong(String.valueOf(objEach[iDaysCnt+4]));
						
						
						String sBudgetAmount = String.format("%1$,3d", lngBgAmount).trim();
						
						wDaItem.setlBudgetId(lngBgId);
						wDaItem.setlBudgetAmount(lngBgAmount);
						wDaItem.setsBudgetAmount(sBudgetAmount);
						
						//大分類行に加算
						if(!(wDA.getsBudgetAmount()==null || wDA.getsBudgetAmount().equals(""))) {
							Number nEBudgetAmount;
							try {
								nEBudgetAmount = nf.parse(wDA.getsBudgetAmount());
								sBudgetAmount = String.format("%1$,3d", nEBudgetAmount.intValue() + lngBgAmount);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						if(!(wDA.getlBudgetAmount()==null || wDA.getlBudgetAmount().equals("")))
							lngBgAmount += wDA.getlBudgetAmount();
						wDA.setlBudgetAmount(lngBgAmount);
						wDA.setsBudgetAmount(sBudgetAmount);
					}

					
					
					
					
					
					
					
					// 日毎
	//				long[] lAryDays = new long[iDaysCnt];
	//				String[] sAryDays = new String[iDaysCnt];
					calendar.setTime(dStartDay);
					List<WkDaToDl> lstWdtd = new ArrayList<WkDaToDl>();
					for(int iDay = 0; iDay < iDaysCnt; iDay++) {
	//			   		String sSqlBaseD = "" +
	//							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	//					BigInteger biAryDays = (BigInteger)JPA.em().createNativeQuery(
	//							sSqlBase + sSqlBaseD +
	//							"   AND i.item_name = '" + itemMst.item_name + "' " +
	//							"   AND r.ideal_deposit_mst_id IS NULL "
	//							 + ((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "")).getSingleResult();;
	//					lAryDays[iDay] = objEach[iDay] == null ? 0L : Long.parseLong(String.valueOf(objEach[iDay]));
	//					sAryDays[iDay] = df.format(lAryDays[iDay]);
	
						WkDaToDl workDaToDl = new WkDaToDl();
						long lAmount = objEach[iDay+1] == null ? 0L : Long.parseLong(String.valueOf(objEach[iDay+1]));
						workDaToDl.setlAmount(lAmount);
						workDaToDl.setsAmount(df.format(lAmount));
						String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
						workDaToDl.setsPaymentDateFr(sDate);
						workDaToDl.setsPaymentDateTo(sDate);
						workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
						workDaToDl.setlHandlingId(null);
						//取扱(My貯金)＝NULL
						workDaToDl.setlIdealDepositId((long) -1);
//						workDaToDl.setiItemId(((ItemMst)(ItemMst.find("byItem_name", itemMst.item_name)).first()).id);
						workDaToDl.setiItemId(Long.parseLong(String.valueOf(objEach[iDaysCnt+2])));
						lstWdtd.add(workDaToDl);
						
						calendar.add(Calendar.DATE, 1);
					}
	//				wDaItem.setLAryDays(lAryDays);
	//				wDaItem.setsAryDays(sAryDays);
		
					wDaItem.setLstWdtd(lstWdtd);
			
					lWDA.add(wDaItem);
					
					
					
					
					
					
				}
				
				
				
				
//			}
		//「My貯金預入」・「My貯金から支払」
		} else if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN) ||
				sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
			//My貯金ごとのループ
//			List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = " + haUser.id).fetch();
//			int intIdealDepoCnt = idealDepositMsts.size();
			int intCnt = 0;
//			for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
//				IdealDepositMst idealDepositMst = itrIdealDeposit.next();
				
			
				String strSqlEach = "";
				//「My貯金預入」
				if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
					strSqlEach = sSqlBaseEach + sSqlBaseG +
							"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
//							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'" +
							((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
							" GROUP BY id.id, id.ideal_deposit_name, bg.id, bg.amount " +
							" ORDER BY id.id" +
							"";
					
		   		//「My貯金から支払」
		   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
					strSqlEach = sSqlBaseEach + sSqlBaseG +
							"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
//							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'" +
							((session.get("actionMode")).equals("View") ? " AND r.secret_rec_flg = FALSE " : "") +
							" GROUP BY id.id, id.ideal_deposit_name, bg.id, bg.amount" +
							" ORDER BY id.id" +
							"";
		   			
		   		}
				
				List<Object[]> lstObjEach = null;
	   			lstObjEach = JPA.em().createNativeQuery(
						strSqlEach).getResultList();

	   			int intIdealDepoCnt = lstObjEach.size();
	   			for(Object[] objEach : lstObjEach) {
	   				
					WkDailyAccount wDaIdealDepo = new WkDailyAccount();
					
					//  月計をセット
					long lSumMonthMyDp = objEach[0] == null ? 0L : Long.parseLong(String.valueOf(objEach[0]));
					
					wDaIdealDepo.setsLargeCategory(sLargeCategoryName);
					wDaIdealDepo.setsItem(String.valueOf(objEach[iDaysCnt+1]));
					if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
						wDaIdealDepo.setbBudgetFlg(true);
					} else {
						wDaIdealDepo.setbBudgetFlg(false);
					}
	
					//項目の最終行フラグ
					intCnt++;
					if(intIdealDepoCnt==intCnt) {
						wDaIdealDepo.setBolLastItemFlg(true);
					}
					
					//「My貯金預入」なら予算がある時だけセット
					if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
						if(objEach[iDaysCnt+3]!=null) {
							Long lngBgId = Long.parseLong(String.valueOf(objEach[iDaysCnt+3]));
//							Integer intBgAmount = Integer.parseInt(String.valueOf(objEach[iDaysCnt+4]));
							Long lngBgAmount = Long.parseLong(String.valueOf(objEach[iDaysCnt+4]));
							
							String sBudgetAmount = String.format("%1$,3d", lngBgAmount).trim();
	
							wDaIdealDepo.setlBudgetId(lngBgId);
							wDaIdealDepo.setlBudgetAmount(lngBgAmount);
							wDaIdealDepo.setsBudgetAmount(sBudgetAmount);
							
							//大分類行に加算
							if(!(wDA.getsBudgetAmount()==null || wDA.getsBudgetAmount().equals(""))) {
								Number nEBudgetAmount;
								try {
									nEBudgetAmount = nf.parse(wDA.getsBudgetAmount());
									sBudgetAmount = String.format("%1$,3d", nEBudgetAmount.intValue() + lngBgAmount);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							if(!(wDA.getlBudgetAmount()==null || wDA.getlBudgetAmount().equals("")))
								lngBgAmount += wDA.getlBudgetAmount();
							wDA.setlBudgetAmount(lngBgAmount);
							wDA.setsBudgetAmount(sBudgetAmount);
						}
					}
					
					
					// 日毎
					long[] lAryDaysMyDp = new long[iDaysCnt];
					String[] sAryDaysMyDp = new String[iDaysCnt];
					calendar.setTime(dStartDay);
					List<WkDaToDl> lstWdtd = new ArrayList<WkDaToDl>();
					for(int iDay = 0; iDay < iDaysCnt; iDay++) {
	
						lAryDaysMyDp[iDay] = objEach[iDay+1] == null ? 0L : Long.parseLong(String.valueOf(objEach[iDay+1]));
						sAryDaysMyDp[iDay] = df.format(lAryDaysMyDp[iDay]);
						
						WkDaToDl workDaToDl = new WkDaToDl();
						long lAmount = objEach[iDay+1] == null ? 0L : Long.parseLong(String.valueOf(objEach[iDay+1]));
						workDaToDl.setlAmount(lAmount);
						workDaToDl.setsAmount(df.format(lAmount));
						String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
						workDaToDl.setsPaymentDateFr(sDate);
						workDaToDl.setsPaymentDateTo(sDate);
						workDaToDl.setlHandlingId(null);
						// 「My貯金預入」
						if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
							workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
							
						// 「My貯金から支払」
						} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
							workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT)).first()).id);
							workDaToDl.setlIdealDepositId(Long.parseLong(String.valueOf(objEach[iDaysCnt+2])));
						}
						lstWdtd.add(workDaToDl);
	
						calendar.add(Calendar.DATE, 1);
					}
					//列を作成
					wDaIdealDepo.setLAryDays(lAryDaysMyDp);
		
					wDaIdealDepo.setLstWdtd(lstWdtd);
					
					lWDA.add(wDaIdealDepo);
	   				
	   			}
	   			
	   			
	   			
				
//			}
		}
	}
	
	
	/**
	 * 単純に呼ばれた時の基準日のセット
	 * @param calendar
	 * @return
	 */
	private static String setBasisDate() {
		//セッションに絞込条件が入っている時はセット
		if((session.get("daFilExistFlg") != null) &&
				(session.get("daFilExistFlg").equals("true")))
			return session.get("daStrBasisDate");
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
    		List<String> e_large_category,	/* 変更行の大分類行の名称「収入」・「支出」・「My貯金預入」 */
    		List<String> e_item,			/* 変更行の項目 */
    		List<String> e_budget_amount	/* 変更行の金額 */
			) {
		
		Iterator<String> sELargeCategory = e_large_category.iterator();
		Iterator<String> sEItem = e_item.iterator();
   		Iterator<String> sEBudgetAmount = e_budget_amount.iterator();
		for (Long lngId : e_budget_id) {
			String sEBudgetAmountVal = sEBudgetAmount.next(); 
			String sELargeCategoryVal = sELargeCategory.next();
			String sEItemVal = sEItem.next();

			//予算が空白にされた時
			if(sEBudgetAmountVal.equals("")) {
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
					sELargeCategoryVal,
					sEItemVal,
					sEBudgetAmountVal
					);
		}
		
    	dailyAccount(bg_basis_date);
	}
	
	/**
	 * 予算更新実処理
	 * @param bg_basis_date
	 * @param lngId
	 * @param sELargeCategoryVal
	 * @param sEItemVal
	 * @param sEBudgetAmountVal
	 */
	private static void actUpdBudget(
			String bg_basis_date,
			Long lngId,
			String sELargeCategoryVal,
			String sEItemVal,
			String sEBudgetAmountVal
			) {
		//カンマ区切りの数値文字列を数値型に変換するNumberFormatクラスのインスタンスを取得する
		NumberFormat nf = NumberFormat.getInstance();

		try {
			//数値文字列をNumber型のオブジェクトに変換する
			Number nEBudgetAmount = nf.parse(sEBudgetAmountVal);
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
						budget.ideal_deposit_mst
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
			IdealDepositMst idealDepositMst = null;
			//大分類が「My貯金預入」の場合は「取扱(My貯金)」ごとの登録
			if(sELargeCategoryVal.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
				idealDepositMst = IdealDepositMst.find("ha_user = " + haUser.id + " and ideal_deposit_name = '" + sEItemVal + "'").first();
			//大分類が「My貯金預入」でない場合は「項目」ごとの登録
			} else {
				itemMst = ItemMst.find("ha_user = " + haUser.id + " and item_name = '" + sEItemVal + "'").first();
			}
			Budget budget = new Budget(
					haUser,
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH) + 1,
					iEBudgetAmount,
					itemMst,
					idealDepositMst
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

	/**
	 * dailyAccount.html描画時の引数の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	static class RefDailyAccountRender {
		WkDailyAccountRender wkDAR;
	}
}
