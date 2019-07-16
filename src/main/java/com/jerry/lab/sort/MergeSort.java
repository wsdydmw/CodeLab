package com.jerry.lab.sort;

public class MergeSort extends SortAdapter {

    public void execute() {
        Integer[] result = sort(this._array, 0, this._array.length - 1);

        if (!super.isSorted(result)) {
            System.err.println("sort failed");
        }
    }

    private Integer[] sort(Integer[] array, int low, int high) {
        if (low == high) {
            return new Integer[]{array[low]};
        }

        int mid = low + (high - low) / 2;
        addOperateCount();

        Integer[] leftArr = sort(array, low, mid); // 左有序数组
        Integer[] rightArr = sort(array, mid + 1, high); // 右有序数组
        Integer[] result = new Integer[leftArr.length + rightArr.length]; // 新有序数组

        int newIndex = 0, leftIndex = 0, rightIndex = 0;
        while (leftIndex < leftArr.length && rightIndex < rightArr.length) {// 比较左右有序数组，选择较小值放入新数组
            result[newIndex++] = leftArr[leftIndex] < rightArr[rightIndex] ? leftArr[leftIndex++] : rightArr[rightIndex++];
            addOperateCount();
        }

        while (leftIndex < leftArr.length) {
            result[newIndex++] = leftArr[leftIndex++];
            addOperateCount();
        }
        while (rightIndex < rightArr.length) {
            result[newIndex++] = rightArr[rightIndex++];
            addOperateCount();
        }
        return result;
    }
}
