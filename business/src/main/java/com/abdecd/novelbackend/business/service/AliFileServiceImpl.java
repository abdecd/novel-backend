package com.abdecd.novelbackend.business.service;

import com.abdecd.tokenlogin.common.context.UserContext;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CopyObjectRequest;
import com.aliyun.oss.model.GetObjectRequest;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "ali.oss.enable", havingValue = "true")
public class AliFileServiceImpl implements FileService {
    @Value("${ali.oss.endpoint:empty}")
    String endpoint;
    @Value("${ali.oss.endpoint:empty}")
    String FILE_BASE_PATH;
    @Value("${ali.oss.bucket-name}")
    String bucketName;
    @Value("${ali.oss.access-key-id}")
    String accessKeyId;
    @Value("${ali.oss.access-key-secret}")
    String accessKeySecret;
    @Value("${ali.oss.url-prefix}")
    private String URL_PREFIX;

    public static final String TMP_FOLDER_BASE = "/tmp";
    public static String getTmpFolder() {
        return TMP_FOLDER_BASE + "/user" + UserContext.getUserId();
    }

    public static String getFileFolder() {
        return "/img";
    }

    private String basicUpload(MultipartFile file, String folder, String fileName) throws IOException {
        if (FILE_BASE_PATH.equals("empty")) return "";
        // 保存文件
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, (folder + "/" + fileName).substring(1), file.getInputStream());
        // 关闭OSSClient。
        ossClient.shutdown();
        // 适配前端
        return URL_PREFIX + folder + "/" + fileName;
    }

    private String basicUpload(InputStream inputStream, String folder, String fileName) throws IOException {
        if (FILE_BASE_PATH.equals("empty")) return "";
        // 保存文件
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, (folder + "/" + fileName).substring(1), inputStream);
        // 关闭OSSClient。
        ossClient.shutdown();
        // 适配前端
        return URL_PREFIX + folder + "/" + fileName;
    }

    @Override
    public String uploadTmpFile(MultipartFile file) throws IOException {
        var suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        return basicUpload(file, getTmpFolder(), UUID.randomUUID() + suffix);
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        return basicUpload(file, getFileFolder(), fileName);
    }

    @Override
    public String uploadFile(MultipartFile file, String folder, String fileName) throws IOException {
        return basicUpload(file, folder, fileName);
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName) throws IOException {
        return basicUpload(inputStream, getFileFolder(), fileName);
    }

    @Override
    public String uploadFile(InputStream inputStream, String folder, String fileName) throws IOException {
        return basicUpload(inputStream, folder, fileName);
    }

    @Override
    public String changeTmpFileToStatic(String fullTmpFilePath, String folder, String fileName) throws IOException {
        // getTmpFolder()/xxx  ->  folder/xxx
        if (FILE_BASE_PATH.equals("empty")) return "";
        if (folder == null || folder.isBlank()) folder = getFileFolder();
        if (!fullTmpFilePath.startsWith(URL_PREFIX)) return "";
        var tmpFilePath = fullTmpFilePath.substring(URL_PREFIX.length());
        if (!tmpFilePath.startsWith(getTmpFolder()+"/")) return "";
        var newFileName = (fileName == null || fileName.isBlank()) ? tmpFilePath.substring(tmpFilePath.lastIndexOf("/")) : ("/" + fileName);
        var newPath = folder + newFileName;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        CopyObjectRequest request = new CopyObjectRequest(bucketName, tmpFilePath.substring(1), bucketName, newPath.substring(1));
        ossClient.copyObject(request);
        ossClient.shutdown();
        return URL_PREFIX + newPath;
    }

    @Override
    public void deleteFile(String path) {
        if (FILE_BASE_PATH.equals("empty")) return;
        if (!path.startsWith(URL_PREFIX)) return;
        var tmpFilePath = path.substring(URL_PREFIX.length());
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.deleteObject(bucketName, tmpFilePath.substring(1));
        ossClient.shutdown();
    }

    @Override
    public void deleteFileInSystem(String path) {
        if (FILE_BASE_PATH.equals("empty")) return;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.deleteObject(bucketName, path.substring(1));
        ossClient.shutdown();
    }

    @Override
    public InputStream getFileInSystem(String path) throws IOException {
        if (FILE_BASE_PATH.equals("empty")) return null;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        var obj = ossClient.getObject(new GetObjectRequest(bucketName, path.substring(1)));
        return new AliInputStream(obj.getObjectContent(), ossClient);
    }
    static class AliInputStream extends InputStream {
        private final InputStream inputStream;
        private final OSS ossClient;
        AliInputStream(InputStream inputStream, OSS ossClient) {
            this.inputStream = inputStream;
            this.ossClient = ossClient;
        }
        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
        @Override
        public void close() throws IOException {
            inputStream.close();
            ossClient.shutdown();
        }
        @Override
        public int available() throws IOException {
            return inputStream.available();
        }
        @Override
        public long skip(long n) throws IOException {
            return inputStream.skip(n);
        }
        @Override
        public int read(@Nonnull byte[] b) throws IOException {
            return inputStream.read(b);
        }
        @Override
        public int read(@Nonnull byte[] b, int off, int len) throws IOException {
            return inputStream.read(b, off, len);
        }
    }
}
