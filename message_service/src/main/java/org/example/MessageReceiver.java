package org.example;

import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageReceiver {

    private static final String QUEUE_NAME = "energy.queue";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/energydb";
    private static final String DB_USER = "disysuser";
    private static final String DB_PASSWORD = "disyspw";

    public static void main(String[] args) throws Exception {
        // Verbindung zu RabbitMQ aufbauen
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        com.rabbitmq.client.Connection rabbitConnection = factory.newConnection();
        Channel channel = rabbitConnection.createChannel();

        // Queue deklarieren
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("🔍 Warte auf Nachrichten in '" + QUEUE_NAME + "'...");

        // Datenbankverbindung herstellen
        java.sql.Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageStr = new String(delivery.getBody(), StandardCharsets.UTF_8);

            try {
                JSONObject messageJson = new JSONObject(messageStr);
                System.out.println("\n📩 Neue Nachricht empfangen: " + messageJson);

                // Nachricht verarbeiten
                processMessage(dbConnection, messageJson);

            } catch (Exception e) {
                System.err.println("⚠️ Fehler beim Verarbeiten der Nachricht: " + e.getMessage());
            }
        };

        // Nachrichten asynchron empfangen
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    private static void processMessage(java.sql.Connection dbConnection, JSONObject messageJson) throws SQLException {
        String type = messageJson.getString("type");
        String association = messageJson.getString("association");
        double kwh = messageJson.getDouble("kwh");
        LocalDateTime datetime = LocalDateTime.parse(messageJson.getString("datetime"), DateTimeFormatter.ISO_DATE_TIME);

        // Stunde berechnen
        LocalDateTime hour = datetime.withMinute(0).withSecond(0).withNano(0);

        // Tabelle `UsageService` aktualisieren
        updateUsageService(dbConnection, hour, type, association, kwh);

        // Tabelle `CurrentPercentageService` aktualisieren
        updateCurrentPercentageService(dbConnection, hour);
    }

    private static void updateUsageService(java.sql.Connection dbConnection, LocalDateTime hour, String type, String association, double kwh) throws SQLException {
        String query = "INSERT INTO UsageService (hour, community_produced, community_used, grid_used) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (hour) DO UPDATE SET " +
                "community_produced = CASE WHEN EXCLUDED.community_produced > 0 THEN UsageService.community_produced + EXCLUDED.community_produced ELSE UsageService.community_produced END, " +
                "community_used = CASE WHEN EXCLUDED.community_used > 0 THEN UsageService.community_used + EXCLUDED.community_used ELSE UsageService.community_used END, " +
                "grid_used = CASE WHEN EXCLUDED.grid_used > 0 THEN UsageService.grid_used + EXCLUDED.grid_used ELSE UsageService.grid_used END";

        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(hour));
            stmt.setDouble(2, type.equals("PRODUCER") ? kwh : 0);
            stmt.setDouble(3, type.equals("USER") ? kwh : 0);
            stmt.setDouble(4, type.equals("GRID") ? kwh : 0); // Grid usage aktualisieren

            // Debugging-Ausgabe
            System.out.println("Update UsageService: hour=" + hour + ", type=" + type + ", kWh=" + kwh);
            System.out.println("Grid Used: " + (type.equals("GRID") ? kwh : 0));

            stmt.executeUpdate();
        }
    }

    private static void updateCurrentPercentageService(java.sql.Connection dbConnection, LocalDateTime hour) throws SQLException {
        String query = "INSERT INTO CurrentPercentageService (hour, community_depleted, grid_portion) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (hour) DO UPDATE SET " +
                "community_depleted = (SELECT CASE WHEN SUM(community_produced) > 0 THEN SUM(community_used) / SUM(community_produced) * 100 ELSE 0 END FROM UsageService WHERE hour = ?), " +
                "grid_portion = (SELECT CASE WHEN SUM(community_used + grid_used) > 0 THEN SUM(grid_used) / SUM(community_used + grid_used) * 100 ELSE 0 END FROM UsageService WHERE hour = ?)";

        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(hour));
            stmt.setDouble(2, 0); // Initialwerte
            stmt.setDouble(3, 0);
            stmt.setTimestamp(4, Timestamp.valueOf(hour)); // Für die Unterabfrage
            stmt.setTimestamp(5, Timestamp.valueOf(hour)); // Für die Unterabfrage

            stmt.executeUpdate();
        }
    }
}