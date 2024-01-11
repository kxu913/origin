package com.origin.framework.file.domain.response;

import com.origin.framework.file.domain.result.ResultReport;
import io.vertx.redis.client.Response;
import lombok.Data;

@Data
public class HashDataResponse {
    private Response response;
    private ResultReport resultReport;

    public HashDataResponse(Response response) {
        this.response = response;
    }


    public HashDataResponse withResultReport(ResultReport resultReport) {
        this.resultReport = resultReport;
        return this;
    }

}
