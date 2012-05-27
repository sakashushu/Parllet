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
		String[] sAryDaysDiff = new String[iDaysCnt];
   		
   		
   		//「収入」「支出」ループ
   		String[] sInOut = {"収入", "支出"};
   		for(int i = 0; i < 2; i++) {
	   		//合計行
	   		WorkDailyAccount wDA = new WorkDailyAccount();
	   		
			BigInteger biSumMonthG = (BigInteger)JPA.em().createNativeQuery(
					
					sSqlBase +
					sSqlBaseG +
					"   AND a.actual_type_name = '" + sInOut[i] + "' " +
					"   AND r.ideal_deposit_mst_id IS NULL "
					).getSingleResult();
			//  「差額」に加算
   			switch(i) {
   			case 0:
   				if(biSumMonthG != null) {
   					biSumMonthDiff = biSumMonthDiff.add(biSumMonthG);
   				}
   				break;
   			case 1:
   				if(biSumMonthG != null) {
   					biSumMonthDiff = biSumMonthDiff.subtract(biSumMonthG);
   				}
   				break;
   			}
			
			wDA.setsActualType(sInOut[i]);
			wDA.setsItem("");
			wDA.setsSumMonth(biSumMonthG==null ? "" : String.format("%1$,3d", biSumMonthG));
			
			// 日毎
			BigInteger[] biAryDaysG = new BigInteger[iDaysCnt];
			String[] sAryDaysG = new String[iDaysCnt];
			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
				calendar.set(year, month - 1, iDay + 1);
		   		String sSqlBaseD = "" +
						"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				biAryDaysG[iDay] = (BigInteger)JPA.em().createNativeQuery(
						sSqlBase +
						sSqlBaseD +
						"   AND a.actual_type_name = '" + sInOut[i] + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL "
						).getSingleResult();
				sAryDaysG[iDay] = biAryDaysG[iDay]==null ? "" : String.format("%1$,3d", biAryDaysG[iDay]);
				
				//  「差額」に加算
	   			switch(i) {
	   			case 0:
	   				if(biAryDaysG[iDay] != null) {
	   					biAryDaysDiff[iDay] = biAryDaysG[iDay]; 
	   				} else {
	   					biAryDaysDiff[iDay] = new BigInteger("0"); 
	   				}
	   				break;
	   			case 1:
	   				if(biAryDaysG[iDay] != null) {
	   					biAryDaysDiff[iDay] = biAryDaysDiff[iDay].subtract(biAryDaysG[iDay]); 
	   				}
	   				break;
	   			}

			}
			wDA.setsAryDays(sAryDaysG);
			
			lWDA.add(wDA);
	
			//項目ごとのループ
			List<ItemMst> itemMsts = ItemMst.find("ha_user = " + hauser.id + " and actual_type_mst.actual_type_name = '" + sInOut[i] + "' order by id").fetch();
			for(Iterator<ItemMst> itrItem = itemMsts.iterator(); itrItem.hasNext();) {
				ItemMst itemMst = itrItem.next();
				
				WorkDailyAccount wDaItem = new WorkDailyAccount();

				BigInteger biSumMonth = (BigInteger)JPA.em().createNativeQuery(
						sSqlBase +
						sSqlBaseG +
						"   AND i.item_name = '" + itemMst.item_name + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL "
						).getSingleResult();
				
				wDaItem.setsActualType("");
				wDaItem.setsItem(itemMst.item_name);
				wDaItem.setsSumMonth(biSumMonth==null ? "" : String.format("%1$,3d", biSumMonth));

				// 日毎
				BigInteger[] biAryDays = new BigInteger[iDaysCnt];
				String[] sAryDays = new String[iDaysCnt];
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
					calendar.set(year, month - 1, iDay + 1);
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
					biAryDays[iDay] = (BigInteger)JPA.em().createNativeQuery(
							sSqlBase +
							sSqlBaseD +
							"   AND i.item_name = '" + itemMst.item_name + "' " +
							"   AND r.ideal_deposit_mst_id IS NULL "
							).getSingleResult();
					sAryDays[iDay] = biAryDays[iDay]==null ? "" : String.format("%1$,3d", biAryDays[iDay]);
				}
				wDaItem.setsAryDays(sAryDays);

				lWDA.add(wDaItem);
				
			}
   			
   		}
   		
   		
   		//「My貯金」
   		//合計行
   		WorkDailyAccount wDaMyDp = new WorkDailyAccount();
   		
		BigInteger biSumMonthMyDpG = (BigInteger)JPA.em().createNativeQuery(
				sSqlBase +
				sSqlBaseG +
				"   AND b.balance_type_name = 'My貯金預入' " +
				"   AND r.ideal_deposit_mst_id IS NOT NULL "

				).getSingleResult();
		
		//  「差額」に加算
		if(biSumMonthMyDpG != null) {
			biSumMonthDiff = biSumMonthDiff.subtract(biSumMonthMyDpG);
		}

   		wDaMyDp.setsActualType("My貯金");
		wDaMyDp.setsItem("");
		wDaMyDp.setsSumMonth(biSumMonthMyDpG==null ? "" : String.format("%1$,3d", biSumMonthMyDpG));
		
		// 日毎
		BigInteger[] biAryDaysMyDpG = new BigInteger[iDaysCnt];
		String[] sAryDaysMyDpG = new String[iDaysCnt];
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			calendar.set(year, month - 1, iDay + 1);
	   		String sSqlBaseD = "" +
					"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			biAryDaysMyDpG[iDay] = (BigInteger)JPA.em().createNativeQuery(
					sSqlBase +
					sSqlBaseD +
					"   AND b.balance_type_name = 'My貯金預入' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL "
					).getSingleResult();
			sAryDaysMyDpG[iDay] = biAryDaysMyDpG[iDay]==null ? "" : String.format("%1$,3d", biAryDaysMyDpG[iDay]);
			//  「差額」に加算
			sAryDaysDiff[iDay] = "";
			if(biAryDaysMyDpG[iDay] != null) {
				biAryDaysDiff[iDay] = biAryDaysDiff[iDay].subtract(biAryDaysMyDpG[iDay]);
				sAryDaysDiff[iDay] = biAryDaysDiff[iDay]==null ? "" : String.format("%1$,3d", biAryDaysDiff[iDay]);
			}
		}
		wDaMyDp.setsAryDays(sAryDaysMyDpG);
		
		lWDA.add(wDaMyDp);
   		
		
		//My貯金ごとのループ
		List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = " + hauser.id).fetch();
		for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
			IdealDepositMst idealDepositMst = itrIdealDeposit.next();
			
			WorkDailyAccount wDaIdealDepo = new WorkDailyAccount();

			BigInteger biSumMonthMyDp = (BigInteger)JPA.em().createNativeQuery(
					sSqlBase +
					sSqlBaseG +
					"   AND b.balance_type_name = 'My貯金預入' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL "
					).getSingleResult();
			
			wDaIdealDepo.setsActualType("");
			wDaIdealDepo.setsItem(idealDepositMst.ideal_deposit_name);
			wDaIdealDepo.setsSumMonth(biSumMonthMyDp==null ? "" : String.format("%1$,3d", biSumMonthMyDp));

			// 日毎
			BigInteger[] biAryDaysMyDp = new BigInteger[iDaysCnt];
			String[] sAryDaysMyDp = new String[iDaysCnt];
			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
				calendar.set(year, month - 1, iDay + 1);
		   		String sSqlBaseD = "" +
						"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				biAryDaysMyDp[iDay] = (BigInteger)JPA.em().createNativeQuery(
						sSqlBase +
						sSqlBaseD +
						"   AND b.balance_type_name = 'My貯金預入' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL "
						).getSingleResult();
				sAryDaysMyDp[iDay] = biAryDaysMyDp[iDay]==null ? "" : String.format("%1$,3d", biAryDaysMyDp[iDay]);
			}
			wDaIdealDepo.setsAryDays(sAryDaysMyDp);

			lWDA.add(wDaIdealDepo);
			
		}

		
   		//「差額」
   		
		wDaDiff.setsActualType("差額");
		wDaDiff.setsItem("");
		wDaDiff.setsSumMonth(biSumMonthDiff==null ? "" : String.format("%1$,3d", biSumMonthDiff));
		wDaDiff.setsAryDays(sAryDaysDiff);
		
		lWDA.add(wDaDiff);
		
		
		//「My貯金から支払」
   		//合計行
   		WorkDailyAccount wDaMyDpOut = new WorkDailyAccount();
   		
		BigInteger biSumMonthMyDpOutG = (BigInteger)JPA.em().createNativeQuery(
				
				sSqlBase +
				sSqlBaseG +
				"   AND a.actual_type_name = '支出' " +
				"   AND r.ideal_deposit_mst_id IS NOT NULL "
				).getSingleResult();
		
		wDaMyDpOut.setsActualType("My貯金から支払");
		wDaMyDpOut.setsItem("");
		wDaMyDpOut.setsSumMonth(biSumMonthMyDpOutG==null ? "" : String.format("%1$,3d", biSumMonthMyDpOutG));
		
		// 日毎
		BigInteger[] biAryDaysMyDpOutG = new BigInteger[iDaysCnt];
		String[] sAryDaysMyDpOutG = new String[iDaysCnt];
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			calendar.set(year, month - 1, iDay + 1);
	   		String sSqlBaseD = "" +
					"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			biAryDaysMyDpOutG[iDay] = (BigInteger)JPA.em().createNativeQuery(
					sSqlBase +
					sSqlBaseD +
					"   AND a.actual_type_name = '支出' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL "
					).getSingleResult();
			sAryDaysMyDpOutG[iDay] = biAryDaysMyDpOutG[iDay]==null ? "" : String.format("%1$,3d", biAryDaysMyDpOutG[iDay]);
			
		}
		wDaMyDpOut.setsAryDays(sAryDaysMyDpOutG);
		
		lWDA.add(wDaMyDpOut);
		
		//My貯金ごとのループ
		for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
			IdealDepositMst idealDepositMst = itrIdealDeposit.next();
			
			WorkDailyAccount wDaMyDpOutIdealDepo = new WorkDailyAccount();

			BigInteger biSumMonthMyDpOut = (BigInteger)JPA.em().createNativeQuery(
					sSqlBase +
					sSqlBaseG +
					"   AND b.balance_type_name = '支出' " +
					"   AND r.ideal_deposit_mst_id IS NOT NULL "
					).getSingleResult();
			
			wDaMyDpOutIdealDepo.setsActualType("");
			wDaMyDpOutIdealDepo.setsItem(idealDepositMst.ideal_deposit_name);
			wDaMyDpOutIdealDepo.setsSumMonth(biSumMonthMyDpOut==null ? "" : String.format("%1$,3d", biSumMonthMyDpOut));

			// 日毎
			BigInteger[] biAryDaysMyDpOut = new BigInteger[iDaysCnt];
			String[] sAryDaysMyDpOut = new String[iDaysCnt];
			for(int iDay = 0; iDay < iDaysCnt; iDay++) {
				calendar.set(year, month - 1, iDay + 1);
		   		String sSqlBaseD = "" +
						"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				biAryDaysMyDpOut[iDay] = (BigInteger)JPA.em().createNativeQuery(
						sSqlBase +
						sSqlBaseD +
						"   AND b.balance_type_name = '支出' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL "
						).getSingleResult();
				sAryDaysMyDpOut[iDay] = biAryDaysMyDpOut[iDay]==null ? "" : String.format("%1$,3d", biAryDaysMyDpOut[iDay]);
			}
			wDaMyDpOutIdealDepo.setsAryDays(sAryDaysMyDpOut);

			lWDA.add(wDaMyDpOutIdealDepo);
			
		}
		
		
		return lWDA;
	}
	
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
