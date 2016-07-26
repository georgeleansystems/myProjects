package za.co.absa.messaging;

import java.io.FileInputStream;

import javax.mail.Session;

import org.junit.Test;

import za.co.absa.messaging.EmailMessageSender;
import za.co.absa.messaging.MailSessionFactory;
import za.co.absa.messaging.Message;


public class MessageTest {
	
	@Test
	public void testGetInstance() throws Exception{
		
		//TODO: Create a mail handler class.
		FileInputStream fis1 = new FileInputStream("/Users/sellotseka/Documents/git-repos/lean-code/MessagingService/test/page_1_to_2.pdf");		
		byte[] bytes1 = new byte[fis1.available()];		
		fis1.read(bytes1);

		Message message = new Message();
		
		message
		.to("sello.tseka@leansystems.co.za")
		.from("sello.tseka@gmail.com")
		.subject("Hello - From NDE Mailer")
		.body("Hello NDE Mailer")
		.attach(bytes1, Mime.PDF.getMime(), "page_1_to_2.pdf");

		message.setMime(Mime.TXT.getMime());
		
		MailSessionFactory instance = MailSessionFactory.getInstance();
		org.junit.Assert.assertNotNull(instance);
		
		Session session = instance.getSession();

		org.junit.Assert.assertNotNull(session);
		
		EmailMessageSender messageSender = new EmailMessageSender(session);
		
		messageSender.sendMessage(message);

	}

}
