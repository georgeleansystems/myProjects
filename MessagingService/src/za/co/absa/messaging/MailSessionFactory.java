package za.co.absa.messaging;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class MailSessionFactory {
	
	private static MailSessionFactory instance = new MailSessionFactory();

	public static MailSessionFactory getInstance() {
		return instance;
	}

	public Session getSession() throws Exception{
		final Properties props = new Properties();
		
		props.load(getClass().getClassLoader().getResourceAsStream("smtp.properties"));

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return 
						new PasswordAuthentication(
							props.getProperty("mail.smtp.username"),
							props.getProperty("mail.smtp.password"));
				}
			});

		return session;
	}

	
}
