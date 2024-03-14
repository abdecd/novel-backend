package com.abdecd.novelbackend.business.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageChecker {
    private static final int HEAD_BYTES_TO_READ = 12;

    public static boolean isImage(MultipartFile file) throws IOException {
        byte[] headerBytes = new byte[HEAD_BYTES_TO_READ];
        InputStream inputStream = file.getInputStream();
        inputStream.read(headerBytes, 0, HEAD_BYTES_TO_READ);

        return checkImageType(headerBytes);
    }

    public static boolean isImage(File file) throws IOException {
        byte[] headerBytes = new byte[HEAD_BYTES_TO_READ];
        try (InputStream inputStream = new FileInputStream(file)) {
            inputStream.read(headerBytes, 0, HEAD_BYTES_TO_READ);
        }

        return checkImageType(headerBytes);
    }

    /**
     * 检查文件头并确定是否是图片文件
     * @param headerBytes 文件头字节数组
     * @return 是否为图片
     */
    private static boolean checkImageType(byte[] headerBytes) {
        String hexString = bytesToHexString(headerBytes);
        System.out.println("hexString"+hexString);

        // 示例：简单检查JPEG、PNG、GIF等常见图片类型的头部特征
        return (
                (hexString.startsWith("FFD8FF")) // JPEG
                || (hexString.startsWith("89504E47")) // PNG
//                || (hexString.startsWith("47494638")) // GIF
                || (hexString.startsWith("52494646") // RIFF
                        && hexString.startsWith("57454250", 16)) // WEBP
                || hexString.startsWith("56503858") // WEBP
                || hexString.startsWith("6674797061766966", 8)// ftyp avif
        );
    }


    private static String bytesToHexString(byte[] src) {
        StringBuilder sb = new StringBuilder();
        for (byte b : src) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString().toUpperCase();
    }
}
