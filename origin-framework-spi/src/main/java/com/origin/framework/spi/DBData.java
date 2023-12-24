package com.origin.framework.spi;

import io.vertx.sqlclient.Tuple;

/**
 * SPI that used to save origin data to database using below methods.
 *
 * @Auther Kevin Xu
 * @Date 2023/12/24
 */
public interface DBData extends OriginData {
    /**
     * SQL that used to insert data to db
     *
     * @return sql
     */
    public String insertSQL();

    /**
     * convert java object to Tuple.
     *
     * @return tuple.
     */
    public Tuple toTuple();

    /**
     * batch size when insert origin data to db.
     *
     * @return
     */
    default public int batchSize() {
        return 1000;
    }
}
