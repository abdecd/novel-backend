package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.UserComment;
import com.abdecd.novelbackend.business.pojo.vo.user.UserCommentVOBasic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserCommentMapper extends BaseMapper<UserComment> {
    List<UserCommentVOBasic> listCommentVOByNovelId(Integer novelId, Byte status);
}
