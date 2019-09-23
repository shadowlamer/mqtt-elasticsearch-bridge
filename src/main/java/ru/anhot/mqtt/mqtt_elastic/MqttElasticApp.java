package ru.anhot.mqtt.mqtt_elastic;


import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MqttElasticApp {

    private static final int BULK_DEFAULT_ACTIONS   = 200;
    private static final int BULK_DEFAULT_SIZE   = 5;
    private static final int BULK_DEFAULT_FLUSH  = 5;
    private static final int BULK_DEFAULT_CONCURRENT  = 1;

    private static final String OPTION_TEMPLATE  = "template";
    private static final String OPTION_MQTT_URI  = "mqtt-uri";
    private static final String OPTION_ELASTIC_URI  = "elastic-uri";
    private static final String OPTION_BULK_ACTIONS  = "bulk-actions";
    private static final String OPTION_BULK_SIZE  = "bulk-size";
    private static final String OPTION_BULK_FLUSH  = "bulk-flush";
    private static final String OPTION_MQTT_USER  = "mqtt-user";
    private static final String OPTION_MQTT_PASSWD  = "mqtt-passwd";

    private static long longId = new Date().getTime();

    private static JSONObject loadJsonFromFile(String name) throws IOException {
        InputStream jsonStream =  new FileInputStream(name);
        String json = new Scanner(jsonStream,"UTF-8").useDelimiter("\\A").next();
        return new JSONObject(json);
    }

    public static void main(String[] args) {

        Options options = new Options();
        JSONObject template;
        BulkListener listener;
        BulkProcessor bulkProcessor;

        Option templateOption = new Option("t", OPTION_TEMPLATE, true, "Template file path (JSON)");
        templateOption.setRequired(true);
        options.addOption(templateOption);

        Option mqttOption = new Option("m", OPTION_MQTT_URI, true, "MQTT broker URI");
        mqttOption.setRequired(true);
        options.addOption(mqttOption);

        Option elasticOption = new Option("e", OPTION_ELASTIC_URI, true, "Elasticsearch server URI");
        elasticOption.setRequired(true);
        options.addOption(elasticOption);

        Option bulkActionsOption = new Option("n", OPTION_BULK_ACTIONS, true, "Max number of actions per bulk request");
        bulkActionsOption.setRequired(false);
        options.addOption(bulkActionsOption);

        Option bulkSizeOption = new Option("s", OPTION_BULK_SIZE, true, "Max amount of data per bulk request (megabytes)");
        bulkSizeOption.setRequired(false);
        options.addOption(bulkSizeOption);

        Option bulkIntervalOption = new Option("i", OPTION_BULK_FLUSH, true, "Bulk request flush interval (seconds)");
        bulkIntervalOption.setRequired(false);
        options.addOption(bulkIntervalOption);

        Option mqttUserOption = new Option("u", OPTION_MQTT_USER, true, "Username for MQTT broker");
        mqttUserOption.setRequired(false);
        options.addOption(mqttUserOption);

        Option mqttPasswdOption = new Option("p", OPTION_MQTT_PASSWD, true, "Password for MQTT broker");
        mqttPasswdOption.setRequired(false);
        options.addOption(mqttPasswdOption);

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

        String templatePath = cmd.getOptionValue(OPTION_TEMPLATE);

        URI mqttUri;
        try {
            mqttUri = new URI(cmd.getOptionValue(OPTION_MQTT_URI));
        } catch (URISyntaxException e) {
            System.out.println("Wrong MQTT broker URI.");
            System.exit(3);
            return;
        }

        URI elasticUri;
        try {
            elasticUri = new URI(cmd.getOptionValue(OPTION_ELASTIC_URI));
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

        MqttClient mqttClient;
        TransportClient elasticClient;

        System.out.println("Connecting...");

        try {
            mqttClient =  new MqttClient(mqttUri.toString(), MqttClient.generateClientId());
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

        int bulkActions = BULK_DEFAULT_ACTIONS;
        if (cmd.hasOption(OPTION_BULK_ACTIONS))
            bulkActions = Integer.parseInt(cmd.getOptionValue(OPTION_BULK_ACTIONS));
        if (cmd.hasOption(OPTION_BULK_SIZE))
            bulkActions = Integer.parseInt(cmd.getOptionValue(OPTION_BULK_SIZE));
        if (cmd.hasOption(OPTION_BULK_FLUSH))
            bulkActions = Integer.parseInt(cmd.getOptionValue(OPTION_BULK_FLUSH));
                
        listener = new BulkListener();
        bulkProcessor = BulkProcessor.builder(elasticClient, listener)
                .setBulkActions(bulkActions)
                .setBulkSize(new ByteSizeValue(BULK_DEFAULT_SIZE, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(BULK_DEFAULT_FLUSH))
                .setConcurrentRequests(BULK_DEFAULT_CONCURRENT)
                .build();


        mqttClient.setCallback( new BridgeCallback(mqttClient, elasticClient, mappers, bulkProcessor) );
        try {
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            if (cmd.hasOption(OPTION_MQTT_USER)) {
                connectOptions.setUserName(cmd.getOptionValue(OPTION_MQTT_USER));
            }
            if (cmd.hasOption(OPTION_MQTT_PASSWD)) {
                connectOptions.setPassword(cmd.getOptionValue(OPTION_MQTT_PASSWD).toCharArray());
            }
            mqttClient.connect(connectOptions);
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
    }

    public static long getLongId() {
        longId++;
        return longId;
    }

}
