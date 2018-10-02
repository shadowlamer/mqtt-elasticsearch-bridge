# mqtt-elasticsearch-bridge
Consumes MQTT messages with JSON payload, converts them and sends to Elasticsearch. You can define conversion rules as JSON template.
# Usage

## As executable jar
```
java -jar mqtt-elasticsearch-bridge.jar [Options...]
```
Where `Options` are:
```
 -e,--elastic-uri <arg>    Elasticsearch server URI
 -i,--bulk-flush <arg>     Bulk request flush interval
 -m,--mqtt-uri <arg>       MQTT broker URI
 -n,--bulk-actions <arg>   Max number of actions per bulk request
 -s,--bulk-size <arg>      Max amount of data per bulk request
 -t,--template <arg>       Template file path (JSON)
```
For example:
```
java -jar mqtt-elasticsearch-bridge.jar -e tcp://localhost:9300 -m tcp://localhost:1883 -t /etc/template.json
```
## As docker image
```
docker-compose -f src/main/docker/meb.yml up -d
```
Modify src/main/docker/meb.yml to suit your needs.

# Template format
```
{
    "mappers":[
        {
            "topic":"device/(\\w+)/status",
            "index": "status",
            "type": "status",
            "fields":[
                {"from":"timestamp","to":"timestamp", "property":{"type":"date", "format":"epoch_seconds"}},
                {"from":"status","to":"status"},
                {"from":"$1","to":"keyId"},
            ]
        }
    ]
}
```
Where:

`mappers` - array of mappers. Each mapper will produce elasticsearch document.

`topic` - Regular expression to match MQTT topic. Expression can contain groups.

`index` - default index for document if no other rules specified.

`type` - default document type if no other rule specified.

`fields` - array of rules to convert field names. Each rule contains `from` and `to` fields.
if `from` defined as `$n` corresponding group from `topic` will be taken.
There are predefined values for `to`: `__index`,`__type` and `__id` for index, type and id of document. 

You can use generated values in `from` field. There are `@@uuid` and `@@id` generators defined for now. `@@uuid` generates string UUID, `@@id` generates long value, starting from timestamp, when app was started and increments it on each request.

if mapper definition contains at least one `property` field and there is no index named `index` exists index with apropriate name, document types and mappings will be created.

If no id is defined for document UUID will be generated.

# Building

## Build executable jar
```
mvn package
```

## Build docker image
```
mvn dockerfile:build
```
