package com.xik.aibookkeeping.common.context;

import com.xik.aibookkeeping.pojo.context.UserContext;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

    public static void set(UserContext context) {
        contextHolder.set(context);
    }

    public static UserContext getCurrentUser() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}

