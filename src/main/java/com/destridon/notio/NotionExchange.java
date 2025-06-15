package com.destridon.notio;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.destridon.athttp.AtHttp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AtHttp.Header(key = "Authorization", value = "Bearer {api_key}")
@AtHttp.Header(key = "Content-Type", value = "application/json")
@AtHttp.Header(key = "Notion-Version", value = "2022-06-28")
@AtHttp.Path("https://api.notion.com/v1")
public abstract class NotionExchange {

	/* Pages */
	
    @AtHttp.Path("pages")
    public abstract String postPage(@AtHttp.RequestBody PagePostInput input);

    @AtHttp.Path("pages/{pageId}")
    public abstract String patchPage(@AtHttp.RequestParam(value = "pageId") String pageId, @AtHttp.RequestBody PagePatchInput input);

    
	/* Databases */
    
    @AtHttp.Path("databases/{databaseId}")
    public abstract DatabaseModelOutput getDatabase(@AtHttp.RequestParam(value = "databaseId") String databaseId);
    //public abstract String getDatabaseQuery();
    
    @AtHttp.Path("databases/{databaseId}")
    public abstract String getDatabaseAsString(@AtHttp.RequestParam(value = "databaseId") String databaseId);

    @AtHttp.Path("databases/{databaseId}/query")
    public abstract DatabaseQueryOutput postDatabaseQuery(@AtHttp.RequestParam(value = "databaseId") String databaseId);
    
    @AtHttp.Path("databases/{databaseId}/query")
    public abstract String postDatabaseQueryAsString(@AtHttp.RequestParam(value = "databaseId") String databaseId);
    
    

    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseQueryOutput{
    	String object;
    	List<DatabaseOutput> results;
    	String next_cursor;
    	Boolean has_more;
    	String type;
    	Map<String, String> page_or_database;
    	String request_id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagePostInput {
        NotionParent parent;
        Map<String, Map<PropertyType, Object>> properties;
        List<Object> children;
    }

//    public static class Parent {
//        String database_id;
//    }

    public enum PropertyType{
    	title,
    	rich_text,
    	select,
    	number,
    	text,
    }
    
    
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseOutput {
    	String object;
    	String id;
        NotionCover cover;
        NotionIcon icon;
        Date created_time;
        NotionMinimalUser created_by;
        
        NotionMinimalUser last_edited_by;
        Date last_edited_time;
        
        List<NotionPropertyRichText> title;
        List<String> description;
        
        Boolean is_inline;
        Map<String, NotionProperty> properties; 
        
        NotionParent parent;
        
        String url;
        
        String public_url;
        Boolean archived;
        Boolean in_trash;
        String request_id;

    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseModelOutput {
    	String object;
    	String id;
        NotionCover cover;
        NotionIcon icon;
        Date created_time;
        NotionMinimalUser created_by;
        
        NotionMinimalUser last_edited_by;
        Date last_edited_time;
        
        List<NotionPropertyRichText> title;
        List<String> description;
        
        Boolean is_inline;
        Map<String, NotionModelProperty> properties; 
        
        NotionParent parent;
        
        String url;
        
        String public_url;
        Boolean archived;
        Boolean in_trash;
        String request_id;

    }
        
    

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionPropertyRichText{
    	String type;
    	NotionText text;
    	NotionAnnotation annotations;
    	String plain_text;
    	String href;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionText{
    	String content;
    	String link;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionAnnotation{
    	Boolean bold;
    	Boolean italic;
    	Boolean strikethrough;
    	Boolean underline;
    	Boolean code;
    	String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionMinimalUser{
    	String object;
        String id;
    }
    
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionCover {
        String type;
        NotionExternal external;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionExternal {
        String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionIcon {
        String type;
        String emoji;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionParent {
        String type;
        String page_id;
        String database_id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionProperty {
        String id;
        String name; // not used in entry reading
        PropertyType type;
        NotionSelect select;
        List<NotionPropertyRichText> rich_text;
        List<NotionPropertyRichText> title;
        
        public String getContent() {
        	if(type == PropertyType.title) {
        		if(title != null && !title.isEmpty()) {
        			return title.iterator().next().plain_text;
        		}
        	}
        	else if(type == PropertyType.rich_text) {
        		if(rich_text != null && !rich_text.isEmpty()) {
        			return rich_text.iterator().next().plain_text;
        		}
        	}
        	return null;
        }
        
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionModelProperty {
        String id;
        String name; // not used in entry reading
        PropertyType type;
        NotionSelect select;
        List<NotionPropertyRichText> rich_text;
        NotionPropertyRichText title;
    }
    




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionSelect {
    	String id;
    	String name;
    	String color;
        List<NotionSelectOption> options;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionSelectOption{
    	String id;
    	String name;
    	String color;
    	String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagePatchInput{
    	Map<String, Object> properties;
    	Boolean in_trash;
    	NotionIcon icon;
    	NotionCover cover;
    }
    

    
}
