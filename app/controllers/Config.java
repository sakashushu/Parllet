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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;

import au.com.bytecode.opencsv.CSVReader;

import models.BalanceTypeMst;
import models.HaUser;
import models.HandlingMst;
import models.HandlingTypeMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.Record;

import play.data.validation.Required;
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
	            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv),"MS932"));
	            
	            //CSVパーサとして「OpenCSV」を使用する
	            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csv),"MS932"),',','"',1);
	            
	            String str = null;
	            String[] strAryColumn;
	            boolean bFst = true;	//初回フラグ
	            
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
						IdealDepositMst idealDepositMst = IdealDepositMst.find("ideal_deposit_name = ?", ideal_deposit_name).first(); 
						
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
							IdealDepositMst idealDepositMst = IdealDepositMst.find("ideal_deposit_name = ?", ideal_deposit_name).first(); 
							
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
		List<Record> records = Record.find(" payment_date between '" + down_date_fr + "' and '" + down_date_to + "' order by payment_date, amount, balance_type_mst, handling_mst, ideal_deposit_mst, item_mst, content, store, remarks, secret_remarks ").fetch();
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
		
		Calendar calendar = Calendar.getInstance();
		renderBinary(binaryData, String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS", calendar.getTime()) + ".csv");	
	}
	
	//口座編集（リスト）
	public static void cf_bank_list() {
		String sHandlingType = Messages.get("HandlingType.bank");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
//		render(handlingMsts);
		render("@cf_handling_list", sHandlingType, handlingMsts);
	}
	
	//クレジットカード編集（リスト）
	public static void cf_creca_list() {
//		render();
		String sHandlingType = Messages.get("HandlingType.creca");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
		render("@cf_handling_list", sHandlingType, handlingMsts);
	}
	
	//電子マネー編集（リスト）
	public static void cf_emoney_list() {
		String sHandlingType = Messages.get("HandlingType.emoney");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
		render("@cf_handling_list", sHandlingType, handlingMsts);
	}
	
	
	//項目(収入)編集（リスト）
	public static void cf_item_in_list() {
		String sBalanceType = Messages.get("BalanceType.in");
		List<ItemMst> itemMsts = ItemMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and balance_type_mst.balance_type_name = '" + sBalanceType + "' order by order_seq").fetch();
		render("@cf_item_list", sBalanceType, itemMsts);
	}
	
	//項目(支出)編集（リスト）
	public static void cf_item_out_list() {
		String sBalanceType = Messages.get("BalanceType.out");
		List<ItemMst> itemMsts = ItemMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and balance_type_mst.balance_type_name = '" + sBalanceType + "' order by order_seq").fetch();
		render("@cf_item_list", sBalanceType, itemMsts);
	}
	
	//My貯金編集（リスト）
	public static void cf_idealdepo_list() {
		List<IdealDepositMst> idealDepositMsts = IdealDepositMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' order by order_seq").fetch();
		render("@cf_idealdepo_list", idealDepositMsts);
	}
	
	//口座編集
	public static void cf_bank_edit(Long id) {
		String sHandlingType = Messages.get("HandlingType.bank");
		if(id != null) {
			HandlingMst handlingMst = HandlingMst.findById(id);
			render("@cf_handling_edit", handlingMst, sHandlingType);
		}
		render("@cf_handling_edit", sHandlingType);
	}
	
	//クレジットカード編集
	public static void cf_creca_edit(Long id) {
		String sHandlingType = Messages.get("HandlingType.creca");
		if(id != null) {
			HandlingMst handlingMst = HandlingMst.findById(id);
			render("@cf_handling_edit", handlingMst, sHandlingType);
		}
		render("@cf_handling_edit", sHandlingType);
	}
	
	//電子マネー編集
	public static void cf_emoney_edit(Long id) {
		String sHandlingType = Messages.get("HandlingType.emoney");
		if(id != null) {
			HandlingMst handlingMst = HandlingMst.findById(id);
			render("@cf_handling_edit", handlingMst, sHandlingType);
		}
		render("@cf_handling_edit", sHandlingType);
	}
	
	//項目(収入)編集
	public static void cf_item_in_edit(Long id) {
		String sBalanceType = Messages.get("BalanceType.in");
		if(id != null) {
			ItemMst itemMst = ItemMst.findById(id);
			render("@cf_item_edit", itemMst, sBalanceType);
		}
		render("@cf_item_edit", sBalanceType);
	}
	
	//項目(支出)編集
	public static void cf_item_out_edit(Long id) {
		String sBalanceType = Messages.get("BalanceType.out");
		if(id != null) {
			ItemMst itemMst = ItemMst.findById(id);
			render("@cf_item_edit", itemMst, sBalanceType);
		}
		render("@cf_item_edit", sBalanceType);
	}
	
	//My貯金編集
	public static void cf_idealdepo_edit(Long id) {
		if(id != null) {
			IdealDepositMst idealDepositMst = IdealDepositMst.findById(id);
			render(idealDepositMst);
		}
		render();
	}
	
	//口座保存
	public static void cf_bank_save(
			Long id,
			String handling_name,
			Boolean zero_hidden,
			Boolean invalidity_flg
			) {
		String sHandlingType = Messages.get("HandlingType.bank");
		
		RefHandlingMst refHandlingMst = new RefHandlingMst();
		
		//HandlingMst保存
		Integer iRtn = cf_handling_mst_save(id, handling_name, zero_hidden, invalidity_flg, refHandlingMst, sHandlingType);
		HandlingMst handlingMst = refHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(handlingMst);
			render("@cf_handling_edit", handlingMst, sHandlingType);
		}
		
		cf_bank_list();
		
	}
	
	//電子マネー保存
	public static void cf_emoney_save(
			Long id,
			String handling_name,
			Boolean zero_hidden,
			Boolean invalidity_flg
			) {
		String sHandlingType = Messages.get("HandlingType.emoney");
		
		RefHandlingMst refHandlingMst = new RefHandlingMst();
		
		//HandlingMst保存
		Integer iRtn = cf_handling_mst_save(id, handling_name, zero_hidden, invalidity_flg, refHandlingMst, sHandlingType);
		HandlingMst handlingMst = refHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(handlingMst);
			render("@cf_handling_edit", handlingMst, sHandlingType);
		}
		
		cf_emoney_list();
		
	}
	
	//クレジットカード保存
	public static void cf_creca_save(
			Long id,
			String handling_name,
			Long debit_bank,
			Integer cutoff_day,
			String debit_month,
			Integer debit_day
			) {
		String sHandlingType = Messages.get("HandlingType.creca");
		
		RefHandlingMst refHandlingMst = new RefHandlingMst();
		
		//HandlingMst保存
		Integer iRtn = cf_handling_mst_save(id, handling_name, false, true, refHandlingMst, sHandlingType, debit_bank, cutoff_day, debit_month, debit_day);
		HandlingMst handlingMst = refHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(handlingMst);
			render("@cf_handling_edit", handlingMst, sHandlingType);
		}
		
		cf_creca_list();
		
	}
	
	//項目(収入)保存
	public static void cf_item_in_save(
			Long id,
			String item_name
			) {
		String sBalanceType = Messages.get("BalanceType.in");
		
		RefItemMst refItemMst = new RefItemMst();
		
		//ItemMst保存
		Integer iRtn = cf_item_mst_save(id, item_name, refItemMst, sBalanceType);
		ItemMst itemMst = refItemMst.itemMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(itemMst);
			render("@cf_item_edit", itemMst, sBalanceType);
		}
		
		cf_item_in_list();
		
	}
	
	//項目(支出)保存
	public static void cf_item_out_save(
			Long id,
			String item_name
			) {
		String sBalanceType = Messages.get("BalanceType.out");
		
		RefItemMst refItemMst = new RefItemMst();
		
		//ItemMst保存
		Integer iRtn = cf_item_mst_save(id, item_name, refItemMst, sBalanceType);
		ItemMst itemMst = refItemMst.itemMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(itemMst);
			render("@cf_item_edit", itemMst, sBalanceType);
		}
		
		cf_item_out_list();
		
	}
	
	//My貯金保存
	public static void cf_idealdepo_save(
			Long id,
			String ideal_deposit_name,
			Boolean zero_hidden
			) {
		RefIdealDepositMst refItemMst = new RefIdealDepositMst();
		
		//IdealDepositMst保存
		Integer iRtn = cf_ideal_deposit_mst_save(id, ideal_deposit_name, zero_hidden, refItemMst);
		IdealDepositMst idealDepositMst = refItemMst.idealDepositMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(idealDepositMst);
			render("@cf_idealdepo_edit", idealDepositMst);
		}
		
		cf_idealdepo_list();
		
	}

	//「取扱(実際)」削除
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
	
	//「項目」削除
	public static void cf_item_del(Long id, String sBalanceType) {
		// 項目データの読み出し
		ItemMst itemMst = ItemMst.findById(id);
		// 削除
		itemMst.delete();

		if(sBalanceType.equals(Messages.get("BalanceType.in"))) {
			cf_item_in_list();
		} else if(sBalanceType.equals(Messages.get("BalanceType.out"))) {
			cf_item_out_list();
		}
	}
	
	//「My貯金」削除
	public static void cf_idealdepo_del(Long id) {
		// 項目データの読み出し
		IdealDepositMst idealDepositMst = IdealDepositMst.findById(id);
		// 削除
		idealDepositMst.delete();

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
	 * HandlingMstの保存メソッド
	 * @param id
	 * @param handling_name
	 * @param zero_hidden
	 * @param refHandlingMst
	 * @param sHandlingType
	 * @return
	 */
	private static Integer cf_handling_mst_save(
			Long id,
			String handling_name,
			Boolean zero_hidden,
			Boolean invalidity_flg,
			RefHandlingMst refHandlingMst,
			String sHandlingType
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		HandlingTypeMst handlingTypeMst = HandlingTypeMst.find("byHandling_type_name", sHandlingType).first();
		Integer iCutoffDay = null;
		Integer iDebitDay = null;
		if(id == null) {
			// 取扱データの作成
			refHandlingMst.handlingMst = new HandlingMst(
					haUser,
					handlingTypeMst,
					handling_name,
					null,
					iCutoffDay,
					null,
					iDebitDay,
					zero_hidden==null ? false : (zero_hidden==true ? true : false),
					invalidity_flg==null ? false : (invalidity_flg==true ? true : false),
					10000
			);
		} else {
			// 取扱データの読み出し
			refHandlingMst.handlingMst = HandlingMst.findById(id);
			// 編集
			refHandlingMst.handlingMst.handling_name = handling_name;
			refHandlingMst.handlingMst.zero_hidden = zero_hidden==null ? false : (zero_hidden==true ? true : false);
			refHandlingMst.handlingMst.invalidity_flg = invalidity_flg==null ? false : (invalidity_flg==true ? true : false);
		}
		// Validate
		validation.valid(refHandlingMst.handlingMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refHandlingMst.handlingMst.save();
		
		return 0;
	}
	
	/**
	 * HandlingMstの保存メソッド（クレジットカード用）
	 * @param id
	 * @param handling_name
	 * @param zero_hidden
	 * @param refHandlingMst
	 * @param sHandlingType
	 * @return
	 */
	private static Integer cf_handling_mst_save(
			Long id,
			String handling_name,
			Boolean zero_hidden,
			Boolean invalidity_flg,
			RefHandlingMst refHandlingMst,
			String sHandlingType,
			Long debit_bank,
			Integer cutoff_day,
			String debit_month,
			Integer debit_day
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		HandlingTypeMst handlingTypeMst = HandlingTypeMst.find("byHandling_type_name", sHandlingType).first();
		HandlingMst debitBank = null;
		if(debit_bank!=null) {
			debitBank = HandlingMst.findById(debit_bank);
		}
		if(id == null) {
			// 取扱データの作成
			refHandlingMst.handlingMst = new HandlingMst(
					haUser,
					handlingTypeMst,
					handling_name,
					debitBank,
					cutoff_day,
					debit_month,
					debit_day,
					zero_hidden,
					invalidity_flg==null ? false : (invalidity_flg==true ? true : false),
					10000
			);
		} else {
			// 取扱データの読み出し
			refHandlingMst.handlingMst = HandlingMst.findById(id);
			// 編集
			refHandlingMst.handlingMst.handling_name = handling_name;
			refHandlingMst.handlingMst.debit_bank = debitBank;
			refHandlingMst.handlingMst.cutoff_day = cutoff_day;
			refHandlingMst.handlingMst.debit_month = debit_month;
			refHandlingMst.handlingMst.debit_day = debit_day;
			refHandlingMst.handlingMst.zero_hidden = zero_hidden;
			refHandlingMst.handlingMst.invalidity_flg = invalidity_flg==null ? false : (invalidity_flg==true ? true : false);
		}
		// Validate
		validation.valid(refHandlingMst.handlingMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refHandlingMst.handlingMst.save();
		
		return 0;
	}
	
	/**
	 * ItemMstの保存メソッド
	 * @param id
	 * @param item_name
	 * @param refItemMst
	 * @param sBalanceType
	 * @return
	 */
	private static Integer cf_item_mst_save(
			Long id,
			String item_name,
			RefItemMst refItemMst,
			String sBalanceType
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		BalanceTypeMst balanceTypeMst = BalanceTypeMst.find("byBalance_type_name", sBalanceType).first();
		if(id == null) {
			// 取扱データの作成
			refItemMst.itemMst = new ItemMst(
					haUser,
					balanceTypeMst,
					item_name,
					10000
			);
		} else {
			// 取扱データの読み出し
			refItemMst.itemMst = ItemMst.findById(id);
			// 編集
			refItemMst.itemMst.item_name = item_name;
		}
		// Validate
		validation.valid(refItemMst.itemMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refItemMst.itemMst.save();
		
		return 0;
	}
	
	/**
	 * IdealDepositMstの保存メソッド
	 * @param id
	 * @param ideal_deposit_name
	 * @param zero_hidden
	 * @param refIdealDepositMst
	 * @return
	 */
	private static Integer cf_ideal_deposit_mst_save(
			Long id,
			String ideal_deposit_name,
			Boolean zero_hidden,
			RefIdealDepositMst refIdealDepositMst
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		if(id == null) {
			// My貯金データの作成
			refIdealDepositMst.idealDepositMst = new IdealDepositMst(
					haUser,
					ideal_deposit_name,
					zero_hidden==null ? false : (zero_hidden==true ? true : false),
					10000
			);
		} else {
			// My貯金データの読み出し
			refIdealDepositMst.idealDepositMst = IdealDepositMst.findById(id);
			// 編集
			refIdealDepositMst.idealDepositMst.ideal_deposit_name = ideal_deposit_name;
			refIdealDepositMst.idealDepositMst.zero_hidden = zero_hidden==null ? false : (zero_hidden==true ? true : false);
		}
		// Validate
		validation.valid(refIdealDepositMst.idealDepositMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		refIdealDepositMst.idealDepositMst.save();
		
		return 0;
	}
	
	/**
	 * 取扱種類を元にHandlingMstのリストの取得
	 * @param sHandlingType
	 * @return
	 */
	private static List<HandlingMst> get_handling_msts(String sHandlingType) {
		List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and handling_type_mst.handling_type_name = '" + sHandlingType + "' order by order_seq").fetch();
		return handlingMsts;
	}
	
	/**
	 * HandlingMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	static class RefHandlingMst {
		HandlingMst handlingMst;
	}

	/**
	 * ItemMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	static class RefItemMst {
		ItemMst itemMst;
	}
	
	/**
	 * IdealDepositMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	static class RefIdealDepositMst {
		IdealDepositMst idealDepositMst;
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
