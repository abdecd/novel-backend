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
        // 保存文件
        dest.getParentFile().mkdirs();
        file.transferTo(dest);
        log.info("当前线程：{}", Thread.currentThread().getName());
        log.info("上传成功,{}", dest.getAbsolutePath());
        // 适配前端
        return URL_PREFIX + folder + "/" + dest.getName();
    }

    @Override
    public String uploadTmpImg(MultipartFile file) throws IOException {
        return basicUpload(file, "/tmp");
    }

    @Override
    public String uploadImg(MultipartFile file) throws IOException {
        return basicUpload(file, "/img");
    }

    @Override
    public String changeTmpImgToStatic(String fullTmpFilePath, String folder) {
        // /tmp/xxx  ->  folder/xxx
        if (IMG_PATH.equals("empty")) return "";
        if (folder == null || folder.isEmpty()) folder = "/img";
        if (!fullTmpFilePath.startsWith(URL_PREFIX)) return "";
        var tmpFilePath = fullTmpFilePath.substring(URL_PREFIX.length());
        if (!tmpFilePath.startsWith("/tmp/")) return "";
        var oldPath = IMG_PATH + tmpFilePath;
        var newPath = IMG_PATH + folder + tmpFilePath.substring(tmpFilePath.lastIndexOf("/"));
        var tmp = new File(oldPath);
        if (!tmp.renameTo(new File(newPath))) return "";
        return URL_PREFIX + folder + tmpFilePath.substring(tmpFilePath.lastIndexOf("/"));
    }

    @Override
    public void deleteImg(String path) {
        if (IMG_PATH.equals("empty")) return;
        if (!path.startsWith(URL_PREFIX)) return;
        var tmpFilePath = path.substring(URL_PREFIX.length());
        var oldPath = IMG_PATH + tmpFilePath;
        var file = new File(oldPath);
        if (file.exists() && file.isFile()) file.delete();
    }

    public void clearTmpImg(Integer ttl) throws IOException {
        if (IMG_PATH.equals("empty")) return;
        var tmpDir = new File(IMG_PATH + "/tmp");
        if (tmpDir.exists()) {
            var files = tmpDir.listFiles();
            if (files != null) {
                for (var file : files) {
                    var fileTime = Files.getLastModifiedTime(file.toPath()).toMillis();
                    var now = System.currentTimeMillis();
                    if (now - fileTime > ttl * 1000) {
                        log.info("删除过期文件:{}", file.getAbsolutePath());
                        file.delete();
                    }
                }
            }
        }
    }

    @Override
    public void viewImg(String path, HttpServletResponse response) throws IOException {
        if (IMG_PATH.equals("empty")) return;
        response.setContentType("image/jpeg");
        response.setHeader("Cache-Control", "public, max-age=31536000");
        var file = new File(IMG_PATH + path);
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
