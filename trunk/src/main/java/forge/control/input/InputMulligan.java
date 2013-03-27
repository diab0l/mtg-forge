/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.control.input;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardPredicates;
import forge.Singletons;
import forge.card.ability.AbilityFactory;
import forge.card.spellability.SpellAbility;
import forge.game.GameAction;
import forge.game.GameState;
import forge.game.GameType;
import forge.game.ai.ComputerUtil;
import forge.game.player.AIPlayer;
import forge.game.player.Player;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiDialog;
import forge.gui.framework.SDisplayUtil;
import forge.gui.match.CMatchUI;
import forge.gui.match.nonsingleton.VField;
import forge.gui.match.views.VMessage;
import forge.view.ButtonUtil;
 /**
  * <p>
  * InputMulligan class.
  * </p>
  * 
  * @author Forge
  * @version $Id$
  */
public class InputMulligan extends InputBase {
    /** Constant <code>serialVersionUID=-8112954303001155622L</code>. */
    private static final long serialVersionUID = -8112954303001155622L;

    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        ButtonUtil.setButtonText("No", "Yes");
        ButtonUtil.enableAllFocusOk();

        GameState game = Singletons.getModel().getGame();
        Player startingPlayer = game.getPhaseHandler().getPlayerTurn();
        Player localPlayer = Singletons.getControl().getPlayer();

        StringBuilder sb = new StringBuilder();
        sb.append(startingPlayer.getName()).append(" is going first. ");

        if (!startingPlayer.equals(localPlayer)) {
            sb.append("You are going ").append(game.getOrdinalPosition(localPlayer, startingPlayer)).append(". ");
        }

        sb.append("Do you want to Mulligan?");
        CMatchUI.SINGLETON_INSTANCE.showMessage(sb.toString());
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonOK() {
        this.end();
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonCancel() {
        final Player humanPlayer = Singletons.getControl().getPlayer();
        humanPlayer.doMulligan();

        if (humanPlayer.getCardsIn(ZoneType.Hand).isEmpty()) {
            this.end();
        } else {
            ButtonUtil.enableAllFocusOk();
        }
    }

    final void end() {
        GameState game = Singletons.getModel().getGame();

        // Computer mulligan
        for (Player p : game.getPlayers()) {
            if (!(p instanceof AIPlayer)) {
                continue;
            }
            AIPlayer ai = (AIPlayer) p;
            while (ComputerUtil.wantMulligan(ai)) {
                ai.doMulligan();
            }
        }

        // Human Leylines & Chancellors
        ButtonUtil.reset();

        final GameAction ga = game.getAction();
        for (Player p : game.getPlayers()) {
            final List<Card> openingHand = new ArrayList<Card>(p.getCardsIn(ZoneType.Hand));

            for (final Card c : openingHand) {
                if (p.isHuman()) {
                    for (String kw : c.getKeyword()) {
                        if (kw.startsWith("MayEffectFromOpeningHand")) {
                            final String effName = kw.split(":")[1];

                            final SpellAbility effect = AbilityFactory.getAbility(c.getSVar(effName), c);
                            if (GuiDialog.confirm(c, "Use " + c +"'s  ability?")) {
                                // If we ever let the AI memorize cards in the players
                                // hand, this would be a place to do so.
                                game.getActionPlay().playSpellAbilityNoStack(p, effect, false);
                            }
                        }
                    }
                    if (c.getName().startsWith("Leyline of")) {
                        if (GuiDialog.confirm(c, "Use " + c + "'s ability?")) {
                            ga.moveToPlay(c);
                        }
                    }
                } else { // Computer Leylines & Chancellors
                    if (!c.getName().startsWith("Leyline of")) {
                        for (String kw : c.getKeyword()) {
                            if (kw.startsWith("MayEffectFromOpeningHand")) {
                                final String effName = kw.split(":")[1];

                                final SpellAbility effect = AbilityFactory.getAbility(c.getSVar(effName), c);

                                // Is there a better way for the AI to decide this?
                                if (effect.doTrigger(false, (AIPlayer)p)) {
                                    GuiDialog.message("Computer reveals " + c.getName() + "(" + c.getUniqueNumber() + ").");
                                    ComputerUtil.playNoStack((AIPlayer)p, effect, game);
                                }
                            }
                        }
                    }
                    if (c.getName().startsWith("Leyline of")
                            && !(c.getName().startsWith("Leyline of Singularity")
                            && (Iterables.any(game.getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Leyline of Singularity"))))) {
                        ga.moveToPlay(c);
                        //ga.checkStateEffects();
                    }
                }
            }
        }

        ga.checkStateEffects();
        
        Player next = game.getPhaseHandler().getPlayerTurn();
        
        if(game.getType() == GameType.Planechase)
        {
            next.initPlane();
        }

        //Set Field shown to current player.        
        VField nextField = CMatchUI.SINGLETON_INSTANCE.getFieldViewFor(next);
        SDisplayUtil.showTab(nextField);

        game.setMulliganned(true);
        Singletons.getModel().getMatch().getInput().clearInput();
    }

    @Override
    public void selectCard(Card c0) {
        Zone z0 = Singletons.getModel().getGame().getZoneOf(c0);
        if (c0.getName().equals("Serum Powder") && z0.is(ZoneType.Hand)) {
            if (GuiDialog.confirm(c0, "Use " + c0.getName() + "'s ability?")) {
                List<Card> hand = new ArrayList<Card>(c0.getController().getCardsIn(ZoneType.Hand));
                for (Card c : hand) {
                    Singletons.getModel().getGame().getAction().exile(c);
                }
                c0.getController().drawCards(hand.size());
            }
        } else {
            SDisplayUtil.remind(VMessage.SINGLETON_INSTANCE);
        }
    }
}
