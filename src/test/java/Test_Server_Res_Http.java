import Server_Connection.Request_Logic.Req_Http;
import org.junit.jupiter.api.Test;
import Server_Connection.Request_Logic.Req_Processor;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Test_Server_Res_Http {

    /**
     * Dies ist ein Unit-Testfall zur Überprüfung der Funktionalität der Extraktion und Verarbeitung von Req_Http aus einer Eingabe.
     */
    @Test
    void httpRequestExtraction() {
        // Anordnen: Setzen Sie die Testbedingungen

        // Generieren Sie eine Beispiel-HTTP-Anforderungszeichenkette
        String exampleHTTPRequest = generateExampleHTTPRequest();

        // Erstellen Sie einen BufferedReader, der mit der Beispiel-HTTP-Anforderungszeichenkette verknüpft ist
        BufferedReader readerForInput = new BufferedReader(new StringReader(exampleHTTPRequest));

        // Generieren Sie eine Karte der erwarteten Header, die in dieser Anforderung gefunden werden sollen
        Map<String, String> anticipatedHeaders = generateAnticipatedHeaders();

        // Erstellen Sie eine Instanz von Req_Processor unter Verwendung des Eingabe-Readers
        Req_Processor requestInterpreter = new Req_Processor(readerForInput);

        // Ausführen: Führen Sie die Funktionalität aus, die wir testen

        // Verwenden Sie den Parser, um die HTTP-Anforderung zu interpretieren und Req_Http zu extrahieren
        Req_Http derivedRequestContext = requestInterpreter.processHttpRequest();

        // Überprüfen: Überprüfen Sie die Testergebnisse

        // Bestätigen Sie, dass das extrahierte Req_Http unseren Erwartungen entspricht
        verifyHttpRequest(derivedRequestContext, anticipatedHeaders);
    }

    // Hilfsmethode zum Erstellen einer Beispiel-HTTP-Anforderung
    private String generateExampleHTTPRequest() {
        return "GET /messages/cards HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Key: value\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 8\r\n" +
                "\r\n" +
                "{id:123}";
    }

    // Hilfsmethode zum Erstellen einer Karte der erwarteten Header
    private Map<String, String> generateAnticipatedHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("host:", "localhost");
        headers.put("key:", "value");
        headers.put("content-type:", "application/json");
        headers.put("content-length:", "8");
        return headers;
    }

    // Hilfsmethode zum Überprüfen der Eigenschaften von Req_Http
    private void verifyHttpRequest(Req_Http derivedRequestContext, Map<String, String> anticipatedHeaders) {
        assertEquals("GET", derivedRequestContext.getHttpRequestMethod());
        assertEquals("/messages/cards", derivedRequestContext.getHttpRequestResource());
        assertEquals("HTTP/1.1", derivedRequestContext.getHttpProtocolVersion());
        assertEquals(anticipatedHeaders, derivedRequestContext.getHttpRequestHeaders());
        assertEquals("{id:123}", derivedRequestContext.getHttpRequestBody());
    }
}

