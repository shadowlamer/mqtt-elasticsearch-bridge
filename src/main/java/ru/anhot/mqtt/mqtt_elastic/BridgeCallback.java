package ru.anhot.mqtt.mqtt_elastic;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.List;

public class BridgeCallback implements MqttCallback {

    public static final int BULK_SIZE = 100;

    private Client elasticClient;
    private List<MessageMapper> mappers;
    private BulkProcessor bulkProcessor;

    public BridgeCallback(Client elasticClient, List<MessageMapper> mappers, BulkProcessor bulkProcessor) {
        this.elasticClient = elasticClient;
        this.mappers = mappers;
        this.bulkProcessor = bulkProcessor;
    }

    public void connectionLost(Throwable cause) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        for (MessageMapper mapper:mappers)
            mapper.map(topic,message.toString()).ifPresent(payload->{
                bulkProcessor.add(elasticClient.prepareIndex(payload.getIndex(), payload.getType(), payload.getId())
                        .setSource(payload.getSource(), XContentType.JSON).request());
            });
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
