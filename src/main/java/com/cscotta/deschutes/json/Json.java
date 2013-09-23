package com.cscotta.deschutes.json;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * Wrapper around Jackson to return an ObjectMapper with custom serialization
 * modules applied.
 */
public class Json {
    public final ObjectMapper objectMapper = new ObjectMapper();

    public Json() {
        SimpleModule olapMods = new SimpleModule("olapMods", new Version(1,0,0,null));
        olapMods.addSerializer(new HistogramSnapshotSerializer());
        objectMapper.registerModule(olapMods);
    }

}
