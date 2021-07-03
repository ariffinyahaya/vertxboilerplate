package com.ariffin.verticle;

import com.agewell.config.AgewellApplicationConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

// REMINDER: Prototype scope is needed if multiple instances of this verticle will be deployed
public abstract class VxHttpAbstractVerticle extends AbstractVerticle {
    protected final Logger LOGGER = LogManager.getLogger(this.getClass());
    protected EventBus eventBus = null;

    // Server general level health checks
    protected HealthChecks healthChecks;
    // TODO: add healthcheck procedures and endpoints

    protected HttpServer server = null;

    public HttpServer getServer() {
        return server;
    }


    @Autowired
    protected AgewellApplicationConfiguration agewellApplicationConfiguration;

    // Define a clustered session store
    // MAY be used in router but its optional
    // MUST use https if we use sessions
    protected ClusteredSessionStore mainSessionStore;
    protected SessionHandler sessionHandler;

    @Override
    public void start(Promise<Void> startPromise) throws Exception { // Rename startFuture to startPromise
        super.start();

        eventBus  = vertx.eventBus();

        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setMaxWebSocketFrameSize(agewellApplicationConfiguration.serverMaxWebsocketFramesize())
                .setLogActivity(agewellApplicationConfiguration.serverLogActivity())
                .setDecompressionSupported(agewellApplicationConfiguration.serverSetDecompressionSupported())
                // native netty options
                .setTcpFastOpen(agewellApplicationConfiguration.nettySetTcpFastOpen())
                .setTcpCork(agewellApplicationConfiguration.nettySetTcpCork())
                .setTcpQuickAck(agewellApplicationConfiguration.nettySetTcpQuickAck())
                .setReusePort(agewellApplicationConfiguration.nettySetReusePort());

        Promise<Router> routerPromise = Promise.promise();
        Future<Router> routerFuture = routerPromise.future();
        router(routerPromise);
        routerFuture.onComplete(routerResult -> {
            if (routerResult.succeeded()) {
                server = vertx.createHttpServer(httpServerOptions).requestHandler(routerResult.result()).listen(agewellApplicationConfiguration.httpPort(), results -> {
                    if (results.succeeded()) {
                        LOGGER.info("HTTP Server id is: " + results.result());
                        startPromise.complete();
                    } else {
                        LOGGER.error("HTTP Server failed! " + results.toString());
                        startPromise.fail(results.cause());
                    }
                });
            } else {
                LOGGER.info("Failed to create HTTP server router!");
                startPromise.fail("HTTP Server Router Creation Failure");
            }
        });
    }

    @Override
    // Optional - called when verticle is undeployed
    public void stop() {
    }

    protected abstract void router(Promise<Router> routerPromise);
}
