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
import java.util.Map;

import forge.card.mana.ManaCostShard;
import forge.card.mana.IParserManaCost;
import forge.card.mana.ManaCost;


/**
 * <p>
 * CardReader class.
 * </p>
 * 
 * Forked from forge.CardReader at rev 10010.
 * 
 * @version $Id$
 */
public class CardRulesReader {

    private CardRuleCharacteristics[] characteristics = new CardRuleCharacteristics[] { new CardRuleCharacteristics(),
            null };
    private int curCharacteristics = 0;

    private boolean removedFromAIDecks = false;
    private boolean removedFromRandomDecks = false;
    private List<String> originalScript = new ArrayList<String>();

    // Reset all fields to parse next card (to avoid allocating new
    // CardRulesReader N times)
    /**
     * Reset.
     */
    public final void reset() {
        this.characteristics = new CardRuleCharacteristics[] { new CardRuleCharacteristics(), null };
        this.curCharacteristics = 0;
        this.removedFromAIDecks = false;
        this.removedFromRandomDecks = false;
        originalScript.clear();
        // this.isDoubleFacedCard = false;
        // this.isFlipCard = false;
    }

    /**
     * Gets the card.
     * 
     * @return the card
     */
    public final CardRules getCard() {
        final boolean hasOtherPart = this.characteristics[1] != null;
        final CardRules otherPart = hasOtherPart ? new CardRules(this.characteristics[1], null, true, null,
                this.removedFromRandomDecks, this.removedFromAIDecks) : null;

        return new CardRules(this.characteristics[0], originalScript, hasOtherPart, otherPart, this.removedFromRandomDecks,
                this.removedFromAIDecks);
    }

    /**
     * Parses the line.
     * 
     * @param line
     *            the line
     */
    public final void parseLine(final String line) {

        originalScript.add(line);

        switch(line.charAt(0)) {
            case 'A':
                if (line.startsWith("AlternateMode:")) {
                    // this.isDoubleFacedCard = "DoubleFaced".equalsIgnoreCase(CardRulesReader.getValueAfterKey(line, "AlternateMode:"));
                    // this.isFlipCard = "Flip".equalsIgnoreCase(CardRulesReader.getValueAfterKey(line, "AlternateMode:"));
                } else if (line.equals("ALTERNATE")) {
                    this.characteristics[1] = new CardRuleCharacteristics();
                    this.curCharacteristics = 1;
                }
            break;

            case 'C':
                if (line.startsWith("Colors:")) {
                    // This is forge.card.CardColor not forge.CardColor.
                    // Why do we have two classes with the same name?
                    final String value = line.substring("Colors:".length());
                    ColorSet newCol = ColorSet.fromNames(value.split(","));
                    this.characteristics[this.curCharacteristics].setColor(newCol);
                }
                break;

            case 'D':
                if (line.startsWith("DeckHints:")) {
                    this.characteristics[this.curCharacteristics].setDeckHints(CardRulesReader.getValueAfterKey(line, "DeckHints:"));
                } else if (line.startsWith("DeckNeeds:")) {
                    this.characteristics[this.curCharacteristics].setDeckNeeds(CardRulesReader.getValueAfterKey(line, "DeckNeeds:"));
                }
                break;

            case 'H':
                if (line.startsWith("HandLifeModifier:")) {
                    this.characteristics[this.curCharacteristics].setPtLine(CardRulesReader.getValueAfterKey(line, "HandLifeModifier:"));
                }
                break;

            case 'K':
                if (line.startsWith("K:")) {
                    final String value = line.substring(2);
                    this.characteristics[this.curCharacteristics].addKeyword(value);
                }
                break;

            case 'L':
                if (line.startsWith("Loyalty:")) {
                    this.characteristics[this.curCharacteristics].setPtLine(CardRulesReader.getValueAfterKey(line, "Loyalty:"));

                }
                break;

            case 'M':
                if (line.startsWith("ManaCost:")) {
                    final String sCost = CardRulesReader.getValueAfterKey(line, "ManaCost:");
                    this.characteristics[this.curCharacteristics].setManaCost("no cost".equals(sCost) ? ManaCost.NO_COST
                            : new ManaCost(new ParserCardnameTxtManaCost(sCost)));
                }

            case 'N':
                if (line.startsWith("Name:")) {
                    this.characteristics[this.curCharacteristics].setCardName(CardRulesReader.getValueAfterKey(line, "Name:"));
                    if ((this.characteristics[this.curCharacteristics].getCardName() == null)
                            || this.characteristics[this.curCharacteristics].getCardName().isEmpty()) {
                        throw new RuntimeException("Card name is empty");
                    }
                }
                break;

            case 'O':
                if (line.startsWith("Oracle:")) {
                    this.characteristics[this.curCharacteristics].setCardRules(CardRulesReader
                            .getValueAfterKey(line, "Oracle:"));

                }
                break;

            case 'P':
                if (line.startsWith("PT:")) {
                    this.characteristics[this.curCharacteristics].setPtLine(CardRulesReader.getValueAfterKey(line, "PT:"));
                }
                break;

            case 'S':
                if (line.startsWith("SVar:RemAIDeck:")) {
                    this.removedFromAIDecks = "True".equalsIgnoreCase(CardRulesReader.getValueAfterKey(line, "SVar:RemAIDeck:"));
                } else if (line.startsWith("SVar:RemRandomDeck:")) {
                    this.removedFromRandomDecks = "True".equalsIgnoreCase(CardRulesReader.getValueAfterKey(line, "SVar:RemRandomDeck:"));
                } else if (line.startsWith("SVar:Picture:")) {
                    this.characteristics[this.curCharacteristics].setDlUrl(CardRulesReader.getValueAfterKey(line, "SVar:Picture:"));
                } else if (line.startsWith("SetInfo:")) {
                    CardRulesReader.parseSetInfoLine(line, this.characteristics[this.curCharacteristics].getSetsData());
                }
                break;

            case 'T':
                if (line.startsWith("Types:")) {
                    this.characteristics[this.curCharacteristics].setCardType(CardType.parse(CardRulesReader.getValueAfterKey(
                            line, "Types:")));
                }
                break;
        }

    }

