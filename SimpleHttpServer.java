import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);

    public static void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/api", new ApiHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port 8081");
        } catch (IOException e) {
            logger.error("Server start error", e);
        }
    }

    public static void main(String[] args) {
        SimpleHttpServer.start();
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                saveToDatabase(requestBody);
                String response = "{\"message\":\"数据已接收并存储。\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private void saveToDatabase(String data) {
            String url = "jdbc:mysql://localhost:3306/form_data";
            String user = "root";
            String password = "your_mysql_password";  // 将此替换为你的 MySQL 密码

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                String sql = "INSERT INTO submissions (data) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, data);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.error("Database error", e);
            }
        }
    }
}
