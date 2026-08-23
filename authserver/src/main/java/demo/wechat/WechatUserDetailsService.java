package demo.wechat;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信小程序用户加载服务。
 *
 * <p>按 openid 加载用户，首次登录时自动注册。本项目使用内存存储作为演示；
 * 生产环境可替换为数据库或其它持久化存储。</p>
 */
@Service
public class WechatUserDetailsService implements UserDetailsService {

    private final Map<String, WechatUser> usersByOpenid = new ConcurrentHashMap<>();

    /**
     * 根据微信 openid 加载用户，首次登录时自动注册。
     */
    public UserDetails loadUserByWechatOpenid(String openid) {
        WechatUser user = this.usersByOpenid.computeIfAbsent(openid, WechatUser::new);
        return new WechatUserDetails(user);
    }

    /**
     * 本服务仅用于微信登录，不支持用户名/密码方式。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("仅支持微信小程序登录");
    }
}