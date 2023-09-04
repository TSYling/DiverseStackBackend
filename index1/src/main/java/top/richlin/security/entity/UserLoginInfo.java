package top.richlin.security.entity;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataUnit;
import top.richlin.security.util.IPUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UserInfo
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/16 0:34
 * @description
 */
public class UserLoginInfo extends UsernamePasswordAuthenticationToken {
    private final SecurityUserInfo securityUserInfo;
    private final Details details;

    public UserLoginInfo(UsernamePasswordAuthenticationToken authenticationToken) throws IOException {
        super(authenticationToken.getPrincipal(),authenticationToken.getCredentials(),authenticationToken.getAuthorities());
        WebAuthenticationDetails webAuthenticationDetails = (WebAuthenticationDetails) authenticationToken.getDetails();
        this.details = new Details(webAuthenticationDetails.getRemoteAddress(),webAuthenticationDetails.getSessionId());
        CustomUser customUser = (CustomUser) authenticationToken.getPrincipal();
        this.securityUserInfo = new SecurityUserInfo(customUser);
    }
    public Map<String,Object> info(){
        Map<String,Object> info = new HashMap<>();
        info.put("details",details);
        info.put("principal",securityUserInfo);
        return info;
    }
}
@Data
class SecurityUserInfo{
    private Integer id;

    private String email;

    private String name;
    private String phone;

    private int vipLevel;
    private Integer coin;
    private String registerTime;
    private String lastLoginTime;
    private boolean isAdmin;
    private String inviteCode;
    private int activeStatusCode;
    private String activeStatus;

    public SecurityUserInfo(CustomUser customUser) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.id = customUser.getId();
        this.email = customUser.getEmail();
        this.name  = customUser.getName();
        if(StringUtils.hasText(customUser.getPhone()))
            this.phone = new StringBuffer(customUser.getPhone()).replace(3,7,"*".repeat(4)).toString();
        else phone = null;
        this.vipLevel = customUser.getVipLevel();
        this.coin = customUser.getCoin();
        this.registerTime = dateFormat.format(customUser.getRegisterTime());
        if(customUser.getLastLoginTime()==null)
            customUser.setLastLoginTime(new Date(0));
        this.lastLoginTime = dateFormat.format(customUser.getLastLoginTime());
        this.isAdmin = customUser.isAdmin();
        this.inviteCode = customUser.getInviteCode();
        this.activeStatusCode = customUser.getActiveStatus();
        /**
         * 用户账号所处状态 1表示激活 0表示未激活 2表示手动冻结 3表示封禁 4表示永久封禁 5表示在线
         */
        this.activeStatus = switch (this.activeStatusCode) {
            case 0 -> "inactive";
            case 1 -> "active";
            case 2 -> "frozen";
            case 3 -> "banned";
            case 4 -> "banish";
            case 5 -> "online";
            default -> "error";
        };
    }
}
class Details extends WebAuthenticationDetails{
    private final String location;

    public String getLocation() {
        return location;
    }

    public Details(String remoteAddress, String sessionId) throws IOException {
        super(remoteAddress, sessionId);
        location = IPUtils.getLocation(this.getRemoteAddress());
    }
}
