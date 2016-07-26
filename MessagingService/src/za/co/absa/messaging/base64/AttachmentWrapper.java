package za.co.absa.messaging.base64;

import java.io.Serializable;

public class AttachmentWrapper implements Serializable{

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
