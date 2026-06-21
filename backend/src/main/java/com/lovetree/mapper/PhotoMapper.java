package com.lovetree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lovetree.dto.PhotoResponse;
import com.lovetree.entity.Photo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PhotoMapper extends BaseMapper<Photo> {

    IPage<PhotoResponse> selectPhotoPage(Page<?> page,
                                         @Param("coupleId") Long coupleId);
}
