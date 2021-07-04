package com.ariffin.verticle;

import com.ariffin.api.QueryApiHandler;
import com.ariffin.core.VxApiFailureHandler;
import com.ariffin.core.VxHttpAbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


@Component
// Prototype scope is needed as multiple instances of this verticle will be deployed
@Scope(SCOPE_PROTOTYPE)
public class QueryHttpVerticle extends VxHttpAbstractVerticle {

    @Autowired
    private ResourceLoader resourceLoader;
    private Map<String, Handler<RoutingContext>> operationHandlers = new HashMap<>();
    protected void router(Promise<Router> routerPromise) {
        //create a router defining the endpoints of the service
        final Router router = Router.router(vertx);
        LOGGER.info("setting up Router in AgewellHttpVerticle");

        QueryApiHandler queryApiHandler = new QueryApiHandler(vertx);
        VxApiFailureHandler failureHandler = new VxApiFailureHandler(vertx);

        router.get("/info").handler(queryApiHandler).failureHandler(failureHandler);

        routerPromise.complete(router);
    }

}
