package com.ariffin.query;

import com.ariffin.core.VxSqlite;
import com.ariffin.enums.ConditionsEnum;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class QueryEngineSingleton {
    private static QueryEngineSingleton instance = null;
    protected static Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass().getName());
    protected static String dbName;
    private static VxSqlite sqliteDb = null;

    public static QueryEngineSingleton getInstance(Vertx vertx, String sqliteDbName, int poolSize) {
        // used when instantiating from test
        if (instance != null)
            LOGGER.warn("Redefining instance DISALLOWED for " + MethodHandles.lookup().lookupClass().getName());
        else
            instance = new QueryEngineSingleton(vertx, sqliteDbName, poolSize);
        return instance;
    }

    private QueryEngineSingleton(Vertx vertx, String sqliteDbName, int poolSize)  {
        LOGGER = LogManager.getLogger(this.getClass().getName());
        sqliteDb = new VxSqlite(vertx, sqliteDbName,poolSize);
    }

    // get questions from QuestionReferenceNumber
    public Future<Map<Integer,String>> getSomething(String q) {
        Promise<Map<Integer,String>> retPromise = Promise.promise();

        Future<RowSet<Row>> futRow = sqliteDb.query(q);
        futRow.onSuccess(rows -> {
            LinkedHashMap<Integer,String> ret = new LinkedHashMap<>();

            for (Row row : rows)
                ret.put(row.getInteger("reference"),row.getString("something"));

            retPromise.complete(ret);

        }).onFailure(e -> {
            // handle the failure
            LOGGER.error("getSomething Failure: " + e.getCause().getMessage() + " on db : " + QueryEngineSingleton.dbName + " from sql: " + q);
            retPromise.fail(e.getCause());
        });
        return retPromise.future();
    }

    public Future<Map<Integer,String>> getManySomethings(List<String> myParams) {
        Promise<Map<Integer,String>> retPromise = Promise.promise();

        Map<Integer,String> retMap = new HashMap<>();

        @SuppressWarnings("rawtypes")
        List<Future> futureList = new ArrayList<>();

        for (String s: myParams) {
            futureList.add(getSomething(s));
        }

        CompositeFuture.all(futureList).onSuccess(compositeResult -> {
            int count = 0;

            for (@SuppressWarnings({"rawtypes"}) Future future : futureList) {
                @SuppressWarnings({"unchecked"})
                Map<Integer,String> map = (Map<Integer,String>) future.result();

                // process map here
                for (Integer ref : map.keySet())
                    // create retmap
                    retMap.put(Integer.valueOf(count++),map.get(ref));

            }

            retPromise.complete(retMap);

        });
        return retPromise.future();
    }
}
