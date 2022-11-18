package com.example.autojob.api.task;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.task.script.ScriptTask;
import com.example.autojob.util.convert.DefaultValueUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * ScriptTask参数对象
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/02 15:47
 * @Email 1158055613@qq.com
 */
@Getter
@Setter
public class AutoJobScriptTaskAttributes extends AutoJobTaskAttributes {
    /**
     * 脚本内容
     */
    private String scriptContent;

    /**
     * 脚本路径
     */
    private String scriptPath;

    /**
     * 脚本文件名，如果不带后缀，请勿在命名中包含.字符
     */
    private String scriptFilename;

    /**
     * 脚本后缀
     */
    private String scriptFileSuffix;

    /**
     * 启动命令
     */
    private String cmd;

    /**
     * 是否是脚本文件
     */
    private Boolean isScriptFile;

    /**
     * 是否需要写入
     */
    private Boolean isNeedWrite;

    /**
     * 是否是命令
     */
    private Boolean isCmd;

    /**
     * 是否已写入
     */
    private Boolean isWrote;


    public AutoJobScriptTaskAttributes(ScriptTask task) {
        super(task);
        scriptContent = task.getScriptContent();
        scriptPath = task.getScriptPath();
        scriptFilename = task.getScriptFilename();
        scriptFileSuffix = task.getScriptFileSuffix();
        cmd = task.getCmd();
        isScriptFile = task.isScriptFile();
        isNeedWrite = task.isNeedWrite();
        isCmd = task.isCmd();
        if (task.getType() != null) {
            type = task
                    .getType()
                    .toString();
        }
        isWrote = task.isWrote();
    }

    @Override
    public ScriptTask convert() {
        ScriptTask scriptTask = new ScriptTask();
        scriptTask.setTaskLevel(taskLevel);
        if (triggerAttributes != null) {
            scriptTask.setTrigger(triggerAttributes.convert());
        }
        scriptTask.setAlias(alias);
        scriptTask.setId(id);
        scriptTask.setAnnotationId(annotationId);
        scriptTask.setType(AutoJobTask.TaskType.convert(type));
        scriptTask.setBelongTo(belongTo);
        scriptTask.setIsChildTask(isChildTask);

        scriptTask.setScriptFileSuffix(scriptFileSuffix);
        scriptTask.setScriptFilename(scriptFilename);
        scriptTask.setCmd(cmd);
        scriptTask.setScriptPath(scriptPath);

        scriptTask.setWrote(DefaultValueUtil.defaultValue(isWrote, null));
        scriptTask.setNeedWrite(DefaultValueUtil.defaultValue(isNeedWrite, null));
        scriptTask.setScriptFile(DefaultValueUtil.defaultValue(isScriptFile, null));
        scriptTask.setIsCmd(DefaultValueUtil.defaultValue(isCmd, null));

        scriptTask.setScriptContent(scriptContent);
        return scriptTask;
    }
}
