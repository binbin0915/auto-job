package com.example.autojob.skeleton.framework.mq;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Auther Huang Yongxiang
 * @Date 2022/03/21 14:20
 */
public interface IConsumer<M> {
    /**
     * 阻塞的获取一条消息，可以决定是否将该消息取出，即移出队列
     *
     * @param topic     主题
     * @param isTakeout 是否移出队列，当为false时该方法将退化成非阻塞的
     * @return M
     * @author Huang Yongxiang
     * @date 2022/3/20 10:55
     */
    M takeMessageBlock(final String topic, final boolean isTakeout);

    M takeMessageNoBlock(final String topic, final boolean isTakeout);

    List<M> takeAllMessageNoBlock(final String topic, final boolean isTakeout);

    List<M> takeMessageByRegexTopicBlock(final String regexTopic,final boolean isTakeout);

    List<M> takeMessageByRegexTopicNoBlock(final String regexTopic,final boolean isTakeout);


    MessageQueueContext.MessageQueue<MessageQueueContext.MessageEntry<M>> subscriptionMessage(String topic);

    /**
     * 阻塞的尝试订阅指定消息队列，如果存在则立即返回，否则将会等待指定时长，若期间创建则会立即返回，否则等
     * 到结束返回null
     *
     * @param topic 主题
     * @param wait  要阻塞获取的时长
     * @param unit  wait的时间单位
     * @return com.example.autojob.skeleton.framework.mq.MessageQueueContext.MessageQueue<com.example.autojob.skeleton.framework.mq.MessageQueueContext.MessageEntry < M>>
     * @author Huang Yongxiang
     * @date 2022/3/22 14:32
     */
    MessageQueueContext.MessageQueue<MessageQueueContext.MessageEntry<M>> subscriptionMessage(String topic, long wait, TimeUnit unit);

    void unsubscribe(String topic);

    void unsubscribe(String topic,long wait,TimeUnit unit);
}
