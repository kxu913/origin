package com.origin.framework.spi;

/**
 *
 */
public interface RedisShardingAlgorithm {
    default String getType() {
        return "redis";
    }

    /**
     * Return Redis configuration key according to algorithm key.
     *
     * @param key: Algorithm key.
     * @return Redis configuration key.
     */
    String lookFor(String key);

    /**
     * Return optimize threshold, if total number of keys is greater than threshold, then use scan instead of get all keys.
     *
     * @return Optimize threshold.
     */
    default int optimizeThreshold() {
        return 10000000;
    }

    /**
     * Return batch size, when use scan to get keys, batch size is used.
     *
     * @return Batch size.
     */
    default int batchSize() {
        return 100000;
    }

}
