package notifiers;

import org.apache.commons.mail.EmailAttachment;

import models.HaUser;
import play.Play;
import play.mvc.Mailer;

public class Mails extends Mailer {
	public static void welcome(HaUser hU) {
		setSubject("Welcome %s", hU.email);
		addRecipient(hU.email);
		setFrom("Parlletサポート <support@parllet.com>");
//		EmailAttachment attachment = new EmailAttachment();
//		attachment.setDescription("A pdf document");
//		attachment.setPath(Play.getFile("rules.pdf").getPath());
//		addAttachment(attachment);
		send(hU);
	}

}
