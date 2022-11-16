package com.example.autojob.skeleton.model.interpreter;

import com.example.autojob.skeleton.model.builder.AttributesBuilder;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.util.convert.RegexUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 参数构建环境
 * @Author Huang Yongxiang
 * @Date 2022/07/06 17:02
 */
@Slf4j
public class AutoJobAttributeContext {
    private final String attributeString;
    private AutoJobTask task;

    public AutoJobAttributeContext(String attributeString) {
        this.attributeString = attributeString;
    }

    public AutoJobAttributeContext(AutoJobTask task) {
        this.task = task;
        this.attributeString = task.getParamsString();
    }

    public boolean isSimpleAttribute() {
        return RegexUtil.isMatch(attributeString, "\\{(((\\d+)|((-?\\d+)(\\.\\d+))|(true|false)|(\\'.*\\')){1}(,)?)*\\}");
    }

    private String convertSimple() {
        if (isSimpleAttribute()) {
            //去掉首尾花括号，分割参数
            String[] attributes = attributeString.trim().substring(1, attributeString.length() - 1).split(",");
            AttributesBuilder attributesBuilder = AttributesBuilder.getInstance();
            attributesBuilder.clear();
            for (String params : attributes) {
                String trimParams = params.trim();
                if (RegexUtil.isMatch(trimParams, "^\\d+$")) {
                    long value = Long.parseLong(trimParams);
                    if (value < Integer.MAX_VALUE) {
                        attributesBuilder.addParams(AttributesBuilder.AttributesType.INTEGER, (int) value);
                    } else {
                        attributesBuilder.addParams(AttributesBuilder.AttributesType.LONG, value);
                    }
                } else if (RegexUtil.isMatch(trimParams, "(-?\\d+)(\\.\\d+)")) {
                    BigDecimal value = new BigDecimal(trimParams);
                    attributesBuilder.addParams(AttributesBuilder.AttributesType.DECIMAL, value.doubleValue());
                } else if (RegexUtil.isMatch(trimParams, "(^true$|^false$)")) {
                    attributesBuilder.addParams(AttributesBuilder.AttributesType.BOOLEAN, Boolean.valueOf(trimParams));
                } else if (RegexUtil.isMatch(trimParams, "^\\'.*\\'$")) {
                    attributesBuilder.addParams(AttributesBuilder.AttributesType.STRING, trimParams.substring(1, trimParams.length() - 1));
                }
            }
            return attributesBuilder.getAttributesString();
        }
        return attributeString;
    }

    public List<Attribute> convert() {
        try {
            if (task != null) {
                if (isSimpleAttribute()) {
                    task.setParamsString(convertSimple());
                }
                return InterpreterDelegate.convertAttributeString(task);
            } else {
                return InterpreterDelegate.convertAttributeString(isSimpleAttribute() ? convertSimple() : attributeString);
            }
        } catch (Exception e) {
            log.error("参数转化失败：{}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 获取参数对象实体
     *
     * @return java.lang.Object[]
     * @author Huang Yongxiang
     * @date 2022/8/19 12:49
     */
    public Object[] getAttributeEntity() {
        List<Attribute> attributeList = convert();
        return attributeList.stream().map(Attribute::getValue).collect(Collectors.toList()).toArray(new Object[]{});
    }


}
