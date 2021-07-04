package com.ariffin.api;

import com.ariffin.core.VxApiHandler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class QueryApiHandler extends VxApiHandler {

    public QueryApiHandler(Vertx vertx) {
        super(vertx);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        HttpServerRequest request = routingContext.request();
        MultiMap params = request.params();
        MultiMap headers = request.headers();

        response.end(Json.encodePrettily("Hello" + " there.\nPath is " +
                request.path() + "\n Headers are " + headers.toString() + "\nParams are " +
                params.toString()), results -> {
            LOGGER.info(results);
            if (results.succeeded()) {
                LOGGER.info(this.getClass().getName() + " response success: " + results.result());
            } else {
                LOGGER.error(this.getClass().getName() + " response success: " + results.result());
            }
        });
    }

}
