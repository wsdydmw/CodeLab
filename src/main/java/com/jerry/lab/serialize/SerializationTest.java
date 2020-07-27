package com.jerry.lab.serialize;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class SerializationTest {
    public static void main(String[] args) {
        String fileName = "files/serialize/period";
        Date now = new Date();
        Date yesterday = Date.from(now.toInstant().minus(1, ChronoUnit.DAYS));
        Period period1 = new Period(yesterday, now);

        System.out.println("开始序列化");
        SerializeUtil.serialize(period1, fileName);
        System.out.println("开始反序列化");
        SerializeUtil.<Period>deserialize(fileName);
    }
}

class Period implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
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

    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        System.out.println("进入writeObject");
        oos.writeObject(start);
        oos.writeObject(end);
    }

    private void readObject(ObjectInputStream ois) throws InvalidObjectException {
        try {
            System.out.println("进入readObject");
            Date start = (Date) ois.readObject();
            Date end = (Date) ois.readObject();

            //进行有效性验证，防止字节码被恶意更改
            if (null == start || null == end || start.after(end)) {
                throw new IllegalArgumentException("请传入正确的时间区间!");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}