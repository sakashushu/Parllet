package controllers;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.BalanceTypeMst;
import models.HaUser;
import models.HandlingMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.Record;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class RecordEdit extends Controller {
	
	@Before
	static void setConnectedUser() {
		if (Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("haUser", haUser);
		}
	}
	
	/**
	 * 家計簿入力
	 * @param id
	 * @param df_payment_date
	 * @param df_balance_type_id
	 * @param df_ideal_deposit_id
	 * @param df_item_id
	 * @param df_debit_date
	 * @param calledFrom
	 */
	public static void recordEdit(
			Long id,
			String calledFrom,				/* 呼び出し元 */
			String df_payment_date,			/* 初期支払日 */
			Long df_balance_type_id,  		/* 初期収支種類ID */
			Long df_ideal_deposit_id,		/* 初期取扱(My貯金)ID */
			Long df_item_id,				/* 初期項目ID */
			String df_debit_date
			) {
		//編集
		if (id != null) {
//			Record record = Record.findById(id);
//			render(record, calledFrom);
			recordUpd(id, calledFrom);
		}
		//追加
//		render(df_payment_date, df_balance_type_id, df_ideal_deposit_id, df_item_id, df_debit_date);
		recordIns(df_payment_date, df_balance_type_id, df_ideal_deposit_id, df_item_id, df_debit_date);
	}
	
	public static void recordIns(
			String df_payment_date,			/* 初期支払日 */
			Long df_balance_type_id,  		/* 初期収支種類ID */
			Long df_ideal_deposit_id,		/* 初期取扱(My貯金)ID */
			Long df_item_id,				/* 初期項目ID */
			String df_debit_date
			) {
		render("@recordEdit", df_payment_date, df_balance_type_id, df_ideal_deposit_id, df_item_id, df_debit_date);
	}
	
	public static void recordUpd(
			Long id,
			String calledFrom	/* 呼び出し元 */
			) {
		Record record = Record.findById(id);
		render("@recordEdit", record, calledFrom);
	}
	
	
	/**
	 * 収支レコードの保存
	 * @param id
	 * @param payment_date
	 * @param balance_type_mst
	 * @param handling_mst
	 * @param ideal_deposit_mst
	 * @param item_mst
	 * @param amount
	 * @param debit_date
	 * @param content
	 * @param store
	 * @param remarks
	 * @param secret_remarks
	 * @param secret_rec_flg
	 * @param calledFrom
	 */
	public static void save_rec(
			Long id,
			String payment_date,
			Long balance_type_mst,
			Long handling_mst,
			Long ideal_deposit_mst,
			Long item_mst,
			String amount,
			String debit_date,
			String content,
			String store,
			String remarks,
			String secret_remarks,
			Boolean secret_rec_flg,
			String calledFrom				/* 呼び出し元 */
			) {
		checkAuthenticity();
		
		Record rec = null;
		Date paymentDate = null;
		if (payment_date!=null && !payment_date.equals("")) {  // 「payment_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
			try {
				paymentDate = DateFormat.getDateTimeInstance().parse(payment_date+":00");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//HaUser haUser = HaUser.find("byEmail", Security.connected()).first();
		HaUser haUser  = (HaUser)renderArgs.get("haUser");
		BalanceTypeMst balanceTypeMst = BalanceTypeMst.findById(balance_type_mst);
		ItemMst itemMst = null;
		if (item_mst!=null) {
			itemMst = ItemMst.findById(item_mst); 
		}
		HandlingMst handlingMst = null;
		if (handling_mst!=null) {
			handlingMst = HandlingMst.findById(handling_mst);
		}
		Integer intAmount = null;
		//カンマ区切りの数値文字列を数値型に変換するNumberFormatクラスのインスタンスを取得する
		NumberFormat nf = NumberFormat.getInstance();
		if (amount!=null && !amount.equals("")) {
			//数値文字列をNumber型のオブジェクトに変換する
			Number numAmount;
			try {
				numAmount = nf.parse(amount);
				//Number型のオブジェクトからInteger値を取得する
				intAmount = numAmount.intValue();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Date debitDate = null;
		if (debit_date!=null && !debit_date.equals("")) {  // 「debit_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
			try {
				debitDate = DateFormat.getDateInstance().parse(debit_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IdealDepositMst idealDepositMst = null;
		if (ideal_deposit_mst!=null) {
			idealDepositMst = IdealDepositMst.findById(ideal_deposit_mst);
		}
		if (id==null) {
			// 収支データの作成
			rec = new Record(
					haUser,
					paymentDate,
					balanceTypeMst,
					handlingMst,
					idealDepositMst,
					itemMst,
					null,
					intAmount,
					0,
					0,
					debitDate,
					content,
					store,
					remarks,
					secret_remarks,
					secret_rec_flg==null ? false : (secret_rec_flg==true ? true : false),
					null
			);
		} else {
			// 収支データの読み出し
			rec = Record.findById(id);
			// 編集
			rec.payment_date = paymentDate;
			rec.balance_type_mst = balanceTypeMst;
			rec.handling_mst = handlingMst;
			rec.ideal_deposit_mst = idealDepositMst;
			rec.item_mst = itemMst;
			rec.amount = intAmount;
			rec.debit_date = debitDate;
			rec.content = content;
			rec.store = store;
			rec.remarks = remarks;
			rec.secret_remarks = secret_remarks;
			rec.secret_rec_flg = secret_rec_flg==null ? false : (secret_rec_flg==true ? true : false);
		}
		// Validate
		validation.valid(rec);
		if (validation.hasErrors()) {
			render("@recordEdit", rec);
		}
		// 保存
		rec.save();
		
		RecordEdit reEd = new RecordEdit();
		
		// 明細表の絞り込み条件を、作成データにやんわり合わせる
		reEd.setSessionDetailList(rec, calledFrom);
		
		// 呼び出し元画面に戻る
		reEd.returnToCelledFrom(calledFrom);
	}

	/**
	 * レコード削除
	 * @param id
	 * @param calledFrom
	 */
	public static void del_rec(
			Long id,
			String calledFrom				/* 呼び出し元 */
			) {
		// 取扱データの読み出し
		Record record = Record.findById(id);
		// 削除
		record.delete();
		
		RecordEdit reEd = new RecordEdit();
		reEd.returnToCelledFrom(calledFrom);
	}
	
	/**
	 * 呼び出し元画面に戻る
	 * @param calledFrom
	 */
	private void returnToCelledFrom(
			String calledFrom				/* 呼び出し元 */
			) {
		if (calledFrom==null || calledFrom.equals("") || calledFrom.equals("dl_balance"))
			DetailList.dl_balance(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		if (calledFrom.equals("dl_remainderBank"))
			DetailList.dl_remainderBank(null, null, null, null, null, null);
		if (calledFrom.equals("dl_remainderIdeal"))
			DetailList.dl_remainderIdeal(null, null, null, null, null, null);
		
		DetailList.dl_balance(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	
	/**
	 * 明細表の絞り込み条件を、作成データにやんわり合わせる
	 * @param h_payment_date
	 */
	private void setSessionDetailList(
			Record rec,
			String calledFrom				/* 呼び出し元 */
			) {
		//収支明細に戻す場合
		if (calledFrom==null || calledFrom.equals("") || calledFrom.equals("dl_balance"))
			setSessionDlRtnBal(rec);
		
		//残高明細（口座系）に戻す場合
		if (calledFrom.equals("dl_remainderBank"))
			setSessionDlRtnRemBk(rec);
		
		//残高明細（My貯金）に戻す場合
		if (calledFrom.equals("dl_remainderIdeal"))
			setSessionDlRtnRemId(rec);
	}
	
	/**
	 * 明細表の絞り込み条件を、作成データにやんわり合わせる（収支明細に戻す場合）
	 * @param rec
	 */
	private void setSessionDlRtnBal(
			Record rec
			) {
		//セッションに絞込条件が入っている時
		if ((session.get(Common.FLTR_DL_BAL_EXST_FLG) != null) &&
				(session.get(Common.FLTR_DL_BAL_EXST_FLG).equals("true"))) {
			String strBalanceTypeId = session.get(Common.FLTR_DL_BAL_BTYPE_ID);
			String strHandlingId = session.get(Common.FLTR_DL_BAL_HDLG_ID);
			String strIdealDepositId = session.get(Common.FLTR_DL_BAL_IDEPO_ID);
			String strItemId = session.get(Common.FLTR_DL_BAL_ITEM_ID);

			//購入日
			setSessionDlDate(String.format("%1$tY/%1$tm/%1$td", rec.payment_date), Common.FLTR_DL_BAL_PDTE_FR, Common.FLTR_DL_BAL_PDTE_TO);
			
			//収支種類
			if (!strBalanceTypeId.equals("") &&
					rec.balance_type_mst.id!=Long.parseLong(strBalanceTypeId))
				session.put(Common.FLTR_DL_BAL_BTYPE_ID, "");
			
			//取扱(実際)
			if (!strHandlingId.equals("")) {
				boolean bolClear = false;
				if (Long.parseLong(strHandlingId)==-2) {
					if (rec.handling_mst==null)
						bolClear = true;
				} else if (Long.parseLong(strHandlingId)==-1) {
					if (rec.handling_mst!=null)
						bolClear = true;
				} else {
					if (rec.handling_mst==null) {
						bolClear = true;
					} else {
						if (rec.handling_mst.id!=Long.parseLong(strHandlingId))
							bolClear = true;
					}
				}
				if (bolClear) session.put(Common.FLTR_DL_BAL_HDLG_ID, "");
			}
			
			//取扱(My貯金)
			if (!strIdealDepositId.equals("")) {
				boolean bolClear = false;
				if (Long.parseLong(strIdealDepositId)==-2) {
					if (rec.ideal_deposit_mst==null)
						bolClear = true;
				} else if (Long.parseLong(strIdealDepositId)==-1) {
					if (rec.ideal_deposit_mst!=null)
						bolClear = true;
				} else {
					if (rec.ideal_deposit_mst==null) {
						bolClear = true;
					} else {
						if (rec.ideal_deposit_mst.id!=Long.parseLong(strIdealDepositId))
							bolClear = true;
					}
				}
				if (bolClear) session.put(Common.FLTR_DL_BAL_IDEPO_ID, "");
			}
			
			//項目
			if (!strItemId.equals("")) {
				boolean bolClear = false;
				if (Long.parseLong(strItemId)==-2) {
					if (rec.item_mst==null)
						bolClear = true;
				} else if (Long.parseLong(strItemId)==-1) {
					if (rec.item_mst!=null)
						bolClear = true;
				} else {
					if (rec.item_mst==null) {
						bolClear = true;
					} else {
						if (rec.item_mst.id!=Long.parseLong(strItemId))
							bolClear = true;
					}
				}
				if (bolClear) session.put(Common.FLTR_DL_BAL_ITEM_ID, "");
			}
			
			//引落日
			setSessionDlDate(String.format("%1$tY/%1$tm/%1$td", rec.debit_date), Common.FLTR_DL_BAL_DDTE_FR, Common.FLTR_DL_BAL_DDTE_TO);
		}
	}
	
	/**
	 * 明細表の絞り込み条件を、作成データにやんわり合わせる（残高明細（口座系）に戻す場合）
	 * @param rec
	 */
	private void setSessionDlRtnRemBk(
			Record rec
			) {
		//セッションに絞込条件が入っている時
		if ((session.get(Common.FLTR_DL_RB_EXST_FLG) != null) &&
				(session.get(Common.FLTR_DL_RB_EXST_FLG).equals("true"))) {
			String strHandlingId = session.get(Common.FLTR_DL_RB_HDLG_ID);
			
			//引落日
			setSessionDlDate(String.format("%1$tY/%1$tm/%1$td", rec.debit_date), Common.FLTR_DL_RB_DDTE_FR, Common.FLTR_DL_RB_DDTE_TO);
			
			//取扱(実際)
			if (!strHandlingId.equals("") &&
					rec.handling_mst!=null &&
					rec.handling_mst.id!=Long.parseLong(strHandlingId))
				session.put(Common.FLTR_DL_RB_HDLG_ID, "");
		}
	}
	
	/**
	 * 明細表の絞り込み条件を、作成データにやんわり合わせる（残高明細（My貯金）に戻す場合）
	 * @param rec
	 */
	private void setSessionDlRtnRemId(
			Record rec
			) {
		//セッションに絞込条件が入っている時
		if ((session.get(Common.FLTR_DL_RI_EXST_FLG) != null) &&
				(session.get(Common.FLTR_DL_RI_EXST_FLG).equals("true"))) {
			String strIdealDepositId = session.get(Common.FLTR_DL_RI_IDEPO_ID);
			
			//引落日
			setSessionDlDate(String.format("%1$tY/%1$tm/%1$td", rec.debit_date), Common.FLTR_DL_RI_DDTE_FR, Common.FLTR_DL_RI_DDTE_TO);
			
			//取扱(My貯金)
			if (!strIdealDepositId.equals("") &&
					rec.ideal_deposit_mst!=null &&
					rec.ideal_deposit_mst.id!=Long.parseLong(strIdealDepositId))
				session.put(Common.FLTR_DL_RI_IDEPO_ID, "");
		}
	}
	
	/**
	 * 明細表の日付の絞り込み条件を、作成データにやんわり合わせる
	 * （既存の日付の絞り込み条件からはみ出ていたら、その分だけ広げる）
	 * @param strDate
	 */
	private void setSessionDlDate(
			String strDate,
			String strSessionDateFr,
			String strSessionDateTo
			) {
		String h_date_fr = session.get(strSessionDateFr);
		String h_date_to = session.get(strSessionDateTo);
		
   		Date dteHdDate;
		try {
			dteHdDate = DateFormat.getDateInstance().parse(strDate);
			if (h_date_fr!=null && !h_date_fr.equals("")) {
				Date dteHdDateFr = DateFormat.getDateInstance().parse(h_date_fr);
				if (dteHdDate.compareTo(dteHdDateFr) < 0)
					session.put(strSessionDateFr, strDate);
			}
			
			if (h_date_to!=null && !h_date_to.equals("")) {
				Date dteHdDateTo = DateFormat.getDateInstance().parse(h_date_to);
				if (dteHdDate.compareTo(dteHdDateTo) > 0)
					session.put(strSessionDateTo, strDate);
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   		
	}
}
