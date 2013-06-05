package forge.deck.generate;

import org.testng.Assert;
import org.testng.annotations.Test;

import forge.item.PaperCard;
import forge.item.ItemPoolView;

/**
 * Created by IntelliJ IDEA. User: dhudson
 */
@Test(groups = { "UnitTest" }, enabled = false)
public class Generate2ColorDeckTest {

    /**
     * Generate2 color deck test1.
     */
    @Test(enabled = false)
    public void generate2ColorDeckTest1() {
        final Generate2ColorDeck gen = new Generate2ColorDeck("white", "blue");
        final ItemPoolView<PaperCard> cardList = gen.getDeck(60, false);
        Assert.assertNotNull(cardList);
    }
}
