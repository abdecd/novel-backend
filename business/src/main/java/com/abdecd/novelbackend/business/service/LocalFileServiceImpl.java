package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.util.ImageChecker;
import com.abdecd.tokenlogin.common.context.UserContext;
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

    @Value("${novel.local-file-service.file-base-path:empty}")
    private String FILE_BASE_PATH;

    @Value("${novel.local-file-service.url-prefix:empty}")
    private String URL_PREFIX;

    public static final String TMP_FOLDER_BASE = "/tmp";
    public static String getTmpFolder() {
        return TMP_FOLDER_BASE + "/user" + UserContext.getUserId();
    }

    public static String getFileFolder() {
        return "/img";
    }

    private String basicUpload(MultipartFile file, String folder) throws IOException {
        if (FILE_BASE_PATH.equals("empty")) return "";
        var suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        var dest = new File(FILE_BASE_PATH + folder + "/" + UUID.randomUUID() + suffix);
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
    public String uploadTmpFile(MultipartFile file) throws IOException {
        return basicUpload(file, getTmpFolder());
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        return basicUpload(file, getFileFolder());
    }

    @Override
    public String changeTmpFileToStatic(String fullTmpFilePath, String folder) throws IOException {
        // getTmpFolder()/xxx  ->  folder/xxx
        if (FILE_BASE_PATH.equals("empty")) return "";
        if (folder == null || folder.isEmpty()) folder = getFileFolder();
        if (!fullTmpFilePath.startsWith(URL_PREFIX)) return "";
        var tmpFilePath = fullTmpFilePath.substring(URL_PREFIX.length());
        if (!tmpFilePath.startsWith(getTmpFolder()+"/")) return "";
        var oldPath = FILE_BASE_PATH + tmpFilePath;
        var newPath = FILE_BASE_PATH + folder + tmpFilePath.substring(tmpFilePath.lastIndexOf("/"));
        var tmp = new File(oldPath);
        var newFile = new File(newPath);
        newFile.getParentFile().mkdirs();
        Files.move(tmp.toPath(), new File(newPath).toPath());
        return URL_PREFIX + folder + tmpFilePath.substring(tmpFilePath.lastIndexOf("/"));
    }

    @Override
    public void deleteFile(String path) {
        if (FILE_BASE_PATH.equals("empty")) return;
        if (!path.startsWith(URL_PREFIX)) return;
        var tmpFilePath = path.substring(URL_PREFIX.length());
        var oldPath = FILE_BASE_PATH + tmpFilePath;
        var file = new File(oldPath);
        if (file.exists() && file.isFile()) file.delete();
    }

    /**
     * 清除过期的临时文件
     * @param ttl 时长，单位秒
     * @throws IOException :
     */
    public void clearTmpFile(Integer ttl) throws IOException {
        if (FILE_BASE_PATH.equals("empty")) return;
        var tmpDir = new File(FILE_BASE_PATH + TMP_FOLDER_BASE);
        if (tmpDir.exists()) {
            var files = tmpDir.listFiles();
            if (files != null) {
                for (var file : files) {
                    clearTmpFileBase(file, ttl);
                }
            }
        }
    }
    private void clearTmpFileBase(File tmpDir, Integer ttl) throws IOException {
        if (tmpDir.exists() && tmpDir.isDirectory()) {
            var files = tmpDir.listFiles();
            if (files != null) {
                for (var file : files) {
                    if (file.isDirectory()) clearTmpFileBase(file, ttl);
                    else {
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
    }

    @Override
    public void viewImg(String path, HttpServletResponse response) throws IOException {
        if (FILE_BASE_PATH.equals("empty")) return;
        response.setContentType("image/jpeg");
        response.setHeader("Cache-Control", "public, max-age=31536000");
        var file = new File(FILE_BASE_PATH + path);
        if (file.exists() && file.isFile() && ImageChecker.isImage(file))
            Files.copy(file.toPath(), response.getOutputStream());
        else throw new BaseException("文件不存在或格式错误");
    }
}
