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
package forge.card.spellability;

import com.esotericsoftware.minlog.Log;

import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.game.Game;
import forge.game.card.Card;

/**
 * <p>
 * Abstract Ability class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class Ability extends SpellAbility {

    /**
     * <p>
     * Constructor for Ability.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.game.card.Card} object.
     * @param manaCost
     *            a {@link java.lang.String} object.
     */
    public Ability(final Card sourceCard, final ManaCost manaCost) {
        this(sourceCard, new Cost(manaCost, true));
    }
    public Ability(final Card sourceCard, final Cost cost) {
        super(sourceCard, cost);
    }
    /**
     * <p>
     * Constructor for Ability.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.game.card.Card} object.
     * @param manaCost
     *            a {@link java.lang.String} object.
     * @param stackDescription
     *            a {@link java.lang.String} object.
     */
    public Ability(final Card sourceCard, final ManaCost manaCost, final String stackDescription) {
        this(sourceCard, manaCost);
        this.setStackDescription(stackDescription);
        Log.debug("an ability is being played from" + sourceCard.getName());
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPlay() {
        final Game game = getActivatingPlayer().getGame();
        if (game.getStack().isSplitSecondOnStack() && !this.isManaAbility()) {
            return false;
        }

        return this.getSourceCard().isInPlay() && !this.getSourceCard().isFaceDown();
    }
    
    public static final Ability PLAY_LAND_SURROGATE = new Ability(null, (Cost)null){
    	@Override
        public boolean canPlay() {
    		return true; //if this ability is added anywhere, it can be assumed that land can be played
        }
        @Override
        public void resolve() {
            throw new RuntimeException("This ability is intended to indicate \"land to play\" choice only");
        }
        @Override
        public String toUnsuppressedString() { return "Play land"; }
    }; 
}
