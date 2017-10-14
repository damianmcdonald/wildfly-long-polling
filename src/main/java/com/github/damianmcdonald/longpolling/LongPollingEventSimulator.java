package com.github.damianmcdonald.longpolling;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LongPollingEventSimulator {

    private static final Logger LOGGER = Logger.getLogger(LongPollingEventSimulator.class.getName());
    public static final BlockingQueue<LongPollingSession> LPS_QUEUE = new ArrayBlockingQueue<LongPollingSession>(100);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    static {
        // Simulates a new event being fired every X seconds
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(LongPollingEventSimulator::newEvent, 1, 5, TimeUnit.SECONDS);
    }


    // Simulated event handler
    private static void newEvent() {
        LOGGER.log(Level.INFO, "New event fired!");

        // track the dossier ids that have event data so that we can clear event data
        // once all responses have been sent to the long polling clients
        final Set<Long> dossierIds = new HashSet<Long>();

        // determine which long polling clients are registered for dossiers that have event data
        final List<LongPollingSession> lpsClients = new ArrayList<LongPollingSession>(LPS_QUEUE.size());
        final Iterator<LongPollingSession> itr = LPS_QUEUE.iterator();
        while (itr.hasNext()) {
            final LongPollingSession lps = itr.next();
            if (LongPollingController.EVENT_DATA.containsKey(lps.getDossierId())) {
                lpsClients.add(lps);
                dossierIds.add(lps.getDossierId());
                itr.remove();
            }
        }

        // send the event data to the long polling clients
        lpsClients.stream()
                .forEach((LongPollingSession lps) -> {
                    LOGGER.log(Level.INFO, "Checking if we have a registered long-polling client for dossierId: " + lps.getDossierId());
                    dossierIds.add(lps.getDossierId());
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

        // clean up the global event map if we have already sent client responses
        final Iterator<Map.Entry<Long, String>> it = LongPollingController.EVENT_DATA.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Long, String> pair = it.next();
            if (dossierIds.contains(pair.getKey())) {
                it.remove();
            }
        }
    }
}
