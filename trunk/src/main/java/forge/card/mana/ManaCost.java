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
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import forge.Constant;
import forge.gui.input.InputPayManaCostUtil;

/**
 * <p>
 * ManaCost class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class ManaCost {
    // holds Mana_Part objects
    // ManaPartColor is stored before ManaPartColorless
    private final ArrayList<Object> manaPart;
    private final HashMap<String, Integer> sunburstMap = new HashMap<String, Integer>();
    private int xcounter = 0;
    private final ArrayList<String> manaNeededToAvoidNegativeEffect = new ArrayList<String>();
    private final ArrayList<String> manaPaidToAvoidNegativeEffect = new ArrayList<String>();

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
    public ManaCost(String manaCost) {
        if (manaCost.equals("") || manaCost.equals("C")) {
            manaCost = "0";
        }

        while (manaCost.contains("X")) {
            if (manaCost.length() < 2) {
                manaCost = "0";
            } else {
                manaCost = manaCost.replaceFirst("X ", "");
            }
            this.setXcounter(this.getXcounter() + 1);
        }
        this.manaPart = this.split(manaCost);
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
    private ArrayList<ManaPartPhyrexian> getUnpaidPhyrexianMana() {
        final ArrayList<ManaPartPhyrexian> res = new ArrayList<ManaPartPhyrexian>();
        for (final Object o : this.manaPart) {
            if (o instanceof ManaPartPhyrexian) {
                final ManaPartPhyrexian phy = (ManaPartPhyrexian) o;

                if (!phy.isPaid()) {
                    res.add(phy);
                }
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
        for (final Object o : this.manaPart) {
            if (o instanceof ManaPartPhyrexian) {
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
        final ArrayList<ManaPartPhyrexian> phy = this.getUnpaidPhyrexianMana();

        if (phy.size() > 0) {
            phy.get(0).payLife();

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
        for (final Object s : this.manaPart) {
            if (s.toString().contains(color)) {
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
            mana = InputPayManaCostUtil.getShortColorString(mana);
        }
        ManaPart m;
        for (int i = 0; i < this.manaPart.size(); i++) {
            m = (ManaPart) this.manaPart.get(i);
            if (m.isNeeded(mana)) {
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
     * @param mana
     *            a {@link forge.card.mana.Mana} object.
     * @return a boolean.
     */
    public final boolean isNeeded(final Mana mana) {
        ManaPart m;
        for (int i = 0; i < this.manaPart.size(); i++) {
            m = (ManaPart) this.manaPart.get(i);
            if (m.isNeeded(mana)) {
                return true;
            }
            if ((m instanceof ManaPartSnow) && mana.isSnow()) {
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
        ManaPart m;
        for (int i = 0; i < this.manaPart.size(); i++) {
            m = (ManaPart) this.manaPart.get(i);
            if (!m.isPaid()) {
                return false;
            }
        }
        return true;
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
        color = InputPayManaCostUtil.getShortColorString(color);
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
        if (manaToAdd <= 0) {
            return;
        }

        ManaPart m;
        for (int i = 0; i < this.manaPart.size(); i++) {
            m = (ManaPart) this.manaPart.get(i);
            if (m instanceof ManaPartColorless) {
                ((ManaPartColorless) m).addToManaNeeded(manaToAdd);
                return;
            }
        }
        this.manaPart.add(new ManaPartColorless(manaToAdd));
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
        if (manaToSubtract <= 0) {
            return;
        }

        ManaPart m;
        for (int i = 0; i < this.manaPart.size(); i++) {
            m = (ManaPart) this.manaPart.get(i);
            if (m instanceof ManaPartColorless) {
                final int remainingColorless = ((ManaPartColorless) m).getManaNeeded() - manaToSubtract;
                if (remainingColorless <= 0) {
                    this.manaPart.remove(m);
                    break;
                } else {
                    this.manaPart.remove(m);
                    this.manaPart.add(new ManaPartColorless(remainingColorless));
                }
            }
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
        for (final Object m : this.manaPart) {
            if (m instanceof ManaPartColorless) {
                return ((ManaPartColorless) m).getManaNeeded();
            }
        }
        return 0;
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
            throw new RuntimeException("ManaCost : addMana() error, mana not needed - " + mana);
        }

        ManaPart choice = null;

        for (int i = 0; i < this.manaPart.size(); i++) {
            final ManaPart m = (ManaPart) this.manaPart.get(i);
            if (m.isNeeded(mana)) {
                // if m is a better to pay than choice
                if (choice == null) {
                    choice = m;
                    continue;
                }
                if (m.isColor(mana) && choice.isEasierToPay(m)) {
                    choice = m;
                }
            }
        } // for
        if (choice == null) {
            return false;
        }

        choice.reduce(mana);
        if (!mana.equals(Constant.Color.COLORLESS)) {
            if (this.sunburstMap.containsKey(mana)) {
                this.sunburstMap.put(mana, this.sunburstMap.get(mana) + 1);
            } else {
                this.sunburstMap.put(mana, 1);
            }
        }
        return true;
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

        ManaPart choice = null;

        for (int i = 0; i < this.manaPart.size(); i++) {
            final ManaPart m = (ManaPart) this.manaPart.get(i);
            if (m.isNeeded(mana)) {
                // if m is a better to pay than choice
                if (choice == null) {
                    choice = m;
                    continue;
                }
                if (m.isColor(mana) && choice.isEasierToPay(m)) {
                    choice = m;
                }
            }
        } // for
        if (choice == null) {
            return false;
        }

        choice.reduce(mana);
        if (!mana.isColor(Constant.Color.COLORLESS)) {
            if (this.sunburstMap.containsKey(mana.getColor())) {
                this.sunburstMap.put(mana.getColor(), this.sunburstMap.get(mana.getColor()) + 1);
            } else {
                this.sunburstMap.put(mana.getColor(), 1);
            }
        }
        return true;
    }

    /**
     * <p>
     * combineManaCost.
     * </p>
     * 
     * @param extra
     *            a {@link java.lang.String} object.
     */
    public final void combineManaCost(final String extra) {
        final ArrayList<Object> extraParts = this.split(extra);

        ManaPartColorless part = null;
        for (int i = 0; i < this.manaPart.size(); i++) {
            final Object o = this.manaPart.get(i);
            if (o instanceof ManaPartColorless) {
                part = (ManaPartColorless) o;
            }
        }
        if (part != null) {
            this.manaPart.remove(part);
        }

        while (extraParts.size() > 0) {
            final Object o = extraParts.get(0);
            if (o instanceof ManaPartColorless) {
                if (part == null) {
                    part = (ManaPartColorless) o;
                } else {
                    part.addToManaNeeded(((ManaPartColorless) o).getManaNeeded());
                }
            } else {
                this.manaPart.add(o);
            }
            extraParts.remove(o);
        }
        if (part != null) {
            this.manaPart.add(part);
        }
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
        final ArrayList<Object> list = new ArrayList<Object>(this.manaPart);
        // need to reverse everything since the colored mana is stored first
        Collections.reverse(list);

        if (addX) {
            for (int i = 0; i < this.getXcounter(); i++) {
                sb.append("X").append(" ");
            }
        }

        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toString()).append(" ");
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
        for (final Object s : this.manaPart) {
            cmc += ((ManaPart) s).getConvertedManaCost();
        }
        return cmc;
    }

    /**
     * Returns Mana cost, adjusted slightly to make colored mana parts more
     * significant. Should only be used for comparison purposes; using this
     * method allows the sort: 2 < X 2 < 1 U < U U == UR U < X U U < X X U U
     * 
     * @return The converted cost + 0.0001* the number of colored mana in the
     *         cost + 0.00001 * the number of X's in the cost
     */
    public final double getWeightedManaCost() {
        double cmc = 0;
        for (final Object s : this.manaPart) {
            cmc += ((ManaPart) s).getConvertedManaCost();
            if (s instanceof ManaPartColor) {
                cmc += 0.0001;
            }
        }

        cmc += 0.00001 * this.getXcounter();
        return cmc;
    }

    /**
     * <p>
     * split.
     * </p>
     * 
     * @param cost
     *            a {@link java.lang.String} object.
     * @return a {@link java.util.ArrayList} object.
     */
    private ArrayList<Object> split(final String cost) {
        final ArrayList<Object> list = new ArrayList<Object>();

        // handles costs like "3", "G", "GW", "10", "S"
        if ((cost.length() == 1) || (cost.length() == 2)) {
            if (Character.isDigit(cost.charAt(0))) {
                list.add(new ManaPartColorless(cost));
            } else if (cost.charAt(0) == 'S') {
                list.add(new ManaPartSnow());
            } else if (cost.charAt(0) == 'P') {
                list.add(new ManaPartPhyrexian(cost));
            } else {
                list.add(new ManaPartColor(cost));
            }
        } else {
            // handles "3 GW", "10 GW", "1 G G", "G G", "S 1"
            // all costs that have a length greater than 2 have a space
            final StringTokenizer tok = new StringTokenizer(cost);

            while (tok.hasMoreTokens()) {
                list.add(this.getManaPart(tok.nextToken()));
            }

            // ManaPartColorless needs to be added AFTER the colored mana
            // in order for isNeeded() and addMana() to work correctly
            Object o = list.get(0);
            if (o instanceof ManaPartSnow) {
                // move snow cost to the end of the list
                list.remove(0);
                list.add(o);
            }
            o = list.get(0);

            if (o instanceof ManaPartColorless) {
                // move colorless cost to the end of the list
                list.remove(0);
                list.add(o);
            }
        } // else

        return list;
    } // split()

    /**
     * <p>
     * Getter for the field <code>manaPart</code>.
     * </p>
     * 
     * @param partCost
     *            a {@link java.lang.String} object.
     * @return a {@link forge.card.mana.ManaPart} object.
     */
    private ManaPart getManaPart(final String partCost) {
        if (partCost.length() == 3) {
            return new ManaPartSplit(partCost);
        } else if (Character.isDigit(partCost.charAt(0))) {
            return new ManaPartColorless(partCost);
        } else if (partCost.equals("S")) {
            return new ManaPartSnow();
        } else if (partCost.startsWith("P")) {
            return new ManaPartPhyrexian(partCost);
        } else {
            return new ManaPartColor(partCost);
        }
    }

    /**
     * <p>
     * Setter for the field <code>xcounter</code>.
     * </p>
     * 
     * @param xcounter
     *            a int.
     */
    public final void setXcounter(final int xcounter) {
        this.xcounter = xcounter;
    }

    /**
     * <p>
     * Getter for the field <code>xcounter</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getXcounter() {
        return this.xcounter;
    }

    /**
     * <p>
     * removeColorlessMana.
     * </p>
     * 
     * @since 1.0.15
     */
    public final void removeColorlessMana() {

        for (int i = 0; i < this.manaPart.size(); i++) {
            if (this.manaPart.get(i) instanceof ManaPartColorless) {
                this.manaPart.remove(this.manaPart.get(i));
            }
        }
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
}
