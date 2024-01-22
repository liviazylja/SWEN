package Server_Connection.Request_Logic;


import java.io.BufferedReader;
import java.io.IOException;



/**
 * Verarbeitet HTTP-Anfragen von einer BufferedReader-Quelle.
 * Diese Klasse interpretiert die eingehenden HTTP-Daten und erstellt ein Req_Http-Objekt.
 */
public class Req_Processor {

    // BufferedReader zum Lesen der eingehenden Anfrage
    private BufferedReader bufferedReader;

    /**
     * Konstruktor zur Initialisierung des BufferedReader für das Lesen von HTTP-Anfragen.
     * @param bufferedReader Quelle zum Lesen der HTTP-Anfrage.
     */
    public Req_Processor(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    /**
     * Interpretiert die HTTP-Anfrage und erstellt ein Req_Http-Objekt.
     * @return Req_Http Objekt mit den geparsten Anfragedaten.
     */
    public Req_Http processHttpRequest() {
        try {
            // Kopfzeile der HTTP-Anfrage parsen
            Req_Http httpRequest = parseHttpRequestHeader();

            if (httpRequest != null) {
                // Länge des Inhalts bestimmen und den Body parsen
                int contentLength = httpRequest.calculateLength();
                String body = parseHttpRequestBody(contentLength);
                httpRequest.setHttpRequestBody(body);
            }

            return httpRequest;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parst den HTTP-Header und erstellt ein Req_Http-Objekt.
     * @return Req_Http Objekt mit den geparsten Header-Daten.
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    private Req_Http parseHttpRequestHeader() throws IOException {
        Req_Http httpRequest = new Req_Http();

        // Liest die erste Zeile der HTTP-Anfrage
        String line = bufferedReader.readLine();
        if (line == null) return null;

        // Zerlegt die Anfragezeile in ihre Bestandteile
        String[] requestLineComponents = line.split(" ");
        if (requestLineComponents.length != 3) return null;

        // Setzt Methode, Ressource und Protokollversion
        httpRequest.setHttpRequestMethod(requestLineComponents[0]);
        httpRequest.setHttpRequestResource(requestLineComponents[1]);
        httpRequest.setHttpProtocolVersion(requestLineComponents[2]);

        // Verarbeitung der Header-Zeilen
        while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
            String[] headerComponents = line.split(" ", 2);
            if (headerComponents.length == 2) {
                httpRequest.addHttpRequestHeader(headerComponents[0].toLowerCase(), headerComponents[1]);
            }
        }

        return httpRequest;
    }

    /**
     * Parst den HTTP-Body basierend auf der Inhaltslänge.
     * @param contentLength Die Länge des zu lesenden Inhalts.
     * @return String Der geparste Body der HTTP-Anfrage.
     * @throws IOException Wenn ein I/O-Fehler auftritt oder die Länge des Bodys nicht übereinstimmt.
     */
    private String parseHttpRequestBody(int contentLength) throws IOException {
        char[] body = new char[contentLength];
        int bytesRead = 0;

        // Liest den Body bis zur angegebenen Länge
        while (bytesRead < contentLength) {
            int readChars = bufferedReader.read(body, bytesRead, contentLength - bytesRead);
            if (readChars == -1) throw new IOException("Incomplete HTTP body received.");
            bytesRead += readChars;
        }

        return new String(body);
    }
}



