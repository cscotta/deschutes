package com.cscotta.deschutes.api;

/**
 * Primary aggregator interface. A StreamAggregator must implement an
 * observe({@link Event}) method, and a method to attach output listeners.
 * @param <Input>
 */
public interface StreamAggregator<Input extends Event> {

    public void observe(Input event);

    public StreamAggregator<Input> addListener(OutputListener<Input> listener);

}
