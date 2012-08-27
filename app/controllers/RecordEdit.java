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
   		Calendar calendar = Calendar.getInstance();
   		
   		
   		
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
		try {
			Date paymentDate = null;
			if(payment_date!=null && !payment_date.equals("")) {  // 「payment_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
				paymentDate = DateFormat.getDateTimeInstance().parse(payment_date + ":00");
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
				debitDate = DateFormat.getDateInstance().parse(debit_date);
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
				record.payment_date = DateFormat.getDateTimeInstance().parse(payment_date+":00");
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
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Validate
		validation.valid(record);
		if(validation.hasErrors()) {
	        render("@recordEdit", record);
	    }
		// 保存
		record.save();
		//index();
		DetailList.detailList(null, null, null, null, null, null, null, null, null, null, null,null);
	}

	//レコード削除
	public static void del_rec(Long id) {
		// 取扱データの読み出し
		Record record = Record.findById(id);
		// 保存
		record.delete();
		
//		session.put(key, value);
//		session.get(key);

		DetailList.detailList(null, null, null, null, null, null, null, null, null, null, null,null);
	}
}
