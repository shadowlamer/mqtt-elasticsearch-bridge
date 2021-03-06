package ru.anhot.mqtt.mqtt_elastic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexFactory {

    public static final String CONFIG_ELEMENT_MAPPINGS   = "mappings";
    public static final String CONFIG_ELEMENT_PROPERTIES = "properties";

    public static Map<String,JSONObject> composeIndexes(List<MessageMapper> mappers) {
        Map<Map.Entry<String, String>, Map<String,JSONObject>> mappings = new HashMap<Map.Entry<String, String>, Map<String,JSONObject>>();
        for (MessageMapper mapper: mappers) {
            Map.Entry<String, String> key = new AbstractMap.SimpleImmutableEntry<String, String>(mapper.getDefaultIndex(), mapper.getDefaultType());
            Map<String,JSONObject> properties = mappings.get(key);
            if (properties==null) {
                properties = new HashMap<String, JSONObject>();
                mappings.put(key,properties);
            }
            for (String field:mapper.getProperties().keySet()) {
                properties.put(field,mapper.getProperties().get(field));
            }
        }
        Map<String,JSONObject> result = new HashMap<String,JSONObject>();
        for (Map.Entry<String, String> key: mappings.keySet()) {
            boolean emptyIndex = true;
            String indexName = key.getKey();
            String typeName = key.getValue();
            JSONObject index = result.get(indexName);
            JSONObject resultMappings;

            if (index==null) {
                index = new JSONObject();
            }

            try {
                resultMappings = index.getJSONObject(CONFIG_ELEMENT_MAPPINGS);
            } catch (JSONException e) {
                resultMappings = new JSONObject();
                index.put(CONFIG_ELEMENT_MAPPINGS, resultMappings);
            }

            JSONObject doc;
            try {
                doc = resultMappings.getJSONObject(typeName);
            } catch (JSONException e) {
                doc = new JSONObject();
                resultMappings.put(typeName, doc);
            }

            JSONObject properties = new JSONObject();
            for (String field:mappings.get(key).keySet()){
                properties.put(field, mappings.get(key).get(field));
                emptyIndex = false;
            }

            doc.put(CONFIG_ELEMENT_PROPERTIES, properties);

            if (!emptyIndex) {
                result.put(indexName, index);
            }
        }
        return result;
    }
}
