package ru.anhot.mqtt.mqtt_elastic;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.List;

public class BridgeCallback implements MqttCallback {

    public static final int BULK_SIZE = 100;

    private Client elasticClient;
    private List<MessageMapper> mappers;
    private ActionListener<BulkResponse> listener;
    private BulkRequestBuilder bulkRequest;

    public BridgeCallback(Client elasticClient, List<MessageMapper> mappers) {
        this.elasticClient = elasticClient;
        this.mappers = mappers;
        this.listener = new ElasticListener();
        bulkRequest = elasticClient.prepareBulk();
    }

    public void connectionLost(Throwable cause) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        for (MessageMapper mapper:mappers)
            mapper.map(topic,message.toString()).ifPresent(payload->{
                bulkRequest.add(elasticClient.prepareIndex(payload.getIndex(), payload.getType(), payload.getId())
                        .setSource(payload.getSource(), XContentType.JSON));
            });
        if (bulkRequest.numberOfActions() > BULK_SIZE)
            bulkRequest.execute(listener);
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
