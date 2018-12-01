package ru.anhot.mqtt.mqtt_elastic.generators;

public interface Generator {
    boolean matches(String s);
    Object getValue(String topic, String from, Object value);
}
