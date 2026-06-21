package com.lovetree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lovetree.entity.Tree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TreeMapper extends BaseMapper<Tree> {
    Tree findByCoupleId(@Param("coupleId") Long coupleId);
}
