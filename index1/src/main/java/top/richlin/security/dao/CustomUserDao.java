package top.richlin.security.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.UserInfo;

/**
 * CustomUserDao
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/15 22:33
 * @description
 */
@Mapper
public interface CustomUserDao extends BaseMapper<CustomUser> {
    CustomUser loadByUsername(String username);
    UserInfo getInfoByUsername(String username);

}
