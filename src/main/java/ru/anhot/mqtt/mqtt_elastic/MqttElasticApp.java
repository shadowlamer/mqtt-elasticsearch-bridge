package ru.anhot.mqtt.mqtt_elastic;


import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MqttElasticApp {

    private static long longId = new Date().getTime();

    private static JSONObject loadJsonFromFile(String name) throws IOException {
        InputStream jsonStream =  new FileInputStream(name);
        String json = new Scanner(jsonStream,"UTF-8").useDelimiter("\\A").next();
        return new JSONObject(json);
    }

    public static void main(String[] args) {

        Options options = new Options();
        JSONObject template;

        Option templateOption = new Option("t", "template", true, "Template file path (JSON)");
        templateOption.setRequired(true);
        options.addOption(templateOption);

        Option mqttOption = new Option("m", "mqtt-uri", true, "MQTT broker URI");
        mqttOption.setRequired(true);
        options.addOption(mqttOption);

        Option elasticOption = new Option("e", "elastic-uri", true, "Elasticsearch server URI");
        elasticOption.setRequired(true);
        options.addOption(elasticOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MQTT-Elasticsearch bridge", options);
            System.exit(1);
            return;
        }

        String templatePath = cmd.getOptionValue("template");

        URI mqttUri = null;
        try {
            mqttUri = new URI(cmd.getOptionValue("mqtt-uri"));
        } catch (URISyntaxException e) {
            System.out.println("Wrong MQTT broker URI.");
            System.exit(3);
            return;
        }

        URI elasticUri = null;
        try {
            elasticUri = new URI(cmd.getOptionValue("elastic-uri"));
        } catch (URISyntaxException e) {
            System.out.println("Wrong Elasticsearch server URI.");
            System.exit(4);
            return;
        }

        try {
            template = loadJsonFromFile(templatePath);
        } catch (IOException e) {
            System.out.println("Can't read template from "+templatePath);
            System.exit(2);
            return;
        }

        List<MessageMapper> mappers = MessageMapperFactory.createMultiFromTemplate(template);
        Map<String, JSONObject> indexes = IndexFactory.composeIndexes(mappers);

        MqttClient mqttClient= null;
        TransportClient elasticClient = null;

        System.out.println("Connecting...");

        try {
            mqttClient = new MqttClient(mqttUri.toString(), MqttClient.generateClientId());
        } catch (MqttException e) {
            System.out.println("Can't connect to MQTT broker.");
            System.exit(3);
            return;
        }

        try {
            elasticClient = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(elasticUri.getHost()), elasticUri.getPort()));
        } catch (Exception e) {
            System.out.println("Can't connect to Elasticsearch server.");
            System.exit(4);
            return;
        }

        mqttClient.setCallback( new BridgeCallback(elasticClient, mappers) );
        try {
            mqttClient.connect();
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(5);
            return;
        }

        for (MessageMapper mapper: mappers) {
            String indexName = mapper.getDefaultIndex();
            boolean exists = elasticClient.admin().indices()
                    .prepareExists(indexName)
                    .execute().actionGet().isExists();
            if (!exists) {
                JSONObject index = indexes.get(indexName);
                if (index!=null) {
                    if (!elasticClient.admin().indices().prepareCreate(indexName)
                            .setSource(index.toString(), XContentType.JSON).execute().actionGet().isAcknowledged()) {
                        System.out.println("Can't create index "+indexName+".");
                    }
                }
            }

        }

        System.out.println("Server started.");

        try {
            mqttClient.subscribe("#");
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(6);
            return;
        }

    }

    public static long getLongId() {
        longId++;
        return longId;
    }

}
