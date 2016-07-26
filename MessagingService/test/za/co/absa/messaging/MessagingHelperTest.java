package za.co.absa.messaging;

import java.io.FileInputStream;

import javax.mail.Session;

import org.junit.Test;

import com.lowagie.text.pdf.codec.Base64;

import za.co.absa.messaging.EmailMessageSender;
import za.co.absa.messaging.MailSessionFactory;
import za.co.absa.messaging.Message;
import za.co.absa.messaging.base64.MessageWrapper;
import za.co.absa.messaging.base64.MessagingHelper;


public class MessagingHelperTest {
	
	@Test
	public void testSendMessage() throws Exception{
		
		//TODO: Create a mail handler class.
		FileInputStream fis1 = new FileInputStream("/Users/sellotseka/Documents/git-repos/lean-code/MessagingService/test/page_1_to_2.pdf");		
		byte[] bytes1 = new byte[fis1.available()];		
		fis1.read(bytes1);

		MessageWrapper message = new MessageWrapper();
		
		message
		.to("sello.tseka@leansystems.co.za")
		.from("sello.tseka@gmail.com")
		.subject("Hello - From NDE Mailer Wrapper")
		.body("Hello NDE Mailer Wrapper")
		.attach(Base64.encodeBytes(bytes1), Mime.PDF.getMime(), "page_1_to_2.pdf");

		message.setMime(Mime.TXT.getMime());
		
		MailSessionFactory instance = MailSessionFactory.getInstance();
		org.junit.Assert.assertNotNull(instance);
		
		Session session = instance.getSession();

		org.junit.Assert.assertNotNull(session);
		
		MessagingHelper messageSender = new MessagingHelper(session);
		
		messageSender.sendMessage(message);

	}

}
