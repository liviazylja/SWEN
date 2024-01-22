package Project_Logic.CardandDuel_Logic;


import Project_Logic.DatenbankandMethods.Datenbank;
import Project_Logic.Monster_Logic.Elements;
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
import java.util.Random;

public class DuelManager {

    // Einzige Instanz der DuelManager-Klasse
    private static DuelManager uniqueInstance = null;

    // Teilnehmer des Duells
    private User challenger;
    private User defender;
    // Ergebnis des Duells im JSON-Format
    private String duelOutcome;
    // Status, ob ein Duell aktuell stattfindet
    private boolean inDuel = false;
    // Objekt zur Synchronisation zwischen Threads
    final Object SYNC_OBJECT = new Object();

    // Privater Konstruktor für Singleton-Muster
    private DuelManager() {
    }

    // Methode, um die Singleton-Instanz von DuelManager zu erhalten
    public static DuelManager getManager() {
        if (uniqueInstance == null) {
            uniqueInstance = new DuelManager();
        }
        return uniqueInstance;
    }

    /**
     * Diese Methode beginnt ein Duell zwischen zwei Benutzern.
     * @param newUser Benutzer, der registriert und bekämpft wird.
     * @return Ergebnis des Duells.
     */
    // Methode, um einen Benutzer für ein Duell zu registrieren und die Ergebnisse zurückzugeben
    public String engageAndResolveDuel(User newUser) {
        // Ersten Benutzer registrieren und inDuel auf true setzen
        if (challenger == null) {
            challenger = newUser;
            duelOutcome = null;
            inDuel = true;

            // Synchronisieren am SYNC_OBJECT, um exklusiven Zugriff zu gewährleisten
            synchronized (SYNC_OBJECT) {
                while (inDuel) {
                    try {
                        // Warten auf das Ende des Duells
                        SYNC_OBJECT.wait();
                    } catch (InterruptedException ie) {
                        // Behandlung einer Unterbrechung als Abbruchanforderung
                        break;
                    }
                }
            }
            return duelOutcome;
        }
        // Zweiten Benutzer registrieren und das Duell starten
        else if (defender == null) {
            defender = newUser;
            CardHandler cardHandler = new CardHandler();
            CardDeck challengerDeck = cardHandler.perform_Deck_Retrieve(challenger);
            CardDeck defenderDeck = cardHandler.perform_Deck_Retrieve(defender);
            duelOutcome = executeDuel(challenger, defender, challengerDeck, defenderDeck);
            inDuel = false;

            // Alle wartenden Threads benachrichtigen, dass das Duell beendet ist
            synchronized (SYNC_OBJECT) {
                SYNC_OBJECT.notifyAll();
            }
            // Rücksetzen der Benutzer auf null
            challenger = null;
            defender = null;
            // Ergebnisse des Duells zurückgeben
            return duelOutcome;
        }
        // Wenn beide Benutzer bereits registriert sind, null zurückgeben
        return null;
    }



