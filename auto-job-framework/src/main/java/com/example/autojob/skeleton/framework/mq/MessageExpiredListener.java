package com.example.autojob.skeleton.framework.mq;

/**
 * 消息过期监听器
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/02 15:18
 * @Email 1158055613@qq.com
 */
public interface MessageExpiredListener<M> {
    void onMessageExpired(M message);
}
