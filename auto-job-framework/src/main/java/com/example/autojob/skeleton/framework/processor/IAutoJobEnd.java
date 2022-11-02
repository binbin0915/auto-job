package com.example.autojob.skeleton.framework.processor;

/**
 * @Description 实现该接口的类将会在系统关闭前执行，该类需要在Spring环境中存在
 * @See IAutoJobLoader
 * @Author Huang Yongxiang
 * @Date 2022/07/29 11:05
 */
public interface IAutoJobEnd extends IAutoJobProcessor{
    void end();
}
