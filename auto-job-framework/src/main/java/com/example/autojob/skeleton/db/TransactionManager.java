package com.example.autojob.skeleton.db;

import java.sql.Connection;

/**
 * OrientDB会话事务管理器
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/08 14:40
 */
public class TransactionManager {
    private static final ThreadLocal<Boolean> isOpenTransaction = new ThreadLocal<>();
    private static final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    public static boolean openTransaction(DataSourceHolder dataSourceHolder) {
        isOpenTransaction.set(true);
        Connection connection = dataSourceHolder.getConnection();
        try {
            connection.setAutoCommit(false);
        } catch (Exception ignored) {
            isOpenTransaction.set(false);
            return false;
        }
        currentConnection.set(connection);
        return true;
    }

    public static void closeTransaction() {
        isOpenTransaction.set(false);
        isOpenTransaction.remove();
        if (currentConnection.get() != null) {
            try {
                currentConnection.get().commit();
                currentConnection.get().close();
                currentConnection.remove();
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean isOpenTransaction() {
        return isOpenTransaction.get() != null && isOpenTransaction.get();
    }

    /**
     * 获取当前线程绑定的会话实例
     *
     * @return com.orientechnologies.orient.core.db.Connection
     * @author Huang Yongxiang
     * @date 2022/10/8 15:31
     */
    public static Connection getCurrentConnection() {
        return currentConnection.get();
    }
}
