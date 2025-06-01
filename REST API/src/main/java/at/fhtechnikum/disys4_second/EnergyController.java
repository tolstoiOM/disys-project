package at.fhtechnikum.disys4_second;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {
    private final CurrentPercentageServiceRepository currentPercentageServiceRepository;
    private final UsageServiceRepository usageServiceRepository;

    @Autowired
    public EnergyController(CurrentPercentageServiceRepository currentPercentageServiceRepository, UsageServiceRepository usageServiceRepository) {
        this.currentPercentageServiceRepository = currentPercentageServiceRepository;
        this.usageServiceRepository = usageServiceRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentPercentage() {
        CurrentPercentageService result = currentPercentageServiceRepository.findLatest();
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/historical")
    public List<UsageService> getHistoricalUsage(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return usageServiceRepository.findByHourBetween(start, end);
    }
}