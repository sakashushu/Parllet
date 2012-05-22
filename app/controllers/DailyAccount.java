package controllers;

import java.util.Calendar;

import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class DailyAccount extends Controller {

	public static void form(
			Integer year,
			Integer month,
			Integer thisMonthFlg
			) {
   		Calendar calendar = Calendar.getInstance();
		if((year==null)||(month==null)) {
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH) + 1;
		}
		if((year==calendar.get(Calendar.YEAR))&&
    	   (month==calendar.get(Calendar.MONTH)+1)) {
			thisMonthFlg = 1;
		} else {
			thisMonthFlg = 0;
		}
		render(year, month, thisMonthFlg);
	}
	
	public static void jump(
			Integer year,
			Integer month,
    		String prevYear,	/* 「<<」ボタン */
    		String prevMonth,	/* 「<」ボタン */
    		String nextMonth,	/* 「>」ボタン */
    		String nextYear,	/* 「>>」ボタン */
    		String thisMonth	/* 「今月」ボタン */
			) {

		Calendar calendar = Calendar.getInstance();
    	// 「今月」ボタンが押された場合
    	if(thisMonth != null) {
    		form(null, null, null);
    	} else {
			calendar.set(year, month - 1, 1);
	    	// 「<<」ボタンが押された場合
	    	if(prevYear != null) {
	    		calendar.add(Calendar.YEAR, -1);
				
	    	// 「<」ボタンが押された場合
	    	} else if(prevMonth != null) {
	    		calendar.add(Calendar.MONTH, -1);

	    	// 「>」ボタンが押された場合
	    	} else if(nextMonth != null) {
	    		calendar.add(Calendar.MONTH, 1);

	    	// 「>>」ボタンが押された場合
	    	} else if(nextYear != null) {
	    		calendar.add(Calendar.YEAR, 1);

	    	}
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH) + 1;
	    		
	    	form(year, month, null);
    	}
	}
}
