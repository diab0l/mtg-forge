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
package forge;

import java.util.HashMap;

/**
 * <p>
 * Untap class.
 * Handles "until next untap", "until your next untap" and "at beginning of untap"
 * commands from cards.
 * </p>
 * 
 * @author Forge
 * @version $Id: Untap 12482 2011-12-06 11:14:11Z Sloth $
 */
public class Untap implements java.io.Serializable {

    private static final long serialVersionUID = 4515266331266259123L;
    private final HashMap<Player, CommandList> until = new HashMap<Player, CommandList>();

    /**
     * <p>
     * Add a Command that will terminate an effect with "until <Player's> next untap".
     * </p>
     * 
     * @param p
     *            a {@link forge.Player} object
     * @param c
     *            a {@link forge.Command} object.
     */
    public final void addUntil(Player p, final Command c) {
        if (null == p) {
            p = AllZone.getPhase().getPlayerTurn();
        }

        if (this.until.containsKey(p)) {
            this.until.get(p).add(c);
        } else {
            this.until.put(p, new CommandList(c));
        }
    }

    /**
     * <p>
     * Executes the termination of effects that apply "until <Player's> next untap".
     * </p>
     * 
     * @param p
     *            the player the execute until for
     */
    public final void executeUntil(final Player p) {
        if (this.until.containsKey(p)) {
            this.execute(this.until.get(p));
        }
    }

    private void execute(final CommandList c) {
        final int length = c.size();

        for (int i = 0; i < length; i++) {
            c.remove(0).execute();
        }
    }

    /**
     * <p>
     * Handles all the hardcoded events that happen at the beginning of each Untap Phase.
     * 
     * This will freeze the Stack at the start, and unfreeze the Stack at the end.
     * </p>
     */
    public final void executeAt() {
        AllZone.getStack().freezeStack();

        AllZone.getStack().unfreezeStack();
    }

} //end class Untap
