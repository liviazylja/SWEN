package Project_Logic.Monster_Logic;



// Enum für verschiedene Monsterarten.
public enum TypeofMonsters {

    Goblin("Goblin"),
    Dragon("Dragon"),
    Wizard("Wizard"),
    Ork("Ork"),
    Knight("Knight"),
    Troll("Troll"),
    Kraken("Kraken"),
    FireElf("FireElf"),
    Spell("Spell"),
    magicdice("MagicDice");

    // Name der Monsterkategorie.
    private final String monsterKategorieName;

    // Konstruktor für Monsterarten-Enum.
    TypeofMonsters(String monsterKategorieName) {
        this.monsterKategorieName = monsterKategorieName;
    }

    // Gibt den Namen der Monsterkategorie zurück.
    public String getMonsterCategoryName() {
        return this.monsterKategorieName;
    }
}

