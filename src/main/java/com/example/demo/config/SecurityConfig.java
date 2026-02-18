package com.example.demo.config;


import com.example.demo.domain.Role;
import com.example.demo.dto.MemberDTO;
import com.example.demo.service.CustomOAuth2UserService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor


public class SecurityConfig {



    private final CustomOAuth2UserService customOAuth2UserService;

    private final MemberService memberService;
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
                        .requestMatchers("/board/**", "/board/paging", "/board/{id}", "/css/**", "/js/**").permitAll()
                        .requestMatchers( "/member/**").authenticated()
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

                // 5. 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 소셜 로그인 성공 후 처리를 담당할 서비스 등록
                        )
                        .defaultSuccessUrl("/board/", true) // 로그인 성공 시 이동할 주소
                );

        return http.build();
    }

}