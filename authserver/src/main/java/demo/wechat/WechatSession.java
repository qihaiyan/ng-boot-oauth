package demo.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信 jscode2session 接口返回的会话数据。
 *
 * <p>对应微信开放平台 <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html">code2Session</a> 的响应结构。</p>
 */
public class WechatSession {

    /** 用户在当前小程序下的唯一标识。 */
    private String openid;

    /** 会话密钥，仅服务端使用，不能下发给小程序。 */
    @JsonProperty("session_key")
    private String sessionKey;

    /** 同一微信开放平台账号下的小程序/公众号统一标识，可能为空。 */
    private String unionid;

    /** 错误码，0 表示成功。 */
    private Integer errcode;

    /** 错误信息。 */
    private String errmsg;

    public String getOpenid() {
        return this.openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getSessionKey() {
        return this.sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUnionid() {
        return this.unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public Integer getErrcode() {
        return this.errcode;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return this.errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}