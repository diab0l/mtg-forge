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
package forge.screens.match.input;

import java.util.List;

import forge.game.Game;
import forge.game.card.Card;
import forge.game.phase.PhaseHandler;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.screens.match.FControl;
import forge.toolbox.VCardZoom.ZoomController;

/**
 * <p>
 * Abstract Input class.
 * </p>
 * 
 * @author Forge
 * @version $Id: InputBase.java 24769 2014-02-09 13:56:04Z Hellfish $
 */
public abstract class InputBase implements java.io.Serializable, Input {
    /** Constant <code>serialVersionUID=-6539552513871194081L</code>. */
    private static final long serialVersionUID = -6539552513871194081L;
    private boolean finished = false;
    protected final boolean isFinished() { return finished; }
    protected final void setFinished() { finished = true; }

    // showMessage() is always the first method called
    @Override
    public final void showMessageInitial() {
        finished = false;
        showMessage();
    }

    protected abstract void showMessage();

    @Override
    public final void selectPlayer(final Player player) {
        if (isFinished()) { return; }
        onPlayerSelected(player);
    }

    @Override
    public void selectAbility(SpellAbility ab) { }

    @Override
    public final void selectButtonCancel() {
        if (isFinished()) { return; }
        onCancel();
    }

    @Override
    public final void selectButtonOK() {
        if (isFinished()) { return; }
        onOk();
    }

    @Override
    public final void selectCard(final Card card, final List<Card> orderedCardOptions) {
        if (isFinished()) { return; }
        onCardSelected(card, orderedCardOptions);
    }

    protected void onCardSelected(final Card card, final List<Card> orderedCardOptions) {
        FControl.getView().getCardZoom().show("", card, orderedCardOptions, new ZoomController<SpellAbility>() {
            @Override
            public List<SpellAbility> getOptions(Card card) {
                return null;
            }

            @Override
            public boolean selectOption(Card card, SpellAbility option) {
                return true;
            }
        });
    }

    protected void onPlayerSelected(final Player p) {}
    protected void onCancel() {}
    protected void onOk() {}

    // to remove need for CMatchUI dependence
    protected final void showMessage(String message) {
        FControl.showMessage(message);
    }

    protected final void flashIncorrectAction() {
        FControl.getView().getPrompt().remind();
    }

    protected String getTurnPhasePriorityMessage(Game game) {
        final PhaseHandler ph = game.getPhaseHandler();
        final StringBuilder sb = new StringBuilder();

        sb.append("Priority: ").append(ph.getPriorityPlayer()).append("\n");
        sb.append("Turn ").append(ph.getTurn()).append(" (").append(ph.getPlayerTurn()).append(")\n");
        sb.append("Phase: ").append(ph.getPhase().nameForUi).append("\n");
        sb.append("Stack: ");
        if (!game.getStack().isEmpty()) {
            sb.append(game.getStack().size()).append(" to Resolve.");
        } else {
            sb.append("Empty");
        }
        sb.append("\n");
        return sb.toString();
    }
}
