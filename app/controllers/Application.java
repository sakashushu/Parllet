package controllers;

import play.*;
import play.mvc.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void detailList(
    		String dateFr,	/* 絞込日時範囲（開始） */
    		String dateTo,	/* 絞込日時範囲（終了） */
    		List<Long> e_id,				/* 変更行のID */
    		List<String> e_payment_date,	/* 変更行の支払日 */
    		List<Integer> e_item_id,		/* 変更行の項目ＩＤ */
    		String srch,	/* 「絞込」ボタン */
    		String save		/* 「保存」ボタン */
    		) {
    	String strSrchSql = "";
    	if(dateFr == null) {
    		Calendar calendar = Calendar.getInstance();
    		dateTo = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
    		calendar.add(Calendar.YEAR, -1);
    		dateFr = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
    	}

    	List<Record> records = null;
    	
    	String dFr = dateFr;
    	String dTo = dateTo;
	    	
    	// 「保存」ボタンが押された場合
    	if(save != null) {
	    	if(save.equals("保存")) {
	    		// 更新
	    		String strSaveSql = "";
	    		
	    		Iterator<String> strEPayDt = e_payment_date.iterator();
	    		Iterator<Integer> intEItemId = e_item_id.iterator();
	    		for (Long lId : e_id) {
	    			strSaveSql = "id = " + lId;
	    			
	    			Record rec = Record.findById(lId);
	    			
	    			try {
						rec.payment_date = DateFormat.getDateInstance().parse(strEPayDt.next());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

	    			rec.item_id = intEItemId.next();
	    			
	    			// Validate
				    validation.valid(rec);
				    if(validation.hasErrors()) {
				    	// 以下の描画では駄目かも？
				        render(records, dFr, dTo);
				    }
				    
				    // 保存
				    rec.save();
				    
	    		}
	    		
	    		
	    		// 新規
	    		
	    	}
    	}

    	// 検索処理
   		strSrchSql += "payment_date between '" + dateFr + "' and '" + dateTo + "'";
    	strSrchSql += " order by payment_date desc";
    	records = Record.find(
    			strSrchSql).from(0).fetch(50);
    	
    	render(records, dFr, dTo);
    }
	
    public static void dtlSave(List<String> e_payment_date, List<Integer> e_item_id) {
    	String sQuely = "";
    	Iterator<Integer> iEItemId = e_item_id.iterator();
    	for (String str : e_payment_date) {
    		sQuely += "payment_date1 > '" + str + "' itemid(" + iEItemId.next() + ") ";
    	}
    	sQuely += " order by payment_date desc";
    	
//    	List<Record> records = Record.find(
//    			sQuely).from(0).fetch(50);
    	
//    	detailList(null, null, null);
    }
    
    public static void test() {
        render();
    }

}