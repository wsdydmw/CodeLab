package com.jerry.lab.sort;

import java.util.Arrays;

public class HeapSort extends SortAdapter {

    public void execute() {
        sort(this._array, this._array.length - 1);

        if (!super.isSorted(_array)) {
            Arrays.stream(this._array).forEach(a -> {
                System.out.print(a + " ");
            });
        }
    }

    public void sort(Integer[] array, int lastIndex) {
        // 1. 建堆
        buildHeap(array, lastIndex);

        // 2. 不断将最大值交换至数组末尾，然后重新进行堆化
        for (int sortedIndex = lastIndex; sortedIndex > 0; sortedIndex--) {
            swap(array, 0, sortedIndex);
            downwardHeapify(array, sortedIndex - 1, 0);
        }
    }

    private void buildHeap(Integer[] array, int lastIndex) {
        // 从非叶子节点开始，从后往前，向下堆化
        for (int index = lastIndex / 2; index >= 0; --index) {
            downwardHeapify(array, lastIndex, index);
        }
    }

    private void downwardHeapify(Integer[] array, int lastIndex, int index) {
        while (true) {
            int tempMaxDataIndex = index;
            if (index * 2 + 1 <= lastIndex && array[tempMaxDataIndex] < array[index * 2 + 1]) {
                tempMaxDataIndex = index * 2 + 1;
                addOperateCount();
            }
            if (index * 2 + 2 <= lastIndex && array[tempMaxDataIndex] < array[index * 2 + 2]) {
                tempMaxDataIndex = index * 2 + 2;
                addOperateCount();
            }
            if (tempMaxDataIndex == index) break;//已经是最大堆了，堆化结束

            swap(array, index, tempMaxDataIndex);
            index = tempMaxDataIndex;//继续往下堆化
        }
    }

    private void swap(Integer[] array, int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
        addOperateCount();
    }

}
