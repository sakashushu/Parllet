package controllers;


import com.google.gson.JsonObject;

import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.Crypto;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Http;
import play.mvc.results.Redirect;
import models.*;


public class Security extends Secure.Security {

//	private static final String BR = System.getProperty("line.separator");
	
	public static void text2(String id) {
		String[] result = {"エッ？", "オハヨー！", "コンニチワ！", "バイバーイ♪" };
		
		renderJSON(result["abc".indexOf(id)+1]);
	}
	
	/**
	 * Facebook連携したユーザーの存在チェック
	 * @param id
	 */
	public static void checkFbUser(Long id) {
		WkSyEsFbUsRslt wr = new WkSyEsFbUsRslt();
		HaUser hU = HaUser.find("byFbId", id).first();
		if(hU==null) {
			//アカウントが作られていない場合
			wr.setBolVirginFlg(true);
			renderJSON(wr);
		}
		//既にアカウントがある場合
		wr.setBolVirginFlg(false);
		wr.setStrEmail(hU.email);
		wr.setStrPassword(hU.password);
		renderJSON(wr);
	}
	
	/**
	 * 無ければ（初回は）Facebook情報からユーザを作成
	 * @param id
	 * @param email
	 * @param password
	 * @param nickname
	 */
	public static void ensureFbUser(Long id, String name, String link, String email) {
		WkSyEsFbUsRslt wr = new WkSyEsFbUsRslt();
		
//		//email重複チェック
//		HaUser huEmail = HaUser.find("byEmail", email).first();
//		if(huEmail!=null) {
//			wr.setIntRslt(2);
//			wr.setStrErr(Messages.get("views.login.err.duplicate",
//							Messages.get("views.login.fbLogin.email")));
//			renderJSON(wr);
//		}
		
		String strPass = String.valueOf(id);
		StringBuffer sb = new StringBuffer(strPass);
		strPass = String.valueOf(sb.reverse());
		HaUser haUser = new HaUser(email, strPass, null, null, id, name, link, false, false, false, false, false, null);
		// Validate
	    validation.valid(haUser);
	    if(validation.hasErrors()) {
    		wr.setIntRslt(99);
	    	if(validation.errors().get(0).getKey().equals("haUser.email"))
	    		wr.setIntRslt(1);
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
	
	@Before
	static void addDefaults(){
    	renderArgs.put("fbAppId", Play.configuration.getProperty("fb.app_id"));
	}
	static boolean authenticate(
			String username,
			String password
			) {
		return HaUser.connect(username, password) != null;
	}
	
	static void onDisconnected() {
		MyDpRt.index();
	}
	
	static void onAuthenticated() {
		//セッションに保持する値の初期化
		Security sec = new Security();
		sec.initializeSessionValue();
		
		//初期画面は残高表
		DailyAccount.balanceTable(null);
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
