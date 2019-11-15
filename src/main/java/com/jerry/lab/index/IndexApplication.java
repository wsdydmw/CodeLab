package com.jerry.lab.index;

import com.jerry.lab.common.Utils;
import com.jerry.lab.sort.DataGenerator;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndexApplication {
    static int MAX_NUMBER_SIZE = 1000; // the size of numbers to be indexed
    static int EXECUTE_TIMES = 20;

    public static void main(String[] args) throws IOException {
        try (BufferedReader random_br = new BufferedReader(new FileReader("files/sort/random_numbers_1000000"))) {
            List<Integer> random_numbers_list = Arrays.stream(random_br.readLine().split(",")).limit(MAX_NUMBER_SIZE).map(Integer::valueOf).collect(Collectors.toList());

            IndexAdapter[] indexAdapters = new IndexAdapter[]{new BinarySearchArray(), new BinarySearchTree(), new AvlTree()};

            System.out.println("Index Type | Insert | Find | Delete");
            System.out.println("-- | -- | -- | --");
            for (IndexAdapter indexAdapter : indexAdapters) {
                Integer[] random_numbers = random_numbers_list.toArray(new Integer[random_numbers_list.size()]);
                indexAdapter.load(random_numbers);

                System.out.print(StringUtils.substringAfterLast(indexAdapter.getClass().toString(), ".") + " | ");

                String insertResult = execute(indexAdapter, 1);
                System.out.print(Utils.displayNumber(Long.parseLong(StringUtils.substringBefore(insertResult, ":")) / EXECUTE_TIMES) +
                        "(" +
                        Utils.displayNumber(Long.parseLong(StringUtils.substringAfter(insertResult, ":")) / EXECUTE_TIMES) +
                        "ns) | ");

                String findResult = execute(indexAdapter, 2);
                System.out.print(Utils.displayNumber(Long.parseLong(StringUtils.substringBefore(findResult, ":")) / EXECUTE_TIMES) +
                        "(" +
                        Utils.displayNumber(Long.parseLong(StringUtils.substringAfter(findResult, ":")) / EXECUTE_TIMES) +
                        "ns) | ");

                String deleteResult = execute(indexAdapter, 3);
                System.out.println(Utils.displayNumber(Long.parseLong(StringUtils.substringBefore(deleteResult, ":")) / EXECUTE_TIMES) +
                        "(" +
                        Utils.displayNumber(Long.parseLong(StringUtils.substringAfter(deleteResult, ":")) / EXECUTE_TIMES) +
                        "ns)");
            }

        }
    }

    public static String execute(IndexAdapter indexAdapter, int type) {
        String result = Stream.generate(DataGenerator::getRandomNumber).limit(EXECUTE_TIMES)
                .<String>map(key -> {
                    CountUtil.initOperateCount();
                    long begin = System.nanoTime();

                    switch (type) {
                        case 1:
                            indexAdapter.insert(key);
                            break;
                        case 2:
                            indexAdapter.find(key);
                            break;
                        case 3:
                            indexAdapter.delete(key);
                            break;
                    }

                    long end = System.nanoTime();
                    return CountUtil.getOperateCount() + ":" + (end - begin);
                }).<String>reduce("0:0", (result1, result2) -> {
                    return String.valueOf(Integer.parseInt(StringUtils.substringBefore(result1, ":")) +
                            Integer.parseInt(StringUtils.substringBefore(result2, ":"))) +
                            ":" +
                            String.valueOf(Integer.parseInt(StringUtils.substringAfter(result1, ":")) +
                                    Integer.parseInt(StringUtils.substringAfter(result2, ":")));
                });

        return result;
    }

}
