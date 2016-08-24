package V1_0;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbXMLNSC;
import com.ibm.misc.BASE64Encoder;
import com.sun.mail.pop3.POP3SSLStore;

public class MF_NDE_MailProcessor_JavaCompute extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		MbMessage outMessage = new MbMessage();
		outAssembly = new MbMessageAssembly(inAssembly,	outMessage);
		copyMessageHeaders(inMessage, outMessage);

		try{
			// Get user defined properties / configuration for mail server
			String mode 	= (String)getUserDefinedAttribute("mode");//Allowed values: "MOCK", "OFFICE_365", "LIVE"
																	  //MOCK - Ignores settings and returns mocked response. No POP3 required.
																	  //OFFICE_365 - Uses a cloudbased office365 account : 
																	  //LIVE - Uses your supplied Pop3 settings

			String readOnly 	= (String)getUserDefinedAttribute("readOnly");//Y N
			String protocol 	= (String)getUserDefinedAttribute("protocol");
			String host 		= (String)getUserDefinedAttribute("host");
			String userName 	= (String)getUserDefinedAttribute("username");
			String password 	= (String)getUserDefinedAttribute("password");
			String deleteEmail 	= (String)getUserDefinedAttribute("deleteEmail");//Y N
			String port 	= (String)getUserDefinedAttribute("port");
			
			MessagePoller poller = null;
			if("OFFICE_365".equals(mode)){
				poller = new Office365Poller();
			}
			else {
				poller = new MockMessagePoller();
			}
			
			List<MessageWrapper> messages = poller.pollForMessages();

			if(messages != null && !messages.isEmpty()){
				MbElement outRoot = outMessage.getRootElement();
	
				int i = 1;
				MbElement outBody = outRoot.createElementAsLastChild(MbXMLNSC.PARSER_NAME);
				MbElement mails = outBody.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"MAILS",null);
				mails.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "NUMBER_OF_EMAILS", messages.size());

				for(MessageWrapper message : messages){
					MbElement mail = mails.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"MAIL",null);
					mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_GUID", UUID.randomUUID().toString());
					mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_SUBJECT", message.getSubject());
					mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_FROM", message.getFrom());
					
					if(message.getTo() != null && !message.getTo().isEmpty()){
						MbElement toElement = mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"TO",null);
						for(String to:message.getTo()){
							toElement.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_TO", to);
						}
					}
					if(message.getCc() != null && !message.getCc().isEmpty()){
						MbElement ccElement = mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"CC",null);
						for(String cc:message.getCc()){
							ccElement.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_CC", cc);
						}
					}
					mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_BODY", message.getBody());
					mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_MIME", message.getMime());
					
					if(message.getAttachments() != null && !message.getAttachments().isEmpty()){
						MbElement attachmentsElements = mail.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"ATTACHMENTS",null);
						for(AttachmentWrapper aw:message.getAttachments()){
							MbElement attachmentElement = attachmentsElements.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"ATTACHMENT",null);
							attachmentElement.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_ATTACHMENT_GUID", UUID.randomUUID().toString());
							attachmentElement.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_ATTACHMENT_MIME", aw.getMime());
							attachmentElement.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MAIL_ATTACHMENT_BYTES", aw.getBase64Bytes());
						}
					}
				}
			}
		}
		catch(Exception e){
			MbMessage globalEnv = inAssembly.getGlobalEnvironment();
		    MbElement Environment = globalEnv.getRootElement();
		    MbElement errorHandling = Environment.createElementAsLastChild(MbElement.TYPE_NAME, "ErrorHandling", null);
		    MbElement java = errorHandling.createElementAsLastChild(MbElement.TYPE_NAME, "Java", null);
		    java.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "ErrorMessage", e.getMessage());

		    StringWriter sw = new StringWriter();
		    PrintWriter pw = new PrintWriter(sw);
		    e.printStackTrace(pw);

		    java.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "StackTrace", sw.toString());
		}
		finally{
			
			out.propagate(outAssembly);
		}

		// The following should only be changed
		// if not propagating message to the 'out' terminal
	}

	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage)
			throws MbException {
		MbElement outRoot = outMessage.getRootElement();

		// iterate though the headers starting with the first child of the root
		// element
		MbElement header = inMessage.getRootElement().getFirstChild();
		while (header != null && header.getNextSibling() != null) // stop before
																	// the last
																	// child
																	// (body)
		{
			// copy the header and add it to the out message
			outRoot.addAsLastChild(header.copy());
			// move along to next header
			header = header.getNextSibling();
		}
	}

	class MockMessagePoller implements MessagePoller{
		@Override
		public List<MessageWrapper> pollForMessages()
				throws MessagingException, IOException {
			List<MessageWrapper> messages = new ArrayList<MessageWrapper>();
			
			for(int i = 1;i<=5;i++){
				messages.add(createMockMessage(i));
			}
			return messages;
		}

		private MessageWrapper createMockMessage(int i) {
			String encodedHello = new BASE64Encoder().encode("HELLO".getBytes());
			
			MessageWrapper wrap = new MessageWrapper("test"+i+"@test.co.za",
					"cctest"+i+"@test.co.za",
					"test"+i+"@test.co.za",
					"",
					"HELLO"+i,
					encodedHello);
			wrap.setMime("text/plain");
			wrap.attach(encodedHello, "text/plain", "hello"+i+".txt");
			wrap.attach(encodedHello, "text/plain", "hello"+i+".txt");
			wrap.attach(encodedHello, "text/plain", "hello"+i+".txt");
			
			return wrap;
		}
	}
	class LivePoller extends AbstractMessagePoller{
		@Override
		public Store getStore() throws MessagingException {
		      final Properties properties = new Properties();

		      final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

		      properties.setProperty( "mail.pop3.socketFactory.class", SSL_FACTORY);
		      properties.setProperty( "", "");
		      
		      Session emailSession = Session.getDefaultInstance(properties );
		      Store store = emailSession.getStore("pop3");
		      		      
		      URLName url = new URLName("pop3", "mobile.absa.co.za", 995, "",
		              "", "");
		       
		      store = new POP3SSLStore(emailSession, url);
		      store.connect();
		      return store;
		}
	}

	class Office365Poller extends AbstractMessagePoller{
		@Override
		public Store getStore() throws MessagingException {
		      //create properties field
		      Properties properties = new Properties();

		      String host = "pop-mail.outlook.com";
		      properties.put("mail.pop3.host", host);
		      properties.put("mail.pop3.port", "995");
		      //properties.put("mail.pop3.starttls.enable", "true");
		      properties.put("mail.pop3.auth", "true");
		      properties.put("mail.pop3.ssl.enable", "true");
		      Session emailSession = Session.getDefaultInstance(properties);
		  
		      //create the POP3 store object and connect with the pop server
		      Store store = emailSession.getStore("pop3");

		      store.connect(host, "leansys@outlook.com", "user1pass");
		      
		      return store;
		}
	}
	interface MessagePoller{
		public List<MessageWrapper> pollForMessages() throws MessagingException, IOException; 
	}
	
	abstract class AbstractMessagePoller implements MessagePoller {
		
	abstract public Store getStore() throws NoSuchProviderException, MessagingException;

	public List<MessageWrapper> pollForMessages() throws MessagingException, IOException {

		 Store store = null;
         Folder emailFolder = null;
         
		try{
					 store = getStore();
					 List<MessageWrapper> messageWrappers = new ArrayList<MessageWrapper>(); 
					 
			         // create the folder object and open it
			         emailFolder = store.getFolder("INBOX");
			         emailFolder.open(Folder.READ_ONLY);
	
			         // retrieve the messages from the folder in an array and print it
			         Message[] messages = emailFolder.getMessages();
			         
			         
			         for (int i = 0; i < messages.length; i++) {
			            Message message = messages[i];
			            MessageWrapper messageWrap = new MessageWrapper();	            
			            appendPart(messageWrap,message,null,null,false);
			            
			            //message.setFlag(Flag.DELETED, true);
			            
			         }			         
			         return messageWrappers;
		}finally{
	         if (emailFolder != null && emailFolder.isOpen())emailFolder.close(true);
	         if(store != null && store.isConnected())store.close();
		}
	}
	 public void appendPart(MessageWrapper message,Object p,String mime,String name,boolean isBody) throws MessagingException, IOException {
	  
		  if (p instanceof Message){ 
		      Message m = (Message)p;
			  Address[] a;      
		      
		      if ((a = m.getFrom()) != null) {
		         for (int j = 0; j < a.length; j++){
		        	 message.from( a[j].toString()); 
		         }
		      }
		      
		      if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
		         for (int j = 0; j < a.length; j++){
		        	 message.to( a[j].toString()); 
		         }
		      }
		      
		      if ((a = m.getRecipients(Message.RecipientType.CC)) != null) {
		         for (int j = 0; j < a.length; j++){
		        	 message.to( a[j].toString()); 
		         }
		      }
		           
		      if (m.getSubject() != null){
		     	 message.subject(m.getSubject()); 
		      }
		      
		      appendPart(message,m.getContent(),m.getContentType(),m.getFileName(),true);

	      }
	      else if (p instanceof Multipart) {
	         Multipart mp = (Multipart) p;
	         int count = mp.getCount();
	         for (int i = 0; i < count; i++){
	        	 if(i == 0){
	        		 appendPart(message,mp.getBodyPart(i),mp.getBodyPart(i).getContentType(),mp.getBodyPart(i).getFileName(),true);
	        	 }
	        	 else{
	        		 appendPart(message,mp.getBodyPart(i),mp.getBodyPart(i).getContentType(),mp.getBodyPart(i).getFileName(),false);
	        	 }
	         }
	      } 
	      else if (p instanceof Part) {
	    	Part part = (Part) p;
			appendPart(message,part.getContent(),part.getContentType(),part.getFileName(),false);
	      } 
	      else if (p instanceof InputStream) {
	         InputStream x = (InputStream) p;
	         byte[] bArray = new byte[x.available()];         
	         x.read(bArray);
	         sun.misc.BASE64Encoder base64Encoder = new sun.misc.BASE64Encoder();
	         
	         if(isBody){
	         	message.setBody(base64Encoder.encode(bArray));
	         	message.setMime(mime);
	         }
	         else{
	        	 message.attach(base64Encoder.encode(bArray), mime, name);
	         }
	      }      
	   }
	}
	
	  class MessageWrapper implements Serializable{

			
			public MessageWrapper(){} 

			public MessageWrapper(
					String to,
					String cc,
					String bcc,
					String from,
					String subject,
					String body){
				to(to);
				cc(cc);
				bcc(bcc);
				subject(subject);
				this.from = from;
				this.body = body;
			} 

			
			
			private String from;
			private String mime;
			
			private List<String> to;
			private List<String> cc;
			private List<String> bcc;

			private String subject;
			private String body;
			private Map<String,Object> subjectParameters;
			private Map<String,Object> bodyParameters;
			private List<AttachmentWrapper> attachments;
			
			public List<String> getTo() {
				return to;
			}
			public void setTo(List<String> to) {
				this.to = to;
			}
			public String getFrom() {
				return from;
			}
			public void setFrom(String from) {
				this.from = from;
			}
			public String getBody() {
				return body;
			}
			public void setBody(String body) {
				this.body = body;
			}

			public List<String> getCc() {
				return cc;
			}

			public void setCc(List<String> cc) {
				this.cc = cc;
			}

			public List<String> getBcc() {
				return bcc;
			}

			public void setBcc(List<String> bcc) {
				this.bcc = bcc;
			}

			public String getSubject() {
				return subject;
			}

			public void setSubject(String subject) {
				this.subject = subject;
			}

			public Map<String, Object> getSubjectParameters() {
				return subjectParameters;
			}

			public void setSubjectParameters(Map<String, Object> subjectParameters) {
				this.subjectParameters = subjectParameters;
			}

			public Map<String, Object> getBodyParameters() {
				return bodyParameters;
			}

			public void setBodyParameters(Map<String, Object> bodyParameters) {
				this.bodyParameters = bodyParameters;
			}

			public MessageWrapper to(String to){
				if(this.to == null){
					this.to = new ArrayList<String>();
				}
				this.to.add(to);
				return this;
			}

			public MessageWrapper from(String from){
				this.from = from;
				return this;
			}
			
			public MessageWrapper subject(String subject){
				this.subject = subject;
				return this;
			}
			
			public MessageWrapper body(String body){
				this.body = body;
				return this;
			}

			public MessageWrapper cc(String cc){
				if(this.cc == null){
					this.cc = new ArrayList<String>();
				}
				this.cc.add(cc);
				return this;

			}


			public MessageWrapper bcc(String bcc){
				if(this.bcc == null){
					this.bcc = new ArrayList<String>();
				}
				this.bcc.add(bcc);
				return this;
			}
			
			public MessageWrapper attach(String base64Bytes,String mime,String fileName){
				AttachmentWrapper att = new AttachmentWrapper(base64Bytes, mime,fileName);
				return attach(att);
			}

			private MessageWrapper attach(AttachmentWrapper att) {
				if(this.attachments == null){
					this.attachments = new ArrayList<AttachmentWrapper>();
				}
				this.attachments.add(att);
				return this;
			}


			public MessageWrapper subjectParam(String name,Object value){
				if(subjectParameters == null){
					subjectParameters = new HashMap<String, Object>();
				}
				subjectParameters.put(name, value);
				return this;

			}
			
			public MessageWrapper bodyParam(String name,Object value){
				if(bodyParameters == null){
					bodyParameters = new HashMap<String, Object>();
				}
				bodyParameters.put(name, value);
				return this;

			}

			public String getMime() {
				return mime;
			}

			public void setMime(String mime) {
				this.mime = mime;
			}

			public List<AttachmentWrapper> getAttachments() {
				return attachments;
			}

			public void setAttachments(List<AttachmentWrapper> attachments) {
				this.attachments = attachments;
			}
			
		}
	 
	  class AttachmentWrapper implements Serializable{

			public AttachmentWrapper(){}
			
			public AttachmentWrapper(String base64Bytes,String mime,String filename){
				this.base64Bytes = base64Bytes;
				this.mime = mime;
				this.filename = filename;
			}
			
			
			private String base64Bytes;
			private String mime;
			private String filename;
			
			public String getBase64Bytes() {
				return base64Bytes;
			}

			public void setBase64Bytes(String base64Bytes) {
				this.base64Bytes = base64Bytes;
			}

			public String getMime() {
				return mime;
			}
			public void setMime(String mime) {
				this.mime = mime;
			}

			public String getFilename() {
				return filename;
			}

			public void setFilename(String filename) {
				this.filename = filename;
			}
			
		}


}
