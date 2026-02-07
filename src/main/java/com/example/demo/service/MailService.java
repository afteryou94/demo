package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender; // application.yml 설정을 바탕으로 주입됨
    private static int number; // 생성된 인증 번호를 담을 변수

    // 1. 6자리 무작위 번호 생성
    public static void createNumber() {
        number = (int)(Math.random() * (900000)) + 100000; // 100000 ~ 999999
    }

    // 2. 메일 양식 생성 및 발송
    public int sendMail(String mail) {
        createNumber(); // 번호 생성
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("developerlbc94@gmail.com"); // 보내는 사람
            helper.setTo(mail); // 받는 사람 (사용자가 입력한 이메일)
            helper.setSubject("[Demo 게시판] 이메일 점유 인증 번호입니다."); // 제목

            // 메일 본문 내용
            String body = "";
            body += "<h3>요청하신 인증 번호입니다.</h3>";
            body += "<h1>" + number + "</h1>";
            body += "<p>해당 번호를 인증창에 입력해 주세요.</p>";

            helper.setText(body, true); // true는 HTML 형식을 사용하겠다는 뜻
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return number; // 생성된 번호를 컨트롤러에 전달하여 나중에 검증에 사용
    }
}