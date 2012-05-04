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
			HaUser hauser  = HaUser.find("byEmail", Security.connected()).first();
			renderArgs.put("user", hauser.fullname);
		}
	}
	
	public static void index() {
		String haUser = Security.connected();
		List<Record> records = Record.find("ha_user.email", haUser).fetch();
		render(records);
	}
}
