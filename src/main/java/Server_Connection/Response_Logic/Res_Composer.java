package Server_Connection.Response_Logic;

import Project_Logic.CardandDuel_Logic.DuelCard;
import Project_Logic.CardandDuel_Logic.CardHandler;
import Project_Logic.CardandDuel_Logic.Trading;
import Project_Logic.DatenbankandMethods.Datenbank;
import Project_Logic.User_Logic.User;
import Project_Logic.User_Logic.UserHandler;
import Server_Connection.Request_Logic.Req_Http;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import Project_Logic.CardandDuel_Logic.DuelManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Klasse zum Erstellen von HTTP-Antworten.
 * Verarbeitet eine HTTP-Anfrage und generiert die entsprechende Antwort.
 */
public class Res_Composer {
    private BufferedWriter writer;


    // Konstruktor, der einen BufferedWriter annimmt
    public Res_Composer(BufferedWriter writer) {
        this.writer = writer;
    }
    // Methode zum Generieren und Senden der HTTP-Antwort
    public void generateResponse(Req_Http request) {
        Res_Http response;
        // Prüfen, ob die Anfrage oder deren Header null sind
        if (request == null || request.getHttpRequestHeaders() == null) {
            response = new Res_Http("400 Bad Request");
            sendResponse(response, writer);
            return;
        }
        // Aufteilen der Anfrage-URL in Teile
        String[] urlParts = request.getHttpRequestResource().split("/");
        if (urlParts.length < 2) {
            response = new Res_Http("400 Bad Request");
            sendResponse(response, writer);
            return;
        }
        // Identifizieren der Hauptressource aus der URL
        String mainResource = urlParts[1];
        User user;


        // Verwenden von switch-Anweisungen, um auf verschiedene Ressourcen zu reagieren
        switch (mainResource) {
            case "delete":
                response = removeAllData(request);
                break;
            case "users":
                response = handleUserRequests(request);
                break;
            case "sessions":
                response = handleSessionRequests(request);
                break;
            case "packages":
                response = handlePackageRequests(request);
                break;
            default:
                response = handleOtherRequests(request, urlParts, mainResource);
                break;
        }

        // Senden der Antwort mit dem BufferedWriter
        sendResponse(response, writer);
    }

    // Methode zur Behandlung von Anfragen, die nicht direkt im Switch-Case abgefangen werden
    private Res_Http handleOtherRequests(Req_Http request, String[] urlParts, String mainResource) {
        User user = authenticateUser(request);

        if (mainResource.equals("transactions") && urlParts.length == 3 && "packages".equals(urlParts[2])) {
            return (user != null) ? handlePackageExchange(user, request) : UnpermittedResponse_composer("Access denied");
        } else if (mainResource.equals("cards")) {
            return (user != null) ? displayUserCards(user, request) : UnpermittedResponse_composer("Access denied");
        } else if (mainResource.equals("deck")) {
            return (user != null) ? handleDeckRequest(user, request) : UnpermittedResponse_composer("Access denied");
        } else if (mainResource.equals("stats")) {
            return (user != null) ? handleUserInfoRequest(user, request) : UnpermittedResponse_composer("Access denied");
        } else if (mainResource.equals("score")) {
            return (user != null) ? displayDuelResults(request) : UnpermittedResponse_composer("Access denied");
        } else if (mainResource.equals("tradings")) {
            return (user != null) ? manageCardTrading(request, user) : UnpermittedResponse_composer("Access denied");
        } else if (mainResource.equals("battles")) {
            return (user != null) ? executeDuel(request, user) : UnpermittedResponse_composer("Access denied");
        } else {
            return new Res_Http("400 Bad Request");
        }
    }



    /**
     * Erstellt eine nicht autorisierte Antwort.
     * @param message Die Nachricht für den Antwortkörper.
     * @return Res_Http Das Antwortobjekt mit Status 401.
     */
    private static Res_Http UnpermittedResponse_composer(String message) {
        Res_Http unauthorizedResponse = new Res_Http("401 Unauthorized");
        unauthorizedResponse.setBodyContent(message);
        return unauthorizedResponse;
    }

