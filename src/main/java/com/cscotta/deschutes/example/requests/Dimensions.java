package com.cscotta.deschutes.example.requests;

import java.util.Map;
import com.cscotta.deschutes.api.Dimension;
import com.google.common.collect.ImmutableMap;

public class Dimensions {

    public static final Dimension<RequestEvent> protocol = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return req.getProtocol().toString(); }
    };

    public static final Dimension<RequestEvent> locale = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return req.getLocale(); }
    };

    public static final Dimension<RequestEvent> referrer = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return req.getReferrer(); }
    };

    public static final Dimension<RequestEvent> operationType = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return req.getOperationType().toString(); }
    };

    public static final Dimension<RequestEvent> uri = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return req.getUri(); }
    };

    public static final Dimension<RequestEvent> clientIp = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return Integer.toString(req.getClientIp()); }
    };

    public static final Dimension<RequestEvent> serverIp = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return Integer.toString(req.getServerIp()); }
    };

    public static final Dimension<RequestEvent> responseCode = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return Integer.toString(req.getResponseCode()); }
    };

    public static final Dimension<RequestEvent> userAgent = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return req.getUserAgent(); }
    };

    public static final Dimension<RequestEvent> userId = new Dimension<RequestEvent>() {
        public String getDimension(RequestEvent req) { return Integer.toString(req.getUserId()); }
    };

    public static final Map<String, Dimension<RequestEvent>> map =
        new ImmutableMap.Builder<String, Dimension<RequestEvent>>().
            put("protocol", protocol).
            put("locale", locale).
            put("referrer", referrer).
            put("operationType", operationType).
            put("uri", uri).
            put("clientIp", clientIp).
            put("serverIp", serverIp).
            put("responseCode", responseCode).
            put("userAgent", userAgent).
            put("userId", userId).build();
}
