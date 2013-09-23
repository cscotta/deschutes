package com.cscotta.deschutes.example.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Example application configuration.
 */
public class Config extends Configuration {

    @NotEmpty
    @JsonProperty
    private String aggregatorType;

    @JsonProperty
    private boolean decodeOnly;

    @JsonProperty
    private int processorThreads;

    @JsonProperty
    private int runFor;

    @NotEmpty
    @JsonProperty
    private String dataSource;

    @NotEmpty
    @JsonProperty
    private String dataFile;

    @JsonProperty
    private int windowLength;

    public boolean getDecodeOnly() {
        return decodeOnly;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getDataFile() {
        return dataFile;
    }

    public int getWindowLength() {
        return windowLength;
    }

    public int getRunFor() {
        return runFor;
    }

    public int getProcessorThreads() {
        return processorThreads;
    }

    public String getAggregatorType() {
        return aggregatorType;
    }

}
