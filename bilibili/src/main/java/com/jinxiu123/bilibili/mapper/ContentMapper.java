package com.jinxiu123.bilibili.mapper;

import com.jinxiu123.bilibili.enity.Content;

import java.util.List;
import java.util.Map;

public interface ContentMapper {
    int deleteByPrimaryKey(Integer contentId);

    int insert(Content record);

    int insertSelective(Content record);

    Content selectByPrimaryKey(Integer contentId);

    int updateByPrimaryKeySelective(Content record);

    int updateByPrimaryKey(Content record);

    List<Map> findparams(Map params);

}