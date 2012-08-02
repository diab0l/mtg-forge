package forge;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import forge.game.limited.ReadDraftRankings;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.util.FileUtil;

/**
 * Tests for ReadDraftRankings.
 * 
 */
@Test(enabled = true)
public class ReadDraftRankingsTest {

    /**
     * Card test.
     */
    @Test(enabled = true)
    void test() {
        ReadDraftRankings rdr = new ReadDraftRankings();
        Assert.assertNotNull(rdr);

        List<String> cardLines = FileUtil.readFile(new File(ForgeProps.getFile(NewConstants.CARDSFOLDER) + "/g",
                "garruk_primal_hunter.txt"));
        Card c = CardReader.readCard(cardLines);
        Assert.assertEquals(1, rdr.getRanking(c.getName(), "M13").intValue());

        cardLines = FileUtil.readFile(new File(ForgeProps.getFile(NewConstants.CARDSFOLDER) + "/c", "clone.txt"));
        c = CardReader.readCard(cardLines);
        Assert.assertEquals(38, rdr.getRanking(c.getName(), "M13").intValue());

        cardLines = FileUtil.readFile(new File(ForgeProps.getFile(NewConstants.CARDSFOLDER) + "/t",
                "tamiyo_the_moon_sage.txt"));
        c = CardReader.readCard(cardLines);
        Assert.assertEquals(1, rdr.getRanking(c.getName(), "AVR").intValue());

        // Mikaeus, the Lunarch has a comma in its name in the rankings file
        cardLines = FileUtil.readFile(new File(ForgeProps.getFile(NewConstants.CARDSFOLDER) + "/m",
                "mikaeus_the_lunarch.txt"));
        c = CardReader.readCard(cardLines);
        Assert.assertEquals(4, rdr.getRanking(c.getName(), "ISD").intValue());

    }
}
