package com.jerry.lab.concurrent.thread;

import lombok.AllArgsConstructor;
import lombok.Data;

public class ThreadLocalTest2 {
    static int THREAD_NUM = 5;

    public static void main(String[] args) {
        Thread[] threads = new Thread[THREAD_NUM];
        for (int j = 0; j < THREAD_NUM; j++) {
            final int userId = j;
            threads[j] = new Thread(() -> {
                // 注入用户信息，一般通过拦截器方式
                AuthLoginUserContext.setLoginUser(new LoginUserInfo(userId, "名字编号" + userId));
                System.out.println(Thread.currentThread().getName() + " set user info at interceptor " + AuthLoginUserContext.getLoginUserInfo());

                // 模拟在DAO层访问
                System.out.println(Thread.currentThread().getName() + " get user info at dao " + AuthLoginUserContext.getLoginUserInfo());

                // 模拟在Service层访问
                System.out.println(Thread.currentThread().getName() + " get user info at service " + AuthLoginUserContext.getLoginUserInfo());
            }, "Thread-" + j);
        }

        for (Thread thread : threads) {
            thread.start();
        }
    }
}

class AuthLoginUserContext {

    private static final ThreadLocal<LoginUserInfo> LOGIN_USER_INFO_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置登录用户信息，可采用拦截器的方式，在完成登录的时候执行
     *
     * @param loginUserInfo
     */
    public static void setLoginUser(LoginUserInfo loginUserInfo) {
        if (loginUserInfo != null) {
            LOGIN_USER_INFO_THREAD_LOCAL.set(loginUserInfo);
        } else {
            LOGIN_USER_INFO_THREAD_LOCAL.remove();
        }
    }

    /**
     * 获取用户登录信息
     *
     * @return
     */
    public static LoginUserInfo getLoginUserInfo() {
        return LOGIN_USER_INFO_THREAD_LOCAL.get();
    }

}

@Data
@AllArgsConstructor
class LoginUserInfo {

    private int userId;

    private String userName;
}