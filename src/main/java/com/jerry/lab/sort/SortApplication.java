package com.jerry.lab.sort;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SortApplication {

    public static void main(String[] args) throws IOException {
        long begin, end;
        try (BufferedReader random_br = new BufferedReader(new FileReader("data/random_numbers"));
             BufferedReader asc_br = new BufferedReader(new FileReader("data/asc_numbers"));
             BufferedReader desc_br = new BufferedReader(new FileReader("data/desc_numbers"))) {
            List<Integer> random_numbers_list = Arrays.stream(random_br.readLine().split(",")).map(Integer::valueOf).collect(Collectors.toList());
            List<Integer> asc_numbers_list = Arrays.stream(asc_br.readLine().split(",")).map(Integer::valueOf).collect(Collectors.toList());
            List<Integer> desc_numbers_list = Arrays.stream(desc_br.readLine().split(",")).map(Integer::valueOf).collect(Collectors.toList());

            Integer[] random_numbers = random_numbers_list.toArray(new Integer[random_numbers_list.size()]);
            Integer[] asc_numbers = asc_numbers_list.toArray(new Integer[asc_numbers_list.size()]);
            Integer[] desc_numbers = desc_numbers_list.toArray(new Integer[desc_numbers_list.size()]);

            System.out.println("--- randow number result ---");
            SortAdapter sort1 = getSortInstance();
            sort1.loadArray(random_numbers);

            begin = System.currentTimeMillis();
            sort1.execute();
            end = System.currentTimeMillis();

            System.out.println("numbers size : " + random_numbers.length);
            System.out.println("operate count : " + sort1.getOperateCount());
            System.out.println("time use : " + (end - begin) + "ms");

            System.out.println("--- asc number result ---");
            SortAdapter sort2 = getSortInstance();
            sort2.loadArray(asc_numbers);

            begin = System.currentTimeMillis();
            sort2.execute();
            end = System.currentTimeMillis();

            System.out.println("numbers size : " + asc_numbers.length);
            System.out.println("operate count : " + sort2.getOperateCount());
            System.out.println("time use : " + (end - begin) + "ms");

            System.out.println("--- desc number result ---");
            SortAdapter sort3 = getSortInstance();
            sort3.loadArray(desc_numbers);

            begin = System.currentTimeMillis();
            sort3.execute();
            end = System.currentTimeMillis();

            System.out.println("numbers size : " + desc_numbers.length);
            System.out.println("operate count : " + sort3.getOperateCount());
            System.out.println("time use : " + (end - begin) + "ms");
        }

    }

    public static SortAdapter getSortInstance() {
        return new HeapSort();
    }
}
