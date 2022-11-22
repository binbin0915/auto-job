package com.example.spring.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 测试Mapper
 *
 * @author Huang Yongxiang
 * @date 2022-11-22 16:41
 * @email 1158055613@qq.com
 */
@Mapper
public interface TestMapper {
    int count();
}
