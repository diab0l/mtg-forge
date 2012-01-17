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
 * Phase class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class Phase implements java.io.Serializable {

    private static final long serialVersionUID = 4665309652476851977L;

    protected final CommandList at = new CommandList();
    protected final CommandList until = new CommandList();

    protected final HashMap<Player, CommandList> untilMap = new HashMap<Player, CommandList>();

    /**
     * <p>
     * Add a Command that will terminate an effect with "until <Player's> next <phase>".
     * </p>
     * 
     * @param p
     *            a {@link forge.Player} object
     * @param c
     *            a {@link forge.Command} object.
     */
    public final void addUntil(Player p, final Command c) {
        if (null == p) {
            p = AllZone.getPhaseHandler().getPlayerTurn();
        }

        if (this.untilMap.containsKey(p)) {
            this.untilMap.get(p).add(c);
        } else {
            this.untilMap.put(p, new CommandList(c));
        }
    }

    /**
     * <p>
     * Executes the termination of effects that apply "until <Player's> next <phase>".
     * </p>
     * 
     * @param p
     *            the player the execute until for
     */
    public final void executeUntil(final Player p) {
        if (this.untilMap.containsKey(p)) {
            this.execute(this.untilMap.get(p));
        }
    }

    /**
     * <p>
     * Add a hardcoded trigger that will execute "at <phase>".
     * </p>
     * 
     * @param c
     *            a {@link forge.Command} object.
     */
    public final void addAt(final Command c) {
        this.at.add(c);
    }

    /**
     * <p>
     * Add a Command that will terminate an effect with "until <phase>".
     * </p>
     * 
     * @param c
     *            a {@link forge.Command} object.
     */
    public final void addUntil(final Command c) {
        this.until.add(c);
    }

    /**
     * <p>
     * Executes any hardcoded triggers that happen "at <phase>".
     * </p>
     */
    public void executeAt() {
        this.execute(this.at);
    }

    /**
     * <p>
     * Executes the termination of effects that apply "until <phase>".
     * </p>
     */
    public final void executeUntil() {
        this.execute(this.until);
    }

    /**
     * <p>
     * execute.
     * </p>
     * 
     * @param c
     *            a {@link forge.CommandList} object.
     */
    protected void execute(final CommandList c) {
        final int length = c.size();

        for (int i = 0; i < length; i++) {
            c.remove(0).execute();
        }
    }

} //end class Phase
