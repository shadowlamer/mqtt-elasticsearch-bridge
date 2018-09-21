package ru.anhot.mqtt.mqtt_elastic;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class MessageMapperFactoryTest {

    @Test
    public void testCreateFromTemplate() throws IOException {
        JSONObject template = TestUtils.loadJsonFromResource(this.getClass(), "template_single.json");
        MessageMapper mapper = MessageMapperFactory.createFromTemplate(template);
        Assert.assertEquals("(\\w+)/(\\w+)",mapper.getPattern().toString());
        Assert.assertEquals(5, mapper.getFields().size());
        Assert.assertEquals("{from1=to1, from2=to2, id=__id, $1=__index, $2=__type}", mapper.getFields().toString());
    }

    @Test
    public void testCreateMultiFromTemplate() throws IOException {
        JSONObject template = TestUtils.loadJsonFromResource(this.getClass(), "template_multi.json");
        List<MessageMapper> mappers = MessageMapperFactory.createMultiFromTemplate(template);
        Assert.assertEquals(5,mappers.size());
        MessageMapper mapper = mappers.get(0);
        Assert.assertEquals("test1",mapper.getPattern().toString());
        Assert.assertEquals(0, mapper.getFields().size());
    }
}
