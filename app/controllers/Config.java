package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;

import org.postgresql.translation.messages_bg;

import controllers.Common.RefHandlingMst;
import controllers.Common.RefIdealDepositMst;
import controllers.Common.RefItemMst;

import au.com.bytecode.opencsv.CSVReader;

import models.BalanceTypeMst;
import models.HaUser;
import models.HandlingMst;
import models.HandlingTypeMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.Record;

import play.data.validation.Required;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Config extends Controller {

	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("haUser", haUser);
		}
	}
	
	public static void cf_io() {
		render();
	}
	
	/**
	 * データの取込(インポート)
	 * @param csv
	 */
	public static void cf_upload(File csv) {
		if(csv != null) {
			try {
				//Shift-JISファイルを読み込む想定。（良く分かっていないが、色々試して文字化けしたが、下記のやり方なら文字化けしないようだ。）
//				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv),"MS932"));
				
				//CSVパーサとして「OpenCSV」を使用する
				CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csv),"MS932"),',','"',1);
				
//				String str = null;
				String[] strAryColumn;
//				boolean bFst = true;	//初回フラグ
				
				int iCnt = 0;
				
				// ファイルを1行ずつ読み込む
				while ( ( strAryColumn = reader.readNext() ) != null ) {
					boolean bTransferFlg = false;
					
					Record record = null;
					String payment_date = strAryColumn[0];				//支払日
					String balance_type_name = strAryColumn[1];			//収支種類
					if(balance_type_name.equals("口座振替")) {
						bTransferFlg = true;
						balance_type_name = "口座預入";
					}
					String handling_name = strAryColumn[2];				//取扱
					String ideal_deposit_name = strAryColumn[3];		//My貯金
					String item_name = strAryColumn[4];					//項目
					Integer amount = Integer.parseInt(strAryColumn[5]);	//金額
					String debit_date = strAryColumn[6];				//引落日
					String content = strAryColumn[7];					//内容
					String store = strAryColumn[8];						//お店		
					String remarks = strAryColumn[9];					//備考
					String secret_remarks = strAryColumn[10];			//備考（非公開）
					Boolean secret_rec_flg = Boolean.valueOf(strAryColumn[11]);	//非公開レコードフラグ
					
					try {
						Date paymentDate = null;
						if(payment_date!=null && !payment_date.equals("")) {  // 「payment_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
							String strPdTail = payment_date.substring(payment_date.length()-3, payment_date.length()-2);
							if(!strPdTail.equals(":"))
								payment_date += " 00:00";
							paymentDate = DateFormat.getDateTimeInstance().parse(payment_date + ":00");
						}
						HaUser haUser = (HaUser)renderArgs.get("haUser");
						BalanceTypeMst balanceTypeMst = BalanceTypeMst.find("balance_type_name = ?", balance_type_name).first();
						ItemMst itemMst = null;
						if(item_name!=null) {
							itemMst = ItemMst.find("ha_user = ? and item_name = ?", haUser, item_name).first(); 
						}
						HandlingMst handlingMst = HandlingMst.find("ha_user = ? and handling_name = ?", haUser, handling_name).first();
						Date debitDate = null;
						if(debit_date!=null && !debit_date.equals("")) {  // 「debit_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
							String strDdTail = debit_date.substring(debit_date.length()-3, debit_date.length()-2);
							if(!strDdTail.equals(":"))
								debit_date += " 00:00";
							debitDate = DateFormat.getDateTimeInstance().parse(debit_date + ":00");
						}
						IdealDepositMst idealDepositMst = IdealDepositMst.find("ha_user = ? and ideal_deposit_name = ?", haUser, ideal_deposit_name).first(); 
						
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
								secret_rec_flg,
								null
						);
						
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
						//エラー処理が必要
				
					}
					// 保存
					record.save();
					
					if(bTransferFlg) {
						balance_type_name = "口座引出";
						handling_name = secret_remarks;
						try {
							Date paymentDate = null;
							if(payment_date!=null && !payment_date.equals("")) {  // 「payment_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
								String strPdTail = payment_date.substring(payment_date.length()-3, payment_date.length()-2);
								if(!strPdTail.equals(":"))
									payment_date += " 00:00";
								paymentDate = DateFormat.getDateTimeInstance().parse(payment_date + ":00");
							}
							HaUser haUser = (HaUser)renderArgs.get("haUser");
							BalanceTypeMst balanceTypeMst = BalanceTypeMst.find("balance_type_name = ?", balance_type_name).first();
							ItemMst itemMst = null;
							if(item_name!=null) {
								itemMst = ItemMst.find("ha_user = ? and item_name = ?", haUser, item_name).first(); 
							}
							HandlingMst handlingMst = HandlingMst.find("ha_user = ? and handling_name = ?", haUser, handling_name).first();
							Date debitDate = null;
							if(debit_date!=null && !debit_date.equals("")) {  // 「debit_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
								String strDdTail = debit_date.substring(debit_date.length()-3, debit_date.length()-2);
								if(!strDdTail.equals(":"))
									debit_date += " 00:00";
								debitDate = DateFormat.getDateTimeInstance().parse(debit_date + ":00");
							}
							IdealDepositMst idealDepositMst = IdealDepositMst.find("ha_user = ? and ideal_deposit_name = ?", haUser, ideal_deposit_name).first(); 
							
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
									secret_rec_flg,
									null
							);
							
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
							//エラー処理が必要
				
						}
						// 保存
						record.save();
					}
					
					iCnt++;
					System.out.println(iCnt);
					
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	/**
	 * データの出力(エクスポート)
	 * @param down_date_fr
	 * @param down_date_to
	 * @throws UnsupportedEncodingException
	 */
	public static void cf_download(
    		String down_date_fr,	/* 絞込日時範囲（開始） */
    		String down_date_to		/* 絞込日時範囲（終了） */
			) throws UnsupportedEncodingException {

		String sOutCsv = "";
		int iCnt = 0;

		if((down_date_fr==null) || (down_date_fr.equals(""))) {
			down_date_fr = "1900/01/01";
		}
		if((down_date_to==null) || (down_date_to.equals(""))) {
			down_date_to = "2999/12/31";
		}
		
		Calendar calendar = Calendar.getInstance();
		Date dteTmp = null;
		try {
			dteTmp = DateFormat.getDateInstance().parse(down_date_to);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calendar.setTime(dteTmp);
		calendar.add(Calendar.DATE, 1);
		down_date_to = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
		
		//ヘッダー行
		Field[] fldAryHd = Record.class.getDeclaredFields();
		for(Field fldHd : fldAryHd) {
			//「家計簿ユーザー」・「項目詳細」・「単価」・「数量」は無視
			if(fldHd.getName().equals("ha_user") ||
					fldHd.getName().equals("detail_mst") ||
					fldHd.getName().equals("price") ||
					fldHd.getName().equals("quantity") ||
					fldHd.getName().equals("remainder")) continue;
			
			iCnt++;
			
			//先頭項目以外はカンマで区切る
			if(iCnt != 1) sOutCsv += ",";

			sOutCsv += "\"" + Messages.get(fldHd.getName()) + "\"";
			
		}
		sOutCsv += System.getProperty("line.separator");	//改行
		
		//明細行
//		List<Record> records = Record.find(" payment_date between '" + down_date_fr + "' and '" + down_date_to + "' order by payment_date, amount, balance_type_mst, handling_mst, ideal_deposit_mst, item_mst, content, store, remarks, secret_remarks ").fetch();
		List<Record> records = Record.find(" payment_date >= '" + down_date_fr + "' and payment_date < '" + down_date_to + "' order by payment_date, amount, balance_type_mst, handling_mst, ideal_deposit_mst, item_mst, content, store, remarks, secret_remarks ").fetch();
		for(Record rec : records) {
			iCnt = 0;
			
			Field[] fldAry = rec.getClass().getDeclaredFields();
			
			//項目毎に取得し、カンマ区切り
			for(Field fld : fldAry) {
				//「家計簿ユーザー」・「項目詳細」・「単価」・「数量」・「残高」は無視
				if(fld.getName().equals("ha_user") ||
						fld.getName().equals("detail_mst") ||
						fld.getName().equals("price") ||
						fld.getName().equals("quantity") ||
						fld.getName().equals("remainder")) continue;
				
				iCnt++;
				
				//先頭項目以外はカンマで区切る
				if(iCnt != 1) sOutCsv += ",";
	
				boolean bDblQwtFlg = false;
				
				//ダブルクウォーテーション判定
				if(fld.getType() == Date.class ||
						fld.getType() == String.class ||
						fld.getType() == BalanceTypeMst.class ||
						fld.getType() == ItemMst.class ||
						fld.getType() == HandlingMst.class ||
						fld.getType() == IdealDepositMst.class) {
					bDblQwtFlg = true;
				}
				if(bDblQwtFlg) sOutCsv += "\"";
				
				try {
					if(fld.get(rec) != null) {
						if(fld.getType() == Date.class) {
							sOutCsv += String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM", fld.get(rec));							
						} else {
							sOutCsv += fld.get(rec);
						}
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(bDblQwtFlg) sOutCsv += "\"";

			}
			
			sOutCsv += System.getProperty("line.separator");	//改行
		}
		
		response.setContentTypeIfNotSet("application/binary");
		
		//Shift_JISで出力
		java.io.InputStream binaryData = new ByteArrayInputStream(sOutCsv.getBytes("Shift_JIS"));
		
		calendar = Calendar.getInstance();
		renderBinary(binaryData, String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS", calendar.getTime()) + ".csv");	
	}
	
	/**
	 * 口座編集（リスト）
	 */
	public static void cf_bank_list() {
		String sHandlingType = Messages.get("HandlingType.bank");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
//		render(handlingMsts);
		render("@cf_handling_list", sHandlingType, handlingMsts);
	}
	
	/**
	 * クレジットカード編集（リスト）
	 */
	public static void cf_creca_list() {
//		render();
		String sHandlingType = Messages.get("HandlingType.creca");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
		render("@cf_handling_list", sHandlingType, handlingMsts);
	}
	
	/**
	 * 電子マネー編集（リスト）
	 */
	public static void cf_emoney_list() {
		String sHandlingType = Messages.get("HandlingType.emoney");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
		render("@cf_handling_list", sHandlingType, handlingMsts);
	}
	
	
	/**
	 * 項目(収入)編集（リスト）
	 */
	public static void cf_item_in_list() {
		String sBalanceType = Messages.get("BalanceType.in");
		List<ItemMst> itemMsts = ItemMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and balance_type_mst.balance_type_name = '" + sBalanceType + "' order by order_seq, id").fetch();
		render("@cf_item_list", sBalanceType, itemMsts);
	}
	
	/**
	 * 項目(支出)編集（リスト）
	 */
	public static void cf_item_out_list() {
		String sBalanceType = Messages.get("BalanceType.out");
		List<ItemMst> itemMsts = ItemMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and balance_type_mst.balance_type_name = '" + sBalanceType + "' order by order_seq, id").fetch();
		render("@cf_item_list", sBalanceType, itemMsts);
	}
	
	/**
	 * My貯金編集（リスト）
	 */
	public static void cf_idealdepo_list() {
		List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' order by order_seq, id").fetch();
		render("@cf_idealdepo_list", idealDepositMsts);
	}
	
	/**
	 * 口座編集
	 * @param id
	 */
	public static void cf_bank_edit(Long id) {
		String sHandlingType = Messages.get("HandlingType.bank");
		if(id != null) {
			HandlingMst hM = HandlingMst.findById(id);
			render("@cf_handling_edit", hM, sHandlingType);
		}
		render("@cf_handling_edit", sHandlingType);
	}
	
	/**
	 * クレジットカード編集
	 * @param id
	 */
	public static void cf_creca_edit(Long id) {
		String sHandlingType = Messages.get("HandlingType.creca");
		if(id != null) {
			HandlingMst hM = HandlingMst.findById(id);
			render("@cf_handling_edit", hM, sHandlingType);
		}
		render("@cf_handling_edit", sHandlingType);
	}
	
	/**
	 * 電子マネー編集
	 * @param id
	 */
	public static void cf_emoney_edit(Long id) {
		String sHandlingType = Messages.get("HandlingType.emoney");
		if(id != null) {
			HandlingMst hM = HandlingMst.findById(id);
			render("@cf_handling_edit", hM, sHandlingType);
		}
		render("@cf_handling_edit", sHandlingType);
	}
	
	/**
	 * 項目(収入)編集
	 * @param id
	 */
	public static void cf_item_in_edit(Long id) {
		String sBalanceType = Messages.get("BalanceType.in");
		if(id != null) {
			ItemMst iM = ItemMst.findById(id);
			render("@cf_item_edit", iM, sBalanceType);
		}
		render("@cf_item_edit", sBalanceType);
	}
	
	/**
	 * 項目(支出)編集
	 * @param id
	 */
	public static void cf_item_out_edit(Long id) {
		String sBalanceType = Messages.get("BalanceType.out");
		if(id != null) {
			ItemMst iM = ItemMst.findById(id);
			render("@cf_item_edit", iM, sBalanceType);
		}
		render("@cf_item_edit", sBalanceType);
	}
	
	/**
	 * My貯金編集
	 * @param id
	 */
	public static void cf_idealdepo_edit(Long id) {
		if(id != null) {
			IdealDepositMst iDM = IdealDepositMst.findById(id);
			render(iDM);
		}
		render();
	}
	
	/**
	 * 口座保存
	 * @param id
	 * @param handling_name
	 * @param hM_zero_hidden
	 * @param hM_invalidity_flg
	 */
	public static void cf_bank_save(
			Long id,
			String hM_handling_name,
			Boolean hM_zero_hidden,
			Boolean hM_invalidity_flg
			) {
		String sHandlingType = Messages.get("HandlingType.bank");
		
		RefHandlingMst refHandlingMst = new RefHandlingMst();
		
		//HandlingMst保存
		Common cmn = new Common();
		Integer iRtn = cmn.handling_mst_save(id, hM_handling_name, hM_zero_hidden, hM_invalidity_flg, refHandlingMst, sHandlingType);
		HandlingMst hM = refHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(hM);
			render("@cf_handling_edit", hM, sHandlingType);
		}
		
		cf_bank_list();
		
	}
	
	/**
	 * 電子マネー保存
	 * @param id
	 * @param handling_name
	 * @param hM_zero_hidden
	 * @param hM_invalidity_flg
	 */
	public static void cf_emoney_save(
			Long id,
			String hM_handling_name,
			Boolean hM_zero_hidden,
			Boolean hM_invalidity_flg
			) {
		String sHandlingType = Messages.get("HandlingType.emoney");
		
		RefHandlingMst refHandlingMst = new RefHandlingMst();
		
		//HandlingMst保存
		Common cmn = new Common();
		Integer iRtn = cmn.handling_mst_save(id, hM_handling_name, hM_zero_hidden, hM_invalidity_flg, refHandlingMst, sHandlingType);
		HandlingMst hM = refHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(hM);
			render("@cf_handling_edit", hM, sHandlingType);
		}
		
		cf_emoney_list();
		
	}
	
	/**
	 * クレジットカード保存
	 * @param id
	 * @param hM_handling_name
	 * @param hM_debit_bank
	 * @param hM_cutoff_day
	 * @param hM_debit_month
	 * @param hM_debit_day
	 * @param hM_invalidity_flg
	 */
	public static void cf_creca_save(
			Long id,
			String hM_handling_name,
			Long hM_debit_bank,
			Integer hM_cutoff_day,
			String hM_debit_month,
			Integer hM_debit_day,
			Boolean hM_invalidity_flg
			) {
		String sHandlingType = Messages.get("HandlingType.creca");
		
		RefHandlingMst refHandlingMst = new RefHandlingMst();
		
		//HandlingMst保存
		Common cmn = new Common();
		Integer iRtn = cmn.handling_mst_save(id, hM_handling_name, false, hM_invalidity_flg, refHandlingMst, sHandlingType, hM_debit_bank, hM_cutoff_day, hM_debit_month, hM_debit_day);
		HandlingMst hM = refHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(hM);
			render("@cf_handling_edit", hM, sHandlingType);
		}
		
		cf_creca_list();
		
	}
	
	/**
	 * 項目(収入)保存
	 * @param id
	 * @param iM_item_name
	 */
	public static void cf_item_in_save(
			Long id,
			String iM_item_name
			) {
		String sBalanceType = Messages.get("BalanceType.in");
		
		RefItemMst refItemMst = new RefItemMst();
		
		//ItemMst保存
		Common cmn = new Common();
		Integer iRtn = cmn.item_mst_save(id, iM_item_name, refItemMst, sBalanceType);
		ItemMst iM = refItemMst.itemMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(iM);
			render("@cf_item_edit", iM, sBalanceType);
		}
		
		cf_item_in_list();
		
	}
	
	/**
	 * 項目(支出)保存
	 * @param id
	 * @param iM_item_name
	 */
	public static void cf_item_out_save(
			Long id,
			String iM_item_name
			) {
		String sBalanceType = Messages.get("BalanceType.out");
		
		RefItemMst refItemMst = new RefItemMst();
		
		//ItemMst保存
		Common cmn = new Common();
		Integer iRtn = cmn.item_mst_save(id, iM_item_name, refItemMst, sBalanceType);
		ItemMst iM = refItemMst.itemMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(iM);
			render("@cf_item_edit", iM, sBalanceType);
		}
		
		cf_item_out_list();
		
	}
	
	/**
	 * My貯金保存
	 * @param id
	 * @param iDM_ideal_deposit_name
	 * @param iDM_zero_hidden
	 */
	public static void cf_idealdepo_save(
			Long id,
			String iDM_ideal_deposit_name,
			Boolean iDM_zero_hidden
			) {
		RefIdealDepositMst refItemMst = new RefIdealDepositMst();
		
		//IdealDepositMst保存
		Common cmn = new Common();
		Integer iRtn = cmn.ideal_deposit_mst_save(id, iDM_ideal_deposit_name, iDM_zero_hidden, refItemMst);
		IdealDepositMst iDM = refItemMst.idealDepositMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(iDM);
			render("@cf_idealdepo_edit", iDM);
		}
		
		cf_idealdepo_list();
		
	}

	/**
	 * 「取扱(実際)」削除
	 * @param id
	 * @param sHandlingType
	 */
	public static void cf_handling_del(Long id, String sHandlingType) {
		// 取扱データの読み出し
		HandlingMst handlingMst = HandlingMst.findById(id);
		// 削除
		handlingMst.delete();

		if(sHandlingType.equals(Messages.get("HandlingType.bank"))) {
			cf_bank_list();
		} else if(sHandlingType.equals(Messages.get("HandlingType.creca"))) {
			cf_creca_list();
		} else if(sHandlingType.equals(Messages.get("HandlingType.emoney"))) {
			cf_emoney_list();
		}
	}
	
	/**
	 * 「項目」削除
	 * @param id
	 * @param sBalanceType
	 */
	public static void cf_item_del(Long id, String sBalanceType) {
		// 項目データの読み出し
		ItemMst iM = ItemMst.findById(id);
		// 削除
		iM.delete();

		if(sBalanceType.equals(Messages.get("BalanceType.in"))) {
			cf_item_in_list();
		} else if(sBalanceType.equals(Messages.get("BalanceType.out"))) {
			cf_item_out_list();
		}
	}
	
	/**
	 * 「My貯金」削除
	 * @param id
	 */
	public static void cf_idealdepo_del(Long id) {
		// 項目データの読み出し
		IdealDepositMst iDM = IdealDepositMst.findById(id);
		// 削除
		iDM.delete();

		cf_idealdepo_list();
	}
	
	/**
	 * HandlingMstの並べ替え
	 * @param id
	 * @param order
	 * @param sHandlingType
	 */
	public static void cf_handling_orderChange(
    		List<Long> id,
    		List<Integer> order,
    		String sHandlingType
    		) {
		Iterator<Integer> intOrder = order.iterator();
		for (Long lngId : id) {
			// 「取扱（実際）」データの読み出し
			HandlingMst handlingMst = HandlingMst.findById(lngId);
			// 編集
			handlingMst.order_seq = intOrder.next();
			// Validate
			validation.valid(handlingMst);
			if(validation.hasErrors()) {
				break;
		    }
			// 保存
			handlingMst.save();
		}
		
		if(sHandlingType.equals(Messages.get("HandlingType.bank"))) {
			cf_bank_list();
		} else if(sHandlingType.equals(Messages.get("HandlingType.creca"))) {
			cf_creca_list();
		} else if(sHandlingType.equals(Messages.get("HandlingType.emoney"))) {
			cf_emoney_list();
		}
	}
	
	/**
	 * ItemMstの並べ替え
	 * @param id
	 * @param order
	 * @param sBalanceType
	 */
	public static void cf_item_orderChange(
    		List<Long> id,
    		List<Integer> order,
    		String sBalanceType
    		) {
		Iterator<Integer> intOrder = order.iterator();
		for (Long lngId : id) {
			// 「項目」データの読み出し
			ItemMst itemMst = ItemMst.findById(lngId);
			// 編集
			itemMst.order_seq = intOrder.next();
			// Validate
			validation.valid(itemMst);
			if(validation.hasErrors()) {
				break;
		    }
			// 保存
			itemMst.save();
		}
		
		if(sBalanceType.equals(Messages.get("BalanceType.in"))) {
			cf_item_in_list();
		} else if(sBalanceType.equals(Messages.get("BalanceType.out"))) {
			cf_item_out_list();
		}
	}
	
	/**
	 * IdealDepositMstの並べ替え
	 * @param id
	 * @param order
	 */
	public static void cf_ideal_orderChange(
    		List<Long> id,
    		List<Integer> order
    		) {
		Iterator<Integer> intOrder = order.iterator();
		for (Long lngId : id) {
			// 「取扱（実際）」データの読み出し
			IdealDepositMst idealDepositMst = IdealDepositMst.findById(lngId);
			// 編集
			idealDepositMst.order_seq = intOrder.next();
			// Validate
			validation.valid(idealDepositMst);
			if(validation.hasErrors()) {
				break;
		    }
			// 保存
			idealDepositMst.save();
		}
		
		cf_idealdepo_list();
	}
	
	/**
	 * 会員情報編集
	 */
	public static void cf_hauser_edit() {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		render(haUser);
	}
	
	/**
	 * 会員情報保存
	 * @param id
	 * @param item_name
	 */
	public static void cf_hauser_save(
			String haUser_email,
			String haUser_nickname,
			String haUser_fullname
			) {
		boolean bolEmailChg = false;
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		if(!haUser.email.equals(haUser_email))
			bolEmailChg = true;
		haUser.email = haUser_email;
		haUser.nickname = haUser_nickname;
		haUser.fullname = haUser_fullname;
		// Validate
		validation.valid(haUser);
		if(validation.hasErrors()) {
			render("@cf_hauser_edit", haUser);
	    }
		// 保存
		haUser.save();
		
		if(bolEmailChg)
			session.put("username", haUser.email);
		
		cf_hauser_edit();
	}
	
	/**
	 * 会員情パスワード編集
	 */
	public static void cf_hauser_pw_edit(
			String firstTime
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		render(haUser, firstTime);
	}
	
	/**
	 * 会員情報パスワード保存
	 * @param id
	 * @param item_name
	 */
	public static void cf_hauser_pw_save(
			String firstTime,
			String crt_password,
			String haUser_password,
			String cnf_password,
			String save,
			String cancel
			) {
		/* キャンセルボタンが押されたら、前の画面に戻る */
		if(cancel!=null) {
			cf_hauser_edit();
			return;
		}
		
		/* 必須チェック */
		String strReq = Messages.get("validation.required");
		
//	   	#{if haUser.fbId && !haUser.pwSetFlg}
//			jQuery('#firstTime').val('true');
//		#{/if}
		
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		if(haUser.pwSetFlg) {
			
//		}
//		if(firstTime!=null || !firstTime.equals("true")) {
			if(crt_password==null || crt_password.equals("")) {
				validation.addError("crt_password", strReq);
			}
		}
		if(haUser_password==null || haUser_password.equals("")) {
			validation.addError("haUser.password", strReq);
		}
		if(cnf_password==null || cnf_password.equals("")) {
			validation.addError("cnf_password", strReq);
		}
		if(validation.hasErrors())
			render("@cf_hauser_pw_edit", firstTime);
		
//		HaUser haUser = (HaUser)renderArgs.get("haUser");
		
		/* 現在のパスワード */
		if(!firstTime.equals("true")) {
			if(!haUser.password.equals(crt_password)) {
				validation.addError("crt_password", Messages.get("validation.crtPasswordError"));
				render("@cf_hauser_pw_edit");
			}
		}
		
		/* 新しいパスワード */
		if(!haUser_password.equals(cnf_password)) {
			validation.addError("cnf_password", Messages.get("validation.cnfPasswordError"));
			render("@cf_hauser_pw_edit", firstTime);
		}
		haUser.password = haUser_password;
		haUser.pwSetFlg = true;
		// Validate
		validation.valid(haUser);
		if(validation.hasErrors()) {
			render("@cf_hauser_pw_edit", firstTime);
	    }
		// 保存
		haUser.save();
		
		cf_hauser_edit();
	}
	
	/**
	 * 取扱種類を元にHandlingMstのリストの取得
	 * @param sHandlingType
	 * @return
	 */
	private static List<HandlingMst> get_handling_msts(String sHandlingType) {
		List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and handling_type_mst.handling_type_name = '" + sHandlingType + "' order by order_seq, id").fetch();
		return handlingMsts;
	}
	
	//実行モード
	public static void cf_actionMode() {
		render();
	}
	
	//実行モードを編集モードに
	public static void cf_actionMode_changeToEdit() {
		session.put("actionMode", "Edit");
		cf_actionMode();
	}
	
	//実行モードを閲覧モードに
	public static void cf_actionMode_changeToView() {
		session.put("actionMode", "View");
		cf_actionMode();
	}
	
}
