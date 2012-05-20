package controllers;

import java.util.Calendar;

import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class DailyAccount extends Controller {

	public static void form(String yearMonth) {
		if(yearMonth == null) {
    		Calendar calendar = Calendar.getInstance();
    		yearMonth = String.format("%1$tY年%1$tm月", calendar.getTime());
		}
		render(yearMonth);
	}
}
