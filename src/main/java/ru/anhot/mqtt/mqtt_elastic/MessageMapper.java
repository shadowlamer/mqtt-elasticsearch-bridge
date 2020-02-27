package ru.anhot.mqtt.mqtt_elastic;

import org.elasticsearch.common.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.anhot.mqtt.mqtt_elastic.generators.Generator;
import ru.anhot.mqtt.mqtt_elastic.generators.GeneratorFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageMapper {

    public static final String FIELD_INDEX = "__index";
    public static final String FIELD_TYPE  = "__type";
    public static final String FIELD_ID    = "__id";
    private String defaultIndex = "index";
    private String defaultType = "type";

    private Pattern pattern;
    private Map<String, String> fields;

    private Map<String, JSONObject> properties;
    private Map<String, Object> defaults;

    private GeneratorFactory generatorFactory;


    public MessageMapper(String pattern, Map<String, String> fields, Map<String, JSONObject> properties, Map<String, Object> defaults) {
        this.pattern = Pattern.compile(pattern);
        this.fields = fields;
        this.properties = properties;
        this.defaults = defaults;
        this.generatorFactory = new GeneratorFactory(this.pattern);
    }

    public Optional<ElasticPayload> map(String topic, String mqttPayload) {
        if (Strings.isNullOrEmpty(topic) || Strings.isNullOrEmpty(mqttPayload))
            return Optional.empty();
        Matcher matcher = pattern.matcher(topic);
        if (!matcher.matches())
            return Optional.empty();
        int fieldCounter = 1;
        while (mqttPayload.contains("\"\":")) {
            mqttPayload = mqttPayload.replaceFirst("\"\":", "\"emptyField" + fieldCounter + "\":");
            fieldCounter++;
        }
        JSONObject jsonFrom = new JSONObject(mqttPayload);
        JSONObject jsonTo = new JSONObject();
        ElasticPayload result = new ElasticPayload();
        UUID uuid = UUID.randomUUID();
        for (String to : fields.keySet()) {
            String from = fields.get(to);

            Object val;
            Object jsonValue;

            Generator suitableGenerator = generatorFactory.findSuitableGenerator(from);
            try {
                jsonValue = jsonFrom.get(from);
            } catch (JSONException e) {
                jsonValue = defaults.getOrDefault(to, new JSONObject());
            }
            val = suitableGenerator.getValue(topic, from, jsonValue);

            if (FIELD_INDEX.equals(to))
                result.setIndex(String.valueOf(val));
            else if (FIELD_TYPE.equals(to))
                result.setType(String.valueOf(val));
            else if (FIELD_ID.equals(to))
                result.setId(String.valueOf(val));
            else
                jsonTo.put(to,val);

            if (result.getIndex()==null)
                result.setIndex(defaultIndex);
            if (result.getType()==null)
                result.setType(defaultType);
            if (result.getId()==null)
                result.setId(uuid.toString());
        }
        result.setSource(jsonTo.toString());
        return Optional.of(result);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Pattern: \"").append(pattern).append("\" ")
                .append("Fields: ").append(fields.toString())
                .append("Properties: ").append(properties.toString())
                .append("Defaults: ").append(defaults.toString());
        return builder.toString();
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public String getDefaultIndex() {
        return defaultIndex;
    }

    public void setDefaultIndex(String defaultIndex) {
        this.defaultIndex = defaultIndex;
    }

    public String getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    public Map<String, JSONObject> getProperties() {
        return properties;
    }
}
