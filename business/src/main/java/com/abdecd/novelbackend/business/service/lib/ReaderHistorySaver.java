package com.abdecd.novelbackend.business.service.lib;

import com.abdecd.novelbackend.business.pojo.entity.ReaderHistory;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReaderHistorySaver {
    public static final int SAVE_SIZE = 1000;
    public static final int CLEAR_SIZE = 100_0000;
    private final List<ReaderHistory> readerHistoryList = new ArrayList<>(SAVE_SIZE);

    synchronized public void addReaderHistory(ReaderHistory readerHistory) {
        readerHistoryList.add(readerHistory);
        if (readerHistoryList.size() >= SAVE_SIZE) {
            saveReaderHistory();
        }
    }

    synchronized public void saveReaderHistory() {
        var tmpList = new ArrayList<>(readerHistoryList);
        Db.saveBatch(tmpList);
        readerHistoryList.clear();
    }

    /**
     * 数据量过大时删除3个月前的数据
     */
    public void clearOldReaderHistory() {
        if (Db.count(ReaderHistory.class) < CLEAR_SIZE) return;
        Db.lambdaUpdate(ReaderHistory.class)
                .lt(ReaderHistory::getTimestamp, LocalDateTime.now().minusMonths(3))
                .remove();
    }
}
