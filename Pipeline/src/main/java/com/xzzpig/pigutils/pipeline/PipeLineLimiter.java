package com.xzzpig.pigutils.pipeline;

public interface PipeLineLimiter<T> {
	/**
	 * @param t
	 *            上次从Creater中取出的元素
	 * @return 返回true时停止从Creater中取元素
	 */
    boolean isEnd(T t);
}
