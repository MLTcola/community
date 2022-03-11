package com.MLTcola.community.controller;

import com.MLTcola.community.entity.Page;
import com.MLTcola.community.entity.User;
import com.MLTcola.community.service.FollowService;
import com.MLTcola.community.service.UserService;
import com.MLTcola.community.util.CommunityConstant;
import com.MLTcola.community.util.CommunityUtil;
import com.MLTcola.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        Map<String, Object> map = new HashMap<>();
        followService.follow(user.getId(), entityType, entityId);
        map.put("followerCount", followService.getFollowerCount(entityType, entityId));
        map.put("followeeCount", followService.getFolloweeCount(user.getId(), entityType));
        return CommunityUtil.getJSONString(0, null, map);
    }

    @RequestMapping(path = "/unFollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        Map<String, Object> map = new HashMap<>();
        followService.unfollow(user.getId(), entityType, entityId);
        map.put("followerCount", followService.getFollowerCount(entityType, entityId));
        map.put("followeeCount", followService.getFolloweeCount(user.getId(), entityType));
        return CommunityUtil.getJSONString(0, null, map);
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户为空!");
        }

        model.addAttribute("user", user);

        // 分页配置
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.getFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> followerList = followService.getFollowerList(userId,page.getOffset(), page.getLimit());
        model.addAttribute("users", followerList);

        return "/site/follower";
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户为空!");
        }

        model.addAttribute("user", user);

        // 分页配置
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.getFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String, Object>> followeeList = followService.getFolloweeList(userId,page.getOffset(), page.getLimit());
        model.addAttribute("users", followeeList);

        return "/site/followee";
    }
}
