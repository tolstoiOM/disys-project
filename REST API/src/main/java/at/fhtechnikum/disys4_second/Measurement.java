package at.fhtechnikum.disys4_second;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Measurement {
    private LocalDateTime timestamp;
    private double consumption;

    // Standardkonstruktor
    public Measurement() {
    }

    // Konstruktor mit Parametern (optional, falls benötigt)
    public Measurement(LocalDateTime timestamp, double consumption) {
        this.timestamp = timestamp;
        this.consumption = consumption;
    }

    // Getter und Setter
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

    @Override
    public String toString() {
        return "Measurement{" +
                "timestamp=" + timestamp +
                ", consumption=" + consumption +
                '}';
    }
}