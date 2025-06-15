package org.example;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageReceiverTest {

    @Test
    void testUpdateUsageService() throws SQLException {
        // Arrange: Mocks für die DB-Verbindung und das PreparedStatement erstellen
        Connection dbConnection = Mockito.mock(Connection.class);
        PreparedStatement stmt = Mockito.mock(PreparedStatement.class);
        when(dbConnection.prepareStatement(anyString())).thenReturn(stmt);

        LocalDateTime hour = LocalDateTime.of(2023, 10, 1, 10, 0);
        String type = "PRODUCER";
        double kwh = 5.0;
        String association = "COMMUNITY";

        // Act: Die Methode aufrufen
        MessageReceiver.updateUsageService(dbConnection, hour, type, association, kwh);

        // Assert: Überprüfen, dass die Parameter gesetzt und das Update ausgeführt wurde
        verify(stmt).setTimestamp(1, Timestamp.valueOf(hour));
        verify(stmt).setDouble(2, type.equals("PRODUCER") ? kwh : 0);
        verify(stmt).setDouble(3, type.equals("USER") ? kwh : 0);
        verify(stmt).setDouble(4, 0);
        verify(stmt).setString(5, type);
        verify(stmt).setString(6, type);
        verify(stmt).setString(7, type);
        verify(stmt).executeUpdate();
    }

    @Test
    void testProcessMessage() throws Exception {
        // Arrange: Mocks für DB und Statement
        Connection dbConnection = Mockito.mock(Connection.class);
        PreparedStatement stmtUsage = Mockito.mock(PreparedStatement.class);
        PreparedStatement stmtCurrent = Mockito.mock(PreparedStatement.class);
        when(dbConnection.prepareStatement(Mockito.contains("UsageService"))).thenReturn(stmtUsage);
        when(dbConnection.prepareStatement(Mockito.contains("CurrentPercentageService"))).thenReturn(stmtCurrent);

        // Erstelle ein JSON-Objekt, das eine gültige Nachricht repräsentiert
        String datetimeStr = "2023-10-01T10:15:30";
        JSONObject messageJson = new JSONObject();
        messageJson.put("type", "PRODUCER");
        messageJson.put("association", "COMMUNITY");
        messageJson.put("kwh", 5.0);
        messageJson.put("datetime", datetimeStr);

        // Act: processMessage aufrufen
        MessageReceiver.processMessage(dbConnection, messageJson);

        // Assert: Überprüfen, ob executeUpdate für beide Statements aufgerufen wurde
        verify(stmtUsage).executeUpdate();
        verify(stmtCurrent).executeUpdate();
    }
}