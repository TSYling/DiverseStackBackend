package top.richlin.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * CustomUser
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/15 22:18
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class CustomUser implements UserDetails {
    /**
     * 用户id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 用户邮箱
     */

    private String email;

    /**
     * 用户大名
     */
    private String name;
    /**
     * 用户头像地址
     */
    private String headUrl;
    /**
     * 用户电话
     */

    private String phone;
    /**
     * 用户密码
     */

    private String password;
    /**
     * 用户会员等级
     */
    private int vipLevel;
    /**
     * 用户硬币数
     */
    private Integer coin;
    /**
     * 用户注册时间
     */
    private Date registerTime;
    /**
     * 用户最后登录时间
     */
    private Date lastLoginTime;
    /**
     * 用户是否为管理员
     */
    private boolean isAdmin;
    /**
     * 用户填写的邀请码 可为空
     */
    private String inviteCode;
    /**
     * 用户账号所处状态 1表示激活 0表示未激活 2表示手动冻结 3表示封禁 4表示永久封禁 5表示在线
     */
    private int activeStatus;
    /**
     * 用户是否被删除 逻辑删除
     */
    private boolean isDelete;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(isAdmin? "Admin":"user"));
        authorities.add(new SimpleGrantedAuthority(String.valueOf(vipLevel)));
        return authorities;
    }

    @Override
    public String getUsername() {
        return id.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !isDelete;
    }
}
