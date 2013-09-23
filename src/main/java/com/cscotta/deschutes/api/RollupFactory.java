package com.cscotta.deschutes.api;

/**
 * An interface defining an object that returns new rollup objects.
 */
public interface RollupFactory<T> {

    public ConcurrentRollup<T> buildRollup(long timestamp);

}
