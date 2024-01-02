package com.origin.framework.spi;

/**
 * SPI that used to save origin data to elastic search.
 *
 * @Auther Kevin Xu
 * @Date 2023/12/24
 */
public interface ESData extends OriginData {
    /**
     * index of elastic search.
     *
     * @return key
     */
    String esIndex();

    /**
     * id of index.
     *
     * @return id
     */
    String getId();

    /**
     * batch size when insert origin data to es.
     *
     * @return batch size
     */
    default int esBatchSize() {
        return 1000;
    }


}
