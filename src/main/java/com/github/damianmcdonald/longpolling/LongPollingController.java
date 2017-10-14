package com.github.damianmcdonald.longpolling;

import org.fluttercode.datafactory.impl.DataFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class LongPollingController {

    private static final DataFactory DATA_FACTORY = new DataFactory();
    public static final Map<Long, String> EVENT_DATA = new ConcurrentHashMap<Long, String>(100);
    private static final Logger LOGGER = Logger.getLogger(LongPollingController.class.getName());

    @GET
    @Path("/register/{dossierId}")
    public void registerClient(@PathParam("dossierId") final long dossierId, @Suspended final AsyncResponse asyncResponse) {
        LOGGER.log(Level.INFO, "Registering client for dossier id: " + dossierId);
        // Add paused http requests to event queue
        LongPollingEventSimulator.LONG_POLLING_QUEUE.add(new LongPollingSession(dossierId, asyncResponse));
    }

    @POST
    @Path("/simulate/{dossierId}")
    public void simulateEvent(@PathParam("dossierId") final long dossierId) {
        // simulate an event for this dossierId by adding data to the event data map
        // this event data will be sent to registered clients via the LongPollingEventSimulator
        if (!EVENT_DATA.containsKey(dossierId)) {
            LOGGER.log(Level.INFO, "Adding event data for dossier id: " + dossierId);
            EVENT_DATA.put(dossierId, randomData());
        }
    }

    // generate random data to be sent to the UI
    private static String randomData() {
        final String address = DATA_FACTORY.getAddress() + "," + DATA_FACTORY.getCity() + "," + DATA_FACTORY.getNumberText(5);
        final String business = DATA_FACTORY.getBusinessName();
        return String.format("%s located at %s", business, address);
    }
}
