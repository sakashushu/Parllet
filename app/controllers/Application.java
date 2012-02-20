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

    public static void detailList(String dateFr, String dateTo) {
    	String sQuely = "";
    	if(dateFr == null) {
    		Calendar calendar = Calendar.getInstance();
    		dateTo = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
    		calendar.add(Calendar.YEAR, -1);
    		dateFr = String.format("%1$tY/%1$tm/%1$td", calendar.getTime());
    	}
   		sQuely = "payment_date between '" + dateFr + "' and '" + dateTo + "'";
    	sQuely += " order by payment_date desc";
    	
    	List<Record> records = Record.find(
    			sQuely).from(0).fetch(50);
    	
    	String dFr = dateFr;
    	String dTo = dateTo;
    	
    	render(records, dFr, dTo);
    }
	
    public static void dtlSave(String e_payment_date) {
    	String sQuely = "";
   		sQuely = "payment_date1 > '" + e_payment_date + "'";
    	sQuely += " order by payment_date desc";
    	
    	List<Record> records = Record.find(
    			sQuely).from(0).fetch(50);
    	
    	detailList(null, null);
    }
    
}