# mqtt-elasticsearch-bridge
Consumes MQTT messages with JSON payload, converts them and sends to Elasticsearch. You can define conversion rules as JSON template.
# Usage
```
java -jar mqtt-elasticsearch-bridge-0.1-SNAPSHOT.jar [Options...]
```
Where `Options` are:
```
 -e,--elastic-uri <arg>   Elasticsearch server URI
 -m,--mqtt-uri <arg>      MQTT broker URI
 -t,--template <arg>      Template file path
```
For example:
```
java -jar mqtt-elasticsearch-bridge-0.1-SNAPSHOT.jar -e tcp://localhost:9300 -m tcp://localhost:1883 -t /etc/template.json
```
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

if mapper definition contains at least one `property` field and there is no index named `index` exists index with apropriate name, document types and mappings will be created.

If no id is defined for document UUID will be generated.
 
