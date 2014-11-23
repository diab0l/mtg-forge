package forge.deck.generation;

import java.util.HashMap;

import forge.item.PaperCard;

public class DeckGenPool implements IDeckGenPool {
    private final HashMap<String, PaperCard> cards = new HashMap<String, PaperCard>();

    public void add(PaperCard c) {
        cards.put(c.getName(), c);
    }
    public void addAll(Iterable<PaperCard> cc) {
        for (PaperCard c : cc) {
            add(c);
        }
    }

    @Override
    public PaperCard getCard(String name) {
        return cards.get(name);
    }

    @Override
    public PaperCard getCard(String name, String edition) {
        return cards.get(name);
    }

    @Override
    public PaperCard getCard(String name, String edition, int artIndex) {
        return cards.get(name);
    }

    @Override
    public Iterable<PaperCard> getAllCards() {
        return cards.values();
    }
}
