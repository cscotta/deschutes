package com.cscotta.deschutes.aggregation;

import com.cscotta.deschutes.api.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A ConcurrentHashMap-based OLAP aggregator.
 * @param <Input>
 */
public final class CHMOLAPAggregator<Input extends Event> implements StreamAggregator<Input> {

    private final int windowLength;
    private final RollupFactory<Input> builder;
    private final Dimension<Input> dimension;
    private final List<OutputListener<Input>> listeners = Lists.newCopyOnWriteArrayList();
    private final Set<Long> modified = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
    private static final Logger log = LoggerFactory.getLogger(CHMOLAPAggregator.class);

    public CHMOLAPAggregator(int windowLength,
                             Dimension<Input> dimension,
                             RollupFactory<Input> builder) {
        this.builder = builder;
        this.windowLength = windowLength;
        this.dimension = dimension;
    }

    public CHMOLAPAggregator(Dimension<Input> dimension, RollupFactory<Input> builder) {
        this.builder = builder;
        this.dimension = dimension;
        this.windowLength = 10;
    }

    private final AtomicLong epochTimestamp = new AtomicLong(0);
    private final AtomicLong discardedEvents = new AtomicLong(0);
    private final ConcurrentNavigableMap<Long, ConcurrentMap<String, ConcurrentRollup<Input>>> window =
        new ConcurrentSkipListMap<Long, ConcurrentMap<String, ConcurrentRollup<Input>>>();

    @Override
    public void observe(Input event) {
        long currentTs = epochTimestamp.get();
        long eventTs = event.getTimestamp();
        if (expired(currentTs, eventTs)) return;
        if (advanced(currentTs, eventTs)) emit();

        String key = dimension.getDimension(event);
        ConcurrentRollup<Input> values = getOrCreateValues(getOrCreateTimeSlice(eventTs), key, eventTs);
        values.rollup(event);
        modified.add(eventTs);
    }

    @Override
    public CHMOLAPAggregator<Input> addListener(OutputListener<Input> listener) {
        listeners.add(listener);
        sendState(listener);
        return this;
    }

    private void sendState(OutputListener<Input> listener) {
        Map<Long, Map<String, ConcurrentRollup<Input>>> insert = Maps.newHashMap();
        insert.putAll(window);
        List<Long> remove = Lists.newArrayList();
        listener.update(remove, insert);
    }

    private void emit() {
        log.info("Current time: " + epochTimestamp.get());
        Set<Long> expired = window.headMap(epochTimestamp.get() - (windowLength * 1000)).keySet();
        List<Long> remove = Lists.newArrayList();
        //remove.addAll(expired);
        remove.addAll(modified);
        Map<Long, Map<String, ConcurrentRollup<Input>>> insert = Maps.newHashMap();

        for (Long key : modified) insert.put(key, window.get(key));
        for (Long key : expired) window.remove(key);

        modified.removeAll(expired);
        modified.removeAll(insert.keySet());

        for (OutputListener<Input> listener : listeners)
            listener.update(remove, insert);
    }

    // putIfAbsent dance to fetch a value for a timeslice or initialize it if not yet present
    private ConcurrentRollup<Input> getOrCreateValues(
            ConcurrentMap<String, ConcurrentRollup<Input>> timeSlice, String key, long timestamp) {

        ConcurrentRollup<Input> values = timeSlice.get(key);
        if (values == null) {
            ConcurrentRollup<Input> attempted = builder.buildRollup(timestamp);
            ConcurrentRollup<Input> result = timeSlice.putIfAbsent(key, attempted);
            return (result == null) ? attempted : result;
        } else {
            return values;
        }
    }

    // get-or-else-update boilerplate for time slice
    private ConcurrentMap<String, ConcurrentRollup<Input>> getOrCreateTimeSlice(long ts) {
        final ConcurrentMap<String, ConcurrentRollup<Input>> timeslice = window.get(ts);
        if (timeslice == null) {
            ConcurrentMap<String, ConcurrentRollup<Input>> attempted =
                new ConcurrentHashMap<String, ConcurrentRollup<Input>>();
            ConcurrentMap<String, ConcurrentRollup<Input>> result = window.putIfAbsent(ts, attempted);
            return (result == null) ? attempted : result;
        } else {
            return timeslice;
        }
    }

    // Determines if an incoming event is expired.
    private boolean expired(long currentTs, long eventTs) {
        boolean expired = (eventTs < currentTs - windowLength);
        if (expired) discardedEvents.incrementAndGet();
        return expired;
    }

    // Determines if an incoming event advances the clock.
    private boolean advanced(long currentTs, long eventTs) {
        return eventTs > currentTs && epochTimestamp.compareAndSet(currentTs, eventTs);
    }

}
