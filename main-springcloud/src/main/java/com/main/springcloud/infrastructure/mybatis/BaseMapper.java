package com.main.springcloud.infrastructure.mybatis;

import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * Created by song on 2018/4/22.
 * 通用的mybatis mapper,实体mapper通过implements BaseMapper来减少通用代码的编写
 */
public interface BaseMapper<T> extends Mapper<T>, IdsMapper<T>, InsertListMapper<T> {

}
