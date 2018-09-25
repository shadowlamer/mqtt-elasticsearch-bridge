package ru.anhot.mqtt.mqtt_elastic;

import org.elasticsearch.common.Strings;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageMapper {

    public static final String FIELD_INDEX = "__index";
    public static final String FIELD_TYPE  = "__type";
    public static final String FIELD_ID    = "__id";
    public static final String FIELD_UUID    = "__uuid";
    private static final String GROUP_PATTERN = "\\$(\\d)";
    private String defaultIndex = "index";
    private String defaultType = "type";

    private Pattern pattern;
    private Pattern groupPattern;
    private Map<String, String> fields;

    private Map<String, String> properties;

    public MessageMapper(String pattern, Map<String, String> fields, Map<String, String> properties, String index, String type) {
        this(pattern, fields, properties);
        this.defaultIndex = index;
        this.defaultType = type;
    }

    public MessageMapper(String pattern, Map<String, String> fields, Map<String,String> properties) {
        this.pattern = Pattern.compile(pattern);
        this.fields = fields;
        this.properties = properties;
        this.groupPattern = Pattern.compile(GROUP_PATTERN);
    }

    public Optional<ElasticPayload> map(String topic, String mqttPayload) {
        if (Strings.isNullOrEmpty(topic) || Strings.isNullOrEmpty(mqttPayload))
            return Optional.empty();
        Matcher matcher = pattern.matcher(topic);
        if (!matcher.matches())
            return Optional.empty();
        JSONObject jsonFrom = new JSONObject(mqttPayload);
        JSONObject jsonTo = new JSONObject();
        ElasticPayload result = new ElasticPayload();
        String uuid = UUID.randomUUID().toString();
        for (String to : fields.keySet()) {
            String from = fields.get(to);

            Object val;
            Matcher groupMatcher = groupPattern.matcher(from);
            if (groupMatcher.matches()) {
                Integer group = Integer.valueOf(groupMatcher.group(1));
                val = matcher.group(group);
            } else if (FIELD_UUID.equals(from)) {
                val = uuid;
            } else
                val = jsonFrom.get(from);

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
                result.setId(uuid);
        }
        result.setSource(jsonTo.toString());
        return Optional.of(result);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Pattern: \"").append(pattern).append("\" ")
                .append("Fields: ").append(fields.toString())
                .append("Properties: ").append(fields.toString());
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

    public Map<String, String> getProperties() {
        return properties;
    }
}
