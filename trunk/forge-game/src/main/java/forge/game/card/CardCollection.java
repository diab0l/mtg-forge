package forge.game.card;

import java.util.Collection;
import forge.util.FCollection;

public class CardCollection extends FCollection<Card> {
    public static final CardCollection EMPTY = new CardCollection();

    public static boolean hasCard(CardCollection cards) {
        return cards != null && !cards.isEmpty();
    }
    public static boolean hasCard(CardCollection cards, Card c) {
        return cards != null && cards.contains(c);
    }
    public static CardCollectionView getView(CardCollection cards) {
        return getView(cards, false);
    }
    public static CardCollectionView getView(CardCollection cards, boolean allowModify) {
        if (cards == null) {
            return EMPTY.getView();
        }
        if (allowModify) { //create copy to allow modifying original set while iterating
            return new CardCollection(cards).getView();
        }
        return cards.getView();
    }

    public CardCollection() {
        super();
    }
    public CardCollection(Card card) {
        super(card);
    }
    public CardCollection(Collection<Card> cards) {
        super(cards);
    }
    public CardCollection(Iterable<Card> cards) {
        super(cards);
    }

    @Override
    protected FCollection<Card> createNew() {
        return new CardCollection();
    }

    public CardCollection subList(int fromIndex, int toIndex) {
        return (CardCollection)super.subList(fromIndex, toIndex);
    }

    public CardCollectionView getView() {
        if (view == null) {
            view = new CardCollectionView();
        }
        return (CardCollectionView)view;
    }

    public class CardCollectionView extends FCollectionView {
        protected CardCollectionView() {
        }
    }
}
