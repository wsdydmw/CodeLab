package com.jerry.lab.common;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueueLengthOpsMonitor {
    public static void showResult(List<Result> results) {
        System.out.println("---- 汇总结果 ----");
        // 标题
        System.out.print("Length\t");
        List<String> queueNames = results.stream()
                .sorted(Comparator.comparingInt(Result::getDisplayOrder))
                .map(Result::getQueueName)
                .distinct()
                .collect(Collectors.toList());

        IntStream.range(0, queueNames.size()).forEachOrdered(index -> {
            System.out.print(queueNames.get(index) + "\t");
        });
        System.out.println();

        // 每行
        results.stream().collect(Collectors.groupingBy(Result::getQueueLength, Collectors.groupingBy(Result::getQueueName, Collectors.toList())))
                .entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(entry -> {
            System.out.print(entry.getKey() + "\t");//queue length
            Map value = entry.getValue();//queueName -> results
            IntStream.range(0, queueNames.size()).forEachOrdered(index -> {
                String queueName = queueNames.get(index);
                List<Result> resultList = (List<Result>) value.get(queueName);
                long avgCostTime = resultList.stream().collect(Collectors.averagingLong(Result::getOps)).longValue();
                System.out.print(avgCostTime + "\t");

            });
            System.out.println();
        });
    }

    @Data
    public static class Result {
        String objectName;
        int queueLength;
        Long ops;

        public String getQueueName() {
            return StringUtils.substringAfter(objectName, ":");
        }

        public int getDisplayOrder() {
            return Integer.parseInt(StringUtils.substringBefore(objectName, ":"));
        }
    }


}
