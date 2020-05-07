package com.jerry.lab.designpatterns.proxy;

import com.jerry.lab.designpatterns.proxy.cglib.CGLIBProxy;
import com.jerry.lab.designpatterns.proxy.jdk.JDKProxy;
import com.jerry.lab.designpatterns.proxy.service.CountImpl;
import com.jerry.lab.designpatterns.proxy.service.ICount;

public class ProxyTest {

    public static void main(String args[]) {
        //JDK
        ICount count = new CountImpl();
        ICount countProxy = (ICount) JDKProxy.getPoxyObject(count);//这里必须使用接口
        countProxy.queryCount();

        //Cglib
        CountImpl count2 = new CountImpl();
        CountImpl countCglibProxy = (CountImpl) CGLIBProxy.getPoxyObject(count2);
        countCglibProxy.queryCount();
    }
}
