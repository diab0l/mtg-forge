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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.Card;
import forge.card.CardInSet;
import forge.card.CardRules;
import forge.util.Aggregates;

public final class CardDb {
    private static volatile CardDb commonCards = null; // 'volatile' keyword makes this working
    private static volatile CardDb variantCards = null; // 'volatile' keyword makes this working
    public final static String foilSuffix = " foil";
    private final static int foilSuffixLength = foilSuffix.length(); 

    public static CardDb instance() {
        if (CardDb.commonCards == null) {
            throw new NullPointerException("CardDb has not yet been initialized, run setup() first");
        }
        return CardDb.commonCards;
    }
    
    public static CardDb variants() {
        if (CardDb.variantCards == null) {
            throw new NullPointerException("CardDb has not yet been initialized, run setup() first");
        }
        return CardDb.variantCards;
    }

    public static void setup(final Iterable<CardRules> list) {
        if (CardDb.commonCards != null) {
            throw new RuntimeException("CardDb has already been initialized, don't do it twice please");
        }
        synchronized (CardDb.class) {
            if (CardDb.commonCards == null) { // It's broken under 1.4 and below, on 1.5+ works again!
                CardSorter cs = new CardSorter(list);
                commonCards = new CardDb(cs.uniqueCommonCards, cs.allCommonCardsFlat, cs.allCommonCardsBySet);
                variantCards = new CardDb(cs.uniqueSpecialCards, cs.allSpecialCardsFlat, cs.allSpecialCardsBySet);
            }
        }
    }

    // Here are refs, get them by name
    private final Map<String, CardPrinted> uniqueCards;

    
    // need this to obtain cardReference by name+set+artindex
    private final Map<String, Map<String, CardPrinted[]>> allCardsBySet;
    // this is the same list in flat storage
    private final List<CardPrinted> allCardsFlat;
    
    // Lambda to get rules for selects from list of printed cards
    /** The Constant fnGetCardPrintedByForgeCard. */
    public static final Function<Card, CardPrinted> FN_GET_CARD_PRINTED_BY_FORGE_CARD = new Function<Card, CardPrinted>() {
        @Override
        public CardPrinted apply(final Card from) {
            return CardDb.instance().getCard(from.getName());
        }
    };

    private CardDb(Map<String, CardPrinted> uniqueCards, List<CardPrinted> cardsFlat, Map<String, Map<String, CardPrinted[]>> cardsBySet) {
        this.uniqueCards = Collections.unmodifiableMap(uniqueCards);
        this.allCardsFlat = Collections.unmodifiableList(cardsFlat);
        this.allCardsBySet = cardsBySet;
    }

    /**
     * Splits cardname into Name and set whenever deck line reads as name|set.
     */
    private static ImmutablePair<String, String> splitCardName(final String name) {
        String cardName = name; // .trim() ?
        final int pipePos = cardName.indexOf('|');

        if (pipePos >= 0) {
            final String setName = cardName.substring(pipePos + 1).trim();
            cardName = cardName.substring(0, pipePos);
            // only if set is not blank try to load it
            if (StringUtils.isNotBlank(setName) && !"???".equals(setName)) {
                return new ImmutablePair<String, String>(cardName, setName);
            }
        }
        return new ImmutablePair<String, String>(cardName, null);
    }

    private boolean isFoil(final String cardName) {
        return cardName.toLowerCase().endsWith(CardDb.foilSuffix) && (cardName.length() > CardDb.foilSuffixLength);
    }

    /**
     * Removes the foil suffix.
     *
     * @param cardName the card name
     * @return the string
     */
    public String removeFoilSuffix(final String cardName) {
        return cardName.substring(0, cardName.length() - CardDb.foilSuffixLength);
    }

