package com.main.utils;

import com.main.exception.CustomizeRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MainMapUtil {

    /**
     * 实体类转换为map集合
     *
     * @return
     */
    public static <T> Map<String, Object> objectToMap(T t) {

        Class<?> clazz = t.getClass();
        Map<String, Object> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String propertyName = descriptor.getName();
                if (!propertyName.equals("class")) {
                    Method readMethod = descriptor.getReadMethod();
                    Object result = readMethod.invoke(t);
                    if (result != null) {
                        map.put(propertyName, result);
                    } else {
                        map.put(propertyName, "");
                    }
                }
            }
        } catch (Exception e) {
            log.info("装换成map异常，", e);
            throw new CustomizeRuntimeException("服务器内部异常");
        }

        return map;
    }
}
