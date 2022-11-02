package com.example.autojob.logging.model.memory;

import java.util.List;

/**
 * @Description
 * @Author Huang Yongxiang
 * @Date 2022/07/12 16:17
 */
public interface IAutoJobLogCache<L> {
    boolean insert(String taskPath, L log);

    boolean insertAll(String taskPath, List<L> autoJobLogs);

    boolean exist(String taskPath);

    List<L> get(String taskPath);

    boolean remove(String taskPath);
}
