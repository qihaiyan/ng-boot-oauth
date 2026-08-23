package demo;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class UiApplication {

    @GetMapping("/messages")
    public String[] getMessages() {
        return new String[]{"Message 1", "Message 2", "Message 3"};
    }

    /**
     * 用于验证资源服务器是否正确解析微信授权签发的 JWT：
     * 返回 JWT 中的 sub（用户名，即微信 openid）与自定义 openid 声明。
     */
    @GetMapping("/messages/me")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "sub", jwt.getSubject(),
                "openid", jwt.getClaimAsString("openid"),
                "scope", jwt.getClaimAsStringList("scope"));
    }

    public static void main(String[] args) {
        SpringApplication.run(UiApplication.class, args);
    }
}
