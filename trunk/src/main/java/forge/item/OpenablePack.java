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
package forge.item;

import java.util.Arrays;
import java.util.List;

import net.slightlymagic.braids.util.UtilFunctions;
import forge.Singletons;
import forge.card.BoosterData;
import forge.card.BoosterGenerator;
import forge.card.CardEdition;
import forge.card.CardRules;
import forge.util.Predicate;

/**
 * TODO: Write javadoc for this type.
 */
public abstract class OpenablePack implements InventoryItemFromSet {
    
    /** The contents. */
    protected final BoosterData contents;
    
    /** The name. */
    protected final String name;
    private List<CardPrinted> cards = null;

    private BoosterGenerator generator = null;

    /**
     * Instantiates a new openable pack.
     *
     * @param name0 the name0
     * @param boosterData the booster data
     */
    public OpenablePack(final String name0, final BoosterData boosterData) {
        this.contents = boosterData;
        this.name = name0;
    }

    /* (non-Javadoc)
     * @see forge.item.InventoryItemFromSet#getName()
     */
    @Override
    public final String getName() {
        return this.name + " " + this.getType();
    }

    /* (non-Javadoc)
     * @see forge.item.InventoryItemFromSet#getEdition()
     */
    @Override
    public final String getEdition() {
        return this.contents.getEdition();
    }

    /**
     * Gets the cards.
     * 
     * @return the cards
     */
    public final List<CardPrinted> getCards() {
        if (null == this.cards) {
            this.cards = this.generate();
        }
        return this.cards;
    }

    /**
     * Gets the total cards.
     *
     * @return the total cards
     */
    public int getTotalCards() {
        return this.contents.getTotal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final OpenablePack other = (OpenablePack) obj;
        if (this.contents == null) {
            if (other.contents != null) {
                return false;
            }
        } else if (!this.contents.equals(other.contents)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    /**
     * Hash code.
     * 
     * @return int
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.contents == null) ? 0 : this.contents.hashCode());
        return result;
    }

    /**
     * Generate.
     *
     * @return the list
     */
    protected List<CardPrinted> generate() {
        if (null == this.generator) {
            this.generator = new BoosterGenerator(this.contents.getEditionFilter());
        }
        final List<CardPrinted> myCards = this.generator.getBoosterPack(this.contents);

        final int cntLands = this.contents.getLand();
        if (cntLands > 0) {
            myCards.add(this.getLandFromNearestSet());
        }
        return myCards;
    }

    private CardPrinted getLandFromNearestSet() {
        final CardEdition[] editions = UtilFunctions.iteratorToArray(Singletons.getModel().getEditions().iterator(),
                new CardEdition[] {});
        final int iThisSet = Arrays.binarySearch(editions, this.contents);
        for (int iSet = iThisSet; iSet < editions.length; iSet++) {
            final CardPrinted land = this.getRandomBasicLand(editions[iSet]);
            if (null != land) {
                return land;
            }
        }
        // if not found (though that's impossible)
        return this.getRandomBasicLand(Singletons.getModel().getEditions().get("M12"));
    }

    /**
     * Gets the random basic land.
     *
     * @param set the set
     * @return the random basic land
     */
    protected CardPrinted getRandomBasicLand(final CardEdition set) {
        return this.getRandomBasicLands(set, 1).get(0);
    }

    /**
     * Gets the random basic lands.
     *
     * @param set the set
     * @param count the count
     * @return the random basic lands
     */
    protected List<CardPrinted> getRandomBasicLands(final CardEdition set, final int count) {
        return Predicate.and(CardPrinted.Predicates.printedInSets(set.getCode()),
                CardRules.Predicates.Presets.IS_BASIC_LAND, CardPrinted.FN_GET_RULES).random(
                CardDb.instance().getAllCards(), count);
    }

}
