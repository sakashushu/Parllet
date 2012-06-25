package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.BalanceTypeMst;
import models.HaUser;
import models.HandlingMst;
import models.IdealDepositMst;
import models.ItemMst;
import models.Record;

import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Config extends Controller {

	public static void config() {
		render();
	}
	
	public static void upload(File csv) {
		if(csv != null) {
			// FileReaderクラスをインスタンス化
            FileReader fr;
			try {
				fr = new FileReader(csv);
	            // BufferedReaderクラスをインスタンス化
	            BufferedReader br = new BufferedReader(fr);
	
	            String str = null;
	
	            // ファイルを1行ずつ読み込む
				while ( ( str = br.readLine() ) != null ) {
				    System.out.println( str );
				}
	
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
			
			//「家計簿ユーザー」・「単価」・「数量」は無視
			if(fldHd.getName().equals("ha_user") ||
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
				
				//「家計簿ユーザー」・「単価」・「数量」は無視
				if(fld.getName().equals("ha_user") ||
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
		//java.io.InputStream binaryData = new ByteArrayInputStream(sOutCsv.getBytes("utf-8"));
		java.io.InputStream binaryData = new ByteArrayInputStream(sOutCsv.getBytes("sjis"));
		Calendar calendar = Calendar.getInstance();
		renderBinary(binaryData, String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS", calendar.getTime()) + ".csv");	
	}
}
