package com.example.autojob.skeleton.framework.mq;


/**
 * @Description
 * @Auther Huang Yongxiang
 * @Date 2022/03/20 9:57
 */
public interface IMessageQueueContext<M> {

    int length(String topic);

    /**
     * 使得消息立即过期
     *
     * @param topic 主题
     * @return void
     * @throws ErrorExpiredException 过期时发生异常抛出
     * @author Huang Yongxiang
     * @date 2022/3/20 11:31
     */
    void expire(String topic, MessageQueueContext.MessageEntry<M> messageEntry) throws ErrorExpiredException;

    /**
     * 摧毁消息容器并启动垃圾清理
     *
     * @return void
     * @author Huang Yongxiang
     * @date 2022/3/22 14:33
     */
    void destroy();

    void addMessagePublishedListener(String topic, MessagePublishedListener<M> listener);

    void addMessageExpiredListener(String topic, MessageExpiredListener<M> listener);

}
