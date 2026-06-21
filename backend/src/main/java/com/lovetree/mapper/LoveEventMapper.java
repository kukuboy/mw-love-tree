package com.lovetree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lovetree.entity.LoveEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LoveEventMapper extends BaseMapper<LoveEvent> {

    List<Map<String, Object>> countByType(@Param("coupleId") Long coupleId);

    List<Map<String, Object>> countByMonth(@Param("coupleId") Long coupleId);
}
