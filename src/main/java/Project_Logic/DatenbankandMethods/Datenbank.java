package Project_Logic.DatenbankandMethods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton-Klasse für den Datenbankzugriff.
 */
public class Datenbank {

    // Singleton-Instanz der Datenbank
    private static Datenbank databaseInstance;

    // Authentifizierungsdaten
    private static final String databaseUrl = "jdbc:postgresql://localhost/postgres?currentschema=public";
    private static final String databaseUser = "postgres";
    private static final String databasePassword = "12345678";

    /**
     * Methode zur Erlangung der Singleton-Instanz.
     */
    public static Datenbank getDatabaseInstance() {
        if (Datenbank.databaseInstance == null) {
            Datenbank.databaseInstance = new Datenbank();
        }
        return Datenbank.databaseInstance;
    }

    /**
     * Methode zum Herstellen einer Verbindung zur Datenbank.
     * Gibt eine Connection-Instanz zurück oder null bei einem Fehler.
     */
    public Connection createConnection() {
        try {
            return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }
}
