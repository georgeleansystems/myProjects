package za.co.absa.messaging.base64;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.pdf.codec.Base64;

import za.co.absa.messaging.Attachment;
import za.co.absa.messaging.Message;
import za.co.absa.messaging.Message.MessageType;

public class MessageWrapper implements Serializable{

	
	public MessageWrapper(){} 

	public MessageWrapper(
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
	
	public Message toMessage(){
		Message message = new Message();
		
		if(attachments != null){
			List<Attachment> tmpAttachments = new ArrayList<Attachment>();
			for(AttachmentWrapper tmpWrapper:attachments){
				tmpAttachments.add(new Attachment(Base64.decode(tmpWrapper.getBase64Bytes()) , tmpWrapper.getMime(), tmpWrapper.getMime()));
			}
			message.setAttachments(tmpAttachments);
		}
		
		
		message.setBcc(bcc);
		message.setBody(body);
		message.setBodyParameters(subjectParameters);
		message.setCc(cc);
		message.setFrom(from);
		message.setMessageType(messageType);
		message.setMime(mime);
		message.setSubject(subject);
		message.setTo(to);
		
		return message;
	}

}
