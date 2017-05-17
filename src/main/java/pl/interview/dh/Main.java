package pl.interview.dh;

import pl.interview.dh.tasks.Runner;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String OS_NAME = "os.name";
    private static final String WINDOWS = "Windows";

    private static volatile Properties properties = new Properties();

    public static void main(String... args) {
        try {
            getProperties();
        } catch (Exception e) {
            throw new RuntimeException("could not load config");
        }

        final Long delay = Long.valueOf(getProperty("delay"));
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runner(), 1, delay, TimeUnit.SECONDS);
    }

    public static String getProperty(final String name) {
        return properties.getProperty(name);
    }

    private static void getProperties() throws IOException {
        properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
        if (isWindows()) {
            properties.put("ping.count_param", getProperty("ping.count_param.windows"));
            properties.put("trace.command", getProperty("trace.command.windows"));
        } else {
            properties.put("ping.count_param", getProperty("ping.count_param.nix"));
            properties.put("trace.command", getProperty("trace.command.nix"));
        }
    }

    private static boolean isWindows() {
        return System.getProperty(OS_NAME).startsWith(WINDOWS);
    }
}
