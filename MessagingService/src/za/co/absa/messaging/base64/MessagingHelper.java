package za.co.absa.messaging.base64;

import javax.mail.Session;

import za.co.absa.messaging.EmailMessageSender;

public class MessagingHelper {
	private  EmailMessageSender messageSender;
	
	public MessagingHelper(Session session) {
		messageSender = new EmailMessageSender(session);
	}

	public void sendMessage(MessageWrapper messageWrapper) throws Exception{
		messageSender.sendMessage(messageWrapper.toMessage());
	}

}
