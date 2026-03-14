package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * @Service: 이 클래스가 비즈니스 로직을 담은 '서비스'임을 스프링에 알립니다.
 * @RequiredArgsConstructor: final이 붙은 필드(javaMailSender)를 자동으로 초기화해주는 생성자를 만듭니다.
 */
@Service
@RequiredArgsConstructor
public class MailService {


    private final JavaMailSender javaMailSender;


    private static int number;

    /**
     * [기능 1] 6자리의 무작위 인증 번호를 생성합니다.
     */
    public static void createNumber() {


        number = (int) (Math.random() * (900000)) + 100000;
    }

    /**
     * [기능 2] 실제 메일을 작성하고 발송하는 메인 메서드입니다.
     *
     * @param mail 수신자의 이메일 주소
     * @return 생성된 인증 번호 (검증을 위해 컨트롤러로 전달)
     */
    public int sendMail(String mail) {

        System.out.println("메일 발송 요청됨! 대상: " + mail);


        createNumber();


        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            /**
             * MimeMessageHelper: 복잡한 MimeMessage 설정을 쉽게 도와주는 도구입니다.
             * true: 멀티파트 메시지 사용 여부 (파일 첨부나 HTML 포함 시 true)
             * "UTF-8": 인코딩 설정 (한글 깨짐 방지)
             */
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");


            helper.setFrom("developerlbc94@gmail.com");


            helper.setTo(mail);


            helper.setSubject("[Demo 게시판] 이메일 점유 인증 번호입니다.");


            String body = "";
            body += "<h3>요청하신 인증 번호입니다.</h3>";
            body += "<h1>" + number + "</h1>";
            body += "<p>해당 번호를 인증창에 입력해 주세요.</p>";


            helper.setText(body, true);


            javaMailSender.send(message);
            System.out.println("메일 발송 성공!");

        } catch (MessagingException e) {

            System.out.println("메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
        }


        return number;
    }
}