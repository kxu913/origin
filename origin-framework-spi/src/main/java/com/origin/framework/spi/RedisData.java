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
    default String redisKey() {
        return "";
    }

    /**
     * key that will store in redis set structure.
     *
     * @return key
     */
    String key();

    /**
     * determine whether you need cache object.
     *
     * @return true or false
     */
    default boolean needCacheObject() {
        return false;
    }

    /**
     * key of hash type in redis.
     *
     * @return key
     */
    default String redisHashKey() {
        return "";
    }


}
