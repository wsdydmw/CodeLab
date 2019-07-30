package com.jerry.lab.index;

import java.util.Arrays;

public abstract class IndexAdapter implements IIndexable, ICheckable {

    public void load(Integer[] array) {
        Arrays.stream(array).forEach(key -> {
            insert(key);
        });
    }

    public void insertWithCheck(int key) {
        insert(key);
        assert check();
    }

    public void deleteWithCheck(int key) {
        delete(key);
        assert check();
    }

}
