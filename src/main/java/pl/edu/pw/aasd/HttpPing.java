package pl.edu.pw.aasd;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class HttpPing {
    private HttpServer server = null;
    private int socketNum = 8000;

    public HttpPing() {
        try {
            InetSocketAddress socket = null;
            while (socket == null) {
                try {
                    socket = new InetSocketAddress(8000);
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

    interface PingHandler {
        String handle(String body);
    }

    public void handle(String path, PingHandler handler) {
        this.server.createContext(path, httpExchange -> {
            var res = handler.handle("");
//            httpExchange.getResponseBody().write(res);
            httpExchange.close();
        });
    }
}
