package Project_Logic.User_Logic;

import Project_Logic.DatenbankandMethods.Datenbank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Verarbeitet benutzerbezogene Vorgänge im System.
 */
public class UserHandler {

    // Singleton instance of the class for global access
    private static UserHandler single_instance = null;

    /**
     * Ruft die Singleton-Instanz von UserHandler ab.
     * Implementiert doppelt überprüfte Sperren für Thread-Sicherheit.
     *
     * @return single_instance of UserHandler
     */
    public static UserHandler getInstance() {
        // Erster Check, um unnötigen Eintritt in den synchronisierten Block zu vermeiden
        if (single_instance == null) {
            // Synchronisiere auf UserHandler.class, um sicherzustellen, dass nur ein Thread das Objekt instanziiert
            synchronized (UserHandler.class) {
                // Zweiter Check, falls ein anderer Thread vorher in den Block eingetreten ist
                if (single_instance == null) {
                    // Instanziere das Objekt, wenn es immer noch null ist
                    single_instance = new UserHandler();
                }
            }
        }
        // Rückgabe der Instanz
        return single_instance;
    }

    /**
     * Autorisiert einen Benutzer basierend auf einem Token und ruft Benutzerdaten ab.
     *
     * @param token Das zur Benutzerauthentifizierung verwendete Token.
     * @return Benutzerobjekt, wenn die Authentifizierung erfolgreich ist, andernfalls null.
     */
    public User givepermissions(String token) {
        // Initialisiere das User-Objekt als null
        User user = null;

        try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
            // Bereite SQL-Query vor, um Benutzerdaten anhand des Tokens abzurufen
            String query = "SELECT username, name, bio, image, coins, games, wins, elo " +
                    "FROM users WHERE token = ? AND islogged = TRUE;";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                // Setze das Token-Parameter für das Prepared Statement
                ps.setString(1, token);

                // Führe die Abfrage aus und erhalte die Ergebnisse
                ResultSet rs = ps.executeQuery();

                // Erstelle ein User-Objekt und befülle es mit den abgerufenen Daten, falls ein Ergebnis gefunden wurde
                if (rs.next()) {
                    user = perform_Usercreation_FromResult(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Rückgabe des User-Objekts, falls gefunden, sonst null
        return user;
    }

    // Hilfsmethode, um ein User-Objekt aus einem ResultSet zu erstellen
    private User perform_Usercreation_FromResult(ResultSet rs) throws SQLException {
        // Extrahiere Daten aus dem ResultSet und erstelle ein User-Objekt
        String username = rs.getString(1);
        String name = rs.getString(2);
        String bio = rs.getString(3);
        String image = rs.getString(4);
        int coins = rs.getInt(5);
        int games = rs.getInt(6);
        int wins = rs.getInt(7);
        int elo = rs.getInt(8);

        // Rückgabe des befüllten User-Objekts
        return new User(username, name, bio, image, coins, games, wins, elo);
    }

    // Diese Methode überprüft, ob der Benutzer ein Administrator ist
    public boolean perform_Verificationofrole(String token) {
        // Initialisiere das Admin-Flag als false
        boolean admin = false;

        try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
            // Bereite SQL-Query vor, um zu überprüfen, ob der Benutzer anhand des Tokens ein Admin ist
            String query = "SELECT COUNT(username) FROM users " +
                    "WHERE token = ? AND admin = TRUE AND islogged = TRUE;";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                // Setze das Token-Parameter für das Prepared Statement
                ps.setString(1, token);

                // Führe die Abfrage aus und erhalte die Ergebnisse
                ResultSet rs = ps.executeQuery();

                // Wenn ein Ergebnis gefunden wurde und die Anzahl 1 ist, setze das Admin-Flag auf true
                if (rs.next() && rs.getInt(1) == 1) {
                    admin = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Rückgabe des Admin-Flags
        return admin;
    }


    /**
     * Registriert einen neuen Benutzer mit dem angegebenen Benutzernamen und Passwort.
     * Generiert einen Token für den Benutzer und überprüft, ob der Benutzername bereits existiert.
     *
     * @param username Der Benutzername für den neuen Benutzer.
     * @param password Das Passwort für den neuen Benutzer.
     * @return true, wenn der Benutzer erfolgreich registriert wird, sonst false.
     */
    public boolean perform_User_Registartion(String username, String password) {
        System.out.println("Benutzerregistrierung gestartet");
        // Erzeugt einen Token für den Benutzer
        String token = "Bearer " + username + "-mtcgToken";

        try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
            // Überprüfen, ob der Benutzer bereits existiert
            if (Exists_user(conn, username)) {
                System.out.println("Benutzer ist bereits registriert!");
                return false;
            }

            // Setzt das Admin-Flag, wenn der Benutzername 'admin' ist
            boolean admin = username.equals("admin");

            // Registriert den Benutzer in der Datenbank
            return perform_User_insert(conn, username, password, token, admin);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Hilfsmethode, um zu überprüfen, ob ein Benutzer bereits in der Datenbank existiert.
     *
     * @param conn Verbindung zur Datenbank.
     * @param username Der zu überprüfende Benutzername.
     * @return true, wenn der Benutzer existiert, sonst false.
     * @throws SQLException Bei einem Datenbankzugriffsfehler.
     */
    private boolean Exists_user(Connection conn, String username) throws SQLException {
        String query = "SELECT COUNT(username) FROM users WHERE username = ?;";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            // Überprüft, ob der Benutzer bereits existiert
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Methode zum Einfügen eines neuen Benutzers in die Datenbank
    private boolean perform_User_insert(Connection conn, String username, String password, String token, boolean admin) throws SQLException {
        // Entscheidet, welches SQL-Query basierend auf dem Admin-Status zu verwenden ist
        String query = admin ? "INSERT INTO users(username, password, token, admin) VALUES(?,?,?,TRUE);"
                : "INSERT INTO users(username, password, token) VALUES(?,?,?);";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            // Setzt die Parameter für das PreparedStatement
            ps.setString(1, username); // Benutzername
            ps.setString(2, password); // Passwort
            ps.setString(3, token);    // Token
            // Führt das Update aus und speichert die Anzahl der betroffenen Zeilen
            int affectedRows = ps.executeUpdate();

            // Gibt true zurück, wenn mindestens eine Zeile betroffen ist
            return affectedRows != 0;
        }
    }

    // Methode zum Einloggen eines Benutzers mit Benutzername und Passwort
    public boolean perform_User_SigningIn(String username, String password) {
        // SQL-Query, das 'islogged' auf TRUE setzt für den spezifizierten Benutzer
        String query = "UPDATE users SET islogged = TRUE WHERE username = ? AND password = ?;";

        try (Connection conn = Datenbank.getDatabaseInstance().createConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Setzt Benutzername und Passwort für das PreparedStatement
            ps.setString(1, username);
            ps.setString(2, password);
            // Führt das Update aus und speichert die Anzahl der betroffenen Zeilen
            int affectedRows = ps.executeUpdate();

            // Wenn eine Zeile betroffen ist, war das Login erfolgreich
            if (affectedRows == 1) {
                System.out.println("Benutzer eingeloggt: " + username);
                return true;
            }
        } catch (SQLException e) {
            // Im Falle einer SQL-Exception, Stacktrace ausgeben und false zurückgeben
            e.printStackTrace();
            return false;
        }

        return false;
    }

    // Methode zum Ausloggen eines Benutzers mit Benutzername und Passwort
    public boolean perform_User_SigningOut(String username, String password) {
        // SQL-Query, das 'islogged' auf FALSE setzt für den spezifizierten Benutzer
        String query = "UPDATE users SET islogged = FALSE WHERE username = ? AND password = ?;";

        try (Connection conn = Datenbank.getDatabaseInstance().createConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Setzt Benutzername und Passwort für das PreparedStatement
            ps.setString(1, username);
            ps.setString(2, password);
            // Führt das Update aus und speichert die Anzahl der betroffenen Zeilen
            int affectedRows = ps.executeUpdate();

            // Gibt true zurück, wenn mindestens eine Zeile betroffen ist
            if (affectedRows == 1) {
                return true;
            }
        } catch (SQLException e) {
            // Im Falle einer SQL-Exception, Stacktrace ausgeben und false zurückgeben
            e.printStackTrace();
            return false;
        }

        return false;
    }




}
