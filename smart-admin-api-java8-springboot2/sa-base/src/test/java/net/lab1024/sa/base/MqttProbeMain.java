package net.lab1024.sa.base;

import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

import java.util.concurrent.CountDownLatch;

/**
 * 临时验证 — MQTT 5.0 CONNECT username 解码探针（验证完删除）。
 */
public class MqttProbeMain {

    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Vertx vertx = Vertx.vertx();
        MqttServer server = MqttServer.create(vertx, new MqttServerOptions().setPort(18831));
        server.endpointHandler(endpoint -> {
            System.out.println("[PROBE] protocolVersion=" + endpoint.protocolVersion()
                    + " clientId=" + endpoint.clientIdentifier());
            System.out.println("[PROBE] username=[" + endpoint.auth().getUsername() + "]");
            System.out.println("[PROBE] password=[" + endpoint.auth().getPassword() + "]");
            endpoint.accept(false);
        });
        server.listen(ar -> {
            System.out.println("[PROBE] server listen " + (ar.succeeded() ? "ok" : "fail " + ar.cause()));
            if (ar.failed()) {
                ar.cause().printStackTrace();
            }
            latch.countDown();
        });
        latch.await();
    }
}
