package controllers;

import play.*;
import play.mvc.*;


import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import models.*;

import static play.mvc.Http.WebSocketEvent.TextFrame;

public class Application extends Controller {

    public static void index() {
        render();
    }

	public static void detailList(
    		String h_payment_date_fr,	/* 絞込日時範囲（開始） */
    		String h_payment_date_to,	/* 絞込日時範囲（終了） */
    		Long h_balance_type_id,  /* 絞込収支種類ID */
    		Integer h_item_id,	/* 絞込項目ＩＤ */
    		List<Long> e_id,				/* 変更行のID */
    		List<String> e_payment_date,	/* 変更行の支払日 */
    		List<Integer> e_item_id,		/* 変更行の項目ＩＤ */
    		List<String> n_payment_date,	/* 変更行の支払日 */
    		List<Integer> n_item_id,		/* 変更行の項目ＩＤ */
    		String srch,	/* 「絞込」ボタン */
    		String save		/* 「保存」ボタン */
    		) {
    	String strSrchRecSql = "";
    	if(h_payment_date_fr == null) {
    		Calendar calendar = Calendar.getInstance();
    		h_payment_date_to = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
    		h_payment_date_fr = String.format("%1$tY/%1$tm", calendar.getTime()) + "/01";
    	}

    	List<Record> records = null;
    	List<BalanceTypeMst> bTypes = null;
    	ActualTypeMst aTypeIn = null;
    	ActualTypeMst aTypeOut = null;
    	List<ItemMst> itemsIn = null;
    	List<ItemMst> itemsOut = null;
    	
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
	    				ItemMst item = ItemMst.findById(intEItemId.next());
	    				// 変更有無チェック用のレコードにセット
	    				Record eRec = new Record(
	    						DateFormat.getDateInstance().parse(strEPayDt.next()),
	    						rec.balance_type_mst,  //変更しない
	    						item,
	    						0,
	    						0,
	    						"",
	    						"",
	    						0,
	    						0,
	    						0,
	    						null,
	    						"",
	    						"",
	    						0);
	    				
		    			// Validate
					    validation.valid(eRec);
					    if(validation.hasErrors()) {
					    	// 以下の描画では駄目かも？
					    	render(bTypes, itemsIn, itemsOut, records, h_payment_date_fr, h_payment_date_to, h_balance_type_id, h_item_id);
					    }
	    				// 何れかの項目が変更されていた行だけ更新
	    				if (rec.payment_date != eRec.payment_date ||
	    						rec.item_mst != eRec.item_mst) {
							rec.payment_date = eRec.payment_date;
			    			rec.item_mst = eRec.item_mst;
						    
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


//			    // 新規作成
//	    		Iterator<Integer> intNItemId = n_item_id.iterator();
//	    		for (String spDt : n_payment_date) {
//	    			Record nRec = null;
//	    			try {
//	    				// 新規作成用のレコードにセット
//	    				nRec = new Record(
//	    						DateFormat.getDateInstance().parse(spDt),
//	    						intNItemId.next(),
//	    						"",
//	    						0,
//	    						"",
//	    						0,
//	    						"",
//	    						0,
//	    						0,
//	    						0,
//	    						"",
//	    						null,
//	    						"",
//	    						"",
//	    						"",
//	    						0,
//	    						"",
//	    						0,
//	    						"");
//	    				
//		    			// Validate
//					    validation.valid(nRec);
//					    if(validation.hasErrors()) {
//					    	// 以下の描画では駄目かも？
//					        render(records, h_payment_date_fr, h_payment_date_to, h_item_id);
//					    }
//					    // 保存
//					    nRec.save();
//					} catch (ParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//	    			// 作成後の行をそのまま戻す
//				    records.add(nRec);
//	    		}
//	    		
	    	}
	    	
	    	
	    	
    	} else {
	    	// 検索処理(BalanceTypeMst)
    		bTypes = BalanceTypeMst.find("order by id").fetch();

	    	// 検索処理(ItemMst(収入))
    		aTypeIn = ActualTypeMst.find("actual_type_name = '収入'").first();
    		itemsIn = ItemMst.find("byActual_type_mst", aTypeIn).fetch();
    		
	    	// 検索処理(ItemMst(支出))
    		aTypeOut = ActualTypeMst.find("actual_type_name = '支出'").first();
    		itemsOut = ItemMst.find("byActual_type_mst", aTypeOut).fetch();

    		// 検索処理(Record)
	    	//  日付範囲
	   		strSrchRecSql += "payment_date between '" + h_payment_date_fr + "' and '" + h_payment_date_to + "'";
	   		//  収支種類
	   		if(h_balance_type_id != null) {
	   			if(h_balance_type_id != 0) {
	   				strSrchRecSql += " and balance_type_mst.id = " + h_balance_type_id;
	   			}
	   		}
	    	//  項目
	   		if(h_item_id != null) {
	   			if(h_item_id != 0) {
	   				strSrchRecSql += " and item_mst.id = " + h_item_id;
	   			}
	   		}
	    	strSrchRecSql += " order by payment_date";
	    	records = Record.find(
	    			strSrchRecSql).from(0).fetch(50);
    	}

    	render(bTypes, itemsIn, itemsOut, records, h_payment_date_fr, h_payment_date_to, h_balance_type_id, h_item_id);
    }
	
	public static void test() {
        render();
    }
	
	public static class WebSocketApp extends WebSocketController {
		public static void listen() {
			// WebSocketが接続されている間、isbound.isOpen()はtrue
			while(inbound.isOpen()) {
				// クライアントから送られるメッセージを、継続を使って非同期で待ちます。
				Http.WebSocketEvent event = await(inbound.nextEvent());

				// メッセージがテキストであればfor内が実行されます。
				// パターンマッチにfor文を使うのは珍しいですね。
				for(String data : TextFrame.match(event)) {
					// クライアントにメッセージを返送します。(今のところ返送する意味はない。)
					outbound.send(data);
					
					ObjectMapper mapper = new ObjectMapper();
					// JSON文字列 を Bean に変換する
					RecSingleColumn eRec;
					try {
						eRec = mapper.readValue(data, RecSingleColumn.class);
						
//						// Bean の内容を標準出力に書き出す
//						System.out.println(bean);
//						// Bean を JSON文字列 に変換して標準出力に書き出す
//						mapper.writeValue(System.out, bean);

						// 変更
						if(eRec.act_type.equals("update")) {
							Record rec = Record.findById(eRec.id);
		    				
			    			// Validate
						    validation.valid(eRec);
						    if(validation.hasErrors()) {
						    	// 以下の描画では駄目かも？
//						        render(records, h_payment_date_fr, h_payment_date_to, h_item_id);
						    }
						    
						    // 支払日
						    if(eRec.edited_col.equals("payment_date")) {
			    				// 項目が変更されていた場合だけ更新
							    Date ePayDate = DateFormat.getDateInstance().parse(eRec.col_val);
			    				if (rec.payment_date != ePayDate) {
									rec.payment_date = ePayDate;
								    // 保存
								    rec.save();
			    				}
			    				
//						    // 収支種類
//						    } else if(eRec.edited_col.equals("balance_type_id")) {
//			    				// 項目が変更されていた場合だけ更新
//							    int eBTypeId = Integer.parseInt(eRec.col_val);
//			    				if (rec.balance_type_id != eBTypeId) {
//									rec.balance_type_id = eBTypeId;
//								    // 保存
//								    rec.save();
//			    				}
//						    	
						    }
						}
	    			} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	
}