package pl.interview.dh.tasks.report;

import pl.interview.dh.Utils;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(ReportTask.class.getName());

    private final String path;
    private final String content;

    public ReportTask(final String path, final String content) {
        this.path = path;
        this.content = content;
    }

    @Override
    public void run() {
        LOG.log(Level.WARNING, "Running report task for path {0}", new Object[]{path});
        Utils.writeFile(path, String.format("%s%s%s", new Date(), Utils.NEW_LINE, content));
    }
}
