package controllers;

import models.HaUser;
import models.WkSyEsFbUsRslt;
import play.Play;
import play.i18n.Messages;
import play.mvc.*;

public class Application extends Controller {

	public static void index() {
		String strDomain = Play.configuration.getProperty("site.domain");
		if (!request.domain.equals(strDomain)) {
			redirect("http://"+strDomain+"/");
		}
		render();
	}
	
	public static void test() {
		render();
	}
}
