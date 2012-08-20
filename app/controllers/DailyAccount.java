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
import models.WorkDaToDl;
import models.WorkDailyAccount;

import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class DailyAccount extends Controller {

	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("haUser", haUser);
		}
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
			String sBasisDate,
			String sTableType
			) {

   		Calendar calendar = Calendar.getInstance();
   		
   		//単純に呼ばれた時（初回等）は、今日を表示
		if(sBasisDate==null) {
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

		int iDaysCnt = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		
		if(sTableType.equals(VIEWS_DAILY_ACCOUNT)) {
			iDaysCnt = 3;
			calendar.add(Calendar.DATE, -1);
		} else if(sTableType.equals(VIEWS_BALANCE_TABLE)) {
			iDaysCnt = 1;
		}
		
   		
		//日計表のヘッダーの日付の配列
		String[] sAryDays = new String[iDaysCnt];
		Date dStartDay = calendar.getTime();
		SimpleDateFormat sdf1 = new SimpleDateFormat("M/d");
		
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			sAryDays[iDay] = sdf1.format(calendar.getTime());
			calendar.add(Calendar.DATE, 1);
		}
		

		//日計表の行に相当するリストの作成
		List<WorkDailyAccount> lWDA = makeWorkList(year, month, dStartDay, iDaysCnt, sTableType);
   		
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
    			dailyAccount(e_basis_date);
    		} else if(sTableType.equals(VIEWS_BALANCE_TABLE)) {
    			balanceTable(e_basis_date);
    		}
    	}
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
		
		//カンマ区切りの数値文字列を数値型に変換するNumberFormatクラスのインスタンスを取得する
		NumberFormat nf = NumberFormat.getInstance();

		Iterator<String> sELargeCategory = e_large_category.iterator();
		Iterator<String> sEItem = e_item.iterator();
   		Iterator<String> sEBudgetAmount = e_budget_amount.iterator();
		for (Long lId : e_budget_id) {
			
			String sELargeCategoryVal = sELargeCategory.next();
			String sEItemVal = sEItem.next();
			String sEBudgetAmountVal = sEBudgetAmount.next();
			
			Budget budget;
				
			//予算が空白にされた時
			if(sEBudgetAmountVal.equals("")) {
				//既存レコードがある場合レコード削除
				if(lId!=0L) {
					budget = Budget.findById(lId);
					budget.delete();
				}
				
			//予算が入力された時
			} else {
				try {
					//数値文字列をNumber型のオブジェクトに変換する
					Number nEBudgetAmount = nf.parse(sEBudgetAmountVal);
					//Number型のオブジェクトからInteger値を取得する
					Integer iEBudgetAmount = nEBudgetAmount.intValue();
					
					//既存レコードがある場合は更新
					if(lId!=0L) {
						budget = Budget.findById(lId);
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
					
					//既存レコードが無い場合は新規登録
					} else {
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
						budget = new Budget(
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
					}
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
    	dailyAccount(bg_basis_date);
	}
	
	
	/**
	 * 日計表・残高表の行に相当するリストの作成
	 * @param year
	 * @param month
	 * @param dStartDay
	 * @param iDaysCnt
	 * @param sTableType
	 * @return
	 */
	private static List<WorkDailyAccount> makeWorkList(
			Integer year,
			Integer month,
			Date dStartDay,
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
   		
		HaUser haUser = (HaUser)renderArgs.get("haUser");

		
		if(sTableType.equals(VIEWS_DAILY_ACCOUNT)) {
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
			
		} else if(sTableType.equals(VIEWS_BALANCE_TABLE)) {
			//「実残高」
			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					REMAINDER_TYPE_REAL, lWDA);
			
			//「My貯金残高」
			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					REMAINDER_TYPE_IDEAL_DEPOSIT, lWDA);
  	  	
  	  	
			//「My貯金してないお金」
			makeWorkListEach(year, month, dStartDay, iDaysCnt, haUser, sFirstDay, sNextFirst,
					REMAINDER_TYPE_NOT_IDEAL_DEPOSIT, lWDA);
			
		}

		return lWDA;
	}
	
	
	/**
	 * 日計表の行に相当するリストの作成（「収入」・「支出」・「My貯金預入」・「差額」・「My貯金から支払」毎に作成する）
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
			List<WorkDailyAccount> lWDA
			) {
		//カンマ区切りの数値文字列を数値型に変換するNumberFormatクラスのインスタンスを取得する
		NumberFormat nf = NumberFormat.getInstance();

		//数値をカンマ区切りの数値文字列に変換するフォーマットを定義する
		DecimalFormat df = new DecimalFormat("###,###");
		
   		Calendar calendar = Calendar.getInstance();
   		String sSql = "";
		
   		//合計行
   		WorkDailyAccount wDA = new WorkDailyAccount();

   		String sSqlBase = "";
   		String sSqlBaseG = "";
   		
   		//「収入」・「支出」・「My貯金預入」・「My貯金から支払」
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
					" WHERE r.ha_user_id = " + haUser.id +
					"";
	   		sSqlBaseG = "" +
					"   AND cast(r.payment_date as date) >= to_date('" + sFirstDay + "', 'YYYYMMDD')" +
					"   AND cast(r.payment_date as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
	   		
	   	//「実残高」
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				//「収入」は加算
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IN + "' THEN r.amount" +
	   				//「支出」は減算
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
					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
	   		
	   	//「My貯金残高」
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN -r.amount" +
	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NOT NULL) THEN r.amount" +
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
					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
	   		
	   	//「My貯金してないお金」
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
	   		sSqlBase = "" +
	   				" SELECT COALESCE(SUM(" +
	   				"   CASE " +
	   				//「収入」で「取扱(My貯金)」未選択は加算
	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_IN + "' AND" +
	   				"           r.ideal_deposit_mst_id IS NULL) THEN r.amount " +
	   				//「支出」で「取扱(My貯金)」未選択は減算
	   				"     WHEN (b.balance_type_name = '" + BALANCE_TYPE_OUT + "' AND " +
	   				"           r.ideal_deposit_mst_id IS NULL) THEN -r.amount " +
	   				//「My貯金預入」は減算
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' THEN -r.amount" +
	   				//「My貯金引出」は加算
	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "' THEN r.amount" +
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
					" WHERE r.ha_user_id = " + haUser.id +
					""
					;
	   		sSqlBaseG = "" +
					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) < to_date('" + sNextFirst + "', 'YYYYMMDD')";
   		}
   		

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
   		//「実残高」
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') ";
   		//「My貯金残高」
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
	   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
	   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
	   				"        )";
   		//「My貯金してないお金」
   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
   			sSql = sSqlBase + sSqlBaseG +
					"   AND ((b.balance_type_name in('" + BALANCE_TYPE_IN + "','" + BALANCE_TYPE_OUT + "') AND " +
					"         r.ideal_deposit_mst_id IS NULL) OR " +
   				    "        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "') " +
	   				"        )";
   		}
   		
		//  「収入」・「支出」・「My貯金預入」・「My貯金から支払」の場合のみ月計をセット
   		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_OUT) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN) ||
   				sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
			BigInteger biSumMonthG = (BigInteger)JPA.em().createNativeQuery(
					sSql).getSingleResult();
			long lSumMonthG = biSumMonthG == null ? 0L : biSumMonthG.longValue();
			wDA.setLSumMonth(lSumMonthG);
			wDA.setsSumMonth(String.format("%1$,3d", lSumMonthG));
   		}
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
		List<WorkDaToDl> lstWdtdG = new ArrayList<WorkDaToDl>();
		for(int iDay = 0; iDay < iDaysCnt; iDay++) {
	   		String sSqlBaseD = "" +
					"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
	   		//「収入」・「支出」
	   		if(sLargeCategoryName.equals(BALANCE_TYPE_IN) || sLargeCategoryName.equals(BALANCE_TYPE_OUT)) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + sLargeCategoryName + "' " +
						"   AND r.ideal_deposit_mst_id IS NULL ";
	   		//「My貯金預入」
	   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		//「My貯金から支払」
	   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
						"   AND r.ideal_deposit_mst_id IS NOT NULL ";
	   		//「実残高」
	   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
				//初日のみデータ集約。以降は加算
				if(iDay == 0) {
					sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				} else {
					sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				}
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') ";
	   		//「My貯金残高」
	   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
				//初日のみデータ集約。以降は加算
				if(iDay == 0) {
		   			sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				} else {
		   			sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				}
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
		   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
		   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
		   				"        )";
	   		//「My貯金してないお金」
	   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) {
				//初日のみデータ集約。以降は加算
				if(iDay == 0) {
		   			sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				} else {
		   			sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
				}
	   			sSql = sSqlBase + sSqlBaseD +
						"   AND ((b.balance_type_name in('" + BALANCE_TYPE_IN + "','" + BALANCE_TYPE_OUT + "') AND " +
						"         r.ideal_deposit_mst_id IS NULL) OR " +
   						"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "') " +
	   	  				"        )";
	   		}
	   		BigInteger biAryDaysG = (BigInteger)JPA.em().createNativeQuery(
					sSql).getSingleResult();
			lAryDaysG[iDay] = biAryDaysG == null ? 0L : biAryDaysG.longValue();
			//「実残高」・「My貯金残高」・「My貯金してないお金」は初日のみデータ集約。以降は加算
			if((sLargeCategoryName.equals(REMAINDER_TYPE_REAL) ||
					sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT) ||
					sLargeCategoryName.equals(REMAINDER_TYPE_NOT_IDEAL_DEPOSIT)) &&
					iDay != 0) {
				lAryDaysG[iDay] = lAryDaysG[iDay-1] + (biAryDaysG == null ? 0L : biAryDaysG.longValue());
			}
			sAryDaysG[iDay] = df.format(lAryDaysG[iDay]);
			
			WorkDaToDl workDaToDl = new WorkDaToDl();
			long lAmount = biAryDaysG == null ? 0L : biAryDaysG.longValue();
			workDaToDl.setlAmount(lAmount);
			workDaToDl.setsAmount(df.format(lAmount));
			String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
			workDaToDl.setsPaymentDateFr(sDate);
			workDaToDl.setsPaymentDateTo(sDate);
			// 「収入」・「支出」・「My貯金預入」
			if(sLargeCategoryName.equals(BALANCE_TYPE_IN) ||
					sLargeCategoryName.equals(BALANCE_TYPE_OUT) ||
					sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
				workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
				
			// 「My貯金から支払」
			} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
				workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT)).first()).id);
				workDaToDl.setlIdealDepositId((long) -2);
			}
			lstWdtdG.add(workDaToDl);

			calendar.add(Calendar.DATE, 1);
		}
		wDA.setLAryDays(lAryDaysG);
		wDA.setsAryDays(sAryDaysG);
		
		wDA.setLstWdtd(lstWdtdG);
		
		lWDA.add(wDA);

		
		//「収入」・「支出」
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
				wDaItem.setLSumMonth(lSumMonth);
				wDaItem.setsSumMonth(String.format("%1$,3d", lSumMonth));


				//ある時だけ予算をセット
				Budget budget = Budget.find("ha_user = " + haUser.id + "" +
						" and year = " + year + "" +
						" and month = " + month + "" +
						" and item_mst = " + itemMst.id
						).first();
				if(budget!=null) {
					String sBudgetAmount = String.format("%1$,3d", budget.amount);
					
					wDaItem.setlBudgetId(budget.id);
					wDaItem.setsBudgetAmount(sBudgetAmount);
					
					//大分類行に加算
					if(!(wDA.getsBudgetAmount()==null || wDA.getsBudgetAmount().equals(""))) {
						Number nEBudgetAmount;
						try {
							nEBudgetAmount = nf.parse(wDA.getsBudgetAmount());
							sBudgetAmount = String.format("%1$,3d", nEBudgetAmount.intValue() + budget.amount);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					wDA.setsBudgetAmount(sBudgetAmount);
				}
				
				
				// 日毎
				long[] lAryDays = new long[iDaysCnt];
				String[] sAryDays = new String[iDaysCnt];
				calendar.setTime(dStartDay);
				List<WorkDaToDl> lstWdtd = new ArrayList<WorkDaToDl>();
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
					BigInteger biAryDays = (BigInteger)JPA.em().createNativeQuery(
							sSqlBase + sSqlBaseD +
							"   AND i.item_name = '" + itemMst.item_name + "' " +
							"   AND r.ideal_deposit_mst_id IS NULL "
							).getSingleResult();
					lAryDays[iDay] = biAryDays == null ? 0L : biAryDays.longValue();
					sAryDays[iDay] = df.format(lAryDays[iDay]);

					WorkDaToDl workDaToDl = new WorkDaToDl();
					long lAmount = biAryDays == null ? 0L : biAryDays.longValue();
					workDaToDl.setlAmount(lAmount);
					workDaToDl.setsAmount(df.format(lAmount));
					String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
					workDaToDl.setsPaymentDateFr(sDate);
					workDaToDl.setsPaymentDateTo(sDate);
					workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
					workDaToDl.setiItemId(((ItemMst)(ItemMst.find("byItem_name", itemMst.item_name)).first()).id);
					lstWdtd.add(workDaToDl);
					
					calendar.add(Calendar.DATE, 1);
				}
				wDaItem.setLAryDays(lAryDays);
				wDaItem.setsAryDays(sAryDays);
	
				wDaItem.setLstWdtd(lstWdtd);
		
				lWDA.add(wDaItem);
				
			}
		//「My貯金預入」・「My貯金から支払」・「My貯金残高」
		} else if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN) ||
				sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT) ||
				sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
			//My貯金ごとのループ
			List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = " + haUser.id).fetch();
			for(Iterator<IdealDepositMst> itrIdealDeposit = idealDepositMsts.iterator(); itrIdealDeposit.hasNext();) {
				IdealDepositMst idealDepositMst = itrIdealDeposit.next();
				
				WorkDailyAccount wDaIdealDepo = new WorkDailyAccount();
				
				//残高表示フラグ
				boolean bRemainderDispFlg = true;
				
				//「My貯金預入」
				if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   			
		   		//「My貯金から支払」
		   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
							"   AND r.ideal_deposit_mst_id IS NOT NULL " +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   			
		   		//「My貯金残高」
		   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
		   			sSql = sSqlBase + sSqlBaseG +
							"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
			   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
			   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
			   				"        )" +
							"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
		   			
					bRemainderDispFlg = false;
					if(idealDepositMst.zero_hidden==false)
						bRemainderDispFlg = true;
		   		}
	
				//  「My貯金預入」・「My貯金から支払」の場合のみ月計をセット
		   		if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN) ||
		   				sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
					BigInteger biSumMonthMyDp = (BigInteger)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					long lSumMonthMyDp = biSumMonthMyDp == null ? 0L : biSumMonthMyDp.longValue();
					wDaIdealDepo.setsSumMonth(String.format("%1$,3d", lSumMonthMyDp));
		   		}
				
				wDaIdealDepo.setsLargeCategory(sLargeCategoryName);
				wDaIdealDepo.setsItem(idealDepositMst.ideal_deposit_name);
				if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
					wDaIdealDepo.setbBudgetFlg(true);
				} else {
					wDaIdealDepo.setbBudgetFlg(false);
				}

				
				//「My貯金預入」なら予算がある時だけセット
				if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
					Budget budget = Budget.find("ha_user = " + haUser.id + "" +
							" and year = " + year + "" +
							" and month = " + month + "" +
							" and ideal_deposit_mst = " + idealDepositMst.id
							).first();
					if(budget!=null) {
						String sBudgetAmount = String.format("%1$,3d", budget.amount);

						wDaIdealDepo.setlBudgetId(budget.id);
						wDaIdealDepo.setsBudgetAmount(sBudgetAmount);
						
						//大分類行に加算
						if(!(wDA.getsBudgetAmount()==null || wDA.getsBudgetAmount().equals(""))) {
							Number nEBudgetAmount;
							try {
								nEBudgetAmount = nf.parse(wDA.getsBudgetAmount());
								sBudgetAmount = String.format("%1$,3d", nEBudgetAmount.intValue() + budget.amount);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						wDA.setsBudgetAmount(sBudgetAmount);
					}
				}
				
				
				// 日毎
				long[] lAryDaysMyDp = new long[iDaysCnt];
				String[] sAryDaysMyDp = new String[iDaysCnt];
				calendar.setTime(dStartDay);
				List<WorkDaToDl> lstWdtd = new ArrayList<WorkDaToDl>();
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			   		String sSqlBaseD = "" +
							"   AND cast(r.payment_date as date) = to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   		
			   		//「My貯金預入」
					if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND b.balance_type_name = '" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   			
			   		//「My貯金から支払」
			   		} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND b.balance_type_name = '" + BALANCE_TYPE_OUT + "' " +
								"   AND r.ideal_deposit_mst_id IS NOT NULL " +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   			
			   		//「My貯金残高」
			   		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT)) {
			   			sSqlBaseD = "" +
			   					"   AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   			sSql = sSqlBase + sSqlBaseD +
								"   AND ((b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "') AND " +
				   				"         r.ideal_deposit_mst_id IS NOT NULL) OR " +
				   				"        b.balance_type_name in('" + BALANCE_TYPE_IDEAL_DEPOSIT_IN + "','" + BALANCE_TYPE_IDEAL_DEPOSIT_OUT + "')" +
				   				"        )" +
								"   AND id.ideal_deposit_name = '" + idealDepositMst.ideal_deposit_name + "'";
			   		}
					BigInteger biAryDaysMyDp = (BigInteger)JPA.em().createNativeQuery(
							sSql).getSingleResult();
					lAryDaysMyDp[iDay] = biAryDaysMyDp == null ? 0L : biAryDaysMyDp.longValue();
					if(sLargeCategoryName.equals(REMAINDER_TYPE_IDEAL_DEPOSIT) &&
							lAryDaysMyDp[iDay]!=0L) {
						bRemainderDispFlg = true;
					}
					sAryDaysMyDp[iDay] = df.format(lAryDaysMyDp[iDay]);
					
					WorkDaToDl workDaToDl = new WorkDaToDl();
					long lAmount = biAryDaysMyDp == null ? 0L : biAryDaysMyDp.longValue();
					workDaToDl.setlAmount(lAmount);
					workDaToDl.setsAmount(df.format(lAmount));
					String sDate = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
					workDaToDl.setsPaymentDateFr(sDate);
					workDaToDl.setsPaymentDateTo(sDate);
					// 「My貯金預入」
					if(sLargeCategoryName.equals(BALANCE_TYPE_IDEAL_DEPOSIT_IN)) {
						workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", sLargeCategoryName)).first()).id);
						
					// 「My貯金から支払」
					} else if(sLargeCategoryName.equals(BALANCE_TYPE_OUT_IDEAL_DEPOSIT)) {
						workDaToDl.setlBalanceTypeId(((BalanceTypeMst)(BalanceTypeMst.find("byBalance_type_name", BALANCE_TYPE_OUT)).first()).id);
						workDaToDl.setlIdealDepositId(((IdealDepositMst)(IdealDepositMst.find("byIdeal_deposit_name", idealDepositMst.ideal_deposit_name)).first()).id);
					}
					lstWdtd.add(workDaToDl);

					calendar.add(Calendar.DATE, 1);
				}
				//残高有りか、0でも表示設定の場合、列を作成する
				if(bRemainderDispFlg) {
					wDaIdealDepo.setLAryDays(lAryDaysMyDp);
					wDaIdealDepo.setsAryDays(sAryDaysMyDp);
		
					wDaIdealDepo.setLstWdtd(lstWdtd);
					
					lWDA.add(wDaIdealDepo);
				}
				
			}
		//「実残高」
		} else if(sLargeCategoryName.equals(REMAINDER_TYPE_REAL)) {
			//取扱(実際)ごとのループ（クレジットカードは除いて、引落口座に集約）
			List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = " + haUser.id + " and handling_type_mst.handling_type_name <> '" + HANDLING_TYPE_CRECA + "'").fetch();
			for(HandlingMst handlingMst : handlingMsts) {
				WorkDailyAccount wDaHandling = new WorkDailyAccount();
				
				//残高表示フラグ
				boolean bRemainderDispFlg = false;
				if(handlingMst.zero_hidden==false)
					bRemainderDispFlg = true;
				
    	   		sSqlBase = "" +
    	   				" SELECT COALESCE(SUM(" +
    	   				"   CASE " +
    	   				//    「収入」は加算
    	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_IN + "' THEN r.amount" +
    	   				//    「支出」は減算
    	   				"     WHEN b.balance_type_name = '" + BALANCE_TYPE_OUT + "' THEN -r.amount" +
    	   				"     ELSE" +
    	   				"       CASE " +
    	   				//        現金の場合
    	   				"         WHEN '" + handlingMst.handling_name + "' = '" + HANDLING_TYPE_CASH + "' THEN " +
    	   				"           CASE " +
    	   				//            「口座引出」は加算
    	   				"             WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_OUT + "' THEN r.amount" +
    	   				//            「口座預入」は減算
    	   				"             WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_IN + "' THEN -r.amount" +
    	   				"           END" +
    	   				//        現金以外（口座・電子マネー）の場合
    	   				"         ELSE" +
    	   				"           CASE " +
    	   				//            「口座引出」は減算
    	   				"             WHEN b.balance_type_name = '" + BALANCE_TYPE_BANK_OUT + "' THEN -r.amount" +
    	   				//            「口座預入」は加算
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
						"   AND b.balance_type_name in('" + BALANCE_TYPE_OUT + "','" + BALANCE_TYPE_IN + "','" + BALANCE_TYPE_BANK_OUT + "','" + BALANCE_TYPE_BANK_IN + "') " +
						"   AND (" +
						//       現金の場合
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
						"        (    '" + handlingMst.handling_name + "' <> '" + HANDLING_TYPE_CASH + "'" +
						"         AND '" + handlingMst.handling_name + "' in(h.handling_name, hb.handling_name)" +
						"         )" +
						"        )";
	   			sSql = sSqlBase + sSqlBaseG;
	
//				BigInteger biSumMonthRlBal = (BigInteger)JPA.em().createNativeQuery(
//						sSql).getSingleResult();
//				long lSumMonthRlBal = biSumMonthRlBal == null ? 0L : biSumMonthRlBal.longValue();
//				if(lSumMonthRlBal!=0L) {
//					bRemainderDispFlg = true;
//				}
				
				wDaHandling.setsLargeCategory(sLargeCategoryName);
				wDaHandling.setsItem(handlingMst.handling_name);
				wDaHandling.setbBudgetFlg(false);
//				wDaHandling.setsSumMonth(lSumMonthRlBal==null ? "" : String.format("%1$,3d", lSumMonthRlBal));
//				wDaHandling.setsSumMonth(String.format("%1$,3d", lSumMonthRlBal));
	
				// 日毎
				long[] lAryDaysRlBal = new long[iDaysCnt];
				String[] sAryDaysRlBal = new String[iDaysCnt];
				calendar.setTime(dStartDay);
				for(int iDay = 0; iDay < iDaysCnt; iDay++) {
			   		String sSqlBaseD = "";
					//初日のみデータ集約。以降は加算
					if(iDay == 0) {
				   		sSqlBaseD = " AND cast((CASE WHEN r.debit_date IS NULL THEN r.payment_date ELSE r.debit_date END) as date) <= to_date('" + String.format("%1$tY%1$tm%1$td", calendar.getTime()) + "', 'YYYYMMDD')";
			   			sSql = sSqlBase + sSqlBaseD;
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
					if(lAryDaysRlBal[iDay]!=0L) {
						bRemainderDispFlg = true;
					}
					sAryDaysRlBal[iDay] = df.format(lAryDaysRlBal[iDay]);
					
					calendar.add(Calendar.DATE, 1);
				}
				//残高有りか、0でも表示設定の場合、列を作成する
				if(bRemainderDispFlg) {
					wDaHandling.setLAryDays(lAryDaysRlBal);
					wDaHandling.setsAryDays(sAryDaysRlBal);
		
					lWDA.add(wDaHandling);
				}
			}
		}
	}
}
