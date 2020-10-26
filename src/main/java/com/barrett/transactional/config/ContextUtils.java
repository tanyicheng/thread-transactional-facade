package com.barrett.transactional.config;

import org.springframework.context.ApplicationContext;

/**
 * //TODO spring 上下文工具类
 * @Author barrett
 * @Date 2020/10/24 20:40
 **/
public class ContextUtils {

    /**上下文*/
    private static ApplicationContext applicationContext;

    /**
     * 初始化上下文
     */
    static void setApplicationContext(ApplicationContext applicationContext) {
        ContextUtils.applicationContext = applicationContext;
    }

    /**
     * 获取上下文
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 根据name获取bean
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 根据klass获取bean
     * @param klass klass类型
     * @param <T>   klass泛型
     * @return      返回 spring 容器中的实例
     */
    public static <T> T getBean(Class<T> klass) {
        return getApplicationContext().getBean(klass);
    }
}
