package com.MLTcola.community;

import com.MLTcola.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class mailTests {
    @Autowired
    private MailClient mailClient;

    @Test
    public void sendTest() {
        mailClient.sendMail("2904188898@qq.com", "测试", "hello");
    }
}
