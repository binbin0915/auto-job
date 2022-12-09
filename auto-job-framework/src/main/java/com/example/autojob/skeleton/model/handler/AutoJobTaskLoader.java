package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.framework.task.AutoJobTask;

import java.util.List;

/**
 * @author Huang Yongxiang
 * @date 2022-12-03 18:30
 * @email 1158055613@qq.com
 */
public interface AutoJobTaskLoader {
    int load(List<AutoJobTask> tasks);
}
