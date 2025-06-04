//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################
//package & imports

package org.example;

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



//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################
//class



public class ProducerMessageGenerator {

    //#########################################################################################################################
    //variables
    private static final String API_KEY = "bfb6969caa254864a2a182551252905";
    private static final String LOCATION = "Korneuburg";
    private static final String QUEUE_NAME = "energy.queue";


    //#########################################################################################################################
    //main
    public static void main(String[] args) {
        //build connection to RabbitMQ
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);


        //query must exist or to be declared (simplier)
        rabbitTemplate.execute(channel -> {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            return null;
        });

        //every 10s a new message will be generated & send
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
        }, 0, 10000); //10s
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    double gridKwh = 2.0 + new Random().nextDouble();
                    JSONObject gridMessage = new JSONObject();
                    gridMessage.put("type", "GRID");
                    gridMessage.put("association", "COMMUNITY");
                    gridMessage.put("kwh", gridKwh);
                    gridMessage.put("datetime", LocalDateTime.now().toString());

                    rabbitTemplate.convertAndSend(QUEUE_NAME, gridMessage.toString());
                    System.out.println("✅ GRID Nachricht gesendet: " + gridMessage.toString());
                } catch (Exception e) {
                    System.err.println("❌ Fehler beim Senden der GRID-Nachricht: " + e.getMessage());
                }
            }
        }, 0, 15000); //every 15s

    }

    //takes actual weatherinformation out of weatherAPI
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

    //calculate energyproduction based on the weather
    public static double calculateProduction(String condition) {
        double baseProduction = 0.5; // baseproduction in kWh

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

    //small randomvariance (z. B. ±0.2 kWh)
    public static double randomVariation() {
        return new Random().nextDouble() * 0.4 - 0.2;
    }
}



//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################