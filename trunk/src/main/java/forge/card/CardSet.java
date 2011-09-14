package forge.card;

import net.slightlymagic.braids.util.lambda.Lambda1;

/**
 * <p>CardSet class.</p>
 *
 * @author Forge
 * @version $Id: CardSet.java 9708 2011-08-09 19:34:12Z jendave $
 */
public final class CardSet implements Comparable<CardSet> { // immutable
    private final int index;
    private final String code;
    private final String code2;
    private final String name;
    private final BoosterData boosterData;

    public CardSet(final int index, final String name, final String code, final String code2) {
        this(index, name, code, code2, null);
    }
    public CardSet(final int index, final String name, final String code, final String code2, BoosterData booster) {
        this.code = code;
        this.code2 = code2;
        this.index = index;
        this.name = name;
        this.boosterData = booster;
    }
    
    public static final CardSet unknown = new CardSet(-1, "Undefined", "???", "??"); 

    public String getName() { return name; }
    public String getCode() { return code; }
    public String getCode2() { return code2; }
    public int getIndex() { return index; }
    
    public boolean canGenerateBooster() { return boosterData != null; }
    public BoosterData getBoosterData() { return boosterData; }
    
    public static final Lambda1<String, CardSet> fnGetName = new Lambda1<String, CardSet>() {
        @Override public String apply(final CardSet arg1) { return arg1.name; } };
    public static final Lambda1<CardSet, CardSet> fn1 = new Lambda1<CardSet, CardSet>() {
        @Override public CardSet apply(final CardSet arg1) { return arg1; } };

    @Override
    public int compareTo(final CardSet o) {
        if (o == null) { return 1; }
        return o.index - this.index;
    }

    @Override
    public int hashCode() {
        return code.hashCode() * 17 + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }

        CardSet other = (CardSet) obj;
        return other.name.equals(this.name) && this.code.equals(other.code);
    }
    
    @Override
    public String toString() {
        return this.name + " (set)";
    }
    
    public static class BoosterData {
        private final int nCommon;
        private final int nUncommon;
        private final int nRare;
        private final int nSpecial;
        private final int nLand;
        private final int foilRate;
        private final static int CARDS_PER_BOOSTER = 15;
        
        //private final String landCode;
        public BoosterData(final int nC, final int nU, final int nR, final int nS) {
            this(nC, nU, nR, nS, CARDS_PER_BOOSTER - nC - nR - nU - nS, 68);
        }

        public BoosterData(final int nC, final int nU, final int nR, final int nS, final int nL, final int oneFoilPer) {
            nCommon = nC;
            nUncommon = nU;
            nRare = nR;
            nSpecial = nS;
            nLand = nL > 0 ? nL : 0;
            foilRate = oneFoilPer;
        }
        
        public int getCommon() { return nCommon; }
        public int getUncommon() { return nUncommon; }
        public int getRare() { return nRare; }
        public int getSpecial() { return nSpecial; }
        public int getLand() { return nLand; }
        public int getFoilChance() { return foilRate; }
    }
}
