package com.MLTcola.community.controller;

import com.MLTcola.community.entity.DiscussPost;
import com.MLTcola.community.entity.Page;
import com.MLTcola.community.entity.User;
import com.MLTcola.community.service.DiscussPostService;
import com.MLTcola.community.service.LikeService;
import com.MLTcola.community.service.UserService;
import com.MLTcola.community.util.CommunityConstant;
import com.MLTcola.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        // 方法调用前，SpringMVC 会自动实例化 Model 和 Page(实体对象)，并将 Page 注入 Model，
        // 所以，在 thymeleaf 中可以直接访问到 page 对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);

        return "/index";
    }

    @RequestMapping(path = "/test", method = RequestMethod.GET)
    @ResponseBody
    public String test(Model model, Page page) {
        return "success";
    }
}
