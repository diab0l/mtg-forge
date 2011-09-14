package forge.card;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import forge.CardList;

import net.slightlymagic.braids.util.lambda.Lambda1;

/**
 * <p>CardPoolView class.</p>
 *
 * @author Forge
 * @version $Id: CardPoolView.java 9708 2011-08-09 19:34:12Z jendave $
 */
public class CardPoolView<T extends InventoryItem> implements Iterable<Entry<T, Integer>> {

    // Field Accessors for select/aggregate operations with filters.
    public final Lambda1<CardRules, Entry<T, Integer>> fnToCard =
        new Lambda1<CardRules, Entry<T, Integer>>() {
          @Override public CardRules apply(final Entry<T, Integer> from) {
              T item = from.getKey();
              return item instanceof CardPrinted ? ((CardPrinted) item).getCard() : null; 
          }
        };
    public final Lambda1<T, Entry<T, Integer>> fnToPrinted =
        new Lambda1<T, Entry<T, Integer>>() {
          @Override public T apply(final Entry<T, Integer> from) { return from.getKey(); }
        };
    public final Lambda1<String, Entry<T, Integer>> fnToCardName =
        new Lambda1<String, Entry<T, Integer>>() {
          @Override public String apply(final Entry<T, Integer> from) { return from.getKey().getName(); }
        };
    public final Lambda1<Integer, Entry<T, Integer>> fnToCount =
        new Lambda1<Integer, Entry<T, Integer>>() {
            @Override public Integer apply(final Entry<T, Integer> from) { return from.getValue(); }
        };

    // Constructors
    public CardPoolView() { cards = new Hashtable<T, Integer>(); }
    public CardPoolView(final Map<T, Integer> inMap) { cards = inMap; }

    // Data members
    protected Map<T, Integer> cards;

    // same thing as above, it was copied to provide sorting (needed by table views in deck editors) 
    protected List<Entry<T, Integer>> cardsListOrdered = new ArrayList<Map.Entry<T,Integer>>();
    protected boolean isListInSync = false;

    @Override
    public final Iterator<Entry<T, Integer>> iterator() { return cards.entrySet().iterator(); }

    // Cards read only operations
    public final boolean contains(final T card) {
        if (cards == null) { return false; }
        return cards.containsKey(card);
    }
    public final int count(final T card) {
        if (cards == null) { return 0; }
        Integer boxed = cards.get(card);
        return boxed == null ? 0 : boxed.intValue();
    }
    public final int countAll() {
        int result = 0;
        if (cards != null) { for (Integer n : cards.values()) { result += n; } }
        return result;
    }
    public final int countDistinct() { return cards.size(); }
    public final boolean isEmpty() { return cards == null || cards.isEmpty(); }

    public final List<Entry<T, Integer>> getOrderedList() {
        if (!isListInSync) { rebuildOrderedList(); }
        return cardsListOrdered;
    }

    private void rebuildOrderedList() {
        cardsListOrdered.clear();
        if (cards != null) {
            for (Entry<T, Integer> e : cards.entrySet()) {
                cardsListOrdered.add(e);
            }
        }
        isListInSync = true;
    }

    public final List<T> toFlatList() {
        List<T> result = new ArrayList<T>();
        for (Entry<T, Integer> e : this) {
            for (int i = 0; i < e.getValue(); i++) { result.add(e.getKey()); }
        }
        return result;
    }

    public final CardList toForgeCardList() {
        CardList result = new CardList();
        for (Entry<T, Integer> e : this) {
            if ( e.getKey() instanceof CardPrinted )
            {
            for (int i = 0; i < e.getValue(); i++) {
                result.add(((CardPrinted) e.getKey()).toForgeCard());
            }
            }
        }
        return result;
    }
}
