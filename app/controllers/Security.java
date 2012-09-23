package controllers;

import play.mvc.Before;
import models.*;


public class Security extends Secure.Security {
	
	static boolean authenticate(String email, String password) {
		return HaUser.connect(email, password) != null;
	}
	
	static void onDisconnected() {
		Application.index();
	}
	
	static void onAuthenticated() {
		DailyAccount.dailyAccount(null);
//		DetailList.detailList(1, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	
	static boolean check(String profile) {
		if("admin".equals(profile)) {
			return HaUser.find("byEmail", connected()).<HaUser>first().isAdmin;
		}
		return false;
	}
	
	static String getUserFullname() {
		if(HaUser.find("byEmail", connected()).<HaUser>first().fullname ==null) {
			return "null";
		} else {
			return "not null";
		}
		//return HaUser.find("byEmail", connected()).<HaUser>first().fullname;
	}
}
