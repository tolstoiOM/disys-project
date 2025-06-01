package at.fhtechnikum.disys4_second;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/measurements")
public class MeasurementController {
    private final MeasurementRepository measurementRepository;

    @Autowired
    public MeasurementController(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @GetMapping
    public List<Measurement> getAllMeasurements() {
        return measurementRepository.findAll();
    }

    @GetMapping("/current-hour")
    public List<Measurement> getCurrentHourData() {
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour(); // Extrahiere die aktuelle Stunde
        return measurementRepository.findByHour(currentHour);
    }

    @GetMapping("/historic")
    public List<Measurement> getHistoricData(@RequestParam("hour") int hour, @RequestParam("date") String date) {
        // Validierung und Parsing des Datums
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime requestedDate = LocalDateTime.parse(date, formatter).withHour(hour);
        return measurementRepository.findByHour(requestedDate.getHour());
    }

    @GetMapping("/range")
    public List<Measurement> getMeasurementsInRange(@RequestParam("start") String start, @RequestParam("end") String end) {
        // Parsing der Start- und Endzeit
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return measurementRepository.findByTimestampBetween(startTime, endTime);
    }
}