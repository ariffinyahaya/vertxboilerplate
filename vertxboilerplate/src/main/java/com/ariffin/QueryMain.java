package com.ariffin;

import com.ariffin.spring.VxSpringVerticleFactory;
import com.ariffin.config.QueryConfiguration;
import com.ariffin.core.VxMain;
import com.ariffin.verticle.QueryDbVerticle;
import com.ariffin.verticle.QueryHttpVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan("com.ariffin.*")
@SpringBootApplication
public class QueryMain extends VxMain {
    @Autowired
    private ApplicationContext springApplicationContext;

    @Autowired
    private QueryConfiguration queryApplicationConfiguration;

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
        SpringApplication.run(QueryMain.class, args);
    }

    @PostConstruct
    public void deployverticle() {
//        ClusterManager clusterManager = new HazelcastClusterManager();

        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setPreferNativeTransport(true)
                .setWorkerPoolSize(queryApplicationConfiguration.vertxWorkerPoolSize());

//        vertxOptions.setHAEnabled(true)
//                .setClusterManager(clusterManager);

//        Vertx.clusteredVertx(vertxOptions, res -> {
//            if (res.succeeded()) {
//                LOGGER.info("clusteredVertx SUCCESS");
//                vertx = res.result();

        vertx = Vertx.vertx();
        eventBus = vertx.eventBus();

        int processorCounts = (Runtime.getRuntime().availableProcessors());
//        Context vertxEventLoopContext = vertx.getOrCreateContext();

        VerticleFactory verticleFactory = springApplicationContext.getBean(VxSpringVerticleFactory.class);

        // The verticle factory is registered manually because it is created by the Spring container
        vertx.registerVerticleFactory(verticleFactory);

        // Setting up global config
        ConfigStoreOptions ebConfigStore = new ConfigStoreOptions()
                .setType("event-bus")
                .setConfig(new JsonObject()
                        .put("address", "address-getting-the-conf")
                );
        ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions().addStore(ebConfigStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, configRetrieverOptions);

        // YOU CAN DO A PARAM CHECK TO RUN A SPECIFIC VERTICLE SO THAT ONLY
        // CERTAIN MACHINES RUN SPECIFIC VERTICLES.


        // Scale the api server verticles on cores: create cpu # of instances during the deployment
        LOGGER.info("Deploying API...");
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(processorCounts).setHa(true);
        vertx.deployVerticle(verticleFactory.prefix() + ":" + QueryHttpVerticle.class.getName(), deploymentOptions, results -> {
            if (results.succeeded()) {
                LOGGER.info("API Deployment id is: " + results.result());
            } else {
                LOGGER.error("API Deployment failed! " + results.toString());
                System.exit(1);
            }
        });

        LOGGER.info("Deploying DB...");
        deploymentOptions = new DeploymentOptions()
                .setInstances(processorCounts).setHa(true);
        vertx.deployVerticle(verticleFactory.prefix() + ":" + QueryDbVerticle.class.getName(), deploymentOptions, results -> {
            if (results.succeeded()) {
                LOGGER.info("DB Deployment id is: " + results.result());
            } else {
                LOGGER.error("DB Deployment failed! " + results.toString());
                System.exit(1);
            }
        });
    }
}

/*

//                // TODO: We should have some flag to indicate that all verticles have been deployed successfully
//            } else {
//                // failed!
//                LOGGER.error("clusteredVertx FAILED");
//                System.exit(1);
//            }
//    });
    }
}
*/
