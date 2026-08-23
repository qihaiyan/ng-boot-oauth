package demo.wechat;

/**
 * 微信小程序自定义授权类型常量。
 *
 * <p>grant_type 采用 RFC 推荐的 URN 格式，避免与标准授权类型冲突。</p>
 */
public final class WechatGrantTypes {

    /**
     * 微信小程序登录授权类型。
     */
    public static final String WECHAT_CODE = "urn:springcamp:params:oauth:grant-type:wechat-code";

    private WechatGrantTypes() {
    }
}