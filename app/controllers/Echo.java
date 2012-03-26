package controllers;

import models.LoggedMessage;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.WebSocketController;

import static play.mvc.Http.WebSocketEvent.TextFrame;

public class Echo extends Controller {
	public static void demo() {
		render();
	}
	
	public static class WebSocketEcho extends WebSocketController {
		public static void listen() {
			// WebSocketが接続されている間、isbound.isOpen()はtrue
			while(inbound.isOpen()) {
				// クライアントから送られるメッセージを、継続を使って非同期で待ちます。
				Http.WebSocketEvent event = await(inbound.nextEvent());
				Http.WebSocketFrame frame = (Http.WebSocketFrame)event;

				// メッセージがテキストであればfor内が実行されます。
				// パターンマッチにfor文を使うのは珍しいですね。
				for(String message : TextFrame.match(event)) {
					// クライアントにメッセージを返送します。
					outbound.send(message);
					//outbound.send("Echo: %s", frame.textData);
					
					// 本題のechoサーバとは何の関係もありませんが、
					// このように、HTTP用のControllerから利用していたModelをWebSocketControllerからも普通に利用することができます。Interoperability!
					new LoggedMessage(message).save();
				}
			}
		}
	}
}
