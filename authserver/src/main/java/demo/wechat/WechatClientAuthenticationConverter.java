package demo.wechat;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * 微信小程序客户端的公共（public）客户端认证转换器。
 *
 * <p>微信小程序是公共客户端（无 client_secret），只能以 {@code client_id} 认证。
 * 标准的 {@code PublicClientAuthenticationConverter} 仅支持 {@code authorization_code} + PKCE，
 * 对小程序的 {@code wechat-code} 授权类型会返回 null；而刷新令牌流程同样只支持
 * PKCE 公共客户端，导致 {@code miniapp-client} 无法用 {@code grant_type=refresh_token} 续期。
 * 本转换器在 grant_type 为 {@code wechat-code} 或 {@code refresh_token} 且仅携带
 * {@code client_id}（无 client_secret）时，将请求转换为
 * {@link WechatClientAuthenticationToken}（认证方式 {@code none}），
 * 交由专用的 {@link WechatClientAuthenticationProvider} 完成认证，从而绕过 PKCE 校验。</p>
 */
public final class WechatClientAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        boolean wechatCode = WechatGrantTypes.WECHAT_CODE.equals(grantType);
        boolean refreshToken = AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType);
        if (!wechatCode && !refreshToken) {
            return null;
        }

        // client_id (REQUIRED for wechat-code)
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if (!StringUtils.hasText(clientId)
                || request.getParameterValues(OAuth2ParameterNames.CLIENT_ID).length != 1) {
            if (wechatCode) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
            }
            // refresh_token 未带 client_id 时，可能是走 Basic / client_secret 认证的机密客户端，交还默认转换器
            return null;
        }

        // 携带 client_secret 说明是机密客户端，交还给默认的 client_secret_post 转换器处理
        if (StringUtils.hasText(request.getParameter(OAuth2ParameterNames.CLIENT_SECRET))) {
            if (wechatCode) {
                // 小程序为公共客户端，不应携带 client_secret
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
            }
            return null;
        }

        return new WechatClientAuthenticationToken(clientId, ClientAuthenticationMethod.NONE, null, null);
    }
}