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

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.healthchecks.HealthChecks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class VxMain {
    protected Vertx vertx = null;
    protected EventBus eventBus = null;
    protected HealthChecks healthChecks = null;
    protected Logger LOGGER = LogManager.getLogger(this.getClass());

    protected Promise<Void> deployHelper(Vertx vertx, String name, DeploymentOptions deploymentOptions) {
        final Promise<Void> promise = Promise.promise();
        if (null != deploymentOptions) {
            vertx.deployVerticle(name, deploymentOptions, res -> {
                if (res.failed()) {
                    LOGGER.error("Failed to deploy verticle " + name);
                    promise.fail(res.cause());
                } else {
                    LOGGER.info("Deployed verticle " + name);
                    promise.complete();
                }
            });
        } else {
            vertx.deployVerticle(name, res -> {
                if (res.failed()) {
                    LOGGER.error("Failed to deploy verticle " + name);
                    promise.fail(res.cause());
                } else {
                    LOGGER.info("Deployed verticle " + name);
                    promise.complete();
                }
            });
        }
        return promise;
    }
}
