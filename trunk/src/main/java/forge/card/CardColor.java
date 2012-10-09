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

import forge.Constant;
import forge.util.BinaryUtil;

/**
 * <p>CardColor class.</p>
 * <p>Represents a set of any number of colors out of 5 possible in the game</p>
 * <p><i>This class is immutable, do not generate any setters here</i></p>
 * 
 * @author Max mtg
 * @version $Id: CardColor.java 9708 2011-08-09 19:34:12Z jendave $
 * 
 * 
 */
public final class CardColor implements Comparable<CardColor> {

    public static final byte WHITE = 1 << 1;
    public static final byte BLUE = 1 << 2;
    public static final byte BLACK = 1 << 3;
    public static final byte RED = 1 << 4;
    public static final byte GREEN = 1 << 5;

    private final byte myColor;
    private final int orderWeight;

    private static CardColor[] allColors = new CardColor[32];
    private static final CardColor noColor = new CardColor();

    // TODO: some cards state "CardName is %color%" (e.g. pacts of...) - fix
    // this later
    /**
     * Instantiates a new card color.
     * 
     * @param mana
     *            the mana
     */
    private CardColor() {
        myColor = 0;
        orderWeight = -1;
    }

    private CardColor(final byte mask) {
        this.myColor = mask;
        this.orderWeight = this.getOrderWeight();

    }

    public static CardColor fromMask(final int mask) {
        int mask32 = (mask & 0x3E) >> 1;
        if (allColors[mask32] == null) {
            allColors[mask32] = new CardColor((byte) mask);
        }
        return allColors[mask32];
    }

    public static CardColor fromNames(String... colors) {
        byte mask = 0;
        for (String s : colors) {
            if (s.equalsIgnoreCase(Constant.Color.WHITE) || s.equalsIgnoreCase("w")) {
                mask |= WHITE;
            }
            if (s.equalsIgnoreCase(Constant.Color.BLUE) || s.equalsIgnoreCase("u")) {
                mask |= BLUE;
            }
            if (s.equalsIgnoreCase(Constant.Color.BLACK) || s.equalsIgnoreCase("b")) {
                mask |= BLACK;
            }
            if (s.equalsIgnoreCase(Constant.Color.RED) || s.equalsIgnoreCase("r")) {
                mask |= RED;
            }
            if (s.equalsIgnoreCase(Constant.Color.GREEN) || s.equalsIgnoreCase("g")) {
                mask |= GREEN;
            }
        }
        return fromMask(mask);
    }

    public static CardColor fromManaCost(final CardManaCost mana) {
        return fromMask(mana.getColorProfile());
    }

    /**
     * Checks for any color.
     * 
     * @param colormask
     *            the colormask
     * @return true, if successful
     */
    public boolean hasAnyColor(final int colormask) {
        return (this.myColor & colormask) != 0;
    }

    /**
     * Checks for all colors.
     * 
     * @param colormask
     *            the colormask
     * @return true, if successful
     */
    public boolean hasAllColors(final int colormask) {
        return (this.myColor & colormask) == colormask;
    }

    /** this has no other colors except defined by operand.  */
    public boolean hasNoColorsExcept(final int colormask) {
        return (this.myColor & ~colormask) == 0;
    }

    /** Operand has no other colors except defined by this. */
    public boolean containsAllColorsFrom(int colorProfile) {
        return (~this.myColor & colorProfile) == 0;
    }

    /**
     * Count colors.
     * 
     * @return the int
     */
    public int countColors() {
        return BinaryUtil.bitCount(this.myColor);
    } // bit count

    // order has to be: W U B R G multi colorless - same as cards numbering
    // through a set
    /**
     * Gets the order weight.
     * 
     * @return the order weight
     */
    public int getOrderWeight() {
        return this.myColor == 0 ? 0x400 : (this.countColors() == 1 ? this.myColor : 0x200);
    }

    /**
     * Checks if is colorless.
     * 
     * @return true, if is colorless
     */
    public boolean isColorless() {
        return this.myColor == 0;
    }

    /**
     * Checks if is multicolor.
     * 
     * @return true, if is multicolor
     */
    public boolean isMulticolor() {
        return this.countColors() > 1;
    }

    /**
     * Checks if is mono color.
     * 
     * @return true, if is mono color
     */
    public boolean isMonoColor() {
        return this.countColors() == 1;
    }

    /**
     * Checks if is equal.
     * 
     * @param color
     *            the color
     * @return true, if is equal
     */
    public boolean isEqual(final byte color) {
        return color == this.myColor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final CardColor other) {
        return this.orderWeight - other.orderWeight;
    }

    // Presets
    /**
     * Checks for white.
     * 
     * @return true, if successful
     */
    public boolean hasWhite() {
        return this.hasAnyColor(CardColor.WHITE);
    }

    /**
     * Checks for blue.
     * 
     * @return true, if successful
     */
    public boolean hasBlue() {
        return this.hasAnyColor(CardColor.BLUE);
    }

    /**
     * Checks for black.
     * 
     * @return true, if successful
     */
    public boolean hasBlack() {
        return this.hasAnyColor(CardColor.BLACK);
    }

    /**
     * Checks for red.
     * 
     * @return true, if successful
     */
    public boolean hasRed() {
        return this.hasAnyColor(CardColor.RED);
    }

    /**
     * Checks for green.
     * 
     * @return true, if successful
     */
    public boolean hasGreen() {
        return this.hasAnyColor(CardColor.GREEN);
    }

    /**
     * Checks if is white.
     * 
     * @return true, if is white
     */
    public boolean isWhite() {
        return this.isEqual(CardColor.WHITE);
    }

    /**
     * Checks if is blue.
     * 
     * @return true, if is blue
     */
    public boolean isBlue() {
        return this.isEqual(CardColor.BLUE);
    }

    /**
     * Checks if is black.
     * 
     * @return true, if is black
     */
    public boolean isBlack() {
        return this.isEqual(CardColor.BLACK);
    }

    /**
     * Checks if is red.
     * 
     * @return true, if is red
     */
    public boolean isRed() {
        return this.isEqual(CardColor.RED);
    }

    /**
     * Checks if is green.
     * 
     * @return true, if is green
     */
    public boolean isGreen() {
        return this.isEqual(CardColor.GREEN);
    }

    public CardColor inverse() {
        byte mask = this.myColor;
        mask ^= (WHITE | BLUE | BLACK | GREEN | RED);
        return fromMask(mask);
    }

    public byte getColor() {
        return myColor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (this.orderWeight == -1) {
            return "n/a";
        }
        switch (this.myColor) {
        case 0:
            return Constant.Color.COLORLESS;
        case WHITE:
            return Constant.Color.WHITE;
        case BLUE:
            return Constant.Color.BLUE;
        case BLACK:
            return Constant.Color.BLACK;
        case RED:
            return Constant.Color.RED;
        case GREEN:
            return Constant.Color.GREEN;
        default:
            return "multi";
        }
    }

    /**
     * Gets the null color.
     * 
     * @return the nullColor
     */
    public static CardColor getNullColor() {
        return noColor;
    }

    /**
     * Shares color with.
     *
     * @param ccOther the cc other
     * @return true, if successful
     */
    public boolean sharesColorWith(CardColor ccOther) {
        return (this.myColor & ccOther.myColor) != 0;
    }
    
    public CardColor getOffColors(CardColor ccOther) {
        return CardColor.fromMask(~this.myColor & ccOther.myColor);
    }
}
