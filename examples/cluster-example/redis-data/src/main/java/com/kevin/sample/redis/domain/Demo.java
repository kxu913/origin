package com.kevin.sample.redis.domain;

import com.origin.framework.spi.RedisData;
import lombok.Data;

import java.util.Date;

@Data
public class Demo implements RedisData {


    private long id;
    private Date date;


    @Override
    public String queueName() {
        return "demo";
    }

    @Override
    public String key() {
        return "id";
    }

    @Override
    public String redisKey() {
        return "demo";
    }

    @Override
    public String redisHashKey() {
        return "demo-hash";
    }

    @Override
    public boolean needCacheObject() {
        return true;
    }
}
