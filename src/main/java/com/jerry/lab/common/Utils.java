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
    }
}
