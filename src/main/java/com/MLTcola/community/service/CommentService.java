package com.MLTcola.community.service;

import com.MLTcola.community.dao.CommentMapper;
import com.MLTcola.community.entity.Comment;
import com.MLTcola.community.util.CommunityConstant;
import com.MLTcola.community.util.SensitiveWordFilter;
import com.zaxxer.hikari.util.IsolationLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    public List<Comment> findCommentsByEntityId(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntityId(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCommentCountByEntityId(entityType, entityId);
    }
    @Transactional(isolation= Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加一条帖子
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveWordFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 修改评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCommentCountByEntityId(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }
}
