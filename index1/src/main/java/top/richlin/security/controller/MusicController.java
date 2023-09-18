package top.richlin.security.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import top.richlin.security.form.SearchDetailForm;
import top.richlin.security.service.MusicService;

import java.io.*;

/**
 * MusicController
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/25 15:40
 * @description 提供音乐服务
 */
@RestController
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @PostMapping("/search")
    @ResponseBody
    public void search(HttpServletResponse response, SearchDetailForm searchDetail) throws IOException {
        musicService.search(response,searchDetail);
    }

    @PostMapping("/info")
    @ResponseBody
    public void info(HttpServletResponse response,String musicId) throws IOException {
        musicService.info(response,musicId);
    }
    @PostMapping("/lrc")
    @ResponseBody
    public void lrc(HttpServletResponse response,String musicId) throws IOException {
        musicService.lrc(response,musicId);
    }

}