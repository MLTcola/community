package com.MLTcola.community.service;

import com.MLTcola.community.entity.User;
import com.MLTcola.community.util.CommunityConstant;
import com.MLTcola.community.util.HostHolder;
import com.MLTcola.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    // 用户关注某个实体
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);

                operations.multi();
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    // 用户取消关注某个实体
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);

                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    // 实体粉丝数
    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    // 关注实体数
    public  long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 当前用户是否关注该实体
    public boolean hasFollowed(int entityType, int entityId) {
        User user = hostHolder.getUser();
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        if (user == null) return false;

        return redisTemplate.opsForZSet().score(followerKey, user.getId()) != null;
    }

    // 用户粉丝数列表
    public List<Map<String, Object>> getFollowerList(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);

        List<Map<String, Object>> followerList = new ArrayList<>();
        Set<Integer> followers = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (followers != null) {
            for (Integer follower : followers) {
                Map<String, Object> map = new HashMap<>();
                map.put("user", userService.findUserById(follower));
                Double score = redisTemplate.opsForZSet().score(followerKey, follower);
                map.put("followTime", new Date(score.longValue()));
                map.put("hasFollowed", hasFollowed(ENTITY_TYPE_USER, follower));

                followerList.add(map);
            }
        }
        return followerList;
    }

    // 关注用户列表
    public List<Map<String, Object>> getFolloweeList(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);

        List<Map<String, Object>> followeeList = new ArrayList<>();
        Set<Integer> followees = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (followees != null) {
            for (Integer followee : followees) {
                Map<String, Object> map = new HashMap<>();
                map.put("user", userService.findUserById(followee));
                Double score = redisTemplate.opsForZSet().score(followeeKey, followee);
                map.put("followTime", new Date(score.longValue()));
                map.put("hasFollowed", hasFollowed(ENTITY_TYPE_USER, followee));

                followeeList.add(map);
            }
        }
        return followeeList;
    }
}
