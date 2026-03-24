package org.openjfx.sio2E4.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonMapperTest {

    static class DummyObject {
        public String name;
        public int value;

        public DummyObject() {}
        public DummyObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    @Test
    void testParseObject() throws IOException {
        String json = "{\"name\":\"test\", \"value\":42}";
        DummyObject obj = JsonMapper.parseObject(json, DummyObject.class);
        
        assertNotNull(obj);
        assertEquals("test", obj.name);
        assertEquals(42, obj.value);
    }

    @Test
    void testParseList() throws IOException {
        String json = "[{\"name\":\"first\", \"value\":1}, {\"name\":\"second\", \"value\":2}]";
        List<DummyObject> list = JsonMapper.parseList(json, DummyObject[].class);
        
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("first", list.get(0).name);
        assertEquals(1, list.get(0).value);
        assertEquals("second", list.get(1).name);
        assertEquals(2, list.get(1).value);
    }

    @Test
    void testToJson() {
        DummyObject obj = new DummyObject("hello", 99);
        String json = JsonMapper.toJson(obj);
        
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"hello\""));
        assertTrue(json.contains("\"value\":99"));
    }
}
