package demo.wechat;

import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * 微信小程序公共客户端的认证令牌。
 *
 * <p>与标准的 {@link OAuth2ClientAuthenticationToken} 区分开，
 * 以便使用专用的 {@link WechatClientAuthenticationProvider} 完成客户端认证，
 * 避免触发 Spring Authorization Server 为 authorization_code 流程设计的 PKCE 校验。</p>
 */
@Transient
public class WechatClientAuthenticationToken extends OAuth2ClientAuthenticationToken {

    public WechatClientAuthenticationToken(String clientId,
            ClientAuthenticationMethod clientAuthenticationMethod,
            Object credentials, Map<String, Object> additionalParameters) {
        super(clientId, clientAuthenticationMethod, credentials, additionalParameters);
    }

    public WechatClientAuthenticationToken(RegisteredClient registeredClient,
            ClientAuthenticationMethod clientAuthenticationMethod, @Nullable Object credentials) {
        super(registeredClient, clientAuthenticationMethod, credentials);
    }
}