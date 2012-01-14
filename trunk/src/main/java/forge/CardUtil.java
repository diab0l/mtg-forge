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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.slightlymagic.maxmtg.Predicate;

import org.apache.commons.lang3.StringUtils;

import forge.card.CardCharacteristics;
import forge.card.mana.ManaCost;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityList;
import forge.gui.input.InputPayManaCostUtil;
import forge.item.CardPrinted;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;

/**
 * <p>
 * CardUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class CardUtil {
    /** Constant <code>RANDOM</code>. */
    public static final Random RANDOM = MyRandom.getRandom();

    /**
     * Do not instantiate.
     */
    private CardUtil() {
        // This space intentionally left blank.
    }

    /**
     * <p>
     * getRandomIndex.
     * </p>
     * 
     * @param o
     *            an array of {@link java.lang.Object} objects.
     * @return a int.
     */
    public static int getRandomIndex(final Object[] o) {
        if ((o == null) || (o.length == 0)) {
            throw new RuntimeException("CardUtil : getRandomIndex() argument is null or length is 0");
        }

        return CardUtil.RANDOM.nextInt(o.length);
    }

    /**
     * <p>
     * getRandom.
     * </p>
     * 
     * @param o
     *            an array of {@link forge.Card} objects.
     * @return a {@link forge.Card} object.
     */
    public static Card getRandom(final Card[] o) {
        return o[CardUtil.getRandomIndex(o)];
    }

    /**
     * <p>
     * getRandomIndex.
     * </p>
     * 
     * @param list
     *            a {@link forge.card.spellability.SpellAbilityList} object.
     * @return a int.
     */
    public static int getRandomIndex(final SpellAbilityList list) {
        if ((list == null) || (list.size() == 0)) {
            throw new RuntimeException("CardUtil : getRandomIndex(SpellAbilityList) argument is null or length is 0");
        }

        return CardUtil.RANDOM.nextInt(list.size());
    }

    /**
     * <p>
     * getRandomIndex.
     * </p>
     * 
     * @param c
     *            a {@link forge.CardList} object.
     * @return a int.
     */
    public static int getRandomIndex(final CardList c) {
        return CardUtil.RANDOM.nextInt(c.size());
    }

    // returns Card Name (unique number) attack/defense
    // example: Big Elf (12) 2/3
    /**
     * <p>
     * toText.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toText(final Card c) {
        return c.getName() + " (" + c.getUniqueNumber() + ") " + c.getNetAttack() + "/" + c.getNetDefense();
    }

    /**
     * <p>
     * toCard.
     * </p>
     * 
     * @param col
     *            a {@link java.util.Collection} object.
     * @return an array of {@link forge.Card} objects.
     */
    public static Card[] toCard(final Collection<Card> col) {
        final Object[] o = col.toArray();
        final Card[] c = new Card[o.length];

        for (int i = 0; i < c.length; i++) {
            final Object swap = o[i];
            if (swap instanceof Card) {
                c[i] = (Card) o[i];
            } else {
                throw new RuntimeException("CardUtil : toCard() invalid class, should be Card - " + o[i].getClass()
                        + " - toString() - " + o[i].toString());
            }
        }

        return c;
    }

    /**
     * <p>
     * toCard.
     * </p>
     * 
     * @param list
     *            a {@link java.util.ArrayList} object.
     * @return an array of {@link forge.Card} objects.
     */
    public static Card[] toCard(final ArrayList<Card> list) {
        final Card[] c = new Card[list.size()];
        list.toArray(c);
        return c;
    }

    /**
     * <p>
     * toList.
     * </p>
     * 
     * @param c
     *            an array of {@link forge.Card} objects.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<Card> toList(final Card[] c) {
        final ArrayList<Card> a = new ArrayList<Card>();
        for (final Card element : c) {
            a.add(element);
        }
        return a;
    }

    // returns "G", longColor is Constant.Color.Green and the like
    /**
     * <p>
     * getShortColor.
     * </p>
     * 
     * @param longColor
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getShortColor(final String longColor) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(Constant.Color.BLACK.toString(), "B");
        map.put(Constant.Color.BLUE.toString(), "U");
        map.put(Constant.Color.GREEN.toString(), "G");
        map.put(Constant.Color.RED.toString(), "R");
        map.put(Constant.Color.WHITE.toString(), "W");

        final Object o = map.get(longColor);
        if (o == null) {
            throw new RuntimeException("CardUtil : getShortColor() invalid argument - " + longColor);
        }

        return (String) o;
    }

    /**
     * <p>
     * isColor.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param col
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isColor(final Card c, final String col) {
        final ArrayList<String> list = CardUtil.getColors(c);
        return list.contains(col);
    }

    /**
     * <p>
     * getColors.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<String> getColors(final Card c) {
        return c.determineColor().toStringArray();
    }

    /**
     * <p>
     * getOnlyColors.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<String> getOnlyColors(final Card c) {
        final String m = c.getManaCost();
        final Set<String> colors = new HashSet<String>();

        for (int i = 0; i < m.length(); i++) {
            switch (m.charAt(i)) {
            case ' ':
                break;
            case 'G':
                colors.add(Constant.Color.GREEN);
                break;
            case 'W':
                colors.add(Constant.Color.WHITE);
                break;
            case 'B':
                colors.add(Constant.Color.BLACK);
                break;
            case 'U':
                colors.add(Constant.Color.BLUE);
                break;
            case 'R':
                colors.add(Constant.Color.RED);
                break;
            default:
                break;
            }
        }
        for (final String kw : c.getKeyword()) {
            if (kw.startsWith(c.getName() + " is ") || kw.startsWith("CARDNAME is ")) {
                for (final String color : Constant.Color.COLORS) {
                    if (kw.endsWith(color + ".")) {
                        colors.add(color);
                    }
                }
            }
        }
        return new ArrayList<String>(colors);
    }

    /**
     * <p>
     * hasCardName.
     * </p>
     * 
     * @param cardName
     *            a {@link java.lang.String} object.
     * @param list
     *            a {@link java.util.ArrayList} object.
     * @return a boolean.
     */
    public static boolean hasCardName(final String cardName, final ArrayList<Card> list) {
        Card c;
        boolean b = false;

        for (int i = 0; i < list.size(); i++) {
            c = list.get(i);
            if (c.getName().equals(cardName)) {
                b = true;
                break;
            }
        }
        return b;
    } // hasCardName()

    // probably should put this somewhere else, but not sure where
    /**
     * <p>
     * getConvertedManaCost.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a int.
     */
    public static int getConvertedManaCost(final SpellAbility sa) {
        return CardUtil.getConvertedManaCost(sa.getManaCost());
    }

    /**
     * <p>
     * getConvertedManaCost.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int getConvertedManaCost(final Card c) {
        if (c.isToken() && !c.isCopiedToken()) {
            return 0;
        }
        return CardUtil.getConvertedManaCost(c.getManaCost());
    }

    /**
     * <p>
     * getConvertedManaCost.
     * </p>
     * 
     * @param manaCost
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int getConvertedManaCost(final String manaCost) {
        if (manaCost.equals("")) {
            return 0;
        }

        final ManaCost cost = new ManaCost(manaCost);
        return cost.getConvertedManaCost();
    }

    /**
     * <p>
     * addManaCosts.
     * </p>
     * 
     * @param mc1
     *            a {@link java.lang.String} object.
     * @param mc2
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String addManaCosts(final String mc1, final String mc2) {
        String tMC = "";

        Integer cl1, cl2, tCL;
        String c1, c2, cc1, cc2;

        c1 = mc1.replaceAll("[WUBRGSX]", "").trim();
        c2 = mc2.replaceAll("[WUBRGSX]", "").trim();

        if (c1.length() > 0) {
            cl1 = Integer.valueOf(c1);
        } else {
            cl1 = 0;
        }

        if (c2.length() > 0) {
            cl2 = Integer.valueOf(c2);
        } else {
            cl2 = 0;
        }

        tCL = cl1 + cl2;

        cc1 = mc1.replaceAll("[0-9]", "").trim();
        cc2 = mc2.replaceAll("[0-9]", "").trim();

        tMC = tCL.toString() + " " + cc1 + " " + cc2;

        // System.out.println("TMC:" + tMC);
        return tMC.trim();
    }

    /**
     * <p>
     * getRelative.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param relation
     *            a {@link java.lang.String} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getRelative(final Card c, final String relation) {
        if (relation.equals("CARDNAME")) {
            return c;
        } else if (relation.startsWith("enchanted ")) {
            return c.getEnchantingCard();
        } else if (relation.startsWith("equipped ")) {
            return c.getEquipping().get(0);
            // else if(relation.startsWith("target ")) return c.getTargetCard();
        } else {
            throw new IllegalArgumentException("Error at CardUtil.getRelative: " + relation + "is not a valid relation");
        }
    }

    /**
     * <p>
     * isACardType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isACardType(final String cardType) {
        return CardUtil.getAllCardTypes().contains(cardType);
    }

    /**
     * <p>
     * getAllCardTypes.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<String> getAllCardTypes() {
        final ArrayList<String> types = new ArrayList<String>();

        // types.addAll(getCardTypes());
        types.addAll(Constant.CardTypes.CARD_TYPES[0].getList());

        // not currently used by Forge
        types.add("Plane");
        types.add("Scheme");
        types.add("Vanguard");

        return types;
    }

    /**
     * <p>
     * getCardTypes.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<String> getCardTypes() {
        final ArrayList<String> types = new ArrayList<String>();

        // types.add("Artifact");
        // types.add("Creature");
        // types.add("Enchantment");
        // types.add("Instant");
        // types.add("Land");
        // types.add("Planeswalker");
        // types.add("Sorcery");
        // types.add("Tribal");

        types.addAll(Constant.CardTypes.CARD_TYPES[0].getList());

        return types;
    }

    /**
     * <p>
     * getBasicTypes.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     * @since 1.1.3
     */
    public static ArrayList<String> getBasicTypes() {
        final ArrayList<String> types = new ArrayList<String>();

        types.addAll(Constant.CardTypes.BASIC_TYPES[0].getList());

        return types;
    }

    /**
     * Gets the land types.
     * 
     * @return the land types
     */
    public static ArrayList<String> getLandTypes() {
        final ArrayList<String> types = new ArrayList<String>();

        types.addAll(Constant.CardTypes.BASIC_TYPES[0].getList());
        types.addAll(Constant.CardTypes.LAND_TYPES[0].getList());

        return types;
    }

    /**
     * <p>
     * getCreatureTypes.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     * @since 1.1.6
     */
    public static ArrayList<String> getCreatureTypes() {
        final ArrayList<String> types = new ArrayList<String>();

        types.addAll(Constant.CardTypes.CREATURE_TYPES[0].getList());

        return types;
    }

    /**
     * <p>
     * isASuperType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */

    public static boolean isASuperType(final String cardType) {
        return (Constant.CardTypes.SUPER_TYPES[0].getList().contains(cardType));
    }

    /**
     * <p>
     * isASubType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isASubType(final String cardType) {
        return (!CardUtil.isASuperType(cardType) && !CardUtil.isACardType(cardType));
    }

    /**
     * <p>
     * isACreatureType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isACreatureType(final String cardType) {
        return (Constant.CardTypes.CREATURE_TYPES[0].getList().contains(cardType));
    }

    /**
     * <p>
     * isALandType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isALandType(final String cardType) {
        return (Constant.CardTypes.LAND_TYPES[0].getList().contains(cardType));
    }

    /**
     * Checks if is a planeswalker type.
     * 
     * @param cardType
     *            the card type
     * @return true, if is a planeswalker type
     */
    public static boolean isAPlaneswalkerType(final String cardType) {
        return (Constant.CardTypes.WALKER_TYPES[0].getList().contains(cardType));
    }

    /**
     * <p>
     * isABasicLandType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isABasicLandType(final String cardType) {
        return (Constant.CardTypes.BASIC_TYPES[0].getList().contains(cardType));
    }

    // this function checks, if duplicates of a keyword are not necessary (like
    // flying, trample, etc.)
    /**
     * <p>
     * isNonStackingKeyword.
     * </p>
     * 
     * @param keyword
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isNonStackingKeyword(final String keyword) {
        return Constant.Keywords.NON_STACKING_LIST[0].getList().contains(keyword);
    }

    /**
     * <p>
     * isStackingKeyword.
     * </p>
     * 
     * @param keyword
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isStackingKeyword(final String keyword) {
        return !CardUtil.isNonStackingKeyword(keyword);
    }

    /**
     * Builds the ideal filename.
     * 
     * @param cardName
     *            the card name
     * @param artIndex
     *            the art index
     * @param artIndexMax
     *            the art index max
     * @return the string
     */
    public static String buildIdealFilename(final String cardName, final int artIndex, final int artIndexMax) {
        final String nn = artIndexMax > 1 ? Integer.toString(artIndex + 1) : "";
        final String mwsCardName = GuiDisplayUtil.cleanStringMWS(cardName);
        // 3 letter set code with MWS filename format
        return String.format("%s%s.full.jpg", mwsCardName, nn);
    }

    /**
     * <p>
     * buildFilename.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @return a {@link java.lang.String} object.
     */
    public static String buildFilename(final Card card) {
        final boolean token = card.isToken() && !card.isCopiedToken();

        final String set = card.getCurSetCode();
        final Predicate<SetInfo> findSetInfo = new Predicate<SetInfo>() {
            @Override
            public boolean isTrue(final SetInfo subject) {
                return subject.getCode().equals(set);
            }
        };
        final SetInfo neededSet = findSetInfo.first(card.getSets());
        final int cntPictures = neededSet == null ? 1 : neededSet.getPicCount();
        return CardUtil
                .buildFilename(card.getName(), card.getCurSetCode(), card.getRandomPicture(), cntPictures, token);
    }

    /**
     * buildFilename for lightweight card. Searches for a matching file on disk,
     * 
     * @param card
     *            the card
     * @return the string
     */
    public static String buildFilename(final CardPrinted card) {
        final int maxIndex = card.getCard().getSetInfo(card.getSet()).getCopiesCount();
        return CardUtil.buildFilename(card.getName(), card.getSet(), card.getArtIndex(), maxIndex, false);
    }

    /**
     * Builds the filename.
     * 
     * @param card
     *            the card
     * @param nameToUse
     *            the name to use
     * @return the string
     */
    public static String buildFilename(final CardPrinted card, final String nameToUse) {
        final int maxIndex = card.getCard().getSetInfo(card.getSet()).getCopiesCount();
        return CardUtil.buildFilename(nameToUse, card.getSet(), card.getArtIndex(), maxIndex, false);
    }

    private static String buildFilename(final String cardName, final String setName, final int artIndex,
            final int artIndexMax, final boolean isToken) {
        final File path = ForgeProps.getFile(isToken ? NewConstants.IMAGE_TOKEN : NewConstants.IMAGE_BASE);
        final String nn = artIndexMax > 1 ? Integer.toString(artIndex + 1) : "";
        final String cleanCardName = GuiDisplayUtil.cleanString(cardName);

        File f = null;
        if (StringUtils.isNotBlank(setName)) {
            final String mwsCardName = GuiDisplayUtil.cleanStringMWS(cardName);

            // First, try 3 letter set code with MWS filename format
            final String mwsSet3 = String.format("%s/%s%s.full", setName, mwsCardName, nn);
            f = new File(path, mwsSet3 + ".jpg");
            if (f.exists()) {
                return mwsSet3;
            }

            // Second, try 2 letter set code with MWS filename format
            final String mwsSet2 = String.format("%s/%s%s.full", SetUtils.getCode2ByCode(setName), mwsCardName, nn);
            f = new File(path, mwsSet2 + ".jpg");
            if (f.exists()) {
                return mwsSet2;
            }

            // Third, try 3 letter set code with Forge filename format
            final String forgeSet3 = String.format("%s/%s%s", setName, cleanCardName, nn);
            f = new File(path, forgeSet3 + ".jpg");
            if (f.exists()) {
                return forgeSet3;
            }
        }

        // Last, give up with set images, go with the old picture type
        final String forgePlain = String.format("%s%s", cleanCardName, nn);

        f = new File(path, forgePlain + ".jpg");
        if (f.exists()) {
            return forgePlain;
        }

        // give up with art index
        f = new File(path, cleanCardName + ".jpg");
        if (f.exists()) {
            return cleanCardName;
        }

        // if still no file, download if option enabled?
        return "none";
    }

    /**
     * <p>
     * getWeightedManaCost.
     * </p>
     * 
     * @param manaCost
     *            a {@link java.lang.String} object.
     * @return a double.
     */
    public static double getWeightedManaCost(final String manaCost) {
        if (manaCost.equals("")) {
            return 0;
        }

        final ManaCost cost = new ManaCost(manaCost);
        return cost.getWeightedManaCost();
    }

    /**
     * <p>
     * getShortColorsString.
     * </p>
     * 
     * @param colors
     *            a {@link java.util.ArrayList} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getShortColorsString(final ArrayList<String> colors) {
        String colorDesc = "";
        for (final String col : colors) {
            if (col.equalsIgnoreCase("White")) {
                colorDesc += "W";
            } else if (col.equalsIgnoreCase("Blue")) {
                colorDesc += "U";
            } else if (col.equalsIgnoreCase("Black")) {
                colorDesc += "B";
            } else if (col.equalsIgnoreCase("Red")) {
                colorDesc += "R";
            } else if (col.equalsIgnoreCase("Green")) {
                colorDesc += "G";
            } else if (col.equalsIgnoreCase("Colorless")) {
                colorDesc = "C";
            }
        }
        return colorDesc;
    }

    /**
     * Compute the canonicalized ASCII form of a card name.
     * 
     * @param cardName
     *            the name to transform (but not side effect)
     * 
     * @return the name in ASCII characters
     */
    public static String canonicalizeCardName(final String cardName) {
        String result = cardName;
        result = result.replace("\u00ae", "(R)"); // Ultimate Nightmare ...
        result = result.replace("\u00c6", "AE");
        result = result.replace("\u00e0", "a");
        result = result.replace("\u00e1", "a");
        result = result.replace("\u00e2", "a");
        result = result.replace("\u00e9", "e");
        result = result.replace("\u00ed", "i");
        result = result.replace("\u00f6", "o");
        result = result.replace("\u00fa", "u");
        result = result.replace("\u00fb", "u");
        result = result.replace("\u2012", "-");
        result = result.replace("\u2013", "-");
        result = result.replace("\u2014", "-");
        result = result.replace("\u2015", "-");
        result = result.replace("\u2018", "'");
        result = result.replace("\u2019", "'");
        result = result.replace("\u221e", "Infinity"); // Mox Lo...

        return result;
    }

    /**
     * getThisTurnEntered.
     * 
     * @param to
     *            zone going to
     * @param from
     *            zone coming from
     * @param valid
     *            a isValid expression
     * @param src
     *            a Card object
     * @return a CardList that matches the given criteria
     */
    public static CardList getThisTurnEntered(final Constant.Zone to, final Constant.Zone from, final String valid,
            final Card src) {
        CardList res = new CardList();
        if (to != Constant.Zone.Stack) {
            res.addAll(((DefaultPlayerZone) AllZone.getComputerPlayer().getZone(to)).getCardsAddedThisTurn(from));
            res.addAll(((DefaultPlayerZone) AllZone.getHumanPlayer().getZone(to)).getCardsAddedThisTurn(from));
        } else {
            res.addAll(((DefaultPlayerZone) AllZone.getStackZone()).getCardsAddedThisTurn(from));
        }

        res = res.getValidCards(valid, src.getController(), src);

        return res;
    }

    /**
     * getThisTurnCast.
     * 
     * @param valid
     *            a String object
     * @param src
     *            a Card object
     * @return a CardList that matches the given criteria
     */
    public static CardList getThisTurnCast(final String valid, final Card src) {
        CardList res = new CardList();

        res.addAll(AllZone.getStack().getCardsCastThisTurn());

        res = res.getValidCards(valid, src.getController(), src);

        return res;
    }

    /**
     * getLastTurnCast.
     * 
     * @param valid
     *            a String object
     * @param src
     *            a Card object
     * @return a CardList that matches the given criteria
     */
    public static CardList getLastTurnCast(final String valid, final Card src) {
        CardList res = new CardList();

        res.addAll(AllZone.getStack().getCardsCastLastTurn());

        res = res.getValidCards(valid, src.getController(), src);

        return res;
    }

    /**
     * getLKICopy.
     * 
     * @param c
     *            a Card.
     * @return a copy of C with LastKnownInfo stuff retained.
     */
    public static Card getLKICopy(final Card c,final String state) {
        if (c.isToken()) {
            return c;
        }
        final Card res = AllZone.getCardFactory().copyCard(c);
        res.setState(state);
        res.setControllerObjects(c.getControllerObjects());
        res.addTempAttackBoost(c.getTempAttackBoost());
        res.addSemiPermanentAttackBoost(c.getSemiPermanentAttackBoost());
        res.addTempDefenseBoost(c.getTempDefenseBoost());
        res.addSemiPermanentDefenseBoost(c.getSemiPermanentDefenseBoost());
        res.setCounters(c.getCounters());
        res.setExtrinsicKeyword(c.getExtrinsicKeyword());
        res.setColor(c.getColor());
        res.setChangedCardTypes(c.getChangedCardTypes());
        res.setNewPT(new ArrayList<CardPowerToughness>(c.getNewPT()));
        res.setReceivedDamageFromThisTurn(c.getReceivedDamageFromThisTurn());
        res.setCreatureGotBlockedThisTurn(c.getCreatureGotBlockedThisTurn());
        res.setEnchanting(c.getEnchanting());
        res.setHaunting(c.getHaunting());
        for (final Card haunter : c.getHauntedBy()) {
            res.addHauntedBy(haunter);
        }

        return res;
    }

    /**
     * Gets the radiance.
     * 
     * @param source
     *            the source
     * @param origin
     *            the origin
     * @param valid
     *            the valid
     * @return the radiance
     */
    public static CardList getRadiance(final Card source, final Card origin, final String[] valid) {
        final CardList res = new CardList();

        for (final CardColor col : origin.getColor()) {
            for (final String strCol : col.toStringArray()) {
                if (strCol.equalsIgnoreCase("Colorless")) {
                    continue;
                }
                for (final Card c : AllZoneUtil.getColorInPlay(strCol)) {
                    if (!res.contains(c) && c.isValid(valid, source.getController(), source) && !c.equals(origin)) {
                        res.add(c);
                    }
                }
            }
        }

        return res;
    }

    /**
     * Gets the convokable colors.
     * 
     * @param cardToConvoke
     *            the card to convoke
     * @param cost
     *            the cost
     * @return the convokable colors
     */
    public static ArrayList<String> getConvokableColors(final Card cardToConvoke, final ManaCost cost) {
        final ArrayList<String> usableColors = new ArrayList<String>();

        if (cost.getColorlessManaAmount() > 0) {
            usableColors.add("colorless");
        }
        for (final CardColor col : cardToConvoke.getColor()) {
            for (final String strCol : col.toStringArray()) {
                if (strCol.equals("colorless")) {
                    continue;
                }
                if (cost.toString().contains(InputPayManaCostUtil.getShortColorString(strCol))) {
                    usableColors.add(strCol.toString());
                }
            }
        }

        return usableColors;
    }

    /**
     * Gets the face down characteristic.
     * 
     * @return the face down characteristic
     */
    public static CardCharacteristics getFaceDownCharacteristic() {
        final ArrayList<String> types = new ArrayList<String>();
        types.add("Creature");

        final CardCharacteristics ret = new CardCharacteristics();
        ret.setBaseAttack(2);
        ret.setBaseDefense(2);

        ret.setName("");
        ret.setType(types);

        ret.setImageFilename("morph");

        return ret;

    }

} // end class CardUtil
