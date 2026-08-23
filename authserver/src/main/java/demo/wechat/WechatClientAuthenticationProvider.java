package demo.wechat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

/**
 * 微信小程序公共（public）客户端的认证提供者。
 *
 * <p>与 Spring Authorization Server 默认的 {@code PublicClientAuthenticationProvider} 不同，
 * 本提供者专门处理 {@link WechatClientAuthenticationToken}，仅按 {@code client_id} 做公共客户端认证，
 * <b>不执行</b>为 {@code authorization_code} 流程设计的 PKCE（code_verifier）校验。
 * 因此 {@code grant_type=wechat-code} 的小程序客户端无需提供 code_verifier 即可完成客户端认证。</p>
 */
@Slf4j
public final class WechatClientAuthenticationProvider implements AuthenticationProvider {

	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-3.2.1";

	private final RegisteredClientRepository registeredClientRepository;

	public WechatClientAuthenticationProvider(RegisteredClientRepository registeredClientRepository) {
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		WechatClientAuthenticationToken wechatClientAuthentication =
				(WechatClientAuthenticationToken) authentication;

		if (!ClientAuthenticationMethod.NONE.equals(wechatClientAuthentication.getClientAuthenticationMethod())) {
			return null;
		}

		String clientId = wechatClientAuthentication.getPrincipal().toString();
		RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
		if (registeredClient == null) {
			throwInvalidClient(OAuth2ParameterNames.CLIENT_ID);
		}

		if (!registeredClient.getClientAuthenticationMethods().contains(
				wechatClientAuthentication.getClientAuthenticationMethod())) {
			throwInvalidClient("authentication_method");
		}

		if (log.isTraceEnabled()) {
			log.trace("Authenticated WeChat Mini Program public client: {}", clientId);
		}

		return new WechatClientAuthenticationToken(registeredClient,
				wechatClientAuthentication.getClientAuthenticationMethod(), null);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return WechatClientAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private static void throwInvalidClient(String parameterName) {
		OAuth2Error error = new OAuth2Error(
				OAuth2ErrorCodes.INVALID_CLIENT,
				"Wechat client authentication failed: " + parameterName,
				ERROR_URI
		);
		throw new OAuth2AuthenticationException(error);
	}

}