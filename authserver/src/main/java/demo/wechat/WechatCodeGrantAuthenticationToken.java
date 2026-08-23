package demo.wechat;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;

/**
 * 微信小程序登录授权认证令牌。
 *
 * <p>由 {@link WechatCodeGrantAuthenticationConverter} 从 token 请求构造，
 * 携带微信一次性临时 code，交由 {@link WechatCodeGrantAuthenticationProvider} 处理。</p>
 */
public class WechatCodeGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final String code;

    public WechatCodeGrantAuthenticationToken(String code, Authentication clientPrincipal,
            Map<String, Object> additionalParameters) {
        super(new AuthorizationGrantType(WechatGrantTypes.WECHAT_CODE), clientPrincipal, additionalParameters);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}