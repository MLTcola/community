package com.MLTcola.community.dao;

import com.MLTcola.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // 注解给参数起个别名,若使用动态sql(在<if>里使用)，只有一个形参，必须起别名
    int selectDiscussPostRows(@Param("userId") int userId);

    // 插入一条帖子
    int insertDiscussPost(DiscussPost post);

    // 根据用户id查询一条帖子
    DiscussPost selectDiscussPostById(int id);

    // 修改帖子数量
    int updateCommentCount(int id, int commentCount);

}
