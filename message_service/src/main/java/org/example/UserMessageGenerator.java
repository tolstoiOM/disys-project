//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################
//package & imports



package org.example;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;



//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################
//class



public class UserMessageGenerator {

    //#########################################################################################################################
    //variables
    private static final String QUEUE_NAME = "energy.queue";
    private static final String COMMUNITY = "COMMUNITY";


    //#########################################################################################################################
    //main
    public static void main(String[] args) {
        //build connection RabbitMQ
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        //query must exist or to be declared (simplier)
        rabbitTemplate.execute(channel -> {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            return null;
        });

        //every 7s a new message will be generated & send
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    double kwh = generateKwhBasedOnTime();
                    JSONObject msg = new JSONObject();
                    msg.put("type", "USER");
                    msg.put("association", COMMUNITY);
                    msg.put("kwh", kwh);
                    msg.put("datetime", LocalDateTime.now().toString());

                    rabbitTemplate.convertAndSend(QUEUE_NAME, msg.toString());
                    System.out.println("✅ Gesendet: " + msg.toString());
                } catch (Exception e) {
                    System.err.println("❌ Fehler beim Senden der Message: " + e.getMessage());
                }
            }
        }, 0, 7000); //7s
    }

    //generate a random Usage of kwh based on Time
    private static double generateKwhBasedOnTime() {
        LocalTime now = LocalTime.now();
        Random random = new Random();

        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(9, 0))) {
            return 3.0 + random.nextDouble() * 2.0; // morning: 3–5 kWh
        } else if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(21, 0))) {
            return 3.0 + random.nextDouble() * 2.5; // afternoon: 3–5.5 kWh
        } else {
            return 0.5 + random.nextDouble() * 2.0; // else: 0.5–2.5 kWh
        }
    }
}



//#########################################################################################################################
//#########################################################################################################################
//#########################################################################################################################