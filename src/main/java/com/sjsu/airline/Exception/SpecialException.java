package com.sjsu.airline.Exception;

import org.springframework.stereotype.Component;

@Component
public class SpecialException extends Exception{

	public int code;
	public String message;
	public boolean xml;
	
	public int getCode() {
		return code;
	}
	public boolean isXml() {
		return xml;
	}
	public void setXml(boolean xml) {
		this.xml = xml;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
