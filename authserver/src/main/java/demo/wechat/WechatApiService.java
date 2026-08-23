package demo.wechat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 微信小程序服务端接口封装。
 *
 * <p>appid / secret 仅保存在 authserver 端，通过配置注入，绝不进入小程序代码包。</p>
 */
@Service
public class WechatApiService {

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    /** 本地开发调试开关：开启后跳过真实的 jscode2session 调用，返回 mock 的 openid。 */
    @Value("${wechat.mock-enabled:false}")
    private boolean mockEnabled;

    /** mock 模式下返回的 openid。 */
    @Value("${wechat.mock-openid:mock-openid}")
    private String mockOpenid;

    private final RestClient restClient = RestClient.create();

    /**
     * 调用微信 jscode2session 接口，用一次性临时 code 换取 openid / session_key。
     */
    public WechatSession code2Session(String jsCode) {
        if (this.mockEnabled) {
            WechatSession session = new WechatSession();
            session.setOpenid(this.mockOpenid);
            session.setErrcode(0);
            return session;
        }

        WechatSession session = this.restClient.get()
                .uri("https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code",
                        this.appid, this.secret, jsCode)
                .retrieve()
                .body(WechatSession.class);

        if (session == null || (session.getErrcode() != null && session.getErrcode() != 0)) {
            // 常见错误：40029 code 无效、45011 频率限制
            String errmsg = session == null ? "空响应" : session.getErrmsg();
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, "微信登录失败: " + errmsg, null));
        }
        return session;
    }
}