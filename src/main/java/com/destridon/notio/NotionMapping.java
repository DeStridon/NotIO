package com.destridon.notio;

import java.util.LinkedHashMap;

public class NotionMapping {
	
	public LinkedHashMap<String, NotionType> types;
	
	
	public static enum NotionType{
		
	}
	
	static NotionMapping fromEntity(Class<?> entity) {
		return null;
	}
	
	static NotionMapping fromNotion(String baseName) {
		return null;
	}

}
