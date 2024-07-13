import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.logging.Logger;

public class SimpleHttpServer {
private static final Logger logger = Logger.getLogger(SimpleHttpServer.class.getName());

public static void start() {
try {
HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
server.createContext("/api", new ApiHandler());
server.setExecutor(null);
server.start();
System.out.println("Server started on port 8081");
} catch (IOException e) {
logger.severe("Failed to start server: " + e.getMessage());
}
}

static class ApiHandler implements HttpHandler {
@Override
public void handle(HttpExchange exchange) throws IOException {
String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
storeData(requestBody);
String response = "{\"message\": \"Data received and stored.\"}";
exchange.getResponseHeaders().set("Content-Type", "application/json");
exchange.sendResponseHeaders(200, response.length());
OutputStream os = exchange.getResponseBody();
os.write(response.getBytes());
os.close();
}

private void storeData(String data) {
String url = "jdbc:sqlite:identifier.sqlite";
String sql = "INSERT INTO submissions(data) VALUES(?)";

try (Connection conn = DriverManager.getConnection(url);
PreparedStatement pstmt = conn.prepareStatement(sql)) {
pstmt.setString(1, data);
pstmt.executeUpdate();
} catch (SQLException e) {
logger.severe("SQL error: " + e.getMessage());
}
}
}
}
