package com.jerry.lab.serialize;

import java.io.*;

public class SerializeUtil {
    public static <T> void serialize(T obj, String fileName) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(obj);
            System.out.println("序列化成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T deserialize(String fileName) {
        T result;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            result = (T) inputStream.readObject();
            System.out.println("反序列化成功");
            return result;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
