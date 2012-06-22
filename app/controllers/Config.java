package controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;

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
	
	public static void download() throws UnsupportedEncodingException {
		//response.contentType = "text/csv";
//		final Record record = Record.findById(412); 
		response.setContentTypeIfNotSet("application/binary");
		java.io.InputStream binaryData = new ByteArrayInputStream("Hello日本語".getBytes("utf-8"));
		renderBinary(binaryData);	
	}
}
