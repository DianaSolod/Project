package com.example.demo;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class EngineTemperatureSensor {

    public static final String TOPIC = "/temperature";
    private static final Logger log = LoggerFactory.getLogger(EngineTemperatureSensor.class);
    private final IMqttClient client;
    private final Random rnd = new Random();

    public EngineTemperatureSensor(IMqttClient client) {
        this.client = client;
    }

    public Void call() throws Exception {

        if (!client.isConnected()) {
            log.info("Client not connected.");
            return null;
        }

        MqttMessage msg = readEngineTemp();
        msg.setQos(0);
        msg.setRetained(true);
        client.publish(TOPIC, msg);

        return null;
    }

    private MqttMessage readEngineTemp() {
        double temp = 20 + rnd.nextDouble() * 10.0;
        byte[] payload = String.valueOf(temp).getBytes();

        MqttMessage msg = new MqttMessage(payload);
        System.out.println("temperature " + temp);
        return msg;
    }
}