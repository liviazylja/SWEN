import Project_Logic.CardandDuel_Logic.CardHandler;
import Project_Logic.CardandDuel_Logic.DuelCard;
import Project_Logic.CardandDuel_Logic.DuelManager;
import Project_Logic.Monster_Logic.Elements;
import Project_Logic.Monster_Logic.TypeofMonsters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_Duel_Damage_Performance {
    @Test
    /**
     * Testet die Fähigkeit der Ritter-Karte gegen andere Karten.
     * Dieser Testfall konzentriert sich darauf, sicherzustellen, dass die
     * Berechnung des Schadens korrekt ist, wenn eine Ritter-Karte gegen eine Wasserzauber-Karte kämpft.
     */
    // Methoden, um eine Karte mit dem Handler_Card zu erstellen
    private DuelCard createCard(String id, String name, float damage) {
        CardHandler handlerCard = CardHandler.getHandlerInstance();
        TypeofMonsters category = handlerCard.decideCategoryOfMonster(name);
        Elements element = handlerCard.decide_TypeOfElement(name);

        return new DuelCard(id, name, damage, category, element);
    }

    // Methoden zum Berechnen und Testen des Schadens zwischen zwei Karten
    private void testDamage(DuelCard card1, DuelCard card2, float expectedDamage1, float expectedDamage2) {
        // Instanz des Duellmanagers holen
        DuelManager manager = DuelManager.getManager();

        // Berechnen des Schadens jeder Karte gegen die andere
        float result1 = manager.computeInflictedDamage(card1, card2);
        float result2 = manager.computeInflictedDamage(card2, card1);

        // Überprüfen, ob der erwartete Schaden für jede Karte korrekt ist
        assertEquals(expectedDamage1, result1);
        assertEquals(expectedDamage2, result2);
    }

    @Test
    public void testKnight() {
        // Anordnen: Zwei Karten erstellen, um die Fähigkeit des Ritters zu testen
        DuelCard knightCard = createCard("1", "Knight", 50);
        DuelCard waterSpellCard = createCard("2", "WaterSpell", 100);

        // Handeln & Überprüfen: Testen Sie den Schaden zwischen den beiden Karten
        testDamage(knightCard, waterSpellCard, -1, 50);
    }


    @Test
    /**
     * Testet die Fähigkeit der Feuerelfen-Karte gegen andere Karten.
     * Speziell überprüft dieser Test die Berechnung des Schadens,
     * wenn eine Feuerelfen-Karte gegen eine Wasserdrachen-Karte kämpft.
     */
    public void testFireElf() {
        // Anordnen: Zwei Karten erstellen, um die Fähigkeit des Feuerelfen zu testen
        DuelCard fireElfCard = createCard("1", "FireElf", 15);
        DuelCard waterDragonCard = createCard("2", "WaterDragon", 15);

        // Handeln & Überprüfen: Testen Sie den Schaden zwischen den beiden Karten
        testDamage(fireElfCard, waterDragonCard, 7.5f, 0);
    }


    @Test
    /**
     * Testet die Fähigkeit der Kraken-Karte gegen andere Karten.
     * Dieser Test überprüft die Richtigkeit der Schadensberechnung,
     * wenn eine Kraken-Karte gegen eine Zauber-Karte kämpft.
     */
    public void testKraken() {
        // Anordnen: Zwei Karten erstellen, um die Fähigkeit des Kraken zu testen
        DuelCard krakenCard = createCard("1", "Kraken", 50);
        DuelCard spellCard = createCard("2", "Spell", 100);

        // Handeln & Überprüfen: Testen Sie den Schaden zwischen den beiden Karten
        testDamage(krakenCard, spellCard, 50f, 0);
    }

    @Test
    /**
     * Dieser Test überprüft die Fähigkeit der Wasserzauber-Karte gegen andere Karten.
     * Es wird sichergestellt, dass die Schadensberechnung genau ist, wenn eine Wasserzauber-Karte
     * gegen Feuerzauber- und Normalzauber-Karten kämpft.
     */
    public void testWaterSpell() {
        CardHandler handlerCard = CardHandler.getHandlerInstance();

        // Drei Karten erstellen, um die Fähigkeit des Wasserzaubers zu testen
        String cardName1 = "WaterSpell";
        String cardName2 = "FireSpell";
        String cardName3 = "NormalSpell";

        float damage = 50;

        // Karten mit dem Handler_Card erstellen
        DuelCard card1 = new DuelCard("1",cardName1,damage, handlerCard.decideCategoryOfMonster(cardName1), handlerCard.decide_TypeOfElement(cardName1));
        DuelCard card2 = new DuelCard("2",cardName2,damage, handlerCard.decideCategoryOfMonster(cardName2), handlerCard.decide_TypeOfElement(cardName2));
        DuelCard card3 = new DuelCard("2",cardName3,damage, handlerCard.decideCategoryOfMonster(cardName3), handlerCard.decide_TypeOfElement(cardName3));

        // Instanz des Duellmanagers holen
        DuelManager manager = DuelManager.getManager();

        // Berechnen des Schadens der Wasserzauber-Karte gegen andere Karten
        float result1 = manager.computeInflictedDamage(card1,card2);
        float result2 = manager.computeInflictedDamage(card1,card3);

        // Überprüfen, ob der erwartete Schaden für Wasserzauber korrekt ist
        assertEquals(100,result1);
        assertEquals(25,result2);
    }

    @Test
    /**
     * Dieser Test überprüft die Fähigkeit der Feuerzauber-Karte gegen andere Karten.
     * Es wird die Richtigkeit der Schadensberechnung getestet, wenn eine Feuerzauber-Karte
     * gegen Wasserzauber- und Normalzauber-Karten kämpft.
     */
    public void testFireSpell() {
        CardHandler handlerCard = CardHandler.getHandlerInstance();

        // Drei Karten erstellen, um die Fähigkeit des Feuerzaubers zu testen
        String cardName1 = "WaterSpell";
        String cardName2 = "FireSpell";
        String cardName3 = "NormalSpell";

        float damage = 50;

        // Karten mit dem Handler_Card erstellen
        DuelCard card1 = new DuelCard("1",cardName1,damage, handlerCard.decideCategoryOfMonster(cardName1), handlerCard.decide_TypeOfElement(cardName1));
        DuelCard card2 = new DuelCard("2",cardName2,damage, handlerCard.decideCategoryOfMonster(cardName2), handlerCard.decide_TypeOfElement(cardName2));
        DuelCard card3 = new DuelCard("2",cardName3,damage, handlerCard.decideCategoryOfMonster(cardName3), handlerCard.decide_TypeOfElement(cardName3));

        // Instanz des Duellmanagers holen
        DuelManager manager = DuelManager.getManager();

        // Berechnen des Schadens der Feuerzauber-Karte gegen andere Karten
        float result1 = manager.computeInflictedDamage(card2,card1);
        float result2 = manager.computeInflictedDamage(card2,card3);

        // Überprüfen, ob der erwartete Schaden für Feuerzauber korrekt ist
        assertEquals(25,result1);
        assertEquals(100,result2);
    }

    /**
     * Dieser Testfall validiert die Angriffsfähigkeit der Normalzauber-Karte gegen andere Karten.
     * Es wird überprüft, ob die Schadensberechnung genau ist, wenn eine Normalzauber-Karte
     * gegen Wasserzauber- und Feuerzauber-Karten kämpft.
     */
    @Test
    public void TestNormalSpell() {
        // Anordnen: Karten für den Test erstellen
        DuelCard waterSpellCard = createCard("1", "WaterSpell", 50);
        DuelCard fireSpellCard = createCard("2", "FireSpell", 50);
        DuelCard normalSpellCard = createCard("3", "NormalSpell", 50);

        // Handeln & Überprüfen: Testen Sie den Schaden der Normalzauber-Karte gegen andere Karten
        testDamage(normalSpellCard, waterSpellCard, 100, 25);
        testDamage(normalSpellCard, fireSpellCard, 25, 100);
    }


    /**
     * Dieser Test überprüft die Angriffsfähigkeit einer NormalMonster-Karte gegen andere Karten.
     * Es wird sichergestellt, dass die Schadensberechnung korrekt ist, wenn eine NormalMonster-Karte (repräsentiert
     * durch einen Drachen, Ritter oder Zauberer) gegen andere Monsterkarten kämpft.
     */
    @Test
    public void TestNormalMonster() {
        // Anordnen: Karten für den Test erstellen
        DuelCard dragonCard = createCard("1", "Dragon", 50);
        DuelCard knightCard = createCard("2", "Knight", 50);
        DuelCard wizardCard = createCard("3", "Wizard", 50);

        // Handeln & Überprüfen: Testen Sie den Schaden jeder Karte gegen die anderen
        testDamage(dragonCard, knightCard, 50, 50);
        testDamage(dragonCard, wizardCard, 50, 50);
        testDamage(knightCard, dragonCard, 50, 50);
        testDamage(knightCard, wizardCard, 50, 50);
        testDamage(wizardCard, dragonCard, 50, 50);
        testDamage(wizardCard, knightCard, 50, 50);
    }
}
