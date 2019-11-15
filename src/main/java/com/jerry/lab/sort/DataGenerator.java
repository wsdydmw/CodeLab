package com.jerry.lab.sort;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DataGenerator {
    static int maxValue = 1000000; // for range
    ;

    public static void main(String[] args) {
        int size = 100000;

        try (BufferedWriter bw1 = new BufferedWriter(new FileWriter("files/sort/random_numbers_100000"));
             BufferedWriter bw2 = new BufferedWriter(new FileWriter("files/sort/asc_numbers_100000"));
             BufferedWriter bw3 = new BufferedWriter(new FileWriter("files/sort/desc_numbers_100000"))
        ) {
            StringBuffer sb1 = new StringBuffer();
            StringBuffer sb2 = new StringBuffer();
            StringBuffer sb3 = new StringBuffer();

            for (int i = 1; i <= size; i++) {
                sb1.append(getRandomNumber() + ",");
                sb2.append(i + ",");
                sb3.append((maxValue - i + 1) + ",");
            }

            bw1.write(StringUtils.substringBeforeLast(sb1.toString(), ","));
            bw2.write(StringUtils.substringBeforeLast(sb2.toString(), ","));
            bw3.write(StringUtils.substringBeforeLast(sb3.toString(), ","));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getRandomNumber() {
        return (int) (Math.random() * maxValue);
    }
}
