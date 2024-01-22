package Project_Logic.Monster_Logic;


// Enum für Elementtypen von Monstern.
public enum Elements {

    Water("Water"),
    Fire("Fire"),
    Normal("Normal");

    // Name des Elements.
    private final String nameDesElements;

    // Konstruktor für Elements-Enum.
    Elements(String nameDesElements) {
        this.nameDesElements = nameDesElements;
    }

    // Gibt den Namen des Elements zurück.
    public String getNameDesElements() {
        return this.nameDesElements;
    }
}

