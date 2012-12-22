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
package forge.deck;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import forge.card.CardCoreType;
import forge.deck.io.DeckFileHeader;
import forge.deck.io.DeckSerializer;
import forge.gui.deckeditor.tables.TableSorter;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.ItemPoolView;
import forge.game.GameType;
import forge.game.limited.ReadDraftRankings;
import forge.util.Aggregates;
import forge.util.FileSection;
import forge.util.FileUtil;
import java.util.Arrays;


/**
 * <p>
 * Deck class.
 * </p>
 * 
 * The set of MTG legal cards that become player's library when the game starts.
 * Any other data is not part of a deck and should be stored elsewhere. Current
 * fields allowed for deck metadata are Name, Title, Description and Deck Type.
 * 
 * @author Forge
 * @version $Id$
 */
public class Deck extends DeckBase {
    /**
     *
     */
    private static final long serialVersionUID = -7478025567887481994L;

    private transient boolean isEdited = false;
    private final DeckSection main;
    private final DeckSection sideboard;
    private transient DeckSection mainEdited;
    private transient DeckSection sideboardEdited;
    private CardPrinted avatar;
    private CardPrinted commander;
    private final DeckSection planes;
    private final DeckSection schemes;

    // gameType is from Constant.GameType, like GameType.Regular
    /**
     * <p>
     * Decks have their named finalled.
     * </p>
     */
    public Deck() {
        this("");
    }

    /**
     * Instantiates a new deck.
     *
     * @param name0 the name0
     */
    public Deck(final String name0) {
        super(name0);
        this.main = new DeckSection();
        this.sideboard = new DeckSection();
        this.mainEdited = new DeckSection();
        this.sideboardEdited = new DeckSection();
        this.avatar = null;
        this.commander = null;
        this.planes = new DeckSection();
        this.schemes = new DeckSection();
    }

    /**
     * <p>
     * hashCode.
     * </p>
     * 
     * @return a int.
     */
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * <p>
     * Getter for the field <code>main</code>.
     * </p>
     * 
     * @return a {@link java.util.List} object.
     */
    public DeckSection getMain() {
        return isEdited ? this.mainEdited : this.main;
    }

