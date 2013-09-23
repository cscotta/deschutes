package com.cscotta.deschutes.api;

/**
 * <p>Events encapsulate the objects in our Stream to be aggregated.</p>
 *
 * <p>The only method that Events must implement is getTimestamp(), which must
 * return an epoch timestamp as a long (represented in milliseconds). Fields
 * exposed on Event objects are used by your Dimension and ConcurrentRollup
 * implementations to segment events and aggregate their values.</p>
 *
 * <p>An example implementation might look something like:</p>
 *
 * <pre>
 *     public class MyEvent implements Event {
 *         private final long timestamp;
 *         private final String country;
 *         private final int pageviewCount;
 *
 *         public MyEvent(long timestamp, String country, int pageviewCount) {
 *             this.timestamp = timestamp;
 *             this.country = country;
 *             this.pageviewCount = pageviewCount;
 *         }
 *
 *         {@literal @Override}
 *         public long getTimestamp()  { return timestamp;     }
 *         public String getCountry()  { return country;       }
 *         public long pageviewCount() { return pageviewCount; }
 *     }
 * </pre>
 */
public interface Event {

    /**
     * Retrieve the timestamp of an event, represented as a long in
     * milliseconds since epoch.
     *
     * @return The timestamp of this event, represented as a long in
     * milliseconds since epoch.
     */
    public long getTimestamp();

}

