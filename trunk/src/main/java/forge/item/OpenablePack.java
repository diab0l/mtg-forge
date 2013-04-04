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

import java.util.List;

import org.apache.commons.lang.NullArgumentException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.card.BoosterData;
import forge.card.BoosterGenerator;
import forge.card.CardRulesPredicates;
import forge.util.Aggregates;

public abstract class OpenablePack implements InventoryItemFromSet {
    protected final BoosterData contents;
    protected final String name;
    private final int hash;
    private List<CardPrinted> cards = null;
    private BoosterGenerator generator = null;

    public OpenablePack(String name0, BoosterData boosterData) {
        if (null == name0)       { throw new NullArgumentException("name0");       }
        if (null == boosterData) { throw new NullArgumentException("boosterData"); }
        contents = boosterData;
        name = name0;
        hash = name.hashCode() ^ getClass().hashCode() ^ contents.hashCode();
    }

    @Override
    public final String getName() {
        return name + " " + getItemType();
    }

    public String getDescription() {
        return contents.toString();
    }
    
    @Override
    public final String getEdition() {
        return contents.getEdition();
    }
    
    public final List<CardPrinted> getCards() {
        if (null == cards) {
            cards = generate();
        }
        
        return cards;
    }
    
    public int getTotalCards() {
        return contents.getTotal();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        OpenablePack other = (OpenablePack)obj;
        return name.equals(other.name) && contents.equals(other.contents);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    protected List<CardPrinted> generate() {
        if (null == generator) {
            generator = new BoosterGenerator(contents.getEditionFilter());
        }
        final List<CardPrinted> myCards = generator.getBoosterPack(contents);

        final int cntLands = contents.getCntLands();
        if (cntLands > 0) {
            myCards.add(getRandomBasicLand(contents.getLandEdition()));
        }
        return myCards;
    }

    protected CardPrinted getRandomBasicLand(final String setCode) {
        return this.getRandomBasicLands(setCode, 1).get(0);
    }

    protected List<CardPrinted> getRandomBasicLands(final String setCode, final int count) {
        Predicate<CardPrinted> cardsRule = Predicates.and(
                IPaperCard.Predicates.printedInSets(setCode),
                Predicates.compose(CardRulesPredicates.Presets.IS_BASIC_LAND, CardPrinted.FN_GET_RULES));
        return Aggregates.random(Iterables.filter(CardDb.instance().getAllCards(), cardsRule), count);
    }
}
