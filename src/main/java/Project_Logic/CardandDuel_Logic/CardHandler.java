package Project_Logic.CardandDuel_Logic;


import Project_Logic.DatenbankandMethods.Datenbank;
import Project_Logic.Monster_Logic.Elements;
import Project_Logic.Monster_Logic.TypeofMonsters;
import Project_Logic.User_Logic.User;
import com.fasterxml.jackson.core.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.*;
import java.util.*;

public class CardHandler {

    /**
     * Singleton-Instanz der Klasse.
     */
    private static CardHandler uniqueHandlerInstance = null;

    /**
     * Methode, um die Singleton-Instanz zu erhalten.
     */
    public static CardHandler getHandlerInstance() {
        if (uniqueHandlerInstance == null) {
            // Synchronisieren auf CardHandler.class, um die einmalige Instanziierung zu gewährleisten
            synchronized (CardHandler.class) {
                if (uniqueHandlerInstance == null) { // Zusätzliche Überprüfung für den Fall, dass ein anderer Thread schneller war
                    uniqueHandlerInstance = new CardHandler(); // Instanziierung, falls noch nicht geschehen
                }
            }
        }
        // Rückgabe der Singleton-Instanz
        return uniqueHandlerInstance;
    }

    /**
     * Diese Methode erstellt ein Kartenset, indem sie sicherstellt, dass jede Karte verfügbar ist
     * und sich nicht bereits in einer Sammlung befindet. Sie gibt true zurück, wenn das Set erfolgreich
     * erstellt wurde, andernfalls false.
     */
    public boolean compileCardSet(List<DuelCard> cardList) {
        // Überprüfung der Eingabedaten auf Gültigkeit und ob genau 5 Karten enthalten sind
        if (cardList == null || cardList.size() != 5) {
            return false;
        }

        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {
            // Prüfung, ob alle Karten verfügbar sind und nicht in einer Sammlung sind
            if (!validateCardAvailability(cardList, connection)) {
                return false;
            }

            // Einfügen des Kartensets in die Datenbank
            if (!insertCardSet(cardList, connection)) {
                return false;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * Überprüft, ob alle Karten in der übergebenen Liste verfügbar und nicht Teil einer Sammlung sind.
     * Gibt true zurück, wenn alle Karten verfügbar sind, andernfalls false.
     */
    private boolean validateCardAvailability(List<DuelCard> cardList, Connection dbConnection) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM cards WHERE cardid = ? AND collection IS NULL;";
        for (DuelCard singleCard : cardList) {
            // Überprüfung der Verfügbarkeit jeder einzelnen Karte
            if (!isCardAvailable(singleCard, dbConnection, checkQuery)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Überprüft, ob eine bestimmte Karte verfügbar ist.
     * Gibt true zurück, wenn die Karte verfügbar ist, andernfalls false.
     */
    private boolean isCardAvailable(DuelCard card, Connection dbConnection, String query) throws SQLException {
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setString(1, card.getCardId());
            ResultSet resultSet = statement.executeQuery();
            // Überprüfung, ob die Karte in der Datenbank vorhanden und verfügbar ist
            return resultSet.next() && resultSet.getInt(1) == 1;
        }
    }

    /**
     * Fügt ein neues Kartenpaket mit den angegebenen Karten in die Datenbank ein.
     * Gibt true zurück, wenn das Einfügen erfolgreich war, andernfalls false.
     */
    private boolean insertCardSet(List<DuelCard> cardList, Connection dbConnection) throws SQLException {
        String insertQuery = "INSERT INTO packages(cardid_1, cardid_2, cardid_3, cardid_4, cardid_5) VALUES (?, ?, ?, ?, ?);";
        // Ausführen der Einfügeoperation für das Kartenpaket
        return executePackageInsertion(cardList, dbConnection, insertQuery);
    }

    /**
     * Führt das Einfügen eines Kartenpakets in die Datenbank aus.
     * Gibt true zurück, wenn das Einfügen erfolgreich war, andernfalls false.
     */
    private boolean executePackageInsertion(List<DuelCard> cardList, Connection dbConnection, String query) throws SQLException {
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            for (int i = 0; i < cardList.size(); i++) {
                statement.setString(i + 1, cardList.get(i).getCardId());
            }
            // Überprüfen, ob genau eine Zeile eingefügt wurde, um den Erfolg zu bestätigen
            return statement.executeUpdate() == 1;
        }
    }



    // Diese Methode holt alle Karten eines Benutzers und gibt sie als JSON-String zurück.
    public String showUserCards(User user) {
        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {
            String query = "SELECT cardid, name, damage FROM cards WHERE owner = ?;";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, user.getUsername());
                String json = convertResultToJson(ps.executeQuery());
                return json;
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Diese Methode holt das Kartendeck eines Benutzers und gibt es als JSON-String zurück.
    public String showDeck(User user) {
        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {
            String query = "SELECT cardid, name, damage FROM cards WHERE owner = ? AND collection = 'deck';";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, user.getUsername());
                String json = convertResultToJson(ps.executeQuery());
                return json;
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Konvertiert das ResultSet in einen JSON-String.
    private String convertResultToJson(ResultSet rs) throws SQLException, JsonProcessingException {
        List<Map<String, String>> cards = new ArrayList<>();
        while (rs.next()) {
            Map<String, String> card = new HashMap<>();
            card.put("ID", rs.getString(1));
            card.put("Name", rs.getString(2));
            card.put("Damage", rs.getString(3));
            cards.add(card);
        }
        rs.close();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(cards);
    }

    private void distributeCardsToClient(Connection conn, String username, int packageId) throws SQLException {
        // SQL-Query, um die Besitzer der Karten im Paket zu aktualisieren.
        String updateQuery = "UPDATE cards\n" +
                "SET owner = ?, collection = 'stack'\n" +
                "WHERE cardID IN (\n" +
                "  SELECT cardid_1 FROM packages WHERE packageid = ?\n" +
                "  UNION\n" +
                "  SELECT cardid_2 FROM packages WHERE packageid = ?\n" +
                "  UNION\n" +
                "  SELECT cardid_3 FROM packages WHERE packageid = ?\n" +
                "  UNION\n" +
                "  SELECT cardid_4 FROM packages WHERE packageid = ?\n" +
                "  UNION\n" +
                "  SELECT cardid_5 FROM packages WHERE packageid = ?\n" +
                ");";
        try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            // Setzt den Benutzernamen und die Paket-ID für das Update.
            ps.setString(1, username);
            ps.setInt(2, packageId);
            ps.setInt(3, packageId);
            ps.setInt(4, packageId);
            ps.setInt(5, packageId);
            ps.setInt(6, packageId);
            // Führt das Update aus.
            ps.executeUpdate();
        }
    }

    // Berechnet durchschnittlichen Schaden aller Karten.
    public float calculateAverageDamage(List<DuelCard> cardList) {
        if (cardList == null || cardList.isEmpty()) {
            return 0.0f;
        }
        float totalDamage = 0.0f;
        for (DuelCard card : cardList) {
            totalDamage += card.getAttackPoints();
        }
        return totalDamage / cardList.size();
    }



    // Paket einem Benutzer zuordnen, wenn verfügbar.
    public boolean allocatePackageToClient(User client) {
        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {

            // Verfügbarkeit des Pakets prüfen.
            int packageId = searchForPackage(connection);

            // Bei Nichtverfügbarkeit false zurückgeben.
            if (packageId == -1) {
                return false;
            }

            // Kauf des Pakets versuchen.
            if (!client.Packageprocess_Buy()) {
                return false;
            }

            // Karten dem Benutzer zuweisen.
            distributeCardsToClient(connection, client.getUsername(), packageId);

            // Entfernt das Paket aus der Datenbank.
            removePackageFromDB(connection, packageId);

        } catch (SQLException e) {
            // SQL-Ausnahmen protokollieren und false zurückgeben.
            e.printStackTrace();
            return false;
        }

        // Bei Erfolg true zurückgeben.
        return true;
    }

    private int searchForPackage(Connection conn) throws SQLException {
        // Sucht ein verfügbares Paket in der Datenbank.
        String query = "SELECT * FROM packages LIMIT 1;";
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }



    public CardDeck perform_Deck_Retrieve(User user) {

        CardDeck deck = null;

        // Datenbankverbindung herstellen
        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {
            // Bereiten Sie eine Abfrage vor, um Karten abzurufen, die dem Benutzer gehören und sich in seinem Deck befinden
            String query = "SELECT cardid, name, damage FROM cards WHERE owner = ? AND collection = 'deck';";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, user.getUsername());
                ResultSet rs = ps.executeQuery();

                // Eine Liste initialisieren, um die abgerufenen Karten zu speichern
                List<DuelCard> cards = new ArrayList<>();

                // Durchlaufen Sie die Ergebnismenge, erstellen Sie Kartenobjekte und fügen Sie sie der Liste hinzu
                while (rs.next()) {
                    String name = rs.getString(2);
                    DuelCard card = new DuelCard(rs.getString(1), name, rs.getFloat(3), decideCategoryOfMonster(name), decide_TypeOfElement(name));
                    cards.add(card);
                }

                // Erstellen Sie ein CardDeck-Objekt mit der ausgefüllten Kartenliste
                deck = new CardDeck(cards);
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Das CardDeck-Objekt oder null zurückgeben, wenn eine Ausnahme aufgetreten ist
        return deck;
    }


    private void removePackageFromDB(Connection conn, int packageId) throws SQLException {
        // SQL-Query, um das zugewiesene Paket aus der Datenbank zu entfernen.
        String deleteQuery = "DELETE FROM packages WHERE packageid = ?;";
        try (PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
            // Setzt die Paket-ID für das Löschen.
            ps.setInt(1, packageId);
            // Führt das Löschen aus.
            ps.executeUpdate();
        }
    }

    // Konvertieren Sie die Eingabezeichenfolge in Kleinbuchstaben für Vergleiche ohne Berücksichtigung der Groß- und Kleinschreibung
    public Elements decide_TypeOfElement(String element) {
        String lowerCaseElement = element.toLowerCase();

        // Überprüfen Sie, ob das Element „Wasser“, „Feuer“ oder keines enthält, und geben Sie die entsprechende Elements-Enumeration zurück
        if (lowerCaseElement.contains("water")) {
            return Elements.Water;
        } else if (lowerCaseElement.contains("fire")) {
            return Elements.Fire;
        } else {
            return Elements.Normal;
        }
    }

    public TypeofMonsters decideCategoryOfMonster(String name) {
        // Konvertieren Sie die Eingabezeichenfolge in Kleinbuchstaben für Vergleiche ohne Berücksichtigung der Groß- und Kleinschreibung
        String lowerCaseName = name.toLowerCase();

        // Erstelle eine Karte mit Monsternamen und den entsprechenden TypeofMonsters
        Map<String, TypeofMonsters> monsterCategories = new HashMap<>();
        monsterCategories.put("spell", TypeofMonsters.Spell);
        monsterCategories.put("dragon", TypeofMonsters.Dragon);
        monsterCategories.put("fireelf", TypeofMonsters.FireElf);
        monsterCategories.put("goblin", TypeofMonsters.Goblin);
        monsterCategories.put("knight", TypeofMonsters.Knight);
        monsterCategories.put("kraken", TypeofMonsters.Kraken);
        monsterCategories.put("ork", TypeofMonsters.Ork);
        monsterCategories.put("wizard", TypeofMonsters.Wizard);

        // Über die Karte iterieren und die entsprechenden TypeofMonsters zurückgeben, wenn der Name den Schlüssel enthält
        for (Map.Entry<String, TypeofMonsters> entry : monsterCategories.entrySet()) {
            if (lowerCaseName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Null zurückgeben, wenn keine der spezifischen Monsterkategorien übereinstimmt
        return null;
    }



    public boolean perform_Card_log(String id, String name, float damage) {
        // Überprüfen, ob die bereitgestellte ID und der Name nicht leer sind, und ob die Karte ein gültiges Element und eine gültige Monsterkategorie hat
        if (id.isEmpty() || name.isEmpty() || decide_TypeOfElement(name) == null || decideCategoryOfMonster(name) == null) {
            return false;
        }

        // Versuch, eine Datenbankverbindung herzustellen und eine neue Karte in die Tabelle cards einzufügen
        try (Connection conn = Datenbank.getDatabaseInstance().createConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO cards(cardid, name, damage) VALUES(?,?,?)")) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setFloat(3, damage);

            // Überprüfen, ob die Kartenerstellung erfolgreich war
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public void perform_Card_CutOut(String id) {
        // Eine Datenbankverbindung herstellen
        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {
            // Vorbereiten einer Abfrage zum Löschen einer Karte aus der Tabelle cards, wo die Karten-ID mit der bereitgestellten ID übereinstimmt
            // und die Karte nicht Teil einer Sammlung ist
            String query = "DELETE FROM cards WHERE cardid = ? AND collection IS NULL";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, id);

                // Die Abfrage ausführen, um die Karte zu löschen
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean perform_Deck_Creation(User user, List<String> ids) {
        // Überprüfen Sie, ob die bereitgestellte Liste der Karten-IDs die richtige Größe für ein Deck hat
        if (ids.size() != 4) {
            return false;
        }

        // Überprüfen Sie, ob der Benutzer die bereitgestellten Karten besitzt und ob es keine Duplikate gibt
        if (!perform_CardsOfUser(user, ids)) {
            return false;
        }
        perform_GroupOfCards_update(user, "stack");

        // Aktualisiere die Karten in der bereitgestellten Liste, damit sie Teil der „Deck“-Sammlung des Benutzers sind
        updateCardsCollection(user, ids, "deck");

        return true;
    }

    private boolean perform_CardsOfUser(User user, List<String> ids) {
        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {
            // Liste für eindeutige IDs erstellen
            List<String> uniqueIds = new LinkedList<>();

            // Überprüfung jeder Karten-ID in der Liste
            for (String cardID : ids) {
                // Überprüfen, ob die Karten-ID einzigartig ist und nicht zum Handel angeboten wird
                if (uniqueIds.contains(cardID) || Trading.getInstance().perform_PlaceforTrade_Check(cardID)) {
                    return false;
                }
                uniqueIds.add(cardID);

                // Abfrage zur Überprüfung, ob die Karte dem Benutzer gehört
                String query = "SELECT COUNT(cardid) FROM cards WHERE cardid = ? AND owner = ?;";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, cardID);
                    ps.setString(2, user.getUsername());
                    ResultSet rs = ps.executeQuery();

                    // Überprüfen, ob die Karte existiert und dem Benutzer gehört
                    if (!rs.next() || rs.getInt(1) != 1) {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void perform_GroupOfCards_update(User user, String collection) {
        try (Connection connection = Datenbank.getDatabaseInstance().createConnection()) {
            // SQL-Abfrage zur Aktualisierung der Kartensammlung für einen Benutzer
            String updateStackQuery = "UPDATE cards SET collection = ? WHERE owner = ?;";
            try (PreparedStatement ps = connection.prepareStatement(updateStackQuery)) {
                ps.setString(1, collection);
                ps.setString(2, user.getUsername());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCardsCollection(User user, List<String> ids, String collection) {
        try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
            // SQL-Abfrage zur Aktualisierung der Sammlung für jede Karte des Benutzers
            String updateDeckQuery = "UPDATE cards SET collection = ? WHERE owner = ? AND cardid = ?;";
            try (PreparedStatement ps = conn.prepareStatement(updateDeckQuery)) {
                for (String cardID : ids) {
                    ps.setString(1, collection);
                    ps.setString(2, user.getUsername());
                    ps.setString(3, cardID);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
