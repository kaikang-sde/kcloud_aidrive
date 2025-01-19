package com.kang.kcloud_aidrive.util;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON data structure - JSON数据结构
 *
 * @author Kai Kang
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonData {


    private Integer code;
    private Object data;
    private String msg;
    // Jackson for getData without fastjson2
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Jackson的ObjectMapper


    /**
     * Constructor - Success, no data provided
     *
     * @return
     */
    public static JsonData buildSuccess() {
        return new JsonData(0, null, null);
    }

    /**
     * Constructor - Success, data provided
     *
     * @param data
     * @return
     */
    public static JsonData buildSuccess(Object data) {
        return new JsonData(0, data, null);
    }

    /**
     * Constructor - Failed, provide description details
     *
     * @param msg
     * @return
     */
    public static JsonData buildError(String msg) {
        return new JsonData(-1, null, msg);
    }

    /**
     * Constructor - Define custom status codes and error responses
     *
     * @param code
     * @param msg
     * @return
     */
    public static JsonData buildCodeAndMsg(int code, String msg) {
        return new JsonData(code, null, msg);
    }

    /**
     * Constructor - Define custom status codes and error responses via BizCodeEnum
     *
     * @param codeEnum
     * @return
     */
    public static JsonData buildResult(BizCodeEnum codeEnum) {
        return JsonData.buildCodeAndMsg(codeEnum.getCode(), codeEnum.getMessage());
    }

    /**
     * 获取远程调用数据
     *
     * @param typeReference
     * @param <T>
     * @return
     */
    public <T> T getData(Class<T> typeReference) {
        return JSON.parseObject(JSON.toJSONString(data), typeReference);
    }

    /**
     * 获取远程调用数据 with Jackson
     *
     * @param typeReference
     * @param <T>
     * @return
     */
    public <T> T getDataWithJackson(Class<T> typeReference) {
        try {
            // 将data转换为JSON字符串
            String jsonString = objectMapper.writeValueAsString(data);

            // 将JSON字符串反序列化为指定类型的对象
            return objectMapper.readValue(jsonString, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting data", e);
        }
    }


    public boolean isSuccess() {
        return code == 0;
    }
}
