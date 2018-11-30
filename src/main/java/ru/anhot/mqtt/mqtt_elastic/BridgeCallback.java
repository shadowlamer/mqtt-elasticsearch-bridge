package ru.anhot.mqtt.mqtt_elastic;

import org.eclipse.paho.client.mqttv3.*;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.List;

public class BridgeCallback implements MqttCallbackExtended {

    public static final int BULK_SIZE = 100;

    private Client elasticClient;
    private List<MessageMapper> mappers;
    private BulkProcessor bulkProcessor;
    private MqttClient mqttClient;


    public BridgeCallback(MqttClient mqttClient, Client elasticClient, List<MessageMapper> mappers, BulkProcessor bulkProcessor) {
        this.mqttClient = mqttClient;
        this.elasticClient = elasticClient;
        this.mappers = mappers;
        this.bulkProcessor = bulkProcessor;
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Disconnected because of " + cause.getMessage());
        cause.printStackTrace();
        try {
            mqttClient.connect();
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(5);
            return;
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        for (MessageMapper mapper:mappers)
            mapper.map(topic,message.toString()).ifPresent(payload->{
                bulkProcessor.add(elasticClient.prepareIndex(payload.getIndex(), payload.getType(), payload.getId())
                        .setSource(payload.getSource(), XContentType.JSON).request());
            });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        try {
            mqttClient.subscribe("#");
            System.out.println("Connected to MQTT broker at " + serverURI);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(6);
            return;
        }
    }
}
