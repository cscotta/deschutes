package com.cscotta.deschutes.input;

import com.cscotta.deschutes.api.EventWrapper;
import com.google.protobuf.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class CyclingProtobufPreReader<ProtoType, Input> implements Iterable<Input> {

    final Parser<ProtoType> parser;
    final InputStream inputStream;
    final EventWrapper<ProtoType, Input> eventWrapper;
    final ArrayList<Input> input = new ArrayList<Input>();
    private static final Logger log = LoggerFactory.getLogger(CyclingProtobufPreReader.class);

    public CyclingProtobufPreReader(Parser<ProtoType> parser,
                                    EventWrapper<ProtoType, Input> eventWrapper,
                                    BufferedInputStream inputStream) {
        this.parser = parser;
        this.eventWrapper = eventWrapper;
        this.inputStream = inputStream;
    }

    public void initialize() {
        log.info("Reading protobufs into memory...");

        while (true) {
            try {
                ProtoType type = parser.parseDelimitedFrom(inputStream);
                if (type != null) input.add(eventWrapper.wrap(type));
                else break;
            } catch (Exception e) {
                log.error("Error reading protobuf.", e);
                break;
            }
        }

        log.info("Completed reading protobufs from file. Size: " + input.size() + " records.");
    }

    @Override
    public Iterator<Input> iterator() {
        return input.iterator();
    }
}
