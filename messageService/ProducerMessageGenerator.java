import org.json.JSONObject;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ProducerMessageGenerator {

    private static final String API_KEY = "bfb6969caa254864a2a182551252905";
    private static final String LOCATION = "Korneuburg";
    private static final String QUEUE_NAME = "energy.queue";

    public static void main(String[] args) {
        // RabbitMQ Verbindung aufbauen
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setQueue(QUEUE_NAME);

        // Queue muss existieren oder deklariert werden (vereinfacht)
        rabbitTemplate.execute(channel -> {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            return null;
        });

        // Alle 10 Sekunden neue Message erzeugen & senden
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String condition = getWeatherCondition();
                    double kwh = calculateProduction(condition);

                    JSONObject message = new JSONObject();
                    message.put("type", "PRODUCER");
                    message.put("association", "COMMUNITY");
                    message.put("kwh", kwh);
                    message.put("datetime", LocalDateTime.now().toString());

                    rabbitTemplate.convertAndSend(QUEUE_NAME, message.toString());

                    System.out.println("✅ Gesendet: " + message.toString());
                } catch (Exception e) {
                    System.err.println("❌ Fehler beim Senden der Message: " + e.getMessage());
                }
            }
        }, 0, 10000); // 10 Sekunden

    }

    // Holt aktuelle Wetterlage aus WeatherAPI
    public static String getWeatherCondition() {
        try {
            String urlStr = "http://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + LOCATION + "&aqi=no";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            conn.disconnect();

            JSONObject json = new JSONObject(response.toString());
            return json.getJSONObject("current").getJSONObject("condition").getString("text");

        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    // Berechnet Energieproduktion auf Basis der Wetterlage
    public static double calculateProduction(String condition) {
        double baseProduction = 0.5; // Grundproduktion in kWh

        switch (condition.toLowerCase()) {
            case "sunny":
            case "clear":
                return baseProduction + 1.5 + randomVariation();
            case "partly cloudy":
            case "cloudy":
                return baseProduction + 0.8 + randomVariation();
            case "overcast":
            case "mist":
                return baseProduction + 0.3 + randomVariation();
            case "rain":
            case "snow":
            case "fog":
                return baseProduction * 0.2 + randomVariation();
            default:
                return baseProduction + randomVariation();
        }
    }

    // Kleine Zufallsvarianz (z. B. ±0.2 kWh)
    public static double randomVariation() {
        return new Random().nextDouble() * 0.4 - 0.2;
    }
}