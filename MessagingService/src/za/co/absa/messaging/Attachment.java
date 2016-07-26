package za.co.absa.messaging;

import java.io.Serializable;

public class Attachment implements Serializable{

	public Attachment(){}
	
	public Attachment(byte[] bytes,String mime,String filename){
		this.bytes = bytes;
		this.mime = mime;
		this.filename = filename;
	}
	
	
	private byte[] bytes;
	private String mime;
	private String filename;
	
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
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
	
	
	
}
