package forge;

import java.util.List;

import org.testng.annotations.Test;

import forge.card.BoosterGenerator;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.game.card.Card;
import forge.item.PaperCard;
import forge.item.SealedProduct;
import forge.limited.IBoosterDraft;

/**
 * <p>
 * BoosterDraftTest class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
@Test(groups = { "UnitTest" }, timeOut = 1000, enabled = false)
public class BoosterDraftTest implements IBoosterDraft {

    /** The n. */
    private int n = 3;

    /**
     * <p>
     * getDecks.
     * </p>
     * 
     * @return an array of {@link forge.deck.Deck} objects.
     */
    @Override
    @Test(timeOut = 1000)
    public Deck[] getDecks() {
        return null;
    }

    /**
     * <p>
     * nextChoice.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    @Override
    public CardPool nextChoice() {
        this.n--;
        SealedProduct.Template booster = Singletons.getMagicDb().getBoosters().get("M11");
        CardPool result = new CardPool();
        result.addAllFlat(BoosterGenerator.getBoosterPack(booster));
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void setChoice(final PaperCard c) {
        System.out.println(c.getName());
    }

    /**
     * <p>
     * hasNextChoice.
     * </p>
     * 
     * @return a boolean.
     */
    @Override
    public boolean hasNextChoice() {
        return this.n > 0;
    }

    /**
     * <p>
     * getChosenCards.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public List<Card> getChosenCards() {
        return null;
    }

    /**
     * <p>
     * getUnchosenCards.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public List<Card> getUnchosenCards() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.game.limited.IBoosterDraft#finishedDrafting()
     */
    @Override
    public void finishedDrafting() {

    }
}
