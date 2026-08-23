package demo.wechat;

/**
 * 微信小程序用户的内部模型。
 *
 * <p>首次通过微信登录时按 openid 自动注册，openid 作为用户在系统中的唯一业务标识。</p>
 */
public class WechatUser {

    private final String openid;

    public WechatUser(String openid) {
        this.openid = openid;
    }

    public String getOpenid() {
        return this.openid;
    }
}