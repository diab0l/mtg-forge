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

import forge.Card;
import forge.FThreads;
import forge.Singletons;
import forge.game.player.Player;
import forge.gui.match.CMatchUI;

/**
 * <p>
 * Abstract Input class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class InputBase implements java.io.Serializable, Input {
    /** Constant <code>serialVersionUID=-6539552513871194081L</code>. */
    private static final long serialVersionUID = -6539552513871194081L;
    protected final Player player;  
    public InputBase(Player player) {
        this.player = player;
    }
    
    // showMessage() is always the first method called
    @Override
    public abstract void showMessage();

    @Override
    public void selectCard(final Card c) {   }
    @Override
    public void selectPlayer(final Player player) {    }
    @Override
    public void selectButtonOK() {    }
    @Override
    public void selectButtonCancel() {    }

    // to remove need for CMatchUI dependence
    protected void showMessage(String message) { 
        CMatchUI.SINGLETON_INSTANCE.showMessage(message);
    }
    
    // Removes this input from the stack and releases any latches (in synchronous imports)
    protected final void stop() {
        // clears a "temp" Input like Input_PayManaCost if there is one
        Singletons.getModel().getMatch().getInput().removeInput(this);
        afterStop(); // sync inputs will release their latch there
    }

    protected final boolean isActive() {
        return Singletons.getModel().getMatch().getInput().getInput() == this;
    }
    
    protected void afterStop() { }
    
    
    
    protected void passPriority() {
        final Runnable pass = new Runnable() {
            @Override public void run() {
                player.getController().passPriority();
            }
        };
        if( FThreads.isEDT() )
            FThreads.invokeInNewThread(pass, true);
        else 
            pass.run();
    }
}