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
import java.util.List;

import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.view.ButtonUtil;

/**
 * <p>
 * Input_PassPriority class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class InputPassPriority extends InputSyncronizedBase {
    /** Constant <code>serialVersionUID=-581477682214137181L</code>. */
    private static final long serialVersionUID = -581477682214137181L;
    private final Player player;
    
    private SpellAbility chosenSa;
    
    public InputPassPriority(Player human) {
        player = human;
    }

    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        showMessage(getTurnPhasePriorityMessage(player.getGame()));
        chosenSa = null;
        ButtonUtil.enableOnlyOk();
    }

    /** {@inheritDoc} */
    @Override
    protected final void onOk() {
        stop();
    }
    
    public SpellAbility getChosenSa() { return chosenSa; }


    @Override
    protected void onCardSelected(final Card card, final MouseEvent triggerEvent) {
    	List<SpellAbility> abilities = card.getAllPossibleAbilities(player, false);
    	if (abilities.isEmpty()) {
            flashIncorrectAction();
            return;
    	}

    	selectAbility(player.getController().getAbilityToPlay(abilities, triggerEvent));
    }
    
    @Override
    public void selectAbility(final SpellAbility ab) {
    	if (ab != null) {
            chosenSa = ab;
            stop();
        }
    }
}
