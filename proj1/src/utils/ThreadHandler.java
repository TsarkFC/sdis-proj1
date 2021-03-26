package utils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadHandler {
    public static void startMulticastThread(String mcast_addr, int mcast_port, List<String> messages){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Multicast multicastThread = new Multicast(mcast_port, mcast_addr, messages);
        executor.schedule(multicastThread,0, TimeUnit.SECONDS);
    }

}
