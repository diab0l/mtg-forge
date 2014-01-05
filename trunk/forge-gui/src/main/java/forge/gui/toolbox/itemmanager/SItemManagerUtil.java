package forge.gui.toolbox.itemmanager;

import com.google.common.base.Predicate;

import forge.card.CardRules;
import forge.card.CardRulesPredicates;
import forge.gui.deckeditor.controllers.ACEditorBase;
import forge.gui.deckeditor.views.VCardCatalog;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.SkinImage;
import forge.util.ComparableOp;


/** 
 * Static methods for working with top-level editor methods,
 * included but not limited to preferences IO, icon generation,
 * and stats analysis.
 *
 * <br><br>
 * <i>(S at beginning of class name denotes a static factory.)</i>
 *
 */
public final class SItemManagerUtil  {
    /** An enum to encapsulate metadata for the stats/filter objects. */
    public static enum StatTypes {
        WHITE      (FSkin.ManaImages.IMG_WHITE,     CardRulesPredicates.Presets.IS_WHITE, "White cards"),
        BLUE       (FSkin.ManaImages.IMG_BLUE,      CardRulesPredicates.Presets.IS_BLUE, "Blue cards"),
        BLACK      (FSkin.ManaImages.IMG_BLACK,     CardRulesPredicates.Presets.IS_BLACK, "Black cards"),
        RED        (FSkin.ManaImages.IMG_RED,       CardRulesPredicates.Presets.IS_RED, "Red cards"),
        GREEN      (FSkin.ManaImages.IMG_GREEN,     CardRulesPredicates.Presets.IS_GREEN, "Green cards"),
        COLORLESS  (FSkin.ManaImages.IMG_COLORLESS, CardRulesPredicates.Presets.IS_COLORLESS, "Colorless cards"),
        MULTICOLOR (FSkin.EditorImages.IMG_MULTI,   CardRulesPredicates.Presets.IS_MULTICOLOR, "Multicolor cards"),

        PACK_OR_DECK (FSkin.EditorImages.IMG_PACK,         null, "Card packs and prebuilt decks"),
        LAND         (FSkin.EditorImages.IMG_LAND,         CardRulesPredicates.Presets.IS_LAND, "Lands"),
        ARTIFACT     (FSkin.EditorImages.IMG_ARTIFACT,     CardRulesPredicates.Presets.IS_ARTIFACT, "Artifacts"),
        CREATURE     (FSkin.EditorImages.IMG_CREATURE,     CardRulesPredicates.Presets.IS_CREATURE, "Creatures"),
        ENCHANTMENT  (FSkin.EditorImages.IMG_ENCHANTMENT,  CardRulesPredicates.Presets.IS_ENCHANTMENT, "Enchantments"),
        PLANESWALKER (FSkin.EditorImages.IMG_PLANESWALKER, CardRulesPredicates.Presets.IS_PLANESWALKER, "Planeswalkers"),
        INSTANT      (FSkin.EditorImages.IMG_INSTANT,      CardRulesPredicates.Presets.IS_INSTANT, "Instants"),
        SORCERY      (FSkin.EditorImages.IMG_SORCERY,      CardRulesPredicates.Presets.IS_SORCERY, "Sorcerys"),

        CMC_0 (FSkin.ColorlessManaImages.IMG_0, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 0), "Cards with CMC 0"),
        CMC_1 (FSkin.ColorlessManaImages.IMG_1, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 1), "Cards with CMC 1"),
        CMC_2 (FSkin.ColorlessManaImages.IMG_2, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 2), "Cards with CMC 2"),
        CMC_3 (FSkin.ColorlessManaImages.IMG_3, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 3), "Cards with CMC 3"),
        CMC_4 (FSkin.ColorlessManaImages.IMG_4, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 4), "Cards with CMC 4"),
        CMC_5 (FSkin.ColorlessManaImages.IMG_5, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 5), "Cards with CMC 5"),
        CMC_6 (FSkin.ColorlessManaImages.IMG_6, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.GT_OR_EQUAL, 6), "Cards with CMC 6+");

        public final SkinImage img;
        public final Predicate<CardRules> predicate;
        public final String label;

        StatTypes(FSkin.SkinProp prop, Predicate<CardRules> pred, String label0) {
            img = FSkin.getImage(prop, 18, 18);
            predicate = pred;
            label = label0;
        }
    }

    /**
     * Divides X by Y, multiplies by 100, rounds, returns.
     * 
     * @param x0 &emsp; Numerator (int)
     * @param y0 &emsp; Denominator (int)
     * @return rounded result (int)
     */
    public static int calculatePercentage(final int x0, final int y0) {
        return (int) Math.round((double) (x0 * 100) / (double) y0);
    }

    /**
     * Resets components that may have been changed
     * by various configurations of the deck editor.
     */
    public static void resetUI(ACEditorBase<?, ?> editor) {
        editor.getBtnAdd4().setVisible(true);
        editor.getBtnRemove4().setVisible(true);

        VCurrentDeck.SINGLETON_INSTANCE.getBtnSave().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSaveAs().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnNew().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnOpen().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnImport().setVisible(true);

        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setEnabled(true);

        VCardCatalog.SINGLETON_INSTANCE.getPnlHeader().setVisible(false);
        VCardCatalog.SINGLETON_INSTANCE.getLblTitle().setText("");

        VCurrentDeck.SINGLETON_INSTANCE.getPnlHeader().setVisible(true);

        VCardCatalog.SINGLETON_INSTANCE.getTabLabel().setText("Card Catalog");

        VCurrentDeck.SINGLETON_INSTANCE.getBtnPrintProxies().setVisible(true);
        editor.getBtnCycleSection().setVisible(false);

        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getLblTitle().setText("Title:");
    }
}
