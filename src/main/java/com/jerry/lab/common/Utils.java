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
}
