package org.example.badhabitzero.global.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")  // application.yml의 jwt.* 값 바인딩
public class JwtProperties {

    private String secret;      // 비밀키
    private long expiration;    // 만료 시간 (밀리초)
}