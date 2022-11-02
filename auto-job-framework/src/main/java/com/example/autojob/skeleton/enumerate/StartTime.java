package com.example.autojob.skeleton.enumerate;

import com.example.autojob.skeleton.framework.config.TimeConstant;
import com.example.autojob.util.id.SystemClock;

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
    NOW(SystemClock.now() + 5000),

    /**
     * 稍后一分钟
     */
    THEN_ONE_MINUTE(SystemClock.now() + TimeConstant.A_MINUTE),

    /**
     * 稍后三分钟
     */
    THEN_THREE_MINUTES(SystemClock.now() + TimeConstant.A_MINUTE * 3),

    /**
     * 稍后五分钟
     */
    THEN_FIVE_MINUTES(SystemClock.now() + TimeConstant.A_MINUTE * 5),

    /**
     * 稍后半小时
     */
    THEN_HALF_HOUR(SystemClock.now() + TimeConstant.A_MINUTE * 30),

    /**
     * 稍后一个小时
     */
    THEN_HOUR(SystemClock.now() + TimeConstant.A_HOUR),

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
