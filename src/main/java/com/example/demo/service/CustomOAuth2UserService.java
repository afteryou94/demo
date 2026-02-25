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
    private final MemberRepository memberRepository; // DB 조작을 위한 리포지토리
    private final HttpSession httpSession;           // 로그인 정보를 세션에 담기 위한 도구

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2 서비스를 생성하여 대리자(delegate)로 사용합니다.
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

        // 2. 소셜 서비스(구글, 네이버 등)에서 사용자 정보를 가져옵니다.
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 3. 어떤 소셜 서비스인지 구분하는 ID를 가져옵니다. (예: "google", "naver")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 4. 소셜 서비스마다 유저를 식별하는 고유 키 값이 다릅니다. (구글은 'sub', 네이버는 'id')
        // 이 키 값이 무엇인지 가져옵니다.
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 5. 소셜 서비스마다 다른 유저 정보 양식을 우리 시스템의 공통 DTO(OAuthAttributes)로 변환합니다.
        OAuthAttributes attributes = com.example.demo.dto.OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );

        // 6. 가져온 유저 정보를 DB에 저장하거나, 이미 있다면 업데이트합니다.
        MemberEntity member = saveOrUpdate(attributes);

        // 7. 세션에 로그인 상태를 유지하기 위해 필요한 정보들을 저장합니다.
        // 나중에 JSP나 타임리프에서 ${session.loginNickname} 등으로 꺼내 쓸 수 있습니다.
        httpSession.setAttribute("loginEmail", member.getMemberId());
        httpSession.setAttribute("loginNickname", member.getMemberNickname());
        httpSession.setAttribute("loginId", member.getMemberId());

        // 8. 시큐리티 세션에 저장될 유저 객체를 반환합니다.
        // 권한(Role)과 소셜에서 받은 속성들, 그리고 DB에서 가져온 닉네임을 담아 넘깁니다.
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
                    // [기존 회원] 이미 가입된 이메일이라면, 소셜 프로필의 이름이 변경되었을 때만 업데이트합니다.
                    return entity.update(attributes.getName());
                })
                .orElseGet(() -> {
                    // [신규 회원] 처음 방문했다면 회원가입 처리를 진행합니다.
                    MemberEntity newMember = attributes.toEntity();

                    // DB의 닉네임 중복 제약조건을 피하기 위해 임시 닉네임을 생성합니다.
                    // 예: 이메일이 'abc@gmail.com'이면 "TEMP_abc"로 설정
                    String tempNickname = "TEMP_" + attributes.getEmail().split("@")[0];
                    newMember.setMemberNickname(tempNickname);

                    return newMember;
                });

        // 최종적으로 DB에 저장(Insert 또는 Update)하고 결과를 반환합니다.
        return memberRepository.save(member);
    }
}