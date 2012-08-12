package controllers;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.HaUser;
import models.HandlingMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.WorkDailyAccount;

import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class DailyAccount extends Controller {

//	@Before
//	static void setConnectedUser() {
//		if(Security.isConnected()) {
//			HaUser hauser  = HaUser.find("byEmail", Security.connected()).first();
//			renderArgs.put("userId", hauser.id);
//		}
//	}
	
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
	 * 日計表の表示
	 * @param sBasisDate
	 */
	public static void dailyAccount(
			String sBasisDate
			) {
		String sTableType = VIEWS_DAILY_ACCOUNT;
		displayTable(sBasisDate, sTableType);
	}
	
	/**
	 * 残高表の表示
	 * @param sBasisDate
	 */
	public static void balanceTable(
			String sBasisDate
			) {
		String sTableType = VIEWS_BALANCE_TABLE;
		displayTable(sBasisDate, sTableType);
	}
	
	/**
	 * 日計表・残高表の表示
	 * @param sBasisDate
	 */
	public static void displayTable(
//			Integer year,
//			Integer month,
			String sBasisDate,
			String sTableType
			) {

   		Calendar calendar = Calendar.getInstance();
   		
   		//単純に呼ばれた時（初回等）は、今日を表示
//		if((year==null)||(month==null)||(basisDate==null)) {
		if(sBasisDate==null) {
//			year = calendar.get(Calendar.YEAR);
//			month = calendar.get(Calendar.MONTH) + 1;
			sBasisDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
		}
		
		Date dBasis = null;
		try {
			dBasis = DateFormat.getDateInstance().parse(sBasisDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calendar.setTime(dBasis);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;

//		//今月判定フラグ
//		Integer thisMonthFlg = 0;
//		if((year==calendar.get(Calendar.YEAR)) &&
//    	   (month==calendar.get(Calendar.MONTH)+1)) {
//			thisMonthFlg = 1;
//		}
		
//		calendar.set(year, month - 1, 1);
		int iDaysCnt = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		
		iDaysCnt = 3;
		
		
		
   		
		//日計表のヘッダーの日付の配列
		String[] sAryDays = new String[iDaysCnt];
   		calendar.add(Calendar.DATE, -1);
		int iStartDay = calendar.get(Calendar.DATE);
		SimpleDateFormat sdf1 = new SimpleDateFormat("M/d");
		
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
//			sAryDays[iDay] = String.format("%1$tm/%1$td", calendar.getTime());
			sAryDays[iDay] = sdf1.format(calendar.getTime());
			calendar.add(Calendar.DATE, 1);
		}
		

		//日計表の行に相当するリストの作成
   		List<WorkDailyAccount> lWDA = makeWorkList(year, month, iStartDay, iDaysCnt, sTableType);
   		
   		//日計表の日付ごとの部分のスクロール内の幅の設定
		int iWidth = iDaysCnt * 100;
   		
//		if(sTableType.equals(VIEWS_DAILY_ACCOUNT)) {
////			render(year, month, sBasisDate, thisMonthFlg, iAryDays, lWDA, iWidth);
//			render(month, sBasisDate, sTableType, iAryDays, lWDA, iWidth);
//		} else if(sTableType.equals(VIEWS_BALANCE_TABLE)) {
//			render("@dailyAccount",  month, sBasisDate, sTableType, iAryDays, lWDA, iWidth);
//		}

		render("@dailyAccount",  month, sBasisDate, sTableType, sAryDays, lWDA, iWidth);
		
	}

	
	/**
	 * 基準日変更による移動
	 * @param e_basis_date
	 * @param sTableType
	 * @param move
	 */
	public static void jump(
    		String e_basis_date,
    		String sTableType,
    		String move			/* 「移動」ボタン */
			) {

		Calendar calendar = Calendar.getInstance();
    	// 「移動」ボタンが押された場合
		if(move != null) {
			Date dBasis = null;
			try {
				dBasis = DateFormat.getDateInstance().parse(e_basis_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			calendar.setTime(dBasis);
    		if(sTableType.equals(VIEWS_DAILY_ACCOUNT)) {
//    			dailyAccount(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, e_basis_date);
    			dailyAccount(e_basis_date);
    		} else if(sTableType.equals(VIEWS_BALANCE_TABLE)) {
    			balanceTable(e_basis_date);
    		}
    	}
	}

	/**
	 * 日計表・残高表の行に相当するリストの作成
	 * @param year
	 * @param month
	 * @param iStartDay
	 * @param iDaysCnt
	 * @param sTableType
	 * @return
	 */
	private static List<WorkDailyAccount> makeWorkList(
			Integer year,
			Integer month,
			int iStartDay,
			int iDaysCnt,
			String sTableType
			) {
		//日計表・残高表の行に相当するリスト
   		List<WorkDailyAccount> lWDA = new ArrayList<WorkDailyAccount>();
   		
   		Calendar calendar = Calendar.getInstance();
   		
		calendar.set(year, month - 1, 1);
		String sFirstDay =  String.format("%1$tY%1$tm%1$td", calendar.getTime());
   		calendar.add(Calendar.MONTH, 1);
   		String sNextFirst = String.format("%1$tY%1$tm%1$td", calendar.getTime());
   		
		HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();

		
		if(sTableType.equals(VIEWS_DAILY_ACCOUNT)) {
			//「収入」
//			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					Messages.get("BalanceType.in"), lWDA);
			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_IN, lWDA);
			
			//「支出」
//			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					Messages.get("BalanceType.out"), lWDA);
			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_OUT, lWDA);
			
			//「My貯金預入」
//			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					Messages.get("BalanceType.ideal_deposit_in"), lWDA);
			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_IDEAL_DEPOSIT_IN, lWDA);
			
			
//			//「差額」　　（　「収入」－「支出」－「My貯金預入」　）
//          		WorkDailyAccount wDaDiff = new WorkDailyAccount();
////			BigInteger biSumMonthDiff = new BigInteger("0");
////			BigInteger[] biAryDaysDiff = new BigInteger[iDaysCnt];
//			long lSumMonthDiff = 0L;
//			long[] lAryDaysDiff = new long[iDaysCnt];
//			
//			String[] sAryDaysDiff = new String[iDaysCnt];
//			for (WorkDailyAccount wda : lWDA) {
//				//「収入」・「支出」・「My貯金預入」の合計行で算出する
//				if((wda.getsItem().equals("")) &&
//				   (wda.getsLargeCategory().equals(Messages.get("BalanceType.in")) ||
//					wda.getsLargeCategory().equals(Messages.get("BalanceType.out")) ||
//					wda.getsLargeCategory().equals(Messages.get("BalanceType.ideal_deposit_in")))
//						) {
//					
//					//「収入」は「差額」に加算
//					if(wda.getsLargeCategory().equals(Messages.get("BalanceType.in"))) {
////						if(wda.getBiSumMonth() != null) {
////							lSumMonthDiff = lSumMonthDiff.add(wda.getBiSumMonth());
////						}
//						lSumMonthDiff += wda.getLSumMonth();
//					//「支出」・「My貯金預入」は「差額」から減算
//					} else if(wda.getsLargeCategory().equals(Messages.get("BalanceType.out")) ||
//							wda.getsLargeCategory().equals(Messages.get("BalanceType.ideal_deposit_in"))) {
////						if(wda.getBiSumMonth() != null) {
////							lSumMonthDiff = lSumMonthDiff.subtract(wda.getBiSumMonth());
////						}
//						lSumMonthDiff += wda.getLSumMonth();
//					}
//  	  	
//					// 日毎
////					BigInteger[] biAryDaysTmp = wda.getBiAryDays();
//					long[] lAryDaysTmp = wda.getLAryDays();
//					for(int iDay = 0; iDay < iDaysCnt; iDay++) {
////						if(lAryDaysDiff[iDay] == null) {
////							lAryDaysDiff[iDay] = new BigInteger("0"); 
////						}
//						//「収入」は「差額」に加算
//						if(wda.getsLargeCategory().equals(Messages.get("BalanceType.in"))) {
////			   				if(lAryDaysTmp[iDay] != null) {
////			   					lAryDaysDiff[iDay] = lAryDaysDiff[iDay].add(lAryDaysTmp[iDay]);
////			   				}
//	        					lAryDaysDiff[iDay] += lAryDaysTmp[iDay];
//			   			//「支出」・「My貯金預入」は「差額」から減算
//						} else if(wda.getsLargeCategory().equals(Messages.get("BalanceType.out")) ||
//								wda.getsLargeCategory().equals(Messages.get("BalanceType.ideal_deposit_in"))) {
////			   				if(lAryDaysTmp[iDay] != null) {
////			   					lAryDaysDiff[iDay] = lAryDaysDiff[iDay].subtract(lAryDaysTmp[iDay]); 
////			   				}
//	        					lAryDaysDiff[iDay] += lAryDaysTmp[iDay]; 
//						}
//					}
//					
//				}
//			}
//			wDaDiff.setsLargeCategory(Messages.get("views.detaillist.diff"));
//			wDaDiff.setsItem("");
////			wDaDiff.setBiSumMonth(lSumMonthDiff);
//			wDaDiff.setLSumMonth(lSumMonthDiff);
////			wDaDiff.setsSumMonth(lSumMonthDiff==null ? "" : String.format("%1$,3d", lSumMonthDiff));
//			wDaDiff.setsSumMonth(String.format("%1$,3d", lSumMonthDiff));
////			wDaDiff.setBiAryDays(lAryDaysDiff);
//			wDaDiff.setLAryDays(lAryDaysDiff);
//			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
////				sAryDaysDiff[iDay] = lAryDaysDiff[iDay]==null ? "" : String.format("%1$,3d", lAryDaysDiff[iDay]);
//				sAryDaysDiff[iDay] = String.format("%1$,3d", lAryDaysDiff[iDay]);
//			}
//			wDaDiff.setsAryDays(sAryDaysDiff);
//			
//			lWDA.add(wDaDiff);
			
			
			//「My貯金から支払」
//			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					Messages.get("BalanceType.out_ideal_deposit"), lWDA);
			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					BALANCE_TYPE_OUT_IDEAL_DEPOSIT, lWDA);
			
		} else if(sTableType.equals(VIEWS_BALANCE_TABLE)) {
			//「実残高」
//			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					Messages.get("RemainderType.real"), lWDA);
			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					REMAINDER_TYPE_REAL, lWDA);
			
			//「My貯金残高」
//			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					Messages.get("RemainderType.ideal_deposit"), lWDA);
			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					REMAINDER_TYPE_IDEAL_DEPOSIT, lWDA);
  	  	
  	  	
			//「My貯金してないお金」
