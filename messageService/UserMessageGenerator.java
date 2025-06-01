import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

@Component
public class UserMessageGenerator {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME = "energy.queue";
    private static final String COMMUNITY = "COMMUNITY";

    @Scheduled(fixedRate = 7000) // alle 7 Sekunden
    public void generateAndSendMessage() {
        double kwh = generateKwhBasedOnTime();
        JSONObject msg = new JSONObject();
        msg.put("type", "USER");
        msg.put("association", COMMUNITY);
        msg.put("kwh", kwh);
        msg.put("datetime", LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend(QUEUE_NAME, msg.toString());
        System.out.println("User sent: " + msg);
    }

    private double generateKwhBasedOnTime() {
        LocalTime now = LocalTime.now();
        Random random = new Random();

        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(9, 0))) {
            return 3.0 + random.nextDouble() * 2.0; // Morgen: 3–5 kWh
        } else if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(21, 0))) {
            return 3.0 + random.nextDouble() * 2.5; // Abend: 3–5.5 kWh
        } else {
            return 0.5 + random.nextDouble() * 2.0; // sonst: 0.5–2.5 kWh
        }
    }
}