package top.richlin.security.util;

/**
 * IPUtils
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/16 19:49
 * @description
 */

import jakarta.servlet.http.HttpServletRequest;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
public class IPUtils {
    public static String getLocation(String ip) throws IOException {
        String region = null;
        String dbPath = new ClassPathResource("IPDB/ip2region.xdb").getURL().getPath();
        Searcher searcher = null;
        try {
            searcher = Searcher.newWithFileOnly(dbPath);
        } catch (IOException e) {
            System.out.printf("failed to create searcher with `%s`: %s\n", dbPath, e);
            return "地址查询失败(1)！";
        }

        // 2、查询
        try {
            region = searcher.search(ip);
        } catch (Exception e) {
            System.out.printf("failed to search(%s): %s\n", ip, e);
            return "地址查询失败(2)！";
        }

        // 3、关闭资源
        searcher.close();
        return (new IPLocationInfo(region)).getDetail();
    }

    public static String getLocation(HttpServletRequest request) {
        return "OK";
    }
}

class IPLocationInfo {
    String country;
    String LocationInfo;

    IPLocationInfo(String region) {
        LocationInfo = "";
        List<String> result = List.of(region.split("\\|"));
        country = result.get(0);
        for (int i = 1; i <= 4; i++) {
            if(!result.get(i).equals("0")){
                if(i==4){
                    if(country.equals("0"))
                        break;
                    LocationInfo = LocationInfo + "("+result.get(i)+")";
                }
                else {
                    LocationInfo = LocationInfo + result.get(i);
                }
            }
        }
    }

    public String getDetail() {
        if (country.equals("中国")||country.equals("0")) {
            return LocationInfo;
        }else{
            return country+LocationInfo;
        }
    }

}
