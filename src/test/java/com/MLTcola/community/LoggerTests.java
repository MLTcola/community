package com.MLTcola.community;

import com.MLTcola.community.entity.User;
import com.MLTcola.community.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTests {
    private static final Logger logger = LoggerFactory.getLogger(LoggerTests.class);

    @Autowired
    private UserService userService;
    @Test
    public void testLogger() {
        System.out.println(logger.getName());

        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("erro log");

    }
    @Test
    public void registerTest() {
        User user = new User();
        user.setUsername("xixi");
        user.setEmail("2904188898@qq.com");
        user.setPassword("123");
        Map<String, Object> map = userService.register(user);
        System.out.println(map);
    }
}
