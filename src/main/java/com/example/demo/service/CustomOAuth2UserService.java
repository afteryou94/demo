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

        // CustomOAuth2UserService.java 일부
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                member.getMemberNickname() // DB에서 가져온 닉네임
        );
    }

    // CustomOAuth2UserService.java 내부의 saveOrUpdate 메서드 수정

    private MemberEntity saveOrUpdate(OAuthAttributes attributes) {
        MemberEntity member = memberRepository.findByMemberEmail(attributes.getEmail())
                .map(entity -> {
                    // 이미 존재하는 회원이면 이름만 업데이트 (닉네임은 건드리지 않음)
                    return entity.update(attributes.getName());
                })
                .orElseGet(() -> {
                    // 신규 회원이라면 엔티티를 새로 만드는데,
                    // 이때 memberNickname에 중복될 수 있는 '이름' 대신
                    // 임시로 '이메일'이나 '고유 식별자'를 넣어줍니다.
                    MemberEntity newMember = attributes.toEntity();

                    // 닉네임 중복 에러를 피하기 위한 임시 닉네임 설정 (중요!)
                    // 예: "TEMP_이메일앞부분" 또는 "UUID"
                    String tempNickname = "TEMP_" + attributes.getEmail().split("@")[0];
                    newMember.setMemberNickname(tempNickname);

                    return newMember;
                });

        return memberRepository.save(member);
    }
}