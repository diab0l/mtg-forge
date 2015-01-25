package forge.deck;

public enum DeckType {
    CUSTOM_DECK ("Custom User Decks"),
    PRECONSTRUCTED_DECK("Preconstructed Decks"),
    QUEST_OPPONENT_DECK ("Quest Opponent Decks"),
    COLOR_DECK ("Random Color Decks"),
    THEME_DECK ("Random Theme Decks"),
    RANDOM_DECK ("Random Decks"),
    NET_DECK ("Net Decks");

    private String value;
    private DeckType(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return value;
    }
    public static DeckType fromString(String value){
        for (final DeckType d : DeckType.values()) {
            if (d.toString().equalsIgnoreCase(value)) {
                return d;
            }
        }
        throw new IllegalArgumentException("No Enum specified for this string");
    }
}
