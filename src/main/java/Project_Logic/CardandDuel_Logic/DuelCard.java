package Project_Logic.CardandDuel_Logic;

import Project_Logic.Monster_Logic.Elements;
import Project_Logic.Monster_Logic.TypeofMonsters;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Repräsentiert eine Spielkarte mit Eigenschaften wie Kennung, Bezeichnung,
 * Angriffspunkten, Monsterart und Elementart.
 */
public class DuelCard {

    // Einzigartige Kennung der Karte
    @JsonProperty("Id")

    private String cardId;

    // Bezeichnung der Karte
    @JsonProperty("Name")

    private String cardName;

    // Angriffspunkte der Karte
    @JsonProperty("Damage")
    private float attackPoints;

    // Monsterart der Karte
    @JsonProperty("Category")
    private TypeofMonsters category;

    // Elementart der Karte
    @JsonProperty("Elements")
    private Elements elementType;

    // Standardkonstruktor
    public DuelCard() {}

    /**
     * Konstruktor zur Initialisierung einer Karte mit Kennung, Bezeichnung und Angriffspunkten.
     */
    public DuelCard(String cardId, String cardName, float attackPoints) {
        this.cardId = cardId;
        this.cardName = cardName;
        this.attackPoints = attackPoints;
    }

    /**
     * Konstruktor zur Initialisierung einer Karte mit allen Eigenschaften.
     */
    public DuelCard(String id, String name, float damage, TypeofMonsters monsterType, Elements elementType) {
        this.cardId = id;
        this.cardName = name;
        this.attackPoints = damage;
        this.category = monsterType;
        this.elementType = elementType;
    }

    // Setter-Methoden
    public void setCardId(String id) {
        this.cardId = id;
    }

    public void setCardName(String name) {
        this.cardName = name;
    }

    public void setAttackPoints(float damage) {
        this.attackPoints = damage;
    }

    public void setCategory(TypeofMonsters monsterType) {
        this.category = monsterType;
    }

    public void setElementType(Elements type) {
        this.elementType = type;
    }

    // Getter-Methoden
    public String getCardId() {
        return cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public float getAttackPoints() {
        return attackPoints;
    }

    public TypeofMonsters getCategory() {
        return category;
    }

    public Elements getElementType() {
        return elementType;
    }
}
