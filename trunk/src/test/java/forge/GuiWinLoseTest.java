package forge;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dhudson
 */
@Test(groups = {"UnitTest", "fast"})
public class GuiWinLoseTest {
    /**
     *
     *
     */
    @Test(groups = {"UnitTest", "fast"})
    public void GuiWinLoseTest1() {
        Gui_WinLose dialog = new Gui_WinLose();
        dialog.setVisible(true);
        Assert.assertNotNull(dialog);
        dialog.dispose();
    }
}
