package ru.anhot.mqtt.mqtt_elastic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageMapperFactory {

    public static final String CONFIG_ELEMENT_TOPIC   = "topic";
    public static final String CONFIG_ELEMENT_INDEX   = "index";
    public static final String CONFIG_ELEMENT_TYPE    = "type";
    public static final String CONFIG_ELEMENT_FIELDS  = "fields";
    public static final String CONFIG_ELEMENT_FROM    = "from";
    public static final String CONFIG_ELEMENT_TO      = "to";
    public static final String CONFIG_ELEMENT_PROP    = "property";
    public static final String CONFIG_ELEMENT_MAPPERS = "mappers";

    public static MessageMapper createFromTemplate(JSONObject template) {
        String topic = template.getString(CONFIG_ELEMENT_TOPIC);
        JSONArray jsonFields = template.getJSONArray(CONFIG_ELEMENT_FIELDS);
        Map<String, String> fields = StreamSupport.stream(jsonFields.spliterator(), false)
                .map(JSONObject.class::cast)
                .collect(Collectors.toMap(f->f.getString(CONFIG_ELEMENT_TO), f->f.getString(CONFIG_ELEMENT_FROM)));
        Map<String, String> properties = StreamSupport.stream(jsonFields.spliterator(), false)
                .map(JSONObject.class::cast)
                .filter(f->f.has(CONFIG_ELEMENT_PROP))
                .collect(Collectors.toMap(f->f.getString(CONFIG_ELEMENT_TO), f->f.getJSONObject(CONFIG_ELEMENT_PROP).toString()));
        MessageMapper result = new MessageMapper(topic, fields, properties);

        try {
            result.setDefaultIndex(template.getString(CONFIG_ELEMENT_INDEX));
        } catch (JSONException e) {}

        try {
            result.setDefaultType(template.getString(CONFIG_ELEMENT_TYPE));
        } catch (JSONException e) {}

        return result;
    }

    public static List<MessageMapper> createMultiFromTemplate(JSONObject template){
        JSONArray jsonMappers = template.getJSONArray(CONFIG_ELEMENT_MAPPERS);
        return StreamSupport.stream(jsonMappers.spliterator(), false)
                .map(JSONObject.class::cast)
                .map(MessageMapperFactory::createFromTemplate)
                .collect(Collectors.toList());
    }
}
