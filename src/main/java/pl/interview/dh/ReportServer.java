package pl.interview.dh;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportServer {

    private static final Logger LOG = Logger.getLogger(ReportServer.class.getName());

    public static void main(String... args) throws IOException {
        try (final ServerSocket listener = new ServerSocket(9090)) {
            while (true) {
                try (final Socket socket = listener.accept()) {
                    LOG.log(Level.INFO, "report");
                    final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(new Date().toString());
                }
            }
        }
    }
}
