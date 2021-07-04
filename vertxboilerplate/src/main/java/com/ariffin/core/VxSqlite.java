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
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

// Assumption: sqlite files are always in resource

public class VxSqlite {
    protected Vertx vertx = null;
    protected Logger LOGGER = LogManager.getLogger(this.getClass().getName());
    boolean poolOpen = false;
    String dbName = "none";
    JDBCPool pool = null;

    public VxSqlite(Vertx vertx, String sqliteDbName, int poolSize)  {
        LOGGER = LogManager.getLogger(this.getClass().getName());
        this.vertx = vertx;

        // get path
        File f = new File("");
        try {
            dbName = f.getCanonicalPath().toString()+sqliteDbName;
        } catch (IOException e) {
            LOGGER.error(e);
            exit(1);
        }

        // check file
        f = new File(dbName.trim());
        if(!f.isFile()) {
            LOGGER.error("sqlite file '"+dbName+"'is not a file");
            try {
                LOGGER.error(f.getCanonicalPath().toString());
            } catch (IOException e) {
                LOGGER.error(e);
                e.printStackTrace();
            }
            exit(1);
        } else {

            // start db stuff here
            this.dbName = "jdbc:sqlite:" + dbName;
            this.pool = JDBCPool.pool(
                    this.vertx,
                    // configure the connection
                    new JDBCConnectOptions()
                            // H2 connection string
                            .setJdbcUrl(dbName),
                    // configure the pool
                    new PoolOptions()
                            .setMaxSize(poolSize)
            );

            this.poolOpen = true;

        }
    }

    public JDBCPool getPool() {
        if (this.poolOpen)
            return this.pool;
        else
            return null;
    }

    public void close() {
        this.pool.close();
        this.poolOpen = false;
    }

    public Future<RowSet<Row>> query(String sql) {
        Promise<RowSet<Row>> retPromise = Promise.promise();

        if (this.poolOpen) {
            this.pool
                    .query(sql)
                    .execute()
                    .onFailure(e -> {
                        // handle the failure
                        LOGGER.error("Failure: " + e.getCause().getMessage() + " on db : " + this.dbName + " from sql: " + sql);
                        retPromise.fail(e.getCause());
                    })
                    .onSuccess(rows -> {
                        LOGGER.info("Success: Got " + rows.size() + " rows " + rows.toString() + " on db : " + this.dbName + " from sql: " + sql);
                        retPromise.complete(rows);
                    });
        } else {
            String e = "Failure: sqlite db NOT open on db : " + this.dbName + " from sql: " + sql;
            LOGGER.error(e);
            retPromise.fail(e);
        }
        return retPromise.future();
    }
}