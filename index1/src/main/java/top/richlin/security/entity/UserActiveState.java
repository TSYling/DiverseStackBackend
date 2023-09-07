package top.richlin.security.entity;

/**
 * UserActiveState
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/7 18:53
 * @description
 */
public class UserActiveState {
    /**
     * 是否已激活 可用于冻结封禁账号
     * 1表示激活 不在线
     * 0表示未激活
     * 2表示手动冻结
     * 3表示封禁
     * 4表示永久封禁
     * 5表示在线
     */
    public static int UNACTIVATED = 0;
    public static int ACTIVATED = 1;
    public static int OFFLINE = 1;
    public static int SAFE_FROZEN = 2;
    public static int BANNED = 3;
    public static int BANNED_FOREVER = 4;
    public static int ONLINE = 4;

}
