package com.jinxiu123.bilibili.mapper;

import com.jinxiu123.bilibili.enity.Data;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public interface DataMapper {
    int deleteByPrimaryKey(Long dataId);

    int insert(Data record);

    int insertSelective(Data record);

    Data selectByPrimaryKey(Long dataId);

    int updateByPrimaryKeySelective(Data record);

    int updateByPrimaryKey(Data record);

    List<Data> findByState(String state);
}