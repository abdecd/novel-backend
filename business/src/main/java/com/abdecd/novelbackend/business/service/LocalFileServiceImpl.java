package com.abdecd.novelbackend.business.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileServiceImpl implements FileService {

    @Value("${novel.local-file-service.img-path:empty}")
    private String IMG_PATH;

    @Value("${novel.local-file-service.url-prefix:empty}")
    private String URL_PREFIX;

    private String basicUpload(MultipartFile file, String folder) throws IOException {
        if (IMG_PATH.equals("empty")) return "";
        var suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        var dest = new File(IMG_PATH + folder + "/" + UUID.randomUUID() + suffix);
        if (dest.exists()) throw new IOException("文件已存在");
        dest.getParentFile().mkdirs();
        file.transferTo(dest);
        log.info("当前线程：{}", Thread.currentThread().getName());
        log.info("上传成功,{}", dest.getAbsolutePath());
        // 适配前端
        return URL_PREFIX + folder + "/" + dest.getName();
    }

    @Override
    public String uploadTmpImg(MultipartFile file) throws IOException {
        return basicUpload(file, "/img/tmp");
    }

    @Override
    public String uploadImg(MultipartFile file) throws IOException {
        return basicUpload(file, "/img");
    }

    @Override
    public String changeTmpImgToStatic(String fullTmpFilePath) {
        // /img/tmp/xxx  ->  /img/xxx
        if (IMG_PATH.equals("empty")) return "";
        var tmpFilePath = fullTmpFilePath.substring(URL_PREFIX.length());
        var oldPath = IMG_PATH + tmpFilePath;
        var newPath = IMG_PATH + "/img" + tmpFilePath.substring(tmpFilePath.lastIndexOf("/"));
        var tmp = new File(oldPath);
        // 适配前端
        return URL_PREFIX + (tmp.renameTo(new File(newPath)) ?
                "/img" + tmpFilePath.substring(tmpFilePath.lastIndexOf("/"))
                : tmpFilePath);
    }

    @Override
    public void viewImg(String path, HttpServletResponse response) throws IOException {
        if (IMG_PATH.equals("empty")) return;
        response.setContentType("image/jpeg");
        var file = new File(IMG_PATH + path);
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
