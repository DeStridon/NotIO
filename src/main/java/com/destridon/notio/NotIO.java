package com.destridon.notio;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import lombok.Data;

public class NotIO {
	
	public static interface IEntity { 
		public String getNotionEntryId();
		public void setNotionEntryId(String notionEntryId);
		public String getNotionDatabaseId();
		public void setNotionDatabaseId(String notionDatabaseId);
	}
	
	@Data
	public abstract static class Entity implements IEntity{
		public String notionEntryId;
		public String notionDatabaseId;
	}
	
	public static @interface Table{
		String name = "";
	}
	@Retention(RetentionPolicy.RUNTIME)
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
