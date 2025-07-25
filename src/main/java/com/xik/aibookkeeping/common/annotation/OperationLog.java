package com.xik.aibookkeeping.common.annotation;



import com.xik.aibookkeeping.common.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//自定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog  {
    //数据库操作类型， UPDATE INERT
    OperationType value();
}
