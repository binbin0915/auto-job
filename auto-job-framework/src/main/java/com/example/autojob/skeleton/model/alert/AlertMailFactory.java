package com.example.autojob.skeleton.model.alert;

import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lang.IAutoJobFactory;
import com.example.autojob.skeleton.enumerate.AlertEventLevel;
import com.example.autojob.skeleton.model.alert.event.ClusterCloseProtectedModelEvent;
import com.example.autojob.skeleton.model.alert.event.ClusterOpenProtectedModelAlertEvent;
import com.example.autojob.skeleton.model.alert.event.TaskRefuseHandleEvent;
import com.example.autojob.skeleton.model.alert.event.TaskRunErrorAlertEvent;
import com.example.autojob.util.convert.DateUtils;

/**
 * 警告邮件工厂类
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/28 16:44
 */
public class AlertMailFactory implements IAutoJobFactory {
    public static AlertMail newRunErrorAlertMail(TaskRunErrorAlertEvent event) {
        AlertMailBuilder builder = AlertMailBuilder.newInstance();
        AutoJobTask errorTask = event.getErrorTask();
        return builder
                .setMailClient(errorTask.getMailClient())
                .setTitle(event.getTitle())
                .setLevel(AlertEventLevel.WARN)
                .addContentTitle(String.format("任务：\"%d:%s\"执行失败", errorTask.getId(), errorTask.getAlias()), 1)
                .addBr()
                .addBold("报警时间：" + DateUtils.formatDateTime(event.getPublishTime()))
                .addBr()
                .addBold(String.format("报警机器：%s:%s", event
                        .getNode()
                        .getHost(), event
                        .getNode()
                        .getPort()))
                .addBr()
                .addBold("任务路径：" + errorTask.getReference())
                .addBr()
                .addParagraph("堆栈信息如下：")
                .addParagraph(event
                        .getStackTrace()
                        .replace("\n", "</br>"))
                .addError("请及时处理")
                .getAlertMail();
    }

    public static AlertMail newClusterOpenProtectedModelAlertMail(ClusterOpenProtectedModelAlertEvent event) {
        AlertMailBuilder builder = AlertMailBuilder.newInstance();
        return builder
                .setMailClient(AutoJobApplication
                        .getInstance()
                        .getMailClient())
                .setTitle(event.getTitle())
                .setLevel(AlertEventLevel.SERIOUS_WARN)
                .addContentTitle(String.format("节点：%s:%s启动保护模式", event
                        .getNode()
                        .getHost(), event
                        .getNode()
                        .getPort()), 1)
                .addBr()
                .addBold("报警时间：" + DateUtils.formatDateTime(event.getPublishTime()))
                .addBr()
                .addBold(String.format("报警机器：%s:%s", event
                        .getNode()
                        .getHost(), event
                        .getNode()
                        .getPort()))
                .addError("请检查集群节点情况")
                .getAlertMail();
    }

    public static AlertMail newClusterCloseProtectedModeAlertMail(ClusterCloseProtectedModelEvent event) {
        AlertMailBuilder builder = AlertMailBuilder.newInstance();
        return builder
                .setMailClient(AutoJobApplication
                        .getInstance()
                        .getMailClient())
                .setTitle(event.getTitle())
                .setLevel(AlertEventLevel.INFO)
                .addContentTitle(String.format("节点：%s:%s关闭保护模式", event
                        .getNode()
                        .getHost(), event
                        .getNode()
                        .getPort()), 1)
                .addBr()
                .addBold("报警时间：" + DateUtils.formatDateTime(event.getPublishTime()))
                .addBr()
                .addBold(String.format("报警机器：%s:%s", event
                        .getNode()
                        .getHost(), event
                        .getNode()
                        .getPort()))
                .addError("请检查集群节点情况")
                .getAlertMail();
    }

    public static AlertMail newTaskRefuseHandleAlertMail(TaskRefuseHandleEvent event) {
        AlertMailBuilder builder = AlertMailBuilder.newInstance();
        AutoJobTask refusedTask = event.getRefusedTask();
        String refusedContent = !refusedTask.getIsAllowRegister() ? "白名单之外" : "资源过载";
        return builder
                .setMailClient(refusedTask.getMailClient())
                .setTitle(event.getTitle())
                .setLevel(AlertEventLevel.WARN)
                .addContentTitle(String.format("任务：\"%d:%s\"被拒绝执行", refusedTask.getId(), refusedTask.getAlias()), 1)
                .addBr()
                .addBold("报警时间：" + DateUtils.formatDateTime(event.getPublishTime()))
                .addBr()
                .addBold(String.format("报警机器：%s:%s", event
                        .getNode()
                        .getHost(), event
                        .getNode()
                        .getPort()))
                .addBr()
                .addBold("任务路径：" + refusedTask.getReference())
                .addBr()
                .addBold("拒绝原因：" + refusedContent)
                .addBr()
                .addError("请及时处理")
                .getAlertMail();

    }
}
