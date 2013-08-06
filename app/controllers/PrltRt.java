package controllers;

import java.util.Calendar;
import java.util.Date;

import models.HaUser;
import models.LevelMst;
import models.WkSyEsFbUsRslt;
import play.i18n.Messages;
import play.mvc.*;

public class PrltRt extends Controller {

	public static void index() {
		if (LevelMst.count()==0L) {
			LevelMst lM = new LevelMst(0, "LV0", 0, 100);
			lM.save();
			lM = new LevelMst(1, "LV1", 100, 1000000);
			lM.save();
			lM = new LevelMst(3, "LV3", 300, 3000000);
			lM.save();
			lM = new LevelMst(5, "LV5", 500, 5000000);
			lM.save();
			lM = new LevelMst(10, "LV10", 1000, 10000000);
			lM.save();
		}
		
		render();
	}
	
	public static void validateHaUser(
    		String email,
    		String password
			) {
		WkSyEsFbUsRslt wr = new WkSyEsFbUsRslt();
		LevelMst lM = LevelMst.find("byLevel", 0).first();
//		Date dteNow = Calendar.getInstance().getTime();
		Common cm = new Common();
		Date dteNow = cm.locDate();
		HaUser haUser = new HaUser(email, password, null, "", null, null, null, true, false, false, false, false, lM, null, null, null, dteNow, dteNow);
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
		LevelMst lM = LevelMst.find("byLevel", 0).first();
//		Date dteNow = Calendar.getInstance().getTime();
		Common cm = new Common();
		Date dteNow = cm.locDate();
		HaUser haUser = new HaUser(email, password, null, null, null, null, null, true, false, false, false, false, lM, null, null, null, dteNow, dteNow);
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
	
}
