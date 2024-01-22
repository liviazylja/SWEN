package Project_Logic.User_Logic;

import Project_Logic.DatenbankandMethods.Datenbank;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class User {
    // Deklaration der Mitgliedsvariablen
    private String username; // Benutzername des Users
    private String name;     // Name des Users
    private String bio;      // Biografie des Users
    private String image;    // Bild-URL des Users
    private int coins;       // Anzahl der Münzen des Users
    private int games;       // Anzahl der Spiele, die der User gespielt hat
    private int wins;        // Anzahl der Siege des Users
    private int elo;         // ELO-Bewertung des Users

    // Konstruktor der User-Klasse
    public User(String username, String name, String bio, String image, int coins, int games, int wins, int elo) {
        // Initialisierung der Variablen
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.coins = coins;
        this.games = games;
        this.wins = wins;
        this.elo = elo;
    }

    // Getter-Methode für den Benutzernamen
    public String getUsername() {
        return username;
    }

    // Getter-Methode für den Namen
    public String getName() {
        return name;
    }

    // Methode zur Rückgabe von Benutzerinformationen als JSON-String
    public String info() {
        // Erstellen einer neuen Map zur Speicherung von Benutzerinformationen
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("Name", this.name);
        userInfo.put("Bio", this.bio);
        userInfo.put("Bild", this.image);
        userInfo.put("Münzen", Integer.toString(this.coins));

        // Konvertierung der Map in einen JSON-String
        try {
            return new ObjectMapper().writeValueAsString(userInfo);
        } catch (JsonProcessingException e) {
            // Fehlerbehandlung: Ausgabe der Fehlermeldung im Fehlerfall
            e.printStackTrace();
            return null;
        }
    }

    public boolean Packageprocess_Buy() {
        // Prüfen, ob der Benutzer genug Coins hat
        if (this.coins < 5) {
            return false; // Nicht genug Coins
        }

        try {
            // Verbindung zur Datenbank herstellen
            try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
                // Neue Coins-Menge nach dem Kauf berechnen
                int newCoinAmount = this.coins - 5;

                // Update in der Datenbank ausführen und Ergebnis zurückgeben
                return performDatabaseUpdate(conn, newCoinAmount);
            }

        } catch (SQLException e) {
            // Fehlerausgabe bei Datenbankproblemen
            e.printStackTrace();
        }

        // Rückgabe von false, wenn ein Fehler aufgetreten ist
        return false;
    }

    /**
     * Führt das SQL-Update für die Aktualisierung der Coins in der Datenbank aus.
     *
     * @param conn Die Datenbankverbindung
     * @param newCoinAmount Die aktualisierte Anzahl von Coins
     * @return true, wenn das Update erfolgreich war, sonst false
     * @throws SQLException Bei Fehlern mit der Datenbankverbindung
     */
    private boolean performDatabaseUpdate(Connection conn, int newCoinAmount) throws SQLException {
        // SQL-Update-Statement vorbereiten
        String sql = "UPDATE users SET coins = ? WHERE username = ?;";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            // Setzen der neuen Coins-Menge und des Benutzernamens im SQL-Statement
            statement.setInt(1, newCoinAmount);
            statement.setString(2, this.username);

            // Update ausführen und Erfolg prüfen
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated == 1; // Erfolg, wenn eine Zeile betroffen ist
        }
    }


    public String retrieveStatistics() {
        // Erstellen einer neuen HashMap für die Statistiken
        Map<String, Integer> statistikMap = new HashMap<>();

        // Anzahl der Siege und Spiele zur Map hinzufügen
        statistikMap.put("Wins registered:", this.wins);
        statistikMap.put("Games Played:", this.games);

        // Initialisieren eines ObjectMapper zur JSON-Konvertierung
        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            // Versuchen, die Map in einen JSON-String umzuwandeln und zurückzugeben
            return jsonMapper.writeValueAsString(statistikMap);
        } catch (JsonProcessingException e) {
            // Fehlerbehandlung bei Problemen mit der JSON-Konvertierung
            e.printStackTrace();
        }

        // Rückgabe von null, falls die Umwandlung fehlschlägt
        return null;
    }
    public boolean recordWin(){
        this.increaseVictories().addGamePlayed().adjustEloRating(3);
        return saveUpdatedStats();
    }

    public boolean recordLoss(){
        this.addGamePlayed().adjustEloRating(-5);
        return saveUpdatedStats();
    }

    public boolean recordTie(){
        this.addGamePlayed();
        return saveUpdatedStats();
    }

    private User increaseVictories() {
        // Erhöht die Anzahl der Siege des Benutzers um eins
        this.wins++;
        return this;
    }

    private User addGamePlayed() {
        // Erhöht die Anzahl der gespielten Spiele des Benutzers um eins
        this.games++;
        return this;
    }

    private User adjustEloRating(int change) {
        // Ändert die Elo-Bewertung des Benutzers um den angegebenen Betrag
        this.elo += change;
        return this;
    }

    public boolean saveUpdatedStats() {
        // Aktualisiert die Benutzerstatistiken (Siege, Spiele, Elo) in der Datenbank
        try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
            performStatisticsUpdate(conn);
            return true;
        } catch (SQLException e) {
            // Fehlerbehandlung im Falle einer SQL-Ausnahme
            e.printStackTrace();
        }
        return false;
    }


    // Methode zum Aktualisieren der Statistiken des Benutzers in der Datenbank
    private void performStatisticsUpdate(Connection conn) throws SQLException {
        // SQL-Statement vorbereiten, um die Gewinne, Spiele und ELO-Wert des Benutzers zu aktualisieren
        String updateQuery = "UPDATE users SET wins = ?, games = ?, elo = ? WHERE username = ?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(updateQuery)) {
            // Setze die Parameter für das Prepared Statement
            preparedStatement.setInt(1, this.wins); // Anzahl der Gewinne setzen
            preparedStatement.setInt(2, this.games); // Anzahl der Spiele setzen
            preparedStatement.setInt(3, this.elo); // ELO-Wert setzen
            preparedStatement.setString(4, this.username); // Benutzername setzen
            // Führe das Update-Statement aus
            preparedStatement.executeUpdate();
        }
    }

    // Methode zum Aktualisieren des Namens, der Bio und des Bildes des Benutzers
    public boolean performupdateOnInfos(String newName, String newBio, String newImage) {
        // Versuche, eine Verbindung zur Datenbank herzustellen und die Informationen zu aktualisieren
        try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
            // Führe das Update aus und speichere die Anzahl der betroffenen Zeilen
            int affectedRows = executeperformupdateOnInfos(conn, newName, newBio, newImage);
            // Überprüfe, ob eine Zeile betroffen war
            if (affectedRows == 1) {
                System.out.println("Update was a success.");
                return true;
            } else {
                System.out.println("Update failed. No lines affected were affected.");
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Hilfsmethode zum Ausführen des Updates der Benutzerinformationen
    private int executeperformupdateOnInfos(Connection conn, String newName, String newBio, String newImage) throws SQLException {
        // SQL-Statement vorbereiten, um Name, Bio und Bild des Benutzers zu aktualisieren
        String sql = "UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            // Setze die neuen Werte für Name, Bio und Bild
            preparedStatement.setString(1, newName); // Neuer Name
            preparedStatement.setString(2, newBio); // Neue Bio
            preparedStatement.setString(3, newImage); // Neues Bild
            preparedStatement.setString(4, this.username); // Benutzername
            // Führe das Update-Statement aus und gib die Anzahl der betroffenen Zeilen zurück
            return preparedStatement.executeUpdate();
        }
    }

}
