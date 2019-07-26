package com.jerry.lab.algorithm.knapsack;

import com.rits.cloning.Cloner;

public class KnapsackProblemDynamicPlanning {
    static int[] items = new int[]{2, 2, 4, 6, 3};//物品
    static int weight = 9;//背包承重
    static Cloner cloner = new Cloner();
    static Knapsack[] states = new Knapsack[weight + 1];

    public static void main(String[] args) {
        // 第一行数据特殊处理
        states[0] = new Knapsack(weight);
        if (items[0] < weight) {
            states[items[0]] = new Knapsack(weight);
        }

        // 遍历物品
        for (int itemIndex = 1; itemIndex < items.length; itemIndex++) {
            // 将当前物品装入背包
            for (int j = weight - items[itemIndex]; j >= 0; j--) {
                if (states[j] != null) {
                    Knapsack knapsack = cloner.deepClone(states[j]);
                    knapsack.add(items[itemIndex]);
                    states[j + items[itemIndex]] = knapsack;
                }
            }
        }

        for (int i = 0; i < states.length; i++) {
            System.out.println(i + " -> " + (states[i] != null ? states[i].getItemsInKnapsack() : "-"));
        }

    }

}


