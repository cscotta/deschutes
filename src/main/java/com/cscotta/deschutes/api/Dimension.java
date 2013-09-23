package com.cscotta.deschutes.api;

/**
 * <p>A "Dimension" refers to one of many (usually non-numerical) fields used to
 * group output. Examples of dimensions might be "user agent," "country,"
 * "language," and so forth. Specifying a dimension field segments output into
 * buckets and groups based on them.</p>
 *
 * <p>For example, specifying "country" as the aggregation dimension will result
 * in output grouped by country, with each event belonging to a specific country
 * being rolled up into the given country's bucket.</p>
 *
 * <p>Given an event that contains a "country" field, an implementation of Dimension
 * might look something like:</p>
 *
 * <pre>
 * {@literal Dimension<MyEvent> protocol = new Dimension<MyEvent>()} {
 *     public String getDimension(MyEvent req) { return req.getCountry(); }
 * }
 * </pre>
 *
 * @param <Input> The type of Event this dimension operates upon.
 */
public interface Dimension<Input> {

    /**
     * A method to extract the value of a dimension from an event.
     *
     * @param input The Event from which we're extracting a dimension value.
     * @return A String representing the value of this Dimension.
     */
    public String getDimension(Input input);

}
