package com.cscotta.deschutes.json;

import com.codahale.metrics.Snapshot;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Helper class to teach Jackson how to serialize a Histogram snapshot.
 */
public class HistogramSnapshotSerializer extends JsonSerializer<Snapshot> {

    @Override
    public Class<Snapshot> handledType() {
        return Snapshot.class;
    }

    @Override
    public void serialize(Snapshot snap, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("min", snap.getMin());
        jgen.writeNumberField("max", snap.getMax());
        jgen.writeNumberField("mean", snap.getMean());
        jgen.writeNumberField("stdDev", snap.getStdDev());
        jgen.writeNumberField("median", snap.getMedian());
        jgen.writeNumberField("p75", snap.get75thPercentile());
        jgen.writeNumberField("p95", snap.get95thPercentile());
        jgen.writeNumberField("p98", snap.get98thPercentile());
        jgen.writeNumberField("p99", snap.get99thPercentile());
        jgen.writeNumberField("p999", snap.get999thPercentile());
        jgen.writeEndObject();
    }
}
