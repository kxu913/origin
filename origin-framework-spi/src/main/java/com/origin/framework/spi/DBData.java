package com.origin.framework.spi;

import io.vertx.sqlclient.Tuple;

/**
 * SPI that used to save origin data to database using below methods.
 *
 * @Auther Kevin Xu
 * @Date 2023/12/24
 */
public interface DBData extends OriginData {

    default String dbName() {
        return "db";
    }

    /**
     * SQL that used to insert data to db
     *
     * @return sql
     */
    String insertSQL();

    /**
     * SQL that used to rollback data from db
     *
     * @return sql
     */
    default String rollbackSQL() {
        return "";
    }

    /**
     * convert java object to Tuple.
     *
     * @return tuple.
     */
    Tuple toTuple();

    /**
     * batch size when insert origin data to db.
     *
     * @return batch size
     */
    default int batchSize() {
        return 500;
    }
}
