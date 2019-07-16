package com.jerry.lab.sort;

public class QuickSort extends SortAdapter {

    public void execute() {
        sort(this._array, 0, this._array.length - 1);

        if (!super.isSorted(_array)) {
            System.err.println("sort failed");
        }
    }

    private void sort(Integer[] array, int low, int high) {
        if (low < high) {//使用递归
            int middle = partition(array, low, high);

            sort(array, low, middle - 1);
            sort(array, middle + 1, high);
        } else {
            return;
        }
    }

    /**
     * 通过一趟扫描，将待排序数据分成两部分，左边比基准元素小，右边比基准元素大
     *
     * @param array
     * @param low
     * @param high
     * @return 基准元素下标
     */
    private int partition(Integer[] array, int low, int high) {
        int middleValue = array[low];//将数组第一个元素作为中轴
        addOperateCount();

        while (low < high) {
            while (low < high && array[high] >= middleValue) {
                high--;
                addOperateCount();
            }
            array[low] = array[high];//比中轴小的记录移到低端
            addOperateCount();

            while (low < high && array[low] < middleValue) {
                low++;
                addOperateCount();
            }
            array[high] = array[low];//比中轴大的记录移到高端
            addOperateCount();
        }

        array[low] = middleValue;//中轴记录写回
        addOperateCount();

        return low;
    }
}