    /**
     * Parse a SetInfo line from a card txt file.
     * 
     * @param line
     *            must begin with "SetInfo:"
     * @param setsData
     *            the current mapping of set names to CardInSet instances
     */
    public static void parseSetInfoLine(final String line, final Map<String, CardInSet> setsData) {
        final int setCodeIx = 0;
        final int rarityIx = 1;
        final int numPicIx = 3;

        // Sample SetInfo line:
        // SetInfo:POR|Land|http://magiccards.info/scans/en/po/203.jpg|4

        final String value = line.substring("SetInfo:".length());
        final String[] pieces = value.split("\\|");

        if (pieces.length <= rarityIx) {
            throw new RuntimeException("SetInfo line <<" + value + ">> has insufficient pieces");
        }

        final String setCode = pieces[setCodeIx];
        final String txtRarity = pieces[rarityIx];
        // pieces[2] is the magiccards.info URL for illustration #1, which we do
        // not need.
        int numIllustrations = 1;

        if (setsData.containsKey(setCode)) {
            throw new RuntimeException("Found multiple SetInfo lines for set code <<" + setCode + ">>");
        }

        if (pieces.length > numPicIx) {
            try {
                numIllustrations = Integer.parseInt(pieces[numPicIx]);
            } catch (final NumberFormatException nfe) {
                throw new RuntimeException("Fourth item of SetInfo is not an integer in <<" + value + ">>");
            }

            if (numIllustrations < 1) {
                throw new RuntimeException("Fourth item of SetInfo is not a positive integer, but" + numIllustrations);
            }
        }

        CardRarity rarity = null;
        if ("Land".equals(txtRarity)) {
            rarity = CardRarity.BasicLand;
        } else if ("Common".equals(txtRarity)) {
            rarity = CardRarity.Common;
        } else if ("Uncommon".equals(txtRarity)) {
            rarity = CardRarity.Uncommon;
        } else if ("Rare".equals(txtRarity)) {
            rarity = CardRarity.Rare;
        } else if ("Mythic".equals(txtRarity)) {
            rarity = CardRarity.MythicRare;
        } else if ("Special".equals(txtRarity)) {
            rarity = CardRarity.Special;
        } else {
            throw new RuntimeException("Unrecognized rarity string <<" + txtRarity + ">>");
        }

        final CardInSet cardInSet = new CardInSet(rarity, numIllustrations, pieces[2]);

        setsData.put(setCode, cardInSet);
    }

    /**
     * Gets the value after key.
     * 
     * @param line
     *            the line
     * @param fieldNameWithColon
     *            the field name with colon
     * @return the value after key
     */
    public static String getValueAfterKey(final String line, final String fieldNameWithColon) {
        final int startIx = fieldNameWithColon.length();
        final String lineAfterColon = line.substring(startIx);
        return lineAfterColon.trim();
    }

    /**
     * The Class ParserCardnameTxtManaCost.
     */
    public static class ParserCardnameTxtManaCost implements IParserManaCost {
        private final String[] cost;
        private int nextToken;
        private int colorlessCost;

        /**
         * Instantiates a new parser cardname txt mana cost.
         * 
         * @param cost
         *            the cost
         */
        public ParserCardnameTxtManaCost(final String cost) {
            this.cost = cost.split(" ");
            // System.out.println(cost);
            this.nextToken = 0;
            this.colorlessCost = 0;
        }

        /*
         * (non-Javadoc)
         * 
         * @see forge.card.CardManaCost.ManaParser#getTotalColorlessCost()
         */
        @Override
        public final int getTotalColorlessCost() {
            if (this.hasNext()) {
                throw new RuntimeException("Colorless cost should be obtained after iteration is complete");
            }
            return this.colorlessCost;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public final boolean hasNext() {
            return this.nextToken < this.cost.length;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        @Override
        public final ManaCostShard next() {

            final String unparsed = this.cost[this.nextToken++];
            // System.out.println(unparsed);
            try {
                int iVal = Integer.parseInt(unparsed);
                this.colorlessCost += iVal;
                return null;
            }
            catch (NumberFormatException nex) { }

            return ManaCostShard.parseNonGeneric(unparsed);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
        } // unsuported
    }

}
