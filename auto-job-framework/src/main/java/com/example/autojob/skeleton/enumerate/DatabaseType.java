package com.example.autojob.skeleton.enumerate;

import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.util.convert.StringUtils;

/**
 * @author Huang Yongxiang
 * @date 2022-12-29 17:08
 * @email 1158055613@qq.com
 */
public enum DatabaseType {
    MY_SQL, POSTGRES_SQL;

    public static DatabaseType findByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        if ("postgresql".equalsIgnoreCase(name)) {
            return POSTGRES_SQL;
        } else if ("mysql".equalsIgnoreCase(name)) {
            return MY_SQL;
        }
        return null;
    }

    public static DatabaseType getCurrentDatabaseType() {
        return AutoJobApplication
                .getInstance()
                .getConfigHolder()
                .getAutoJobConfig()
                .getDatabaseType();
    }
}
