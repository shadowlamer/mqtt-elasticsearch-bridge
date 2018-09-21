package ru.anhot.mqtt.mqtt_elastic;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class TestUtils {
    public static JSONObject loadJsonFromResource(Class clazz, String name) throws IOException {
        InputStream jsonStream = clazz.getClassLoader().getResource(name).openStream();
        String json = new Scanner(jsonStream,"UTF-8").useDelimiter("\\A").next();
        return new JSONObject(json);
    }
}
