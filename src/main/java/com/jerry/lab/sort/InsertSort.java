package com.jerry.lab.sort;

public class InsertSort extends SortAdapter {

    public void execute() {
        sort(this._array);

        if (!super.isSorted(_array)) {
            System.err.println("sort failed");
        }
    }

    private void sort(Integer[] array) {
        for (int currentIndex = 1; currentIndex < array.length; currentIndex++) {
            int current = array[currentIndex];
            addOperateCount();

            int cursorIndex = currentIndex - 1;// 已排序数组的最后位置下标
            addOperateCount();
            // 将大于current的值整体后移一位
            for (; cursorIndex >= 0 && current < array[cursorIndex]; cursorIndex--) {
                array[cursorIndex + 1] = array[cursorIndex];
                addOperateCount();
            }

            if (currentIndex != cursorIndex + 1) {
                array[cursorIndex + 1] = current; // 将current插入到正确位置
                addOperateCount();
            }
        }
    }
}
