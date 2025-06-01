package at.fhtechnikum.disys4_second;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private double consumption;
    private int hour;
    private double communityProduced;
    private double communityUsed;
    private double gridUsed;

    // Standardkonstruktor
    public Measurement() {
    }

    // Konstruktor mit Parametern
    public Measurement(LocalDateTime timestamp, double consumption, int hour, double communityProduced, double communityUsed, double gridUsed) {
        this.timestamp = timestamp;
        this.consumption = consumption;
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getCommunityProduced() {
        return communityProduced;
    }

    public void setCommunityProduced(double communityProduced) {
        this.communityProduced = communityProduced;
    }

    public double getCommunityUsed() {
        return communityUsed;
    }

    public void setCommunityUsed(double communityUsed) {
        this.communityUsed = communityUsed;
    }

    public double getGridUsed() {
        return gridUsed;
    }

    public void setGridUsed(double gridUsed) {
        this.gridUsed = gridUsed;
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", consumption=" + consumption +
                ", hour=" + hour +
                ", communityProduced=" + communityProduced +
                ", communityUsed=" + communityUsed +
                ", gridUsed=" + gridUsed +
                '}';
    }
}