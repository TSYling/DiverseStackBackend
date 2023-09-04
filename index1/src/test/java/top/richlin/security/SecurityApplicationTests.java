package top.richlin.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.Rooms;
import top.richlin.security.template.EmailTemplate;
import top.richlin.security.util.IPUtils;

import java.io.IOException;

@SpringBootTest
class SecurityApplicationTests {

    @Autowired
    CustomUserDao userDao;
    @Test
    void contextLoads() {
    }
    @Test
    void IpUtilTest() throws IOException {
//        String ip = "111.78.142.144";
        String ip = "127.0.0.1";
        System.out.println(IPUtils.getLocation(ip));
    }
    @Test
    void JDBCTest(){
        QueryWrapper<CustomUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id","1");
        System.out.println(userDao.selectOne(queryWrapper));
    }
    @Test
    void randomCodeTest(){
//        EmailTemplate emailTemplate = new EmailTemplate();
//        emailTemplate.loginSend("123");
    }
    @Test
    void RoomTest(){

    }

}
