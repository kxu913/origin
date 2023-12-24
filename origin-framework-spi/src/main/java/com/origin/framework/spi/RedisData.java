package com.origin.framework.spi;

/**
 * SPI that used to save origin data to redis as set type or hash type.
 *
 * @Auther Kevin Xu
 * @Date 2023/12/24
 */
public interface RedisData extends OriginData {
    /**
     * key of set type in redis.
     *
     * @return key
     */
    default public String redisKey() {
        return "";
    }

    /**
     * key of hash type in redis.
     *
     * @return key
     */
    default public String redisHashKey() {
        return "";
    }
}
