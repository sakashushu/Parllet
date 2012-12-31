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
		if(Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("haUser", haUser);
		}
	}
	
	public static void recordEdit(
			Long id,
    		String df_payment_date,			/* 初期支払日 */
    		Long df_balance_type_id,  		/* 初期収支種類ID */
    		Long df_ideal_deposit_id,		/* 初期取扱(My貯金)ID */
    		Long df_item_id,				/* 初期項目ID */
    		String df_debit_date,
    		String calledFrom				/* 呼び出し元 */
			) {
		//編集
		if(id != null) {
			Record record = Record.findById(id);
			render(record, calledFrom);
		}
		//追加
		render(df_payment_date, df_balance_type_id, df_ideal_deposit_id, df_item_id, df_debit_date);
	}
	
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
		Record record = null;
		Date paymentDate = null;
		if(payment_date!=null && !payment_date.equals("")) {  // 「payment_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
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
		if(item_mst!=null) {
			itemMst = ItemMst.findById(item_mst); 
		}
		HandlingMst handlingMst = null;
		if(handling_mst!=null) {
			handlingMst = HandlingMst.findById(handling_mst);
		}
		Integer intAmount = null;
		//カンマ区切りの数値文字列を数値型に変換するNumberFormatクラスのインスタンスを取得する
		NumberFormat nf = NumberFormat.getInstance();
		if(amount!=null) {
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
		if(debit_date!=null && !debit_date.equals("")) {  // 「debit_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
			try {
				debitDate = DateFormat.getDateInstance().parse(debit_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IdealDepositMst idealDepositMst = null;
		if(ideal_deposit_mst!=null) {
			idealDepositMst = IdealDepositMst.findById(ideal_deposit_mst);
		}
		if(id==null) {
			// 収支データの作成
			record = new Record(
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
			record = Record.findById(id);
			// 編集
			record.payment_date = paymentDate;
			record.balance_type_mst = balanceTypeMst;
			record.handling_mst = handlingMst;
			record.ideal_deposit_mst = idealDepositMst;
			record.item_mst = itemMst;
			record.amount = intAmount;
			record.debit_date = debitDate;
			record.content = content;
			record.store = store;
			record.remarks = remarks;
			record.secret_remarks = secret_remarks;
			record.secret_rec_flg = secret_rec_flg==null ? false : (secret_rec_flg==true ? true : false);
		}
		// Validate
		validation.valid(record);
		if(validation.hasErrors()) {
	        render("@recordEdit", record);
	    }
		// 保存
		record.save();
		
		// 明細表の絞り込み条件を、作成データにやんわり合わせる
		Date dteFrom = paymentDate;
		if(calledFrom.equals("dl_remainderBank") || calledFrom.equals("dl_remainderIdeal"))
			dteFrom = debitDate;
		
		RecordEdit reEd = new RecordEdit();
		reEd.setSessionDetailList(String.format("%1$tY/%1$tm/%1$td", dteFrom), calledFrom);
		
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
		if(calledFrom==null || calledFrom.equals("") || calledFrom.equals("dl_balance"))
			DetailList.dl_balance(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		if(calledFrom.equals("dl_remainderBank"))
			DetailList.dl_remainderBank(null, null, null, null, null, null);
		if(calledFrom.equals("dl_remainderIdeal"))
			DetailList.dl_remainderIdeal(null, null, null, null, null, null);
		
		DetailList.dl_balance(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	
	/**
	 * 明細表の絞り込み条件を、作成データにやんわり合わせる
	 * @param h_payment_date
	 */
	private void setSessionDetailList(
			String h_payment_date,
    		String calledFrom				/* 呼び出し元 */
			) {
		String strSessionDateFr = "";
		String strSessionDateTo = "";
		if(calledFrom==null || calledFrom.equals("") || calledFrom.equals("dl_balance"))
			strSessionDateFr = "hPaymentDateFr";
			strSessionDateTo = "hPaymentDateTo";
		if(calledFrom.equals("dl_remainderBank"))
			strSessionDateFr = "daRbHdDebitDateFr";
			strSessionDateTo = "daRbHdDebitDateFr";
		if(calledFrom.equals("dl_remainderIdeal"))
			strSessionDateFr = "daRiHdDebitDateFr";
			strSessionDateTo = "daRiHdDebitDateTo";
		setSessionDlDate(h_payment_date, strSessionDateFr, strSessionDateTo);
	}
	
	/**
	 * 明細表の日付の絞り込み条件を、作成データにやんわり合わせる
	 * @param h_payment_date
	 */
	private void setSessionDlDate(
			String h_payment_date,
			String strSessionDateFr,
			String strSessionDateTo
			) {
    	String h_payment_date_fr = session.get(strSessionDateFr);
    	String h_payment_date_to = session.get(strSessionDateTo);
    	
   		Date dteHdPaymentDate;
		try {
			dteHdPaymentDate = DateFormat.getDateInstance().parse(h_payment_date);
	    	if(h_payment_date_fr!=null && !h_payment_date_fr.equals("")) {
	    		Date dteHdPaymentDateFr;
				dteHdPaymentDateFr = DateFormat.getDateInstance().parse(h_payment_date_fr);
	    		if(dteHdPaymentDate.compareTo(dteHdPaymentDateFr) != 0)
	    			session.put(strSessionDateFr, h_payment_date);
	    	}
	    	
	    	if(h_payment_date_to!=null && !h_payment_date_to.equals("")) {
	    		Date dteHdPaymentDateTo;
				dteHdPaymentDateTo = DateFormat.getDateInstance().parse(h_payment_date_to);
	    		if(dteHdPaymentDate.compareTo(dteHdPaymentDateTo) >	 0)
	    			session.put(strSessionDateTo, h_payment_date);
	    	}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   		
	}
}
