package websocket;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@javax.websocket.server.ServerEndpoint(value = "/testing-websockets")
public class ServerEndpoint {
    private static final Set<ServerEndpoint> SERVER_ENDPOINTS = new CopyOnWriteArraySet<>();
    private static final HashMap<String, String> users = new HashMap<>();
    private static boolean IsSensorListenerRunning = false;
    private Session session;

    // starts listening for sensor requests via mqtt
    public ServerEndpoint() throws MqttException, InterruptedException {
        if (IsSensorListenerRunning) {
            return;
        }
        String publisherId = UUID.randomUUID().toString();
        IMqttClient subscriber = new MqttClient("tcp://broker.emqx.io:1883", publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        subscriber.connect(options);

        CountDownLatch receivedSignal = new CountDownLatch(10);
        subscriber.subscribe("/temperature", (topic, msg) -> { //EngineTemperatureSensor.TOPIC
            broadcast(msg.toString());
            receivedSignal.countDown();
        });
        receivedSignal.await(1, TimeUnit.MINUTES);
        IsSensorListenerRunning = true;
    }

    private static void broadcast(String message) throws IOException, EncodeException {
        SERVER_ENDPOINTS.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote()
                            .sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        this.session = session;
        SERVER_ENDPOINTS.add(this);
    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
        SERVER_ENDPOINTS.remove(this);
    }

}

