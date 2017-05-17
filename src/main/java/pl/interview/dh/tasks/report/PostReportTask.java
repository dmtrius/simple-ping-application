package pl.interview.dh.tasks.report;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import pl.interview.dh.Main;
import pl.interview.dh.Utils;
import pl.interview.dh.dto.ReportDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.interview.dh.Utils.COMMA;
import static pl.interview.dh.Utils.DOT;


public class PostReportTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(PostReportTask.class.getName());

    private static final String REPORT_URL = Main.getProperty("report.url");

    private static final int FILE_LINES = 3;

    @Override
    public void run() {
        LOG.log(Level.WARNING, "start PostReportTask @ {0}", new Object[]{new Date()});

        try {
            final List<ReportDto> dtoList = getReports();

            final String logPath = Main.getProperty("report.post.path");
            final ObjectMapper mapper = new ObjectMapper();
            for (final ReportDto dto : dtoList) {
                final String json = mapper.writeValueAsString(dto);

                final boolean result = postReport(json, logPath);

                if (result) {
                    final StringBuilder content = new StringBuilder(json);
                    Utils.appendStringEnd(content);
                    Utils.startReportTask(content, logPath);
                }
            }
            LOG.log(Level.WARNING, "REPORT COMPLETE");

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "cannot parse object to json");
        }
    }

    private boolean postReport(final String json, final String logPath) {
        try {
            final HttpClient client = HttpClientBuilder.create().build();
            final HttpPost post = new HttpPost(REPORT_URL);
            final StringEntity postingString = new StringEntity(json);
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
            client.execute(post);
            return true;
        } catch (IOException e) {
            Utils.startReportTask(new StringBuilder(e.getMessage()), logPath);
            return false;
        }
    }

    private List<ReportDto> getReports() {
        final List<ReportDto> dtos = new ArrayList<>();

        final String logPrefix = Main.getProperty("report.logs.prefix");
        String basePath;
        try {
            basePath = new File(DOT + logPrefix).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            // cannot get logs base directory
            return dtos;
        }

        final String hostsString = Main.getProperty("hosts");
        final String[] hosts = hostsString.split(COMMA);

        final String icmpPath = Main.getProperty("report.icmp.path");
        final String tcpPath = Main.getProperty("report.tcp.path");
        final String tracePath = Main.getProperty("report.trace.path");

        for (final String host : hosts) {
            final ReportDto dto = new ReportDto();
            dto.setHost(host);

            String path = getPath(basePath, host, icmpPath);
            File file = new File(path);
            String result = Utils.tail(file, FILE_LINES);
            dto.setIcmp_ping(Utils.prepareReportLine(result));

            path = getPath(basePath, host, tcpPath);
            file = new File(path);
            result = Utils.tail(file, FILE_LINES);
            dto.setTcp_ping(Utils.prepareReportLine(result));

            path = getPath(basePath, host, tracePath);
            file = new File(path);
            result = Utils.tail(file, FILE_LINES);
            dto.setTrace(Utils.prepareReportLine(result));

            dtos.add(dto);
        }

        return dtos;
    }

    private String getPath(final String base, final String host, final String logPath) {
        return base + Utils.SLASH + Utils.removeProtocol(host) + DOT + logPath;
    }
}
