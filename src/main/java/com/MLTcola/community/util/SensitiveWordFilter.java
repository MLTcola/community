package com.MLTcola.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveWordFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordFilter.class);

    // 根节点
    TreeNode root = new TreeNode();

    // 将敏感词替换成的词语
    private static final String REPLACEMENT = "***";


    // 初始化前缀树结构
    @PostConstruct
    private void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive_words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        }
        catch (IOException e) {
            logger.error("前缀树初始化失败" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TreeNode tempNode = root;
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);
            if (tempNode.getSubNode(c) == null) {
                tempNode.addSubNode(c, new TreeNode());
            }
            // 将子节点指向下一个节点
            tempNode = tempNode.getSubNode(c);
            // 如果是叶子结点,增加结束标志
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 敏感词过滤器
     * @param text 要过滤的字符串
     * @return 过滤后的字符串
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) return null;

        // 指针1 指向前缀树的根节点
        TreeNode tempNode = root;
        // 指针2 字符串的开始
        int start = 0;
        // 指针3 来回运动的指针
        int position = 0;
        // 保存结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            Character c = text.charAt(position);

            // 是否为特殊字符
            if (isSymbol(c)) {
                if (tempNode == root) {
                    sb.append(c);
                    start++;
                }
                position++;
                continue;
            }

            // 检查下级结点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以start开始的不是敏感词
                sb.append(text.charAt(start));
                position = ++start;
                tempNode = root;
            }
            else if (tempNode.isKeywordEnd()) {
                sb.append(REPLACEMENT);
                start = ++position;
            }
            else {
                position++;
            }
        }
        return sb.toString();
    }

    // 判断是不是特殊字符
    // 2e80~9fff是东亚文字
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2e80 || c > 0x9FFF);
    }

    // 前缀树
    private class TreeNode{
        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 树的子节点
        private Map<Character, TreeNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加节点
        public void addSubNode(Character c, TreeNode node) {
            subNodes.put(c, node);
        }
        // 获取子节点
        public TreeNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