    /**
     * <p>
     * Getter for the field <code>sideboard</code>.
     * </p>
     * 
     * @return a {@link java.util.List} object.
     */
    public DeckSection getSideboard() {
        return isEdited ? this.sideboardEdited : this.sideboard;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.item.CardCollectionBase#getCardPool()
     */
    @Override
    public ItemPoolView<CardPrinted> getCardPool() {
        return isEdited ? this.mainEdited : this.main;
    }

    /* (non-Javadoc)
     * @see forge.deck.DeckBase#cloneFieldsTo(forge.deck.DeckBase)
     */
    @Override
    protected void cloneFieldsTo(final DeckBase clone) {
        super.cloneFieldsTo(clone);
        final Deck result = (Deck) clone;
        result.main.addAll(this.main);
        result.sideboard.addAll(this.sideboard);
        result.avatar = this.avatar;
        result.commander = this.commander;

        //This if clause is really only necessary when cloning decks that were
        //around before schemes.
        if (this.schemes != null) {
            result.schemes.addAll(this.schemes);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.deck.DeckBase#newInstance(java.lang.String)
     */
    @Override
    protected DeckBase newInstance(final String name0) {
        return new Deck(name0);
    }

    /**
     * From file.
     *
     * @param deckFile the deck file
     * @return the deck
     */
    public static Deck fromFile(final File deckFile) {
        return Deck.fromSections(FileSection.parseSections(FileUtil.readFile(deckFile)));
    }

    /**
     * From sections.
     *
     * @param sections the sections
     * @return the deck
     */
    public static Deck fromSections(final Map<String, List<String>> sections) {
        return Deck.fromSections(sections, false);
    }

    /**
     * From sections.
     *
     * @param sections the sections
     * @param canThrowExtendedErrors the can throw extended errors
     * @return the deck
     */
    public static Deck fromSections(final Map<String, List<String>> sections, final boolean canThrowExtendedErrors) {
        if ((sections == null) || sections.isEmpty()) {
            return null;
        }

        final DeckFileHeader dh = DeckSerializer.readDeckMetadata(sections, canThrowExtendedErrors);
        if (dh == null) {
            return null;
        }

        final Deck d = new Deck(dh.getName());
        d.setComment(dh.getComment());

        d.getMain().set(Deck.readCardList(sections.get("main")));
        d.getSideboard().set(Deck.readCardList(sections.get("sideboard")));
        List<String> cmd = Deck.readCardList(sections.get("commander"));
        String cmdName = cmd.isEmpty() ? null : cmd.get(0);
        d.commander = CardDb.instance().isCardSupported(cmdName) ? CardDb.instance().getCard(cmdName) : null;
        List<String> av = Deck.readCardList(sections.get("avatar"));
        String avName = av.isEmpty() ? null : av.get(0);
        d.avatar = CardDb.instance().isCardSupported(avName) ? CardDb.instance().getCard(avName) : null;
        d.getPlanes().set(Deck.readCardList(sections.get("planes")));
        d.getSchemes().set(Deck.readCardList(sections.get("schemes")));
        return d;
    }

    private static List<String> readCardList(final List<String> lines) {
        final List<String> result = new ArrayList<String>();
        final Pattern p = Pattern.compile("((\\d+)\\s+)?(.*?)");

        if (lines == null) {
            return result;
        }

        final Iterator<String> lineIterator = lines.iterator();
        while (lineIterator.hasNext()) {
            final String line = lineIterator.next();
            if (line.startsWith(";")) { continue; } // that is a comment or not-yet-supported card
            if (line.startsWith("[")) { break; } // there comes another section

            final Matcher m = p.matcher(line.trim());
            m.matches();
            final String sCnt = m.group(2);
            final String cardName = m.group(3);
            if (StringUtils.isBlank(cardName)) {
                continue;
            }

            final int count = sCnt == null ? 1 : Integer.parseInt(sCnt);
            for (int i = 0; i < count; i++) {
                result.add(cardName);
            }
        }
        return result;
    }

    private static List<String> writeCardPool(final ItemPoolView<CardPrinted> pool) {
        final List<Entry<CardPrinted, Integer>> main2sort = pool.getOrderedList();
        Collections.sort(main2sort, TableSorter.BY_NAME_THEN_SET);
        final List<String> out = new ArrayList<String>();
        for (final Entry<CardPrinted, Integer> e : main2sort) {
            out.add(serializeSingleCard(e.getKey(), e.getValue()));
        }
        return out;
    }

    private static String serializeSingleCard(CardPrinted card, Integer n) {

        final boolean hasBadSetInfo = "???".equals(card.getEdition()) || StringUtils.isBlank(card.getEdition());
        if (hasBadSetInfo) {
            return String.format("%d %s", n, card.getName());
        } else {
            return String.format("%d %s|%s", n, card.getName(), card.getEdition());
        }
    }

    /**
     * <p>
     * writeDeck.
     * </p>
     *
     * @return the list
     */
    public List<String> save() {

        final List<String> out = new ArrayList<String>();
        out.add(String.format("[metadata]"));

        out.add(String.format("%s=%s", DeckFileHeader.NAME, this.getName().replaceAll("\n", "")));
        // these are optional
        if (this.getComment() != null) {
            out.add(String.format("%s=%s", DeckFileHeader.COMMENT, this.getComment().replaceAll("\n", "")));
        }

        out.add(String.format("%s", "[main]"));
        out.addAll(Deck.writeCardPool(this.getMain()));

        out.add(String.format("%s", "[sideboard]"));
        out.addAll(Deck.writeCardPool(this.getSideboard()));

        if (getCommander() != null) {
            out.add(String.format("%s", "[commander]"));
            out.add(Deck.serializeSingleCard(getCommander(), 1));
        }
        if (getAvatar() != null) {
            out.add(String.format("%s", "[avatar]"));
            out.add(Deck.serializeSingleCard(getAvatar(), 1));
        }

        out.add(String.format("%s", "[planes]"));
        out.addAll(Deck.writeCardPool(this.getPlanes()));

        out.add(String.format("%s", "[schemes]"));
        out.addAll(Deck.writeCardPool(this.getSchemes()));
        return out;
    }

    /**
     * <p>
     * getDraftValue.
     * </p>
     *
     * @return the combined draft values of cards in the main deck
     */
    public double getDraftValue() {

        double value = 0;
        double divider = 0;

        ReadDraftRankings ranker = new ReadDraftRankings();

        if (this.getMain().isEmpty()) {
            return 0;
        }

        double best = 1.0;

        for (Entry<CardPrinted, Integer> kv : this.getMain()) {
            CardPrinted evalCard = kv.getKey();
            int count = kv.getValue();
            if (ranker.getRanking(evalCard.getName(), evalCard.getEdition()) != null) {
                double add = ranker.getRanking(evalCard.getName(), evalCard.getEdition());
                // System.out.println(evalCard.getName() + " is worth " + add);
                value += add * count;
                divider += count;
                if (best > add) {
                    best = add;
                }
            }
        }

        if (divider == 0 || value == 0) {
            return 0;
        }

        value /= divider;

        return (20.0 / (best + (2 * value)));
    }

    public String meetsGameTypeRequirements(GameType type) {
        return getGameTypeConformanceMessage(type);
    }

    public String getGameTypeConformanceMessage(GameType type) {
        int deckSize = getMain().countAll();

        int min = type.getMainRange().getMinimumInteger();
        int max = type.getMainRange().getMaximumInteger();

        if (deckSize < min) {
            return String.format("should have a minimum of %d cards", min);
        }

        if (deckSize > max) {
            return String.format("should not exceed a maximum of %d cards", max);
        }
        
        if (type == GameType.Commander) { //Must contain exactly 1 legendary Commander and no sideboard.

            //TODO:Enforce color identity
            if (null == getCommander()) {
                return "is missing a commander";
            }
            if (!getCommander().getCard().getType().isLegendary()) {
                return "has a commander that is not a legendary creature";
            }

            //No sideboarding in Commander
            if (!getSideboard().isEmpty()) {
                return "has sideboard";
            }
        }
        else if (type == GameType.Planechase) { //Must contain at least 10 planes/phenomenons, but max 2 phenomenons. Singleton.

            if (getPlanes().countAll() < 10) {
                return "should gave at least 10 planes";
            }
            int phenoms = 0;
            for (Entry<CardPrinted, Integer> cp : getPlanes()) {

                if (cp.getKey().getCard().getType().typeContains(CardCoreType.Phenomenon)) {
                    phenoms++;
                }
                if(cp.getValue() > 1)
                   return "must not contain multiple copies of any Phenomena";

            }
            if (phenoms > 2) {
                return "must not contain more than 2 Phenomena";
            }
        }
        else if (type == GameType.Archenemy)  { //Must contain at least 20 schemes, max 2 of each.

            if (getSchemes().countAll() < 20) {
                return "must contain at least 20 schemes";
            }

            for (Entry<CardPrinted, Integer> cp : getSchemes()) {
                if (cp.getValue() > 2) {
                    return "must not contain more than 2 copies of any Scheme";
                }
            }
        }
        
        int maxCopies = type.getMaxCardCopies();
        if ( maxCopies < Integer.MAX_VALUE )  {
            //Must contain no more than 4 of the same card
            //shared among the main deck and sideboard, except
            //basic lands and Relentless Rats

            DeckSection tmp = new DeckSection(this.getMain());
            tmp.addAll(this.getSideboard());
            if ( null != this.getCommander())
                tmp.add(this.getCommander());

            List<String> limitExceptions = Arrays.asList("Relentless Rats");

            // should group all cards by name, so that different editions of same card are really counted as the same card
            for (Entry<String, Integer> cp : Aggregates.groupSumBy(tmp, CardPrinted.FN_GET_NAME)) {
                
                CardPrinted simpleCard = CardDb.instance().getCard(cp.getKey());
                boolean canHaveMultiple = simpleCard.getCard().getType().isBasicLand() || limitExceptions.contains(cp.getKey());
                
                if (!canHaveMultiple && cp.getValue() > maxCopies) {
                    return String.format("must not contain more than %d of '%s' card", maxCopies, cp.getKey());
                }
            }

            // The sideboard must contain either 0 or 15 cards
            int sideboardSize = this.getSideboard().countAll();
            IntRange sbRange = type.getSideRange();  
            if ( sbRange != null && sideboardSize > 0 && !sbRange.containsInteger(sideboardSize))
                return sbRange.getMinimumInteger() == sbRange.getMaximumInteger() ? String.format("must have a sideboard of %d cards or no sideboard at all", sbRange.getMaximumInteger()) : String.format("must have a sideboard of %d to %d cards or no sideboard at all", sbRange.getMinimumInteger(), sbRange.getMaximumInteger());

        }

        return null;
    }

    /**
     * @return the commander
     */
    public CardPrinted getCommander() {
        return commander;
    }

    /**
     * @return the planes
     */
    public DeckSection getPlanes() {
        return planes;
    }

    /**
     * @return the schemes
     */
    public DeckSection getSchemes() {
        return schemes;
    }

    /**
     * @return the avatar
     */
    public CardPrinted getAvatar() {
        return avatar;
    }

    public static final Function<Deck, String> FN_NAME_SELECTOR = new Function<Deck, String>() {
        @Override
        public String apply(Deck arg1) {
            return arg1.getName();
        }
    };

    public void clearDeckEdits() {
        isEdited = false;
        if (mainEdited != null) {
            mainEdited.clear();
        } else {
            mainEdited = new DeckSection();
        }
        if (sideboardEdited != null) {
            sideboardEdited.clear();
        } else {
            sideboardEdited = new DeckSection();
        }
    }

    public void startDeckEdits() {
        isEdited = true;
        if (mainEdited.countAll() == 0) {
            mainEdited.add(main.toFlatList());
        }
        if (sideboardEdited.countAll() == 0) {
            sideboardEdited.add(sideboard.toFlatList());
        }
    }

    public DeckSection getOriginalMain() {
        return this.main;
    }

    public DeckSection getOriginalSideboard() {
        return this.sideboard;
    }

    public boolean isEditedDeck() {
        return this.isEdited;
    }
}
