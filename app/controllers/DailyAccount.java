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
				"収入", lWDA);
		
		//「支出」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				"支出", lWDA);
		
		//「My貯金預入」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				"My貯金預入", lWDA);
		
		
		//「差額」　　（　「収入」－「支出」－「My貯金預入」　）
   		WorkDailyAccount wDaDiff = new WorkDailyAccount();
		BigInteger biSumMonthDiff = new BigInteger("0");
		BigInteger[] biAryDaysDiff = new BigInteger[iDaysCnt];
		String[] sAryDaysDiff = new String[iDaysCnt];
		for (WorkDailyAccount wda : lWDA) {
			//「収入」・「支出」・「My貯金預入」の合計行で算出する
			if((wda.getsItem().equals("")) &&
			   (wda.getsLargeCategory().equals("収入") ||
				wda.getsLargeCategory().equals("支出") ||
				wda.getsLargeCategory().equals("My貯金預入"))
					) {
				
				if(wda.getsLargeCategory().equals("収入")) {
					if(wda.getBiSumMonth() != null) {
						biSumMonthDiff = biSumMonthDiff.add(wda.getBiSumMonth());
					}
				} else if(wda.getsLargeCategory().equals("支出") ||
						wda.getsLargeCategory().equals("My貯金預入")) {
					if(wda.getBiSumMonth() != null) {
						biSumMonthDiff = biSumMonthDiff.subtract(wda.getBiSumMonth());
					}
				}

				// 日毎
				BigInteger[] biAryDaysTmp = wda.getBiAryDays();
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					if(biAryDaysDiff[iDay] == null) {
						biAryDaysDiff[iDay] = new BigInteger("0"); 
					}
					if(wda.getsLargeCategory().equals("収入")) {
		   				if(biAryDaysTmp[iDay] != null) {
		   					biAryDaysDiff[iDay] = biAryDaysDiff[iDay].add(biAryDaysTmp[iDay]);
		   				}
					} else if(wda.getsLargeCategory().equals("支出") ||
							wda.getsLargeCategory().equals("My貯金預入")) {
		   				if(biAryDaysTmp[iDay] != null) {
		   					biAryDaysDiff[iDay] = biAryDaysDiff[iDay].subtract(biAryDaysTmp[iDay]); 
		   				}
					}
				}
				
			}
		}
		wDaDiff.setsLargeCategory("差額");
		wDaDiff.setsItem("");
		wDaDiff.setBiSumMonth(biSumMonthDiff);
		wDaDiff.setsSumMonth(biSumMonthDiff==null ? "" : String.format("%1$,3d", biSumMonthDiff));
		wDaDiff.setBiAryDays(biAryDaysDiff);
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			sAryDaysDiff[iDay] = biAryDaysDiff[iDay]==null ? "" : String.format("%1$,3d", biAryDaysDiff[iDay]);
		}
		wDaDiff.setsAryDays(sAryDaysDiff);
		
		lWDA.add(wDaDiff);
		
		
		//「My貯金から支払」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				"My貯金から支払", lWDA);
		
		//「実残高」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				"実残高", lWDA);
		
		//「My貯金残高」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				"My貯金残高", lWDA);


		//「My貯金してないお金」
		makeWorkListEach(year, month, iDaysCnt, haUser, sFirstDay, sNextFirst,
				"My貯金してないお金", lWDA);

		
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

   		if(sLargeCategoryName.equals("収入") ||
   				sLargeCategoryName.equals("支出") ||
   				sLargeCategoryName.equals("My貯金預入") ||
   				sLargeCategoryName.equals("My貯金から支払")) {
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
   		} else if(sLargeCategoryName.equals("実残高")) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				"     WHEN b.balance_type_name = '収入' THEN r.amount" +
	   				"     WHEN b.balance_type_name = '支出' THEN -r.amount" +
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
					" WHERE r.ha_user_id = " + haUser.id;
	   		sSqlBaseG = "" +
					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
   		} else if(sLargeCategoryName.equals("My貯金残高")) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				"     WHEN (b.balance_type_name = '支出' AND " +
	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
	   				"     WHEN b.balance_type_name = 'My貯金預入' THEN r.amount" +
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
					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
   		} else if(sLargeCategoryName.equals("My貯金してないお金")) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				"     WHEN b.balance_type_name = '収入' THEN r.amount " +
	   				"     WHEN (b.balance_type_name = '支出' AND " +
	   				"           r.ideal_deposit_mst_id IS NULL) THEN -r.amount " +
	   				"     WHEN b.balance_type_name = 'My貯金預入' THEN -r.amount" +
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
					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
   		}
   		
   		
   		if(sLargeCategoryName.equals("収入") || sLargeCategoryName.equals("支出")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
					"   AND r.ideal_deposit_mst_id IS NULL ";
   		} else if(sLargeCategoryName.equals("My貯金預入")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = 'My貯金預入' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = '支出' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		} else if(sLargeCategoryName.equals("実残高")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name in('支出','収入') ";
   		} else if(sLargeCategoryName.equals("My貯金残高")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND ((b.balance_type_name = '支出' AND " +
	   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
	   				"        b.balance_type_name = 'My貯金預入'" +
	   				"        )";
   		} else if(sLargeCategoryName.equals("My貯金してないお金")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND (b.balance_type_name = '収入' OR " +
	   				"        (b.balance_type_name = '支出' AND " +
   				    "         r.ideal_deposit_mst_id IS NULL) OR " +
   				    "        b.balance_type_name = 'My貯金預入' " +
	   				"        )";
   		}
		BigInteger biSumMonthG = (BigInteger)JPA.em().createNativeQuery(
				sSql).getSingleResult();

		wDA.setsLargeCategory(sLargeCategoryName);
		wDA.setsItem("");
		wDA.setbBudgetFlg(false);
		wDA.setBiSumMonth(biSumMonthG);
		wDA.setsSumMonth(biSumMonthG==null ? "" : String.format("%1$,3d", biSumMonthG));
		
		//  「収入」・「支出」・「My貯金預入」の場合、予算有無フラグを立てる
		if(sLargeCategoryName.equals("収入") ||
				sLargeCategoryName.equals("支出") ||
				sLargeCategoryName.equals("My貯金預入")) {
			wDA.setbBudgetFlg(true);
		}
		
		
		// 日毎
		BigInteger[] biAryDaysG = new BigInteger[iDaysCnt];
		String[] sAryDaysG = new String[iDaysCnt];
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			calendar.set(year, month - 1, iDay + 1);
	   		String sSqlBaseD = "" +
					"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   		if(sLargeCategoryName.equals("収入") || sLargeCategoryName.equals("支出")) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL ";
	   		} else if(sLargeCategoryName.equals("My貯金預入")) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = 'My貯金預入' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '支出' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		} else if(sLargeCategoryName.equals("実残高")) {
	   			sSqlBaseD = "" +
	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name in('支出','収入') ";
	   		} else if(sLargeCategoryName.equals("My貯金残高")) {
	   			sSqlBaseD = "" +
	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND ((b.balance_type_name = '支出' AND " +
		   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
		   				"        b.balance_type_name = 'My貯金預入'" +
		   				"        )";
	   		} else if(sLargeCategoryName.equals("My貯金してないお金")) {
	   			sSqlBaseD = "" +
	   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND (b.balance_type_name = '収入' OR " +
		   				"        (b.balance_type_name = '支出' AND " +
	   				    "         r.ideal_deposit_mst_id IS NULL) OR " +
   				        "        b.balance_type_name = 'My貯金預入' " +
		   				"        )";
	   		}
			biAryDaysG[iDay] = (BigInteger)JPA.em().createNativeQuery(
					sSql).getSingleResult();
			sAryDaysG[iDay] = biAryDaysG[iDay]==null ? "" : String.format("%1$,3d", biAryDaysG[iDay]);
			

		}
		wDA.setBiAryDays(biAryDaysG);
		wDA.setsAryDays(sAryDaysG);
		
		lWDA.add(wDA);

		
		if(sLargeCategoryName.equals("収入") || sLargeCategoryName.equals("支出")) {
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
				
				wDaItem.setsLargeCategory(sLargeCategoryName);
				wDaItem.setsItem(itemMst.item_name);
				wDaItem.setbBudgetFlg(true);
				wDaItem.setBiSumMonth(biSumMonth);
				wDaItem.setsSumMonth(biSumMonth==null ? "" : String.format("%1$,3d", biSumMonth));
	
				// 日毎
				BigInteger[] biAryDays = new BigInteger[iDaysCnt];
				String[] sAryDays = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iDay + 1);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
					biAryDays[iDay] = (BigInteger)JPA.em().createNativeQuery(
							sSqlBase + sSqlBaseD +
							"   AND i.item_name = '" + itemMst.item_name + "' " +
							"   AND r.ideal_deposit_mst_id IS NULL "
							).getSingleResult();
					sAryDays[iDay] = biAryDays[iDay]==null ? "" : String.format("%1$,3d", biAryDays[iDay]);
				}
				wDaItem.setBiAryDays(biAryDays);
				wDaItem.setsAryDays(sAryDays);
	
				lWDA.add(wDaItem);
				
			}
		} else if(sLargeCategoryName.equals("My貯金預入") ||
				sLargeCategoryName.equals("My貯金から支払") ||
				sLargeCategoryName.equals("My貯金残高")) {
			//My貯金ごとのループ
			List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = " + haUser.id).fetch();
			for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
				IdealDepositMst idealDepositMst = itrIdealDeposit.next();
				
				WorkDailyAccount wDaIdealDepo = new WorkDailyAccount();
				
				if(sLargeCategoryName.equals("My貯金預入")) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND b.balance_type_name = 'My貯金預入' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND b.balance_type_name = '支出' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   		} else if(sLargeCategoryName.equals("My貯金残高")) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND ((b.balance_type_name = '支出' AND " +
			   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
			   				"        b.balance_type_name = 'My貯金預入'" +
			   				"        )" +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   		}
	
				BigInteger biSumMonthMyDp = (BigInteger)JPA.em().createNativeQuery(
						sSql).getSingleResult();
				
				wDaIdealDepo.setsLargeCategory(sLargeCategoryName);
				wDaIdealDepo.setsItem(idealDepositMst.ideal_deposit_name);
				if(sLargeCategoryName.equals("My貯金預入")) {
					wDaIdealDepo.setbBudgetFlg(true);
				} else {
					wDaIdealDepo.setbBudgetFlg(false);
				}
				wDaIdealDepo.setsSumMonth(biSumMonthMyDp==null ? "" : String.format("%1$,3d", biSumMonthMyDp));
	
				// 日毎
				BigInteger[] biAryDaysMyDp = new BigInteger[iDaysCnt];
				String[] sAryDaysMyDp = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iDay + 1);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
					if(sLargeCategoryName.equals("My貯金預入")) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND b.balance_type_name = 'My貯金預入' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND b.balance_type_name = '支出' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		} else if(sLargeCategoryName.equals("My貯金残高")) {
			   			sSqlBaseD = "" +
			   					"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND ((b.balance_type_name = '支出' AND " +
				   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
				   				"        b.balance_type_name = 'My貯金預入'" +
				   				"        )" +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		}
					biAryDaysMyDp[iDay] = (BigInteger)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					sAryDaysMyDp[iDay] = biAryDaysMyDp[iDay]==null ? "" : String.format("%1$,3d", biAryDaysMyDp[iDay]);
				}
				wDaIdealDepo.setBiAryDays(biAryDaysMyDp);
				wDaIdealDepo.setsAryDays(sAryDaysMyDp);
	
				lWDA.add(wDaIdealDepo);
				
			}
		} else if(sLargeCategoryName.equals("実残高")) {
			//取扱ごとのループ
			List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = " + haUser.id).fetch();
			//for (WorkDailyAccount wda : lWDA) {
			for(HandlingMst handlingMst : handlingMsts) {
				WorkDailyAccount wDaHandling = new WorkDailyAccount();
				
	   			sSql = sSqlBase + sSqlBaseG +
						"   AND b.balance_type_name in('支出','収入') " +
						"   AND h.handling_name = '" + handlingMst.handling_name + "'";
	
				BigInteger biSumMonthRlBal = (BigInteger)JPA.em().createNativeQuery(
						sSql).getSingleResult();
				
				wDaHandling.setsLargeCategory(sLargeCategoryName);
				wDaHandling.setsItem(handlingMst.handling_name);
				wDaHandling.setbBudgetFlg(false);
				wDaHandling.setsSumMonth(biSumMonthRlBal==null ? "" : String.format("%1$,3d", biSumMonthRlBal));
	
				// 日毎
				BigInteger[] biAryDaysRlBal = new BigInteger[iDaysCnt];
				String[] sAryDaysRlBal = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iDay + 1);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
		   			sSql = sSqlBase + sSqlBaseD +
							"   AND b.balance_type_name in('支出','収入') " +
							"   AND h.handling_name = '" + handlingMst.handling_name + "'";
					biAryDaysRlBal[iDay] = (BigInteger)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					sAryDaysRlBal[iDay] = biAryDaysRlBal[iDay]==null ? "" : String.format("%1$,3d", biAryDaysRlBal[iDay]);
				}
				wDaHandling.setBiAryDays(biAryDaysRlBal);
				wDaHandling.setsAryDays(sAryDaysRlBal);
	
				lWDA.add(wDaHandling);
				
			}
		}

	}
	
}
