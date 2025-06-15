package com.destridon.notio;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.destridon.athttp.AtHttp;
import com.destridon.notio.NotIO.Column;
import com.destridon.notio.NotIO.IEntity;
import com.destridon.notio.NotionExchange.DatabaseModelOutput;
import com.destridon.notio.NotionExchange.DatabaseOutput;
import com.destridon.notio.NotionExchange.DatabaseQueryOutput;
import com.destridon.notio.NotionExchange.NotionModelProperty;
import com.destridon.notio.NotionExchange.NotionParent;
import com.destridon.notio.NotionExchange.NotionProperty;
import com.destridon.notio.NotionExchange.PagePatchInput;
import com.destridon.notio.NotionExchange.PagePostInput;
import com.destridon.notio.NotionExchange.PropertyType;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotionDatabase {

	NotionExchange notionExchange;
	String notionDatabaseId;
	Map<String, NotionModelProperty> databaseModel;
	
	
    public NotionDatabase(String apiKey, String notionDatabaseId) {
    
    	Map<String, String> notionVariables = new HashMap<>();
        notionVariables.put("api_key", apiKey);
        notionExchange = AtHttp.generate(NotionExchange.class, notionVariables);

        this.notionDatabaseId = notionDatabaseId;
        
    }
    
    public void retrieveModel() {
    	
    	String databaseString = notionExchange.getDatabaseAsString(notionDatabaseId);
    	
    	DatabaseModelOutput database = notionExchange.getDatabase(notionDatabaseId);
    	databaseModel = database.getProperties();
    }

    
    @SneakyThrows
    public <U extends IEntity> List<U> getEntries(Class<U> entity){

    	DatabaseQueryOutput databaseQueryOutput = notionExchange.postDatabaseQuery(notionDatabaseId);
    	
    	List<U> entities = new ArrayList<>();
    	Constructor<U> constructor = entity.getConstructor();
    	
    	for(DatabaseOutput databaseOutput : databaseQueryOutput.results) {
    		U obj = constructor.newInstance();
    		for(Field field : FieldUtils.getEntityFields(entity)) {
    			
    			Column columnAnnotation = field.getDeclaredAnnotation(Column.class);
    			
    			String name = (columnAnnotation == null || StringUtils.isEmpty(columnAnnotation.value())) ? field.getName() : columnAnnotation.value();
    			
    			String value = getValue(databaseOutput, name);
    			
    			field.setAccessible(true);
    			field.set(obj, value);
				
				
    		
    		}
			entities.add(obj);    		
    	}
    	
    	return entities;
    }
    
    private String getValue(DatabaseOutput databaseOutput, String name) {
		NotionProperty property = databaseOutput.getProperties().get(name);
		if(property == null) {
			return null;
		}

		if(property.getType() == PropertyType.title) {
			return property.getContent();
		}
		else if(property.getType() == PropertyType.rich_text) {
			return property.getContent();
		}
		else if(property.getType() == PropertyType.select) {
			return property.getSelect().getName();
		}
		else{
			log.error("This type is not mapped yet : "+property.getType());
		}
		return null;
	}

	
	@SneakyThrows
	public <U extends IEntity> void update(U obj) {

		PagePatchInput input = new PagePatchInput();

		for(Field field : FieldUtils.getEntityFields(obj.getClass())) {
			Column columnAnnotation = field.getDeclaredAnnotation(Column.class);
			String name = (columnAnnotation == null || StringUtils.isEmpty(columnAnnotation.value())) ? field.getName() : columnAnnotation.value();

			field.setAccessible(true);
			String value = field.get(obj).toString();

			PropertyType propertyType = databaseModel.get(name).getType();
			if(propertyType == PropertyType.title) {
				input.properties.put(name, Map.of(PropertyType.title, Arrays.asList(Map.of("text", Map.of("content", value)))));
			}
			else if(propertyType == PropertyType.rich_text) {
				input.properties.put(name, Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", value)))));
			}
			else if(propertyType == PropertyType.select) {
				input.properties.put(name, Map.of(PropertyType.select, Map.of("name", value)));
			}
			else{
				log.error("This type is not mapped yet : "+propertyType);
			}

		}


		// input.properties = new LinkedHashMap<>();
		// input.properties.put("Name", Map.of(PropertyType.title, Arrays.asList(Map.of("text", Map.of("content", entry.getName())))));
		// input.properties.put("Path", Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", entry.getPath())))));
		// input.properties.put("Verb", Map.of(PropertyType.select, Map.of("name", entry.getHttpVerb())));
		// input.properties.put("Description", Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", entry.getDescription())))));

		String result = notionExchange.patchPage(obj.getNotionEntryId(), input);
		System.out.println(result);

	}

	@SneakyThrows
	public <U extends IEntity> void insertOrUpdate(U obj) {
		PagePostInput input = new PagePostInput();
		input.parent = new NotionParent();
		input.parent.database_id = notionDatabaseId;

		input.properties = new LinkedHashMap<>();
		for(Field field : FieldUtils.getEntityFields(obj.getClass())) {
			Column columnAnnotation = field.getDeclaredAnnotation(Column.class);
			String name = (columnAnnotation == null || StringUtils.isEmpty(columnAnnotation.value())) ? field.getName() : columnAnnotation.value();

			field.setAccessible(true);
			String value = field.get(obj).toString();

			PropertyType propertyType = databaseModel.get(name).getType();
			if(propertyType == PropertyType.title) {
				input.properties.put(name, Map.of(PropertyType.title, Arrays.asList(Map.of("text", Map.of("content", value)))));
			}
			else if(propertyType == PropertyType.rich_text) {
				input.properties.put(name, Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", value)))));
			}
			else if(propertyType == PropertyType.select) {
				input.properties.put(name, Map.of(PropertyType.select, Map.of("name", value)));
			}
			else{
				log.error("This type is not mapped yet : "+propertyType);
			}

		}

		String result = notionExchange.postPage(input);
		System.out.println(result);

	}
    
//    public List<NotionEndpointEntry> getNotionEntries(){
//    	
//    	List<NotionEndpointEntry> entries = new ArrayList<>();
//    	//String databaseQueryString = notionExchange.postDatabaseQueryAsString(notionDatabaseId);
//        
//    	
//    		
//    		NotionEndpointEntry notionEndpointEntry = NotionEndpointEntry.builder()
//    				.notionId(databaseOutput.getId())
//    				.name(databaseOutput.getProperties().get("Name").getContent())
//    				.path(databaseOutput.getProperties().get("Path").getContent())
//    				.httpVerb(HttpVerb.valueOf(databaseOutput.getProperties().get("Verb").getSelect().name))
//    				.description(databaseOutput.getProperties().get("Description").getContent())
//    				.build();
//    		entries.add(notionEndpointEntry);
//    		
//    		if(databaseOutput.getProperties().get("Status").getSelect() != null) {
//    			notionEndpointEntry.setStatus(Status.valueOf(databaseOutput.getProperties().get("Status").getSelect().name));
//    		}
//    		
//    	}
//    	
//    	return entries;
//    	
//    }

//	public void createEntry(NotionEndpointEntry entry) {
//		
//		PagePostInput input = new PagePostInput();
//		input.parent = new NotionParent();
//		input.parent.database_id = notionDatabaseId;
//		
//		input.properties = new LinkedHashMap<>();
//		input.properties.put("Name", Map.of(PropertyType.title, Arrays.asList(Map.of("text", Map.of("content", entry.getName())))));
//		input.properties.put("Path", Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", entry.getPath())))));
//		input.properties.put("Verb", Map.of(PropertyType.select, Map.of("name", entry.getHttpVerb())));
//		if(StringUtils.isNotEmpty(entry.getDescription())) {
//			input.properties.put("Description", Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", entry.getDescription())))));
//		}
//		if(entry.getStatus() != null) {
//			input.properties.put("Status", Map.of(PropertyType.select, Map.of("name", entry.getStatus())));
//		}
////		input.children = new ArrayList<>();
//		
//		String result = notionExchange.postPage(input);
//		System.out.println(result);
//	}
//	
//	
//	public void updateEntry(NotionEndpointEntry entry) {
//		
//		PagePatchInput input = new PagePatchInput();
//		input.properties = new LinkedHashMap<>();
//		input.properties.put("Name", Map.of(PropertyType.title, Arrays.asList(Map.of("text", Map.of("content", entry.getName())))));
//		input.properties.put("Path", Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", entry.getPath())))));
//		input.properties.put("Verb", Map.of(PropertyType.select, Map.of("name", entry.getHttpVerb())));
//		if(StringUtils.isNotEmpty(entry.getDescription())) {
//			input.properties.put("Description", Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", Map.of("content", entry.getDescription())))));
//		}
//		if(entry.getStatus() != null) {
//			input.properties.put("Status", Map.of(PropertyType.select, Map.of("name", entry.getStatus())));
//		}
//		
//		String result = notionExchange.patchPage(entry.getNotionId(), input);
//		//System.out.println(result);
//		
//	}
    



}
