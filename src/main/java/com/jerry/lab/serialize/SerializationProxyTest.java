package com.jerry.lab.serialize;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class SerializationProxyTest {
    public static void main(String[] args) {
        Date now = new Date();
        Date yesterday = Date.from(now.toInstant().minus(1, ChronoUnit.DAYS));
        PeriodWithProxy period1 = new PeriodWithProxy(yesterday, now);

        SerializeUtil.serialize(period1, "files/serialize/period");
        SerializeUtil.<Period>deserialize("files/serialize/period");
    }
}

class PeriodWithProxy implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Date start;
    private final Date end;

    public PeriodWithProxy(Date start, Date end) {
        //进行有效性验证
        if (null == start || null == end || start.after(end)) {
            throw new IllegalArgumentException("请传入正确的时间区间!");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "起始时间：" + start + " , 结束时间：" + end;
    }

    /**
     * 序列化外围类时，调用此方法，其实只是序列化了一个内部的代理类对象
     */
    private Object writeReplace() {
        System.out.println("进入writeReplace()方法");
        return new SerializabtionProxy(this);
    }

    /**
     * 如果攻击者伪造了一个外围类的字节码文件，在反序列化时，会因调用readObject方法产生异常
     */
    private void readObject(ObjectInputStream ois) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required!");
    }

    /**
     * 序列化代理类，其精确表示了当前外围类对象的状态
     */
    private static class SerializabtionProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Date start;
        private final Date end;

        SerializabtionProxy(PeriodWithProxy p) {
            this.start = p.start;
            this.end = p.end;
        }

        /**
         * 反序列化时，调用此方法，通过调用Period的构造函数，进行有效性校验
         */
        private Object readResolve() {
            System.out.println("进入readResolve()方法");
            return new Period(new Date(start.getTime()), new Date(end.getTime()));
        }

    }
}