package com.example.autojob.skeleton.db.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * <p>
 * 任务表
 * </p>
 *
 * @author Huang Yongxiang
 * @since 2022-08-11
 */
@Data
@Accessors(chain = true)
public class AutoJobTaskEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务别名
     */
    private String alias;

    /**
     * 注解ID
     */
    private Long annotationId;

    /**
     * 任务所在类路径
     */
    private String methodClassName;

    /**
     * 任务名称
     */
    private String methodName;

    /**
     * 任务参数
     */
    private String params;

    /**
     * 预留字段，GLUE模式
     */
    private String content;
    /**
     * 任务运行类工厂
     */
    private String methodObjectFactory;

    /**
     * 任务内容，用于存放脚本任务的脚本
     */
    private String scriptContent;
    /**
     * 脚本路径
     */
    private String scriptPath;
    /**
     * 脚本文件名
     */
    private String scriptFileName;
    /**
     * 脚本命令行
     */
    private String scriptCmd;
    /**
     * 任务对应的触发器
     */
    private Long triggerId;
    /**
     * 任务类型，目前已占用的类型有：0-方法型任务 1-脚本型任务
     */
    private Integer type;

    /**
     * 是否是子任务
     */
    private Integer isChildTask;

    /**
     * 启动锁 0-未上锁 1-已上锁
     */
    private Integer runLock;

    /**
     * 任务优先级
     */
    private Integer taskLevel;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 预留字段，所属于
     */
    private Long belongTo;

    /**
     * 状态 0-停用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 删除标识
     */
    private Integer delFlag;


}
