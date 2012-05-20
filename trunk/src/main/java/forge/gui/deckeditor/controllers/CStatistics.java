package forge.gui.deckeditor.controllers;

import java.util.Map.Entry;

import forge.Command;
import forge.card.CardColor;
import forge.card.CardRules;
import forge.card.CardRules.Predicates;
import forge.deck.DeckBase;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.SEditorUtil;
import forge.gui.deckeditor.views.VStatistics;
import forge.gui.framework.ICDoc;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.util.closures.Predicate;

/** 
 * Controls the "analysis" panel in the deck editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CStatistics implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    //========== Overridden methods

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    public void initialize() {
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        analyze();
    }

    //========== Other methods
    @SuppressWarnings("unchecked")
    private <T extends InventoryItem, TModel extends DeckBase> void analyze() {
        final ACEditorBase<T, TModel> ed = (ACEditorBase<T, TModel>)
                CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        if (ed == null) { return; }

        final ItemPoolView<CardPrinted> deck = ItemPool.createFrom(
                ed.getTableDeck().getCards(), CardPrinted.class);

        int tmp = 0;
        int total = deck.countAll();

        // Hack-ish: avoid /0 cases, but still populate labels :)
        if (total == 0) { total = 1; }

        tmp = CardRules.Predicates.Presets.IS_MULTICOLOR
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblMulti().setText(String.valueOf(tmp));

        tmp = CardRules.Predicates.Presets.IS_CREATURE
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblCreature().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = CardRules.Predicates.Presets.IS_LAND
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblLand().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = CardRules.Predicates.Presets.IS_ENCHANTMENT
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblEnchantment().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = CardRules.Predicates.Presets.IS_ARTIFACT
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblArtifact().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = CardRules.Predicates.Presets.IS_INSTANT
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblInstant().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = CardRules.Predicates.Presets.IS_SORCERY
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblSorcery().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = CardRules.Predicates.Presets.IS_PLANESWALKER
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblPlaneswalker().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = CardRules.Predicates.Presets.IS_COLORLESS
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblColorless().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = Predicate.and(
                Predicates.isColor(CardColor.BLACK),
                Predicates.hasCntColors((byte) 1))
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblBlack().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = Predicate.and(
                Predicates.isColor(CardColor.BLUE),
                Predicates.hasCntColors((byte) 1))
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblBlue().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = Predicate.and(
                Predicates.isColor(CardColor.GREEN),
                Predicates.hasCntColors((byte) 1))
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblGreen().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = Predicate.and(
                Predicates.isColor(CardColor.RED),
                Predicates.hasCntColors((byte) 1))
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblRed().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        tmp = Predicate.and(
                Predicates.isColor(CardColor.WHITE),
                Predicates.hasCntColors((byte) 1))
                .aggregate(deck, deck.getFnToCard(), deck.getFnToCount());
        VStatistics.SINGLETON_INSTANCE.getLblWhite().setText(
                tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

        int cmc0 = 0, cmc1 = 0, cmc2 = 0, cmc3 = 0, cmc4 = 0, cmc5 = 0, cmc6 = 0;
        int tmc = 0;

        for (final Entry<CardPrinted, Integer> e : deck) {
            final CardRules cardRules = e.getKey().getCard();
            final int count = e.getValue();
            final int cmc = cardRules.getManaCost().getCMC();

            if (cmc == 0)       { cmc0 += count; }
            else if (cmc == 1)  { cmc1 += count; }
            else if (cmc == 2)  { cmc2 += count; }
            else if (cmc == 3)  { cmc3 += count; }
            else if (cmc == 4)  { cmc4 += count; }
            else if (cmc == 5)  { cmc5 += count; }
            else if (cmc >= 6)  { cmc6 += count; }

            tmc += (cmc * count);
        }

        VStatistics.SINGLETON_INSTANCE.getLblCMC0().setText(
                cmc0 + " (" + SEditorUtil.calculatePercentage(cmc0, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC1().setText(
                cmc1 + " (" + SEditorUtil.calculatePercentage(cmc1, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC2().setText(
                cmc2 + " (" + SEditorUtil.calculatePercentage(cmc2, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC3().setText(
                cmc3 + " (" + SEditorUtil.calculatePercentage(cmc3, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC4().setText(
                cmc4 + " (" + SEditorUtil.calculatePercentage(cmc4, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC5().setText(
                cmc5 + " (" + SEditorUtil.calculatePercentage(cmc5, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC6().setText(
                cmc6 + " (" + SEditorUtil.calculatePercentage(cmc6, total) + "%)");

        double amc = (double) Math.round((double) tmc / (double) total * 100) / 100.0d;

        VStatistics.SINGLETON_INSTANCE.getLblTotal().setText("TOTAL CARDS: " + deck.countAll());
        VStatistics.SINGLETON_INSTANCE.getLblTMC().setText("TOTAL MANA COST: " + tmc);
        VStatistics.SINGLETON_INSTANCE.getLblAMC().setText("AVERAGE MANA COST: " + amc);
    }
}
