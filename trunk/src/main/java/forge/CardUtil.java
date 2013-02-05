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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import forge.card.CardCharacteristics;
import forge.card.EditionInfo;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.mana.ManaCostBeingPaid;
import forge.card.spellability.AbilityManaPart;
import forge.card.spellability.SpellAbility;
import forge.control.input.InputPayManaCostUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiDisplayUtil;
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
    /**
     * Do not instantiate.
     */
    private CardUtil() {
        // This space intentionally left blank.
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
     * getColors.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static List<String> getColors(final Card c) {
        return c.determineColor().toStringList();
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
        String kw = new String(keyword);
        if (kw.startsWith("HIDDEN")) {
            kw = kw.substring(7);
        }
        if (kw.startsWith("Protection")) {
            return true;
        }
        return Constant.Keywords.NON_STACKING_LIST.contains(kw);
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
        EditionInfo neededSet = null;
        for(EditionInfo e : card.getSets()) {
            if ( e.getCode().equals(set) ) {
                neededSet = e;
                break;
            }
        }
        final int cntPictures = neededSet == null ? 1 : neededSet.getPicCount();
        return CardUtil.buildFilename(card.getName(), card.getCurSetCode(), card.getRandomPicture(), cntPictures, token);
    }

    /**
     * buildFilename for lightweight card. Searches for a matching file on disk,
     * 
     * @param card
     *            the card
     * @return the string
     */
    public static String buildFilename(final CardPrinted card) {
        final int maxIndex = card.getCard().getEditionInfo(card.getEdition()).getCopiesCount();
        return CardUtil.buildFilename(card.getName(), card.getEdition(), card.getArtIndex(), maxIndex, false);
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
        final int maxIndex = card.getCard().getEditionInfo(card.getEdition()).getCopiesCount();
        return CardUtil.buildFilename(nameToUse, card.getEdition(), card.getArtIndex(), maxIndex, false);
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
            final String mwsSet2 = String.format("%s/%s%s.full", Singletons.getModel().getEditions().getCode2ByCode(setName), mwsCardName, nn);
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
     * @return a List<Card> that matches the given criteria
     */
    public static List<Card> getThisTurnEntered(final ZoneType to, final ZoneType from, final String valid,
            final Card src) {
        List<Card> res = new ArrayList<Card>();
        if (to != ZoneType.Stack) {
            for (Player p : Singletons.getModel().getGame().getPlayers()) {
                res.addAll(p.getZone(to).getCardsAddedThisTurn(from));
            }
        } else {
            res.addAll(Singletons.getModel().getGame().getStackZone().getCardsAddedThisTurn(from));
        }

        res = CardLists.getValidCards(res, valid, src.getController(), src);

        return res;
    }

    /**
     * getThisTurnCast.
     * 
     * @param valid
     *            a String object
     * @param src
     *            a Card object
     * @return a List<Card> that matches the given criteria
     */
    public static List<Card> getThisTurnCast(final String valid, final Card src) {
        List<Card> res = new ArrayList<Card>();

        res.addAll(Singletons.getModel().getGame().getStack().getCardsCastThisTurn());

        res = CardLists.getValidCards(res, valid, src.getController(), src);

        return res;
    }

    /**
     * getLastTurnCast.
     * 
     * @param valid
     *            a String object
     * @param src
     *            a Card object
     * @return a List<Card> that matches the given criteria
     */
    public static List<Card> getLastTurnCast(final String valid, final Card src) {
        List<Card> res = new ArrayList<Card>();

        res.addAll(Singletons.getModel().getGame().getStack().getCardsCastLastTurn());

        res = CardLists.getValidCards(res, valid, src.getController(), src);

        return res;
    }

    /**
     * getLKICopy.
     * 
     * @param in
     *            a Card to copy.
     * @return a copy of C with LastKnownInfo stuff retained.
     */
    public static Card getLKICopy(final Card in) {
        if (in.isToken()) {
            return in;
        }

        final Card newCopy = new Card();
        newCopy.setUniqueNumber(in.getUniqueNumber());
        newCopy.setCurSetCode(in.getCurSetCode());
        newCopy.setOwner(in.getOwner());
        newCopy.setFlipCard(in.isFlipCard());
        newCopy.setDoubleFaced(in.isDoubleFaced());
        newCopy.getCharacteristics().copy(in.getState(in.getCurState()));
        newCopy.setBaseAttack(in.getNetAttack());
        newCopy.setBaseDefense(in.getNetDefense());
        newCopy.setType(new ArrayList<String>(in.getType()));
        newCopy.setTriggers(in.getTriggers());
        for (SpellAbility sa : in.getManaAbility()) {
            newCopy.addSpellAbility(sa);
        }

        newCopy.setControllerObjects(in.getControllerObjects());
        newCopy.setCounters(in.getCounters());
        newCopy.setExtrinsicKeyword(in.getExtrinsicKeyword());
        newCopy.setColor(in.getColor());
        newCopy.setReceivedDamageFromThisTurn(in.getReceivedDamageFromThisTurn());
        newCopy.getDamageHistory().setCreatureGotBlockedThisTurn(in.getDamageHistory().getCreatureGotBlockedThisTurn());
        newCopy.setEnchanting(in.getEnchanting());
        newCopy.setEnchantedBy(new ArrayList<Card> (in.getEnchantedBy()));
        newCopy.setEquipping(new ArrayList<Card> (in.getEquipping()));
        newCopy.setEquippedBy(new ArrayList<Card> (in.getEquippedBy()));
        newCopy.setClones(in.getClones());
        newCopy.setHaunting(in.getHaunting());
        for (final Card haunter : in.getHauntedBy()) {
            newCopy.addHauntedBy(haunter);
        }
        for (final Object o : in.getRemembered()) {
            newCopy.addRemembered(o);
        }
        for (final Card o : in.getImprinted()) {
            newCopy.addImprinted(o);
        }

        return newCopy;
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
    public static List<Card> getRadiance(final Card source, final Card origin, final String[] valid) {
        final List<Card> res = new ArrayList<Card>();

        for (final CardColor col : origin.getColor()) {
            for (final String strCol : col.toStringList()) {
                if (strCol.equalsIgnoreCase("Colorless")) {
                    continue;
                }
                for (final Card c : Singletons.getModel().getGame().getColoredCardsInPlay(strCol)) {
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
    public static ArrayList<String> getConvokableColors(final Card cardToConvoke, final ManaCostBeingPaid cost) {
        final ArrayList<String> usableColors = new ArrayList<String>();

        if (cost.getColorlessManaAmount() > 0) {
            usableColors.add("colorless");
        }
        for (final CardColor col : cardToConvoke.getColor()) {
            for (final String strCol : col.toStringList()) {
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

        ret.setImageName(NewConstants.MORPH_IMAGE_FILE_NAME);

        return ret;
    }

    // add Colors and
    /**
     * <p>
     * reflectableMana.
     * </p>
     * 
     * @param abMana
     *            a {@link forge.card.spellability.AbilityMana} object.
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param colors
     *            a {@link java.util.ArrayList} object.
     * @param parents
     *            a {@link java.util.ArrayList} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static Set<String> getReflectableManaColors(final SpellAbility abMana, final SpellAbility sa,
            Set<String> colors, final List<Card> parents) {
        // Here's the problem with reflectable Mana. If more than one is out,
        // they need to Reflect each other,
        // so we basically need to have a recursive list that send the parents
        // so we don't infinite recurse.
        final Card card = abMana.getSourceCard();

        if (!parents.contains(card)) {
            parents.add(card);
        }

        final String colorOrType = sa.getParam("ColorOrType"); // currently Color
                                                              // or
        // Type, Type is colors
        // + colorless
        final String validCard = sa.getParam("Valid");
        final String reflectProperty = sa.getParam("ReflectProperty"); // Produce
        // (Reflecting Pool) or Is (Meteor Crater)

        int maxChoices = 5; // Color is the default colorOrType
        if (colorOrType.equals("Type")) {
            maxChoices++;
        }

        List<Card> cards = null;

        // Reuse AF_Defined in a slightly different way
        if (validCard.startsWith("Defined.")) {
            cards = AbilityFactory.getDefinedCards(card, validCard.replace("Defined.", ""), abMana);
        } else {
            cards = CardLists.getValidCards(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), validCard, abMana.getActivatingPlayer(), card);
        }

        // remove anything cards that is already in parents
        for (final Card p : parents) {
            if (cards.contains(p)) {
                cards.remove(p);
            }
        }

        if ((cards.size() == 0) && !reflectProperty.equals("Produced")) {
            return colors;
        }

        if (reflectProperty.equals("Is")) { // Meteor Crater
            for (final Card card1 : cards) {
                // For each card, go through all the colors and if the card is that color, add
                for (final String col : Constant.Color.ONLY_COLORS) {
                    if (card1.isColor(col)) {
                        colors.add(col);
                        if (colors.size() == maxChoices) {
                            break;
                        }
                    }
                }
            }
        } else if (reflectProperty.equals("Produced")) {
            final String producedColors = (String) abMana.getTriggeringObject("Produced");
            for (final String col : Constant.Color.ONLY_COLORS) {
                final String s = InputPayManaCostUtil.getShortColorString(col);
                if (producedColors.contains(s)) {
                    colors.add(col);
                }
            }
            if (maxChoices == 6 && producedColors.contains("1")) {
                colors.add(Constant.Color.COLORLESS);
            }
        } else if (reflectProperty.equals("Produce")) {
            final ArrayList<SpellAbility> abilities = new ArrayList<SpellAbility>();
            for (final Card c : cards) {
                abilities.addAll(c.getManaAbility());
            }
            // currently reflected mana will ignore other reflected mana
            // abilities

            final ArrayList<SpellAbility> reflectAbilities = new ArrayList<SpellAbility>();

            for (final SpellAbility ab : abilities) {
                if (maxChoices == colors.size()) {
                    break;
                }

                if (ab.getManaPart().isReflectedMana()) {
                    if (!parents.contains(ab.getSourceCard())) {
                        // Recursion!
                        reflectAbilities.add(ab);
                        parents.add(ab.getSourceCard());
                    }
                    continue;
                }
                colors = canProduce(maxChoices, ab.getManaPart(), colors);
                if (!parents.contains(ab.getSourceCard())) {
                    parents.add(ab.getSourceCard());
                }
            }

            for (final SpellAbility ab : reflectAbilities) {
                if (maxChoices == colors.size()) {
                    break;
                }

                colors = CardUtil.getReflectableManaColors(ab, sa, colors, parents);
            }
        }
        return colors;
    }

    public static Set<String> canProduce(final int maxChoices, final AbilityManaPart ab,
            final Set<String> colors) {
        for (final String col : Constant.Color.ONLY_COLORS) {
            final String s = InputPayManaCostUtil.getShortColorString(col);
            if (ab.canProduce(s)) {
                colors.add(col);
            }
        }

        if (maxChoices == 6 && ab.canProduce("1")) {
            colors.add(Constant.Color.COLORLESS);
        }

        return colors;
    }


} // end class CardUtil
