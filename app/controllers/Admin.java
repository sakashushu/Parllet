package controllers;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import models.BalanceTypeMst;
import models.HaUser;
import models.HandlingMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.Record;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Admin extends Controller {
	
	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			HaUser hauser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("user", hauser.fullname);
		}
	}
	
	public static void index() {
		String haUser = Security.connected();
		List<Record> records = Record.find("ha_user.email", haUser).fetch();
		render(records);
	}
	
	public static void form() {
		render();
	}
	
	public static void save(
			String payment_date,
			Long balance_type_mst,
			Long item_mst,
			String detail_mst,
			Integer amount,
			Long handling_mst,
			String debit_date,
			String content,
			String store,
			String remarks,
			String secret_remarks,
			Long ideal_deposit_mst
			) {
		// 収支データの作成
		HaUser haUser = HaUser.find("byEmail", Security.connected()).first();
		BalanceTypeMst balanceTypeMst = BalanceTypeMst.findById(balance_type_mst);
		ItemMst itemMst = ItemMst.findById(item_mst);
		HandlingMst handlingMst = HandlingMst.findById(handling_mst);
		Date debitDate = null;
		if(debit_date!=null) {
			debitDate = DateFormat.getDateInstance().parse(debit_date);
		}
		IdealDepositMst idealDepositMst = IdealDepositMst.findById(ideal_deposit_mst); 
		Record record = new Record(
				haUser,
				DateFormat.getDateInstance().parse(payment_date),
				balanceTypeMst,
				itemMst,
				detail_mst, 
				amount, 
				0,
				0,
				handlingMst,
				debitDate,
				content,
				store,
				remarks,
				secret_remarks,
				idealDepositMst
		);
		// Validate
		validation.valid(record);
		if(validation.hasErrors()) {
	        render("@form", record);
	    }
		// 保存
		record.save();
		index();
	}
}
