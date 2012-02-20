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

import forge.AllZone;
import forge.ButtonUtil;
import forge.Card;
import forge.GuiDisplayUtil;
import forge.Player;
import forge.PlayerZone;
import forge.Singletons;

/**
 * <p>
 * Input_PassPriority class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class InputPassPriority extends Input implements java.io.Serializable {
    /** Constant <code>serialVersionUID=-581477682214137181L</code>. */
    private static final long serialVersionUID = -581477682214137181L;

    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        GuiDisplayUtil.updateGUI();
        ButtonUtil.enableOnlyOK();

        final String phase = AllZone.getPhaseHandler().getPhase();
        final Player player = AllZone.getPhaseHandler().getPriorityPlayer();

        if (player.isComputer()) {
            System.out.println(phase + ": Computer in passpriority");
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("Turn : ").append(AllZone.getPhaseHandler().getPlayerTurn()).append("\n");
        sb.append("Phase: ").append(phase).append("\n");
        sb.append("Stack: ");
        if (AllZone.getStack().size() != 0) {
            sb.append(AllZone.getStack().size()).append(" to Resolve.");
        } else {
            sb.append("Empty");
        }
        sb.append("\n");
        sb.append("Priority: ").append(player);

        Singletons.getControl().getControlMatch().showMessage(sb.toString());
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonOK() {
        AllZone.getPhaseHandler().passPriority();
        GuiDisplayUtil.updateGUI();
        final Input in = AllZone.getInputControl().getInput();
        if ((in == this) || (in == null)) {
            AllZone.getInputControl().resetInput();
            // Clear out PassPriority after clicking button
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void selectCard(final Card card, final PlayerZone zone) {
        if (Singletons.getModel().getGameAction().playCard(card)) {
            AllZone.getPhaseHandler().setPriority(AllZone.getHumanPlayer());
        }
        else {
            Singletons.getControl().getControlMatch().getMessageControl().remind();
        }
    } // selectCard()
}
