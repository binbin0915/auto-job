package com.example.autojob.skeleton.model.alert;

import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.util.mail.MailHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Description 报警邮件对象
 * @Author Huang Yongxiang
 * @Date 2022/07/28 15:50
 */
@Getter
@Setter
@Accessors(chain = true)
public class AlertMail {
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 报警级别
     */
    private String level;

    public boolean send() {
        if (AutoJobApplication.getInstance().getConfigHolder().getAutoJobConfig().getEnableMailAlert()) {
            MailHelper mailHelper = AutoJobApplication.getInstance().getMailHelper();
            if (mailHelper != null) {
                return mailHelper.sendMail(level + "：" + title, String.format("<h2 style='color: %s'>报警级别：%s\n%s</h2>", getLevelColor(), level, content));
            }
        }
        return false;
    }

    private String getLevelColor() {
        switch (level) {
            case "提醒": {
                return "MediumSpringGreen";
            }
            case "警告": {
                return "Coral";
            }
            case "严重警告": {
                return "HotPink";
            }
            case "系统错误": {
                return "red";
            }
        }
        return "black";
    }
}
