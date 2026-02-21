package com.example.demo.config;


import com.example.demo.domain.Role;
import com.example.demo.dto.MemberDTO;
import com.example.demo.service.CustomOAuth2UserService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor


public class SecurityConfig {



    private final CustomOAuth2UserService customOAuth2UserService;

    private final MemberService memberService;
    // 1. 클래스 상단에 필드 추가

    // CustomAuthenticationSuccessHandler.java (SecurityConfig에서 사용)
    @Component
    public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException {

            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            String nickname = oAuth2User.getNickname(); // DB나 OAuth2User에서 가져온 닉네임

            // 닉네임이 없거나 임시 값(이름 등)이라면 설정 페이지로 보냄
            if (nickname == null || nickname.isEmpty() || nickname.startsWith("TEMP_")) {
                response.sendRedirect("/member/set-nickname");
            } else {
                response.sendRedirect("/board/");
            }
        }
    }

    // SecurityConfig 클래스 내부에 추가
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 시큐리티가 아예 검사조차 안 하고 통과시키는 경로들
        return (web) -> web.ignoring().requestMatchers("/board/**", "/css/**", "/js/**", "/images/**", "/comment/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 이 부분이 확실히 있어야 합니다.
                // H2 콘솔 사용 시 프레임 허용
                .headers(headers -> headers.frameOptions(options -> options.disable()))
                .authorizeHttpRequests(authorize -> authorize
                        // 명시적으로 /board/delete, /board/update를 허용 목록에 추가
                        .requestMatchers("/board/**", "/board/paging", "/board/{id}", "/css/**", "/js/**","member/save", "/member/set-nickname","/member/mail-auth",
                                "/member/id-check", "/member/login", "/member/nickname-check").permitAll()
                        .requestMatchers( "/member/update", "/member/delete", "/member/my-page").authenticated()
                        .anyRequest().authenticated()
                )
                // 3. 로그인 설정 (여기가 질문하신 부분입니다!)
                // SecurityConfig.java 내의 formLogin 부분만 교체하세요
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .usernameParameter("memberId")
                        .passwordParameter("memberPassword")
//

                                // SuccessHandler 부분 수정
                                .successHandler((request, response, authentication) -> {
                                    String memberId = authentication.getName();
                                    MemberDTO memberDTO = memberService.findByMemberId(memberId);

                                    if (memberDTO != null) {
                                        // 위와 똑같은 키값, 똑같은 DB 데이터를 담습니다.
                                        request.getSession().setAttribute("loginEmail", memberDTO.getMemberId());
                                        request.getSession().setAttribute("loginNickname", memberDTO.getMemberNickname());
                                    }
                                    response.sendRedirect("/board/");
                                })
                        .permitAll()
                )
// 3. 로그아웃 설정 추가 (컨트롤러의 /member/logout 대신 시큐리티 기능을 쓰는 게 깔끔합니다)
                .logout(logout -> logout
                        .logoutUrl("/member/logout") // 로그아웃을 처리할 주소
                        .logoutSuccessUrl("/board/") // 로그아웃 성공 시 이동할 주소
                        .invalidateHttpSession(true) // 세션 날리기
                )

                // 5. 소셜 로그인 설정 (수정본)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // [중요] defaultSuccessUrl을 삭제하거나 주석 처리하고, 아래 핸들러를 등록해야 합니다!
                        .successHandler(new CustomAuthenticationSuccessHandler())
                );

        return http.build();
    }

}