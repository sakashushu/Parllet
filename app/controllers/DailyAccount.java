package controllers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import models.HaUser;
import models.IdealDepositMst;
import models.ItemMst;
import models.WorkDailyAccount;

import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class DailyAccount extends Controller {

	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			HaUser hauser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("userId", hauser.id);
		}
	}
	
	/**
	 * form.htmlの表示
	 * @param year	表示対象の年
	 * @param month	表示対象の月
	 */
	public static void form(
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
		if((year==calendar.get(Calendar.YEAR))&&
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
		
   		List<WorkDailyAccount> lWDA = makeWorkList(year, month, iDaysCnt);
   		
		int iWidth = iDaysCnt * 93;
   		
		render(year, month, thisMonthFlg, iAryDays, lWDA, iWidth);
	}
	
	/**
	 * 日計表の行に相当するリストの作成（「収入」・「支出」・「My貯金」・「差額」・「My貯金から支払」毎に作成する）
	 * @param year
	 * @param month
	 * @param iDaysCnt
	 * @param sSqlBase
	 * @param sSqlBaseG
	 * @param hauser
	 * @param wDaDiff
	 * @param sLargeCategoryName
	 * @param lWDA
	 */
	private static void makeWorkListEach(
			Integer year,
			Integer month,
			int iDaysCnt,
			String sSqlBase,
			String sSqlBaseG,
			HaUser hauser,
			WorkDailyAccount wDaDiff,
			String sLargeCategoryName,	// 大分類行の名称「収入」・「支出」・「My貯金」・「差額」・「My貯金から支払」
			List<WorkDailyAccount> lWDA
			) {
   		Calendar calendar = Calendar.getInstance();
   		String sSql = "";
		BigInteger biSumMonthDiff = wDaDiff.getBiSumMonth();
		BigInteger[] biAryDaysDiff = wDaDiff.getBiAryDays();
		String[] sAryDaysDiff = new String[iDaysCnt];
		
   		//合計行
   		WorkDailyAccount wDA = new WorkDailyAccount();
   		
   		if(sLargeCategoryName.equals("収入") || sLargeCategoryName.equals("支出")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND a.actual_type_name = '" + sLargeCategoryName + "' " +
					"   AND r.ideal_deposit_mst_id IS NULL ";
   		} else if(sLargeCategoryName.equals("My貯金")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name = 'My貯金' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND a.actual_type_name = '支出' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL ";
   		}
		BigInteger biSumMonthG = (BigInteger)JPA.em().createNativeQuery(
				sSql).getSingleResult();

		wDA.setsLargeCategory(sLargeCategoryName);
		wDA.setsItem("");
		wDA.setbBudgetFlg(false);
		wDA.setsSumMonth(biSumMonthG==null ? "" : String.format("%1$,3d", biSumMonthG));
		
		//  「収入」・「支出」・「My貯金」の場合
		if(sLargeCategoryName.equals("収入") ||
				sLargeCategoryName.equals("支出") ||
				sLargeCategoryName.equals("My貯金")) {

			//予算有無フラグを立てる
			wDA.setbBudgetFlg(true);

			//「差額」に加算
			if(biSumMonthDiff == null) {
				biSumMonthDiff = new BigInteger("0");
			}
			if(sLargeCategoryName.equals("収入")) {
				if(biSumMonthG != null) {
					biSumMonthDiff = biSumMonthDiff.add(biSumMonthG);
				}
			} else if(sLargeCategoryName.equals("支出") || sLargeCategoryName.equals("My貯金")) {
				if(biSumMonthG != null) {
					biSumMonthDiff = biSumMonthDiff.subtract(biSumMonthG);
				}
			}
			wDaDiff.setBiSumMonth(biSumMonthDiff);
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
						"   AND a.actual_type_name = '" + sLargeCategoryName + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL ";
	   		} else if(sLargeCategoryName.equals("My貯金")) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = 'My貯金' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND a.actual_type_name = '支出' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		}
			biAryDaysG[iDay] = (BigInteger)JPA.em().createNativeQuery(
					sSql).getSingleResult();
			sAryDaysG[iDay] = biAryDaysG[iDay]==null ? "" : String.format("%1$,3d", biAryDaysG[iDay]);
			
			//  「収入」・「支出」・「My貯金」の場合
			if(sLargeCategoryName.equals("収入") ||
					sLargeCategoryName.equals("支出") ||
					sLargeCategoryName.equals("My貯金")) {
				
				//  「差額」に加算
				if(biAryDaysDiff[iDay] == null) {
					biAryDaysDiff[iDay] = new BigInteger("0"); 
				}
				if(sLargeCategoryName.equals("収入")) {
	   				if(biAryDaysG[iDay] != null) {
	   					biAryDaysDiff[iDay] = biAryDaysDiff[iDay].add(biAryDaysG[iDay]); 
	   				}
				} else if(sLargeCategoryName.equals("支出") || sLargeCategoryName.equals("My貯金")) {
	   				if(biAryDaysG[iDay] != null) {
	   					biAryDaysDiff[iDay] = biAryDaysDiff[iDay].subtract(biAryDaysG[iDay]); 
	   				}
					if(sLargeCategoryName.equals("My貯金")) {
						sAryDaysDiff[iDay] = biAryDaysDiff[iDay]==null ? "" : String.format("%1$,3d", biAryDaysDiff[iDay]);
						wDaDiff.setsAryDays(sAryDaysDiff);
					}
				}
				wDaDiff.setBiAryDays(biAryDaysDiff);
			}

		}
		wDA.setsAryDays(sAryDaysG);
		
		lWDA.add(wDA);

		
		if(sLargeCategoryName.equals("収入") || sLargeCategoryName.equals("支出")) {
			//項目ごとのループ
			List<ItemMst> itemMsts = ItemMst.find("ha_user = " + hauser.id + " and actual_type_mst.actual_type_name = '" + sLargeCategoryName + "' order by id").fetch();
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
				wDaItem.setsAryDays(sAryDays);
	
				lWDA.add(wDaItem);
				
			}
		} else if(sLargeCategoryName.equals("My貯金") || sLargeCategoryName.equals("My貯金から支払")) {
			//My貯金ごとのループ
			List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = " + hauser.id).fetch();
			for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
				IdealDepositMst idealDepositMst = itrIdealDeposit.next();
				
				WorkDailyAccount wDaIdealDepo = new WorkDailyAccount();
				
				if(sLargeCategoryName.equals("My貯金")) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND b.balance_type_name = 'My貯金' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL ";
		   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND a.actual_type_name = '支出' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL ";
		   		}
	
				BigInteger biSumMonthMyDp = (BigInteger)JPA.em().createNativeQuery(
						sSql).getSingleResult();
				
				wDaIdealDepo.setsLargeCategory(sLargeCategoryName);
				wDaIdealDepo.setsItem(idealDepositMst.ideal_deposit_name);
				if(sLargeCategoryName.equals("My貯金")) {
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
					if(sLargeCategoryName.equals("My貯金")) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND b.balance_type_name = 'My貯金' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL ";
			   		} else if(sLargeCategoryName.equals("My貯金から支払")) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND a.actual_type_name = '支出' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL ";
			   		}
					biAryDaysMyDp[iDay] = (BigInteger)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					sAryDaysMyDp[iDay] = biAryDaysMyDp[iDay]==null ? "" : String.format("%1$,3d", biAryDaysMyDp[iDay]);
				}
				wDaIdealDepo.setsAryDays(sAryDaysMyDp);
	
				lWDA.add(wDaIdealDepo);
				
			}
		}

	}
	
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
   		
		HaUser hauser  = HaUser.find("byEmail", Security.connected()).first();

   		String sSqlBase = "" +
   				" SELECT SUM(r.amount) FROM Record r " +
				" LEFT JOIN ItemMst i " +
				"   ON r.item_mst_id = i.id " +
				" LEFT JOIN ActualTypeMst a " +
				"   ON i.actual_type_mst_id = a.id " +
				" LEFT JOIN BalanceTypeMst b " +
				"   ON r.balance_type_mst_id = b.id " +
				" WHERE r.ha_user_id = " + hauser.id;
   		String sSqlBaseG = "" +
				"   AND cast(r.payment_date as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD')" +
				"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
   		
   		//「差額」用 変数
   		WorkDailyAccount wDaDiff = new WorkDailyAccount();
		BigInteger biSumMonthDiff = new BigInteger("0");
		BigInteger[] biAryDaysDiff = new BigInteger[iDaysCnt];
		wDaDiff.setBiSumMonth(biSumMonthDiff);
		wDaDiff.setBiAryDays(biAryDaysDiff);
   		
   		
		//「収入」
		makeWorkListEach(year, month, iDaysCnt, sSqlBase, sSqlBaseG, hauser, wDaDiff,
				"収入", lWDA);
		
		//「支出」
		makeWorkListEach(year, month, iDaysCnt, sSqlBase, sSqlBaseG, hauser, wDaDiff,
				"支出", lWDA);
		
		//「My貯金」
		makeWorkListEach(year, month, iDaysCnt, sSqlBase, sSqlBaseG, hauser, wDaDiff,
				"My貯金", lWDA);
		

   		//「差額」
		wDaDiff.setsLargeCategory("差額");
		wDaDiff.setsItem("");
		biSumMonthDiff = wDaDiff.getBiSumMonth();
		wDaDiff.setsSumMonth(biSumMonthDiff==null ? "" : String.format("%1$,3d", biSumMonthDiff));
		
		lWDA.add(wDaDiff);
		
		
		//「My貯金から支払」
		makeWorkListEach(year, month, iDaysCnt, sSqlBase, sSqlBaseG, hauser, wDaDiff,
				"My貯金から支払", lWDA);
		
		
		
		return lWDA;
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
    		form(null, null);
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
	    		
	    	form(year, month);
    	}
	}
}
