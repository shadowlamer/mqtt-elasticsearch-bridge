package ru.anhot.mqtt.mqtt_elastic.generators;

import org.json.JSONException;

public class ValueGenerator implements Generator {
    @Override
    public boolean matches(String s) {
        return true;
    }

    @Override
    public Object getValue(String topic, String from, Object value) {
        return value;
    }

}
