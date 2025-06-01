package at.fhtechnikum.disys4_second;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageServiceRepository extends JpaRepository<UsageService, Integer> {
    @Query(value = "SELECT * FROM usageservice WHERE hour BETWEEN :startHour AND :endHour", nativeQuery = true)
    List<UsageService> findByHourBetween(@Param("startHour") LocalDateTime startHour, @Param("endHour") LocalDateTime endHour);
}