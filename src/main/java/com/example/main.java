package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            logger.error("Failed to start server", e);
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String response = "{\"message\": \"Data received and stored.\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            storeData(requestBody);
        }

        private void storeData(String data) {
            String url = "jdbc:sqlite:/mnt/c/Users/Administrator/Pictures/DoDo/test.db";
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "INSERT INTO data (content) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, data);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.error("Failed to store data", e);
            }
        }
    }
}

