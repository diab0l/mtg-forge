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
package forge.card;

import java.util.ArrayList;
import java.util.List;

import forge.item.CardPrinted;
import forge.util.StorageReaderFile;
import forge.util.closures.Lambda1;
import forge.util.closures.Predicate;

/**
 * This is a CardBlock class.
 */
public final class CardBlock implements Comparable<CardBlock> {
    private static final CardEdition[] EMPTY_SET_ARRAY = new CardEdition[] {};

    private final int orderNum;
    private final String name;
    private final CardEdition[] sets;
    private final CardEdition landSet;
    private final int cntBoostersDraft;
    private final int cntBoostersSealed;
    private Predicate<CardPrinted> filter = null;

    /**
     * Instantiates a new card block.
     * 
     * @param index
     *            the index
     * @param name
     *            the name
     * @param sets
     *            the sets
     * @param landSet
     *            the land set
     * @param cntBoostersDraft
     *            the cnt boosters draft
     * @param cntBoostersSealed
     *            the cnt boosters sealed
     */
    public CardBlock(final int index, final String name, final List<CardEdition> sets, final CardEdition landSet,
            final int cntBoostersDraft, final int cntBoostersSealed) {
        this.orderNum = index;
        this.name = name;
        this.sets = sets.toArray(CardBlock.EMPTY_SET_ARRAY);
        this.landSet = landSet;
        this.cntBoostersDraft = cntBoostersDraft;
        this.cntBoostersSealed = cntBoostersSealed;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the sets.
     * 
     * @return the sets
     */
    public CardEdition[] getSets() {
        return this.sets;
    }

    /**
     * Gets the land set.
     * 
     * @return the land set
     */
    public CardEdition getLandSet() {
        return this.landSet;
    }

    /**
     * Gets the cnt boosters draft.
     * 
     * @return the cnt boosters draft
     */
    public int getCntBoostersDraft() {
        return this.cntBoostersDraft;
    }

    /**
     * Gets the cnt boosters sealed.
     * 
     * @return the cnt boosters sealed
     */
    public int getCntBoostersSealed() {
        return this.cntBoostersSealed;
    }

    /**
     * Gets the filter.
     * 
     * @return the filter
     */
    public Predicate<CardPrinted> getFilter() {
        if (this.filter == null) {
            this.filter = this.buildFilter();
        }
        return this.filter;
    }

    private Predicate<CardPrinted> buildFilter() {
        final List<String> setCodes = new ArrayList<String>();
        for (final CardEdition set : this.sets) {
            setCodes.add(set.getCode());
        }
        return CardPrinted.Predicates.printedInSets(setCodes, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.landSet == null) ? 0 : this.landSet.hashCode());
        result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final CardBlock other = (CardBlock) obj;
        if (!this.landSet.equals(other.landSet)) {
            return false;
        }
        if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final CardBlock o) {
        return this.orderNum - o.orderNum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name + " (block)";
    }

    public static final Lambda1<String, CardBlock> FN_GET_NAME = new Lambda1<String, CardBlock>() {

        @Override
        public String apply(CardBlock arg1) {
            return arg1.getName();
        }
    };

    public static class Reader extends StorageReaderFile<CardBlock> {

        private final EditionCollection editions;
        /**
         * TODO: Write javadoc for Constructor.
         * @param pathname
         * @param keySelector0
         */
        public Reader(String pathname, EditionCollection editions0) {
            super(pathname, CardBlock.FN_GET_NAME);
            editions = editions0;
        }

        /* (non-Javadoc)
         * @see forge.util.StorageReaderFile#read(java.lang.String)
         */
        @Override
        protected CardBlock read(String line) {
            final String[] sParts = line.trim().split("\\|");

            String name = null;
            int index = -1;
            final List<CardEdition> sets = new ArrayList<CardEdition>(4);
            CardEdition landSet = null;
            int draftBoosters = 3;
            int sealedBoosters = 6;

            for (final String sPart : sParts) {
                final String[] kv = sPart.split(":", 2);
                final String key = kv[0].toLowerCase();
                if ("name".equals(key)) {
                    name = kv[1];
                } else if ("index".equals(key)) {
                    index = Integer.parseInt(kv[1]);
                } else if ("set0".equals(key) || "set1".equals(key) || "set2".equals(key)) {
                    sets.add(editions.getEditionByCodeOrThrow(kv[1]));
                } else if ("landsetcode".equals(key)) {
                    landSet = editions.getEditionByCodeOrThrow(kv[1]);
                } else if ("draftpacks".equals(key)) {
                    draftBoosters = Integer.parseInt(kv[1]);
                } else if ("sealedpacks".equals(key)) {
                    sealedBoosters = Integer.parseInt(kv[1]);
                }

            }
            return new CardBlock(index, name, sets, landSet, draftBoosters, sealedBoosters);
        }

    }
}
