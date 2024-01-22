import Project_Logic.CardandDuel_Logic.CardHandler;
import Project_Logic.User_Logic.UserHandler;
import Server_Connection.Request_Logic.Req_Http;
import Server_Connection.Response_Logic.Res_Composer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import Project_Logic.User_Logic.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Test_Server_Res_Composer {

    @Mock
    UserHandler handlerUser;
    @Mock
    CardHandler handlerCard;
    @Mock
    User user;
    @Mock
    BufferedWriter writer;

    Req_Http request;


     //Bereiten Sie die Testumgebung vor jedem Test vor.
     //Diese Methode initialisiert das Req_Http-Objekt
     //und setzt die Header und den Body für die Anfrage.

    @BeforeEach
    void prepare() {
        // Initialisieren Sie das Req_Http-Objekt
        initializeHttpRequest();

        // Setzen Sie die Header für die HTTP-Anfrage
        setRequestHeaders();

        // Setzen Sie den Body für die HTTP-Anfrage
        setRequestBody();
    }


     //Initialisieren Sie das Req_Http-Objekt.

    private void initializeHttpRequest() {
        request = new Req_Http();
        request.setHttpProtocolVersion("HTTP/1.1");
    }


     //Setzen Sie die Header für die HTTP-Anfrage.

    private void setRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("user-agent:", "Mozilla/5.0");
        headers.put("content-length:", "10");
        headers.put("content-type:", "application/json");
        headers.put("accept:", "application/json");
        headers.put("host:", "localhost:8080");
        headers.put("authorization:", "test");

        request.setHttpRequestHeaders(headers);
    }


    //  Setzen Sie den Body für die HTTP-Anfrage.

    private void setRequestBody() {
        request.setHttpRequestBody("{\"Username\":\"Test\", \"Password\":\"test\"}");
    }



     // Testen Sie den Benutzerregistrierungsprozess.
     // Dieser Test überprüft, ob die Methode registerUser aufgerufen wird und die Antwort korrekt generiert wird.

    @Test
    public void testRegister() throws IOException {
        // Richten Sie die Testumgebung für die Benutzerregistrierung ein
        try (MockedStatic<UserHandler> mockedHandlerUser = Mockito.mockStatic(UserHandler.class)) {
            prepareTestRegister(mockedHandlerUser);

            // Führen Sie den eigentlichen Test durch
            executeTestRegister();
        }
    }

    //Richten Sie die Testumgebung für die Benutzerregistrierung ein.
    private void prepareTestRegister(MockedStatic<UserHandler> mockedHandlerUser) {
        // Konfigurieren Sie die gemockte Handler_User-Instanz
        mockedHandlerUser.when(UserHandler::getInstance)
                .thenReturn(handlerUser);

        // Setzen Sie die Anfragemethode und die Ressource
        request.setHttpRequestMethod("POST");
        request.setHttpRequestResource("/users");
    }

    //Führen Sie den Test für die Benutzerregistrierung aus.
    private void executeTestRegister() throws IOException {
        // Erstellen Sie ein Res_Composer-Objekt
        Res_Composer responseGenerator = new Res_Composer(writer);

        // Generieren Sie die Antwort für die gegebene Anfrage
        responseGenerator.generateResponse(request);

        // Überprüfen Sie, ob die Methode registerUser mit den erwarteten Parametern aufgerufen wird
        verify(handlerUser).perform_User_Registartion(anyString(), anyString());

        // Überprüfen Sie, ob die flush-Methode des Writers aufgerufen wird
        verify(writer).flush();
    }



     //Testen Sie den Benutzeranmeldeprozess.
     //Dieser Test überprüft, ob die Methode loginUser aufgerufen wird und die Antwort korrekt generiert wird.

    @Test
    public void testAnmelden() throws IOException {
        // Richten Sie die Testumgebung für die Benutzeranmeldung ein
        try (MockedStatic<UserHandler> mockedHandlerUser = Mockito.mockStatic(UserHandler.class)) {
            prepareTestAnmelden(mockedHandlerUser);

            // Führen Sie den eigentlichen Test durch
            executeTestAnmelden();
        }
    }

    //Richten Sie die Testumgebung für die Benutzeranmeldung ein.
    private void prepareTestAnmelden(MockedStatic<UserHandler> mockedHandlerUser) {
        // Konfigurieren Sie die gemockte Handler_User-Instanz
        mockedHandlerUser.when(UserHandler::getInstance)
                .thenReturn(handlerUser);

        // Setzen Sie die Anfragemethode und die Ressource
        request.setHttpRequestMethod("POST");
        request.setHttpRequestResource("/sessions");
    }

    // Führung der Test für die Benutzeranmeldung aus.

    private void executeTestAnmelden() throws IOException {
        // Erstellen Sie ein Res_Composer-Objekt
        Res_Composer handler = new Res_Composer(writer);

        // Generieren Sie die Antwort für die gegebene Anfrage
        handler.generateResponse(request);

        // Überprüfen Sie, ob die loginUser-Methode mit den erwarteten Parametern aufgerufen wird
        verify(handlerUser).perform_User_SigningIn(anyString(), anyString());

        // Überprüfen Sie, ob die flush-Methode des Writers aufgerufen wird
        verify(writer).flush();
    }


    //Testen Sie den Benutzerabmeldeprozess.
    //Dieser Test überprüft, ob die Methode logoutUser aufgerufen wird und die Antwort korrekt generiert wird.

    @Test
    public void testAbmelden() throws IOException {
        // Richten Sie die Testumgebung für die Benutzerabmeldung ein
        try (MockedStatic<UserHandler> mockedHandlerUser = Mockito.mockStatic(UserHandler.class)) {
            prepareTestAbmelden(mockedHandlerUser);

            // Führen Sie den eigentlichen Test durch
            executeTestAbmelden();
        }
    }

    //Richten Sie die Testumgebung für die Benutzerabmeldung ein.

    private void prepareTestAbmelden(MockedStatic<UserHandler> mockedHandlerUser) {
        // Konfigurieren Sie die gemockte Handler_User-Instanz
        mockedHandlerUser.when(UserHandler::getInstance)
                .thenReturn(handlerUser);

        // Setzen Sie die Anfragemethode und die Ressource
        request.setHttpRequestMethod("DELETE");
        request.setHttpRequestResource("/sessions");
    }

    //Führen Sie den Test für die Benutzerabmeldung aus.
    private void executeTestAbmelden() throws IOException {
        // Erstellen Sie ein Res_Composer-Objekt
        Res_Composer handler = new Res_Composer(writer);

        // Generieren Sie die Antwort für die gegebene Anfrage
        handler.generateResponse(request);

        // Überprüfen Sie, ob die logoutUser-Methode mit den erwarteten Parametern aufgerufen wird
        verify(handlerUser).perform_User_SigningOut(anyString(), anyString());

        // Überprüfen Sie, ob die flush-Methode des Writers aufgerufen wird
        verify(writer).flush();
    }

    // Test für den Benutzerbearbeitungsprozess
    @Test
    public void testChangeUser() throws IOException {
        // Testumgebung für Benutzerbearbeitung einrichten
        try (MockedStatic<UserHandler> mockedHandlerUser = Mockito.mockStatic(UserHandler.class)) {
            prepareTestChangeUser(mockedHandlerUser);

            // Eigentlichen Test durchführen
            executeTestChangeUser();
        }
    }

    // Testumgebung für Benutzerbearbeitung vorbereiten
    private void prepareTestChangeUser(MockedStatic<UserHandler> mockedHandlerUser) {
        mockedHandlerUser.when(UserHandler::getInstance).thenReturn(handlerUser);

        // Benutzerberechtigungen einrichten
        when(handlerUser.givepermissions(anyString())).thenReturn(user);
        when(user.getUsername()).thenReturn("kienboec");

        // Anfrage Methode, Ressource und Körper einstellen
        request.setHttpRequestMethod("PUT");
        request.setHttpRequestResource("/users/kienboec");
        request.setHttpRequestBody("{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}");
    }

    // Benutzerbearbeitungstest durchführen
    private void executeTestChangeUser() throws IOException {
        Res_Composer handler = new Res_Composer(writer);

        // Antwort für die gegebene Anfrage generieren
        handler.generateResponse(request);

        // Überprüfen, ob die Methode setUserInfo mit den erwarteten Parametern aufgerufen wird
        verify(user).performupdateOnInfos(anyString(), anyString(), anyString());

        // Überprüfen, ob die Flush-Methode des Writers aufgerufen wird
        verify(writer).flush();
    }


    // Test für den Bearbeitungsprozess bei nicht autorisiertem Benutzer
    @Test
    public void testWrongUser() throws IOException {
        // Testumgebung für unberechtigte Benutzerbearbeitung einrichten
        try (MockedStatic<UserHandler> mockedHandlerUser = Mockito.mockStatic(UserHandler.class)) {
            prepareTestWrongUser(mockedHandlerUser);

            // Eigentlichen Test durchführen
            executeTestWrongUser();
        }
    }

    // Testumgebung für unberechtigte Benutzerbearbeitung einrichten
    private void prepareTestWrongUser(MockedStatic<UserHandler> mockedHandlerUser) {
        // Konfiguration der gemockten Handler_User Instanz
        mockedHandlerUser.when(UserHandler::getInstance).thenReturn(handlerUser);

        // Benutzerberechtigung einrichten
        when(handlerUser.givepermissions(anyString())).thenReturn(user);
        when(user.getUsername()).thenReturn("altenhof");

        // Anfrage Methode, Ressource und Körper einstellen
        request.setHttpRequestMethod("PUT");
        request.setHttpRequestResource("/users/kienboec");
        request.setHttpRequestBody("{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}");
    }

    // Test für unberechtigte Benutzerbearbeitung durchführen
    private void executeTestWrongUser() throws IOException {
        // Res_Composer Objekt erstellen
        Res_Composer handler = new Res_Composer(writer);

        // Antwort für die gegebene Anfrage generieren
        handler.generateResponse(request);

        // Überprüfen, ob die Methode setUserInfo nicht aufgerufen wird
        verify(user, times(0)).performupdateOnInfos(anyString(), anyString(), anyString());

        // Überprüfen, ob die Flush-Methode des Writers aufgerufen wird
        verify(writer).flush();
    }



    // Test für die Erstellung von Kartenpaketen für Admin-Benutzer
    @Test
    public void testPackage() throws IOException {
        // Testumgebung für die Erstellung von Kartenpaketen einrichten
        try (MockedStatic<UserHandler> mockedHandlerUser = Mockito.mockStatic(UserHandler.class);
             MockedStatic<CardHandler> mockedHandlerCard = Mockito.mockStatic(CardHandler.class)) {

            prepareTestPackage(mockedHandlerUser, mockedHandlerCard);

            // Eigentlichen Test durchführen
            executeTestPackage();
        }
    }

    // Testumgebung für die Erstellung von Kartenpaketen vorbereiten
    private void prepareTestPackage(MockedStatic<UserHandler> mockedHandlerUser, MockedStatic<CardHandler> mockedHandlerCard) {
        // Konfiguration der gemockten Handler_User und Handler_Card Instanzen
        mockedHandlerUser.when(UserHandler::getInstance).thenReturn(handlerUser);
        mockedHandlerCard.when(CardHandler::getHandlerInstance).thenReturn(handlerCard);

        // Admin-Berechtigung und Kartenregistrierung einrichten
        when(handlerUser.perform_Verificationofrole(anyString())).thenReturn(true);
        when(handlerCard.perform_Card_log(anyString(), anyString(), anyFloat())).thenReturn(true);
        when(handlerCard.compileCardSet(anyList())).thenReturn(true);

        // Anfrage Methode, Ressource und Körper einstellen
        request.setHttpRequestMethod("POST");
        request.setHttpRequestResource("/packages");
        request.setHttpRequestBody("[{\"Id\":\"b017ee50-1c14-44e2-bfd6-2c0c5653a37c\", \"Name\":\"WaterGoblin\", \"Damage\": 11.0}, {\"Id\":\"d04b736a-e874-4137-b191-638e0ff3b4e7\", \"Name\":\"Dragon\", \"Damage\": 70.0}, {\"Id\":\"88221cfe-1f84-41b9-8152-8e36c6a354de\", \"Name\":\"WaterSpell\", \"Damage\": 22.0}, {\"Id\":\"1d3f175b-c067-4359-989d-96562bfa382c\", \"Name\":\"Ork\", \"Damage\": 40.0}, {\"Id\":\"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\", \"Name\":\"RegularSpell\", \"Damage\": 28.0}]");
    }


    /// Test für die Erstellung von Kartenpaketen durchführen
    private void executeTestPackage() throws IOException {
        // Res_Composer Objekt erstellen
        Res_Composer handler = new Res_Composer(writer);

        // Antwort für die gegebene Anfrage generieren
        handler.generateResponse(request);

        // Überprüfen, ob die Methode registerCard 5 Mal aufgerufen wird
        verify(handlerCard, times(5)).perform_Card_log(anyString(), anyString(), anyFloat());

        // Überprüfen, ob die Methode createCardPackage aufgerufen wird
        verify(handlerCard).compileCardSet(anyList());

        // Überprüfen, ob die Flush-Methode des Writers aufgerufen wird
        verify(writer).flush();
    }
}