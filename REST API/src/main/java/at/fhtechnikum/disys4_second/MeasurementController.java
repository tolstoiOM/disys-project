package at.fhtechnikum.disys4_second;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/measurements")
public class MeasurementController {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    private final MeasurementRepository measurementRepository;

    @Autowired
    public MeasurementController(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @GetMapping("/current-hour")
    public List<Measurement> getCurrentHourData() {
        LocalDateTime simulatedNow = LocalDateTime.of(2023, 10, 1, 11, 0);
        return measurementRepository.findByHour(simulatedNow);
    }

    @GetMapping("/historic")
    public List<Measurement> getHistoricData(@RequestParam("hour") int hour, @RequestParam("date") String date) {
        LocalDateTime requestedTime = LocalDateTime.parse(date + "T" + String.format("%02d", hour) + ":00:00");
        return measurementRepository.findByHour(requestedTime);
    }
}