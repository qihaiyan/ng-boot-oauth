package demo.wechat;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.security.Principal;
import java.util.Set;

/**
 * 微信小程序登录授权的核心认证逻辑。
 *
 * <p>校验客户端、用微信 code 换取 openid、加载（或注册）用户、签发并保存令牌。</p>
 */
public class WechatCodeGrantAuthenticationProvider implements AuthenticationProvider {

    private final WechatApiService wechatApiService;
    private final WechatUserDetailsService userDetailsService;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public WechatCodeGrantAuthenticationProvider(WechatApiService wechatApiService,
            WechatUserDetailsService userDetailsService,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        this.wechatApiService = wechatApiService;
        this.userDetailsService = userDetailsService;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WechatCodeGrantAuthenticationToken wechatGrant = (WechatCodeGrantAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(wechatGrant);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        // client 必须注册了 wechat-code 授权类型
        if (!registeredClient.getAuthorizationGrantTypes()
                .contains(new AuthorizationGrantType(WechatGrantTypes.WECHAT_CODE))) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        // 1. 调用微信接口校验 code，换取 openid
        WechatSession session = this.wechatApiService.code2Session(wechatGrant.getCode());

        // 2. 根据 openid 加载用户，首次登录自动注册
        UserDetails userDetails = this.userDetailsService.loadUserByWechatOpenid(session.getOpenid());
        // 以 openid 作为认证主体名称（String），避免将自定义 UserDetails 类型持久化到
        // JdbcOAuth2AuthorizationService 后，在刷新令牌反序列化时被 Jackson 的多态类型校验拒绝。
        Authentication principal = UsernamePasswordAuthenticationToken.authenticated(
                userDetails.getUsername(), null, userDetails.getAuthorities());

        // 默认授予 client 注册的全部 scope
        Set<String> authorizedScopes = registeredClient.getScopes();

        // 3. 生成 access_token
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(principal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(new AuthorizationGrantType(WechatGrantTypes.WECHAT_CODE))
                .authorizationGrant(wechatGrant)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "令牌生成失败", null));
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt());

        // client 同时注册了 refresh_token 授权类型时，签发 refresh_token
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            OAuth2TokenContext refreshContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(principal)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .authorizedScopes(authorizedScopes)
                    .authorizationGrantType(new AuthorizationGrantType(WechatGrantTypes.WECHAT_CODE))
                    .authorizationGrant(wechatGrant)
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(refreshContext);
            if (generatedRefreshToken instanceof OAuth2RefreshToken oauth2RefreshToken) {
                refreshToken = oauth2RefreshToken;
            }
        }

        // 4. 保存授权信息，供后续 refresh_token、撤销、内省使用
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(principal.getName())
                .authorizationGrantType(new AuthorizationGrantType(WechatGrantTypes.WECHAT_CODE))
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), principal);

        if (generatedAccessToken instanceof ClaimAccessor claimAccessor) {
            builder.token(accessToken, metadata ->
                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claimAccessor.getClaims()));
        }
        else {
            builder.accessToken(accessToken);
        }

        if (refreshToken != null) {
            builder.refreshToken(refreshToken);
        }

        this.authorizationService.save(builder.build());

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken, refreshToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WechatCodeGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
            Authentication authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (OAuth2ClientAuthenticationToken.class
                .isAssignableFrom(authentication.getPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }
}