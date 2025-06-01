package at.fhtechnikum.disys4_second;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findAll();

    List<Measurement> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<Measurement> findByHour(int hour);
}