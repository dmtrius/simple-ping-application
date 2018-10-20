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
            final long delay = Long.parseLong(getProperties("delay"));
            final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runner(), 1, delay, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("could not load config or system error");
        }
    }

    private static void getProperties() throws IOException {
        properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
        if (isWindows()) {
            properties.put("ping.count_param", getProperties("ping.count_param.windows"));
            properties.put("trace.command", getProperties("trace.command.windows"));
        } else {
            properties.put("ping.count_param", getProperties("ping.count_param.nix"));
            properties.put("trace.command", getProperties("trace.command.nix"));
        }
    }

    public static String getProperties(final String name) {
        return properties.getProperty(name);
    }

    private static boolean isWindows() {
        return System.getProperty(OS_NAME).startsWith(WINDOWS);
    }
}
