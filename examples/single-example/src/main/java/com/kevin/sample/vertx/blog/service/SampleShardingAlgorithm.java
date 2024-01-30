package com.kevin.sample.vertx.blog.service;

import com.origin.framework.spi.RedisShardingAlgorithm;

public class SampleShardingAlgorithm implements RedisShardingAlgorithm {
    @Override
    public String lookFor(String key) {
        int index = Integer.parseInt(key);
        if (index % 2 == 1) {
            return "redis-v1";
        }
        return "redis-v2";
    }

    @Override
    public int optimizeThreshold() {
        return 3;
    }
}
