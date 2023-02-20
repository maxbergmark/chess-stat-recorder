package com.chessanalysis.statrecorder;

public interface MergeableData<T> {
    T merge(T other);
}
