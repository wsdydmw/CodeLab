package com.jerry.lab.sort;

public class ShellSort extends SortAdapter {

    public void execute() {
        sort(this._array);

        if (!super.isSorted(_array)) {
            System.err.println("sort failed");
        }
    }

    private void sort(Integer[] array) {
        for (int step = array.length; step > 0; step = (int) Math.ceil(step / 2)) {//步长控制
            for (int groupNo = 0; groupNo < step; groupNo++) {
                for (int currentIndex = groupNo + step; currentIndex < array.length; currentIndex += step) {
                    int current = array[currentIndex];
                    int cursorIndex = currentIndex - step;
                    addOperateCount();

                    // 将大于current的值整体后移一个单位
                    for (; cursorIndex >= 0 && current < array[cursorIndex]; cursorIndex -= step) {
                        array[cursorIndex + step] = array[cursorIndex];
                        addOperateCount();
                    }

                    if (currentIndex != cursorIndex + step) {
                        array[cursorIndex + step] = current; // 插入current值到正确位置
                        addOperateCount();
                    }
                }
            }
        }
    }
}
