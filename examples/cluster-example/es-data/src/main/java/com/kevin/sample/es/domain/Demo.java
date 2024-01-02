package com.kevin.sample.es.domain;

import com.origin.framework.spi.ESData;
import lombok.Data;

import java.util.Date;

@Data
public class Demo implements ESData {


    private long id;
    private Date date;


    @Override
    public String esIndex() {
        return "idx-demo";
    }

    @Override
    public String queueName() {
        return "demo";
    }
}
