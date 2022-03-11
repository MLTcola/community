package com.MLTcola.community.service;

import com.MLTcola.community.dao.UserMapper;
import com.MLTcola.community.entity.LoginTicket;
import com.MLTcola.community.entity.User;
import com.MLTcola.community.util.CommunityConstant;
import com.MLTcola.community.util.CommunityUtil;
import com.MLTcola.community.util.MailClient;
import com.MLTcola.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initUser(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> hashmap = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("用户对象为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            hashmap.put("usernameMessage", "用户名字不能为空！");
            return hashmap;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            hashmap.put("userEmailMessage", "用户邮箱不能为空！");
            return hashmap;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            hashmap.put("userPasswordMessage", "用户密码不能为空！");
            return hashmap;
        }

        // 邮箱，姓名是否已存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            hashmap.put("usernameMessage", "用户已存在！");
            return hashmap;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            hashmap.put("userEmailMessage", "用户邮箱已注册！");
            return hashmap;
        }

        // 用户注册
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮箱
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return hashmap;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }
        else if (!user.getActivationCode().equals(code)) {
            return ACTIVATION_FALLURE;
        }
        else {
            userMapper.updateStatus(userId, 1);
            clearCacheUser(userId);
            return ACTIVATION_SUCCESS;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> hashmap = new HashMap<>();
        // 空值判断
        if (StringUtils.isBlank(username)) {
            hashmap.put("usernameMessage", "用户名不能为空！");
            return hashmap;
        }
        if (StringUtils.isBlank(password)) {
            hashmap.put("passwordMessage", "密码不能为空！");
            return hashmap;
        }

        //合法性验证
        User user = userMapper.selectByName(username);
        if (user == null) {
            hashmap.put("usernameMessage", "用户名未注册！");
            return hashmap;
        }
        // 验证状态
        if (user.getStatus() == 0) {
            hashmap.put("userMessage", "该账户未激活");
            return hashmap;
        }
        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            hashmap.put("passwordMessage", "密码不正确");
            return hashmap;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        // 将登录凭证放入redis
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

//        loginTicketMapper.insertLoginTicket(loginTicket);


        hashmap.put("ticket", loginTicket.getTicket());
        return hashmap;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
//        return loginTicketMapper.selectLoginTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCacheUser(userId);
        return rows;
    }

    // 判断缓存中是否有user没有返回null
    public User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(userKey);
        return user;
    }
    // 将用户存入缓存redis
    public User initUser(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user);
        return user;
    }

    // 删除缓存中的用户
    public void clearCacheUser(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

}
