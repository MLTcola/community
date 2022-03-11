package com.MLTcola.community;

import com.MLTcola.community.util.SensitiveWordFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveWordsFilderTests {

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Test
    public void filderTest() {
        String text = "我要$赌%博，我要$嫖%娼，我要$开%票$，我要$吸%毒$。$$。。";
        String res = sensitiveWordFilter.filter(text);
        System.out.println(res);
    }

}
