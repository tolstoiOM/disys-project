package at.fhtechnikum.disys4_second;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

@Repository
public interface CurrentPercentageServiceRepository extends JpaRepository<CurrentPercentageService, Integer> {
    //@Query(value = "SELECT * FROM currentpercentageservice WHERE hour = :hour", nativeQuery = true)
    @Query(value = "SELECT * FROM currentpercentageservice ORDER BY hour DESC LIMIT 1", nativeQuery = true)
    CurrentPercentageService findLatest();
    CurrentPercentageService findByHour(@Param("hour") LocalDateTime hour);
}