package com.github.damianmcdonald.longpolling;


import javax.ws.rs.container.AsyncResponse;

public class LongPollingSession {

    private final long dossierId;
    private final AsyncResponse asyncResponse;

    public LongPollingSession(final long dossierId, final AsyncResponse asyncResponse) {
        this.dossierId = dossierId;
        this.asyncResponse = asyncResponse;
    }

    public long getDossierId() {
        return dossierId;
    }

    public AsyncResponse getAsyncResponse() {
        return asyncResponse;
    }
}
