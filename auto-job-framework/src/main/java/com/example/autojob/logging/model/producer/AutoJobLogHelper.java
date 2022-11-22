package com.example.autojob.logging.model.producer;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.model.AutoJobLogContainer;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.mq.MessageProducer;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.TaskRunningContext;
import com.example.autojob.util.convert.DateUtils;
import com.example.autojob.util.convert.DefaultValueUtil;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.id.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 日志辅助类，该类可直接实现日志打印，同时该类打印的日志会被直接加入任务日志
 * 该类支持子方法、子线程日志捕获
 * <p>为了保证项目统一使用slf4j logger，该类允许设置对slf4j logger进行代理，为了能真实记录原log输出位置，将会在原日志上增加
 * $Actual-Location - [fileName:lineNum]$</p>
 * 在与Spring集成使用时强烈建议在任务方法内部重新new该对象，防止Spring在AutoJob上下文初始化前调用该类的实例化方法导致报错，请参考{@link #AutoJobLogHelper(Logger)}
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/05 14:33
 */
@Slf4j
public class AutoJobLogHelper implements IAutoJobLogProducer<AutoJobLog> {

    private volatile Logger slf4jLogger;

    private MessageProducer<AutoJobLog> producer;


    public AutoJobLogHelper() {
        this(null);
    }

    public AutoJobLogHelper(Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
        this.producer = new MessageProducer<>(AutoJobLogContainer
                .getInstance()
                .getMessageQueueContext(AutoJobLog.class));
        if (producer.getMessageQueueContext() == null) {
            throw new IllegalStateException("AutoJob上下文尚未初始化");
        }
    }

    /**
     * 获取一个日志辅助类单例，注意单例模式下如果使用slf4j logger代理不是线程安全的，如果你需要对slf4j logger进行代理，请优先调用构造方法创建新的一个实例
     *
     * @return com.example.autojob.logging.model.producer.AutoJobLogHelper
     * @author Huang Yongxiang
     * @date 2022/8/29 15:46
     */
    public static AutoJobLogHelper getInstance() {
        AutoJobApplication
                .getInstance()
                .getLogContext()
                .getLogHelper()
                .setSlf4jProxy(null);
        return AutoJobApplication
                .getInstance()
                .getLogContext()
                .getLogHelper();
    }


    private static String now() {
        return DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss,SSS");
    }

    private static String getThreadName() {
        return Thread
                .currentThread()
                .getName();
    }


    private static String getLogLocation() {
        StackTraceElement stackTraceElement = Thread
                .currentThread()
                .getStackTrace()[4];
        return String.format("%s - [%s:%d]", stackTraceElement.getClassName(), stackTraceElement.getFileName(), stackTraceElement.getLineNumber());
    }


    private static String getFormatMessage(String appendLogPattern, Object... appendLogArguments) {
        FormattingTuple ft = MessageFormatter.arrayFormat(appendLogPattern, appendLogArguments);
        return ft.getMessage();
    }

    /**
     * 对slf4j的logger进行代理，日志的输出将通过slf4j logger输出，不指定代理将使用默认输出且只能输出到控制台
     *
     * @param logger slf4j logger
     * @return void
     * @author Huang Yongxiang
     * @date 2022/8/19 17:07
     */
    public void setSlf4jProxy(Logger logger) {
        slf4jLogger = logger;
    }

    public void debug(String appendLogPattern, Object... appendLogArguments) {
        String message = getLevelMessage("DEBUG", appendLogPattern, appendLogArguments);
        String id = DefaultValueUtil.chooseString(TaskRunningContext
                .getContextHolder()
                .get() == null, null, "" + TaskRunningContext
                .getContextHolder()
                .get());
        if (!StringUtils.isEmpty(id)) {
            produce(producer, id, getAutoJobLog(message, "DEBUG"));
        }
        if (slf4jLogger != null) {
            slf4jLogger.debug(getActualLocation() + appendLogPattern, appendLogArguments);
        } else {
            System.out.println(message);
        }
    }

    public void info(String appendLogPattern, Object... appendLogArguments) {
        String message = getLevelMessage("INFO", appendLogPattern, appendLogArguments);
        String id = DefaultValueUtil.chooseString(TaskRunningContext
                .getContextHolder()
                .get() == null, null, "" + TaskRunningContext
                .getContextHolder()
                .get());
        if (!StringUtils.isEmpty(id)) {
            produce(producer, id, getAutoJobLog(message, "INFO"));
        }
        if (slf4jLogger != null) {
            slf4jLogger.info(getActualLocation() + appendLogPattern, appendLogArguments);
        } else {
            System.out.println(message);
        }
    }

    public void warn(String appendLogPattern, Object... appendLogArguments) {
        String message = getLevelMessage("WARN", appendLogPattern, appendLogArguments);
        String id = DefaultValueUtil.chooseString(TaskRunningContext
                .getContextHolder()
                .get() == null, null, "" + TaskRunningContext
                .getContextHolder()
                .get());
        if (!StringUtils.isEmpty(id)) {
            produce(producer, id, getAutoJobLog(message, "WARN"));
        }
        if (slf4jLogger != null) {
            slf4jLogger.warn(getActualLocation() + appendLogPattern, appendLogArguments);
        } else {
            System.out.println(message);
        }
    }

    public void error(String appendLogPattern, Object... appendLogArguments) {
        String message = getLevelMessage("ERROR", appendLogPattern, appendLogArguments);
        String id = DefaultValueUtil.chooseString(TaskRunningContext
                .getContextHolder()
                .get() == null, null, "" + TaskRunningContext
                .getContextHolder()
                .get());
        if (!StringUtils.isEmpty(id)) {
            produce(producer, id, getAutoJobLog(message, "ERROR"));
        }
        if (slf4jLogger != null) {
            slf4jLogger.error(getActualLocation() + appendLogPattern, appendLogArguments);
        } else {
            System.out.println(message);
        }
    }

    private String getActualLocation() {
        StackTraceElement stackTraceElement = Thread
                .currentThread()
                .getStackTrace()[3];
        return String.format("$Actual-Location - [%s:%s]$ - ", stackTraceElement.getFileName(), stackTraceElement.getLineNumber());
    }

    private static String getLevelMessage(String level, String appendLogPattern, Object... appendLogArguments) {
        return String.format("%s - %s - [%s] %s - %s", now(), level, getThreadName(), getLogLocation(), getFormatMessage(appendLogPattern, appendLogArguments));
    }

    private static AutoJobLog getAutoJobLog(String message, String level) {
        AutoJobLog autoJobLog = new AutoJobLog();
        autoJobLog.setId(IdGenerator.getNextIdAsLong());
        autoJobLog.setInputTime(DateUtils.getTime());
        autoJobLog.setLevel(level);
        autoJobLog.setTaskId((Long) DefaultValueUtil.defaultObjectWhenNull(TaskRunningContext
                .getContextHolder()
                .get(), -1L));
        autoJobLog.setMessage(message);
        return autoJobLog;
    }


    @Override
    public void produce(MessageProducer<AutoJobLog> producer, String topic, AutoJobLog autoJobLog) {

        if (producer != null && autoJobLog != null) {
            if (!producer.hasTopic(topic)) {
                producer.registerMessageQueue(topic);
            }
            AutoJobTask concurrentTask = TaskRunningContext
                    .getConcurrentThreadTask()
                    .get();
            if (concurrentTask != null) {
                producer.publishMessageBlock(autoJobLog, topic, concurrentTask
                        .getTrigger()
                        .getMaximumExecutionTime() * 1000 + 10000, TimeUnit.MILLISECONDS);
            } else {
                producer.publishMessageBlock(autoJobLog, topic);
            }
        }
    }

}