//			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
//					Messages.get("RemainderType.not_ideal_deposit"), lWDA);
			makeWorkListEach(year, month, iStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					REMAINDER_TYPE_NOT_IDEAL_DEPOSIT, lWDA);
			
		}

		
		return lWDA;
	}
	
	/**
	 * 日計表の行に相当するリストの作成（「収入」・「支出」・「My貯金預入」・「差額」・「My貯金から支払」毎に作成する）
	 * @param year
	 * @param month
	 * @param iStartDay
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
			int iStartDay,
			int iDaysCnt,
			HaUser haUser,
			String sFirstDay,
			String sNextFirst,
			String sLargeCategoryName,	// 大分類行の名称「収入」・「支出」・「My貯金預入」・「My貯金から支払」
			List<WorkDailyAccount> lWDA
			) {
   		Calendar calendar = Calendar.getInstance();
   		String sSql = "";
		
   		//合計行
   		WorkDailyAccount wDA = new WorkDailyAccount();

   		String sSqlBase = "";
   		String sSqlBaseG = "";
   		
   		//「収入」・「支出」・「My貯金預入」・「My貯金から支払」
//   		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) ||
//   				sLargeCategoryName.equals(Messages.get("BalanceType.out")) ||
//   				sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in")) ||
//   				sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
   		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_OUT) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
   			sSqlBase = "" +
   					"" +
	   				" SELECT SUM(r.amount) FROM Record r " +
					" LEFT JOIN ItemMst i " +
					"   ON r.item_mst_id = i.id " +
					" LEFT JOIN BalanceTypeMst b " +
					"   ON r.balance_type_mst_id = b.id " +
					" LEFT JOIN IdealDepositMst id " +
					"   ON r.ideal_deposit_mst_id = id.id " +
					" WHERE r.ha_user_id = " + haUser.id;
	   		sSqlBaseG = "" +
					"   AND cast(r.payment_date as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD')" +
					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
	   		
	   	//「実残高」
//   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				//「収入」は加算
//	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.in") + "' THEN r.amount" +
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IN + "' THEN r.amount" +
	   				//「支出」は減算
//	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.out") + "' THEN -r.amount" +
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_OUT + "' THEN -r.amount" +
	   				"   END" +
	   				"   ), 0) " +
	   				" FROM Record r " +
					" LEFT JOIN ItemMst i " +
					"   ON r.item_mst_id = i.id " +
					" LEFT JOIN BalanceTypeMst b " +
					"   ON r.balance_type_mst_id = b.id " +
					" LEFT JOIN HandlingMst h " +
					"   ON r.handling_mst_id = h.id " +
					" LEFT JOIN HandlingTypeMst ht " +
					"   ON h.handling_type_mst_id = ht.id" +
					" LEFT JOIN HandlingMst hb " +
					"   ON h.debit_bank_id = hb.id" +
					" WHERE r.ha_user_id = " + haUser.id;
	   		sSqlBaseG = "" +
//					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
	   		
	   	//「My貯金残高」
//   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
//	   				"     WHEN (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN r.amount" +
//	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' THEN r.amount" +
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN r.amount" +
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN -r.amount" +
	   				"   END" +
	   				"   ), 0) " +
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
					" WHERE r.ha_user_id = " + haUser.id;
	   		sSqlBaseG = "" +
//					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
	   		
	   	//「My貯金してないお金」
//   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.not_ideal_deposit"))) {
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				//「収入」は加算
//	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.in") + "' THEN r.amount " +
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IN + "' THEN r.amount " +
	   				//「支出」で「取扱(My貯金)」未選択は減算
//	   				"     WHEN (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NULL) THEN -r.amount " +
	   				//「My貯金預入」は減算
//	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' THEN -r.amount" +
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN -r.amount" +
	   				"   END" +
	   				"   ), 0) " +
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
					" WHERE r.ha_user_id = " + haUser.id;
	   		sSqlBaseG = "" +
//					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
   		}
   		

   		//「収入」・「支出」
//   		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) || sLargeCategoryName.equals(Messages.get("BalanceType.out"))) {
   		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) || sLargeCategoryName.equals(BALANCE_TYPE_OUT)) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
					"   AND r.ideal_deposit_mst_id IS NULL ";
   		//「My貯金預入」
//   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
   			sSql = sSqlBase + sSqlBaseG +
//					"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
					"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		//「My貯金から支払」
//   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
   			sSql = sSqlBase + sSqlBaseG +
//					"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
					"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		//「実残高」
//   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
   			sSql = sSqlBase + sSqlBaseG +
//					"   AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "') ";
					"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') ";
   		//「My貯金残高」
//   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
   			sSql = sSqlBase + sSqlBaseG +
//					"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
					"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
	   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
//	   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
	   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
	   				"        )";
   		//「My貯金してないお金」
//   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.not_ideal_deposit"))) {
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
   			sSql = sSqlBase + sSqlBaseG +
//					"   AND (b.balance_type_name = '" + Messages.get("BalanceType.in") + "' OR " +
//	   				"        (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
					"   AND (b.balance_type_name = '" + BALANCE_TYPE_IN + "' OR " +
	   				"        (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
					"         r.ideal_deposit_mst_id IS NULL) OR " +
//					"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
   				    "        b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
	   				"        )";
   		}
		BigInteger biSumMonthG = (BigInteger)JPA.em().createNativeQuery(
				sSql).getSingleResult();
		long lSumMonthG = biSumMonthG == null ? 0L : biSumMonthG.longValue();

		wDA.setsLargeCategory(sLargeCategoryName);
		wDA.setsItem("");
		wDA.setbBudgetFlg(false);
//		wDA.setBiSumMonth(biSumMonthG);
		wDA.setLSumMonth(lSumMonthG);
//		wDA.setsSumMonth(lSumMonthG==null ? "" : String.format("%1$,3d", lSumMonthG));
		wDA.setsSumMonth(String.format("%1$,3d", lSumMonthG));
		
		//  「収入」・「支出」・「My貯金預入」の場合、予算有無フラグを立てる
//		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) ||
//				sLargeCategoryName.equals(Messages.get("BalanceType.out")) ||
//				sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
				sLargeCategoryName.equals(BALANCE_TYPE_OUT) ||
				sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
			wDA.setbBudgetFlg(true);
		}
		
		
		// 日毎
//		BigInteger[] biAryDaysG = new BigInteger[iDaysCnt];
		long[] lAryDaysG = new long[iDaysCnt];
		String[] sAryDaysG = new String[iDaysCnt];
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			calendar.set(year, month - 1, iStartDay + iDay);
	   		String sSqlBaseD = "" +
					"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   		//「収入」・「支出」
//	   		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) || sLargeCategoryName.equals(Messages.get("BalanceType.out"))) {
	   		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) || sLargeCategoryName.equals(BALANCE_TYPE_OUT)) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL ";
	   		//「My貯金預入」
//	   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
	   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
	   			sSql = sSqlBase + sSqlBaseD +
//						"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
						"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		//「My貯金から支払」
//	   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
	   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
	   			sSql = sSqlBase + sSqlBaseD +
//						"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
						"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		//「実残高」
//	   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
	   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
				//初日のみデータ集約。以降は加算
				if(iDay == 0) {
					sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				} else {
					sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				}
	   			sSql = sSqlBase + sSqlBaseD +
//						"   AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "') ";
						"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') ";
	   		//「My貯金残高」
//	   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
	   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
	   			sSqlBaseD = "" +
//	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
//						"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
						"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
		   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
//		   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
		   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
		   				"        )";
	   		//「My貯金してないお金」
//	   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.not_ideal_deposit"))) {
	   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
	   			sSqlBaseD = "" +
//	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
//						"   AND (b.balance_type_name = '" + Messages.get("BalanceType.in") + "' OR " +
//		   				"        (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
						"   AND (b.balance_type_name = '" + BALANCE_TYPE_IN + "' OR " +
		   				"        (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
	   				    "         r.ideal_deposit_mst_id IS NULL) OR " +
//						"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
						"        b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
		   				"        )";
	   		}
//			biAryDaysG[iDay] = (BigInteger)JPA.em().createNativeQuery(
//					sSql).getSingleResult();
//			sAryDaysG[iDay] = lAryDaysG[iDay]==null ? "" : String.format("%1$,3d", lAryDaysG[iDay]);
	   		BigInteger biAryDaysG = (BigInteger)JPA.em().createNativeQuery(
					sSql).getSingleResult();
			lAryDaysG[iDay] = biAryDaysG == null ? 0L : biAryDaysG.longValue();
			//実残高は初日のみデータ集約。以降は加算
//			if(sLargeCategoryName.equals(Messages.get("RemainderType.real")) && iDay != 0) {
			if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL) && iDay != 0) {
				lAryDaysG[iDay] = lAryDaysG[iDay-1] + (biAryDaysG == null ? 0L : biAryDaysG.longValue());
			}
			sAryDaysG[iDay] = String.format("%1$,3d", lAryDaysG[iDay]);

		}
//		wDA.setBiAryDays(biAryDaysG);
		wDA.setLAryDays(lAryDaysG);
		wDA.setsAryDays(sAryDaysG);
		
		lWDA.add(wDA);

		
		//「収入」・「支出」
//		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) || sLargeCategoryName.equals(Messages.get("BalanceType.out"))) {
		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) || sLargeCategoryName.equals(BALANCE_TYPE_OUT)) {
			//項目ごとのループ
			List<ItemMst> itemMsts = ItemMst.find("ha_user = " + haUser.id + " and balance_type_mst.balance_type_name = '" + sLargeCategoryName + "' order by id").fetch();
			for(Iterator<ItemMst> itrItem = itemMsts.iterator(); itrItem.hasNext();) {
				ItemMst itemMst = itrItem.next();
				
				WorkDailyAccount wDaItem = new WorkDailyAccount();
	
				BigInteger biSumMonth = (BigInteger)JPA.em().createNativeQuery(
						sSqlBase + sSqlBaseG +
						"   AND i.item_name = '" + itemMst.item_name + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL "
						).getSingleResult();
				long lSumMonth = biSumMonth == null ? 0L : biSumMonth.longValue();
				
				wDaItem.setsLargeCategory(sLargeCategoryName);
				wDaItem.setsItem(itemMst.item_name);
				wDaItem.setbBudgetFlg(true);
//				wDaItem.setBiSumMonth(lSumMonth);
//				wDaItem.setsSumMonth(lSumMonth==null ? "" : String.format("%1$,3d", lSumMonth));
				wDaItem.setLSumMonth(lSumMonth);
				wDaItem.setsSumMonth(String.format("%1$,3d", lSumMonth));
	
				// 日毎
//				BigInteger[] biAryDays = new BigInteger[iDaysCnt];
				long[] lAryDays = new long[iDaysCnt];
				String[] sAryDays = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iStartDay + iDay);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
//					biAryDays[iDay] = (BigInteger)JPA.em().createNativeQuery(
//							sSqlBase + sSqlBaseD +
//							"   AND i.item_name = '" + itemMst.item_name + "' " +
//							"   AND r.ideal_deposit_mst_id IS NULL "
//							).getSingleResult();
//					sAryDays[iDay] = lAryDays[iDay]==null ? "" : String.format("%1$,3d", lAryDays[iDay]);
					BigInteger biAryDays = (BigInteger)JPA.em().createNativeQuery(
							sSqlBase + sSqlBaseD +
							"   AND i.item_name = '" + itemMst.item_name + "' " +
							"   AND r.ideal_deposit_mst_id IS NULL "
							).getSingleResult();
					lAryDays[iDay] = biAryDays == null ? 0L : biAryDays.longValue();
					sAryDays[iDay] = String.format("%1$,3d", lAryDays[iDay]);
				}
//				wDaItem.setBiAryDays(lAryDays);
				wDaItem.setLAryDays(lAryDays);
				wDaItem.setsAryDays(sAryDays);
	
				lWDA.add(wDaItem);
				
			}
		//「My貯金預入」・「My貯金から支払」・「My貯金残高」
//		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in")) ||
//				sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit")) ||
//				sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
		} else if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN) ||
				sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT) ||
				sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
			//My貯金ごとのループ
			List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = " + haUser.id).fetch();
			for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
				IdealDepositMst idealDepositMst = itrIdealDeposit.next();
				
				WorkDailyAccount wDaIdealDepo = new WorkDailyAccount();
				
				//「My貯金預入」
//				if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
				if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
		   			sSql = sSqlBase + sSqlBaseG +
//							"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
							"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   			
		   		//「My貯金から支払」
//		   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
		   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
		   			sSql = sSqlBase + sSqlBaseG +
//							"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
							"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   			
		   		//「My貯金残高」
//		   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
		   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
		   			sSql = sSqlBase + sSqlBaseG +
//							"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
							"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
			   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
//			   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
			   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
			   				"        )" +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   		}
	
				BigInteger biSumMonthMyDp = (BigInteger)JPA.em().createNativeQuery(
						sSql).getSingleResult();
				long lSumMonthMyDp = biSumMonthMyDp == null ? 0L : biSumMonthMyDp.longValue();
				
				wDaIdealDepo.setsLargeCategory(sLargeCategoryName);
				wDaIdealDepo.setsItem(idealDepositMst.ideal_deposit_name);
//				if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
				if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
					wDaIdealDepo.setbBudgetFlg(true);
				} else {
					wDaIdealDepo.setbBudgetFlg(false);
				}
//				wDaIdealDepo.setsSumMonth(lSumMonthMyDp==null ? "" : String.format("%1$,3d", lSumMonthMyDp));
				wDaIdealDepo.setsSumMonth(String.format("%1$,3d", lSumMonthMyDp));
	
				// 日毎
//				BigInteger[] biAryDaysMyDp = new BigInteger[iDaysCnt];
				long[] lAryDaysMyDp = new long[iDaysCnt];
				String[] sAryDaysMyDp = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iStartDay + iDay);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   		
			   		//「My貯金預入」
//					if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
					if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
			   			sSql = sSqlBase + sSqlBaseD +
//								"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
								"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   			
			   		//「My貯金から支払」
//			   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
			   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
			   			sSql = sSqlBase + sSqlBaseD +
//								"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
								"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   			
			   		//「My貯金残高」
//			   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
			   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
			   			sSqlBaseD = "" +
//			   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   			sSql = sSqlBase + sSqlBaseD +
//								"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
								"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
				   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
//				   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
				   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
				   				"        )" +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		}
//					biAryDaysMyDp[iDay] = (BigInteger)JPA.em().createNativeQuery(
//							sSql).getSingleResult();
//					sAryDaysMyDp[iDay] = lAryDaysMyDp[iDay]==null ? "" : String.format("%1$,3d", lAryDaysMyDp[iDay]);
					BigInteger biAryDaysMyDp = (BigInteger)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					lAryDaysMyDp[iDay] = biAryDaysMyDp == null ? 0L : biAryDaysMyDp.longValue();
					sAryDaysMyDp[iDay] = String.format("%1$,3d", lAryDaysMyDp[iDay]);
				}
//				wDaIdealDepo.setBiAryDays(lAryDaysMyDp);
				wDaIdealDepo.setLAryDays(lAryDaysMyDp);
				wDaIdealDepo.setsAryDays(sAryDaysMyDp);
	
				lWDA.add(wDaIdealDepo);
				
			}
		//「実残高」
//		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
			//取扱(実際)ごとのループ（クレジットカードは除いて、引落口座に集約）
//			List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = " + haUser.id + " and handling_type_mst.handling_type_name <> '" + Messages.get("HandlingType.creca") + "'").fetch();
			List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = " + haUser.id + " and handling_type_mst.handling_type_name <> '" + HANDLING_TYPE_CRECA + "'").fetch();
			//for (WorkDailyAccount wda : lWDA) {
			for(HandlingMst handlingMst : handlingMsts) {
				WorkDailyAccount wDaHandling = new WorkDailyAccount();
				
    	   		sSqlBase = "" +
    	   				" SELECT COALESCE(SUM(" +
    	   				"   CASE " +
    	   				//    「収入」は加算
//    	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.in") + "' THEN r.amount" +
    	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IN + "' THEN r.amount" +
    	   				//    「支出」は減算
//    	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.out") + "' THEN -r.amount" +
    	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_OUT + "' THEN -r.amount" +
    	   				"     ELSE" +
    	   				"       CASE " +
    	   				//        現金の場合
//    	   				"         WHEN '" + handlingMst.handling_name + "' = '" + Messages.get("HandlingType.cash") + "' THEN " +
    	   				"         WHEN '" + handlingMst.handling_name + "' = '" + HANDLING_TYPE_CASH + "' THEN " +
    	   				"           CASE " +
    	   				//            「口座引出」は加算
//    	   				"             WHEN b.balance_type_name = '" + Messages.get("BalanceType.bank_out") + "' THEN r.amount" +
    	   				"             WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_OUT + "' THEN r.amount" +
    	   				//            「口座預入」は減算
//    	   				"             WHEN b.balance_type_name = '" + Messages.get("BalanceType.bank_in") + "' THEN -r.amount" +
    	   				"             WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_IN + "' THEN -r.amount" +
    	   				"           END" +
    	   				//        現金以外（口座・電子マネー）の場合
    	   				"         ELSE" +
    	   				"           CASE " +
    	   				//            「口座引出」は減算
//    	   				"             WHEN b.balance_type_name = '" + Messages.get("BalanceType.bank_out") + "' THEN -r.amount" +
    	   				"             WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_OUT + "' THEN -r.amount" +
    	   				//            「口座預入」は加算
//    	   				"             WHEN b.balance_type_name = '" + Messages.get("BalanceType.bank_in") + "' THEN r.amount" +
    	   				"             WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_IN + "' THEN r.amount" +
    	   				"           END" +
    	   				"       END" +
    	   				"   END" +
    	   				"   ), 0) " +
    	   				" FROM Record r " +
    					" LEFT JOIN ItemMst i " +
    					"   ON r.item_mst_id = i.id " +
    					" LEFT JOIN BalanceTypeMst b " +
    					"   ON r.balance_type_mst_id = b.id " +
    					" LEFT JOIN HandlingMst h " +
    					"   ON r.handling_mst_id = h.id " +
    					" LEFT JOIN HandlingTypeMst ht " +
    					"   ON h.handling_type_mst_id = ht.id" +
    					" LEFT JOIN HandlingMst hb " +
    					"   ON h.debit_bank_id = hb.id" +
    					" WHERE r.ha_user_id = " + haUser.id +
//						"   AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "','" + Messages.get("BalanceType.bank_out") + "','" + Messages.get("BalanceType.bank_in") + "') " +
						"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "','" + BALANCE_TYPE_BANK_OUT + "','" + BALANCE_TYPE_BANK_IN + "') " +
						"   AND (" +
						//       現金の場合
//						"        (    '" + handlingMst.handling_name + "' = '" + Messages.get("HandlingType.cash") + "'" +
//						"         AND (   (    ht.handling_type_name = '" + Messages.get("HandlingType.cash") + "' " +
//						"                  AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "')" +
//						"                  )" +
//						"              OR (    ht.handling_type_name in('" + Messages.get("HandlingType.bank") + "','" + Messages.get("HandlingType.emoney") + "')" +
//						"                  AND b.balance_type_name in('" + Messages.get("BalanceType.bank_out") + "','" + Messages.get("BalanceType.bank_in") + "')" +
//						"                  )" +
//						"              )" +
//						"         ) OR " +
						"        (    '" + handlingMst.handling_name + "' = '" + HANDLING_TYPE_CASH + "'" +
						"         AND (   (    ht.handling_type_name = '" + HANDLING_TYPE_CASH + "' " +
						"                  AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "')" +
						"                  )" +
						"              OR (    ht.handling_type_name in('" + HANDLING_TYPE_BANK + "','" + HANDLING_TYPE_EMONEY + "')" +
						"                  AND b.balance_type_name in('" + BALANCE_TYPE_BANK_OUT + "','" + BALANCE_TYPE_BANK_IN + "')" +
						"                  )" +
						"              )" +
						"         ) OR " +
						//       現金以外（口座・電子マネー）の場合
//						"        (    '" + handlingMst.handling_name + "' <> '" + Messages.get("HandlingType.cash") + "'" +
						"        (    '" + handlingMst.handling_name + "' <> '" + HANDLING_TYPE_CASH + "'" +
						"         AND '" + handlingMst.handling_name + "' in(h.handling_name, hb.handling_name)" +
						"         )" +
						"        )";
	   			sSql = sSqlBase + sSqlBaseG;
	
				BigInteger biSumMonthRlBal = (BigInteger)JPA.em().createNativeQuery(
						sSql).getSingleResult();
				long lSumMonthRlBal = biSumMonthRlBal == null ? 0L : biSumMonthRlBal.longValue();
				
				wDaHandling.setsLargeCategory(sLargeCategoryName);
				wDaHandling.setsItem(handlingMst.handling_name);
				wDaHandling.setbBudgetFlg(false);
//				wDaHandling.setsSumMonth(lSumMonthRlBal==null ? "" : String.format("%1$,3d", lSumMonthRlBal));
				wDaHandling.setsSumMonth(String.format("%1$,3d", lSumMonthRlBal));
	
				// 日毎
//				BigInteger[] biAryDaysRlBal = new BigInteger[iDaysCnt];
				long[] lAryDaysRlBal = new long[iDaysCnt];
				String[] sAryDaysRlBal = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iStartDay + iDay);
			   		String sSqlBaseD = "";
					//初日のみデータ集約。以降は加算
					if(iDay == 0) {
				   		sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   			sSql = sSqlBase + sSqlBaseD;
//					    biAryDaysRlBal[iDay] = (BigInteger)JPA.em().createNativeQuery(
//					    		sSql).getSingleResult();
//					    sAryDaysRlBal[iDay] = biAryDaysRlBal[iDay]==null ? "" : String.format("%1$,3d", biAryDaysRlBal[iDay]);
					    BigInteger biAryDaysRlBal = (BigInteger)JPA.em().createNativeQuery(
					    		sSql).getSingleResult();
					    lAryDaysRlBal[iDay] = biAryDaysRlBal == null ? 0L : biAryDaysRlBal.longValue();
					} else {
				   		sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   			sSql = sSqlBase + sSqlBaseD;
					    BigInteger biAryDaysRlBal = (BigInteger)JPA.em().createNativeQuery(
					    		sSql).getSingleResult();
					    lAryDaysRlBal[iDay] = lAryDaysRlBal[iDay-1] + (biAryDaysRlBal == null ? 0L : biAryDaysRlBal.longValue());
					}
					sAryDaysRlBal[iDay] = String.format("%1$,3d", lAryDaysRlBal[iDay]);
				}
//				wDaHandling.setBiAryDays(biAryDaysRlBal);
				wDaHandling.setLAryDays(lAryDaysRlBal);
				wDaHandling.setsAryDays(sAryDaysRlBal);
	
				lWDA.add(wDaHandling);
				
			}
		}

	}
	
}
