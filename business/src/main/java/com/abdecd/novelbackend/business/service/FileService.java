package com.abdecd.novelbackend.business.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    /**
     * 临时图片上传
     * @param file :
     * @return url
     * @throws IOException :
     */
    String uploadTmpImg(MultipartFile file) throws IOException;

    /**
     * 图片上传
     * @param file :
     * @return url
     * @throws IOException :
     */
    default String uploadImg(MultipartFile file) throws IOException {
        throw new RuntimeException("未实现");
    }

    /**
     * 临时图片转正
     * @param tmpPath url
     * @return url
     */
    default String changeTmpImgToStatic(String tmpPath) {
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
