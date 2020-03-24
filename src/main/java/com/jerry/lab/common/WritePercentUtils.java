package com.jerry.lab.common;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WritePercentUtils {
    @Data
    public static class Result {
        String objectName;
        int writePercent;
        Long ops;
        String safe = "Safe";
    }

    public static void showResult(Object[] targetObjects, List<Result> results) {
        System.out.println("---- 汇总结果 ----");
        // 标题
        System.out.print("write%\t");
        IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
            System.out.print(Utils.getClassName(targetObjects[index]) + "\t");
        });
        System.out.println();

        // 每行
        results.stream().collect(Collectors.groupingBy(Result::getWritePercent, Collectors.groupingBy(Result::getObjectName, Collectors.toList())))
                .entrySet().forEach(entry -> {
            System.out.print(entry.getKey() + "%\t");//write percent
            Map value = entry.getValue();//objectName -> results
            IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
                String objectName = Utils.getClassName(targetObjects[index]);
                List<Result> resultList = (List<Result>)value.get(objectName);
                long avgCostTime = resultList.stream().collect(Collectors.averagingLong(Result :: getOps)).longValue();
                if (resultList.stream().anyMatch(result -> {return !StringUtils.equalsIgnoreCase(result.getSafe(), "Safe");})) {
                    System.out.print(avgCostTime + "(Unsafe)" + "\t");
                } else {
                    System.out.print(avgCostTime + "\t");
                }

            });
            System.out.println();
        });
    }


}
