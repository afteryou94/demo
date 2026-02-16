package com.example.demo.dto;
import com.example.demo.entity.MemberEntity;
import lombok.Builder;
import lombok.Getter;
import com.example.demo.domain.Role;   // Role이 domain 패키지에 있다면

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;

    // 구글인지 네이버인지 구분하여 attributes를 추출함
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 처음 가입할 때 Entity를 생성함
    public MemberEntity toEntity() {
        return MemberEntity.builder()
                .memberId(email) // 소셜 사용자는 이메일을 아이디로 사용
                .memberName(name)
                .memberEmail(email)
                .memberNickname(name) // 닉네임도 이름으로 일단 저장
                .role(Role.USER)
                .build();
    }
}