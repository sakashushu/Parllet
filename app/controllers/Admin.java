package controllers;

import java.util.List;

import models.HaUser;
import models.Record;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Admin extends Controller {
	
	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			HaUser haUser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("haUser", haUser);
		}
	}
	
	public static void index() {
//		List<Record> records = Record.find("ha_user = '" + ((HaUser)renderArgs.get("haUser")).id + "' order by payment_date").fetch();
		HaUser haUser  = (HaUser)renderArgs.get("haUser");
		List<Record> records = Record.find("ha_user = '" + haUser.id + "' order by payment_date").fetch();
		render(records);
	}
	
}
