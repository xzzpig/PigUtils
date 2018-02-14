package com.xzzpig.pigutils.pipeline;

public interface PipeLineSolver<T, R> {
    R solve(T t);
}