    /**
     * Sendet die HTTP-Antwort.
     * @param response Das zu sendende Res_Http-Objekt.
     * @param writer BufferedWriter zum Schreiben der Antwort.
     */
    private static void sendResponse(Res_Http response, BufferedWriter writer) {
        try {
            StringBuilder responseBuilder = new StringBuilder();

            responseBuilder.append(response.getProtocolVersion()).append(" ")
                    .append(response.getResponseStatus()).append("\r\n");
            responseBuilder.append("Server: ").append(response.getServerIdentifier()).append("\r\n");
            responseBuilder.append("Content-Type: ").append(response.getContentType()).append("\r\n");
            responseBuilder.append("Content-Length: ").append(response.getBodySize()).append("\r\n\r\n");
            responseBuilder.append(response.getBodyContent());

            writer.write(responseBuilder.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Löscht alle Daten in der Datenbank.
     * @param request Das eingehende Req_Http-Objekt.
     * @return Res_Http Antwortobjekt, das den Erfolg oder Misserfolg der Operation anzeigt.
     */
    private Res_Http removeAllData(Req_Http request) {
        Res_Http resultResponse = new Res_Http("400 Bad Request");
        UserHandler userHandler = UserHandler.getInstance();

        // Überprüfen, ob die Anfragemethode DELETE ist
        if ("DELETE".equals(request.getHttpRequestMethod())) {
            try (Connection conn = Datenbank.getDatabaseInstance().createConnection()) {
                String[] targetTables = {"packages", "marketplace", "cards", "users"};

                for (String table : targetTables) {
                    try (PreparedStatement statement = conn.prepareStatement("DELETE FROM " + table + ";")) {
                        statement.executeUpdate();
                    }
                }

                resultResponse.setResponseStatus("200 OK");
                resultResponse.setBodyContent("Deleted Succesfully");
            } catch (SQLException e) {
                e.printStackTrace();
                resultResponse.setResponseStatus("409 Conflict");
                resultResponse.setBodyContent("An Issue by Deleting");
            }
        }

        return resultResponse;
    }


    /**
     * Verarbeitet Benutzeranfragen und gibt die entsprechende HTTP-Antwort zurück.
     * @param request Das Req_Http-Objekt der eingehenden Anfrage.
     * @return Res_Http Antwortobjekt basierend auf der Benutzeranfrage.
     */
    private Res_Http handleUserRequests(Req_Http request) {

        // Initialisiere Standardantwort als "400 Bad Request"
        Res_Http userResponse = new Res_Http("400 Bad Request");
        UserHandler userHandler = UserHandler.getInstance();

        // Ermittle die Methode der Anfrage (GET, POST, PUT)
        String methodType = request.getHttpRequestMethod();

        // Teile die Anfrage-URL in ihre Bestandteile
        String[] urlParts = request.getHttpRequestResource().split("/");

        // Authentifiziere den Benutzer basierend auf der Anfrage
        User authenticatedUser = authenticateUser(request);

        // JSON-Parser für das Parsen von Anfragedaten
        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            // Behandle GET-Anfragen
            if ("GET".equals(methodType)) {
                // Überprüfe, ob Benutzer authentifiziert ist und URL korrekt formatiert ist
                if (authenticatedUser != null && urlParts.length == 3 && authenticatedUser.getUsername().equals(urlParts[2])) {
                    String userInfo = authenticatedUser.info();
                    // Setze Antwortstatus basierend auf Verfügbarkeit der Benutzerinformationen
                    userResponse.setResponseStatus(userInfo != null ? "200 OK" : "404 Not Found");
                    // Setze den Inhalt der Antwort (Benutzerinformationen oder Fehlermeldung)
                    userResponse.setBodyContent(userInfo != null ? userInfo : "User not found");
                }
            }
            // Behandle POST-Anfragen
            else if ("POST".equals(methodType)) {
                JsonNode requestBody = jsonMapper.readTree(request.getHttpRequestBody());
                if (requestBody.has("Username") && requestBody.has("Password")) {
                    boolean registrationStatus = userHandler.perform_User_Registartion(requestBody.get("Username").asText(), requestBody.get("Password").asText());
                    userResponse.setResponseStatus(registrationStatus ? "201 Created" : "409 Conflict");
                    userResponse.setBodyContent(registrationStatus ? "User successfully created" : "Username already exists");
                }
            }
            // Behandle PUT-Anfragen
            else if ("PUT".equals(methodType)) {
                if (authenticatedUser != null && urlParts.length == 3 && authenticatedUser.getUsername().equals(urlParts[2])) {
                    JsonNode requestBody = jsonMapper.readTree(request.getHttpRequestBody());
                    if (requestBody.has("Name") && requestBody.has("Bio") && requestBody.has("Image")) {
                        boolean updateStatus = authenticatedUser.performupdateOnInfos(requestBody.get("Name").asText(), requestBody.get("Bio").asText(), requestBody.get("Image").asText());
                        userResponse.setResponseStatus(updateStatus ? "200 OK" : "404 Not Found");
                        userResponse.setBodyContent(updateStatus ? "User information updated successfully" : "User not found");
                    } else {
                        // Fehlende erforderliche Felder im Anfragekörper
                        userResponse.setResponseStatus("400 Bad Request");
                        userResponse.setBodyContent("Missing required fields for user update");
                    }
                } else {
                    // Zugriff verweigert, falls Benutzer nicht authentifiziert ist
                    userResponse.setResponseStatus("401 Unauthorized");
                    userResponse.setBodyContent("Access denied");
                }
            }
        } catch (IOException e) {
            // Fehlerbehandlung für Ausnahmen
            e.printStackTrace();
            userResponse.setResponseStatus("500 Internal Server Error");
            userResponse.setBodyContent("Internal server error");
        }

        // Gebe die erstellte HTTP-Antwort zurück
        return userResponse;
    }




    /**
     * Verarbeitet Sitzungsanfragen für Benutzeranmeldung und -abmeldung.
     * @param request Das Req_Http-Objekt der eingehenden Anfrage.
     * @return Res_Http Antwortobjekt für die Sitzungsanfrage.
     */
    private Res_Http handleSessionRequests(Req_Http request) {
        // Instanz des UserHandler für Benutzerverwaltung
        UserHandler userSessionManager = UserHandler.getInstance();

        // Standard-Antwort initialisieren
        Res_Http sessionResponse = new Res_Http("400 Bad Request");

        // JSON-Parser für die Anfragekörper
        ObjectMapper jsonParser = new ObjectMapper();

        // Verarbeitung einer POST-Anfrage für die Benutzeranmeldung
        String methodType = request.getHttpRequestMethod();
        if ("POST".equals(methodType)) {
            try {
                // Anfragekörper als JSON-Objekt lesen
                JsonNode requestBody = jsonParser.readTree(request.getHttpRequestBody());

                // Überprüfung auf Vorhandensein von Benutzername und Passwort
                if (requestBody.has("Username") && requestBody.has("Password")) {
                    String username = requestBody.get("Username").asText();
                    String password = requestBody.get("Password").asText();

                    // Versuch, den Benutzer anzumelden
                    if (userSessionManager.perform_User_SigningIn(username, password)) {
                        sessionResponse.setResponseStatus("200 OK");
                        sessionResponse.setBodyContent("User is logged in.");
                    }
                    else {
                        sessionResponse.setResponseStatus("401 Unauthorized");
                        sessionResponse.setBodyContent("Username or Password are Invalid.");
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Verarbeitung einer DELETE-Anfrage für die Benutzerabmeldung
        else if ("DELETE".equals(methodType)) {
            try {
                JsonNode requestBody = jsonParser.readTree(request.getHttpRequestBody());
                if (requestBody.has("Username") && requestBody.has("Password")) {
                    String username = requestBody.get("Username").asText();
                    String password = requestBody.get("Password").asText();

                    // Versuch, den Benutzer abzumelden
                    if (userSessionManager.perform_User_SigningOut(username, password)) {
                        sessionResponse.setResponseStatus("200 OK");
                        sessionResponse.setBodyContent("User is logged out.");
                    } else {
                        sessionResponse.setResponseStatus("401 Unauthorized");
                        sessionResponse.setBodyContent("Username or Password are Invalid..");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sessionResponse;
    }

    /**
     * Verarbeitet Anfragen für Paketoperationen.
     * @param request Das Req_Http-Objekt der eingehenden Anfrage.
     * @return Res_Http Antwortobjekt für die Paketanfrage.
     */
    private Res_Http handlePackageRequests(Req_Http request) {
        Res_Http packageResponse = new Res_Http("400 Bad Request");

        // Überprüft, ob die Anfragemethode POST ist
        if (!"POST".equals(request.getHttpRequestMethod())) {

            // Nur POST-Anfragen sind für diese Operation erlaubt
            return packageResponse;
        }

        // Überprüfung der Benutzerautorisierung
        String authorizationHeader = request.getHttpRequestHeaders().get("authorization:");

        // Wenn kein Autorisierungsheader vorhanden ist oder die Rolle nicht verifiziert werden kann
        if (authorizationHeader == null || !UserHandler.getInstance().perform_Verificationofrole(authorizationHeader)) {
            packageResponse.setResponseStatus("403 Forbidden");
            packageResponse.setBodyContent("Access is denied");
            return packageResponse;
        }

        // Karten aus dem Anfragekörper parsen
        List<DuelCard> cardList = extractCardsFromJson(request.getHttpRequestBody());
        if (cardList == null || cardList.size() != 5) {
            return packageResponse;
        }

        // Versucht, Karten zu registrieren und ein Paket zu erstellen
        if (!processCardRegistrationAndPackageCreation(cardList)) {

            // Wenn die Registrierung oder Paketerstellung fehlschlägt
            revertCardRegistration(cardList);
        } else {
            // Erfolgreiche Erstellung des Pakets
            packageResponse.setResponseStatus("201 Created");
            packageResponse.setBodyContent("Package is created.");
        }

        return packageResponse;
    }

    /**
     * Parst Karten aus dem Anfragekörper.
     * @param requestBody Der JSON-String mit den Karteninformationen.
     * @return List<DuelCard> Liste von DuelCard-Objekten.
     */
    private List<DuelCard> extractCardsFromJson(String requestBody) {
        ObjectMapper jsonParser = new ObjectMapper();
        jsonParser.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        try {
            return jsonParser.readValue(requestBody, new TypeReference<List<DuelCard>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registriert Karten und erstellt ein Kartenpaket.
     * @param cards Liste der zu registrierenden DuelCards.
     * @return boolean True, wenn die Registrierung und Paketerstellung erfolgreich waren.
     */
    private boolean processCardRegistrationAndPackageCreation(List<DuelCard> cards) {
        CardHandler cardHandler = CardHandler.getHandlerInstance();
        for (DuelCard card : cards) {
            if (!cardHandler.perform_Card_log(card.getCardId(), card.getCardName(), card.getAttackPoints())) {
                return false;
            }
        }
        return cardHandler.compileCardSet(cards);
    }


    /**
     * Löscht registrierte Karten.
     * @param cardList Liste der zu löschenden DuelCards.
     */
    private void revertCardRegistration(List<DuelCard> cardList) {
        CardHandler cardManager = CardHandler.getHandlerInstance();
        for (DuelCard card : cardList) {
            cardManager.perform_Card_CutOut(card.getCardId());
        }
    }

    /**
     * //Verarbeitet den Kartenpaket-Tausch für einen Benutzer.
     * @param user Das Benutzerobjekt.
     * @param request Das Req_Http-Objekt der Anfrage.
     * @return Res_Http Antwortobjekt für den Pakettausch.
     */
    private Res_Http handlePackageExchange(User user, Req_Http request) {
        CardHandler cardManager = CardHandler.getHandlerInstance();
        Res_Http packageResponse = new Res_Http("400 Bad Request");

        if ("POST".equals(request.getHttpRequestMethod())) {
            boolean packageAllocated = cardManager.allocatePackageToClient(user);
            if (packageAllocated) {
                packageResponse.setResponseStatus("200 OK");
                packageResponse.setBodyContent("The package is assigned to the user .");
            } else {
                packageResponse.setResponseStatus("409 Conflict");
                packageResponse.setBodyContent("An Issue by the Package Reference.");
            }
        }

        return packageResponse;
    }

    /**
     * Zeigt Karten für einen Benutzer an.
     * @param user Das Benutzerobjekt.
     * @param request Das Req_Http-Objekt der Anfrage.
     * @return Res_Http Antwortobjekt für die Kartendarstellung.
     */
    private Res_Http displayUserCards(User user, Req_Http request) {
        Res_Http cardResponse = new Res_Http("400 Bad Request");

        if ("GET".equals(request.getHttpRequestMethod())) {
            cardResponse = handleGetUserCards(user, cardResponse);
        }

        return cardResponse;
    }

    /**
     * Verarbeitet GET-Anfragen für Benutzerkarten.
     * @param user Der Benutzer.
     * @param response Das ursprüngliche Res_Http-Objekt.
     * @return Res_Http Das aktualisierte Antwortobjekt.
     */
    private Res_Http handleGetUserCards(User user, Res_Http response) {
        // JSON-Antwortstring für die Karten des Benutzers abrufen
        String jsonResponse = fetchUserCardsAsJson(user);

        // Antwortstatus und -körper basierend auf der JSON-Antwort aktualisieren
        response = modifyResponseBasedOnJson(response, jsonResponse);

        return response;
    }

    //Holt die Benutzerkarten als JSON-String.
    private String fetchUserCardsAsJson(User user) {
        return CardHandler.getHandlerInstance().showUserCards(user);
    }

    //Aktualisiert die Res_Http-Antwort basierend auf einem JSON-String.
    private Res_Http modifyResponseBasedOnJson(Res_Http response, String jsonResponse) {
        // Status auf "200 OK" und Körper auf den JSON-Antwortstring setzen, wenn dieser nicht null ist
        if (jsonResponse != null) {
            response.setResponseStatus("200 OK");
            response.setBodyContent(jsonResponse);
        } else {
            // Status auf "404 Error" und Körper auf "Keine Karten verfügbar." setzen, wenn der JSON-Antwortstring null ist
            response.setResponseStatus("404 Error");
            response.setBodyContent("There are no cards available.");
        }

        return response;
    }


    // Verarbeitung von Anfragen bezüglich Benutzerdeck
    private Res_Http handleDeckRequest(User user, Req_Http request) {
        Res_Http deckResponse = new Res_Http("400 Bad Request");
        CardHandler cardManager = CardHandler.getHandlerInstance();

        String methodType = request.getHttpRequestMethod();

        // GET-Anfragen für das Deck des Benutzers
        if ("GET".equals(methodType)) {
            deckResponse = processDeckGetRequest(user, deckResponse, cardManager);
        }

        // PUT-Anfragen zum Erstellen oder Aktualisieren des Decks
        else if ("PUT".equals(methodType)) {
            deckResponse = handleDeckPutRequest(user, request, deckResponse, cardManager);
        }

        return deckResponse;
    }

    /**
     * Verarbeitet GET-Anfragen für das Benutzerdeck.
     * @param user Der Benutzer.
     * @param response Das vorbereitete Res_Http-Objekt.
     * @param cardManager Der CardHandler.
     * @return Res_Http Das aktualisierte Antwortobjekt.
     */
    private Res_Http processDeckGetRequest(User user, Res_Http response, CardHandler cardManager) {
        String jsonDeck = cardManager.showDeck(user);

        // Antwort basierend auf der Verfügbarkeit des Decks
        if (jsonDeck != null) {
            response.setResponseStatus("200 OK");
            response.setBodyContent(jsonDeck);
        } else {
            response.setResponseStatus("404 Not Found");
            response.setBodyContent("Deck has not been found.");
        }

        return response;
    }

    /**
     * Verarbeitet PUT-Anfragen für Benutzerdecks.
     * @param user Der Benutzer.
     * @param request Das Req_Http-Objekt der Anfrage.
     * @param response Das vorbereitete Res_Http-Objekt.
     * @param cardManager Der CardHandler.
     * @return Res_Http Das aktualisierte Antwortobjekt.
     */
    private Res_Http handleDeckPutRequest(User user, Req_Http request, Res_Http response, CardHandler cardManager) {
        ObjectMapper jsonParser = new ObjectMapper();

        try {
            List<String> cardIDs = jsonParser.readValue(request.getHttpRequestBody(), new TypeReference<List<String>>() {});

            // Überprüfen, ob die Anzahl der Karten korrekt ist
            if (cardIDs.size() == 4) {

                // Versuch, ein Deck zu erstellen
                if (cardManager.perform_Deck_Creation(
                        user, cardIDs)) {
                    response.setResponseStatus("201 Created");
                    response.setBodyContent("Deck is Created.");
                } else {
                    response.setResponseStatus("409 Conflict");
                    response.setBodyContent("There is an Issue during the creation of the deck.");
                }
            } else {
                response.setResponseStatus("400 Bad Request");
                response.setBodyContent("Invalid number of cards. Deck must have only 4 Cards.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setResponseStatus("400 Bad Request");
            response.setBodyContent("Invalid Body Request.");
        }

        return response;
    }

    /**
     * Verarbeitet Anfragen zur Benutzerinformation.
     * @param user Der Benutzer.
     * @param request Das Req_Http-Objekt der Anfrage.
     * @return Res_Http Das Antwortobjekt mit Benutzerinformationen.
     */
    private Res_Http handleUserInfoRequest(User user, Req_Http request) {
        ObjectMapper jsonMapper = new ObjectMapper();
        Res_Http userInfoResponse = new Res_Http("400 Bad Request");

        // Verarbeitung von GET-Anfragen zur Benutzerstatistik
        if ("GET".equalsIgnoreCase(request.getHttpRequestMethod())) {
            try {

                // Konvertierung der Benutzerstatistiken in einen JSON-String
                String userStatsJson = jsonMapper.writeValueAsString(user.retrieveStatistics());
                userInfoResponse.setResponseStatus("200 OK");
                userInfoResponse.setBodyContent(userStatsJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                userInfoResponse.setResponseStatus("500 Internal Server Error");
                userInfoResponse.setBodyContent("There is an Issue during the call of Statistics of the user.");
            }
        }

        return userInfoResponse;
    }

    // Verarbeitet Anfragen zur Anzeige von Duellergebnissen.
    private Res_Http displayDuelResults(Req_Http request) {
        DuelManager duelManager = DuelManager.getManager();
        Res_Http response = new Res_Http("400 Bad Request");

        // Überprüfen, ob die Anfragemethode GET ist
        if ("GET".equalsIgnoreCase(request.getHttpRequestMethod())) {
            try {
                ObjectMapper jsonMapper = new ObjectMapper();
                String scoreboardJson = jsonMapper.writeValueAsString(duelManager.retrieveScoreboardData());

                // Erfolgreiche Antwort setzen
                response.setResponseStatus("200 OK");
                response.setBodyContent(scoreboardJson);
            } catch (JsonProcessingException e) {
                // Fehlerbehandlung und Rückgabe einer Serverfehler-Antwort
                e.printStackTrace();
                response.setResponseStatus("500 Internal Server Error");
                response.setBodyContent("There is an issue during the retrieving of the leaderboard.");
            }
        }

        return response;
    }

    // Verarbeitet Kampfanfragen.
    private Res_Http executeDuel(Req_Http request, User user) {
        Res_Http response = new Res_Http("400 Bad Request");

        // Überprüfung, ob die Methode POST ist
        if ("POST".equals(request.getHttpRequestMethod())) {
            DuelManager duelManager = DuelManager.getManager();
            String duelOutcome = duelManager.engageAndResolveDuel(user);

            // Entscheidung basierend auf dem Ergebnis des Duells
            if (duelOutcome != null) {
                response.setResponseStatus("200 OK");
                response.setBodyContent(duelOutcome);
            } else {
                response.setResponseStatus("404 Not Found");
                response.setBodyContent("No opponent found for the battle.");
            }
        }

        return response;
    }


    private User authenticateUser(Req_Http request) {
        // Überprüfen, ob der "authorization"-Schlüssel in den Headern vorhanden ist
        if (!request.getHttpRequestHeaders().containsKey("authorization:")) {
            // Bei Abwesenheit des Schlüssels wird null zurückgegeben, was auf eine fehlgeschlagene Autorisierung hinweist
            return null;
        }

        // "authorization"-Wert aus den Headern extrahieren
        String authValue = request.getHttpRequestHeaders().get("authorization:");

        // UserHandler-Instanz erzeugen
        UserHandler userHandler = UserHandler.getInstance();

        // Autorisierungsmethode des UserHandlers aufrufen und das resultierende User-Objekt zurückgeben
        // Kann null sein, wenn die Autorisierung fehlschlägt
        return userHandler.givepermissions(authValue);
    }


    private Res_Http manageCardTrading(Req_Http request, User user) {
        Trading tradeManager = Trading.getInstance();
        Res_Http tradeResponse = new Res_Http("400 Bad Request");
        String[] urlParts;

        switch (request.getHttpRequestMethod()) {
            case "GET":
                tradeResponse.setBodyContent(tradeManager.showing_Marketfortrade());
                tradeResponse.setResponseStatus("200 OK");
                break;

            case "POST":
                urlParts = request.getHttpRequestResource().split("/");
                if (urlParts.length == 3) {
                    tradeResponse = handlePostTradeRequest(user, request, tradeManager, urlParts);
                } else {
                    tradeResponse = handleCreateTradeRequest(user, request, tradeManager);
                }
                break;

            case "DELETE":
                urlParts = request.getHttpRequestResource().split("/");
                if (urlParts.length == 3 && tradeManager.perform_Trade_cutout(user, urlParts[2])) {
                    tradeResponse.setResponseStatus("200 OK");
                    tradeResponse.setBodyContent("The Trade offer is withdrawn.");
                } else {
                    tradeResponse.setResponseStatus("400 Bad Request");
                    tradeResponse.setBodyContent("There is an Issue during the withdrawing of the trade offer.");
                }
                break;

            default:
                tradeResponse.setResponseStatus("400 Bad Request");
                tradeResponse.setBodyContent("Invalid request method.");
                break;
        }

        return tradeResponse;
    }


    //Verarbeitet die POST-Anfrage für den Handel mit Karten.

    private Res_Http handlePostTradeRequest(User user, Req_Http request, Trading tradeManager, String[] urlParts) {
        Res_Http response = new Res_Http("400 Bad Request");
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(request.getHttpRequestBody());
            if (jsonNode.has("Card2Trade")) {
                boolean tradeSuccess = tradeManager.perform_TradeofCards(user, urlParts[2], jsonNode.get("Card2Trade").asText());
                response.setResponseStatus(tradeSuccess ? "200 OK" : "400 Bad Request");
                response.setBodyContent(tradeSuccess ? "Cards are exchanged successfully" : "There is an Issue during the exchanging of the cards");
            } else {
                response.setResponseStatus("400 Bad Request");
                response.setBodyContent("Parameter 'Card2Trade' is missing.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setResponseStatus("400 Bad Request");
            response.setBodyContent("Error processing the request.");
        }

        return response;
    }

    // Verarbeitet die Erstellung eines Handelsangebots.

    private Res_Http handleCreateTradeRequest(User user, Req_Http request, Trading tradeManager) {
        Res_Http response = new Res_Http("400 Bad Request");
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(request.getHttpRequestBody());
            if (jsonNode.has("Id") && jsonNode.has("CardToTrade") && jsonNode.has("Type") && jsonNode.has("MinimumDamage")) {
                boolean tradeCreationSuccess = tradeManager.placeforCardstrading(user, jsonNode.get("Id").asText(), jsonNode.get("CardToTrade").asText(), (float) jsonNode.get("MinimumDamage").asDouble(), jsonNode.get("Type").asText());
                response.setResponseStatus(tradeCreationSuccess ? "201 Created" : "400 Bad Request");
                response.setBodyContent(tradeCreationSuccess ? "Trade offer successfully created.." : "There is an Issue during the creation of the trade offer.");
            } else {
                response.setResponseStatus("400 Bad Request");
                response.setBodyContent("One or more required parameters are missing.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setResponseStatus("400 Bad Request");
            response.setBodyContent("There is an Issue during the processing of the request.");
        }

        return response;
    }

}

