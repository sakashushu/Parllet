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

    public static void detailList(String yearFr) {
    	
//    	Date date = null;
//		try {
//			date = DateFormat.getDateInstance().parse("2012/2/13");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	Record record = new Record(date, 1, "食費");
//    	record.save();
    	
    	String sQuely = "";
    	if(yearFr == null) {
    		Calendar calendar = Calendar.getInstance();
    		sQuely = "payment_date >= '" + String.format("%1$tY/%1$tm/%1$td", calendar.getTime()) + "'";
    	} else {
    		sQuely = "payment_date >= '" + yearFr + "/01/01'";
    	}
    	sQuely += " order by payment_date desc";
    	
    	List<Record> records = Record.find(
    			sQuely).from(0).fetch(50);
    	
    	render(records, yearFr);
    }
	
    public static void dtlSrch(String yearFr) {
    }
}