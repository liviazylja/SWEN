package Server_Connection.Request_Logic;

import java.util.HashMap;
import java.util.Map;



/**
 * Klasse für HTTP Anfragen.
 * Diese Klasse speichert Details einer HTTP Anfrage wie Methode, Ressource, Version, Header und Body.
 * Sie ermöglicht die Bearbeitung und Analyse von HTTP-Anfragen.
 */
public class Req_Http {

    // Variablen für HTTP Anfrage Details
    private String httpRequestMethod; // HTTP-Methode, z.B. GET, POST
    private String httpRequestResource; // Zielressource der Anfrage, z.B. URL-Pfad
    private String httpProtocolVersion; // HTTP-Version, z.B. HTTP/1.1
    private Map<String, String> httpRequestHeaders; // Karte für Header-Werte
    private String httpRequestBody; // Textkörper der Anfrage

    /**
     * Konstruktor zum Initialisieren der Header Map.
     * Erstellt eine neue leere Map für die Header.
     */
    public Req_Http() {
        this.httpRequestHeaders = new HashMap<>();
    }

    // Getter und Setter Methoden

    /**
     * Gibt die HTTP-Methode zurück.
     * @return String Methode der Anfrage
     */
    public String getHttpRequestMethod() {
        return this.httpRequestMethod;
    }

    /**
     * Setzt die HTTP-Methode.
     * @param method Methode der Anfrage
     */
    public void setHttpRequestMethod(String method) {
        this.httpRequestMethod = method;
    }

    /**
     * Gibt die Ressource der Anfrage zurück.
     * @return String Ressource der Anfrage
     */
    public String getHttpRequestResource() {
        return this.httpRequestResource;
    }

    /**
     * Setzt die Ressource der Anfrage.
     * @param resource Ressource der Anfrage
     */
    public void setHttpRequestResource(String resource) {
        this.httpRequestResource = resource;
    }

    /**
     * Gibt die HTTP-Version zurück.
     * @return String HTTP-Version
     */
    public String getHttpProtocolVersion() {
        return this.httpProtocolVersion;
    }

    /**
     * Setzt die HTTP-Version.
     * @param version HTTP-Version
     */
    public void setHttpProtocolVersion(String version) {
        this.httpProtocolVersion = version;
    }

    /**
     * Gibt die Header der Anfrage zurück.
     * @return Map<String, String> Map der Anfrageheader
     */
    public Map<String, String> getHttpRequestHeaders() {
        return this.httpRequestHeaders;
    }

    /**
     * Gibt den Body der Anfrage zurück.
     * @return String Anfrage Body
     */
    public String getHttpRequestBody() {
        return this.httpRequestBody;
    }

    /**
     * Setzt den Body der Anfrage.
     * @param body Body der Anfrage
     */
    public void setHttpRequestBody(String body) {
        this.httpRequestBody = body;
    }

    /**
     * Methode zum Hinzufügen eines Schlüssel-Wert-Paares zu den HTTP-Headern.
     * @param key Schlüssel des Header-Elements
     * @param value Wert des Header-Elements
     */
    public void addHttpRequestHeader(String key, String value) {
        this.httpRequestHeaders.put(key, value);
    }

    /**
     * Setzt die Header der Anfrage.
     * @param headers Map von Header Schlüssel-Wert-Paaren
     */
    public void setHttpRequestHeaders(Map<String, String> headers) {
        this.httpRequestHeaders = headers;
    }

    /**
     * Methode zur Ermittlung der Inhaltslänge aus den HTTP-Headern.
     * Überprüft, ob der Header "content-length" vorhanden ist und gibt den Wert zurück.
     * @return int Länge des Inhalts, 0 wenn nicht vorhanden oder ungültig
     */
    public int calculateLength() {
        if (this.httpRequestHeaders.containsKey("content-length:")) {
            try {
                return Integer.parseInt(this.httpRequestHeaders.get("content-length:"));
            } catch (NumberFormatException e) {
                System.out.println("No Valid Integer");
            }
        }
        return 0;
    }
}



