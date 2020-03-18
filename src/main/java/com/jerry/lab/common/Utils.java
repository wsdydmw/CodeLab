package com.jerry.lab.common;

import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;
import java.util.List;

public class Utils {
    static NumberFormat numberFormat;

    static {
        numberFormat = NumberFormat.getInstance();
    }

    public static String displayNumber(long number) {
        return numberFormat.format(number);
    }

    public static void calNumber(long number) {
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0)
                return;
        }
    }

    public static void shuffleList(List list) {
        for (int i = 0; i < list.size(); i++) {
            int exchangeKey = (int) (Math.random() * list.size());
            Object old = list.get(i);
            list.set(i, list.get(exchangeKey));
            list.set(exchangeKey, old);
        }
    }

    public static String getClassName(Object object) {
        return StringUtils.substringAfterLast(object.getClass().toString(), ".");
    }
}
