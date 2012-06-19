package controllers;

import java.io.File;

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
}
