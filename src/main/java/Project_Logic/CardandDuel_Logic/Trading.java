package Project_Logic.CardandDuel_Logic;


import Project_Logic.DatenbankandMethods.Datenbank;
import Project_Logic.Monster_Logic.TypeofMonsters;
import Project_Logic.User_Logic.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Trading {

    // Statisches Mitglied, das nur eine Instanz der Trading-Klasse hält, e kam bo getInstance sepse esht singleton sepse trading funksionon si marketplace dhe kemi nevoj vetem nje instanc te klases per me bo
    private static Trading single_instance;

    // Stellt einen globalen Zugriffspunkt bereit
    public static Trading getInstance() {
        // Initialisiere die Instanz, wenn sie null ist
        try {
            if (single_instance == null) {
                single_instance = new Trading();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Gibt die Singleton-Instanz zurück
        return single_instance;
    }

    // Methode zur Demonstration des Marktplatzes
    public String showing_Marketfortrade() {
        // Variablen deklarieren
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        try {
            // Hole eine Verbindungsinstanz
            conn = Datenbank.getDatabaseInstance().createConnection();

            // Bereite das Statement vor
            ps = conn.prepareStatement("SELECT tradeid, cards.cardid, name,damage, owner, mindamage, type " +
                    "FROM marketplace " +
                    "JOIN cards " +
                    "ON cards.cardID = marketplace.cardID;");

            // Führe die Abfrage aus
            rs = ps.executeQuery();

            // Durchlaufe die Ergebnisse und füge sie dem ArrayNode hinzu
            while (rs.next()) {
                ObjectNode transaction = mapper.createObjectNode();
                // Füge jede Spalte der Zeile als Feld im ObjectNode hinzu
                transaction.put("TradeID", rs.getString(1));
                transaction.put("CardID", rs.getString(2));
                transaction.put("Name", rs.getString(3));
                transaction.put("Damage", rs.getString(4));
                transaction.put("Owner", rs.getString(5));
                transaction.put("MinimumDamage", rs.getString(6));
                transaction.put("Type", rs.getString(7));

                // Füge das befüllte ObjectNode dem ArrayNode hinzu
                arrayNode.add(transaction);
            }

            // Schließe das Statement und ResultSet
            rs.close();
            ps.close();

            // Schließe die Verbindung
            conn.close();

            // Gib das ArrayNode als formatierten JSON-String zurück
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);

        } catch (SQLException | JsonProcessingException e) {
            // Behandle Ausnahmen und gib null zurück
            e.printStackTrace();
            return null;
        } finally {
            // Schließe das Statement, ResultSet und die Verbindung, wenn sie nicht null sind
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean placeforCardstrading(User user, String tradeID, String cardID, float minimumDamage, String type) {
        // Deklaration der notwendigen Variablen für die Datenbankverbindung und Abfragen
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Verbindung zur Datenbankinstanz herstellen
            conn = Datenbank.getDatabaseInstance().createConnection();

            // Überprüfen, ob die Karte bereits auf dem Marktplatz vorhanden ist
            if (perform_PlaceforTrade_Check(cardID)) {
                // Falls die Karte schon vorhanden ist, Abbruch und Rückgabe von false
                return false;
            }
            // SQL-Statement vorbereiten, um zu überprüfen, ob der Benutzer die Karte besitzt
            ps = conn.prepareStatement("SELECT COUNT(cardid) " + "FROM cards " + "WHERE owner = ? AND cardid = ? AND collection LIKE 'stack';");
            ps.setString(1, user.getUsername());// Setzen des Benutzernamens im SQL-Statement
            ps.setString(2, cardID);// Setzen der Karten-ID im SQL-Statement

            // Ausführen der SQL-Abfrage
            rs = ps.executeQuery();

            // Überprüfen, ob der Benutzer die Karte besitzt
            if (!rs.next() || rs.getInt(1) != 1) {
                rs.close();
                ps.close();
                conn.close();
                // Schließen der Ressourcen und Rückgabe von false, falls Benutzer die Karte nicht besitzt
                return false;
            }

            // Schließen des ResultSets und des Statements, da sie nicht mehr benötigt werden
            rs.close();
            ps.close();

            // Neues SQL-Statement vorbereiten, um die Karte in den Marktplatz einzufügen
            ps = conn.prepareStatement("INSERT INTO marketplace(tradeid, cardid, mindamage, type) " +"VALUES(?,?,?,?);");
            ps.setString(1, tradeID); // Setzen der Handels-ID
            ps.setString(2, cardID); // Setzen der Karten-ID
            ps.setFloat(3, minimumDamage); // Setzen des minimalen Schadens
            ps.setString(4, type); // Setzen des Karten-Typs

            // Ausführen des Update-Statements und Speichern der Anzahl betroffener Zeilen
            int affectedRows = ps.executeUpdate();

            // Close the statement
            ps.close();

            // Überprüfen, ob die Karte erfolgreich eingefügt wurde
            if (affectedRows != 1) {
                conn.close();
                return false;
            }
            // Close the connection
            conn.close();
            return true;

            // Bei Auftreten einer SQL-Exception, Fehler ausgeben und false zurückgeben
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try {
                    // Schließen aller geöffneten Ressourcen
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean perform_Trade_cutout(User user, String id) {
        // Variablen deklarieren
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Verbindung zur Datenbank herstellen
            conn = Datenbank.getDatabaseInstance().createConnection();

            // Statement vorbereiten, um zu überprüfen, ob der Benutzer die Karte besitzt
            ps = conn.prepareStatement("SELECT cards.owner " +
                    "FROM cards " +
                    "JOIN marketplace " +
                    "ON cards.cardID = marketplace.cardID " +
                    "WHERE marketplace.tradeID = ?;");
            ps.setString(1, id);

            // Abfrage ausführen
            rs = ps.executeQuery();

            // Überprüfen, ob der Benutzer die Karte besitzt
            if (!rs.next() || !rs.getString(1).equals(user.getUsername())) {
                // Ressourcen schließen, falls der Benutzer nicht der Besitzer ist
                rs.close();
                ps.close();
                conn.close();
                return false;
            }

            // Result-Set und Statement schließen
            rs.close();
            ps.close();

            // Statement vorbereiten, um den Handel zu entfernen
            ps = conn.prepareStatement("DELETE FROM marketplace " +
                    "WHERE tradeID = ?;");
            ps.setString(1, id);

            // Aktualisierungsabfrage ausführen
            int affectedRows = ps.executeUpdate();

            // Statement schließen
            ps.close();

            // Überprüfen, ob der Handel erfolgreich entfernt wurde
            if (affectedRows != 1) {
                // Verbindung schließen, falls kein Eintrag betroffen war
                conn.close();
                return false;
            }

            // Verbindung schließen
            conn.close();

            // Erfolg zurückgeben
            return true;

        } catch (SQLException e) {
            // Ausnahmen behandeln und false zurückgeben
            e.printStackTrace();
            return false;
        } finally {
            // Result-Set, Statement und Verbindung schließen, falls sie nicht null sind
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean perform_PlaceforTrade_Check(String cardID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Datenbank.getDatabaseInstance().createConnection();

            ps = conn.prepareStatement("SELECT COUNT(cardid) FROM marketplace WHERE cardid = ?;");
            ps.setString(1, cardID);

            rs = ps.executeQuery();

            if (rs.next() && rs.getInt(1) == 1) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    // Methods to trade cards between users based on the tradeID and cardID
    public boolean perform_TradeofCards(User user, String tradeID, String cardID) {
        // Check if the user is null; if yes, return false
        if (user == null) {
            return false;
        }

        // Declare database-related variables
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Get a connection instance from the database
            conn = Datenbank.getDatabaseInstance().createConnection();

            // Check if the card is already in the marketplace; if yes, return false
            if (perform_PlaceforTrade_Check(cardID)) {
                return false;
            }

            // Declare variables to store card data
            String cardName;
            float cardDamage;

            // Prepare a query to get the card details based on the user and cardID
            ps = conn.prepareStatement("SELECT name, damage " +
                                           "FROM cards " +
                                           "WHERE owner = ? AND cardid = ? AND collection LIKE 'stack';");
            ps.setString(1, user.getUsername());
            ps.setString(2, cardID);

            // Execute the query and get the results
            rs = ps.executeQuery();

            // If the card is not found, close resources and return false
            if (!rs.next()) {
                rs.close();
                ps.close();
                conn.close();
                return false;
            }

            // Store the card data from the result set
            cardName = rs.getString(1);
            cardDamage = rs.getFloat(2);

            // Close the result set and prepared statement
            rs.close();
            ps.close();

            // Declare variables to store offered card data
            String offeredCardID;
            String offeredCardOwner;
            float minDamage;
            String type;

            // Prepare a query to get the offered card data based on the tradeID
            ps = conn.prepareStatement("SELECT marketplace.cardID, owner, minDamage, type " +
                                           "FROM marketplace " +
                                           "JOIN cards " +
                                           "ON marketplace.cardID = cards.cardID " +
                                           "WHERE tradeID = ?;");
            ps.setString(1, tradeID);

            // Execute the query and get the results
            rs = ps.executeQuery();

            // If the offered card is not found, close resources and return false
            if (!rs.next()) {
                rs.close();
                ps.close();
                conn.close();
                return false;
            }

            // Store the offered card data from the result set
            offeredCardID = rs.getString(1);
            offeredCardOwner = rs.getString(2);
            minDamage = rs.getFloat(3);
            type = rs.getString(4);

            // Close the result set and prepared statement
            rs.close();
            ps.close();

            // Get the instance of Handler_Card
            CardHandler cardHandler = CardHandler.getHandlerInstance();

            // Check the trade conditions based on the card type
            if (type.equalsIgnoreCase("monster")) {
                // If card type is a monster, but the determined category is spell, return false
                if (cardHandler.decideCategoryOfMonster(cardName) == TypeofMonsters.Spell) {
                    return false;
                }
            } else {
                // If the determined category of the card doesn't match the specified type, return false
                if (cardHandler.decideCategoryOfMonster(cardName) != cardHandler.decideCategoryOfMonster(type)) {
                    return false;
                }
            }

            // If the damage value of the card is less than the minimum required damage, return false
            if (cardDamage < minDamage) {
                return false;
            }

            // If the user offering the card is the same as the user attempting to make the trade, return false
            if (offeredCardOwner.equalsIgnoreCase(user.getUsername())) {
                return false;
            }

            // Prepare and execute SQL statements to update the owner of the card
            try (PreparedStatement ps1 = conn.prepareStatement("UPDATE cards SET owner = ? WHERE cardID = ?")) {
                ps1.setString(1, offeredCardOwner);
                ps1.setString(2, cardID);
                ps1.executeUpdate();
            }

            // Prepare and execute SQL statements to update the owner of the offered card
            try (PreparedStatement ps2 = conn.prepareStatement("UPDATE cards SET owner = ? WHERE cardID = ?")) {
                ps2.setString(1, user.getUsername());
                ps2.setString(2, offeredCardID);
                ps2.executeUpdate();
            }

            // Prepare and execute SQL statements to remove the trade from the marketplace
            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM marketplace WHERE tradeID = ?;")) {
                ps3.setString(1, tradeID);
                ps3.executeUpdate();
            }

            // Close the connection
            conn.close();


            // Return true as the trade has been successfully completed
            return true;

        } catch (SQLException e) {
            // Print the stack trace in case of any SQL exceptions
            e.printStackTrace();
        } finally {
            // Close resources in the finally block to ensure they are closed even if an exception occurs
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

// Return false if the trade could not be completed
        return false;
    }

}
