package com.example.demo.service;

import com.example.demo.config.CustomOAuth2User;
import com.example.demo.entity.MemberEntity;
import com.example.demo.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.example.demo.dto.OAuthAttributes;

import java.util.Collections;

/**
 * @Service: 스프링이 관리하는 서비스 빈으로 등록합니다.
 * OAuth2UserService 인터페이스를 구현하여 소셜 로그인 성공 이후의 후속 처리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();


        OAuth2User oAuth2User = delegate.loadUser(userRequest);


        String registrationId = userRequest.getClientRegistration().getRegistrationId();


        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();


        OAuthAttributes attributes = com.example.demo.dto.OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );


        MemberEntity member = saveOrUpdate(attributes);


        httpSession.setAttribute("loginEmail", member.getMemberId());
        httpSession.setAttribute("loginNickname", member.getMemberNickname());
        httpSession.setAttribute("loginId", member.getMemberId());


        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                member.getMemberNickname()
        );
    }

    /**
     * [유저 저장 및 업데이트 로직]
     * 이메일을 기준으로 기존 회원인지 신규 회원인지 판단합니다.
     */
    private MemberEntity saveOrUpdate(OAuthAttributes attributes) {
        MemberEntity member = memberRepository.findByMemberEmail(attributes.getEmail())
                .map(entity -> {

                    return entity.update(attributes.getName());
                })
                .orElseGet(() -> {

                    MemberEntity newMember = attributes.toEntity();


                    String tempNickname = "TEMP_" + attributes.getEmail().split("@")[0];
                    newMember.setMemberNickname(tempNickname);

                    return newMember;
                });


        return memberRepository.save(member);
    }
}