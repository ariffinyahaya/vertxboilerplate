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
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class VxAbstractVerticle extends AbstractVerticle {
    protected final Logger LOGGER = LogManager.getLogger(this.getClass());
    protected EventBus eventBus = null;

    @Autowired
    protected QueryConfiguration boilerplateApplicationConfiguration;

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
