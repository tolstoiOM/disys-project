//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################
//packages & imports



package org.example;

import com.rabbitmq.client.*;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################
//class



public class MessageReceiver {

    //#########################################################################################################################
    //variables
    private static final String QUEUE_NAME = "energy.queue";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/energydb";
    private static final String DB_USER = "disysuser";
    private static final String DB_PASSWORD = "disyspw";


    //#########################################################################################################################
    //main
    public static void main(String[] args) throws Exception {
        //build connection to RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        com.rabbitmq.client.Connection rabbitConnection = factory.newConnection();
        Channel channel = rabbitConnection.createChannel();

        //declare queue
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("🔍 Warte auf Nachrichten in '" + QUEUE_NAME + "'...");

        //connection to db
        java.sql.Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        //try to receive and proceed message
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageStr = new String(delivery.getBody(), StandardCharsets.UTF_8);
            try {
                JSONObject messageJson = new JSONObject(messageStr);
                System.out.println("\n📩 Neue Nachricht empfangen: " + messageJson);
                processMessage(dbConnection, messageJson);
            } catch (Exception e) {
                System.err.println("⚠️ Fehler beim Verarbeiten der Nachricht: " + e.getMessage());
            }
        };

        //receive async message
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    //#########################################################################################################################
    //methods

    //process received message
    static void processMessage(java.sql.Connection dbConnection, JSONObject messageJson) throws SQLException {
        //local variables
        String type = messageJson.getString("type");
        String association = messageJson.getString("association");
        double kwh = messageJson.getDouble("kwh");
        LocalDateTime datetime = LocalDateTime.parse(messageJson.getString("datetime"), DateTimeFormatter.ISO_DATE_TIME);

        //calculate hours
        LocalDateTime hour = datetime.withMinute(0).withSecond(0).withNano(0);

        //table 'UsageService' refreshen
        updateUsageService(dbConnection, hour, type, association, kwh);

        //table `CurrentPercentageService` refreshen
        updateCurrentPercentageService(dbConnection, hour);
    }

    //update table 'UsageService'
    static void updateUsageService(java.sql.Connection dbConnection, LocalDateTime hour, String type, String association, double kwh) throws SQLException {
        //query statement
        String query = "INSERT INTO UsageService (hour, community_produced, community_used, grid_used) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (hour) DO UPDATE SET " +
                "community_produced = CASE WHEN ? = 'PRODUCER' THEN UsageService.community_produced + EXCLUDED.community_produced ELSE UsageService.community_produced END, " +
                "community_used = CASE WHEN ? = 'USER' THEN LEAST(UsageService.community_produced, UsageService.community_used + EXCLUDED.community_used) ELSE UsageService.community_used END, " +
                "grid_used = CASE WHEN ? = 'USER' THEN UsageService.grid_used + GREATEST(0, (UsageService.community_used + EXCLUDED.community_used) - UsageService.community_produced) ELSE UsageService.grid_used END";

        //set parameters of the query and execute
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(hour));

            stmt.setDouble(2, type.equals("PRODUCER") ? kwh : 0);
            stmt.setDouble(3, type.equals("USER") ? kwh : 0);
            stmt.setDouble(4, 0); // Grid usage starts at 0

            stmt.setString(5, type);
            stmt.setString(6, type);
            stmt.setString(7, type);

            stmt.executeUpdate();
        }
    }

    //update table 'CurrentPercentageService'
    private static void updateCurrentPercentageService(java.sql.Connection dbConnection, LocalDateTime hour) throws SQLException {
        //query statement
        String query = "INSERT INTO CurrentPercentageService (hour, community_depleted, grid_portion) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (hour) DO UPDATE SET " +
                "community_depleted = (SELECT CASE WHEN SUM(community_produced) > 0 THEN SUM(community_used) / SUM(community_produced) * 100 ELSE 0 END FROM UsageService WHERE hour = ?), " +
                "grid_portion = (SELECT CASE WHEN SUM(grid_used + community_used) > 0 THEN SUM(grid_used) / (SUM(grid_used) + SUM(community_used)) * 100 ELSE 0 END FROM UsageService WHERE hour = ?)";

        //set parameters of the query and execute
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(hour));

            stmt.setDouble(2, 0); // Initialvalue
            stmt.setDouble(3, 0);

            stmt.setTimestamp(4, Timestamp.valueOf(hour)); // for the subquery
            stmt.setTimestamp(5, Timestamp.valueOf(hour)); // for the subquery

            stmt.executeUpdate();
        }
    }
}



//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################