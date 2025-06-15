package com.destridon.notio;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import lombok.Data;

public class NotIO {
	
	public static interface IEntity { 
		public String getNotionEntryId();
		public String getNotionDatabaseId();
	}
	
	@Data
	public abstract static class Entity implements IEntity{
		String notionEntryId;
		String notionDatabaseId;
	}
	
	public static @interface Table{
		String name = "";
	}
	
	public static @interface Title {
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Column {
		String value() default "";
	}
	
	public static @interface Transient { }
	
	public static NotionDatabase database(String apiKey, String databaseId) {
		NotionDatabase notionDb = new NotionDatabase(apiKey, databaseId);
		notionDb.retrieveModel();
		return notionDb;
	}
	

}
