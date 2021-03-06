package com.MLTcola.community.controller;

import com.MLTcola.community.annotation.LoginRequiredAnnotation;
import com.MLTcola.community.entity.User;
import com.MLTcola.community.service.FollowService;
import com.MLTcola.community.service.LikeService;
import com.MLTcola.community.service.UserService;
import com.MLTcola.community.util.CommunityConstant;
import com.MLTcola.community.util.CommunityUtil;
import com.MLTcola.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @LoginRequiredAnnotation
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile imageHeader, Model model) {
        if (imageHeader == null) {
            model.addAttribute("error", "????????????????????????!");
            return "/site/setting";
        }

        String fileName = imageHeader.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // ???????????????????????????
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "????????????????????????");
            return "/site/setting";
        }
        // ?????????????????????
        fileName = CommunityUtil.generateUUID() + suffix;
        // ??????????????????
        File dest = new File(uploadPath + "/" + fileName);
        try {
            imageHeader.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????:" + e.getMessage());
            throw new RuntimeException("??????????????????,?????????????????????", e);
        }

        // ????????????????????????
        // url=http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ?????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // ????????????
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
                ){
            int b = 0;
            byte[] buffer = new byte[1024];
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
        }
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("???????????????");
        }

        // ??????
        model.addAttribute("user", user);
        // ????????????
        model.addAttribute("likeCount", likeService.findEntityUserLikeCount(userId));

        // ????????????
        long followeeCount = followService.getFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // ?????????
        long followerCount = followService.getFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ??????????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null)
                hasFollowed = followService.hasFollowed(ENTITY_TYPE_USER, userId);
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }
}
