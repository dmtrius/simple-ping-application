package pl.interview.dh.tasks.icmp;

import pl.interview.dh.Main;
import pl.interview.dh.Utils;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IcmpTask implements Callable<Integer> {

    private static final Logger LOG = Logger.getLogger(IcmpTask.class.getName());

    private static final String PING = "ping";

    private final String countParam;
    private final String count;
    private final String host;

    public IcmpTask(final String host, final String countParam, final String count) {
        this.host = host;
        this.countParam = countParam;
        this.count = count;
    }

    @Override
    public Integer call() {
        LOG.log(Level.WARNING, "start IcmpTask @ {0} for host {1}", new Object[]{new Date(), host});

        final String[] commands = {PING, countParam, count, host};
        final String logPath = Main.getProperty("report.icmp.path");
        Utils.exec(commands, host, logPath);

        return 1;
    }
}
