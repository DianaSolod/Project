package com.example.demo;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MyApp {

    public static void main(String[] args) throws InterruptedException, MqttException {
        String publisherId = UUID.randomUUID().toString();
        IMqttClient publisher = new MqttClient("tcp://broker.emqx.io:1883", publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options);
        Thread.sleep(1000);

        EngineTemperatureSensor tempSensor = new EngineTemperatureSensor(publisher);

        Timer timer = new Timer();

        // create a timer task object that publishes and sends a random temperature value
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    tempSensor.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 0, 5000);
    }
}