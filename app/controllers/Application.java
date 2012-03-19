package controllers;

import play.*;
import play.mvc.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import net.arnx.jsonic.JSON;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

	public static void detailList(
    		String h_payment_date_fr,	/* 絞込日時範囲（開始） */
    		String h_payment_date_to,	/* 絞込日時範囲（終了） */
    		Integer h_item_id,	/* 絞込項目ＩＤ */
    		List<Long> e_id,				/* 変更行のID */
    		List<String> e_payment_date,	/* 変更行の支払日 */
    		List<Integer> e_item_id,		/* 変更行の項目ＩＤ */
    		String srch,	/* 「絞込」ボタン */
    		String save		/* 「保存」ボタン */
    		) {
    	String strSrchSql = "";
    	if(h_payment_date_fr == null) {
    		Calendar calendar = Calendar.getInstance();
    		h_payment_date_to = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
    		h_payment_date_fr = String.format("%1$tY/%1$tm", calendar.getTime()) + "/01";
    	}

    	List<Record> records = null;
    	
    	// 「保存」ボタンが押された場合
    	if(save != null) {
	    	if(save.equals("保存")) {
			    records = new ArrayList<Record>();
	    		// 更新
	    		Iterator<String> strEPayDt = e_payment_date.iterator();
	    		Iterator<Integer> intEItemId = e_item_id.iterator();
	    		for (Long lId : e_id) {
	    			Record rec = Record.findById(lId);
	    			try {
	    				// 変更有無チェック用のレコードにセット
	    				Record eRec = new Record(
	    						DateFormat.getDateInstance().parse(strEPayDt.next()),
	    						intEItemId.next(),
	    						"",
	    						0,
	    						"",
	    						0,
	    						"",
	    						0,
	    						0,
	    						0,
	    						"",
	    						null,
	    						"",
	    						"",
	    						"",
	    						0,
	    						"",
	    						0,
	    						"");
	    				
		    			// Validate
					    validation.valid(eRec);
					    if(validation.hasErrors()) {
					    	// 以下の描画では駄目かも？
					        render(records, h_payment_date_fr, h_payment_date_to, h_item_id);
					    }
	    				// 何れかの項目が変更されていた行だけ更新
	    				if (rec.payment_date != eRec.payment_date ||
	    						rec.item_id != eRec.item_id) {
							rec.payment_date = eRec.payment_date;
			    			rec.item_id = eRec.item_id;
						    
						    // 保存
						    rec.save();
	    				}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

	    			// 編集後の行をそのまま戻す
				    records.add(rec);
	    		}
	    	}
    	} else {
	    	// 検索処理
	    	//  日付範囲
	   		strSrchSql += "payment_date between '" + h_payment_date_fr + "' and '" + h_payment_date_to + "'";
	    	//  項目
	   		if(h_item_id != null) {
	   			if(h_item_id != 0) {
	   				strSrchSql += " and item_id = " + h_item_id;
	   			}
	   		}
	    	strSrchSql += " order by payment_date";
	    	records = Record.find(
	    			strSrchSql).from(0).fetch(50);
    	}

    	render(records, h_payment_date_fr, h_payment_date_to, h_item_id);
    }
	
	public static void test() {
        render();
    }
}