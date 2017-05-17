package pl.interview.dh.tasks;


import pl.interview.dh.Main;
import pl.interview.dh.Utils;
import pl.interview.dh.tasks.icmp.IcmpTask;
import pl.interview.dh.tasks.report.PostReportTask;
import pl.interview.dh.tasks.tcp.TcpTask;
import pl.interview.dh.tasks.trace.TraceTask;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Runner implements Runnable {

    private static final Logger LOG = Logger.getLogger(Runner.class.getName());

    private volatile boolean isWait = true;

    @Override
    public void run() {
        LOG.log(Level.WARNING, "start runner @ {0}", new Object[]{new Date()});

        final String hostsString = Main.getProperty("hosts");
        final String[] hosts = hostsString.split(Utils.COMMA);

        Collection<Callable<Integer>> services = new LinkedList<>();

        // ICMP task
        final String countParam = Main.getProperty("ping.count_param");
        final String count = Main.getProperty("ping.count");
        for (final String host : hosts) {
            services.add(new IcmpTask(Utils.removeProtocol(host), countParam, count));
        }

        // TRACE task
        final String traceCommand = Main.getProperty("trace.command");
        for (final String host : hosts) {
            services.add(new TraceTask(Utils.removeProtocol(host), traceCommand));
        }

        // TCP task
        final String tcpTimeout = Main.getProperty("tcp.timeout");
        for (final String host : hosts) {
            services.add(new TcpTask(host, tcpTimeout));
        }

        // execute tasks
        final ExecutorService executor = Executors.newCachedThreadPool();
        services.forEach(executor::submit);
        executor.shutdown();

        final String timeout = Main.getProperty("tcp.timeout");

        while (isWait) {
            try {
                // just guess - 2 * hosts.length * tcp.timeout
                final int magic = 1;
                while(true) {
                    isWait = !executor.awaitTermination(magic * hosts.length * Integer.parseInt(timeout), TimeUnit.MILLISECONDS);
                    if (!isWait) {
                        new Thread(new PostReportTask(), "POST_REPORT: " + new Date()).start();
                        executor.shutdownNow();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
