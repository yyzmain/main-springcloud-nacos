package com.main.utils;

import java.time.Instant;

/**
 * @author yyz
 * @description:
 * @date 2022/1/14 10:46
 */
public class MainDurTimeUtils {

    private MainDurTimeUtils() {
    }

    /**
     * @return 当前毫秒数
     */
    public static long nowMs() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 当前毫秒与起始毫秒差
     *
     * @param startMillis 开始纳秒数
     * @return 时间差
     */
    public static long diffMs(long startMillis) {
        return nowMs() - startMillis;
    }


}
