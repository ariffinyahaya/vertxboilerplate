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

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VxApiFailureHandler implements Handler<RoutingContext> {
    protected static Logger LOGGER = null;
    protected CircuitBreaker circuitBreaker = null;
    protected HealthChecks healthChecks = null;
    protected Vertx vertx = null;
    protected EventBus eventBus = null;
    protected Class<?> myClass = this.getClass();
    protected String myClassName = myClass.getName();

    public VxApiFailureHandler(Vertx vertx) {
        this.vertx = vertx;
        eventBus = vertx.eventBus();
        this.myClass = this.getClass();
        this.myClassName = myClass.getName();
        LOGGER = LogManager.getLogger(myClass);
    }

    public void handle(RoutingContext context) {
        Throwable thrown = context.failure();
        recordError(thrown);
        context.response().setStatusCode(500).end();
    }

    private void recordError(Throwable throwable) {
        // Your logging/tracing/metrics framework here
        LOGGER.error("API Failure: " + myClassName, throwable);
    }
}