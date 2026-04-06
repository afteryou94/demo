package com.example.demo.config; 

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User extends DefaultOAuth2User {
    private String nickname;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            String nickname) {
        super(authorities, attributes, nameAttributeKey);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}