package com.MLTcola.community.util;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * 激活失败
     */
    int ACTIVATION_FALLURE = 2;
    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住我状态下的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体为帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体为评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体为用户
     */
    int ENTITY_TYPE_USER = 3;
}
