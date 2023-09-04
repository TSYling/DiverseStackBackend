package top.richlin.security.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import top.richlin.security.form.SearchDetailForm;
import top.richlin.security.service.MusicService;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;

/**
 * MusicServiceImpl
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/29 17:34
 * @description 与音乐 搜索查询音乐相关服务
 */
@Service
public class MusicServiceImpl implements MusicService {
    @Override
    public void search(HttpServletResponse response, SearchDetailForm searchForm) throws IOException {
        String key = searchForm.getKey();
        String pn = searchForm.getPn();
        String rn = searchForm.getRn();
        // 判断传入的参数
        if(!StringUtils.isNumeric(pn)||!StringUtils.isNumeric(rn)||!StringUtils.isNoneBlank(key)){
            response=new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","参数错误！")
                    .build();
            return;
        }

//        String url = "http://www.kuwo.cn/api/www/search/searchMusicBykeyWord?httpsStatus=1&reqId=adee0ce0-1e45-11ee-a3a8-21d20b32f0df&plat=web_www&from="; // 要请求的URL
//        url += "&key="+key+"&pn="+pn+"&rn="+rn;
        String url = "https://search.kuwo.cn/r.s?ft=music&rformat=json&encoding=utf8&plat=pc&vipver=MUSIC_9.2.0.0_W6&pcjson=1";
        url += "&all="+key+"&pn="+pn+"&rn="+rn;
        JsonNode responseBodyEntity = getRequestResponseBodyEntity(url);
        System.out.println(responseBodyEntity);

        response=new ResponseTemplate(response)
                .successTemplate()
                .putInformation("data",responseBodyEntity.get("abslist"))
                .putInformation("total",responseBodyEntity.get("TOTAL"))
                .build();
        response.flushBuffer();
    }

    @Override
    public void info(HttpServletResponse response, String musicId) throws IOException {
        // 判断传入的参数
        if(!StringUtils.isNumeric(musicId)){
            response=new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","参数错误！")
                    .build();
            return;
        }
        String url = "http://www.kuwo.cn/api/v1/www/music/playUrl?type=music&httpsStatus=1&reqId=5a3ed935b759f7f6cabd9a180acf85e2"; // 要请求的URL
        url += "&mid="+musicId;

        JsonNode responseBodyEntity = getRequestResponseBodyEntity(url);
        response=new ResponseTemplate(response)
                .successTemplate()
                .putInformation("msg",responseBodyEntity.get("msg"))
                .putInformation("data",responseBodyEntity.get("data"))
                .build();
        response.flushBuffer();
    }

    @Override
    public void lrc(HttpServletResponse response, String musicId) throws IOException {
        // 判断传入的参数
        if(!StringUtils.isNumeric(musicId)){
            response=new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","参数错误！")
                    .build();
            return;
        }
        String url = "http://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId=";
        url+=musicId;
        JsonNode responseBodyEntity = getRequestResponseBodyEntity(url);
        response=new ResponseTemplate(response)
                .successTemplate()
                .putInformation("data",responseBodyEntity.get("data"))
                .build();
        response.flushBuffer();
    }

    private JsonNode getRequestResponseBodyEntity(String url) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36 SE 2.X MetaSr 1.0");
        request.setHeader("Accept","application/json, text/plain, */*");
        request.setHeader("Accept-Encoding","gzip, deflate");
        request.setHeader("Referer","https://www.kuwo.cn/search/list?key=%E5%91%A8%E6%9D%B0%E4%BC%A6");
        request.setHeader("Secret","44f165b2aacd665d3cc78018a1db619f6ab652546010027ffb1649fd62ae001701eff8aa");
        request.setHeader("Cookie","_ga=GA1.2.1345412530.1691590027; _gid=GA1.2.564150091.1691590027; Hm_lvt_cdb524f42f0ce19b169a8071123a4797=1690328592,1690451916,1691143514,1691589934; _gat=1; Hm_lpvt_cdb524f42f0ce19b169a8071123a4797=1691590098; _ga_ETPBRPM9ML=GS1.2.1691590027.1.1.1691590097.52.0.0; Hm_Iuvt_cdb524f42f0cer9b268e4v7y734w5esq24=dHzyS6B7G7RHHBn4pWnKWws38SFnHXhx");
        request.setHeader("sec-ch-ua-mobile","?0");
        request.setHeader("Sec-Fetch-Site","same-origin");

        HttpResponse dataResponse = client.execute(request);

        // 获取响应代码
//        int statusCode = dataResponse.getStatusLine().getStatusCode();

        String responseBody = EntityUtils.toString(dataResponse.getEntity());
        client.close();
        return new ObjectMapper().readTree(responseBody);
    }
}
