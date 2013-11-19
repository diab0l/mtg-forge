package forge.deck.generate;

import org.testng.Assert;
import org.testng.annotations.Test;

import forge.item.PaperCard;
import forge.util.ItemPoolView;

/**
 * Created by IntelliJ IDEA. User: dhudson
 */
@Test(groups = { "UnitTest" }, timeOut = 1000, enabled = false)
public class Generate3ColorDeckTest {

    /**
     * Generate3 color deck test1.
     */
    @Test(timeOut = 1000, enabled = false)
    public void generate3ColorDeckTest1() {
        final Generate3ColorDeck gen = new Generate3ColorDeck("white", "blue", "black");
        final ItemPoolView<PaperCard> cardList = gen.getDeck(60, false);
        Assert.assertNotNull(cardList);
    }
}
