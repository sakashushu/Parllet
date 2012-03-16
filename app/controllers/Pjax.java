package controllers;

import java.util.ArrayList;
import java.util.List;

import play.mvc.Controller;

public class Pjax extends Controller {
	public static void index() {
		String data1 = params.get("data1");
		
		
		if(data1 == null) {
			render();
		} else {
			List<String> results = new ArrayList<String>();
			for(int i = 0; i < 10; i++) {
				results.add("data" + i);
			}
			
			if(isPjax()) {
				render("/tags/result.html", results);
			} else {
				results.add("directCalled");
				render(results);
			}
		}
	}
	
	private static boolean isPjax() {
		if(params._contains("_pjax")) {
			return true;
		} else if(request.headers.containsKey("X-PJAX")) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void test() {
		List<String> results = new ArrayList<String>();
		results.add("test1");
		results.add("test2");
		render(results);
	}
}
