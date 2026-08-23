package demo.wechat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 微信小程序用户的 {@link UserDetails} 实现。
 *
 * <p>以 openid 作为用户名，并携带 openid 供令牌定制器写入 JWT claims。
 * 由于实例会被作为授权主体（principal）持久化到 {@code OAuth2Authorization}，
 * 因此提供无参构造与 setter，并对派生方法标注 {@link JsonIgnore}，保证能被
 * Jackson 正确序列化 / 反序列化（用于刷新令牌恢复用户身份）。</p>
 */
public class WechatUserDetails implements UserDetails {

    private String openid;

    public WechatUserDetails() {
        // 供 Jackson 反序列化使用
    }

    public WechatUserDetails(WechatUser user) {
        this.openid = user.getOpenid();
    }

    public String getOpenid() {
        return this.openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.openid;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}