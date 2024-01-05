package com.origin.framework.file.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.vertx.core.json.JsonObject;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ResultReport {
    public JsonObject additionalInfo;
    private AtomicInteger totalSize;
    private AtomicInteger loadedSize;
    private AtomicInteger filteredSize;
    private AtomicInteger errorSize;
    private AtomicInteger fileIndex;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private long loadedCostTime;

    /**
     * The method is used to initialize the state of a ResultReport object, including the total count, loaded count, filtered count, error count, and record the start time. It returns the initialized ResultReport object.
     *
     * @return ResultReport
     */
    public ResultReport start() {
        this.totalSize = new AtomicInteger(0);
        this.loadedSize = new AtomicInteger(0);
        this.filteredSize = new AtomicInteger(0);
        this.errorSize = new AtomicInteger(0);
        this.fileIndex = new AtomicInteger(0);
        this.startTime = LocalDateTime.now();
        this.additionalInfo = new JsonObject();
        return this;

    }

    /**
     * This method ends the result report and records the end time. It calculates the duration and returns the result report object.
     *
     * @return the result report object
     */
    public ResultReport end() {
        this.endTime = LocalDateTime.now();
        Duration duration = Duration.between(this.startTime, this.endTime);
        this.loadedCostTime = duration.getSeconds();
        return this;
    }


    public ResultReport additionalInfo(String key, String value) {
        this.additionalInfo.put(key, value);
        return this;
    }

}
