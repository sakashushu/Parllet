package controllers;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import models.HaUser;
import models.Record;

import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Config extends Controller {

	public static void config() {
		render();
	}
	
	public static void upload(File csv) {
		if(csv != null) {
			File saveTo = new File("C:\\Saya\\" + csv.getName());
			csv.renameTo(saveTo);
		}

	}
	
	public static void download(
    		String down_date_fr,	/* 絞込日時範囲（開始） */
    		String down_date_to		/* 絞込日時範囲（終了） */
			) throws UnsupportedEncodingException {
		
		if((down_date_fr==null) || (down_date_fr.equals(""))) {
			down_date_fr = "1900/01/01";
		}
		if((down_date_to==null) || (down_date_to.equals(""))) {
			down_date_to = "2999/12/31";
		}
		
		String sOutCsv = "";
		List<Record> records = Record.find(" payment_date between '" + down_date_fr + "' and '" + down_date_to + "'").fetch();
		for(Record rec : records) {
			sOutCsv += "\"" + String.format("%1$tY/%1$tm/%1$td", rec.payment_date) + "\"" +
					System.getProperty("line.separator")	//改行
					;
		}
		
		response.setContentTypeIfNotSet("application/binary");
		java.io.InputStream binaryData = new ByteArrayInputStream(sOutCsv.getBytes("utf-8"));
		renderBinary(binaryData, "testName.csv");	
	}
}
