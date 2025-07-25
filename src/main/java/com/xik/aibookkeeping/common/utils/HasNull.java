package com.xik.aibookkeeping.common.utils;


import java.lang.reflect.Field;

/**
 * 判断一个类里面的所有字段是否为空
 */
public class HasNull {
    public static boolean hasNullField(Object obj) {
        if (obj == null) return true;

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null) {
                    return true; // 有一个字段为 null
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return false; // 所有字段都非 null
    }

}
