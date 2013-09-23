package com.cscotta.deschutes.api;

import java.util.List;
import java.util.Map;

public interface OutputListener<Input> {

    public void update(List<Long> remove,
                       Map<Long, Map<String, ConcurrentRollup<Input>>> insert);

}
