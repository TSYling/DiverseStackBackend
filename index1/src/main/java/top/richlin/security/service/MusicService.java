package top.richlin.security.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import top.richlin.security.form.SearchDetailForm;

import java.io.IOException;

/**
 * MusciService
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/29 17:33
 * @description
 */
public interface MusicService {
    public void search(HttpServletResponse response, SearchDetailForm searchForm) throws IOException;
    public void info(HttpServletResponse response,String musicId)throws IOException;
    public void lrc(HttpServletResponse response,String musicId)throws IOException;
}