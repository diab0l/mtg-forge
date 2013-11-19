package forge.deck.generate;

import org.testng.Assert;
import org.testng.annotations.Test;

import forge.item.PaperCard;
import forge.util.ItemPoolView;

/**
 * Created by IntelliJ IDEA. User: dhudson
 */
@Test(groups = { "UnitTest" }, timeOut = 1000, enabled = false)
public class Generate5ColorDeckTest {

    /**
     * Generate5 color deck test1.
     */
    @Test(timeOut = 1000, enabled = false)
    public void generate5ColorDeckTest1() {
        final Generate5ColorDeck gen = new Generate5ColorDeck();
        final ItemPoolView<PaperCard> cardList = gen.getDeck(60, false);
        Assert.assertNotNull(cardList);
    }
}
