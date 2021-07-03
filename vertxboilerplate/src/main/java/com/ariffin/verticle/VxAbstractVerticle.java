package com.ariffin.verticle;

import com.agewell.config.AgewellApplicationConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class VxAbstractVerticle extends AbstractVerticle {
    protected final Logger LOGGER = LogManager.getLogger(this.getClass());
    protected EventBus eventBus = null;

    @Autowired
    protected AgewellApplicationConfiguration agewellApplicationConfiguration;

    @Override
    // Called when verticle is deployed
    public void start(Promise<Void> startPromise) throws Exception { // Rename startFuture to startPromise
        super.start();
        eventBus  = vertx.eventBus();

        // startPromise.complete() is not required here as it is the responsibility of the method of the class that inherits from this one to call startPromise.complete() on verticle initialization
    }

    @Override
    // Optional - called when verticle is undeployed
    public void stop() {
    }

}
