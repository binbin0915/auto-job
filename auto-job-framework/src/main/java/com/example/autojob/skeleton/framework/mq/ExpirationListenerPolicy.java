package com.example.autojob.skeleton.framework.mq;

/**
 * @Description 过期策略
 * @Auther Huang Yongxiang
 * @Date 2022/03/21 9:30
 */
public enum ExpirationListenerPolicy {
    /**
     * 单线程监听过期
     */
    SINGLE_THREAD,
    /**
     * 按照主题并发监听过期，总消息数目过多时采取该方式可以使得效率更高，注意topic过多会占用大量线程资源
     */
    TOPIC_CONCURRENCY
}
