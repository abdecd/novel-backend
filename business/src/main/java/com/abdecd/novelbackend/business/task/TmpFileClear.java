package com.abdecd.novelbackend.business.task;

import com.abdecd.novelbackend.business.service.LocalFileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnBean(LocalFileServiceImpl.class)
public class TmpFileClear {
    @Autowired
    private LocalFileServiceImpl localFileService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void clearTmpFile() throws IOException {
        System.out.println("开始清理临时文件");
        localFileService.clearTmpFile(300);
        System.out.println("清理临时文件完成");
    }
}
