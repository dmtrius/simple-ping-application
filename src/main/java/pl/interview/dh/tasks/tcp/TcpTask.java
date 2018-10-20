package pl.interview.dh.tasks.tcp;


import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import pl.interview.dh.Main;
import pl.interview.dh.Utils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpTask implements Callable<Integer> {

    private static final Logger LOG = Logger.getLogger(TcpTask.class.getName());

    private static final String DELIMITER = ":";
    private static final String MSEC = "ms";

    private final String host;
    private final String timeout;

    public TcpTask(final String host, final String timeout) {
        this.host = host;
        this.timeout = timeout;
    }

    @Override
    public Integer call() {
        LOG.log(Level.WARNING, "start TcpTask @ {0} for host {1}", new Object[]{new Date(), host});

        final StringBuilder result = new StringBuilder();
        result.append(host).append(DELIMITER);

        final long requestStart = System.currentTimeMillis();
        int status = 0;
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(Integer.parseInt(timeout))
                .setConnectTimeout(Integer.parseInt(timeout))
                .build();
        final HttpGet httpget = new HttpGet(host);
        httpget.setConfig(requestConfig);

        try (final CloseableHttpClient httpclient = HttpClients.createDefault();
             final CloseableHttpResponse response = httpclient.execute(httpget)) {
            final StatusLine statusLine = response.getStatusLine();
            final long requestDuration = System.currentTimeMillis() - requestStart;
            if (null != statusLine) {
                status = statusLine.getStatusCode();
            }
            result.append(status).append(DELIMITER).append(requestDuration).append(MSEC);

            // REPORT
            final String logPath = Main.getProperties("report.tcp.path");
            Utils.appendStringEnd(result);
            Utils.startReportTask(result, logPath, host);
        } catch (UnknownHostException e) {
            LOG.log(Level.WARNING, "UNKNOWN HOST: {0}", e.getMessage());
            return 0;
        } catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}
