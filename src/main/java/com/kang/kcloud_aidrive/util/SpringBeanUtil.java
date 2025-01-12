package com.kang.kcloud_aidrive.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Object copying tool - 对象拷贝工具
 *
 * @author Kai Kang
 */
public class SpringBeanUtil {

    /**
     * 复制属性 - DO, DTO
     *
     * @param <T>    Target object type - 目标对象类型
     * @param source Source object - 源对象
     * @param target Target object type目标对象类型
     * @return Copied target object - 复制后的目标对象
     */
    public static <T> T copyProperties(Object source, Class<T> target) {
        try {
            T t = target.getConstructor().newInstance();
            BeanUtils.copyProperties(source, t);
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 复制一份具有相同属性的列表
     *
     * @param sourceList Source list - 源列表
     * @param target     Target object type - 目标对象类型
     * @param <T>        Target object type - 目标对象类型
     * @return Copied target object
     */
    public static <T> List<T> copyProperties(List<?> sourceList, Class<T> target) {
        ArrayList<T> targetList = new ArrayList<>();

        sourceList.forEach(source -> {
            T t = copyProperties(source, target);
            targetList.add(t);
        });
        return targetList;
    }


    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

}