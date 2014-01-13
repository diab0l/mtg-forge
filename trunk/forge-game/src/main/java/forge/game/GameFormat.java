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
package forge.game;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import forge.StaticData;
import forge.card.CardEdition;
import forge.item.PaperCard;
import forge.item.IPaperCard;
import forge.util.FileSection;
import forge.util.storage.StorageBase;
import forge.util.storage.StorageReaderBase;
import forge.util.storage.StorageReaderFileSections;


/**
 * TODO: Write javadoc for this type.
 * 
 */
public class GameFormat implements Comparable<GameFormat> {
    private final String name;
    // contains allowed sets, when empty allows all sets
    protected final List<String> allowedSetCodes;
    protected final List<String> bannedCardNames;

    protected final transient List<String> allowedSetCodes_ro;
    protected final transient List<String> bannedCardNames_ro;

    protected final transient Predicate<PaperCard> filterRules;
    protected final transient Predicate<PaperCard> filterPrinted;

    private final int index; 
    
    /**
     * Instantiates a new game format.
     * 
     * @param fName
     *            the f name
     * @param sets
     *            the sets
     * @param bannedCards
     *            the banned cards
     */
    public GameFormat(final String fName, final Iterable<String> sets, final List<String> bannedCards) {
        this(fName, sets, bannedCards, 0);
    }
    
    public GameFormat(final String fName, final Iterable<String> sets, final List<String> bannedCards, int compareIdx) {
        this.index = compareIdx;
        this.name = fName;
        this.allowedSetCodes = sets == null ? new ArrayList<String>() : Lists.newArrayList(sets);
        this.bannedCardNames = bannedCards == null ? new ArrayList<String>() : Lists.newArrayList(bannedCards);

        this.allowedSetCodes_ro = Collections.unmodifiableList(allowedSetCodes);
        this.bannedCardNames_ro = Collections.unmodifiableList(bannedCardNames);

        this.filterRules = this.buildFilterRules();
        this.filterPrinted = this.buildFilterPrinted();
    }

    private Predicate<PaperCard> buildFilterPrinted() {
        final Predicate<PaperCard> banNames = Predicates.not(IPaperCard.Predicates.names(this.bannedCardNames));
        if (this.allowedSetCodes == null || this.allowedSetCodes.isEmpty()) {
            return banNames;
        }
        return Predicates.and(banNames, IPaperCard.Predicates.printedInSets(this.allowedSetCodes, true));
    }

    private Predicate<PaperCard> buildFilterRules() {
        final Predicate<PaperCard> banNames = Predicates.not(IPaperCard.Predicates.names(this.bannedCardNames));
        if (this.allowedSetCodes == null || this.allowedSetCodes.isEmpty()) {
            return banNames;
        }
        return Predicates.and(banNames, StaticData.instance().getCommonCards().wasPrintedInSets(this.allowedSetCodes));
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
     * Gets the set list (for GameFormatQuest).
     * 
     * @return list of allowed set codes
     */
    public List<String> getAllowedSetCodes() {
        return this.allowedSetCodes_ro;
    }

    /**
     * Gets the banned cards (for GameFormatQuest).
     * 
     * @return list of banned card names
     */
    public List<String> getBannedCardNames() {
        return this.bannedCardNames_ro;
    }

    /**
     * Gets the filter rules.
     * 
     * @return the filter rules
     */
    public Predicate<PaperCard> getFilterRules() {
        return this.filterRules;
    }

    /**
     * Gets the filter printed.
     * 
     * @return the filter printed
     */
    public Predicate<PaperCard> getFilterPrinted() {
        return this.filterPrinted;
    }

    /**
     * Checks if is sets the legal.
     * 
     * @param setCode
     *            the set code
     * @return true, if is sets the legal
     */
    public boolean isSetLegal(final String setCode) {
        return this.allowedSetCodes.isEmpty() || this.allowedSetCodes.contains(setCode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name + " (format)";
    }

    public static final Function<GameFormat, String> FN_GET_NAME = new Function<GameFormat, String>() {
        @Override
        public String apply(GameFormat arg1) {
            return arg1.getName();
        }
    };

    /* (non-Javadoc)
     * just used for ordering -- comparing the name is sufficient
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(GameFormat other) {
        if (null == other) {
            return 1;
        }
        return index - other.index;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Instantiates a new format utils.
     */
    public static class Reader extends StorageReaderFileSections<GameFormat> {
        public Reader(File file0) {
            super(file0, GameFormat.FN_GET_NAME);
        }

        @Override
        protected GameFormat read(String title, Iterable<String> body, int idx) {
            List<String> sets = null; // default: all sets allowed
            List<String> bannedCards = null; // default: nothing banned

            FileSection section = FileSection.parse(body, ":");
            String strSets = section.get("sets");
            if ( null != strSets ) {
                sets = Arrays.asList(strSets.split(", "));
            }
            String strCars = section.get("banned");
            if ( strCars != null ) {
                bannedCards = Arrays.asList(strCars.split("; "));
            }

            return new GameFormat(title, sets, bannedCards, 1 + idx);
        }
    }

    public static class Collection extends StorageBase<GameFormat> {
        private final Map<String, Predicate<PaperCard>> formatPredicates;

        public Collection(StorageReaderBase<GameFormat> reader) {
            super("Format collections", reader);

            formatPredicates = new HashMap<String, Predicate<PaperCard>>();
            for (Entry<String, GameFormat> format : this.entrySet()) {
                formatPredicates.put(format.getKey(), format.getValue().getFilterRules()); //allow reprints
            }
        }

        public GameFormat getStandard() {
            return this.map.get("Standard");
        }

        public GameFormat getExtended() {
            return this.map.get("Extended");
        }

        public GameFormat getModern() {
            return this.map.get("Modern");
        }

        public GameFormat getFormat(String format) {
            return this.map.get(format);
        }

        public Map<String, Predicate<PaperCard>> getFormatPredicates() {
            return this.formatPredicates;
        }
    }
    
    // declared here because
    public final Predicate<CardEdition> editionLegalPredicate = new Predicate<CardEdition>() {
        @Override
        public boolean apply(final CardEdition subject) {
            return GameFormat.this.isSetLegal(subject.getCode());
        }
    };
}
