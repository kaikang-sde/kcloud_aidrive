package com.kang.kcloud_aidrive.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON tool
 *
 * @author Kai Kang
 */

@Slf4j
public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {

        // Enable the use of single quotes - 设置可用单引号
        MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // All properties of the serialized object during serialization - 序列化的时候序列对象的所有属性
        MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        // Do not throw an exception if additional properties are present during deserialization 反序列化的时候如果多了其他属性,不抛出异常
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Conversion between snake_case and camelCase - 下划线和驼峰互转
        // mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        // Do not throw an exception when the object is null or empty - 如果是空对象的时候,不抛异常
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Disable the default timestamp format conversion; allow disabling it and set a custom time format for display when needed.
        // 取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static ObjectMapper get() {
        return MAPPER;
    }

    /**
     * Convert an object to a JSON string
     *
     * @param obj data
     */
    public static String obj2Json(Object obj) {
        String jsonStr = null;
        try {
            jsonStr = MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON formatting exception: {}", e.getMessage());
        }
        return jsonStr;
    }

    /**
     * Convert a JSON string to an object
     */
    public static <T> T json2Obj(String jsonStr, Class<T> beanType) {
        T obj = null;
        try {
            obj = MAPPER.readValue(jsonStr, beanType);
        } catch (Exception e) {
            log.error("JSON formatting exception: {}", e.getMessage());
        }
        return obj;
    }

    /**
     * Convert JSON data to a list of POJO objects
     */
    public static <T> List<T> json2List(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            return MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            log.error("JSON formatting exception: {}", e.getMessage());
        }
        return new ArrayList<>(0);
    }

}