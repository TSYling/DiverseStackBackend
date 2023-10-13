package top.richlin.security.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * UserInfo
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/30 21:03
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String id;
    // user
    private String email;
    private String name;
    private String headUrl;
    private String phone;
    private int vipLevel;
    private int coin;
    private Date registerTime;
    private Date lastLoginTime;
    private String inviteCode;
    private int activeStatus;
    private String activeStatusString;
    // user_info
    private String nickname;
    private String signature;
    private String gender;
    private Date birthday;
    private String address;
    private int followers;
    private int following;
    // user_level_info
    private int level;
    private int exp;

}
