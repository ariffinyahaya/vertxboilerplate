package com.ariffin.verticle;

import com.ariffin.config.QueryConfiguration;
import com.ariffin.core.VxAbstractVerticle;
import com.ariffin.core.VxSqliteSingleton;
import com.ariffin.enums.EventBusDatabaseMessages;
import com.ariffin.enums.MessageHeaderKeyEnum;
import com.ariffin.enums.MessageHeaderStatusValueEnum;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
// Prototype scope is needed as multiple instances of this verticle will be deployed
@Scope(SCOPE_PROTOTYPE)

// use Cassandra SHARED CLIENT!!!!

public class QueryDbVerticle extends VxAbstractVerticle {
    // Because we may have multiple instances, use SEND and not publish !!!!

    VxSqliteSingleton myDB = null;

    // Listen for instructions
    protected MessageConsumer<Object> dbQueryOneMessage = null;
    protected MessageConsumer<Object> dbConfigMessage = null;

    @Autowired
    private QueryConfiguration queryConfiguration;

    private EventBusDatabaseMessages getDbQueryMessage(Message<Object> message) {
        return EventBusDatabaseMessages.valueOfLabel(message.address());
    }

    // setupEbListen lets its caller know whether it has failed or succeeded
    private Future<Void> setupEbListen(MessageConsumer<Object> msg, String listenString) {
        Promise<Void> ebListenPromise = Promise.promise();

        msg = eventBus.consumer(listenString);
        msg.handler(objectMessage -> queryEventHandler(objectMessage));
        msg.completionHandler(res -> {
            if (res.succeeded()) {
                LOGGER.info("The " + listenString + " handler registration has reached all nodes");
                ebListenPromise.complete();
            } else {
                LOGGER.error("Registration failed for " + listenString);
                ebListenPromise.fail(res.cause());
            }
        });

        return ebListenPromise.future();
    }

    private void queryEventHandler(Message<Object> message) {
        LOGGER.info("I have received a message: " + message.body() + " addressed to " + message.address());

        DeliveryOptions options = new DeliveryOptions();

        switch (getDbQueryMessage(message)) {
            // check that the db is not null
            case DBQuery1:
                JsonObject getQuestionParamJson = new JsonObject(message.body().toString());
                Future<RowSet<Row>> dbQuery1Future = null;

                // Validate message parameters & call DB
                if (getQuestionParamJson.fieldNames().contains("xxx")) {
                    // call query
                    dbQuery1Future = myDB.query("select * from table1;");
                } else {
                    options.addHeader(MessageHeaderKeyEnum.STATUS.toString(), MessageHeaderStatusValueEnum.ERROR.toString());
                    options.addHeader(MessageHeaderKeyEnum.CAUSE.toString(),"bad parameters");
                    message.reply("",options);
                }

                // set the future handlers
                if (null != dbQuery1Future)
                    dbQuery1Future.onSuccess(m -> {
                        options.addHeader(MessageHeaderKeyEnum.STATUS.toString(), MessageHeaderStatusValueEnum.SUCCESS.toString());
                        JsonObject t = new JsonObject();
                        for (Row r : m)
                            t.put(r.getString(1),r.getString(2));
                        message.reply(t.encode(),options);
                    }).onFailure(e -> {
                        options.addHeader(MessageHeaderKeyEnum.STATUS.toString(), MessageHeaderStatusValueEnum.ERROR.toString());
                        options.addHeader(MessageHeaderKeyEnum.CAUSE.toString(), e.getCause().toString());
                        message.reply("",options);
                    });
                break;
//            case DBQuery2:
//
//                break;
//            case DBQuery3:
//
//                break;
            case DBConfig:
                // do some DB config
                break;
            default:
                LOGGER.error("Unknown Question EventBus message " + message.toString());
                break;
        }
    }

    @Override
    // Called when verticle is deployed
    public void start(Promise<Void> startPromise) throws Exception {
        // The purpose of startPromise is to tell it's caller whether the function has been executed
        super.start();

        eventBus = vertx.eventBus();

        myDB = VxSqliteSingleton.getInstance(vertx, queryConfiguration.sqliteStaticDbName(), queryConfiguration.sqliteStaticDbPoolSize());


        // open cassandra shared client here

        // Listen for instructions
        setupEbListen(dbQueryOneMessage, EventBusDatabaseMessages.DBQuery1.toString()) // These functions are run sequentially
                .compose(e -> setupEbListen(dbConfigMessage, EventBusDatabaseMessages.DBConfig.toString())) // If the first function fails the second, third and fourth function will not run
                .onComplete(startPromise); // Required, otherwise else vertx.deployVerticle() will never know if deployment has succeeded
    }
}

