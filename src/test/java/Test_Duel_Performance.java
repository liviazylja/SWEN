import Project_Logic.CardandDuel_Logic.DuelCard;
import Project_Logic.CardandDuel_Logic.CardDeck;
import Project_Logic.CardandDuel_Logic.DuelManager;
import Project_Logic.Monster_Logic.Elements;
import Project_Logic.Monster_Logic.TypeofMonsters;
import Project_Logic.User_Logic.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Test_Duel_Performance {
    // Deklariere gemockte Benutzerobjekte, um echte Benutzer in den Tests zu simulieren
    @Mock
    private User userA;
    @Mock
    private User userB;

    // Deklariere ein CardDeck-Objekt, das in den Testfällen verwendet wird
    private CardDeck deck_0;

    // Hilfsmethode zum Erstellen einer Karte
    private DuelCard createCard(String id, String name, int damage, TypeofMonsters category, Elements element) {
        return new DuelCard(id, name, damage, category, element);
    }

    // Hilfsmethode zum Erstellen eines Kartendecks
    private CardDeck createDeck(DuelCard... cards) {
        return new CardDeck(Arrays.asList(cards));
    }


     //Die Setup-Methode, die vor jedem Testfall ausgeführt wird.
     //Sie erstellt ein Deck mit vier identischen Karten für Tests.

    @BeforeEach
    void setUp() {
        // Verwende die Hilfsmethode, um vier identische Karten für das Testdeck zu erstellen
        DuelCard card1 = createCard("1","Kraken_0",0, TypeofMonsters.Kraken, Elements.Water);
        DuelCard card2 = createCard("2","Kraken_0",0, TypeofMonsters.Kraken, Elements.Water);
        DuelCard card3 = createCard("3","Kraken_0",0, TypeofMonsters.Kraken, Elements.Water);
        DuelCard card4 = createCard("4","Kraken_0",0, TypeofMonsters.Kraken, Elements.Water);

        // Verwende die Hilfsmethode, um ein Deck mit den vier Karten zu erstellen
        deck_0 = createDeck(card1, card2, card3, card4);
    }



    // Methoden zum Einrichten eines Duells
    private void setupBattle(User userA, User userB, CardDeck deckA, CardDeck deckB) {
        // Definiere das Verhalten der gemockten Benutzerobjekte, wenn die getName-Methode aufgerufen wird
        when(userA.getName()).thenReturn("MockUser_1");
        when(userB.getName()).thenReturn("MockUser_2");

        // Hole die Singleton-Instanz der Battle-Klasse und führe ein Duell aus
        DuelManager.getManager().executeDuel(userA, userB, deckA, deckB);
    }

    @Test
    public void testDraw() {
        // Anordnen: Richte das Duell ein
        setupBattle(userA, userB, deck_0, deck_0);

        // Überprüfen: Überprüfe, ob die erwarteten Ergebnisse eingetreten sind
        verify(userA).recordTie();
        verify(userB).recordTie();
    }

     //Dieser Testfall überprüft die win()- und lose()-Methoden in der Battle-Klasse.
     //Es wird überprüft, ob der Benutzer mit dem stärkeren Deck die win()-Methode aufruft und der Benutzer mit dem schwächeren Deck die lose()-Methode aufruft.

    @Test
    public void testWin() {
        // Hole die Battle-Instanz
        DuelManager manager = DuelManager.getManager();

        // Erstelle eine Gewinnkarte und eine Liste von Gewinnkarten für das gewinnende Deck
        DuelCard winningCard = new DuelCard("1", "Strong_50", 50, TypeofMonsters.Kraken, Elements.Water);
        List<DuelCard> winningCards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            winningCards.add(new DuelCard(winningCard.getCardId(), winningCard.getCardName(), winningCard.getAttackPoints(), winningCard.getCategory(), winningCard.getElementType()));
        }
        CardDeck deck_winner = new CardDeck(winningCards);

        // Erstelle eine Verliererkarte und eine Liste von Verliererkarten für das verlierende Deck
        DuelCard losingCard = new DuelCard("2", "Weak_20", 20, TypeofMonsters.Kraken, Elements.Water);
        List<DuelCard> losingCards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            losingCards.add(new DuelCard(losingCard.getCardId(), losingCard.getCardName(), losingCard.getAttackPoints(), losingCard.getCategory(), losingCard.getElementType()));
        }
        CardDeck deck_loser = new CardDeck(losingCards);

        // Setze das Verhalten des gemockten User-Objekts auf
        when(userA.getName()).thenReturn("MockUser_1");
        when(userB.getName()).thenReturn("MockUser_2");

        // Führe ein Duell mit den gemockten Benutzern und den gewinnenden/verlierenden Decks aus
        manager.executeDuel(userA, userB, deck_winner, deck_loser);

        // Überprüfe, ob userA die win()-Methode aufgerufen hat und userB die lose()-Methode aufgerufen hat
        verify(userA).recordWin();
        verify(userB).recordLoss();
    }
}

