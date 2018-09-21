package ru.anhot.mqtt.mqtt_elastic;

public class ElasticPayload {
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    private String index;
    private String type;
    private String id;
    private String source;

    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Index: ").append(index).append(" ")
                .append("Type: ").append(type).append(" ")
                .append("Id: ").append(id).append(" ")
                .append("Source: ").append(source);
        return builder.toString();

    }
}
