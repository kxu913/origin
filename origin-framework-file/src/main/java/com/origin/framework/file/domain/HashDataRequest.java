package com.origin.framework.file.domain;

import io.vertx.redis.client.Response;
import lombok.Data;

@Data
public class HashDataRequest {
    private Response response;
    private ResultReport resultReport;
//    private Buffer buffer;

//    public HashDataRequest(Response response, Buffer buffer) {
//
//        this.response = response;
//        this.buffer = buffer;
//    }

    public HashDataRequest(Response response) {
        this.response = response;
    }


    public HashDataRequest withResultReport(ResultReport resultReport) {
        this.resultReport = resultReport;
        return this;
    }

}
