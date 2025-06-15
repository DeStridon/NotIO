package com.destridon.notio;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.AttributeInUseException;
import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.destridon.notio.NotIO.Entity;
import com.destridon.notio.NotIO.IEntity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FieldUtils {
	
	static Map<Class<?>, Set<Field>> classFields = new HashMap<>();
	
	public static Set<Field> getEntityFields(Class<?> objectClass){
		
		if(objectClass == null || objectClass == Object.class || objectClass == Entity.class){
			return new HashSet<>();
		}
		
		
		Set<Field> fields = classFields.get(objectClass);
		if(fields == null) {
			
			fields = new HashSet<>();
			fields.addAll(getEntityFields(objectClass.getSuperclass()));
			
			for(Field field : objectClass.getDeclaredFields()) {
				
				// avoid synthetic fields
				if(field.isSynthetic()) {
					continue;
				}
				if(field.isAnnotationPresent(NotIO.Transient.class) || field.isAnnotationPresent(Transient.class)) {
					continue;
				}
				if(field.getType() == org.slf4j.Logger.class) {
					continue;
				}
				
				field.setAccessible(true);
				
				fields.add(field);
				
			}

			classFields.put(objectClass, fields);
		
		}
		
		return fields;
		
	}
	
	@SneakyThrows
    public static <U extends IEntity> void setGeneratedField(String value, Field field, U obj) {
		
			
		if(value == null) {
			return;
		}
		
		Convert convertAnnotation = field.getDeclaredAnnotation(Convert.class);
		
		if(convertAnnotation != null) {
			
			Object converter = convertAnnotation.converter().getConstructor().newInstance();
			if(!(converter instanceof AttributeConverter)) {
				log.error("Cannot convert with converter "+converter.getClass());
				return;
			}
			AttributeConverter attributeConverter = (AttributeConverter) converter;
			Object attributeValue = attributeConverter.convertToEntityAttribute(value);
			field.set(obj, attributeValue);
			return;
		}
		
		Type type = field.getGenericType();
		// @Convert (might be enum, this condition should be tested before classic enum)
		
		if(type == String.class) {
			field.set(obj, value);
		}
		else if(type == long.class) {
			field.setLong(obj, Long.parseLong(value));
		}
		else if(type == Long.class) {
			field.set(obj, Long.parseLong(value));
		}
		else if(type == boolean.class) {
			field.setBoolean(obj, StringUtils.equals("true", value.toLowerCase()) || StringUtils.equals("1", value));
		}
		else if(type == Boolean.class) {
			field.set(obj, StringUtils.equals("true", value.toLowerCase()) || StringUtils.equals("1", value));
		}
		else if(type == Byte.class) {
			field.set(obj, Byte.parseByte(value));
		}
		else if(type == double.class) {
			field.setDouble(obj, Double.parseDouble(value));
		}
		else if(type == Double.class) {
			field.set(obj, Double.parseDouble(value));
		}
		else if(type == Character.class && value.length() > 0) {
			field.set(obj, value.charAt(0));
		}
		else if(type == Float.class) {
			field.set(obj, Float.parseFloat(value));
		}
		else if(type == int.class) {
			field.setInt(obj, Integer.parseInt(value));
		}
		else if(type == Integer.class) {
			field.set(obj, Integer.parseInt(value));
		}
		else if(type == short.class) {
			field.setShort(obj, Short.parseShort(value));
		}
		else if(type == Short.class) {
			field.set(obj, Short.parseShort(value));
		}
		else if(type == Date.class) {
			
			LocalDateTime dateTime;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS][.SS][.S]");
			TemporalAccessor temporalAccessor = formatter.parseBest(value, LocalDateTime::from, LocalDate::from);
			if (temporalAccessor instanceof LocalDateTime) {
			  dateTime = (LocalDateTime)temporalAccessor;
			} else {
			  dateTime = ((LocalDate)temporalAccessor).atStartOfDay();
			}

			field.set(obj, Date.from(dateTime.atZone(ZoneOffset.systemDefault()).toInstant()));
			
		}
		// Enums
		else if(type instanceof Class && ((Class<?>) type).isEnum()) {
			field.set(obj, Enum.valueOf((Class)type, value));
		}
		
		else {
			log.error("This type is not mapped yet : "+type);
			throw new AttributeInUseException("This type is not mapped yet : "+type);
		}
		
		
	}

}
