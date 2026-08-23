package demo.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import demo.authentication.DeviceClientAuthenticationProvider;
import demo.federation.FederatedIdentityAuthenticationSuccessHandler;
import demo.federation.FederatedIdentityIdTokenCustomizer;
import demo.jose.Jwks;
import demo.web.authentication.DeviceClientAuthenticationConverter;
import demo.wechat.WechatApiService;
import demo.wechat.WechatClientAuthenticationConverter;
import demo.wechat.WechatClientAuthenticationProvider;
import demo.wechat.WechatCodeGrantAuthenticationConverter;
import demo.wechat.WechatCodeGrantAuthenticationProvider;
import demo.wechat.WechatGrantTypes;
import demo.wechat.WechatUserDetails;
import demo.wechat.WechatUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http, RegisteredClientRepository registeredClientRepository,
            AuthorizationServerSettings authorizationServerSettings,
            WechatApiService wechatApiService,
            WechatUserDetailsService wechatUserDetailsService,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<?> tokenGenerator) {

        DeviceClientAuthenticationConverter deviceClientAuthenticationConverter =
                new DeviceClientAuthenticationConverter(
                        authorizationServerSettings.getDeviceAuthorizationEndpoint());
        DeviceClientAuthenticationProvider deviceClientAuthenticationProvider =
                new DeviceClientAuthenticationProvider(registeredClientRepository);

        http.oauth2AuthorizationServer((authorizationServer) ->
                        authorizationServer
                                .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint ->
                                        deviceAuthorizationEndpoint.verificationUri("/activate")
                                )
                                .deviceVerificationEndpoint(deviceVerificationEndpoint ->
                                        deviceVerificationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI)
                                )
                                .clientAuthentication(clientAuthentication ->
                                        clientAuthentication
                                                .authenticationConverter(deviceClientAuthenticationConverter)
                                                .authenticationConverter(new WechatClientAuthenticationConverter())
                                                .authenticationProvider(deviceClientAuthenticationProvider)
                                                .authenticationProvider(new WechatClientAuthenticationProvider(registeredClientRepository))
                                )
                                .authorizationEndpoint(authorizationEndpoint ->
                                        authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))
                                .oidc(Customizer.withDefaults())    // Enable OpenID Connect 1.0
                                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                                        .accessTokenRequestConverter(new WechatCodeGrantAuthenticationConverter())
                                        .authenticationProvider(new WechatCodeGrantAuthenticationProvider(
                                                wechatApiService, wechatUserDetailsService,
                                                authorizationService, tokenGenerator)))
                )
                // Redirect to the /login page when not authenticated from the authorization endpoint
                // NOTE: DefaultSecurityConfig is configured with formLogin.loginPage("/login")
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/assets/**", "/login").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .loginPage("/login")
                                .successHandler(authenticationSuccessHandler())
                )
                .cors(Customizer.withDefaults());
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/oauth2/**", "/.well-known/**", "/login/**")
//                        .permitAll()
//                        .anyRequest().authenticated()
//                );
//                .logout(logout -> logout
//                        .addLogoutHandler(clearSiteData)
//                        .clearAuthentication(true)
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .permitAll());
        return http.build();
    }

    HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL));

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new FederatedIdentityAuthenticationSuccessHandler();
    }

    @Bean
    public JdbcRegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient messagingClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("messaging-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUris((uris) -> uris.addAll(Set.of(
                        "http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc",
                        "http://127.0.0.1:8080/authorized"
                        ))
                )
                .postLogoutRedirectUris((uris) -> uris.addAll(Set.of(
                        "http://localhost:8080",
                        "http://127.0.0.1:8080"
                )))
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("message.read")
                .scope("message.write")
                .scope("user.read")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();

        RegisteredClient deviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("device-messaging-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope("message.read")
                .scope("message.write")
                .build();

        RegisteredClient tokenExchangeClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("token-client")
                .clientSecret("{noop}token")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:token-exchange"))
                .scope("message.read")
                .scope("message.write")
                .build();

        RegisteredClient mtlsDemoClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("mtls-demo-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.TLS_CLIENT_AUTH)
                .clientAuthenticationMethod(ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("message.read")
                .scope("message.write")
                .clientSettings(
                        ClientSettings.builder()
                                .x509CertificateSubjectDN("CN=demo-client-sample,OU=Spring Samples,O=Spring,C=US")
                                .jwkSetUrl("http://127.0.0.1:8080/jwks")
                                .build()
                )
                .tokenSettings(
                        TokenSettings.builder()
                                .x509CertificateBoundAccessTokens(true)
                                .build()
                )
                .build();

        RegisteredClient publicClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("public-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(true)
                        .build()
                )
                .redirectUris((uris) -> uris.addAll(Set.of(
                        "http://127.0.0.1:4200",
                        "http://localhost:4200",
                        "http://localhost:5173"
                        ))
                )
                .postLogoutRedirectUris((uris) -> uris.addAll(Set.of(
                        "http://127.0.0.1:4200",
                        "http://localhost:4200",
                        "http://localhost:5173"
                )))
                .build();

        // 微信小程序客户端：不注册 openid 作用域。
        // 小程序无需 OIDC id_token，仅需带 openid 声明的 access_token；
        // 若引入 openid 作用域，刷新令牌时 OAuth2RefreshTokenAuthenticationProvider
        // 会尝试再生成 id_token，但自定义 wechat-code 授权未保存 id_token，
        // 导致 JwtGenerator 抛 "authorizedIdToken cannot be null"。
        RegisteredClient miniappClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("miniapp-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(new AuthorizationGrantType(WechatGrantTypes.WECHAT_CODE))
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope("message.read")
                .build();

        // Save registered client's in db as if in-memory
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        registeredClientRepository.save(messagingClient);
        registeredClientRepository.save(deviceClient);
        registeredClientRepository.save(tokenExchangeClient);
        registeredClientRepository.save(mtlsDemoClient);
        registeredClientRepository.save(publicClient);
        registeredClientRepository.save(miniappClient);

        return registeredClientRepository;
    }

    @Bean
    public JdbcOAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                               RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public JdbcOAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                             RegisteredClientRepository registeredClientRepository) {
        // Will be used by the ConsentController
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer() {
        return new FederatedIdentityIdTokenCustomizer();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JWKSource<SecurityContext> jwkSource,
            OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer) {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        jwtGenerator.setJwtCustomizer(context -> {
            // 保留既有逻辑：融合第三方身份提供商 claims 到 id_token
            idTokenCustomizer.customize(context);
            // 将 openid 写入 access_token，资源服务器可直接获取微信身份。
            // 微信用户以 openid（String）作为认证主体名称；刷新令牌反序列化后仍是 String，
            // 因此这里统一读取 String 主体，避免依赖自定义类型。
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
                    && context.getPrincipal() != null
                    && context.getPrincipal().getPrincipal() instanceof String openid
                    && context.getRegisteredClient().getAuthorizationGrantTypes()
                            .contains(new AuthorizationGrantType(WechatGrantTypes.WECHAT_CODE))) {
                context.getClaims().claim("openid", openid);
            }
        });
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowedOrigins(List.of("http://127.0.0.1:4200", "http://localhost:4200", "http://localhost:5173"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public EmbeddedDatabase embeddedDatabase() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .setScriptEncoding("UTF-8")
                .addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql")
                .addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql")
                .addScript("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql")
                .build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}