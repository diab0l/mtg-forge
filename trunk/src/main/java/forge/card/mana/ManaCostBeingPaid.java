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
package forge.card.mana;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import forge.Card;
import forge.CardColor;
import forge.CardLists;
import forge.CardPredicates;
import forge.Constant;
import forge.card.MagicColor;
import forge.card.spellability.SpellAbility;
import forge.card.staticability.StaticAbility;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.util.MyRandom;

/**
 * <p>
 * ManaCost class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class ManaCostBeingPaid {
    private class ManaCostBeingPaidIterator implements IParserManaCost {
        private Iterator<ManaCostShard> mch;
        private ManaCostShard nextShard;
        private int remainingShards = 0;
        
        public ManaCostBeingPaidIterator() { 
            mch = unpaidShards.keySet().iterator();
        }
    
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public ManaCostShard next() {
            if (remainingShards == 0)
                throw new UnsupportedOperationException("All shards were depleted, call hasNext()");
            remainingShards--;
            return nextShard;
        }
        
        @Override
        public boolean hasNext() {
            if ( remainingShards > 0 ) return true;
            if ( !mch.hasNext() ) return false;
            
            nextShard = mch.next();
            if ( nextShard == ManaCostShard.COLORLESS )
                return this.hasNext(); // skip colorless
            remainingShards = unpaidShards.get(nextShard);
            
            return true;
        }
        
        @Override
        public int getTotalColorlessCost() {
            Integer c = unpaidShards.get(ManaCostShard.COLORLESS);
            return c == null ? 0 : c.intValue();
        }
    }

    // holds Mana_Part objects
    // ManaPartColor is stored before ManaPartColorless
    private final HashMap<ManaCostShard, Integer> unpaidShards = new HashMap<ManaCostShard, Integer>();
    private final HashMap<String, Integer> sunburstMap = new HashMap<String, Integer>();
    private int cntX = 0;
    private final ArrayList<String> manaNeededToAvoidNegativeEffect = new ArrayList<String>();
    private final ArrayList<String> manaPaidToAvoidNegativeEffect = new ArrayList<String>();
    private final String sourceRestriction;
    
    // manaCost can be like "0", "3", "G", "GW", "10", "3 GW", "10 GW"
    // or "split hybrid mana" like "2/G 2/G", "2/B 2/B 2/B"
    // "GW" can be paid with either G or W

    /**
     * <p>
     * Constructor for ManaCost.
     * </p>
     * 
     * @param manaCost
     *            a {@link java.lang.String} object.
     */
    public ManaCostBeingPaid(String sCost) {
        this("0".equals(sCost) || "C".equals(sCost) || sCost.isEmpty() ? ManaCost.ZERO : new ManaCost(new ManaCostParser(sCost)));
    }

    public ManaCostBeingPaid(ManaCost manaCost) {
        this(manaCost, null);
    }
    public ManaCostBeingPaid(ManaCost manaCost, String srcRestriction) {
        sourceRestriction = srcRestriction;
        if( manaCost == null ) return;
        for (ManaCostShard shard : manaCost.getShards()) {
            if (shard == ManaCostShard.X) {
                cntX++;
            } else {
                increaseShard(shard, 1);
            }
        }
        increaseColorlessMana(manaCost.getGenericCost());
    }

    /**
     * <p>
     * getSunburst.
     * </p>
     * 
     * @return a int.
     */
    public final int getSunburst() {
        final int ret = this.sunburstMap.size();
        this.sunburstMap.clear();
        return ret;
    }

    /**
     * <p>
     * getColorsPaid.
     * </p>
     * 
     * @return a String.
     */
    public final String getColorsPaid() {
        String s = "";
        for (final String key : this.sunburstMap.keySet()) {
            if (key.equalsIgnoreCase("black") || key.equalsIgnoreCase("B")) {
                s += "B";
            }
            if (key.equalsIgnoreCase("blue") || key.equalsIgnoreCase("U")) {
                s += "U";
            }
            if (key.equalsIgnoreCase("green") || key.equalsIgnoreCase("G")) {
                s += "G";
            }
            if (key.equalsIgnoreCase("red") || key.equalsIgnoreCase("R")) {
                s += "R";
            }
            if (key.equalsIgnoreCase("white") || key.equalsIgnoreCase("W")) {
                s += "W";
            }
        }
        return s;
    }

    /**
     * <p>
     * getUnpaidPhyrexianMana.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    private List<ManaCostShard> getUnpaidPhyrexianMana() {
        ArrayList<ManaCostShard> res = new ArrayList<ManaCostShard>();
        for (final Entry<ManaCostShard, Integer> part : this.unpaidShards.entrySet()) {

            if (!part.getKey().isPhyrexian()) {
                continue;
            }
            for (int i = 0; i < part.getValue(); i++) {
                res.add(part.getKey());
            }
        }
        return res;
    }

    /**
     * <p>
     * containsPhyrexianMana.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean containsPhyrexianMana() {
        for (ManaCostShard shard : unpaidShards.keySet()) {
            if (shard.isPhyrexian()) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * payPhyrexian.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean payPhyrexian() {
        final List<ManaCostShard> phy = this.getUnpaidPhyrexianMana();

        if (phy.size() > 0) {
            Integer cnt = unpaidShards.get(phy.get(0));
            if (cnt <= 1) {
                unpaidShards.remove(phy.get(0));
            } else {
                unpaidShards.put(phy.get(0), Integer.valueOf(cnt - 1));
            }

            return true;
        }

        return false;
    }

    // takes a Short Color and returns true if it exists in the mana cost.
    // Easier for split costs
    /**
     * <p>
     * isColor.
     * </p>
     * 
     * @param color
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean isColor(final String color) {
        //if ( "1".equals(color) ) return getColorlessManaAmount() > 0;
        if (color.matches("^\\d+$")) {
            return getColorlessManaAmount() > 0;
        }

        for (ManaCostShard shard : unpaidShards.keySet()) {

            String ss = shard.toString();
            if (ss.contains(color)) {
                return true;
            }
        }
        return false;
    }

    // isNeeded(String) still used by the Computer, might have problems
    // activating Snow abilities
    /**
     * <p>
     * isNeeded.
     * </p>
     * 
     * @param mana
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean isNeeded(String mana) {
        if (this.manaNeededToAvoidNegativeEffect.size() != 0) {
            for (final String s : this.manaNeededToAvoidNegativeEffect) {
                if ((s.equalsIgnoreCase(mana) || s.substring(0, 1).equalsIgnoreCase(mana))
                        && !this.manaPaidToAvoidNegativeEffect.contains(mana)) {
                    return true;
                }
            }
        }
        if (mana.length() > 1) {
            mana = MagicColor.toShortString(mana);
        }
        for (ManaCostShard shard : unpaidShards.keySet()) {
            if (canBePaidWith(shard, mana)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * isNeeded.
     * </p>
     * 
     * @param paid
     *            a {@link forge.card.mana.Mana} object.
     * @return a boolean.
     */
    public final boolean isNeeded(final Mana paid) {
        for (ManaCostShard shard : unpaidShards.keySet()) {

            if (canBePaidWith(shard, paid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * isPaid.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isPaid() {
        return unpaidShards.isEmpty();
    } // isPaid()

    /**
     * <p>
     * payMana.
     * </p>
     * 
     * @param mana
     *            a {@link forge.card.mana.Mana} object.
     * @return a boolean.
     */
    public final boolean payMana(final Mana mana) {
        return this.addMana(mana);
    }

    /**
     * <p>
     * payMultipleMana.
     * </p>
     * 
     * @param mana
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final void payMultipleMana(String mana) {
        String[] manas = mana.split(" ");
        for (String manaPart : manas) {
            if (manaPart.matches("[0-9]+")) {
                final int amount = Integer.parseInt(manaPart);
                for (int i = 0; i < amount; i++) {
                    this.payMana(Constant.Color.COLORLESS);
                }
            } else {
                this.payMana(forge.card.MagicColor.toLongString(manaPart));
            }
        }
    }

    /**
     * <p>
     * payMana.
     * </p>
     * 
     * @param color
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean payMana(String color) {
        if (this.manaNeededToAvoidNegativeEffect.contains(color) && !this.manaPaidToAvoidNegativeEffect.contains(color)) {
            this.manaPaidToAvoidNegativeEffect.add(color);
        }
        color = MagicColor.toShortString(color);
        return this.addMana(color);
    }

    /**
     * <p>
     * increaseColorlessMana.
     * </p>
     * 
     * @param manaToAdd
     *            a int.
     */
    public final void increaseColorlessMana(final int manaToAdd) {
        increaseShard(ManaCostShard.COLORLESS, manaToAdd);
    }

    public final void increaseShard(final ManaCostShard shard, final int toAdd) {
        if (toAdd <= 0) {
            return;
        }

        Integer cnt = unpaidShards.get(shard);
        unpaidShards.put(shard, Integer.valueOf(cnt == null || cnt == 0 ? toAdd : toAdd + cnt));
    }

    /**
     * <p>
     * decreaseColorlessMana
     * </p>
     * .
     * 
     * @param manaToSubtract
     *            an int. The amount of colorless mana to subtract from the
     *            cost.Used by Delve.
     */
    public final void decreaseColorlessMana(final int manaToSubtract) {
        decreaseShard(ManaCostShard.COLORLESS, manaToSubtract);
    }

    public final void decreaseShard(final ManaCostShard shard, final int manaToSubtract) {
        if (manaToSubtract <= 0) {
            return;
        }

        Integer genericCnt = unpaidShards.get(shard);
        if (null == genericCnt || genericCnt - manaToSubtract <= 0) {
            unpaidShards.remove(shard);
        } else {
            unpaidShards.put(shard, Integer.valueOf(genericCnt - manaToSubtract));
        }
    }

    /**
     * <p>
     * getColorlessManaAmount
     * </p>
     * Returns how much colorless mana must be paid to pay the cost.Used by
     * Delve AI.
     * 
     * @return an int.
     */
    public final int getColorlessManaAmount() {
        Integer genericCnt = unpaidShards.get(ManaCostShard.COLORLESS);
        return genericCnt == null ? 0 : genericCnt;
    }

    /**
     * <p>
     * addMana.
     * </p>
     * 
     * @param mana
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean addMana(final String mana) {
        if (!this.isNeeded(mana)) {
            System.out.println("ManaCost : addMana() error, mana not needed - " + mana);
            //throw new RuntimeException("ManaCost : addMana() error, mana not needed - " + mana);
        }
        byte colorMask = MagicColor.fromName(mana);
        ManaCostShard choice = null;
        for (ManaCostShard toPay : unpaidShards.keySet()) {
            if (canBePaidWith(toPay, mana)) {
                // if m is a better to pay than choice
                if (choice == null) {
                    choice = toPay;
                    continue;
                }
                if (isFirstChoiceBetter(toPay, choice, colorMask)) {
                    choice = toPay;
                }
            }
        } // for
        if (choice == null) {
            return false;
        }

        decreaseShard(choice, 1);
        if (choice.isOr2Colorless() && choice.getColorMask() != colorMask ) {
            this.increaseColorlessMana(1);
        }

        if (!mana.equals(Constant.Color.COLORLESS)) {
            if (this.sunburstMap.containsKey(mana)) {
                this.sunburstMap.put(mana, this.sunburstMap.get(mana) + 1);
            } else {
                this.sunburstMap.put(mana, 1);
            }
        }
        return true;
    }

    private boolean isFirstChoiceBetter(ManaCostShard s1, ManaCostShard s2, byte b) {
        return getPayPriority(s1, b) > getPayPriority(s2, b);
    }

    private int getPayPriority(ManaCostShard bill, byte paymentColor) {
        if (bill == ManaCostShard.COLORLESS) {
            return 0;
        }

        if (bill.isMonoColor()) {
            if (bill.isOr2Colorless()) {
                return bill.getColorMask() == paymentColor ? 9 : 4;
            }
            if (!bill.isPhyrexian()) {
                return 10;
            }
            return 8;
        }

        return 5;
    }

    private boolean canBePaidWith(ManaCostShard shard, Mana mana) {
        if (shard.isSnow() && mana.isSnow()) {
            return true;
        }
        //System.err.println(String.format("ManaPaid: paying for %s with %s" , shard, mana));
        // debug here even more;
        return canBePaidWith(shard, MagicColor.toShortString(mana.getColor()));
    }

    private boolean canBePaidWith(ManaCostShard shard, String mana) {
        // most debug here!!
        String sShard = shard.toString();
        boolean res = "1".equals(sShard) || sShard.contains(mana) || shard.isOr2Colorless();
        //System.out.println(String.format("Str: paying for %s with %s => %d" , shard, mana, res ? 1 : 0));
        return res;
    }

    /**
     * <p>
     * addMana.
     * </p>
     * 
     * @param mana
     *            a {@link forge.card.mana.Mana} object.
     * @return a boolean.
     */
    public final boolean addMana(final Mana mana) {
        if (!this.isNeeded(mana)) {
            throw new RuntimeException("ManaCost : addMana() error, mana not needed - " + mana);
        }

        ManaCostShard choice = null;
        for (ManaCostShard toPay : unpaidShards.keySet()) {
            if (canBePaidWith(toPay, mana)) {
                // if m is a better to pay than choice
                if (choice == null) {
                    choice = toPay;
                    continue;
                }
                if (isFirstChoiceBetter(toPay, choice, mana.getColorCode())) {
                    choice = toPay;
                }
            }
        } // for
        if (choice == null) {
            return false;
        }

        String manaColor = mana.getColor();

        decreaseShard(choice, 1);
        if (choice.isOr2Colorless() && choice.getColorMask() != mana.getColorCode() ) {
            this.increaseColorlessMana(1);
        }

        if (!mana.isColor(Constant.Color.COLORLESS)) {
            if (this.sunburstMap.containsKey(manaColor)) {
                this.sunburstMap.put(manaColor, this.sunburstMap.get(manaColor) + 1);
            } else {
                this.sunburstMap.put(manaColor, 1);
            }
        }
        return true;
    }

    public final void combineManaCost(final ManaCost extra) {
        for (ManaCostShard shard : extra.getShards()) {
            if (shard == ManaCostShard.X) {
                cntX++;
            } else {
                increaseShard(shard, 1);
            }
        }
        increaseColorlessMana(extra.getGenericCost());
    }

    public final void combineManaCost(final String extra) {
        combineManaCost(new ManaCost(new ManaCostParser(extra)));
    }

    /**
     * To string.
     * 
     * @param addX
     *            the add x
     * @return the string
     */
    public final String toString(final boolean addX) {
        // Boolean addX used to add Xs into the returned value
        final StringBuilder sb = new StringBuilder();

        if (addX) {
            for (int i = 0; i < this.getXcounter(); i++) {
                sb.append("X").append(" ");
            }
        }

        int nGeneric = getColorlessManaAmount();
        if (nGeneric > 0) {
            sb.append(nGeneric).append(" ");
        }

        for (Entry<ManaCostShard, Integer> s : unpaidShards.entrySet()) {
            if (s.getKey() == ManaCostShard.COLORLESS) {
                continue;
            }
            for (int i = 0; i < s.getValue(); i++) {
                sb.append(s.getKey().toString()).append(" ");
            }
        }

        final String str = sb.toString().trim();

        if (str.equals("")) {
            return "0";
        }

        return str;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return this.toString(true);
    }

    /**
     * <p>
     * getConvertedManaCost.
     * </p>
     * 
     * @return a int.
     */
    public final int getConvertedManaCost() {
        int cmc = 0;

        for (final Entry<ManaCostShard, Integer> s : this.unpaidShards.entrySet()) {
            cmc += s.getKey().getCmc() * s.getValue();
        }
        return cmc;
    }
    
    public ManaCost toManaCost() {
        return new ManaCost(new ManaCostBeingPaidIterator());
    }

    /**
     * <p>
     * Getter for the field <code>xcounter</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getXcounter() {
        return cntX;
    }

    /**
     * <p>
     * removeColorlessMana.
     * </p>
     * 
     * @since 1.0.15
     */
    public final void removeColorlessMana() {
        unpaidShards.remove(ManaCostShard.COLORLESS);
    }

    /**
     * Sets the mana needed to avoid negative effect.
     * 
     * @param manaCol
     *            the new mana needed to avoid negative effect
     */
    public final void setManaNeededToAvoidNegativeEffect(final String[] manaCol) {
        for (final String s : manaCol) {
            this.manaNeededToAvoidNegativeEffect.add(s);
        }
    }

    /**
     * Gets the mana needed to avoid negative effect.
     * 
     * @return the mana needed to avoid negative effect
     */
    public final ArrayList<String> getManaNeededToAvoidNegativeEffect() {
        return this.manaNeededToAvoidNegativeEffect;
    }

    /**
     * Gets the mana paid so far to avoid negative effect.
     * 
     * @return the mana paid to avoid negative effect
     */
    public final ArrayList<String> getManaPaidToAvoidNegativeEffect() {
        return this.manaPaidToAvoidNegativeEffect;
    }

    public final void applySpellCostChange(final SpellAbility sa) {
        final GameState game = sa.getActivatingPlayer().getGame();
        // Beached
        final Card originalCard = sa.getSourceCard();
        final SpellAbility spell = sa;


        if (sa.isXCost() && !originalCard.isCopiedSpell()) {
            originalCard.setXManaCostPaid(0);
        }

        if (sa.isTrigger()) {
            return;
        }

        if (spell.isSpell()) {
            if (spell.isDelve()) {
                final Player pc = originalCard.getController();
                final List<Card> mutableGrave = Lists.newArrayList(pc.getZone(ZoneType.Graveyard).getCards());
                final List<Card> toExile = pc.getController().chooseCardsToDelve(this.getColorlessManaAmount(), mutableGrave);
                for (final Card c : toExile) {
                    pc.getGame().getAction().exile(c);
                    decreaseColorlessMana(1);
                }
            } else if (spell.getSourceCard().hasKeyword("Convoke")) {
                adjustCostByConvoke(sa, spell);
            }
        } // isSpell

        List<Card> cardsOnBattlefield = Lists.newArrayList(game.getCardsIn(ZoneType.Battlefield));
        cardsOnBattlefield.addAll(game.getCardsIn(ZoneType.Stack));
        cardsOnBattlefield.addAll(game.getCardsIn(ZoneType.Command));
        if (!cardsOnBattlefield.contains(originalCard)) {
            cardsOnBattlefield.add(originalCard);
        }
        final ArrayList<StaticAbility> raiseAbilities = new ArrayList<StaticAbility>();
        final ArrayList<StaticAbility> reduceAbilities = new ArrayList<StaticAbility>();
        final ArrayList<StaticAbility> setAbilities = new ArrayList<StaticAbility>();

        // Sort abilities to apply them in proper order
        for (Card c : cardsOnBattlefield) {
            final ArrayList<StaticAbility> staticAbilities = c.getStaticAbilities();
            for (final StaticAbility stAb : staticAbilities) {
                if (stAb.getMapParams().get("Mode").equals("RaiseCost")) {
                    raiseAbilities.add(stAb);
                } else if (stAb.getMapParams().get("Mode").equals("ReduceCost")) {
                    reduceAbilities.add(stAb);
                } else if (stAb.getMapParams().get("Mode").equals("SetCost")) {
                    setAbilities.add(stAb);
                }
            }
        }
        // Raise cost
        for (final StaticAbility stAb : raiseAbilities) {
            stAb.applyAbility("RaiseCost", spell, this);
        }

        // Reduce cost
        for (final StaticAbility stAb : reduceAbilities) {
            stAb.applyAbility("ReduceCost", spell, this);
        }

        // Set cost (only used by Trinisphere) is applied last
        for (final StaticAbility stAb : setAbilities) {
            stAb.applyAbility("SetCost", spell, this);
        }
    } // GetSpellCostChange

    private void adjustCostByConvoke(final SpellAbility sa, final SpellAbility spell) {
        
        List<Card> untappedCreats = CardLists.filter(spell.getActivatingPlayer().getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.CREATURES);
        untappedCreats = CardLists.filter(untappedCreats, CardPredicates.Presets.UNTAPPED);

        while (!untappedCreats.isEmpty() && getConvertedManaCost() > 0) {
            Card workingCard = null;
            String chosenColor = null;
            if (sa.getActivatingPlayer().isHuman()) {
                workingCard = GuiChoose.oneOrNone("Tap for Convoke? " + toString(), untappedCreats);
                if( null == workingCard )
                    break; // that means "I'm done"

                List<String> usableColors = getConvokableColors(workingCard);
                if ( !usableColors.isEmpty() ) {
                    chosenColor = usableColors.size() == 1 ? usableColors.get(0) : GuiChoose.one("Convoke for which color?", usableColors);
                } 
            } else {
                // TODO: AI to choose a creature to tap would go here
                // Probably along with deciding how many creatures to
                // tap
                
                if ( MyRandom.getRandom().nextInt(3) == 0 ) // 66% chance to chose first creature, 33% to cancel
                    workingCard = untappedCreats.get(0);
                
                if( null == workingCard ) 
                    break; // that means "I'm done"
                
                List<String> usableColors = getConvokableColors(workingCard);
                if ( !usableColors.isEmpty() ) {
                    // TODO: AI for choosing which color to convoke goes here.
                    chosenColor = usableColors.get(0);
                }

            }
            untappedCreats.remove(workingCard);


            if ( null == chosenColor )
                continue;
            else if (chosenColor.equals("colorless")) {
                decreaseColorlessMana(1);
            } else {
                decreaseShard(ManaCostShard.valueOf(MagicColor.fromName(chosenColor)), 1);
            }

            sa.addTappedForConvoke(workingCard);
        }

        // Convoked creats are tapped here with triggers
        // suppressed,
        // Then again when payment is done(In
        // InputPayManaCost.done()) with suppression cleared.
        // This is to make sure that triggers go off at the
        // right time
        // AND that you can't use mana tapabilities of convoked
        // creatures
        // to pay the convoked cost.
        for (final Card c : sa.getTappedForConvoke()) {
            c.setTapped(true);
        }

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
    private List<String> getConvokableColors(final Card cardToConvoke) {
        final ArrayList<String> usableColors = new ArrayList<String>();
    
        if (getColorlessManaAmount() > 0) {
            usableColors.add("colorless");
        }
        for (final CardColor col : cardToConvoke.getColor()) {
            for (final String strCol : col.toStringList()) {
                if (strCol.equals("colorless")) {
                    continue;
                }
                if (toString().contains(MagicColor.toShortString(strCol))) {
                    usableColors.add(strCol.toString());
                }
            }
        }
    
        return usableColors;
    }

    public String getSourceRestriction() {
        return sourceRestriction;
    }    
}
