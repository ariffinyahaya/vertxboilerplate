package com.ariffin.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class QueryConfiguration {

    @Autowired
    private Environment environment;

    public String applicationName() {
        return environment.getProperty("spring.application.name");
    }

    public int vertxWorkerPoolSize() { return environment.getProperty("vertx.worker.poolsize", Integer.class); }

    public int httpPort() {
        return environment.getProperty("server.port", Integer.class);
    }

    public String serverSessionMapName() { return environment.getProperty("server.session.map",String.class); }

    public int serverMaxWebsocketFramesize() { return environment.getProperty("server.maxwebsocketframesize",Integer.class); }

    public boolean serverLogActivity() { return  environment.getProperty("server.logactivity",Boolean.class); }

    public boolean serverSetDecompressionSupported() { return environment.getProperty("server.setdecompressionsupported",Boolean.class); }
    // Netty
    public boolean nettySetTcpFastOpen() { return environment.getProperty("server.netty.settcpfastopen",Boolean.class); }
    public boolean nettySetTcpCork() { return environment.getProperty("server.netty.settcpcork", Boolean.class); }
    public boolean nettySetTcpQuickAck() { return environment.getProperty("server.netty.settcpquickack",Boolean.class); }
    public boolean nettySetReusePort() { return environment.getProperty("server.netty.setreuseport",Boolean.class); }
    // static db (sqlite)
    public String sqliteStaticDbName() { return environment.getProperty("sqliteStatic.db.name",String.class);}
    public int sqliteStaticDbPoolSize() { return environment.getProperty("sqliteStatic.db.poolsize",Integer.class);}
}
