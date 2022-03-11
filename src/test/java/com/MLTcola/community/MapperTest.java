package com.MLTcola.community;

import com.MLTcola.community.dao.DiscussPostMapper;
import com.MLTcola.community.dao.LoginTicketMapper;
import com.MLTcola.community.dao.UserMapper;
import com.MLTcola.community.entity.LoginTicket;
import com.MLTcola.community.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelect() {
        User res = userMapper.selectById(101);
        System.out.println(res);
    }
    @Test
    public void testSelectPosts() {
        System.out.println(discussPostMapper.selectDiscussPosts(149, 0, 10));
        System.out.println(discussPostMapper.selectDiscussPostRows(149));
    }
    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("asd");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectAndUpdateLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectLoginTicket("asd");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("asd", 1);
        loginTicket = loginTicketMapper.selectLoginTicket("asd");
        System.out.println(loginTicket);
    }
}
