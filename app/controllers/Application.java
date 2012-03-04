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
	    		Iterator<String> strEPayDt = e_payment_date.iterator();
	    		Iterator<Integer> intEItemId = e_item_id.iterator();
	    		for (Long lId : e_id) {
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
	
    public static void dtlSave(
    		Long e_id,				/* 変更行のID */
    		String e_payment_date	/* 変更行の支払日 */
    		) {
		// 更新
//		Record rec = Record.findById(e_id);
		Record rec = Record.find("id1 = " + e_id).first();
		try {
			rec.payment_date = DateFormat.getDateInstance().parse(e_payment_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    // 保存
	    rec.save();
    }
    
    public static void test() {
        render();
    }

    
	/**
	 * グリッドデータをロードする。
	 */
	public static void load() {
//	    List l = new ArrayList<AryRecord>();
//	    long count = 0;
//	    if (shainId != null && !shainId.equals("")) {
//	        count = Record.count("byShainId", shainId);
//	        l.addAll(Record.find("byShainId", shainId).fetch(page, rows));
//	    } else if (shainMei != null && !shainMei.equals("")) {
//	        count = Record.count("byShainMeiLike", "%" + shainMei + "%");
//	        l.addAll(Record.find("byShainMeiLike", "%" + shainMei + "%").fetch(page, rows));
//	    } else {
//	        count = Record.findAll().size();
//	        l.addAll(Record.all().fetch(page, rows));
//	    }
//	    // データをJson形式に変換する
//	    renderJSON(Common.readJson(l, page, count, rows));
		List<Record> rec = Record.findAll();
		
		List<String[]> list = new ArrayList<String[]>();
		String[] strList = {"\"payment_date\":\"2011/03/02\"", "\"item_id\":3", "\"item_name\":\"交通費\""};
		list.add(strList);
		
		List<ARecord> aList = new ArrayList<ARecord>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		for (int i = 0; i < rec.size(); i++) {
			ARecord arec = new ARecord();
			arec.setPayment_date(sdf.format(rec.get(i).payment_date));
			arec.setItem_id(rec.get(i).item_id);
			arec.setItem_name(rec.get(i).item_name);
			aList.add(arec);
		}
		
		renderJSON(aList);
	}

    
//	/**
//	 * グリッドデータをロードする。
//	 */
//	public static void load(int total, int page, int records, int rows,
//	        String shainId, String shainMei) {
//	    List l = new ArrayList<AryRecord>();
//	    long count = 0;
//	    if (shainId != null && !shainId.equals("")) {
//	        count = Record.count("byShainId", shainId);
//	        l.addAll(Record.find("byShainId", shainId).fetch(page, rows));
//	    } else if (shainMei != null && !shainMei.equals("")) {
//	        count = Record.count("byShainMeiLike", "%" + shainMei + "%");
//	        l.addAll(Record.find("byShainMeiLike", "%" + shainMei + "%").fetch(page, rows));
//	    } else {
//	        count = Record.findAll().size();
//	        l.addAll(Record.all().fetch(page, rows));
//	    }
//	    // データをJson形式に変換する
//	    renderJSON(Common.readJson(l, page, count, rows));
//	
//	}
//	
//	/**
//	 * グリッドデータを保存する。
//	 * @param body
//	 */
//	public static void save(String body) {
//	    // グリッド→エンティティ変換にjsonicを利用
//	    Record[] data = JSON.decode(body, Record[].class);
//	    for (int i = 0; i < data.length; i++) {
//	        // jpaの問題か？エンティティを再取得しなければデータを更新できないので検索処理を実行
//	        Record e = Record.findById(data[i].id);
//	        if (e != null) {
//	            Common.Update(data[i], AryRecord.class.toString());
//	            Common.copyField(data[i], e);
//	            e.save();
//	        }
//	    }
//	}
    
}