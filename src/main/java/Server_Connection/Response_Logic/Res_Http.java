package Server_Connection.Response_Logic;

/**
 * Klasse zur Darstellung von HTTP-Antworten.
 * Diese Klasse kapselt die Details einer HTTP-Antwort ein, einschließlich Protokoll, Statuscode, Serverinformationen, MIME-Typ und Antworttext.
 */
public class Res_Http {
    // Variablen zur Speicherung der HTTP-Antwortdetails
    private String protocolVersion;
    private String responseStatus;
    private String serverIdentifier;
    private String contentType;
    private int bodySize;
    private String bodyContent;

    /**
     * Konstruktor zur Initialisierung der HTTP-Antwort mit einem bestimmten Statuscode.
     * Setzt Standardwerte für Protokoll, Serverinfo und MIME-Typ.
     * @param statusCode Der HTTP-Statuscode für die Antwort.
     */
    public Res_Http(String statusCode) {
        // Standardwerte werden gesetzt
        this.protocolVersion = "HTTP/1.1";
        this.responseStatus = statusCode;
        this.serverIdentifier = "mtcg-server";
        this.contentType = "application/json";
        this.bodyContent = "";
        this.bodySize = 0;
    }

    // Getter-Methoden für die Antwortattribute

    // Gibt die Protokollversion zurück
    public String getProtocolVersion() {
        return protocolVersion;
    }

    // Gibt den Statuscode zurück
    public String getResponseStatus() {
        return responseStatus;
    }

    // Gibt den Server-Identifikator zurück
    public String getServerIdentifier() {
        return serverIdentifier;
    }

    // Gibt den MIME-Typ zurück
    public String getContentType() {
        return contentType;
    }

    // Gibt die Größe des Antworttextes zurück
    public int getBodySize() {
        return bodySize;
    }

    // Gibt den Antworttext zurück
    public String getBodyContent() {
        return bodyContent;
    }

    // Setter-Methoden für Antwortattribute

    /**
     * Setzt den Statuscode der HTTP-Antwort.
     * @param statusCode Der zu setzende HTTP-Statuscode.
     */
    public void setResponseStatus(String statusCode) {
        this.responseStatus = statusCode;
    }



    /**
     * Setzt den Inhalt des Antworttextes und aktualisiert automatisch dessen Größe.
     * @param responseBody Der als Antworttext festzulegende Inhalt.
     */
    public void setBodyContent(String responseBody) {
        this.bodyContent = responseBody;
        this.bodySize = responseBody.length();
    }

    /**
     * Generiert einen formatierten HTTP-Antwortstring.
     * Derzeit nicht funktional, könnte jedoch für Debugging oder Protokollierung verwendet werden.
     * @return String, der die komplette HTTP-Antwort darstellt.
     */
    public String generateResponseString() {
        // Erstellen eines StringBuilder für die Antwort
        StringBuilder response = new StringBuilder();
        // Zusammenfügen der Antwortelemente
        response.append(protocolVersion).append(" ").append(responseStatus).append("\n");
        response.append("Server: ").append(serverIdentifier).append("\n");
        response.append("Content-Type: ").append(contentType).append("\n");
        response.append("Content-Length: ").append(bodySize).append("\n");
        response.append("\n").append(bodyContent);
        return response.toString();
    }
}


