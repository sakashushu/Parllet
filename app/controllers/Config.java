package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	            String str = null;
	            String sOut = "";
	            boolean bFst = true;	//初回フラグ
	            
	            // ファイルを1行ずつ読み込む
				while ( ( str = br.readLine() ) != null ) {
					//初回はヘッダー行のため、何もしない
					if(bFst) {
						bFst = false;
						continue;
					}
					
					//split()に区切り文字を指定してトークンに分割する
					String[] tokens = str.split(",");
					
//					//トークンを表示する
//					for(String token : tokens) {
//						System.out.print("<");
//						System.out.print(token);
//						System.out.print(">");
//					}					
//					System.out.println();
					
					Record record = null;
					String payment_date = tokens[0].substring(1, tokens[0].length()-1);			//支払日
					String balance_type_name = tokens[1].substring(1, tokens[1].length()-1);	//収支種類
					String handling_name = tokens[2].substring(1, tokens[2].length()-1);		//取扱
					String ideal_deposit_name = tokens[3].substring(1, tokens[3].length()-1);	//My貯金
					String item_name = tokens[4].substring(1, tokens[4].length()-1);			//項目
					Integer amount = Integer.parseInt(tokens[5]);								//金額
					String debit_date = tokens[6].substring(1, tokens[6].length()-1);			//引落日
					String content = tokens[7].substring(1, tokens[7].length()-1);				//内容
					String store = tokens[8].substring(1, tokens[8].length()-1);				//お店
					String remarks = tokens[9].substring(1, tokens[9].length()-1);				//備考
					
					remarks = "インポートテスト";
					
					String secret_remarks = tokens[10].substring(1, tokens[10].length()-1);		//備考（非公開）
					
					try {
						Date paymentDate = null;
						if(payment_date!=null && !payment_date.equals("")) {  // 「payment_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
//							paymentDate = DateFormat.getDateInstance().parse(payment_date);
							paymentDate = DateFormat.getDateTimeInstance().parse(payment_date + ":00");
						}
						HaUser haUser = (HaUser)renderArgs.get("haUser");
						BalanceTypeMst balanceTypeMst = BalanceTypeMst.find("balance_type_name = '" + balance_type_name + "'").first();
						ItemMst itemMst = null;
						if(item_name!=null) {
							itemMst = ItemMst.find("item_name = '" + item_name + "'").first(); 
						}
						HandlingMst handlingMst = HandlingMst.find("handling_name = '" + handling_name + "'").first();
						Date debitDate = null;
						if(debit_date!=null && !debit_date.equals("")) {  // 「debit_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
							debitDate = DateFormat.getDateInstance().parse(debit_date);
						}
						IdealDepositMst idealDepositMst = IdealDepositMst.find("ideal_deposit_name = '" + ideal_deposit_name + "'").first(); 
						
						// 収支データの作成
						record = new Record(
								haUser,
								paymentDate,
								balanceTypeMst,
								itemMst,
								null,
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
					
					
					
					
					
					sOut += str + System.getProperty("line.separator");
					
//					byte[]bytes = str.getBytes(); // Shift-JISのコードを表示
//					for (byte b: bytes) {
//						System.out.print(String.format("%02X ", (int)b & 0xff));
//					}
				}
				// 書き込みストリームを生成します。
				BufferedWriter out
				  = new BufferedWriter(new FileWriter("C:\\Saya\\test.csv"));
				// ファイルに書き込みします
				out.write(sOut);
				
				// ストリームを閉じます
				out.close();				    
	
	            // ファイルを閉じる
	            br.close();
	            
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            
//			File saveTo = new File("C:\\Saya\\" + csv.getName());
//			csv.renameTo(saveTo);
			
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
			iCnt++;
			
			//「家計簿ユーザー」・「項目詳細」・「単価」・「数量」は無視
			if(fldHd.getName().equals("ha_user") ||
					fldHd.getName().equals("detail_mst") ||
					fldHd.getName().equals("price") ||
					fldHd.getName().equals("quantity")) continue;
			
			sOutCsv += "\"" + Messages.get(fldHd.getName()) + "\"";
			
			//最終項目以外はカンマで区切る
			if(iCnt < fldAryHd.length) sOutCsv += ",";

		}
		sOutCsv += System.getProperty("line.separator");	//改行
		
		//明細行
		List<Record> records = Record.find(" payment_date between '" + down_date_fr + "' and '" + down_date_to + "'").fetch();
		for(Record rec : records) {
			iCnt = 0;
			
			Field[] fldAry = rec.getClass().getDeclaredFields();
			
			//項目毎に取得し、カンマ区切り
			for(Field fld : fldAry) {
				iCnt++;
				
				//「家計簿ユーザー」・「項目詳細」・「単価」・「数量」は無視
				if(fld.getName().equals("ha_user") ||
						fld.getName().equals("detail_mst") ||
						fld.getName().equals("price") ||
						fld.getName().equals("quantity")) continue;
				
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
				if(bDblQwtFlg) {
					sOutCsv += "\"";
				}
				
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
				if(bDblQwtFlg) {
					sOutCsv += "\"";
				}

				//最終項目以外はカンマで区切る
				if(iCnt < fldAry.length) sOutCsv += ",";
				
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
		List<ItemMst> itemMsts = ItemMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and balance_type_mst.balance_type_name = '" + sBalanceType + "' order by id").fetch();
		render("@cf_item_list", sBalanceType, itemMsts);
	}
	
	//項目(支出)編集（リスト）
	public static void cf_item_out_list() {
		String sBalanceType = Messages.get("BalanceType.out");
		List<ItemMst> itemMsts = ItemMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and balance_type_mst.balance_type_name = '" + sBalanceType + "' order by id").fetch();
		render("@cf_item_list", sBalanceType, itemMsts);
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
	
	//口座保存
	public static void cf_bank_save(
			Long id,
			String handling_name
			) {
		String sHandlingType = Messages.get("HandlingType.bank");
		
		EditHandlingMst editHandlingMst = new EditHandlingMst();
		
		//HandlingMst保存
		Integer iRtn = cf_handling_mst_save(id, handling_name, editHandlingMst, sHandlingType);
		HandlingMst handlingMst = editHandlingMst.handlingMst;
		
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
			String handling_name
			) {
		String sHandlingType = Messages.get("HandlingType.emoney");
		
		EditHandlingMst editHandlingMst = new EditHandlingMst();
		
		//HandlingMst保存
		Integer iRtn = cf_handling_mst_save(id, handling_name, editHandlingMst, sHandlingType);
		HandlingMst handlingMst = editHandlingMst.handlingMst;
		
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
		
		EditHandlingMst editHandlingMst = new EditHandlingMst();
		
		//HandlingMst保存
		Integer iRtn = cf_handling_mst_save(id, handling_name, editHandlingMst, sHandlingType, debit_bank, cutoff_day, debit_month, debit_day);
		HandlingMst handlingMst = editHandlingMst.handlingMst;
		
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
		
		EditItemMst editItemMst = new EditItemMst();
		
		//ItemMst保存
		Integer iRtn = cf_item_mst_save(id, item_name, editItemMst, sBalanceType);
		ItemMst itemMst = editItemMst.itemMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(itemMst);
			render("@cf_handling_edit", itemMst, sBalanceType);
		}
		
		cf_item_in_list();
		
	}
	
	//項目(支出)保存
	public static void cf_item_out_save(
			Long id,
			String item_name
			) {
		String sBalanceType = Messages.get("BalanceType.out");
		
		EditItemMst editItemMst = new EditItemMst();
		
		//ItemMst保存
		Integer iRtn = cf_item_mst_save(id, item_name, editItemMst, sBalanceType);
		ItemMst itemMst = editItemMst.itemMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(itemMst);
			render("@cf_handling_edit", itemMst, sBalanceType);
		}
		
		cf_item_out_list();
		
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
	

	/**
	 * HandlingMstの保存メソッド
	 * @param id
	 * @param handling_name
	 * @param editHandlingMst
	 * @param sHandlingType
	 * @return
	 */
	private static Integer cf_handling_mst_save(
			Long id,
			String handling_name,
			EditHandlingMst editHandlingMst,
			String sHandlingType
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		HandlingTypeMst handlingTypeMst = HandlingTypeMst.find("byHandling_type_name", sHandlingType).first();
		Integer iCutoffDay = null;
		Integer iDebitDay = null;
		if(id == null) {
			// 取扱データの作成
			editHandlingMst.handlingMst = new HandlingMst(
					haUser,
					handlingTypeMst,
					handling_name,
					null,
					iCutoffDay,
					null,
					iDebitDay
			);
		} else {
			// 取扱データの読み出し
			editHandlingMst.handlingMst = HandlingMst.findById(id);
			// 編集
			editHandlingMst.handlingMst.handling_name = handling_name;
		}
		// Validate
		validation.valid(editHandlingMst.handlingMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		editHandlingMst.handlingMst.save();
		
		return 0;
	}
	
	/**
	 * HandlingMstの保存メソッド（クレジットカード用）
	 * @param id
	 * @param handling_name
	 * @param editHandlingMst
	 * @param sHandlingType
	 * @return
	 */
	private static Integer cf_handling_mst_save(
			Long id,
			String handling_name,
			EditHandlingMst editHandlingMst,
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
			editHandlingMst.handlingMst = new HandlingMst(
					haUser,
					handlingTypeMst,
					handling_name,
					debitBank,
					cutoff_day,
					debit_month,
					debit_day
			);
		} else {
			// 取扱データの読み出し
			editHandlingMst.handlingMst = HandlingMst.findById(id);
			// 編集
			editHandlingMst.handlingMst.handling_name = handling_name;
			editHandlingMst.handlingMst.debit_bank = debitBank;
			editHandlingMst.handlingMst.cutoff_day = cutoff_day;
			editHandlingMst.handlingMst.debit_month = debit_month;
			editHandlingMst.handlingMst.debit_day = debit_day;
		}
		// Validate
		validation.valid(editHandlingMst.handlingMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		editHandlingMst.handlingMst.save();
		
		return 0;
	}
	
	/**
	 * ItemMstの保存メソッド
	 * @param id
	 * @param item_name
	 * @param editItemMst
	 * @param sBalanceType
	 * @return
	 */
	private static Integer cf_item_mst_save(
			Long id,
			String item_name,
			EditItemMst editItemMst,
			String sBalanceType
			) {
		HaUser haUser = (HaUser)renderArgs.get("haUser");
		BalanceTypeMst balanceTypeMst = BalanceTypeMst.find("byBalance_type_name", sBalanceType).first();
		if(id == null) {
			// 取扱データの作成
			editItemMst.itemMst = new ItemMst(
					haUser,
					balanceTypeMst,
					item_name
			);
		} else {
			// 取扱データの読み出し
			editItemMst.itemMst = ItemMst.findById(id);
			// 編集
			editItemMst.itemMst.item_name = item_name;
		}
		// Validate
		validation.valid(editItemMst.itemMst);
		if(validation.hasErrors()) {
			return 1;
	    }
		// 保存
		editItemMst.itemMst.save();
		
		return 0;
	}
	
	/**
	 * 取扱種類を元にHandlingMstのリストの取得
	 * @param sHandlingType
	 * @return
	 */
	private static List<HandlingMst> get_handling_msts(String sHandlingType) {
		List<HandlingMst> handlingMsts = HandlingMst.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' and handling_type_mst.handling_type_name = '" + sHandlingType + "' order by id").fetch();
		return handlingMsts;
	}
	
	/**
	 * HandlingMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	static class EditHandlingMst {
		HandlingMst handlingMst;
	}

	/**
	 * ItemMst の参照渡し用クラス
	 * @author sakashushu
	 *
	 */
	static class EditItemMst {
		ItemMst itemMst;
	}
}
