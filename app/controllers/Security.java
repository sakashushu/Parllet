package controllers;

import models.*;


public class Security extends Secure.Security {
	
	static boolean authenticate(String email, String password) {
		return HaUser.connect(email, password) != null;
	}
	
	static void onDisconnected() {
		Application.index();
	}
	
	static void onAuthenticated() {
		DailyAccount.form(null, null, null);
	}
	
	static boolean check(String profile) {
		if("admin".equals(profile)) {
			return HaUser.find("byEmail", connected()).<HaUser>first().isAdmin;
		}
		return false;
	}
}
