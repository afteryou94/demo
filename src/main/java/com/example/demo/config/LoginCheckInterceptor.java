package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        // 세션에 로그인 정보가 없으면
        if (session.getAttribute("loginId") == null) {
            // 로그인 페이지로 돌려보내기
            response.sendRedirect("/member/login");
            return false; // 더 이상 컨트롤러로 진행하지 않음
        }
        return true; // 로그인 되어 있으면 통과
    }
}