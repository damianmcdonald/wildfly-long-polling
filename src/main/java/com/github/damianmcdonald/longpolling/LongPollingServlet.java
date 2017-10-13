package com.github.damianmcdonald.longpolling;

import org.fluttercode.datafactory.impl.DataFactory;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(value = "/LongPollingServlet", asyncSupported = true)
public class LongPollingServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LongPollingServlet.class.getName());
    private static final DataFactory DATA_FACTORY = new DataFactory();
    private static final BlockingQueue<AsyncContext> queue = new ArrayBlockingQueue<AsyncContext>(20000);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    static {
        // Simulate a new event being fired
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(LongPollingServlet::newEvent, 0, 3, TimeUnit.SECONDS);
    }

    // Add paused http requests to event queue
    public static void addToWaitingList(final AsyncContext c) {
        queue.add(c);
    }

    // generate random data to be sent to the UI
    private static String randomData() {
        final String address = DATA_FACTORY.getAddress()+","+DATA_FACTORY.getCity()+","+DATA_FACTORY.getNumberText(5);
        final String business = DATA_FACTORY.getBusinessName();
        return String.format("%s located at %s", business, address);
    }

    // Simulated event handler
    private static void newEvent() {
        LOGGER.log(Level.INFO, "New event fired!");
        List<AsyncContext> clients = new ArrayList<AsyncContext>(queue.size());
        queue.drainTo(clients);
        LOGGER.log(Level.INFO, "Clients size after draining: " + clients.size());
        clients.parallelStream().forEach((AsyncContext ac) -> {
            try {
                LOGGER.log(Level.INFO, "Writing response to client: " + ac);
                // resume and write data to the paused Http request
                ac.getResponse().getWriter().println(randomData());
                // complete and close the Http request
                ac.complete();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        LOGGER.log(Level.INFO, "Adding request to the queue: " + req);
        addToWaitingList(req.startAsync());
    }

}