    /**
     * Checks if is card supported.
     */
    public CardPrinted tryGetCard(final String cardName0) {
        if (null == cardName0) {
            return null;  // obviously
        }

        final boolean isFoil = this.isFoil(cardName0);
        final String cardName = isFoil ? this.removeFoilSuffix(cardName0) : cardName0;
        final ImmutablePair<String, String> nameWithSet = CardDb.splitCardName(cardName);
        if (nameWithSet.right == null) {
            return this.uniqueCards.get(nameWithSet.left);
        }
        
        CardPrinted res = tryGetCard(nameWithSet.left, nameWithSet.right);
        if ( null != res && isFoil )
            return CardPrinted.makeFoiled(res);
        return res;
            
    }

    public CardPrinted tryGetCard(final String cardName, String setName) {
        // Set exists?
        final Map<String, CardPrinted[]> cardsFromset = this.allCardsBySet.get(setName.toUpperCase());
        if (cardsFromset == null) {
            return null;
        }
        // Card exists?
        final CardPrinted[] cardCopies = cardsFromset.get(cardName.toLowerCase());
        return cardCopies != null ? cardCopies[0] : null;
    }

    // Single fetch
    public CardPrinted getCard(final String name) {
        return this.getCard(name, false);
    }

    // Advanced fetch by name+set
    public CardPrinted getCard(final String name, final String set) {
        return this.getCard(name, set, 0);
    }

    public CardPrinted getCard(final String name, final String set, final int artIndex) {
        // 1. get set
        final Map<String, CardPrinted[]> cardsFromset = this.allCardsBySet.get(set.toUpperCase());
        if (null == cardsFromset) {
            final String err = String
                    .format("Asked for card '%s' from set '%s': that set was not found. :(", name, set);
            throw new NoSuchElementException(err);
        }
        // 2. Find the card itself
        final CardPrinted[] cardCopies = cardsFromset.get(name.toLowerCase());
        if (null == cardCopies) {
            final String err = String.format("Asked for card '%s' from '%s': set found, but the card wasn't. :(", name,
                    set);
            throw new NoSuchElementException(err);
        }
        // 3. Get the proper copy
        if ((artIndex >= 0) && (artIndex <= cardCopies.length)) {
            return cardCopies[artIndex];
        }
        final String err = String
                .format("Asked for '%s' from '%s' #%d: db didn't find that copy.", name, set, artIndex);
        throw new NoSuchElementException(err);
    }

    // Fetch from Forge's Card instance. Well, there should be no errors, but
    // we'll still check
    public static CardPrinted getCard(final Card forgeCard) {
        final String name = forgeCard.getName();
        final String set = forgeCard.getCurSetCode();
        
        if (StringUtils.isNotBlank(set)) {
            CardPrinted cp = variants().tryGetCard(name, set);
            
            return cp == null ? instance().getCard(name, set) : cp;
        }
        CardPrinted cp = variants().tryGetCard(name);
        return cp == null ? instance().getCard(name) : cp;
    }

    // returns a list of all cards from their respective latest editions
    public Collection<CardPrinted> getUniqueCards() {
        return this.uniqueCards.values();
    }

    public List<CardPrinted> getAllCards() {
        return this.allCardsFlat;
    }

    /**  Returns a modifiable list of cards matching the given predicate */
    public List<CardPrinted> getAllCards(Predicate<CardPrinted> predicate) {
        return Lists.newArrayList(Iterables.filter(this.allCardsFlat, predicate));
    }

