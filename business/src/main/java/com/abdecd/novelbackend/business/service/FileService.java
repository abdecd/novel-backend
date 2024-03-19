package com.abdecd.novelbackend.business.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

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
     * @param fileName :
     * @return url
     * @throws IOException :
     */
    default String uploadFile(MultipartFile file, String fileName) throws IOException {
        throw new RuntimeException("未实现");
    }

    /**
     * 文件上传
     * @param file :
     * @param folder :
     * @param fileName :
     * @return url
     * @throws IOException :
     */
    default String uploadFile(MultipartFile file, String folder, String fileName) throws IOException {
        throw new RuntimeException("未实现");
    }

    /**
     * 文件上传 不会关输入流
     * @param inputStream :
     * @param fileName :
     * @return url
     * @throws IOException :
     */
    default String uploadFile(InputStream inputStream, String fileName) throws IOException {
        throw new RuntimeException("未实现");
    }

    /**
     * 文件上传 不会关输入流
     * @param inputStream :
     * @param folder :
     * @param fileName :
     * @return url
     * @throws IOException :
     */
    default String uploadFile(InputStream inputStream, String folder, String fileName) throws IOException {
        throw new RuntimeException("未实现");
    }

    /**
     * 临时文件转正 不成功返回空字符串
     * @param tmpPath url
     * @param folder :
     * @param fileName :
     * @return url
     */
    default String changeTmpFileToStatic(String tmpPath, String folder, String fileName) throws IOException {
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
     * 文件获取 输入流记得关
     * @param path 相对路径，如 /img/xxx
     * @throws IOException :
     */
    default InputStream getFileInSystem(String path) throws IOException {
        throw new RuntimeException("未实现");
    }
}
