package com.example.energydistributioninterface;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Random;
import java.util.Locale;


public class Controller {

    @FXML
    private Label communityResultLabel;

    @FXML
    private Label gridResultLabel;

    @FXML
    private Label communityProducedLabel;

    @FXML
    private Label communityUsedLabel;

    @FXML
    private Label gridUsedLabel;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    public void fetchButtonAction(javafx.event.ActionEvent actionEvent) {
        try {
            // URL der API
            URL url = new URL("http://localhost:8080/measurements/current-hour");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Überprüfen, ob die Anfrage erfolgreich war
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // JSON-Array verarbeiten
                String jsonResponse = response.toString();
                org.json.JSONArray jsonArray = new org.json.JSONArray(jsonResponse);
                if (jsonArray.length() > 0) {
                    org.json.JSONObject firstObject = jsonArray.getJSONObject(0);

                    // Werte aus JSON extrahieren
                    double communityProduced = firstObject.getDouble("community_produced");
                    double communityUsed = firstObject.getDouble("community_used");
                    double gridUsed = firstObject.getDouble("grid_used");

                    // Berechnungen
                    double communityPercentage = (communityProduced > 0)
                            ? (communityUsed / communityProduced) * 100
                            : 0;

                    // Labels aktualisieren
                    communityResultLabel.setText(String.format(Locale.US,"%.2f", communityPercentage)); // Prozentwert mit 2 Dezimalstellen
                    gridResultLabel.setText(String.valueOf(gridUsed)); // Grid-Wert direkt anzeigen
                }
            } else {
                System.out.println("Fehler: " + connection.getResponseCode());
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void fetchHistoricData(javafx.event.ActionEvent actionEvent) {
        try {
            // Start- und Enddatum auslesen
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                System.out.println("Ungültige Datumsangaben");
                return;
            }

            double totalCommunityProduced = 0;
            double totalCommunityUsed = 0;
            double totalGridUsed = 0;

            // Iteration über die Stunden zwischen den beiden Zeitpunkten
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                for (int hour = 0; hour < 24; hour++) {
                    String urlString = String.format("http://localhost:8080/measurements/historic?hour=%d&date=%s", hour, date);
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");

                    if (connection.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // JSON-Array verarbeiten
                        String jsonResponse = response.toString();
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonResponse);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                            totalCommunityProduced += jsonObject.getDouble("community_produced");
                            totalCommunityUsed += jsonObject.getDouble("community_used");
                            totalGridUsed += jsonObject.getDouble("grid_used");
                        }
                    }
                    connection.disconnect();
                }
            }

            // Labels aktualisieren
            communityProducedLabel.setText(String.format(Locale.US, "%.2f", totalCommunityProduced));
            communityUsedLabel.setText(String.format(Locale.US, "%.2f", totalCommunityUsed));
            gridUsedLabel.setText(String.format(Locale.US, "%.2f", totalGridUsed));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
