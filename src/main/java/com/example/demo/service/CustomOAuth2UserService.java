package com.example.demo.service;
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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 구글인지 네이버인지 구분 (google, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // 각 서비스의 고유 식별자 키 이름 (구글은 'sub', 네이버는 'id')
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 공통 DTO로 변환
        OAuthAttributes attributes = com.example.demo.dto.OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 사용자 저장 또는 업데이트
        MemberEntity member = saveOrUpdate(attributes);

        // 세션에 로그인 정보 저장 (DTO 사용 권장)
        // 중복과 혼란을 제거한 깔끔한 버전
        httpSession.setAttribute("loginEmail", member.getMemberId());      // 식별자 통일 (afteryou 등)
        httpSession.setAttribute("loginNickname", member.getMemberNickname()); // DB에 저장된 닉네임
        httpSession.setAttribute("loginId", member.getMemberId());         // 필요하다면 유지

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private MemberEntity saveOrUpdate(com.example.demo.dto.OAuthAttributes attributes) {
        MemberEntity member = memberRepository.findByMemberEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName())) // 있으면 업데이트
                .orElse(attributes.toEntity()); // 없으면 신규 생성

        return memberRepository.save(member);
    }
}