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

import forge.Card;
import forge.FThreads;
import forge.game.GameState;
import forge.game.GameType;
import forge.game.MatchController;
import forge.game.player.Player;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiDialog;
import forge.gui.framework.SDisplayUtil;
import forge.gui.match.CMatchUI;
import forge.gui.match.nonsingleton.VField;
import forge.view.ButtonUtil;
 /**
  * <p>
  * InputMulligan class.
  * </p>
  * 
  * @author Forge
  * @version $Id: InputMulligan.java 20698 2013-04-01 09:56:12Z Max mtg $
  */
public class InputPartialParisMulligan extends InputBase {
    /** Constant <code>serialVersionUID=-8112954303001155622L</code>. */
    private static final long serialVersionUID = -8112954303001155622L;
    
    private final MatchController match;
    
    private final List<Card> lastExiled = new ArrayList<Card>();
    private final List<Card> allExiled = new ArrayList<Card>();
    
    public InputPartialParisMulligan(MatchController match0, Player humanPlayer) {
        super(humanPlayer);
        match = match0;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        ButtonUtil.setButtonText("Done", "Exile");
        ButtonUtil.enableOnlyOk();

        GameState game = match.getCurrentGame();
        Player startingPlayer = game.getPhaseHandler().getPlayerTurn();

        StringBuilder sb = new StringBuilder();
        sb.append(startingPlayer.getName()).append(" is going first. ");

        if (!startingPlayer.equals(player)) {
            sb.append("You are going ").append(game.getOrdinalPosition(player, startingPlayer)).append(". ");
        }

        sb.append("Do you want to Mulligan?");
        showMessage(sb.toString());
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonOK() {
        this.end();
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonCancel() {
        for(Card c : lastExiled)
        {
            match.getCurrentGame().action.exile(c);
        }
        
        player.drawCards(lastExiled.size()-1);
        allExiled.addAll(lastExiled);
        lastExiled.clear();

        if (player.getCardsIn(ZoneType.Hand).isEmpty()) {
            this.end();
        } else {
            ButtonUtil.enableAllFocusOk();
        }
    }

    final void end() {        
        
        final GameState game = match.getCurrentGame();

        for(Card c : allExiled)
        {
            game.action.moveToLibrary(c);
        }
        player.shuffle();
        
        // Computer mulligan
        //TODO: How should AI approach Partial Paris?
        /*        
        for (Player p : game.getPlayers()) {
            if (!(p instanceof AIPlayer)) {
                continue;
            }
            AIPlayer ai = (AIPlayer) p;
            while (ComputerUtil.wantMulligan(ai)) {
                ai.doMulligan();
            }
        }*/

        // Human Leylines & Chancellors
        ButtonUtil.reset();
        Player next = game.getPhaseHandler().getPlayerTurn();
        if(game.getType() == GameType.Planechase)
        {
            next.initPlane();
        }
        //Set Field shown to current player.        
        VField nextField = CMatchUI.SINGLETON_INSTANCE.getFieldViewFor(next);
        SDisplayUtil.showTab(nextField);
        
        FThreads.invokeInNewThread( new Runnable() {
            @Override
            public void run() {
                match.afterMulligans();
            }
        });
    }

    @Override
    public void selectCard(Card c0) {
        if(lastExiled.contains(c0))
        {
            lastExiled.remove(c0);
            c0.setUsedToPay(false);
        }
        else
        {        
            Zone z0 = match.getCurrentGame().getZoneOf(c0);
            if (c0.getName().equals("Serum Powder") && z0.is(ZoneType.Hand)) {
                if (GuiDialog.confirm(c0, "Use " + c0.getName() + "'s ability?")) {
                    List<Card> hand = new ArrayList<Card>(c0.getController().getCardsIn(ZoneType.Hand));
                    for (Card c : hand) {
                        match.getCurrentGame().getAction().exile(c);
                    }
                    c0.getController().drawCards(hand.size());
                }
                else
                {
                    lastExiled.add(c0);
                    c0.setUsedToPay(true);
                }
            } else {
                lastExiled.add(c0);
                c0.setUsedToPay(true);
            }
        }
        
        if(lastExiled.size() > 0)
            ButtonUtil.enableAllFocusOk();
        else
            ButtonUtil.enableOnlyOk();
    }
}