    /**
     * Führt ein Duell zwischen zwei Benutzern aus und gibt das Duellprotokoll zurück.
     * @param participantOne Der erste Benutzer im Duell.
     * @param participantTwo Der zweite Benutzer im Duell.
     * @param deckOfParticipantOne Kartendeck des ersten Benutzers.
     * @param deckOfParticipantTwo Kartendeck des zweiten Benutzers.
     * @return Das Protokoll des Duells im JSON-Format.
     */
    public String executeDuel(User participantOne, User participantTwo, CardDeck deckOfParticipantOne, CardDeck deckOfParticipantTwo) {
        String duelLog = null;

        // Überprüfung der Eingabeparameter
        if (participantOne != null && participantTwo != null && deckOfParticipantOne != null && deckOfParticipantTwo != null) {
            ObjectMapper jsonMapper = new ObjectMapper();
            ArrayNode duelLogArray = jsonMapper.createArrayNode();
            int roundNumber = 0;

            // Durchführen der Runden bis einer der Decks leer ist oder 100 Runden erreicht sind
            while (!deckOfParticipantOne.isDeckEmpty() && !deckOfParticipantTwo.isDeckEmpty() && roundNumber < 100) {
                ObjectNode roundDetails = generateRoundDetails(jsonMapper, participantOne, participantTwo, deckOfParticipantOne, deckOfParticipantTwo, ++roundNumber);
                duelLogArray.add(roundDetails);
            }

            try {
                // Konvertieren des Duellprotokolls in einen JSON-String
                duelLog = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(duelLogArray);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            // Aktualisierung der Benutzerdaten nach dem Duell
            refreshPlayerStatsPostDuel(participantOne, participantTwo, deckOfParticipantOne, deckOfParticipantTwo);
        }

        // Zurücksetzen der Benutzer und des Duell-Ergebnisses
        this.challenger = null;
        this.defender = null;
        this.duelOutcome = null;

        return duelLog;
    }

    /**
     * Erzeugt Rundendetails für eine Duellrunde.
     */
    private ObjectNode generateRoundDetails(ObjectMapper jsonMapper, User participantOne, User participantTwo, CardDeck deckOfParticipantOne, CardDeck deckOfParticipantTwo, int roundNumber) {
        ObjectNode roundDetails = jsonMapper.createObjectNode();

        // Ziehen einer zufälligen Karte aus jedem Deck
        DuelCard cardFromParticipantOne = deckOfParticipantOne.pickRandomCard();
        DuelCard cardFromParticipantTwo = deckOfParticipantTwo.pickRandomCard();

        // Berechnung des Schadens, den jede Karte verursacht
        float damageByParticipantOne = computeInflictedDamage(cardFromParticipantOne, cardFromParticipantTwo);
        float damageByParticipantTwo = computeInflictedDamage(cardFromParticipantTwo, cardFromParticipantOne);

        // Protokollieren der Rundendetails
        logRoundDetails(roundDetails, participantOne, participantTwo, deckOfParticipantOne, deckOfParticipantTwo, cardFromParticipantOne, cardFromParticipantTwo, damageByParticipantOne, damageByParticipantTwo, roundNumber);
        // Verarbeiten des Rundenergebnisses und Aktualisieren der Decks
        processRoundResult(deckOfParticipantOne, deckOfParticipantTwo, cardFromParticipantOne, cardFromParticipantTwo, damageByParticipantOne, damageByParticipantTwo, roundDetails);

        // Protokollieren der verbleibenden Größe jedes Decks nach der Runde
        roundDetails.put("Deck Size Participant 1 After", deckOfParticipantOne.countDeckCards());
        roundDetails.put("Deck Size Participant 2 After", deckOfParticipantTwo.countDeckCards());

        return roundDetails;
    }


    /**
     * Protokolliert die Rundendetails im Rundenlog.
     */
    private void logRoundDetails(ObjectNode roundLog, User player1, User player2, CardDeck player1Deck, CardDeck player2Deck, DuelCard card1, DuelCard card2, float card1Damage, float card2Damage, int roundCounter) {
        roundLog.put("Runde", roundCounter);
        roundLog.put("Benutzer 1", player1.getName());
        roundLog.put("Benutzer 2", player2.getName());
        roundLog.put("Deckgröße 1", player1Deck.countDeckCards());
        roundLog.put("Deckgröße 2", player2Deck.countDeckCards());
        roundLog.put("Karten-ID 1", card1.getCardId());
        roundLog.put("Karten-ID 2", card2.getCardId());
        roundLog.put("Kartenname 1", card1.getCardName());
        roundLog.put("Kartenname 2", card2.getCardName());
        roundLog.put("Kartenschaden 1", card1Damage);
        roundLog.put("Kartenschaden 2", card2Damage);
    }

    /**
     * Verarbeitet das Ergebnis der Runde und aktualisiert die Kartendecks.
     */
    private void processRoundResult(CardDeck player1Deck, CardDeck player2Deck, DuelCard card1, DuelCard card2, float card1Damage, float card2Damage, ObjectNode roundLog) {
        // Bestimmen des Gewinners der Runde und entsprechende Aktualisierung der Decks
        if (card1Damage > card2Damage) {
            // Wenn der Schaden von Karte 1 größer ist, ist Spieler 1 der Gewinner der Runde
            player2Deck.discardCard(card2);
            player1Deck.addCardToDeck(card2);
            // Gewinner im Rundenlog protokollieren
            roundLog.put("Winner", challenger.getName());
        } else if (card1Damage < card2Damage) {
            // Wenn der Schaden von Karte 2 größer ist, ist Spieler 2 der Gewinner der Runde
            player1Deck.discardCard(card1);
            player2Deck.addCardToDeck(card1);
            roundLog.put("Winner", defender.getName());
        } else {
            roundLog.put("Winner", "Undecided");
        }
    }

    /**
     * Aktualisiert die Statistiken der Spieler nach dem Duell.
     */
    private void refreshPlayerStatsPostDuel(User combatant1, User combatant2, CardDeck combatant1Deck, CardDeck combatant2Deck) {
        // Vergleich der Kartendeckgrößen nach dem Duell
        if (combatant1Deck.countDeckCards() > combatant2Deck.countDeckCards()) {
            // Wenn das Deck von Kämpfer 1 größer ist, gewinnt Kämpfer 1 und Kämpfer 2 verliert
            combatant1.recordWin();
            combatant2.recordLoss();
        } else if (combatant2Deck.countDeckCards() > combatant1Deck.countDeckCards()) {
            // Wenn das Deck von Kämpfer 2 größer ist, gewinnt Kämpfer 2 und Kämpfer 1 verliert
            combatant1.recordLoss();
            combatant2.recordWin();
        } else {
            // Im Falle eines Unentschiedens
            combatant1.recordTie();
            combatant2.recordTie();
        }
    }

    /**
     * Berechnet den zugefügten Schaden zwischen zwei Karten.
     */
    public float computeInflictedDamage(DuelCard aggressor, DuelCard defender) {
        // Überprüfen auf spezielle Schadensbedingungen
        if (appliesSpecialDamage(aggressor)) {
            return getSpecialDamage(aggressor);
        }
        // Kein Schaden unter bestimmten Bedingungen
        if (nullifiesDamage(aggressor, defender)) {
            return 0;
        }
        // Negativer Schaden unter spezifischen Szenarien
        if (causesDamage(aggressor, defender)) {
            return -1;
        }
        // Kein Schaden, wenn der Verteidiger ein Kraken ist
        if (defender.getCategory() == TypeofMonsters.Kraken) {
            return 0;
        }
        // Berechnung des Schadens basierend auf Elementeigenschaften
        return calculateElementalAdvantage(aggressor, defender);
    }


    // Überprüft, ob die Karte speziellen Schaden anwendet.
        private boolean appliesSpecialDamage (DuelCard card){
            // Überprüfen, ob die Monsterkategorie der Karte "magicdice" ist
            return card.getCategory() == TypeofMonsters.magicdice;
        }

// Berechnet speziellen Schaden für die Karte.
        private float getSpecialDamage (DuelCard card){
            Random rnd = new Random();
            // Ein hoher Schaden wird zufällig angewendet
            if (rnd.nextInt(6) + 1 > 3) {
                return 999;
            }
            // Standard-Schaden zurückgeben
            return card.getAttackPoints();
        }

// Überprüft, ob zwischen zwei Karten kein Schaden entsteht.
        private boolean nullifiesDamage (DuelCard attacker, DuelCard defender){
        // Kein Schaden, wenn keine der Karten der Kategorie "Spell" angehört
            return attacker.getCategory() != TypeofMonsters.Spell &&
                    defender.getCategory() != TypeofMonsters.Spell &&
                    nullDamage(attacker.getCategory(), defender.getCategory());
        }

// Überprüft Fälle von Schadensnegierung zwischen zwei Monsterkategorien.
        private boolean nullDamage (TypeofMonsters attackerCategory, TypeofMonsters defenderCategory){
            return (attackerCategory == TypeofMonsters.Dragon && defenderCategory == TypeofMonsters.FireElf) ||
                    (attackerCategory == TypeofMonsters.Goblin && defenderCategory == TypeofMonsters.Dragon) ||
                    (attackerCategory == TypeofMonsters.Ork && defenderCategory == TypeofMonsters.Wizard);
        }

// Überprüft, ob negativer Schaden zwischen zwei Karten entsteht.
        private boolean causesDamage (DuelCard attacker, DuelCard defender){
            // Negativer Schaden tritt auf, wenn der Angreifer ein Ritter ist und der Verteidiger ein Wasserspell
            return attacker.getCategory() == TypeofMonsters.Knight &&
                    defender.getCategory() == TypeofMonsters.Spell &&
                    defender.getElementType() == Elements.Water;
        }

    /**
     * Berechnet den Elementarschaden zwischen zwei Karten.
     */
    private float calculateElementalAdvantage(DuelCard attacker, DuelCard defender) {
        // Basis-Schaden des Angreifers holen
        float baseDamage = attacker.getAttackPoints();

        // Elementtyp des Angreifers und Verteidigers holen
        Elements typeOfAttacker = attacker.getElementType();
        Elements typeOfDefender = defender.getElementType();

        // Überprüfung auf Elementkombinationen, bei denen der Angreifer im Vorteil ist
        // Wenn der Angreifer im Vorteil ist, wird der Schaden verdoppelt
        if ((typeOfAttacker == Elements.Water && typeOfDefender == Elements.Fire) ||
                (typeOfAttacker == Elements.Normal && typeOfDefender == Elements.Water) ||
                (typeOfAttacker == Elements.Fire && typeOfDefender == Elements.Normal)) {
            return baseDamage * 2;
        }

        // Überprüfung auf Elementkombinationen, bei denen der Verteidiger im Vorteil ist
        // Wenn der Verteidiger im Vorteil ist, wird der Schaden halbiert
        if ((typeOfAttacker == Elements.Water && typeOfDefender == Elements.Normal) ||
                (typeOfAttacker == Elements.Fire && typeOfDefender == Elements.Water) ||
                (typeOfAttacker == Elements.Normal && typeOfDefender == Elements.Fire)) {
            return baseDamage / 2;
        }
        // Wenn weder Angreifer noch Verteidiger einen Elementvorteil haben, Basis-Schaden zurückgeben
        return baseDamage;
    }

    /**
     * Holt die Daten der Rangliste.
     */
    public String retrieveScoreboardData() {
        try (Connection dbConnection = Datenbank.getDatabaseInstance().createConnection()) {
            // Ausführen der Abfrage für die Rangliste und das Ergebnis als ArrayNode erhalten
            ArrayNode scoreboardResults = executeScoreboardQuery(dbConnection);
            // Umwandeln des ArrayNode der Ranglistendaten in einen JSON-String und zurückgeben
            return convertToJson(scoreboardResults);
        } catch (SQLException | JsonProcessingException e) {
            // Bei einer SQLException oder JsonProcessingException wird der Stacktrace ausgegeben
            e.printStackTrace();
        }
        // Wenn eine Ausnahme geworfen wurde, null zurückgeben
        return null;
    }


// Methods to execute the scoreboard query and return the results
        private ArrayNode executeScoreboardQuery (Connection connection) throws SQLException {
            String query = "SELECT name, wins, games, elo FROM users WHERE name IS NOT NULL ORDER BY elo DESC;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                return createScoreboardDataArray(resultSet);
            }
        }
// Methods to create an array of scoreboard data from the query result set
            private ArrayNode createScoreboardDataArray (ResultSet resultSet) throws SQLException {
                // Here is created an ObjectMapper, which is used for converting between Java objects and JSON
                ObjectMapper objectMapper = new ObjectMapper();
                // Then is created an ArrayNode that holds the scoreboard data
                ArrayNode scoreboardArray = objectMapper.createArrayNode();
                while (resultSet.next()) {
                    // For each row, create a scoreboard entry as an ObjectNode
                    ObjectNode scoreboardEntry = createScoreboardEntry(objectMapper, resultSet);
                    // Add the scoreboard entry to the ArrayNode
                    scoreboardArray.add(scoreboardEntry);
                }
                // Return the ArrayNode of scoreboard data
                return scoreboardArray;
            }

// Methods to create a scoreboard entry object from the result set
            private ObjectNode createScoreboardEntry (ObjectMapper objectMapper, ResultSet resultSet) throws
            SQLException {
                ObjectNode entry = objectMapper.createObjectNode();
                entry.put("Name", resultSet.getString(1));
                entry.put("Wins", resultSet.getString(2));
                entry.put("Games", resultSet.getString(3));
                entry.put("Elo", resultSet.getString(4));
                return entry;
            }

// Methods to convert an ArrayNode to a JSON string
            private String convertToJson (ArrayNode arrayNode) throws JsonProcessingException {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
            }
}