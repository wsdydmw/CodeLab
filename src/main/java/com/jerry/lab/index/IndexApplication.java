package com.jerry.lab.index;

import com.jerry.lab.sort.DataGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IndexApplication {
    static int MAX_NUMBER_SIZE = 10000;

    public static void main(String[] args) throws IOException {
        try (BufferedReader random_br = new BufferedReader(new FileReader("data/random_numbers"))) {
            List<Integer> random_numbers_list = Arrays.stream(random_br.readLine().split(",")).limit(MAX_NUMBER_SIZE).map(Integer::valueOf).collect(Collectors.toList());

            Integer[] random_numbers = random_numbers_list.toArray(new Integer[random_numbers_list.size()]);

            IndexAdapter index = getIndexInstance();
            index.load(random_numbers);
            System.out.println("loaded number size : " + random_numbers.length);

            for (int i = 0; i < 30; i++) {
                int key = DataGenerator.getRandomNumber();
                if (i / 10 == 0) {
                    CountUtil.initOperateCount();
                    index.insertWithCheck(key);
                    System.out.println("insert " + key + ", operate count : " + CountUtil.getOperateCount());
                } else if (i / 10 == 1) {
                    CountUtil.initOperateCount();
                    System.out.println("find " + key + " at " + index.find(key) + ", operate count : " + CountUtil.getOperateCount());
                } else {
                    CountUtil.initOperateCount();
                    index.deleteWithCheck(key);
                    System.out.println("delete " + key + ", operate count : " + CountUtil.getOperateCount());
                }
            }
        }
    }

    public static IndexAdapter getIndexInstance() {
        return new BinarySearchTree();
    }
}
