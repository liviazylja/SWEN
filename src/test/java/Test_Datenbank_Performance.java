import Project_Logic.DatenbankandMethods.Datenbank;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class Test_Datenbank_Performance {

    /**
     * Dieser Testfall überprüft, ob die Singleton-Instanz der DB korrekt erstellt wird und ob getConnection() eine gültige Verbindung zurückgibt.
     */
    @Test
    public void testDBSingletonAndConnection() {
        // Singleton-Instanz der DB-Klasse holen
        Datenbank dbInstance = Datenbank.getDatabaseInstance();
        assertNotNull(dbInstance, "DB-Instanz sollte nicht null sein");

        // Verbindung von der DB-Instanz holen
        Connection connection = dbInstance.createConnection();
        assertNotNull(connection, "Verbindung sollte nicht null sein");

        // Überprüfen, ob die Verbindung gültig ist
        try {
            assertTrue(connection.isValid(5), "Verbindung sollte gültig sein");
        } catch (SQLException e) {
            fail("Unerwartete SQLException während der isValid()-Überprüfung");
        }
    }

    /**
     * Dieser Testfall überprüft, ob mehrfache Aufrufe von getDatabaseInstance() dieselbe Instanz zurückgeben.
     */
    @Test
    public void testSingletonInstance() {
        // Singleton-Instanz der DB-Klasse holen
        Datenbank firstInstance = Datenbank.getDatabaseInstance();
        assertNotNull(firstInstance, "Erste DB-Instanz sollte nicht null sein");

        // Eine weitere Singleton-Instanz der DB-Klasse holen
        Datenbank secondInstance = Datenbank.getDatabaseInstance();
        assertNotNull(secondInstance, "Zweite DB-Instanz sollte nicht null sein");

        // Überprüfen, ob beide Instanzen das gleiche Objekt sind
        assertSame(firstInstance, secondInstance, "Beide Instanzen sollten dasselbe Objekt sein");
    }
}


