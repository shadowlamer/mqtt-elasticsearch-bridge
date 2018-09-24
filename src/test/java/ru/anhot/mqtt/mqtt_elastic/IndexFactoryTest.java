package ru.anhot.mqtt.mqtt_elastic;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class IndexFactoryTest {

    @Test
    public void testComposeIndexes() throws IOException {
        JSONObject template = TestUtils.loadJsonFromResource(this.getClass(), "template_multi.json");
        List<MessageMapper> mappers = MessageMapperFactory.createMultiFromTemplate(template);
        Map<String,JSONObject> indexes = IndexFactory.composeIndexes(mappers);
        Assert.assertEquals(2,indexes.size());
        JSONObject index;
        index = indexes.get("index1");
        Assert.assertEquals("{\"mappings\":{\"type2\":{\"properties\":{\"to2\":{\"type\":\"integer\"},\"to1\":{\"type\":\"boolean\"}}},\"type1\":{\"properties\":{\"to1\":{\"type\":\"boolean\"}}}}}",index.toString());
        index = indexes.get("index2");
        Assert.assertEquals("{\"mappings\":{\"type2\":{\"properties\":{\"to1\":{\"type\":\"boolean\"}}},\"type1\":{\"properties\":{\"to1\":{\"type\":\"boolean\"}}}}}",index.toString());
    }
}
