package Server_Connection;


import Server_Connection.Request_Logic.Req_Http;
import Server_Connection.Request_Logic.Req_Processor;
import Server_Connection.Response_Logic.Res_Composer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Main {
    // Definiert den Port, auf dem der Server lauscht.
    private static final int SERVER_PORT = 10001;

    public static void main(String[] args) {
        // Startet die Initialisierung des Servers.
        initialize_Server();
    }

    // Diese Methode initialisiert den Server.
    private static void initialize_Server() {
        System.out.println("Serverinitialisierung...");

        // Versucht, einen ServerSocket auf dem festgelegten Port zu erstellen.
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server bereit für Verbindungen...");

            // Wartet auf Client-Anfragen und bearbeitet sie.
            AcceptClientRequests(serverSocket);
        } catch (IOException exception) {
            // Protokolliert Ausnahmen, die während der Serverinitialisierung auftreten.
            exception.printStackTrace();
        }
    }

    // Diese Methode akzeptiert kontinuierlich Client-Anfragen.
    private static void AcceptClientRequests(ServerSocket serverSocket) {
        while (true) {
            try {
                // Akzeptiert eine Verbindung von einem Client.
                Socket client = serverSocket.accept();
                // Verarbeitet die Anfrage des Clients in einem separaten Thread.
                parse_Req(client);
            } catch (IOException e) {
                // Protokolliert Ausnahmen, die beim Akzeptieren von Client-Verbindungen auftreten.
                e.printStackTrace();
            }
        }
    }

    // Diese Methode verarbeitet die Anfrage eines Clients.
    private static void parse_Req(Socket client) {
        new Thread(() -> {
            // Erstellt Eingabe- und Ausgabeströme für die Kommunikation mit dem Client.
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
                // Verarbeitet die Anfrage und generiert eine Antwort.
                Process_Req(reader, writer);
            } catch (IOException e) {
                // Protokolliert Ausnahmen, die bei der Client-Verarbeitung auftreten.
                e.printStackTrace();
            }
        }).start();
    }

    // Diese Methode bearbeitet die Anfrage eines Clients.
    private static void Process_Req(BufferedReader reader, BufferedWriter writer) throws IOException {
        // Verarbeitet die HTTP-Anfrage des Clients.
        Req_Http httpRequest = new Req_Processor(reader).processHttpRequest();

        if (httpRequest != null) {
            // Druckt die Details der Client-Anfrage.
            ClientAnfrageDrucken(httpRequest);

            // Komponiert und sendet die Antwort an den Client.
            new Res_Composer(writer).generateResponse(httpRequest);
        }
    }

    // Diese Methode druckt die Details der Client-Anfrage.
    private static void ClientAnfrageDrucken(Req_Http httpRequest) {
        System.out.println("** Client-Anfrage erhalten **");
        AnfrageDetailsDrucken(httpRequest);
        System.out.println("--------------------------------------------------------");
    }

    // Diese Methode druckt die Details der HTTP-Anfrage.
    private static void AnfrageDetailsDrucken(Req_Http httpRequest) {
        System.out.println("** Header: **");
        System.out.println("    " + httpRequest.getHttpRequestMethod() + " " + httpRequest.getHttpRequestResource() + " " + httpRequest.getHttpProtocolVersion());

        // Iteriert über die HTTP-Header und druckt sie.
        httpRequest.getHttpRequestHeaders().forEach((key, value) ->
                System.out.println("    " + key + " " + value));

        System.out.println("** Body: **");
        System.out.println(httpRequest.getHttpRequestBody());
    }
}

