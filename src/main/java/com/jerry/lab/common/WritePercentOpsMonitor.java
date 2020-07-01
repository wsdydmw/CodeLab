package com.jerry.lab.common;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WritePercentOpsMonitor {
    public static void showResult(Object[] targetObjects, List<Result> results) {
        System.out.println("---- 汇总结果 ----");
        // 1. 标题 如： write%	Vector	Collections$SynchronizedRandomAccessList	CopyOnWriteArrayList
        System.out.print("write%\t");
        // 1.1 _read
        IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
            System.out.print(Utils.getClassName(targetObjects[index]) + "_READ" + "\t");
        });
        // 1.2 _write
        IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
            System.out.print(Utils.getClassName(targetObjects[index]) + "_WRITE");
            System.out.print((index == targetObjects.length - 1 ? "" : "\t"));
        });
        System.out.println();

        // 每行 如：0%	675216	753984	749478
        results.stream().collect(Collectors.groupingBy(Result::getWritePercent, Collectors.groupingBy(Result::getObjectName, Collectors.toList())))
                .entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(entry -> {
            System.out.print(entry.getKey() + "%\t");//write percent
            Map value = entry.getValue();//objectName -> results
            // read ops
            IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
                String objectName = Utils.getClassName(targetObjects[index]);
                List<Result> resultList = (List<Result>) value.get(objectName);
                long avgCostTime = resultList.stream().collect(Collectors.averagingLong(Result::getReadOps)).longValue();
                if (resultList.stream().anyMatch(result -> {
                    return !StringUtils.equalsIgnoreCase(result.getSafe(), "Safe");
                })) {
                    System.out.print(avgCostTime + "(Unsafe)" + "\t");
                } else {
                    System.out.print(avgCostTime + "\t");
                }

            });

            // write ops
            IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
                String objectName = Utils.getClassName(targetObjects[index]);
                List<Result> resultList = (List<Result>) value.get(objectName);
                long avgCostTime = resultList.stream().collect(Collectors.averagingLong(Result::getWriteOps)).longValue();
                if (resultList.stream().anyMatch(result -> {
                    return !StringUtils.equalsIgnoreCase(result.getSafe(), "Safe");
                })) {
                    System.out.print(avgCostTime + "(Unsafe)");
                } else {
                    System.out.print(avgCostTime);
                }
                System.out.print((index == targetObjects.length - 1 ? "" : "\t"));

            });
            System.out.println();
        });
    }

    @Data
    public static class Result {
        String objectName;
        int writePercent;
        long readOps = -1;
        long writeOps = -1;
        String safe = "Safe";
    }


}
