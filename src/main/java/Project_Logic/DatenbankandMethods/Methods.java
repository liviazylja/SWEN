package Project_Logic.DatenbankandMethods;

// Enum für HTTP-Methoden.
public enum Methods {
    GET_REQUEST("GET"),
    POST_REQUEST("POST"),
    PUT_REQUEST("PUT"),
    DELETE_REQUEST("DELETE"),
    OTHER_REQUEST("OTHER");

    // Name der HTTP-Methode.
    private final String httpMethodName;

    // Konstruktor für die Enum-Werte.
    Methods(String httpMethodName) {
        this.httpMethodName = httpMethodName;
    }

    // Gibt den Namen der HTTP-Methode zurück.
    public String getHttpMethodName() {
        return this.httpMethodName;
    }
}
