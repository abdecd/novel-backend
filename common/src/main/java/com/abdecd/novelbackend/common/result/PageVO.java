package com.abdecd.novelbackend.common.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PageVO<T> {
    private Integer total;
    private List<T> records;
}
