package com.jerry.lab.common;

import java.text.NumberFormat;

public class Utils {
    static NumberFormat numberFormat;

    static {
        numberFormat = NumberFormat.getInstance();
    }

    public static String displayNumber(long number) {
        return numberFormat.format(number);
    }

    public static void calNumber(long number) {
        for(int i = 2; i <=Math.sqrt(number); i++){
            if (number % i == 0)
                return;
        }
        /*for (int i = 0 ; i < 1000; i++) {
            if (Math.pow(number, 10) > 0.8)  {
                return;
            }
        }*/
    }

    public static boolean isEqual (double d1, double d2) {
        return Math.abs(d2 -d1) < 0.01 ? true : false;
    }
}
