package ru.anhot.mqtt.mqtt_elastic.generators;

import java.util.UUID;

public class UuidGenerator implements Generator {
    public static final String GENERATOR_ID = "@@uuid";

    @Override
    public boolean matches(String s) {
        return GENERATOR_ID.equals(s);
    }

    @Override
    public Object getValue(String topic, String from, Object value) {
        return UUID.randomUUID().toString();
    }
}
