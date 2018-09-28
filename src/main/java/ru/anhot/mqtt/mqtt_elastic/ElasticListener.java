package ru.anhot.mqtt.mqtt_elastic;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;

public class ElasticListener implements ActionListener<BulkResponse> {
    @Override
    public void onResponse(BulkResponse indexResponse) {
        //TODO: log something
    }

    @Override
    public void onFailure(Exception e) {
        //TODO: log something
    }
}
