package forge;

import java.util.EnumSet;

import forge.card.mana.ManaCost;

// TODO Auto-generated Javadoc
/**
 * <p>
 * Color class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public enum Color {

    /** The Colorless. */
    Colorless(0),
    /** The White. */
    White(1),
    /** The Green. */
    Green(2),
    /** The Red. */
    Red(4),
    /** The Black. */
    Black(8),
    /** The Blue. */
    Blue(16);

    @SuppressWarnings("unused")
    private int flag = 0;

    /**
     * <p>
     * Constructor for Color.
     * </p>
     * 
     * @param c
     *            a int.
     */
    Color(final int c) {
        flag = c;
    }

    /**
     * <p>
     * Colorless.
     * </p>
     * 
     * @return a {@link java.util.EnumSet} object.
     */
    public static EnumSet<Color> Colorless() {
        EnumSet<Color> colors = EnumSet.of(Color.Colorless);
        return colors;
    }

    /**
     * <p>
     * ConvertStringsToColor.
     * </p>
     * 
     * @param s
     *            an array of {@link java.lang.String} objects.
     * @return a {@link java.util.EnumSet} object.
     */
    public static EnumSet<Color> ConvertStringsToColor(String[] s) {
        EnumSet<Color> colors = EnumSet.of(Color.Colorless);

        for (int i = 0; i < s.length; i++) {
            colors.add(ConvertFromString(s[i]));
        }

        if (colors.size() > 1) {
            colors.remove(Color.Colorless);
        }

        return colors;
    }

    /**
     * <p>
     * ConvertFromString.
     * </p>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     * @return a {@link forge.Color} object.
     */
    public static Color ConvertFromString(String s) {

        if (s.equals(Constant.Color.White)) {
            return Color.White;
        } else if (s.equals(Constant.Color.Green)) {
            return Color.Green;
        } else if (s.equals(Constant.Color.Red)) {
            return Color.Red;
        } else if (s.equals(Constant.Color.Black)) {
            return Color.Black;
        } else if (s.equals(Constant.Color.Blue)) {
            return Color.Blue;
        }

        return Color.Colorless;

    }

    /**
     * <p>
     * ConvertManaCostToColor.
     * </p>
     * 
     * @param m
     *            a {@link forge.card.mana.ManaCost} object.
     * @return a {@link java.util.EnumSet} object.
     */
    public static EnumSet<Color> ConvertManaCostToColor(final ManaCost m) {
        EnumSet<Color> colors = EnumSet.of(Color.Colorless);

        if (m.isColor("W")) {
            colors.add(Color.White);
        }
        if (m.isColor("G")) {
            colors.add(Color.Green);
        }
        if (m.isColor("R")) {
            colors.add(Color.Red);
        }
        if (m.isColor("B")) {
            colors.add(Color.Black);
        }
        if (m.isColor("U")) {
            colors.add(Color.Blue);
        }

        if (colors.size() > 1) {
            colors.remove(Color.Colorless);
        }

        return colors;
    }

    /**
     * <p>
     * toString.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        if (this.equals(Color.White)) {
            return Constant.Color.White;
        } else if (this.equals(Color.Green)) {
            return Constant.Color.Green;
        } else if (this.equals(Color.Red)) {
            return Constant.Color.Red;
        } else if (this.equals(Color.Black)) {
            return Constant.Color.Black;
        } else if (this.equals(Color.Blue)) {
            return Constant.Color.Blue;
        } else {
            return Constant.Color.Colorless;
        }
    }
}
