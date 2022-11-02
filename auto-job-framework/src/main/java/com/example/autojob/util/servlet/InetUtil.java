package com.example.autojob.util.servlet;

import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.SocketException;
import java.util.Optional;

/**
 * 网络工具类
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/22 16:33
 */
@Slf4j
public class InetUtil {
    public static String getLocalhostIp() {
        try {
            Optional<Inet4Address> optional = IpUtil.getLocalIp4Address();
            if (optional != null && optional.isPresent()) {
                return optional
                        .get()
                        .getHostAddress();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getPort() {
        return AutoJobApplication
                .getInstance()
                .getConfigHolder()
                .getClusterConfig()
                .getPort();
    }

}
