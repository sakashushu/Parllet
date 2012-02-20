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
    		List<String> e_payment_date,	/* 変更行の支払日 */
    		List<Integer> e_item_id,		/* 変更行の項目ＩＤ */
    		String srch,	/* 「絞込」ボタン */
    		String save		/* 「保存」ボタン */
    		) {
    	String sQuely = "";
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
    	if(save == "保存") {
    		
    	}

    	// 検索処理
   		sQuely = "payment_date between '" + dateFr + "' and '" + dateTo + "'";
    	sQuely += " order by payment_date desc";
    	records = Record.find(
    			sQuely).from(0).fetch(50);
    	
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
    
}