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

import static org.springframework.security.config.Customizer.withDefaults;

import java.io.IOException;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor


public class SecurityConfig {


    private final CustomOAuth2UserService customOAuth2UserService;

    private final MemberService memberService;


    @Component
    public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException {

            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            String nickname = oAuth2User.getNickname();


            if (nickname == null || nickname.isEmpty() || nickname.startsWith("TEMP_")) {
                response.sendRedirect("/member/set-nickname");
            } else {
                response.sendRedirect("/board/");
            }
        }
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .csrf(withDefaults())

                .headers(headers -> headers.frameOptions(options -> options.disable()))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/board/**", "/board/paging", "/board/{id}", "/css/**", "/js/**", "member/save", "/member/set-nickname", "/member/mail-auth",
                                "/member/id-check", "/member/login", "/member/nickname-check", "/images/**", "/comment/**", "/board/delete-check/**"
                                , "/board/delete").permitAll()
                        .requestMatchers("/member/update", "/member/delete", "/member/my-page").authenticated()
                        .anyRequest().authenticated()
                )


                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .usernameParameter("memberId")
                        .passwordParameter("memberPassword")

                        .defaultSuccessUrl("/", true)

                        .failureUrl("/member/login?error=true")


                        .successHandler((request, response, authentication) -> {
                            String memberId = authentication.getName();
                            MemberDTO memberDTO = memberService.findByMemberId(memberId);

                            if (memberDTO != null) {

                                request.getSession().setAttribute("loginEmail", memberDTO.getMemberId());
                                request.getSession().setAttribute("loginNickname", memberDTO.getMemberNickname());
                            }
                            response.sendRedirect("/board/");
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/board/")
                        .invalidateHttpSession(true)
                )


                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )

                        .successHandler(new CustomAuthenticationSuccessHandler())
                );

        return http.build();
    }

}