    public CardPrinted getCard(final String name0, final boolean fromLatestSet) {
        // Sometimes they read from decks things like "CardName|Set" - but we
        // can handle it

        final boolean isFoil = this.isFoil(name0);
        final String name = isFoil ? this.removeFoilSuffix(name0) : name0;
        CardPrinted result = null;

        final ImmutablePair<String, String> nameWithSet = CardDb.splitCardName(name);
        if (nameWithSet.right != null) {
            result = this.getCard(nameWithSet.left, nameWithSet.right);
        } else {
            if (!fromLatestSet) {
                result = this.uniqueCards.get(nameWithSet.left.toLowerCase());
                if (null == result) {
                    throw new NoSuchElementException(String.format("Card '%s' not found in our database.", name));
                }
            } else {
                // OK, plain name here
                final Predicate<CardPrinted> predicate = IPaperCard.Predicates.name(nameWithSet.left);
                final Iterable<CardPrinted> namedCards = Iterables.filter(this.allCardsFlat, predicate);
                // Find card with maximal set index
                result = Aggregates.itemWithMax(namedCards, CardPrinted.FN_GET_EDITION_INDEX);
                if (null == result) {
                    throw new NoSuchElementException(String.format("Card '%s' not found in our database.", name));
                }
            }
        }
        if (isFoil) {
            result = CardPrinted.makeFoiled(result);
        }
        return result;
    }

    private static class CardSorter{
        // need this to obtain cardReference by name+set+artindex
        public final Map<String, Map<String, CardPrinted[]>> allCommonCardsBySet = new TreeMap<String, Map<String, CardPrinted[]>>(String.CASE_INSENSITIVE_ORDER);
        public final Map<String, Map<String, CardPrinted[]>> allSpecialCardsBySet = new TreeMap<String, Map<String, CardPrinted[]>>(String.CASE_INSENSITIVE_ORDER);
        // Here are refs, get them by name
        public final Map<String, CardPrinted> uniqueCommonCards = new TreeMap<String, CardPrinted>(String.CASE_INSENSITIVE_ORDER);
        public final Map<String, CardPrinted> uniqueSpecialCards = new TreeMap<String, CardPrinted>(String.CASE_INSENSITIVE_ORDER);
        // this is the same list in flat storage
        public final List<CardPrinted> allCommonCardsFlat = new ArrayList<CardPrinted>();
        public final List<CardPrinted> allSpecialCardsFlat = new ArrayList<CardPrinted>();

        public CardPrinted addToLists(final CardRules card, final String cardName, final String set, CardInSet cs) {
            CardPrinted lastAdded = null;
        
            final Map<String, Map<String, CardPrinted[]>> allCardsBySet = card.isTraditional() ? allCommonCardsBySet : allSpecialCardsBySet;
            // get this set storage, if not found, create it!
            Map<String, CardPrinted[]> setMap = allCardsBySet.get(set);
            if (null == setMap) {
                setMap = new TreeMap<String, CardPrinted[]>(String.CASE_INSENSITIVE_ORDER);
                allCardsBySet.put(set, setMap);
            }
        
            final int count = cs.getCopiesCount();
            final CardPrinted[] cardCopies = new CardPrinted[count];
            setMap.put(cardName, cardCopies);
            for (int i = 0; i < count; i++) {
                lastAdded = CardPrinted.build(card, set, cs.getRarity(), i);
                if (card.isTraditional()) {
                    this.allCommonCardsFlat.add(lastAdded);
                } else {
                    this.allSpecialCardsFlat.add(lastAdded);
                }
                cardCopies[i] = lastAdded;
            }
        
            return lastAdded;
        }

        private void addNewCard(final CardRules card) {
            if (null == card) {
                return;
            } // consider that a success
              // System.out.println(card.getName());
            final String cardName = card.getName().toLowerCase();
        
            // 1. register among oracle uniques
            // cards.put(cardName, card);
        
            // 2. Save refs into two lists: one flat and other keyed with sets &
            // name
            CardPrinted lastAdded = null;
            for (final String s : card.getSets()) {
                lastAdded = this.addToLists(card, cardName, s, card.getEditionInfo(s));
            }
            if ( lastAdded.getRules().isTraditional() )
                uniqueCommonCards.put(cardName, lastAdded);
            else
                uniqueSpecialCards.put(cardName, lastAdded);
        }

        CardSorter(final Iterable<CardRules> parser) {
            for (CardRules cr : parser) {
                this.addNewCard(cr);
            }
        }
    }
}
