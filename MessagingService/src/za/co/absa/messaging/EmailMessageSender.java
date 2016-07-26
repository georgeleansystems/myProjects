package za.co.absa.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.istack.internal.ByteArrayDataSource;


public class EmailMessageSender {
	
	public EmailMessageSender(Session session){
		this.session = session;
	}
	
	public EmailMessageSender() throws Exception{
		this.session = MailSessionFactory.getInstance().getSession();
	}
	
	private Session session;
	
	public void sendMessage(Message message) throws Exception{
		MimeMessage mailMessage = new MimeMessage(session);
		mailMessage.setFrom(new InternetAddress(message.getFrom()));

		if(message.getTo() != null && !message.getTo().isEmpty()){	
			
			mailMessage.setRecipients(javax.mail.Message.RecipientType.TO,toArray(message.getTo()));
		}

		if(message.getCc() != null && !message.getCc().isEmpty()){	
			mailMessage.setRecipients(javax.mail.Message.RecipientType.CC,toArray(message.getCc()));
		}

		if(message.getBcc() != null && !message.getBcc().isEmpty()){	
			mailMessage.setRecipients(javax.mail.Message.RecipientType.BCC,toArray(message.getBcc()));
		}

		mailMessage.setSubject(message.getSubject());
	    Multipart multipart = new MimeMultipart();

	    BodyPart mainBodyPart = new MimeBodyPart();
	    mainBodyPart.setContent(message.getBody(),message.getMime()+"; charset=utf-8");

	    multipart.addBodyPart(mainBodyPart);
	    
	    if(message.getAttachments() != null && !message.getAttachments().isEmpty()){
	    	for(Attachment attachment:message.getAttachments()){
	    		MimeBodyPart messageBodyPart = new MimeBodyPart();
	    	         DataSource source = new ByteArrayDataSource(attachment.getBytes(),attachment.getMime());
	    	         messageBodyPart.setDataHandler(new DataHandler(source));
	    	         messageBodyPart.setFileName(attachment.getFilename());
	    	         multipart.addBodyPart(messageBodyPart);
	    	}
	    }
	    mailMessage.setContent(multipart);
		Transport.send(mailMessage);

	}
	
	public static  Address[] toArray(List<String> addresses) throws AddressException{
		List<Address> addressesTemp = new ArrayList<Address>();
		
		for(String adress:addresses){
			Address[] addressesObj = InternetAddress.parse(adress);
			for(Address addressObj:addressesObj){
				addressesTemp.add(addressObj);
			}
		}
		
		return addressesTemp.toArray(new Address[addressesTemp.size()]);
	}
}
