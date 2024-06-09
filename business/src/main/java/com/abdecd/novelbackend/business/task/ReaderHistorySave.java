package com.abdecd.novelbackend.business.task;

import com.abdecd.novelbackend.business.lib.ReaderHistorySaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(ReaderHistorySaver.class)
public class ReaderHistorySave {
    @Autowired
    ReaderHistorySaver readerHistorySaver;

    @Scheduled(cron = "0/5 * * * * ?")
    public void save() {
        readerHistorySaver.saveReaderHistory();
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void clear() {
        readerHistorySaver.clearOldReaderHistory();
    }
}
