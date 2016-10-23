package com.example.localbroadcast_eventbus;

import android.util.SparseIntArray;
import android.util.SparseLongArray;

public class Stats {

    private SparseIntArray countArray = new SparseIntArray(4);
    private SparseLongArray elapsedArray = new SparseLongArray(4);

    double getAvg(long elapsedNs, int mode) {
        long totalElapsed = elapsedArray.get(mode, 0);
        totalElapsed += elapsedNs;
        elapsedArray.put(mode, totalElapsed);

        int count = countArray.get(mode, 0);
        countArray.put(mode, ++count);

        return totalElapsed / (double) count;
    }

}
