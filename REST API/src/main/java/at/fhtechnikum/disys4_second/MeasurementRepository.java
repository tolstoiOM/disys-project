package at.fhtechnikum.disys4_second;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MeasurementRepository {
    private final List<Measurement> measurements;

    public MeasurementRepository() {
        this.measurements = loadMeasurementsFromJson();
        System.out.println("Geladene Messwerte: " + measurements);
    }

    private List<Measurement> loadMeasurementsFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        try (InputStream inputStream = getClass().getResourceAsStream("/measurements.json")) {
            if (inputStream == null) {
                throw new IOException("JSON-Datei nicht gefunden");
            }
            return objectMapper.readValue(inputStream, new TypeReference<List<Measurement>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Measurement> findAll() {
        return new ArrayList<>(measurements);
    }

    public List<Measurement> findByHour(LocalDateTime hour) {
        List<Measurement> result = new ArrayList<>();
        for (Measurement measurement : measurements) {
            if (measurement.getTimestamp().getHour() == hour.getHour() &&
                    measurement.getTimestamp().toLocalDate().equals(hour.toLocalDate())) {
                result.add(measurement);
            }
        }
        return result;
    }
}