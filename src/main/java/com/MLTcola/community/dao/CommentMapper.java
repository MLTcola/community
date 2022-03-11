package com.MLTcola.community.dao;

import com.MLTcola.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    // 根据实体查询数据
    List<Comment> selectCommentByEntityId(int entityType, int entityId, int offset, int limit);

    // 根据实体查询评论的数量
    int selectCommentCountByEntityId(int entityType, int entityId);

    // 添加评论
    int insertComment(Comment comment);
}
