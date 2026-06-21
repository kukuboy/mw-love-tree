package com.lovetree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lovetree.dto.MessageResponse;
import com.lovetree.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    List<MessageResponse> selectMessages(@Param("coupleId") Long coupleId,
                                         @Param("userId") Long userId);

    Integer countUnread(@Param("coupleId") Long coupleId,
                        @Param("toId") Long toId);

    int markRead(@Param("id") Long id,
                 @Param("coupleId") Long coupleId);
}
