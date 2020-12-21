package pl.edu.pw.aasd;

import com.sun.net.httpserver.HttpServer;
import pl.edu.pw.aasd.promise.Promise;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpPingServer {
    private HttpServer server = null;
    private int socketNum = 8000;

    public HttpPingServer() {
        while (this.server == null) {
            try {
                var socket = new InetSocketAddress(this.socketNum);

                this.server = HttpServer.create(socket, 0);
                this.server.setExecutor(null);
                this.server.start();
            } catch (Exception e) {
                this.socketNum++;
            }
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
            System.out.println(srvPath);
            var is = getClass().getClassLoader().getResourceAsStream(resPath);
            assert is != null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }));
    }

    public void handle(String path, PingHandler handler) {
        this.server.createContext(path, httpExchange -> {
            try {
                var reqBody = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                var res = handler.handle(reqBody).get();

                httpExchange.sendResponseHeaders(200, res.getBytes().length);

                var os = httpExchange.getResponseBody();
                os.write(res.getBytes());
                os.close();
            } catch (Throwable throwable) {
                httpExchange.sendResponseHeaders(500, 0);
                httpExchange.getResponseBody().close();

                throwable.printStackTrace();
            }
        });
    }


}
