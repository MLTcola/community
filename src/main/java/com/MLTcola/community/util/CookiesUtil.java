package com.MLTcola.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import java.util.Map;

public class CookiesUtil {
    public static String getValue(HttpServletRequest request, String name) {
        if (request == null || name == null)
            throw new IllegalArgumentException("参数为空！");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie i : cookies) {
                if (i.getName().equals(name))
                    return i.getValue();
            }
        }
        return null;
    }
}
