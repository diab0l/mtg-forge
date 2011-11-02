package forge.gui.input;

import java.util.ArrayList;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.ButtonUtil;
import forge.Card;
import forge.CardList;
import forge.ComputerUtil;
import forge.Constant;
import forge.Constant.Zone;
import forge.GameActionUtil;
import forge.Phase;
import forge.Player;
import forge.card.abilityFactory.AbilityFactory;
import forge.card.spellability.SpellAbility;
import forge.game.GamePlayerRating;
import forge.quest.data.QuestData;

/**
 * <p>
 * Input_Mulligan class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Input_Mulligan extends Input {
    /** Constant <code>serialVersionUID=-8112954303001155622L</code>. */
    private static final long serialVersionUID = -8112954303001155622L;

    private static final int MAGIC_NUMBER_OF_SHUFFLES = 100;
    private static final int AI_MULLIGAN_THRESHOLD = 5;

    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        ButtonUtil.enableAll();
        AllZone.getDisplay().getButtonOK().setText("No");
        AllZone.getDisplay().getButtonCancel().setText("Yes");
        AllZone.getDisplay().showMessage("Do you want to Mulligan?");
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonOK() {
        this.end();
    }

    /**
     * 
     * TODO Write javadoc for this method.
     * 
     * @param player
     *            a Player object
     * @param playerRating
     *            a GamePlayerRating object
     * @return an int
     */
    public final int doMulligan(final Player player, final GamePlayerRating playerRating) {
        final CardList hand = player.getCardsIn(Zone.Hand);
        for (final Card c : hand) {
            AllZone.getGameAction().moveToLibrary(c);
        }
        for (int i = 0; i < Input_Mulligan.MAGIC_NUMBER_OF_SHUFFLES; i++) {
            player.shuffle();
        }
        final int newHand = hand.size() - 1;
        for (int i = 0; i < newHand; i++) {
            player.drawCard();
        }
        playerRating.notifyHasMulliganed();
        playerRating.notifyOpeningHandSize(newHand);
        return newHand;
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonCancel() {
        final Player humanPlayer = AllZone.getHumanPlayer();
        final GamePlayerRating humanRating = AllZone.getGameInfo().getPlayerRating(humanPlayer.getName());

        final int newHand = this.doMulligan(humanPlayer, humanRating);

        final QuestData quest = AllZone.getQuestData();
        if ((quest != null) && quest.getInventory().hasItem("Sleight") && (humanRating.getMulliganCount() == 1)) {
            AllZone.getHumanPlayer().drawCard();
            humanRating.notifyOpeningHandSize(newHand + 1);
        }

        if (newHand == 0) {
            this.end();
        }
    } // selectButtonOK()

    /**
     * <p>
     * end.
     * </p>
     */
    final void end() {
        // Computer mulligan
        final Player aiPlayer = AllZone.getComputerPlayer();
        final GamePlayerRating aiRating = AllZone.getGameInfo().getPlayerRating(aiPlayer.getName());
        boolean aiTakesMulligan = true;

        // Computer mulligans if there are no cards with converted mana cost of
        // 0 in its hand
        while (aiTakesMulligan) {

            final CardList handList = aiPlayer.getCardsIn(Zone.Hand);
            final boolean hasLittleCmc0Cards = handList.getValidCards("Card.cmcEQ0", aiPlayer, null).size() < 2;
            aiTakesMulligan = (handList.size() > Input_Mulligan.AI_MULLIGAN_THRESHOLD) && hasLittleCmc0Cards;

            if (aiTakesMulligan) {
                this.doMulligan(aiPlayer, aiRating);
            }
        }

        // Human Leylines & Chancellors
        ButtonUtil.reset();
        final AbilityFactory af = new AbilityFactory();
        final CardList humanOpeningHand = AllZone.getHumanPlayer().getCardsIn(Zone.Hand);

        for (final Card c : humanOpeningHand) {
            final ArrayList<String> kws = c.getKeyword();
            for (int i = 0; i < kws.size(); i++) {
                final String kw = kws.get(i);

                if (kw.startsWith("MayEffectFromOpeningHand")) {
                    final String effName = kw.split(":")[1];

                    final SpellAbility effect = af.getAbility(c.getSVar(effName), c);
                    if (GameActionUtil.showYesNoDialog(c, "Use this card's ability?")) {
                        // If we ever let the AI memorize cards in the players
                        // hand, this would be a place to do so.
                        AllZone.getGameAction().playSpellAbilityNoStack(effect, false);
                    }
                }
            }
            if (c.getName().startsWith("Leyline")) {
                if (GameActionUtil.showYesNoDialog(c, "Use this card's ability?")) {
                    AllZone.getGameAction().moveToPlay(c);
                }
            }
        }

        // Computer Leylines & Chancellors
        final CardList aiOpeningHand = AllZone.getComputerPlayer().getCardsIn(Zone.Hand);
        for (final Card c : aiOpeningHand) {
            if (!c.getName().startsWith("Leyline")) {
                final ArrayList<String> kws = c.getKeyword();
                for (int i = 0; i < kws.size(); i++) {
                    final String kw = kws.get(i);

                    if (kw.startsWith("MayEffectFromOpeningHand")) {
                        final String effName = kw.split(":")[1];

                        final SpellAbility effect = af.getAbility(c.getSVar(effName), c);

                        // Is there a better way for the AI to decide this?
                        if (effect.doTrigger(false)) {
                            GameActionUtil.showInfoDialg("Computer reveals " + c.getName() + "(" + c.getUniqueNumber()
                                    + ").");
                            ComputerUtil.playNoStack(effect);
                        }
                    }
                }
            }
            if (c.getName().startsWith("Leyline")
                    && !(c.getName().startsWith("Leyline of Singularity") && (AllZoneUtil.getCardsIn(Zone.Battlefield,
                            "Leyline of Singularity").size() > 0))) {
                AllZone.getGameAction().moveToPlay(c);
                AllZone.getGameAction().checkStateEffects();
            }
        }
        AllZone.getGameAction().checkStateEffects();

        if (AllZone.getGameAction().isStartCut()
                && !(humanOpeningHand.contains(AllZone.getGameAction().getHumanCut()) || aiOpeningHand.contains(AllZone
                        .getGameAction().getComputerCut()))) {
            AllZone.getGameAction().moveTo(AllZone.getHumanPlayer().getZone(Constant.Zone.Library),
                    AllZone.getGameAction().getHumanCut());
            AllZone.getGameAction().moveTo(AllZone.getComputerPlayer().getZone(Constant.Zone.Library),
                    AllZone.getGameAction().getComputerCut());
        }
        AllZone.getGameAction().checkStateEffects();
        Phase.setGameBegins(1);
        AllZone.getPhase().setNeedToNextPhase(false);
        this.stop();
    }
}
