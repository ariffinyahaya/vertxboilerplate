package com.ariffin.verticle;

import com.ariffin.config.QueryConfiguration;
import com.ariffin.core.VxAbstractVerticle;
import com.ariffin.enums.MessageHeaderKeyEnum;
import com.ariffin.enums.MessageHeaderStatusValueEnum;
import com.ariffin.enums.EventBusQueryMessages;
import com.ariffin.query.QueryEngineSingleton;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Map;


import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
// Prototype scope is needed as multiple instances of this verticle will be deployed
@Scope(SCOPE_PROTOTYPE)

public class QueryVerticle extends VxAbstractVerticle {
    // Because we may have multiple instances, use SEND and not publish !!!!

    // Listen for instructions
    protected MessageConsumer<Object> queryOneMessage = null;
    protected MessageConsumer<Object> configMessage = null;

    @Autowired
    private QueryConfiguration queryConfiguration;

    private QueryEngineSingleton queryEngineSingleton = null;

    private EventBusQueryMessages getQueryMessage(Message<Object> message) {
        return EventBusQueryMessages.valueOfLabel(message.address());
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

        switch (getQueryMessage(message)) {
            // check that the db is not null
            case Query1:
                JsonObject getSomethingParamJson = new JsonObject(message.body().toString());
                Future<Map<Integer,String>> queryFuture = null;
                if (getSomethingParamJson.fieldNames().contains("myParam"))
                    queryFuture = queryEngineSingleton.getSomething(getSomethingParamJson.getString("myParam"));
                else {
                    options.addHeader(MessageHeaderKeyEnum.STATUS.toString(), MessageHeaderStatusValueEnum.ERROR.toString());
                    options.addHeader(MessageHeaderKeyEnum.CAUSE.toString(),"bad parameters");
                    message.reply("",options);
                }

                if (null != queryFuture)
                    queryFuture.onSuccess(m -> {
                        options.addHeader(MessageHeaderKeyEnum.STATUS.toString(), MessageHeaderStatusValueEnum.SUCCESS.toString());
                        JsonObject t = new JsonObject();
                        for (Integer i : m.keySet())
                            t.put(i.toString(),m.get(i));
                        message.reply(t.encode(),options);
                    }).onFailure(e -> {
                        options.addHeader(MessageHeaderKeyEnum.STATUS.toString(), MessageHeaderStatusValueEnum.ERROR.toString());
                        options.addHeader(MessageHeaderKeyEnum.CAUSE.toString(), e.getCause().toString());
                        message.reply("",options);
                    });
                break;
            case Query2:

                break;
            case Query3:

                break;
            case Config:

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

        queryEngineSingleton = QueryEngineSingleton.getInstance(vertx, queryConfiguration.sqliteStaticDbName(), queryConfiguration.sqliteStaticDbPoolSize());

        // Listen for instructions
        setupEbListen(queryOneMessage, EventBusQueryMessages.Query1.toString()) // These functions are run sequentially
                .compose(e -> setupEbListen(configMessage, EventBusQueryMessages.Config.toString())) // If the first function fails the second, third and fourth function will not run
                .onComplete(startPromise); // Required, otherwise else vertx.deployVerticle() will never know if deployment has succeeded
    }
}

