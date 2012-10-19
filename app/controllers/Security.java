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
		//セッションに保持する値の初期化
		Security sec = new Security();
		sec.initializeSessionValue();
		
		//初期画面は日計表
		DailyAccount.dailyAccount(null);
	}
	
	static boolean check(String profile) {
		if("admin".equals(profile)) {
			return HaUser.find("byEmail", connected()).<HaUser>first().isAdmin;
		}
		return false;
	}
	
	/**
	 * セッションに保持する値の初期化
	 */
	void initializeSessionValue() {
		session.put("actionMode", "View");		//actionModeは閲覧モード
		session.put("detailMode", "Balance");	//detailModeは収支モード
	}
}
