package controllers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
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
	
	/**
	 * dailyAccount.htmlの表示
	 * @param year	表示対象の年
	 * @param month	表示対象の月
	 */
	public static void dailyAccount(
			Integer year,
			Integer month
			) {
		
   		Calendar calendar = Calendar.getInstance();
   		
   		//単純に呼ばれた時（初回等）は、今月を表示
		if((year==null)||(month==null)) {
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH) + 1;
		}
		
		//今月判定フラグ
		Integer thisMonthFlg = 0;
		if((year==calendar.get(Calendar.YEAR)) &&
    	   (month==calendar.get(Calendar.MONTH)+1)) {
			thisMonthFlg = 1;
		}
		
		calendar.set(year, month - 1, 1);
		int iDaysCnt = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
   		
		//日計表のヘッダーの日付の配列
		int[] iAryDays = new int[iDaysCnt];
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			iAryDays[iDay] = iDay + 1;
		}
		
		//日計表の行に相当するリストの作成
   		List<WorkDailyAccount> lWDA = makeWorkList(year, month, iDaysCnt);
   		
   		//日計表の日付ごとの部分のスクロール内の幅の設定
		int iWidth = iDaysCnt * 100;
   		
		render(year, month, thisMonthFlg, iAryDays, lWDA, iWidth);
	}
	
	/**
	 * 対象年月の移動
	 * @param year
	 * @param month
	 * @param prevYear
	 * @param prevMonth
	 * @param nextMonth
	 * @param nextYear
	 * @param thisMonth
	 */
	public static void jump(
			Integer year,
			Integer month,
    		String prevYear,	/* 「<<」ボタン */
    		String prevMonth,	/* 「<」ボタン */
    		String nextMonth,	/* 「>」ボタン */
    		String nextYear,	/* 「>>」ボタン */
    		String thisMonth	/* 「今月」ボタン */
			) {

		Calendar calendar = Calendar.getInstance();
    	// 「今月」ボタンが押された場合
    	if(thisMonth != null) {
    		dailyAccount(null, null);
    	} else {
			calendar.set(year, month - 1, 1);
	    	// 「<<」ボタンが押された場合
	    	if(prevYear != null) {
	    		calendar.add(Calendar.YEAR, -1);
				
	    	// 「<」ボタンが押された場合
	    	} else if(prevMonth != null) {
	    		calendar.add(Calendar.MONTH, -1);

	    	// 「>」ボタンが押された場合
	    	} else if(nextMonth != null) {
	    		calendar.add(Calendar.MONTH, 1);

	    	// 「>>」ボタンが押された場合
	    	} else if(nextYear != null) {
	    		calendar.add(Calendar.YEAR, 1);

	    	}
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH) + 1;
	    		
	    	dailyAccount(year, month);
    	}
	}

	/**
	 * 日計表の行に相当するリストの作成
	 * @param year
	 * @param month
	 * @param iDaysCnt
	 * @return
	 */
	private static List<WorkDailyAccount> makeWorkList(
			Integer year,
			Integer month,
			int iDaysCnt
			) {
		//日計表の行に相当するリスト
   		List<WorkDailyAccount> lWDA = new ArrayList<WorkDailyAccount>();
   		
   		Calendar calendar = Calendar.getInstance();
   		
		calendar.set(year, month - 1, 1);
		String sFirstDay =  String.format("%1$tY%1$tm%1$td", calendar.getTime());
   		calendar.add(Calendar.MONTH, 1);
   		String sNextFirst = String.format("%1$tY%1$tm%1$td", calendar.getTime());
   		
		HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();

   		
		//「収入」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				Messages.get("BalanceType.in"), lWDA);
		
		//「支出」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				Messages.get("BalanceType.out"), lWDA);
		
		//「My貯金預入」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				Messages.get("BalanceType.ideal_deposit_in"), lWDA);
		
		
		//「差額」　　（　「収入」－「支出」－「My貯金預入」　）
   		WorkDailyAccount wDaDiff = new WorkDailyAccount();
//		BigInteger biSumMonthDiff = new BigInteger("0");
//		BigInteger[] biAryDaysDiff = new BigInteger[iDaysCnt];
		long lSumMonthDiff = 0;
		long[] lAryDaysDiff = new long[iDaysCnt];
		
		String[] sAryDaysDiff = new String[iDaysCnt];
		for (WorkDailyAccount wda : lWDA) {
			//「収入」・「支出」・「My貯金預入」の合計行で算出する
			if((wda.getsItem().equals("")) &&
			   (wda.getsLargeCategory().equals(Messages.get("BalanceType.in")) ||
				wda.getsLargeCategory().equals(Messages.get("BalanceType.out")) ||
				wda.getsLargeCategory().equals(Messages.get("BalanceType.ideal_deposit_in")))
					) {
				
				//「収入」は「差額」に加算
				if(wda.getsLargeCategory().equals(Messages.get("BalanceType.in"))) {
//					if(wda.getBiSumMonth() != null) {
//						lSumMonthDiff = lSumMonthDiff.add(wda.getBiSumMonth());
//					}
					lSumMonthDiff += wda.getLSumMonth();
				//「支出」・「My貯金預入」は「差額」から減算
				} else if(wda.getsLargeCategory().equals(Messages.get("BalanceType.out")) ||
						wda.getsLargeCategory().equals(Messages.get("BalanceType.ideal_deposit_in"))) {
//					if(wda.getBiSumMonth() != null) {
//						lSumMonthDiff = lSumMonthDiff.subtract(wda.getBiSumMonth());
//					}
					lSumMonthDiff += wda.getLSumMonth();
				}

				// 日毎
//				BigInteger[] biAryDaysTmp = wda.getBiAryDays();
				long[] lAryDaysTmp = wda.getLAryDays();
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
//					if(lAryDaysDiff[iDay] == null) {
//						lAryDaysDiff[iDay] = new BigInteger("0"); 
//					}
					//「収入」は「差額」に加算
					if(wda.getsLargeCategory().equals(Messages.get("BalanceType.in"))) {
//		   				if(lAryDaysTmp[iDay] != null) {
//		   					lAryDaysDiff[iDay] = lAryDaysDiff[iDay].add(lAryDaysTmp[iDay]);
//		   				}
	   					lAryDaysDiff[iDay] += lAryDaysTmp[iDay];
		   			//「支出」・「My貯金預入」は「差額」から減算
					} else if(wda.getsLargeCategory().equals(Messages.get("BalanceType.out")) ||
							wda.getsLargeCategory().equals(Messages.get("BalanceType.ideal_deposit_in"))) {
//		   				if(lAryDaysTmp[iDay] != null) {
//		   					lAryDaysDiff[iDay] = lAryDaysDiff[iDay].subtract(lAryDaysTmp[iDay]); 
//		   				}
	   					lAryDaysDiff[iDay] += lAryDaysTmp[iDay]; 
					}
				}
				
			}
		}
		wDaDiff.setsLargeCategory(Messages.get("views.detaillist.diff"));
		wDaDiff.setsItem("");
