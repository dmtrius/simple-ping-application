package pl.interview.dh;


import org.apache.commons.lang3.StringUtils;
import pl.interview.dh.tasks.report.ReportTask;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private static final String DELIMITER = "-";

    public static final String NEW_LINE = "\r\n";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String SLASH = "/";

    public static void writeFile(final String filePath, final String content) {
        try {
            final File logFile = new File(filePath).getCanonicalFile();
            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            final Path path = Paths.get(logFile.toURI());
            Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "cannot write to file {0}", new Object[]{filePath});
        }
    }

    public static void startReportTask(final StringBuilder content, final String logPath) {
        startReportTask(content, logPath, null);
    }

    public static void startReportTask(final StringBuilder buffer, final String logPath, final String host) {
        try {
            final String logPrefix = Main.getProperty("report.logs.prefix");
            final StringBuilder filePath = new StringBuilder();
            filePath
                    .append(DOT)
                    .append(logPrefix);
            if (null != host) {
                filePath
                        .append(Utils.removeProtocol(host))
                        .append(DOT);
            }
            filePath
                    .append(logPath);

            final String path = new File(filePath.toString()).getCanonicalPath();
            new Thread(new ReportTask(path, buffer.toString())).start();
        } catch (IOException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "cannot get logs path: {0}", new Object[]{logPath});
        }
    }

    public static String tail(final File file, final int lines) {
        try (RandomAccessFile fileHandler = new java.io.RandomAccessFile(file, "r")) {
            long fileLength = fileHandler.length() - 1;
            final StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; --filePointer) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                // LF
                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                    // CR
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength - 1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append((char) readByte);
            }

            return sb.reverse().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void exec(final String[] commands, final String host, final String logPath) {
        final Runtime rt = Runtime.getRuntime();
        Process proc;
        try {
            proc = rt.exec(commands);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "ERROR: no runtime exec for host {0}", new Object[]{host});
            return;
        }

        if (null == proc.getInputStream()) {
            LOG.log(Level.WARNING, "ERROR: no input stream for host {0}", new Object[]{host});
            return;
        }

        String buffer;
        StringBuilder result = new StringBuilder();
        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            while (null != (buffer = stdInput.readLine())) {
                result.append(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // REPORT
        if (result.length() > 0) {
            Utils.appendStringEnd(result);
            Utils.startReportTask(result, logPath, host);
        }

        result.delete(0, result.length());

        // ERRORS
        try (BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
            while ((buffer = stdError.readLine()) != null) {
                result.append(buffer);
                System.out.println(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // REPORT ERRORS IF ANY
        if (result.length() > 0) {
            Utils.appendStringEnd(result);
            Utils.startReportTask(result, logPath, host);
        }
    }

    public static void appendStringEnd(final StringBuilder buffer) {
        buffer.append(NEW_LINE);
        buffer.append(StringUtils.repeat(DELIMITER, 80));
        buffer.append(NEW_LINE);
    }

    public static String removeProtocol(final String host) {
        return host.replaceFirst("^(https?://)", StringUtils.EMPTY);
    }

    public static String prepareReportLine(final String line) {
        String result = line.replaceAll(DELIMITER, StringUtils.EMPTY);
        result = result.replaceAll("^\\s+", StringUtils.EMPTY);
        result = result.replaceAll("\\s+$", StringUtils.EMPTY);
        return result;
    }
}
