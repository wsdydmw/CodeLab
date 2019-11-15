package com.jerry.lab.sort;

import com.jerry.lab.common.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SortApplication {
    static int MAX_NUMBER_SIZE = 10000;// the size of numbers to be sorted

    public static void main(String[] args) throws IOException {
        long begin, end;
        try (BufferedReader random_br = new BufferedReader(new FileReader("files/sort/random_numbers_1000000"));
             BufferedReader asc_br = new BufferedReader(new FileReader("files/sort/asc_numbers_1000000"));
             BufferedReader desc_br = new BufferedReader(new FileReader("files/sort/desc_numbers_1000000"))) {

            List<Integer> random_numbers_list = Arrays.stream(random_br.readLine().split(",")).limit(MAX_NUMBER_SIZE).map(Integer::valueOf).collect(Collectors.toList());
            List<Integer> asc_numbers_list = Arrays.stream(asc_br.readLine().split(",")).limit(MAX_NUMBER_SIZE).map(Integer::valueOf).collect(Collectors.toList());
            List<Integer> desc_numbers_list = Arrays.stream(desc_br.readLine().split(",")).limit(MAX_NUMBER_SIZE).map(Integer::valueOf).collect(Collectors.toList());

            SortAdapter[] sorts = new SortAdapter[]{new HeapSort(), new InsertSort(), new MergeSort(), new QuickSort(), new ShellSort()};

            System.out.println("Sort Type | random numbers | asc numbers | desc numbers");
            System.out.println("-- | -- | -- | --");
            for (SortAdapter sortAdapter : sorts) {
                Integer[] random_numbers = random_numbers_list.toArray(new Integer[random_numbers_list.size()]);
                Integer[] asc_numbers = asc_numbers_list.toArray(new Integer[asc_numbers_list.size()]);
                Integer[] desc_numbers = desc_numbers_list.toArray(new Integer[desc_numbers_list.size()]);

                System.out.print(StringUtils.substringAfterLast(sortAdapter.getClass().toString(), ".") + " | ");
                sortAdapter.loadArray(random_numbers);

                begin = System.currentTimeMillis();
                sortAdapter.execute();
                end = System.currentTimeMillis();

                System.out.print(Utils.displayNumber(sortAdapter.getOperateCount()) + "(" + Utils.displayNumber(end - begin) + "ms) | ");

                sortAdapter.loadArray(asc_numbers);
                begin = System.currentTimeMillis();
                sortAdapter.execute();
                end = System.currentTimeMillis();

                System.out.print(Utils.displayNumber(sortAdapter.getOperateCount()) + "(" + Utils.displayNumber(end - begin) + "ms) | ");

                sortAdapter.loadArray(desc_numbers);
                begin = System.currentTimeMillis();
                sortAdapter.execute();
                end = System.currentTimeMillis();

                System.out.println(Utils.displayNumber(sortAdapter.getOperateCount()) + "(" + Utils.displayNumber(end - begin) + "ms)");
            }

        }

    }

}
