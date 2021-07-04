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

import com.ariffin.config.QueryConfiguration;
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
    protected QueryConfiguration queryConfiguration;

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
                .setMaxWebSocketFrameSize(queryConfiguration.serverMaxWebsocketFramesize())
                .setLogActivity(queryConfiguration.serverLogActivity())
                .setDecompressionSupported(queryConfiguration.serverSetDecompressionSupported())
                // native netty options
                .setTcpFastOpen(queryConfiguration.nettySetTcpFastOpen())
                .setTcpCork(queryConfiguration.nettySetTcpCork())
                .setTcpQuickAck(queryConfiguration.nettySetTcpQuickAck())
                .setReusePort(queryConfiguration.nettySetReusePort());

        Promise<Router> routerPromise = Promise.promise();
        Future<Router> routerFuture = routerPromise.future();
        router(routerPromise);
        routerFuture.onComplete(routerResult -> {
            if (routerResult.succeeded()) {
                server = vertx.createHttpServer(httpServerOptions).requestHandler(routerResult.result()).listen(queryConfiguration.httpPort(), results -> {
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
