package pl.edu.pw.aasd;

import com.sun.net.httpserver.HttpServer;
import pl.edu.pw.aasd.promise.Promise;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpPingServer {
    private HttpServer server = null;
    private int socketNum = 8000;

    public HttpPingServer() {
        try {
            InetSocketAddress socket = null;
            while (socket == null) {
                try {
                    socket = new InetSocketAddress(this.socketNum);
                } catch (Exception e) {
                    this.socketNum++;
                }
            }

            this.server = HttpServer.create(socket, 0);
            this.server.setExecutor(null);
            this.server.start();

        } catch (Exception ignored) {

        }
    }

    public int getSocketNum() {
        return socketNum;
    }

    public interface PingHandler {
        Promise<String> handle(String body) throws Throwable;
    }

    public void handleFile(String srvPath, String resPath) {

        this.handle(srvPath, body -> new Promise<String>().fulfillInAsync(() -> {
            var is = getClass().getClassLoader().getResourceAsStream(resPath);
            assert is != null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }));
    }

    public void handle(String path, PingHandler handler) {
        this.server.createContext(path, httpExchange -> {
            try {
                var res = handler.handle("").get();

                httpExchange.sendResponseHeaders(200, res.getBytes().length);

                var os = httpExchange.getResponseBody();
                os.write(res.getBytes());
                os.close();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }


}
