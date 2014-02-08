package forge.card;

import java.util.List;

import com.google.common.collect.ImmutableList;

/** 
 * Holds byte values for each color magic has.
 *
 */
public class MagicColor {

    public static final byte COLORLESS = 0;
    public static final byte WHITE = 1 << 0;
    public static final byte BLUE = 1 << 1;
    public static final byte BLACK = 1 << 2;
    public static final byte RED = 1 << 3;
    public static final byte GREEN = 1 << 4;

    public static final byte ALL_COLORS = BLACK | BLUE | WHITE | RED | GREEN;
    public static final int NUMBER_OR_COLORS = 5;

    public static final byte[] WUBRG = new byte[] { WHITE, BLUE, BLACK, RED, GREEN }; 
    
    public static byte fromName(String s) {
        if( s == null ) return 0;
        if (s.equalsIgnoreCase(Constant.WHITE) || s.equalsIgnoreCase("w")) {
            return MagicColor.WHITE;
        }
        if (s.equalsIgnoreCase(Constant.BLUE) || s.equalsIgnoreCase("u")) {
            return MagicColor.BLUE;
        }
        if (s.equalsIgnoreCase(Constant.BLACK) || s.equalsIgnoreCase("b")) {
            return MagicColor.BLACK;
        }
        if (s.equalsIgnoreCase(Constant.RED) || s.equalsIgnoreCase("r")) {
            return MagicColor.RED;
        }
        if (s.equalsIgnoreCase(Constant.GREEN) || s.equalsIgnoreCase("g")) {
            return MagicColor.GREEN;
        }
        return 0; // colorless
    }
    
    public static byte fromName(char c) {
        switch(Character.toLowerCase(c)) {
            case 'w': return MagicColor.WHITE;
            case 'u': return MagicColor.BLUE;
            case 'b': return MagicColor.BLACK;
            case 'r': return MagicColor.RED;
            case 'g': return MagicColor.GREEN;
        }
        return 0; // unknown means 'colorless'
    }

    public static String toShortString(String color) {
        if (color.equalsIgnoreCase(Constant.SNOW)) return "S"; // compatibility
        return toShortString(fromName(color));
    }
    
    public static String toLongString(String color) {
        if (color.equalsIgnoreCase("s")) return Constant.SNOW; // compatibility
        return toLongString(fromName(color));
    }
        
    public static String toShortString(byte color) {
        switch(color){
            case GREEN: return "G";
            case RED: return "R";
            case BLUE: return "U";
            case BLACK: return "B";
            case WHITE: return "W";
            default: return "1";
        }
    }

    public static String toLongString(byte color) {
        switch(color){
            case GREEN: return Constant.GREEN ;
            case RED: return Constant.RED;
            case BLUE: return Constant.BLUE;
            case BLACK: return Constant.BLACK;
            case WHITE: return Constant.WHITE;
            default: return Constant.COLORLESS;
        }
    }
    
    public static int getIndexOfFirstColor(byte color){
        for(int i = 0; i < NUMBER_OR_COLORS; i++) {
            if ((color & WUBRG[i]) != 0)
                return i;
        }
        return -1; // colorless
    }
    
    /**
     * The Interface Color.
     */
    public static class Constant {

        /** The Black. */
        public static final String BLACK = "black";

        /** The Blue. */
        public static final String BLUE = "blue";

        /** The Green. */
        public static final String GREEN = "green";

        /** The Red. */
        public static final String RED = "red";

        /** The White. */
        public static final String WHITE = "white";

        /** The Colorless. */
        public static final String COLORLESS = "colorless";
        // color order "wubrg"

        /** The only colors. */
        public static final ImmutableList<String> ONLY_COLORS = ImmutableList.of(WHITE, BLUE, BLACK, RED, GREEN);

        /** The Snow. */
        public static final String SNOW = "snow";

        /** The Basic lands. */
        public static final List<String> BASIC_LANDS = ImmutableList.of("Plains", "Island", "Swamp", "Mountain", "Forest");
        public static final List<String> SNOW_LANDS = ImmutableList.of("Snow-Covered Plains", "Snow-Covered Island", "Snow-Covered Swamp", "Snow-Covered Mountain", "Snow-Covered Forest");
    }    
}
