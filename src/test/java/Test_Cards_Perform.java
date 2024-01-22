import Project_Logic.CardandDuel_Logic.DuelCard;
import Project_Logic.Monster_Logic.Elements;
import Project_Logic.Monster_Logic.TypeofMonsters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Project_Logic.CardandDuel_Logic.CardDeck;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Test_Cards_Perform {

    // Declare an ArrayList of Card objects and a CardDeck
    List<DuelCard> cards = new ArrayList<>();
    CardDeck deck;

    /**
     * This method sets up the initial conditions for each test.
     * It is run before each test case and initializes a deck of cards with various characteristics.
     */
    @BeforeEach
    void setUp() {
        // Add various cards to the ArrayList
        cards.add(new DuelCard("0", "Purple Kraken", 140, TypeofMonsters.Kraken, Elements.Water));
        cards.add(new DuelCard("1", "Fiery Ork", 98, TypeofMonsters.Ork, Elements.Fire));
        cards.add(new DuelCard("2", "Yellow Dragon", 117, TypeofMonsters.Dragon, Elements.Normal));
        cards.add(new DuelCard("3", "Blue Goblin", 87, TypeofMonsters.Goblin, Elements.Water));
        cards.add(new DuelCard("4", "Fire Wizard", 117, TypeofMonsters.Wizard, Elements.Fire));
        cards.add(new DuelCard("5", "Green FireElf", 107, TypeofMonsters.FireElf, Elements.Normal));
        cards.add(new DuelCard("6", "Red Knight", 120, TypeofMonsters.Knight, Elements.Normal));
        cards.add(new DuelCard("7", "Gray WaterElf", 106, TypeofMonsters.FireElf, Elements.Water));
        cards.add(new DuelCard("8", "Deep Ocean Spell", 89, TypeofMonsters.Spell, Elements.Water));
        cards.add(new DuelCard("9", "Black Spell", 100, TypeofMonsters.Spell, Elements.Normal));
        cards.add(new DuelCard("10", "Red Fire Spell", 100, TypeofMonsters.Spell, Elements.Fire));

        //Initialize an ArrayList
        List<DuelCard> testDeck = new ArrayList<>();

        //Add four random cards to the temporary Deck
        for (int i = 0; i < 4; i++) {
            DuelCard card = cards.get((int)(Math.random() * cards.size()));
            cards.remove(card);
            testDeck.add(card);
        }

        //Create a new CardDeck object
        deck = new CardDeck(testDeck);
    }


    /**
     * This test case checks the functionality of adding cards to the deck.
     * It begins with a deck of four cards, adds two more, checks the size,
     * and then removes one card and checks the size again.
     */
    @Test
    public void addCardToDeck() {
        //Initialize ArrayList and fill it up
        List<DuelCard> testDeck = new ArrayList<>();

        DuelCard card1 = new DuelCard("1", "Blue Dragon", 117, TypeofMonsters.Dragon, Elements.Water);
        DuelCard card2 = new DuelCard("2", "Old Fire Elf", 107, TypeofMonsters.FireElf, Elements.Fire);
        DuelCard card3 = new DuelCard("3", "Green Goblin", 87, TypeofMonsters.Goblin, Elements.Normal);
        DuelCard card4 = new DuelCard("4", "Heavy Knight", 120, TypeofMonsters.Knight, Elements.Normal);

        testDeck.add(card1);
        testDeck.add(card2);
        testDeck.add(card3);
        testDeck.add(card4);

        CardDeck deck = new CardDeck(testDeck);

        //Add two new cards to the deck
        DuelCard card5 = new DuelCard("5", "Red Dragon", 123, TypeofMonsters.Dragon, Elements.Fire);
        DuelCard card6 = new DuelCard("6", "Old Water Elf", 106, TypeofMonsters.FireElf, Elements.Water);
        deck.addCardToDeck(card5);
        deck.addCardToDeck(card6);

        //Assert that deck size is 6, remove one card then assert that deck size is 5
        assertEquals(6, deck.countDeckCards());
        deck.discardCard(deck.pickRandomCard());
        assertEquals(5, deck.countDeckCards());
    }



    /**
     * This test case validates the functionality of removing a card from the deck.
     * It initializes a deck with four cards, removes one, and then checks if the deck size is reduced to three.
     */
    @Test
    public void removeCardFromDeck() {
        //Initialize ArrayList
        List<DuelCard> testDeck = new ArrayList<>();

        //Add cards
        DuelCard card1 = new DuelCard("1", "Blue Dragon", 117, TypeofMonsters.Dragon, Elements.Water);
        DuelCard card2 = new DuelCard("2", "Old Fire Elf", 107, TypeofMonsters.FireElf, Elements.Fire);
        DuelCard card3 = new DuelCard("3", "Green Goblin", 87, TypeofMonsters.Goblin, Elements.Normal);
        DuelCard card4 = new DuelCard("4", "Heavy Knight", 120, TypeofMonsters.Knight, Elements.Normal);

        testDeck.add(card1);
        testDeck.add(card2);
        testDeck.add(card3);
        testDeck.add(card4);

        CardDeck deck = new CardDeck(testDeck);

        //Delete card and assert
        deck.discardCard(card1);
        assertEquals(3, deck.countDeckCards());
    }



    /**
     * This test case checks the size of the deck after initialization.
     * The expected deck size is 4 after randomly selecting four cards from a list of 10.
     */
    @Test
    public void testDeckSize() {
        List<DuelCard> deckList = new ArrayList<>();
        deckList.add(new DuelCard("0", "Fire Dragon", 120, TypeofMonsters.Dragon, Elements.Fire));
        deckList.add(new DuelCard("1", "Water Elf", 110, TypeofMonsters.FireElf, Elements.Water));
        deckList.add(new DuelCard("2", "Normal Goblin", 95, TypeofMonsters.Goblin, Elements.Normal));
        deckList.add(new DuelCard("3", "Strong Knight", 135, TypeofMonsters.Knight, Elements.Normal));
        deckList.add(new DuelCard("4", "Black Kraken", 140, TypeofMonsters.Kraken, Elements.Water));
        deckList.add(new DuelCard("5", "Gray Ork", 105, TypeofMonsters.Ork, Elements.Normal));
        deckList.add(new DuelCard("6", "Red Wizard", 118, TypeofMonsters.Wizard, Elements.Fire));
        deckList.add(new DuelCard("7", "Water Spell", 100, TypeofMonsters.Spell, Elements.Water));
        deckList.add(new DuelCard("8", "Fire Spell", 110, TypeofMonsters.Spell, Elements.Fire));

        //Initialize an Array list for the temporary Deck
        List<DuelCard> testDeckDeck = new ArrayList<>();

        //Add four random cards
        for (int i = 0; i < 4; i++) {
            DuelCard card = deckList.get((int) (Math.random() * deckList.size()));
            deckList.remove(card);
            testDeckDeck.add(card);
        }

        //Create a new CardDeck object and assert
        CardDeck deck = new CardDeck(testDeckDeck);
        assertEquals(4, deck.countDeckCards());
    }
}
