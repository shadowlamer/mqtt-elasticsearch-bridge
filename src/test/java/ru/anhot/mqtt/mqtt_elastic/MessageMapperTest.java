package ru.anhot.mqtt.mqtt_elastic;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class MessageMapperTest {

    @Test
    public void testMap() throws IOException {
        JSONObject template = TestUtils.loadJsonFromResource(this.getClass(), "template_single.json");
        JSONObject payload = TestUtils.loadJsonFromResource(this.getClass(), "template_payload.json");
        MessageMapper mapper = MessageMapperFactory.createFromTemplate(template);
        ElasticPayload result = mapper.map("index/type",payload.toString()).orElseThrow(AssertionError::new);
        Assert.assertEquals("{\"to2\":\"val2\",\"to1\":\"val1\"}", result.getSource());
        Assert.assertEquals("index",result.getIndex());
        Assert.assertEquals("type",result.getType());
        Assert.assertEquals("id1",result.getId());
    }

    @Test
    public void testMissingFielsd() throws IOException {
        JSONObject template = TestUtils.loadJsonFromResource(this.getClass(), "template_single.json");
        JSONObject payload = TestUtils.loadJsonFromResource(this.getClass(), "template_payload_missing.json");
        MessageMapper mapper = MessageMapperFactory.createFromTemplate(template);
        ElasticPayload result = mapper.map("index/type",payload.toString()).orElseThrow(AssertionError::new);
        Assert.assertEquals("{\"to2\":[],\"to1\":{}}", result.getSource());
        Assert.assertEquals("index",result.getIndex());
        Assert.assertEquals("type",result.getType());
        Assert.assertEquals("id1",result.getId());
    }
}
