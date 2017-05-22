package pl.interview.dh.tasks.trace;


import pl.interview.dh.Main;
import pl.interview.dh.Utils;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TraceTask implements Callable<Integer> {

    private static final Logger LOG = Logger.getLogger(TraceTask.class.getName());

    private final String host;
    private final String command;

    public TraceTask(final String host, final String command) {
        this.host = host;
        this.command = command;
    }

    @Override
    public Integer call() {
        LOG.log(Level.WARNING, "start TraceTask @ {0} for host {1}", new Object[]{new Date(), host});

        final String logPath = Main.getProperties("report.trace.path");
        final String[] commands = {command, host};
        Utils.exec(commands, host, logPath);

        return 1;
    }
}
