package com.cscotta.deschutes.input;

import com.cscotta.deschutes.api.EventWrapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;

public class ProtobufReader<ProtoType, Input> implements Iterable<Input> {

    final Parser<ProtoType> parser;
    final InputStream inputStream;
    final EventWrapper<ProtoType, Input> eventWrapper;
    private static final Logger log = LoggerFactory.getLogger(ProtobufReader.class);

    public ProtobufReader(Parser<ProtoType> parser,
                          EventWrapper<ProtoType, Input> eventWrapper,
                          BufferedInputStream inputStream) {
        this.parser = parser;
        this.eventWrapper = eventWrapper;
        this.inputStream = inputStream;
    }

    @Override
    public Iterator<Input> iterator() {
        return new Iterator<Input>() {
            Input next = null;

            @Override
            public boolean hasNext() {
                try {
                    next = eventWrapper.wrap(parser.parseDelimitedFrom(inputStream));
                    return (next != null);
                } catch (InvalidProtocolBufferException e) {
                    log.error("Error reading protobuf.", e);
                    return false;
                }
            }

            @Override
            public Input next() {
                return next;
            }

            @Override
            public void remove() {
                throw new RuntimeException("Not implemented.");
            }
        };
    }
}
