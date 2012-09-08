package controllers;

import java.text.DateFormat;
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
	
	public static void recordEdit(Long id) {
		if(id != null) {
			Record record = Record.findById(id);
			render(record);
		}
		render();
	}
	
	public static void save_rec(
			Long id,
			String payment_date,
			Long balance_type_mst,
			Long handling_mst,
			Long ideal_deposit_mst,
			Long item_mst,
			Integer amount,
			String debit_date,
			String content,
			String store,
			String remarks,
			String secret_remarks,
			Boolean secret_rec_flg
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
					amount,
					0,
					0,
					debitDate,
					content,
					store,
					remarks,
					secret_remarks,
					secret_rec_flg==null ? false : (secret_rec_flg==true ? true : false)
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
			record.amount = amount;
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
		
		if(id==null) {
			String h_payment_date = null; 			
			if(payment_date!=null && !payment_date.equals("")) {
				h_payment_date = payment_date.substring(0, 10);
			}
			
			DetailList.detailList(1, h_payment_date, h_payment_date, balanceTypeMst==null ? null : balanceTypeMst.id, handlingMst==null ? null : handlingMst.id, idealDepositMst==null ? null : idealDepositMst.id, itemMst==null ? null : itemMst.id, null, null, null, null, null, null, null);
		} else {
			callSessionDetailList();
		}
	}

	//レコード削除
	public static void del_rec(Long id) {
		// 取扱データの読み出し
		Record record = Record.findById(id);
		// 削除
		record.delete();
		
 		callSessionDetailList();
	}
	
	private static void callSessionDetailList() {
    	String h_payment_date_fr = session.get("hPaymentDateFr");
    	String h_payment_date_to = session.get("hPaymentDateTo");
    	Long h_balance_type_id = null;
		if(!session.get("hBalanceTypeId").equals(""))
			h_balance_type_id = Long.parseLong(session.get("hBalanceTypeId"));
    	Long h_handling_id = null;
		if(!session.get("hHandlingId").equals(""))
			h_handling_id = Long.parseLong(session.get("hHandlingId"));
    	Long h_ideal_deposit_id = null;
		if(!session.get("hIdealDepositId").equals(""))
			h_ideal_deposit_id = Long.parseLong(session.get("hIdealDepositId"));
    	Long h_item_id = null;
		if(!session.get("hItemId").equals(""))
			h_item_id = Long.parseLong(session.get("hItemId"));
		
		DetailList.detailList(1, h_payment_date_fr, h_payment_date_to, h_balance_type_id, h_handling_id, h_ideal_deposit_id, h_item_id, null, null, null, null, null, null, null);
	}
}
