/*
 * Copyright 2020 ariffin yahaya (ariffin.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ariffin.core;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;


public class VxSqliteSingleton {
    private static VxSqliteSingleton instance = null;
    protected static Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass().getName());
    protected static String dbName;
    private static VxSqlite sqliteDb = null;

    public static VxSqliteSingleton getInstance(Vertx vertx, String sqliteDbName, int poolSize) {
        // used when instantiating from test
        if (instance != null)
            LOGGER.warn("Redefining instance DISALLOWED for " + MethodHandles.lookup().lookupClass().getName());
        else
            instance = new VxSqliteSingleton(vertx, sqliteDbName, poolSize);
        return instance;
    }

    private VxSqliteSingleton(Vertx vertx, String sqliteDbName, int poolSize) {
        LOGGER = LogManager.getLogger(this.getClass().getName());
        sqliteDb = new VxSqlite(vertx, sqliteDbName, poolSize);
    }

    public JDBCPool getPool() {
        if (this.sqliteDb.poolOpen)
            return this.sqliteDb.pool;
        else
            return null;
    }

    public void close() {
        this.sqliteDb.pool.close();
        this.sqliteDb.poolOpen = false;
    }

    public Future<RowSet<Row>> query(String sql) {
        return this.sqliteDb.query(sql);
    }
}