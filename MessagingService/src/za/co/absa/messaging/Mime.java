package za.co.absa.messaging;

public enum Mime {
	
	PDF("application/pdf",".pdf"),
	TIFF("image/tiff",".tiff"),
	XLS("application/vnd.ms-excel",".xls"),
	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",".xlsx"),
	PPT("application/vnd.ms-powerpoint",".ppt"),
	PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation",".pptx"),
	RTF("application/rtf",".rtf"),
	TXT("text/plain",".txt"),
	DOC("application/msword",".doc"),
	DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document",".docx"),
	HTML("text/html",".html",".htm"),
	BMP("image/bmp",".bmp"),
	SVG("image/svg+xml",".svg"),
	PNG("image/png",".png"),
	JPG("image/jpeg",".jpg"),
	GIF("image/gif",".gif");
	
	Mime(String mime,String ... extensions){
		this.mime = mime;
		this.extensions = extensions;
	}

	
	
	private String mime;
	private String [] extensions;
	
	public String getMime() {
		return mime;
	}
	
	public String[] getExtensions() {
		return extensions;
	}
	
	public String extension(){
		return extensions[0];
	}
	
	
}
