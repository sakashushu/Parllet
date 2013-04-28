package controllers;

import models.HaUser;
import models.WkSyEsFbUsRslt;
import play.i18n.Messages;
import play.mvc.*;

public class MyDpRt extends Controller {

	public static void index() {
		render();
	}
	
	public static void validateHaUser(
    		String email,
    		String password
			) {
		WkSyEsFbUsRslt wr = new WkSyEsFbUsRslt();
		HaUser haUser = new HaUser(email, password, null, "", null, null, null, true, false);
		// Validate
	    validation.valid(haUser);
	    if(validation.hasErrors()) {
	    	if(validation.errors().get(0).getKey().equals("haUser.email"))
	    		wr.setIntRslt(1);
	    	if(validation.errors().get(0).getKey().equals("haUser.password"))
	    		wr.setIntRslt(2);
			wr.setStrErr(Messages.get(validation.errors().get(0).message()));
			renderJSON(wr);
	    }
   		wr.setIntRslt(0);
		wr.setStrEmail(haUser.email);
		wr.setStrPassword(haUser.password);
		renderJSON(wr);
	}
	
	public static void signup(
    		String email,
    		String password
			) {
		WkSyEsFbUsRslt wr = new WkSyEsFbUsRslt();
		HaUser haUser = new HaUser(email, password, null, null, null, null, null, true, false);
		// Validate
	    validation.valid(haUser);
	    if(validation.hasErrors()) {
    		wr.setIntRslt(99);
	    	if(validation.errors().get(0).getKey().equals("haUser.email"))
	    		wr.setIntRslt(1);
	    	if(validation.errors().get(0).getKey().equals("haUser.password"))
	    		wr.setIntRslt(2);
			wr.setStrErr(Messages.get(validation.errors().get(0).message()));
			renderJSON(wr);
	    }
		haUser.save();
		Common cm = new Common();
		try {
			cm.initUserConf(haUser);
		} catch (Exception e) {
			wr.setIntRslt(99);
			wr.setStrErr(Messages.get("err.unanticipated", "initUserConf"));
			renderJSON(wr);
		}
		wr.setIntRslt(0);
		wr.setStrEmail(haUser.email);
		wr.setStrPassword(haUser.password);
		renderJSON(wr);
	}
	
	
	public static void test() {
		render();
	}
	
}
