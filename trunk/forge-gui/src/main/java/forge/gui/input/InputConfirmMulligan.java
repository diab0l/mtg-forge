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
package forge.gui.input;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import forge.Card;
import forge.FThreads;
import forge.game.Game;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiDialog;
import forge.gui.match.CMatchUI;
import forge.util.Lang;
import forge.view.ButtonUtil;
 /**
  * <p>
  * InputMulligan class.
  * </p>
  * 
  * @author Forge
  * @version $Id$
  */
public class InputConfirmMulligan extends InputSyncronizedBase {
    /** Constant <code>serialVersionUID=-8112954303001155622L</code>. */
    private static final long serialVersionUID = -8112954303001155622L;
    
    
    boolean keepHand = false;
    final boolean isCommander;
    
    private final List<Card> selected = new ArrayList<Card>();
    private final Player player;
    private final Player startingPlayer;
    
    public InputConfirmMulligan(Player humanPlayer, Player startsGame, boolean commander) {
        player = humanPlayer;
        isCommander = commander;
        startingPlayer = startsGame; 
    }
    
    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        Game game = player.getGame();

        StringBuilder sb = new StringBuilder();
        if( startingPlayer == player ) {
            sb.append(player).append(", you are going first!\n\n");
        } else {
            sb.append(startingPlayer.getName()).append(" is going first.\n");
            sb.append(player).append(", you are going ").append(Lang.getOrdinal(game.getPosition(player, startingPlayer))).append(".\n\n");
        }

        if ( isCommander ) {
            ButtonUtil.setButtonText("Keep", "Exile");
            ButtonUtil.enableOnlyOk();
            sb.append("Will you keep your hand or choose some cards to exile those and draw one less card?");
        } else {
            ButtonUtil.setButtonText("Keep", "Mulligan");
            ButtonUtil.enableAllFocusOk();
            sb.append("Do you want to keep your hand?");
        }

        showMessage(sb.toString());
    }

    /** {@inheritDoc} */
    @Override
    protected final void onOk() {
        keepHand = true;
        done();
    }

    /** {@inheritDoc} */
    @Override
    protected final void onCancel() {
        keepHand = false;
        done();
    }

    private void done() {
        ButtonUtil.reset();
        if (isCommander) {
            // Clear the "selected" icon after clicking the done button
            for(Card c : this.selected) {
                CMatchUI.SINGLETON_INSTANCE.setUsedToPay(c, false);
            }
        }
        stop();
    }

    volatile boolean cardSelectLocked = false;
    
    @Override
    protected void onCardSelected(final Card c0, final MouseEvent triggerEvent) { // the only place that would cause troubles - input is supposed only to confirm, not to fire abilities 
        
        boolean fromHand = player.getZone(ZoneType.Hand).contains(c0);
        boolean isSerumPowder = c0.getName().equals("Serum Powder");
        boolean isLegalChoice = fromHand && (isCommander || isSerumPowder);
        if ( !isLegalChoice || cardSelectLocked ) {
            flashIncorrectAction();
            return;
        }
        
        if ( isSerumPowder && GuiDialog.confirm(c0, "Use " + c0.getName() + "'s ability?")) {
            cardSelectLocked = true;
            FThreads.invokeInGameThread(new Runnable() { 
                public void run() {
                    List<Card> hand = new ArrayList<Card>(c0.getController().getCardsIn(ZoneType.Hand));
                    for (Card c : hand) {
                        player.getGame().getAction().exile(c);
                    }
                    c0.getController().drawCards(hand.size());
                    cardSelectLocked = false;
                }
            });
            return;
        }

        if ( isCommander ) { // allow to choose cards for partial paris
            if(selected.contains(c0)) {
                CMatchUI.SINGLETON_INSTANCE.setUsedToPay(c0, false);
                selected.remove(c0);
            } else { 
                CMatchUI.SINGLETON_INSTANCE.setUsedToPay(c0, true);
                selected.add(c0);
            }
            if( selected.isEmpty())
                ButtonUtil.enableOnlyOk();
            else
                ButtonUtil.enableAllFocusOk();
        }
    }

    public final boolean isKeepHand() {
        return keepHand;
    }

    public List<Card> getSelectedCards() {
        return selected;
    }
}
