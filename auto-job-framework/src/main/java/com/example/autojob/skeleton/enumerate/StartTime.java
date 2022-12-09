package com.example.autojob.skeleton.enumerate;

import com.example.autojob.skeleton.framework.config.TimeConstant;

/**
 * 提供部分默认启动时间
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/13 9:53
 */
public enum StartTime {
    /**
     * 现在
     */
    NOW(System.currentTimeMillis() + 5000),

    /**
     * 稍后一分钟
     */
    THEN_ONE_MINUTE(System.currentTimeMillis() + TimeConstant.A_MINUTE),

    /**
     * 稍后三分钟
     */
    THEN_THREE_MINUTES(System.currentTimeMillis() + TimeConstant.A_MINUTE * 3),

    /**
     * 稍后五分钟
     */
    THEN_FIVE_MINUTES(System.currentTimeMillis() + TimeConstant.A_MINUTE * 5),

    /**
     * 稍后半小时
     */
    THEN_HALF_HOUR(System.currentTimeMillis() + TimeConstant.A_MINUTE * 30),

    /**
     * 稍后一个小时
     */
    THEN_HOUR(System.currentTimeMillis() + TimeConstant.A_HOUR),

    /**
     * empty
     */
    EMPTY(-1L);

    private final long startTime;

    StartTime(long startTime) {
        this.startTime = startTime;
    }

    public long valueOf() {
        return startTime;
    }
}
