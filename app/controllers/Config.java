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
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Config extends Controller {

	public static void cf_io() {
		render();
	}
	
	public static void upload(File csv) {
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
					String item_name = tokens[2].substring(1, tokens[2].length()-1);			//項目
					Integer amount = Integer.parseInt(tokens[3]);								//金額
					String handling_name = tokens[4].substring(1, tokens[4].length()-1);		//取扱
					String debit_date = tokens[5].substring(1, tokens[5].length()-1);			//引落日
					String content = tokens[6].substring(1, tokens[6].length()-1);				//内容
					String store = tokens[7].substring(1, tokens[7].length()-1);				//お店
					String remarks = tokens[8].substring(1, tokens[8].length()-1);				//備考
					
					remarks = "インポートテスト";
					
					String secret_remarks = tokens[9].substring(1, tokens[9].length()-1);		//備考（非公開）
					String ideal_deposit_name = tokens[10].substring(1, tokens[10].length()-1);	//My貯金
					
					try {
						Date paymentDate = null;
						if(payment_date!=null && !payment_date.equals("")) {  // 「payment_date!=null」だけでは「java.text.ParseException: Unparseable date: ""」
							paymentDate = DateFormat.getDateInstance().parse(payment_date);
						}
						HaUser haUser = HaUser.find("byEmail", Security.connected()).first();
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
	
	public static void download(
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
							sOutCsv += String.format("%1$tY/%1$tm/%1$td", fld.get(rec));							
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
	public static void cf_list_bank() {
		String sHandlingType = Messages.get("views.config.cf_bank");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
//		render(handlingMsts);
		render("@cf_list_any", sHandlingType, handlingMsts);
	}
	
	//クレジットカード編集（リスト）
	public static void cf_list_creca() {
		render();
	}
	
	//電子マネー編集（リスト）
	public static void cf_list_emoney() {
		String sHandlingType = Messages.get("views.config.cf_emoney");
		List<HandlingMst> handlingMsts = get_handling_msts(sHandlingType);
		render("@cf_list_any", sHandlingType, handlingMsts);
	}
	
	
	//項目編集（リスト）
	public static void cf_list_item() {
		render();
	}
	
	//口座編集
	public static void cf_edit_bank(Long id) {
		String sHandlingType = Messages.get("views.config.cf_bank");
		if(id != null) {
			HandlingMst handlingMst = HandlingMst.findById(id);
			render("@cf_edit_any", handlingMst, sHandlingType);
		}
		render("@cf_edit_any", sHandlingType);
	}
	
	//口座保存
	public static void cf_save_bank(Long id,
			@Required(message="名称 is required") String handling_name
			) {
		String sHandlingType = Messages.get("views.config.cf_bank");
		
		EditHandlingMst editHandlingMst = new EditHandlingMst();
		
		//口座保存
		Integer iRtn = cf_save_handling_mst(id, handling_name, editHandlingMst, sHandlingType);
		HandlingMst handlingMst = editHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(handlingMst);
			render("@cf_edit_any", handlingMst, sHandlingType);
		}
		
		cf_list_bank();
		
	}
	
	//口座削除
	public static void cf_del_bank(Long id) {
		// 取扱データの読み出し
		HandlingMst handlingMst = HandlingMst.findById(id);
		// 保存
		handlingMst.delete();

		cf_list_bank();
	}
	
	//電子マネー編集
	public static void cf_edit_emoney(Long id) {
		String sHandlingType = Messages.get("views.config.cf_emoney");
		if(id != null) {
			HandlingMst handlingMst = HandlingMst.findById(id);
			render("@cf_edit_any", handlingMst, sHandlingType);
		}
		render("@cf_edit_any", sHandlingType);
	}
	

	//電子マネー保存
	public static void cf_save_emoney(Long id,
			@Required(message="名称 is required") String handling_name
			) {
		String sHandlingType = Messages.get("views.config.cf_emoney");
		
		EditHandlingMst editHandlingMst = new EditHandlingMst();
		
		//口座保存
		Integer iRtn = cf_save_handling_mst(id, handling_name, editHandlingMst, sHandlingType);
		HandlingMst handlingMst = editHandlingMst.handlingMst;
		
		if(iRtn == 1) {
			validation.clear();
			validation.valid(handlingMst);
			render("@cf_edit_any", handlingMst, sHandlingType);
		}
		
		cf_list_emoney();
		
	}
	
	//電子マネー削除
	public static void cf_del_emoney(Long id) {
		// 取扱データの読み出し
		HandlingMst handlingMst = HandlingMst.findById(id);
		// 保存
		handlingMst.delete();

		cf_list_emoney();
	}
	
	/**
	 * HandlingMstの保存メソッド
	 * @param id
	 * @param handling_name
	 * @param editHandlingMst
	 * @param sHandlingType
	 * @return
	 */
	private static Integer cf_save_handling_mst(Long id,
			String handling_name,
			EditHandlingMst editHandlingMst,
			String sHandlingType
			) {
		HaUser haUser = HaUser.find("byEmail", Security.connected()).first();
		HandlingTypeMst handlingTypeMst = HandlingTypeMst.find("byHandling_type_name", sHandlingType).first();
		if(id == null) {
			// 取扱データの作成
			editHandlingMst.handlingMst = new HandlingMst(
					haUser,
					handlingTypeMst,
					handling_name
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
	 * 取扱種類を元にHandlingMstのリストの取得
	 * @param sHandlingType
	 * @return
	 */
	private static List<HandlingMst> get_handling_msts(String sHandlingType) {
		List<HandlingMst> handlingMsts = HandlingMst.find("handling_type_mst.handling_type_name = '" + sHandlingType + "'").fetch();
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
}
