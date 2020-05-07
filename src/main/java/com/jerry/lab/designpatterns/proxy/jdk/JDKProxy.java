package com.jerry.lab.designpatterns.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JDKProxy {

    public static Object getPoxyObject(final Object c) {

        return Proxy.newProxyInstance(c.getClass().getClassLoader(), c
                        .getClass().getInterfaces(),// JDK实现动态代理，但JDK实现必须需要接口
                new InvocationHandler() {

                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {
                        Object reObj = null;
                        System.out.println("方法执行前: ");
                        reObj = method.invoke(c, args);
                        System.out.println("方法执行后: ");
                        return reObj;
                    }
                });
    }
}
