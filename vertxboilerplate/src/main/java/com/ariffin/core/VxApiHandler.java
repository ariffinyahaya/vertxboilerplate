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
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.circuitbreaker.CircuitBreakerState;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class VxApiHandler implements Handler<RoutingContext> {
    protected EventBus eventBus;

    // TODO: use fallback for circuitbreaker
    private static final JsonArray FALLBACK = new JsonArray();

    protected static Logger LOGGER = null;
    protected CircuitBreaker circuitBreaker = null;
    protected HealthChecks healthChecks = null;
    protected Vertx vertx = null;

    public VxApiHandler(Vertx vertx) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
        String myClassName = this.getClass().getName();

        LOGGER = LogManager.getLogger(this.getClass());

        // TODO: get these specs from config
        /*
            server.circuitbreaker.setmaxfailures=3
            server.circuitbreaker.settimeout=1000
            server.circuitbreaker.setfallbackonfailure=true
            server.circuitbreaker.setresettimeout=6000
         */
        CircuitBreakerOptions circuitBreakerOptions = new CircuitBreakerOptions()
                .setMaxFailures(3)
                .setTimeout(1000)
                .setFallbackOnFailure(true)
                .setResetTimeout(6000);

        // TODO: Add more circuit breaker functionality
        this.circuitBreaker = CircuitBreaker.create(myClassName, vertx, circuitBreakerOptions);
        this.circuitBreaker.openHandler(v -> LOGGER.info("{} circuit breaker is OPEN", myClassName));
        this.circuitBreaker.closeHandler(v -> LOGGER.info("{} circuit breaker is CLOSED", myClassName));
        this.circuitBreaker.halfOpenHandler(v -> LOGGER.info("{} circuit breaker is HALF Open", myClassName));

        // TODO: add more healthcheck functionality
        this.healthChecks = HealthChecks.create(vertx);

        healthChecks.register(myClassName,1000, future -> {
            if (circuitBreaker.state().equals(CircuitBreakerState.CLOSED)) {
                future.complete(Status.OK());
            } else {
                future.complete(Status.KO());
            }
        });
    }

    @Override
    public abstract void handle(RoutingContext routingContext);
}
