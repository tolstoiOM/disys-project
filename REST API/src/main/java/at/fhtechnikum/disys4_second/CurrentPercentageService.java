package at.fhtechnikum.disys4_second;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "currentpercentageservice")
public class CurrentPercentageService {
    @Id
    @Column(name = "hour", columnDefinition = "TIMESTAMP")
    private LocalDateTime hour;
    private double community_Depleted;
    private double grid_Portion;

    // Getter und Setter
    public LocalDateTime getHour() {
        return hour;
    }

    public void setHour(LocalDateTime hour) {
        this.hour = hour;
    }

    public double getCommunityDepleted() {
        return community_Depleted;
    }

    public void setCommunityDepleted(double communityDepleted) {
        this.community_Depleted = communityDepleted;
    }

    public double getGridPortion() {
        return grid_Portion;
    }

    public void setGridPortion(double gridPortion) {
        this.grid_Portion = gridPortion;
    }
}