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

    // application.yml에 설정된 SMTP 정보를 바탕으로 메일을 보낼 수 있는 도구입니다.
    private final JavaMailSender javaMailSender;

    // 생성된 인증 번호를 임시로 저장하는 변수입니다.
    // ※ 주의: static은 모든 사용자가 공유하므로, 실제 서비스에선 세션이나 DB/Redis를 쓰는 것이 좋습니다.
    private static int number;

    /**
     * [기능 1] 6자리의 무작위 인증 번호를 생성합니다.
     */
    public static void createNumber() {
        // Math.random()은 0.0 ~ 1.0 사이의 실수를 반환합니다.
        // 여기에 900,000을 곱하고 100,000을 더하면 100,000 ~ 999,999 사이의 숫자가 나옵니다.
        number = (int)(Math.random() * (900000)) + 100000;
    }

    /**
     * [기능 2] 실제 메일을 작성하고 발송하는 메인 메서드입니다.
     * @param mail 수신자의 이메일 주소
     * @return 생성된 인증 번호 (검증을 위해 컨트롤러로 전달)
     */
    public int sendMail(String mail) {
        // 로그 출력: 어떤 이메일로 요청이 왔는지 확인용
        System.out.println("메일 발송 요청됨! 대상: " + mail);

        // 1. 번호 생성 메서드 호출
        createNumber();

        // 2. 메일 메시지 객체 생성 (비어있는 편지지 한 장을 꺼냄)
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            /**
             * MimeMessageHelper: 복잡한 MimeMessage 설정을 쉽게 도와주는 도구입니다.
             * true: 멀티파트 메시지 사용 여부 (파일 첨부나 HTML 포함 시 true)
             * "UTF-8": 인코딩 설정 (한글 깨짐 방지)
             */
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 3. 발신자 정보 (보내는 사람)
            helper.setFrom("developerlbc94@gmail.com");

            // 4. 수신자 정보 (받는 사람)
            helper.setTo(mail);

            // 5. 메일 제목
            helper.setSubject("[Demo 게시판] 이메일 점유 인증 번호입니다.");

            // 6. 메일 본문 (HTML 태그를 사용하여 예쁘게 꾸밀 수 있습니다.)
            String body = "";
            body += "<h3>요청하신 인증 번호입니다.</h3>";
            body += "<h1>" + number + "</h1>"; // 위에서 생성한 인증번호 삽입
            body += "<p>해당 번호를 인증창에 입력해 주세요.</p>";

            // setText(내용, true): 두 번째 인자를 true로 주어야 HTML 태그가 적용됩니다.
            helper.setText(body, true);

            // 7. 메일 전송 (우체통에 넣기)
            javaMailSender.send(message);
            System.out.println("메일 발송 성공!");

        } catch (MessagingException e) {
            // 이메일 주소가 잘못되었거나 네트워크 문제 등 예외 상황 처리
            System.out.println("메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
        }

        // 마지막으로 생성된 번호를 반환하여, 호출한 곳에서 비교할 수 있게 합니다.
        return number;
    }
}