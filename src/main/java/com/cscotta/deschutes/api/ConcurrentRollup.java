package com.cscotta.deschutes.api;

/**
 * <p>As a {@link StreamAggregator} receives events from a stream, events are
 * grouped by their dimension and rolled up using ConcurrentRollup objects.
 * ConcurrentRollup objects represent the aggregated form of your input while also
 * specifying the transformation to be applied. As such, they encapsulate both
 * the aggregation logic for your input {@link Event}s and are the final form
 * emitted by an aggregator. Because ConcurrentRollup objects are also
 * {@link Event}s themselves, they can be used as input to further downstream
 * filtering and aggregation stages. This also allows one to compose custom
 * aggregations that join the output of higher-level aggregations and operate
 * on {@link Event}s of different types.</p>
 *
 * <p>For example, consider an event with a field called "count." As our rollup
 * operation, we may want to sum up the count of all objects we've observed
 * that match along a particular dimension. In that case, we would implement
 * ConcurrentRollup as follows:</p>
 *
 * <pre>
 * public class {@literal MyRollup<MyEvent> implements ConcurrentRollup<MyEvent>} {
 *     private final long timestamp;
 *     private final AtomicLong totalCount = new AtomicLong(0);
 *     public long getTotalCount() { return totalCount.get(); }
 *
 *     public MyRollup(long timestamp) {
 *         this.timestamp = timestamp;
 *     }
 *
 *     {@literal @Override}
 *     public void rollup(MyEvent input) {
 *         totalCount.incrementAndGet(input.getCount());
 *     }
 * }
 * </pre>
 *
 * <p>As the aggregator receives each event, a ConcurrentRollup object is created
 * or initialized for the time slice and dimension, and the implementation's
 * ConcurrentRollup.rollup({@link Event} event) method is called. In the example above,
 * the resulting rollup object will be emitted by the {@link StreamAggregator},
 * presenting the total aggregate count to downstream {@link OutputListener}s.</p>
 *
 * <p>The aggregator receives events and applies rollups in parallel, so it is
 * important that your aggregation methods be threadsafe. Summing operations
 * can be implemented locklessly using AtomicIntegers or AtomicLongs, which
 * offer methods for summing values concurrently using CAS operations. Values
 * that are highly contended may be better implemented using the Counter class
 * in <a href="https://github.com/boundary/high-scale-lib">high-scale-lib</a>,
 * which operates as a striped array of values. For less-contended aggregates,
 * simple locking and synchronized collections may be sufficient.</p>
 *
 * <p>In order to enable the output of filtering and aggregation to be used as
 * input to further filtering and aggregation, ConcurrentRollup implementations
 * also implement {@link Event} - that is, rollups must have an associated
 * timestamp.</p>
 */
public interface ConcurrentRollup<Input> extends Event {

    /**
     * <p>Rollup method called as events are received by a stream for
     * aggregation.</p>
     *
     * <p>Implementations of ConcurrentRollup should accept {@link Event}s, maintain
     * the aggregations desired (sums, averages, histograms, and so on), and expose
     * them for downstream {@link OutputListener}s to access as output. See the
     * {@link ConcurrentRollup} documentation above for a sample implementation.</p>
     *
     * @param input An event received by a stream to be rolled up.
     */
    public abstract void rollup(Input input);

    public long getTimestamp();
}
