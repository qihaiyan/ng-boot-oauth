package demo.wechat;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 将 token 端点的 HTTP 请求转换为 {@link WechatCodeGrantAuthenticationToken}。
 *
 * <p>grant_type 不匹配时返回 null，让框架继续尝试其它转换器，
 * 不影响标准的授权码、刷新令牌流程。</p>
 */
public class WechatCodeGrantAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!WechatGrantTypes.WECHAT_CODE.equals(grantType)) {
            return null;
        }

        // clientPrincipal 是 ClientAuthenticationFilter 认证后的客户端身份
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> additionalParameters = new HashMap<>();
        request.getParameterNames().asIterator().forEachRemaining(parameter -> {
            if (!parameter.equals(OAuth2ParameterNames.GRANT_TYPE)
                    && !parameter.equals(OAuth2ParameterNames.CLIENT_ID)
                    && !parameter.equals(OAuth2ParameterNames.CODE)) {
                additionalParameters.put(parameter, request.getParameter(parameter));
            }
        });

        String code = request.getParameter(OAuth2ParameterNames.CODE);
        if (!StringUtils.hasText(code)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "code 参数缺失", null));
        }

        return new WechatCodeGrantAuthenticationToken(code, clientPrincipal, additionalParameters);
    }
}