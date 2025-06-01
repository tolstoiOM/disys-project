import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageReceiver {

    private static final String QUEUE_NAME = "energy.queue";

    public static void main(String[] args) throws Exception {
        // Verbindung zu RabbitMQ aufbauen (localhost)
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // oder IP-Adresse des RabbitMQ-Servers

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Queue deklarieren (falls nicht vorhanden)
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("🔍 Warte auf Nachrichten in '" + QUEUE_NAME + "'...");

        // Nachrichtenempfänger
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageStr = new String(delivery.getBody(), StandardCharsets.UTF_8);

            try {
                JSONObject messageJson = new JSONObject(messageStr);

                System.out.println("\n📩 Neue Nachricht empfangen:");
                System.out.println("Typ: " + messageJson.getString("type"));
                System.out.println("Zugehörigkeit: " + messageJson.getString("association"));
                System.out.println("kWh: " + messageJson.getDouble("kwh"));
                System.out.println("Zeitpunkt: " + messageJson.getString("datetime"));

            } catch (Exception e) {
                System.err.println("⚠️ Fehler beim Parsen der Nachricht: " + e.getMessage());
            }
        };

        // Nachrichten asynchron empfangen
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }
}