package com.abdecd.novelbackend.business.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    /**
     * 临时文件上传
     * @param file :
     * @return url
     * @throws IOException :
     */
    String uploadTmpFile(MultipartFile file) throws IOException;

    /**
     * 文件上传
     * @param file :
     * @return url
     * @throws IOException :
     */
    default String uploadFile(MultipartFile file) throws IOException {
        throw new RuntimeException("未实现");
    }

    /**
     * 临时文件转正 不成功返回空字符串
     * @param tmpPath url
     * @return url
     */
    default String changeTmpFileToStatic(String tmpPath, String folder) throws IOException {
        throw new RuntimeException("未实现");
    }

    /**
     * 文件删除
     * Dangerous
     * @param path url
     */
    default void deleteFile(String path) {
        throw new RuntimeException("未实现");
    }

    /**
     * 图片查看
     * @param path 相对路径，如 /img/xxx
     * @param response :
     * @throws IOException :
     */
    default void viewImg(String path, HttpServletResponse response) throws IOException {
        throw new RuntimeException("未实现");
    }
}
