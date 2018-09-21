package ru.anhot.mqtt.mqtt_elastic;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.HashMap;
import java.util.List;

public class BridgeCallback implements MqttCallback {

    Client elasticClient;
    List<MessageMapper> mappers;

    public BridgeCallback(Client elasticClient, List<MessageMapper> mappers) {
        this.elasticClient = elasticClient;
        this.mappers = mappers;
    }

    public void connectionLost(Throwable cause) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        for (MessageMapper mapper:mappers)
            mapper.map(topic,message.toString()).ifPresent(payload->{
                IndexResponse response = elasticClient.prepareIndex(payload.getIndex(), payload.getType(), payload.getId())
                        .setSource(payload.getSource(), XContentType.JSON).get();
            });
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
