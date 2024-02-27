package com.origin.framework.file.domain.request;

import lombok.Data;

@Data
public class CombineRequest {
    private String leftFile;
    private String rightFile;
    private int leftJoinKeyIndex;
    private int rightJoinKeyIndex;
    private int[] leftJoinFields;
    private int[] rightJoinFields;

    public CombineRequest(String leftFile, String rightFile) {
        this.leftFile = leftFile;
        this.rightFile = rightFile;
        this.leftJoinKeyIndex = 0;
        this.rightJoinKeyIndex = 0;
        this.leftJoinFields = new int[]{1};
        this.rightJoinFields = new int[]{1};
    }

    public CombineRequest() {
        this.leftJoinKeyIndex = 0;
        this.rightJoinKeyIndex = 0;
        this.leftJoinFields = new int[]{1};
        this.rightJoinFields = new int[]{1};
    }

    public CombineRequest leftJoin(int leftJoinKeyIndex) {
        this.leftJoinKeyIndex = leftJoinKeyIndex;
        return this;
    }

    public CombineRequest rightJoin(int rightJoinKeyIndex) {
        this.rightJoinKeyIndex = rightJoinKeyIndex;
        return this;
    }

    public CombineRequest leftColumns(int... leftJoinFields) {
        this.leftJoinFields = leftJoinFields;
        return this;
    }

    public CombineRequest rightColumns(int... rightJoinFields) {
        this.rightJoinFields = rightJoinFields;
        return this;
    }

}
