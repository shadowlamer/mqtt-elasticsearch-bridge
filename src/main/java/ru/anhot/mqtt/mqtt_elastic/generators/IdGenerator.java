package ru.anhot.mqtt.mqtt_elastic.generators;

import ru.anhot.mqtt.mqtt_elastic.MqttElasticApp;

public class IdGenerator implements Generator {
    public static final String GENERATOR_ID = "@@id";

    @Override
    public boolean matches(String s) {
        return GENERATOR_ID.equals(s);
    }

    @Override
    public Object getValue(String topic, String from, Object value) {
        return MqttElasticApp.getLongId();
    }

}
