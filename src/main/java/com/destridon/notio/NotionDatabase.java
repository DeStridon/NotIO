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
import com.destridon.athttp.serializer.JacksonSerializer;
import com.destridon.notio.NotIO.Column;
import com.destridon.notio.NotIO.IEntity;
import com.destridon.notio.NotIO.Title;
import com.destridon.notio.NotionExchange.DatabaseModelOutput;
import com.destridon.notio.NotionExchange.DatabaseOutput;
import com.destridon.notio.NotionExchange.DatabaseQueryOutput;
import com.destridon.notio.NotionExchange.NotionModelProperty;
import com.destridon.notio.NotionExchange.NotionParent;
import com.destridon.notio.NotionExchange.NotionProperty;
import com.destridon.notio.NotionExchange.PagePatchInput;
import com.destridon.notio.NotionExchange.PagePostInput;
import com.destridon.notio.NotionExchange.PropertyType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotionDatabase {

	NotionExchange notionExchange;
	String notionDatabaseId;
	Map<String, NotionModelProperty> databaseModel;
	
	
    public NotionDatabase(String apiKey, String notionDatabaseId) {
    
    	JacksonSerializer mapper = new JacksonSerializer();
    	
    	Map<String, String> notionVariables = new HashMap<>();
        notionVariables.put("api_key", apiKey);
        notionExchange = AtHttp.generate(NotionExchange.class, notionVariables, mapper);
        
        // this.httpClient = client;
        // mapper.getMapper().setSerializationInclusion(Include.NON_NULL);

        mapper.getMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.getMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.getMapper().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.getMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.getMapper().configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

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
    			
    			String columnName = getColumnName(field);
    			
    			FieldUtils.setGeneratedField(getValue(databaseOutput, columnName), field, obj);
    			
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
			if(property.getSelect() == null) {
				return null;
			}
			return property.getSelect().getName();
		}
		else{
			log.error("This type is not mapped yet : "+property.getType());
		}
		return null;
	}

	@SneakyThrows
	public <U extends IEntity> void delete(U obj) {
		notionExchange.patchPage(obj.getNotionEntryId(), PagePatchInput.builder().in_trash(true).build());
	}

	private String getColumnName(Field field) {
		Title titleAnnotation = field.getDeclaredAnnotation(Title.class);
		if(titleAnnotation != null) {
			return StringUtils.isEmpty(titleAnnotation.value()) ? field.getName() : titleAnnotation.value();
		}
		Column columnAnnotation = field.getDeclaredAnnotation(Column.class);
		if(columnAnnotation != null) {
			return StringUtils.isEmpty(columnAnnotation.value()) ? field.getName() : columnAnnotation.value();
		}
		return field.getName();
	}

	@SneakyThrows
	public <U extends IEntity> void updateOrInsert(U obj) {
		
		if(obj.getNotionEntryId() != null) {
			update(obj);
		}
		else {
			insert(obj);
		}

	}

	@SneakyThrows
	public <U extends IEntity> void update(U obj) {

		PagePatchInput input = new PagePatchInput();

		for(Field field : FieldUtils.getEntityFields(obj.getClass())) {
			String name = getColumnName(field);

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

		String result = notionExchange.patchPage(obj.getNotionEntryId(), input);

	}

	@SneakyThrows
	public <U extends IEntity> void insert(U obj) {
		
		PagePostInput input = new PagePostInput();
		input.parent = new NotionParent();
		input.parent.database_id = notionDatabaseId;

		input.properties = new LinkedHashMap<>();
		for(Field field : FieldUtils.getEntityFields(obj.getClass())) {
			String name = getColumnName(field);

			field.setAccessible(true);
			
			String value = field.get(obj) == null ? null : field.get(obj).toString();

			NotionModelProperty property = databaseModel.get(name);
			if(property == null) {
				log.error("Property "+name+" not found in database model");
				continue;
			}

			PropertyType propertyType = property.getType();
			
			if(propertyType == PropertyType.title) {
				input.properties.put(name, Map.of(PropertyType.title, Arrays.asList(Map.of("text", generateMap("content", value)))));
			}
			else if(propertyType == PropertyType.rich_text) {
				input.properties.put(name, Map.of(PropertyType.rich_text, Arrays.asList(Map.of("text", generateMap("content", value)))));
			}
			else if(propertyType == PropertyType.select) {
				input.properties.put(name, Map.of(PropertyType.select, generateMap("name", value)));
			}
			else{
				log.error("This type is not mapped yet : "+propertyType);
			}

		}

		String result = notionExchange.postPage(input);
		if(result.startsWith("{\"object\":\"error\"")) {
			System.out.println(result);
		}
	}

	public static Map<String, String> generateMap(String key, String value) {
		Map<String, String> map = new HashMap<>();
		map.put(key, value);
		return map;
	}

	
		

}
