package com.origin.framework.file.domain.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class CombineResponse {
    private String key;
    private List<String> columns;

    public CombineResponse() {
        this.columns = new ArrayList<>();
    }

    public CombineResponse withKey(String key) {
        this.key = key;
        return this;
    }

    public CombineResponse addColumn(String column) {
        this.columns.add(column);
        return this;
    }

    public CombineResponse addColumns(String... column) {
        this.columns.addAll(Arrays.stream(column).toList());
        return this;
    }


}
