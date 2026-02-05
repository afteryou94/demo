package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
//1.클래스 설정
@Controller//이 클래스가 웹 요청을 처리하는 객체임을 스프링에 알림
public class HomeController {
    //2. 메인 페이지 연결 (index)
    @GetMapping("/") // 사용자가 http://localhost:8080/ 처럼 아무 주소 없이 접속했을 때
    public String index(){
        return "index"; // View Resolver가 templates/index.html 파일을 찾아서 브라우저에 보여줌
    }
}
