package za.co.absa.messaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message implements Serializable{

	
	public Message(){} 

	public Message(
			String to,
			String cc,
			String bcc,
			String from,
			MessageType messageType,
			String body){
		to(to);
		cc(cc);
		bcc(bcc);
		this.from = from;
		this.messageType = messageType;
		this.body = body;
	} 

	
	public enum MessageType{
		EMAIL,
		FAX,
		SMS,
		FACEBOOK,
		TWITTER,
		INSTAGRAM,
		GOOGLEPLUS,
		WHATSAPP
	}
	
	private String from;
	private String mime;
	
	private List<String> to;
	private List<String> cc;
	private List<String> bcc;

	private String subject;
	private MessageType messageType;
	private String body;
	private Map<String,Object> subjectParameters;
	private Map<String,Object> bodyParameters;
	private List<Attachment> attachments;
	
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
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
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

	public Message to(String to){
		if(this.to == null){
			this.to = new ArrayList<String>();
		}
		this.to.add(to);
		return this;
	}

	public Message from(String from){
		this.from = from;
		return this;
	}
	
	public Message subject(String subject){
		this.subject = subject;
		return this;
	}
	
	public Message body(String body){
		this.body = body;
		return this;
	}

	public Message cc(String cc){
		if(this.cc == null){
			this.cc = new ArrayList<String>();
		}
		this.cc.add(cc);
		return this;

	}


	public Message bcc(String bcc){
		if(this.bcc == null){
			this.bcc = new ArrayList<String>();
		}
		this.bcc.add(bcc);
		return this;
	}
	
	public Message attach(byte[] attachment,String mime,String fileName){
		Attachment att = new Attachment(attachment, mime,fileName);
		return attach(att);
	}

	private Message attach(Attachment att) {
		if(this.attachments == null){
			this.attachments = new ArrayList<Attachment>();
		}
		this.attachments.add(att);
		return this;
	}


	public Message subjectParam(String name,Object value){
		if(subjectParameters == null){
			subjectParameters = new HashMap<String, Object>();
		}
		subjectParameters.put(name, value);
		return this;

	}
	
	public Message bodyParam(String name,Object value){
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

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	

}
