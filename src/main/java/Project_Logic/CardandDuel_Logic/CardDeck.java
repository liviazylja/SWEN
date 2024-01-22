package Project_Logic.CardandDuel_Logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Repräsentiert ein Deck von Karten.
 */
public class CardDeck {

    // Liste zur Speicherung der Karten im Deck
    private List<DuelCard> deckCards = new ArrayList<>();

    // Zusätzlicher nicht genutzter Code als Beispiel
    private int unusedCodeExample;

    // Konstruktor, der das Deck mit einer vorgegebenen Liste von Karten initialisiert
    public CardDeck(List<DuelCard> initialDeck) {
        if (initialDeck == null) return;
        // Fügt nur die ersten vier Karten zum Deck hinzu
        this.deckCards.addAll(initialDeck.stream().limit(4).collect(Collectors.toList()));
    }

    // Fügt eine Karte zum Deck hinzu, wenn sie noch nicht vorhanden ist
    public void addCardToDeck(DuelCard cardToAdd) {
        deckCards.stream()
                .filter(card -> card.equals(cardToAdd))
                .findFirst()
                .ifPresentOrElse(
                        existingCard -> {}, // Leere Lambda-Funktion, falls Karte vorhanden ist
                        () -> deckCards.add(cardToAdd) // Fügt Karte hinzu, falls nicht vorhanden
                );
    }

    // Entfernt eine Karte aus dem Deck
    public void discardCard(DuelCard cardToRemove) {
        Optional.ofNullable(deckCards).ifPresent(cards -> cards.remove(cardToRemove));
    }

    // Wählt eine zufällige Karte aus dem Deck aus
    public DuelCard pickRandomCard() {
        return (deckCards.isEmpty()) ? null :
                deckCards.get(ThreadLocalRandom.current().nextInt(deckCards.size()));
    }

    // Überprüft, ob das Deck leer ist
    public boolean isDeckEmpty() {
        return deckCards.isEmpty();
    }

    // Gibt die Anzahl der Karten im Deck zurück
    public int countDeckCards() {
        return deckCards.size();
    }

    // Das Deck das nicht verwendet wird. not completed
    public void notusedDeck() {
        unusedCodeExample = 42; // Beispielwert
    }
}
