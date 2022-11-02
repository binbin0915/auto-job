package com.example.autojob.skeleton.framework.mq;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Auther Huang Yongxiang
 * @Date 2022/03/21 14:19
 */
public interface IProducer<M> {

    /**
     * 注册一个消息队列
     *
     * @param topic 主题名
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/3/20 9:59
     */
    boolean registerMessageQueue(String topic);

    boolean hasTopic(String topic);

    boolean hasRegexTopic(String regexTopic);

    boolean removeMessageQueue(String topic);
    /**
     * 非阻塞的发布一条消息，当容量已满时立即返回
     *
     * @param message 消息内容
     * @param topic   队列主题
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/3/20 9:59
     */
    boolean publishMessageNoBlock(final M message, final String topic);

    /**
     * 非阻塞的发布一条消息，同时指定其过期时间，当容量已满时立即返回
     *
     * @param message      消息内容
     * @param topic        主题
     * @param expiringTime 过期时长
     * @param unit         时间单位
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/3/20 10:09
     */
    boolean publishMessageNoBlock(final M message, final String topic, final long expiringTime, final TimeUnit unit);

    /**
     * 阻塞的发布一条消息，当容量已满时等待空出
     *
     * @param message 消息内容
     * @param topic   队列主题
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/3/20 10:03
     */
    boolean publishMessageBlock(final M message, final String topic);

    boolean publishMessageBlock(final M message, final String topic, final long expiringTime, final TimeUnit unit);
}
