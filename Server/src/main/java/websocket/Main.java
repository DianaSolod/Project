package websocket;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Configuration
public class Main {
    public static void main(String[] args) throws MqttException, InterruptedException {
        String publisherId = UUID.randomUUID().toString();
        IMqttClient subscriber = new MqttClient("tcp://broker.emqx.io:1883", publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        subscriber.connect(options);

        CountDownLatch receivedSignal = new CountDownLatch(10);
        subscriber.subscribe("/temperature", (topic, msg) -> {
            byte[] payload = msg.getPayload();
            receivedSignal.countDown();
        });
        receivedSignal.await(1, TimeUnit.MINUTES);
        System.out.println("all done");
    }
}
