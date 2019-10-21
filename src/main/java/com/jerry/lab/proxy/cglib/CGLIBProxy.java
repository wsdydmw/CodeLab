package com.jerry.lab.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGLIBProxy {

    public static Object getPoxyObject(Object c) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(c.getClass());
        enhancer.setCallback(new MethodInterceptor() {

            public Object intercept(Object arg0, Method arg1, Object[] arg2,
                                    MethodProxy proxy) throws Throwable {
                System.out.println("方法执行前: ");
                proxy.invokeSuper(arg0, arg2);
                System.out.println("方法执行后: ");
                return null;
            }
        });

        return enhancer.create();
    }
}
