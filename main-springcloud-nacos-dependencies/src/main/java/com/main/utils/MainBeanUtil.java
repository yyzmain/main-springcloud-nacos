package com.main.utils;

import com.main.exception.CustomizeRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 反射调用bean copy工具类
 */
@Slf4j
public class MainBeanUtil {

    private MainBeanUtil() {

    }

    /**
     * 根据路径获取bean实例
     *
     * @param classPath bean 路径
     * @return
     * @throws Exception
     */
    public static Object getBean(String classPath) throws Exception {
        try {
            // 根据给定的类名初始化类
            Class catClass = Class.forName(classPath);
            // 实例化这个类
            Object obj = catClass.newInstance();
            return obj;
        } catch (Exception e) {
            log.error("CicBeanUtil Init Bean Error");
            throw e;
        }

    }

    /**
     * copy 数据 包含list
     *
     * @param source      源
     * @param sourceClazz 目标
     * @throws Exception
     */
    public static <T> T copyProperties(Object source, @NonNull Class<T> sourceClazz) {
        try {
            if (Objects.isNull(source)) return null;
            T target = sourceClazz.newInstance();

            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("拷贝属性异常", e);
            throw new CustomizeRuntimeException("拷贝属性异常");
        }
    }


    /**
     * copy 数据 包含list
     *
     * @param source 源
     * @param clazz  目标
     * @throws Exception
     */
    public static <T> T copyBean(Object source, @NonNull Class<T> clazz) {
        try {
            if (Objects.isNull(source)) return null;
            T target = clazz.newInstance();
            //普通属性用beancopy
            BeanUtils.copyProperties(source, target);
            //copy bean中的bean
            copyBeanProperties(source, clazz);
            //copy bean 中list
            copyListProperty(source, target);
            return target;
        } catch (Exception e) {
            log.error("拷贝属性异常", e);
            throw new CustomizeRuntimeException("拷贝属性异常");
        }
    }

    /**
     * copy bean下的bean
     *
     * @param source
     * @param clazz
     * @throws Exception
     */
    private static <T> void copyBeanProperties(Object source, Class<T> clazz) throws Exception {
        T target = clazz.newInstance();
        String path = target.getClass().getName();
        path = path.substring(0, path.lastIndexOf("."));
        //获取该class下所有属性 取目标所需要的list
        Field[] tFields = target.getClass().getDeclaredFields();
        //循环所有属性
        for (Field tField : tFields) {
            //根据目标bean的路径是否包含是否相同去判断是不是属于子bean
            if (tField.getType().getName().contains(path)) {
                //属性名称
                String name = tField.getName();
                //取源数据的同名bean 如果没有该字段跳过，不阻断流程
                Field sField = null;
                try {
                    sField = source.getClass().getDeclaredField(name);
                } catch (NoSuchFieldException e) {
                    continue;
                }
                if (null != sField) {
                    sField.setAccessible(true);
                    tField.setAccessible(true);
                    //获取源数据
                    Object s = sField.get(source);
                    if (null != s) {
                        //实例化目标bean
                        Object t = getBean(tField.getType().getName());
                        //bean拷贝
                        copyBean(s, t.getClass());
                        tField.set(target, t.getClass());
                    }
                }
            }
        }
    }

    /**
     * 根据结构和配置信息组织数据
     * 李红欣
     *
     * @param source 源数据
     * @param clazzt 目标数据
     */
    public static <T> T orgData(@NonNull Object source, @NonNull Class<T> clazzt) throws Exception {
        try {
            return copyBean(source, clazzt);
        } catch (Exception e) {
            log.error("orgData is error", e);
            throw new Exception("CicBeanUtil组织数据出错", e);
        }
    }

    /**
     * 拷贝对象中的list属性
     * 必要条件1、都是list，2、属性名称必须相同
     *
     * @param source 源对象
     * @param target 目标对象
     * @throws Exception
     */
    public static void copyListProperty(Object source, Object target) throws Exception {
        //获取该class下所有属性 取目标所需要的list
        Field[] tFields = target.getClass().getDeclaredFields();
        //循环所有属性
        for (Field tField : tFields) {
            //如果是list
            if (isList(tField.getType().getTypeName())) {
                Type tType = tField.getGenericType();
                try {
                    //取同名属性
                    Field sField = source.getClass().getDeclaredField(tField.getName());
                    //同样为list
                    if (isList(sField.getType().getTypeName())) {
                        //必须是参数化的类型
                        if (tType instanceof ParameterizedType) {
                            ParameterizedType t = (ParameterizedType) tType;
                            String listType = t.getActualTypeArguments()[0].getTypeName();
                            // 参数值为true，禁止访问控制检查
                            sField.setAccessible(true);
                            tField.setAccessible(true);
                            //获取属性值
                            List sValue = (List) sField.get(source);
                            if (null != sValue && sValue.size() > 0) {
                                List tValue = new ArrayList();
                                for (int i = 0; i < sValue.size(); i++) {
                                    //实例化
                                    Object data = getBean(listType);
                                    //拷贝
                                    copyBean(sValue.get(i), data.getClass());
                                    tValue.add(data);
                                }
                                tField.set(target, tValue);
                            }
                        }
                    }

                } catch (NoSuchFieldException e) {
                    log.error("CicBeanUtil Not Found {} : ", source.getClass().getName(), tField.getName());
                }

            }
        }
    }

    /**
     * 判断是否list
     *
     * @param type
     * @return
     */
    private static boolean isList(String type) {
        if (List.class.getTypeName().equals(type) || ArrayList.class.getTypeName().equals(type)) {
            return true;
        } else {
            return false;
        }
    }
}