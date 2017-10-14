package com.github.damianmcdonald.longpolling;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LongPollingEventSimulator {

    private static final Logger LOGGER = Logger.getLogger(LongPollingEventSimulator.class.getName());
    public static final BlockingQueue<LongPollingSession> LONG_POLLING_QUEUE = new ArrayBlockingQueue<LongPollingSession>(100);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    static {
        // Simulates a new event being fired every X seconds
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(LongPollingEventSimulator::newEvent, 1, 5, TimeUnit.SECONDS);
    }

    // Simulated event handler
    private static void newEvent() {
        LOGGER.log(Level.INFO, "New event fired!");
        // send the event data to the long polling clients that are registered for specific dossier event data
        LONG_POLLING_QUEUE.stream()
                .filter(e -> LongPollingController.EVENT_DATA.containsKey(e.getDossierId()))
                .forEach((LongPollingSession lps) -> {
                    LOGGER.log(Level.INFO, "Checking if we have a registered long-polling client for dossierId: " + lps.getDossierId());
                    try {
                        LOGGER.log(Level.INFO, "Long-polling client found for dossierId: " + lps.getDossierId());
                        LOGGER.log(Level.INFO,
                                "Writing response to client: " + lps.getAsyncResponse() + " : "
                                        + LongPollingController.EVENT_DATA.getOrDefault(lps.getDossierId(), "Error: >>> Failed to retrieve data from event map!"));
                        lps.getAsyncResponse().
                                resume(LongPollingController.EVENT_DATA.getOrDefault(lps.getDossierId(), "Error: >>> Failed to retrieve data from event map!"));
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                });
        // remove long polling clients once events have been dispatched
        LONG_POLLING_QUEUE.removeIf(e -> LongPollingController.EVENT_DATA.containsKey(e.getDossierId()));
        // clear the event data queue once all events have been dispatched
        LongPollingController.EVENT_DATA.clear();
    }
}
