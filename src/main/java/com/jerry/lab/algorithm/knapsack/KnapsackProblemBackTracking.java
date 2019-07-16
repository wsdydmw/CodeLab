package com.jerry.lab.algorithm.knapsack;

import com.rits.cloning.Cloner;

public class KnapsackProblemBackTracking {
    static int[] items = new int[]{3, 3, 4, 6};//物品
    static int weight = 9;//背包承重
    static Cloner cloner = new Cloner();
    static boolean[][] mem = new boolean[items.length][weight + 1];//默认false

    public static void main(String[] args) {
        inspect(0, new Knapsack(weight));
    }

    public static void inspect(int index, Knapsack knapsack) {
        if (knapsack.isFull() || index == items.length) { // 表示装满或已经考察完所有的物品
            System.out.println("结果：" + knapsack.getItemsInKnapsack() + " -> " + knapsack.getCurrentWeight());
            return;
        }

        // 查询备忘录
        int currentWeight = knapsack.getCurrentWeight();
        if (mem[index][currentWeight]) {
            System.out.println("备忘：" + index + " -> " + currentWeight);
            return;
        } else {
            mem[index][currentWeight] = true;
        }

        // case1:当前物品不装入背包
        Knapsack knapsack1 = cloner.deepClone(knapsack);
        inspect(index + 1, knapsack1);

        // case2:当前物品装入背包
        Knapsack knapsack2 = cloner.deepClone(knapsack);
        if (knapsack2.canAdd(items[index])) {// 如果还可以装
            knapsack2.add(items[index]);
            inspect(index + 1, knapsack2);
        } else {
            knapsack2.add(items[index]);
            System.out.println("剪枝：" + knapsack2.getItemsInKnapsack() + " -> " + knapsack2.getCurrentWeight());
        }
    }
}