//		wDaDiff.setBiSumMonth(lSumMonthDiff);
		wDaDiff.setLSumMonth(lSumMonthDiff);
//		wDaDiff.setsSumMonth(lSumMonthDiff==null ? "" : String.format("%1$,3d", lSumMonthDiff));
		wDaDiff.setsSumMonth(String.format("%1$,3d", lSumMonthDiff));
//		wDaDiff.setBiAryDays(lAryDaysDiff);
		wDaDiff.setLAryDays(lAryDaysDiff);
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
//			sAryDaysDiff[iDay] = lAryDaysDiff[iDay]==null ? "" : String.format("%1$,3d", lAryDaysDiff[iDay]);
			sAryDaysDiff[iDay] = String.format("%1$,3d", lAryDaysDiff[iDay]);
		}
		wDaDiff.setsAryDays(sAryDaysDiff);
		
		lWDA.add(wDaDiff);
		
		
		//「My貯金から支払」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				Messages.get("BalanceType.out_ideal_deposit"), lWDA);
		
		//「実残高」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				Messages.get("RemainderType.real"), lWDA);
		
		//「My貯金残高」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				Messages.get("RemainderType.ideal_deposit"), lWDA);


		//「My貯金してないお金」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				Messages.get("RemainderType.not_ideal_deposit"), lWDA);

		
		return lWDA;
	}
	
	/**
	 * 日計表の行に相当するリストの作成（「収入」・「支出」・「My貯金預入」・「差額」・「My貯金から支払」毎に作成する）
	 * @param year
	 * @param month
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
   		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) ||
   				sLargeCategoryName.equals(Messages.get("BalanceType.out")) ||
   				sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in")) ||
   				sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
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
   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				//「収入」は加算
	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.in") + "' THEN r.amount" +
	   				//「支出」は減算
	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.out") + "' THEN -r.amount" +
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
   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				"     WHEN (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' THEN r.amount" +
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
   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.not_ideal_deposit"))) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				//「収入」は加算
	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.in") + "' THEN r.amount " +
	   				//「支出」で「取扱(My貯金)」未選択は減算
	   				"     WHEN (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NULL) THEN -r.amount " +
	   				//「My貯金預入」は減算
	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' THEN -r.amount" +
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
   		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) || sLargeCategoryName.equals(Messages.get("BalanceType.out"))) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
					"   AND r.ideal_deposit_mst_id IS NULL ";
   		//「My貯金預入」
   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		//「My貯金から支払」
   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		//「実残高」
   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "') ";
   		//「My貯金残高」
   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
	   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
	   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
	   				"        )";
   		//「My貯金してないお金」
   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.not_ideal_deposit"))) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND (b.balance_type_name = '" + Messages.get("BalanceType.in") + "' OR " +
	   				"        (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
   				    "         r.ideal_deposit_mst_id IS NULL) OR " +
   				    "        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
	   				"        )";
   		}
//		BigInteger biSumMonthG = (BigInteger)JPA.em().createNativeQuery(
//				sSql).getSingleResult();
		long lSumMonthG = (long)JPA.em().createNativeQuery(
				sSql).getSingleResult();

		wDA.setsLargeCategory(sLargeCategoryName);
		wDA.setsItem("");
		wDA.setbBudgetFlg(false);
//		wDA.setBiSumMonth(biSumMonthG);
		wDA.setLSumMonth(lSumMonthG);
//		wDA.setsSumMonth(lSumMonthG==null ? "" : String.format("%1$,3d", lSumMonthG));
		wDA.setsSumMonth(String.format("%1$,3d", lSumMonthG));
		
		//  「収入」・「支出」・「My貯金預入」の場合、予算有無フラグを立てる
		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) ||
				sLargeCategoryName.equals(Messages.get("BalanceType.out")) ||
				sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
			wDA.setbBudgetFlg(true);
		}
		
		
		// 日毎
//		BigInteger[] biAryDaysG = new BigInteger[iDaysCnt];
		long[] lAryDaysG = new long[iDaysCnt];
		String[] sAryDaysG = new String[iDaysCnt];
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			calendar.set(year, month - 1, iDay + 1);
	   		String sSqlBaseD = "" +
					"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   		//「収入」・「支出」
	   		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) || sLargeCategoryName.equals(Messages.get("BalanceType.out"))) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL ";
	   		//「My貯金預入」
	   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		//「My貯金から支払」
	   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		//「実残高」
	   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
	   			sSqlBaseD = "" +
//	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "') ";
	   		//「My貯金残高」
	   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
	   			sSqlBaseD = "" +
//	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
		   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
		   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
		   				"        )";
	   		//「My貯金してないお金」
	   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.not_ideal_deposit"))) {
	   			sSqlBaseD = "" +
//	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND (b.balance_type_name = '" + Messages.get("BalanceType.in") + "' OR " +
		   				"        (b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
	   				    "         r.ideal_deposit_mst_id IS NULL) OR " +
   				        "        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
		   				"        )";
	   		}
//			biAryDaysG[iDay] = (BigInteger)JPA.em().createNativeQuery(
//					sSql).getSingleResult();
//			sAryDaysG[iDay] = lAryDaysG[iDay]==null ? "" : String.format("%1$,3d", lAryDaysG[iDay]);
			lAryDaysG[iDay] = (long)JPA.em().createNativeQuery(
					sSql).getSingleResult();
			sAryDaysG[iDay] = String.format("%1$,3d", lAryDaysG[iDay]);

		}
//		wDA.setBiAryDays(biAryDaysG);
		wDA.setLAryDays(lAryDaysG);
		wDA.setsAryDays(sAryDaysG);
		
		lWDA.add(wDA);

		
		//「収入」・「支出」
		if(sLargeCategoryName.equals(Messages.get("BalanceType.in")) || sLargeCategoryName.equals(Messages.get("BalanceType.out"))) {
			//項目ごとのループ
			List<ItemMst> itemMsts = ItemMst.find("ha_user = " + haUser.id + " and balance_type_mst.balance_type_name = '" + sLargeCategoryName + "' order by id").fetch();
			for(Iterator<ItemMst> itrItem = itemMsts.iterator(); itrItem.hasNext();) {
				ItemMst itemMst = itrItem.next();
				
				WorkDailyAccount wDaItem = new WorkDailyAccount();
	
//				BigInteger biSumMonth = (BigInteger)JPA.em().createNativeQuery(
//						sSqlBase + sSqlBaseG +
//						"   AND i.item_name = '" + itemMst.item_name + "' " +
//						"   AND r.ideal_deposit_mst_id IS NULL "
//						).getSingleResult();
				long lSumMonth = (long)JPA.em().createNativeQuery(
						sSqlBase + sSqlBaseG +
						"   AND i.item_name = '" + itemMst.item_name + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL "
						).getSingleResult();
				
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
					calendar.set(year, month - 1, iDay + 1);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
//					lAryDays[iDay] = (BigInteger)JPA.em().createNativeQuery(
//							sSqlBase + sSqlBaseD +
//							"   AND i.item_name = '" + itemMst.item_name + "' " +
//							"   AND r.ideal_deposit_mst_id IS NULL "
//							).getSingleResult();
//					sAryDays[iDay] = lAryDays[iDay]==null ? "" : String.format("%1$,3d", lAryDays[iDay]);
					lAryDays[iDay] = (long)JPA.em().createNativeQuery(
							sSqlBase + sSqlBaseD +
							"   AND i.item_name = '" + itemMst.item_name + "' " +
							"   AND r.ideal_deposit_mst_id IS NULL "
							).getSingleResult();
					sAryDays[iDay] = String.format("%1$,3d", lAryDays[iDay]);
				}
//				wDaItem.setBiAryDays(lAryDays);
				wDaItem.setLAryDays(lAryDays);
				wDaItem.setsAryDays(sAryDays);
	
				lWDA.add(wDaItem);
				
			}
		//「My貯金預入」・「My貯金から支払」・「My貯金残高」
		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in")) ||
				sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit")) ||
				sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
			//My貯金ごとのループ
			List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = " + haUser.id).fetch();
			for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
				IdealDepositMst idealDepositMst = itrIdealDeposit.next();
				
				WorkDailyAccount wDaIdealDepo = new WorkDailyAccount();
				
				if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
			   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
			   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
			   				"        )" +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   		}
	
//				BigInteger biSumMonthMyDp = (BigInteger)JPA.em().createNativeQuery(
//						sSql).getSingleResult();
				long lSumMonthMyDp = (long)JPA.em().createNativeQuery(
						sSql).getSingleResult();
				
				wDaIdealDepo.setsLargeCategory(sLargeCategoryName);
				wDaIdealDepo.setsItem(idealDepositMst.ideal_deposit_name);
				if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
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
					calendar.set(year, month - 1, iDay + 1);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
					if(sLargeCategoryName.equals(Messages.get("BalanceType.ideal_deposit_in"))) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		} else if(sLargeCategoryName.equals(Messages.get("BalanceType.out_ideal_deposit"))) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND b.balance_type_name = '" + Messages.get("BalanceType.out") + "' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.ideal_deposit"))) {
			   			sSqlBaseD = "" +
//			   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND ((b.balance_type_name = '" + Messages.get("BalanceType.out") + "' AND " +
				   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
				   				"        b.balance_type_name = '" + Messages.get("BalanceType.ideal_deposit_in") + "'" +
				   				"        )" +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		}
//					lAryDaysMyDp[iDay] = (BigInteger)JPA.em().createNativeQuery(
//							sSql).getSingleResult();
//					sAryDaysMyDp[iDay] = lAryDaysMyDp[iDay]==null ? "" : String.format("%1$,3d", lAryDaysMyDp[iDay]);
					lAryDaysMyDp[iDay] = (long)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					sAryDaysMyDp[iDay] = String.format("%1$,3d", lAryDaysMyDp[iDay]);
				}
//				wDaIdealDepo.setBiAryDays(lAryDaysMyDp);
				wDaIdealDepo.setLAryDays(lAryDaysMyDp);
				wDaIdealDepo.setsAryDays(sAryDaysMyDp);
	
				lWDA.add(wDaIdealDepo);
				
			}
		//「実残高」
		} else if(sLargeCategoryName.equals(Messages.get("RemainderType.real"))) {
			
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				//「収入」は加算
	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.in") + "' THEN r.amount" +
	   				//「支出」は減算
	   				"     WHEN b.balance_type_name = '" + Messages.get("BalanceType.out") + "' THEN -r.amount" +
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

			
			//取扱(実際)ごとのループ（クレジットカードは除いて、引落口座に集約）
			List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = " + haUser.id + " and handling_type_mst.handling_type_name <> '" + Messages.get("HandlingType.creca") + "'").fetch();
			//for (WorkDailyAccount wda : lWDA) {
			for(HandlingMst handlingMst : handlingMsts) {
				WorkDailyAccount wDaHandling = new WorkDailyAccount();
				
	   			sSql = sSqlBase + sSqlBaseG +
						"   AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "') " +
//						"   AND h.handling_name = '" + handlingMst.handling_name + "'";
						"   AND '" + handlingMst.handling_name + "' in(h.handling_name, hb.handling_name)";
	
//				BigInteger biSumMonthRlBal = (BigInteger)JPA.em().createNativeQuery(
//						sSql).getSingleResult();
				long lSumMonthRlBal = (long)JPA.em().createNativeQuery(
						sSql).getSingleResult();
				
				wDaHandling.setsLargeCategory(sLargeCategoryName);
				wDaHandling.setsItem(handlingMst.handling_name);
				wDaHandling.setbBudgetFlg(false);
//				wDaHandling.setsSumMonth(lSumMonthRlBal==null ? "" : String.format("%1$,3d", lSumMonthRlBal));
				wDaHandling.setsSumMonth(String.format("%1$,3d", lSumMonthRlBal));
	
				// 日毎
//				BigInteger[] biAryDaysRlBal = new BigInteger[iDaysCnt];
				long[] biAryDaysRlBal = new long[iDaysCnt];
				String[] sAryDaysRlBal = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iDay + 1);
			   		String sSqlBaseD = "" +
//							"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
							"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
		   			sSql = sSqlBase + sSqlBaseD +
							"   AND b.balance_type_name in('" + Messages.get("BalanceType.out") + "','" + Messages.get("BalanceType.in") + "') " +
//							"   AND h.handling_name = '" + handlingMst.handling_name + "'";
							"   AND '" + handlingMst.handling_name + "' in(h.handling_name, hb.handling_name)";
//					biAryDaysRlBal[iDay] = (BigInteger)JPA.em().createNativeQuery(
//							sSql).getSingleResult();
//					sAryDaysRlBal[iDay] = biAryDaysRlBal[iDay]==null ? "" : String.format("%1$,3d", biAryDaysRlBal[iDay]);
					biAryDaysRlBal[iDay] = (long)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					sAryDaysRlBal[iDay] = String.format("%1$,3d", biAryDaysRlBal[iDay]);
				}
//				wDaHandling.setBiAryDays(biAryDaysRlBal);
				wDaHandling.setLAryDays(biAryDaysRlBal);
				wDaHandling.setsAryDays(sAryDaysRlBal);
	
				lWDA.add(wDaHandling);
				
			}
		}

	}
	
}
