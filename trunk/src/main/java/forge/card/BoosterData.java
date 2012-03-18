package forge.card;

import forge.item.CardPrinted;
import forge.util.FileSection;
import forge.util.Lambda1;
import forge.util.Predicate;
import forge.util.StorageReaderFile;

/**
 * The Class BoosterData.
 */
public class BoosterData {
    private final String edition;
    public final String getEdition() {
        return edition;
    }

    private final int nCommon;
    private final int nUncommon;
    private final int nRare;
    private final int nSpecial;
    private final int nDoubleFaced;
    private final int nLand;
    private final int foilRate;
    private static final int CARDS_PER_BOOSTER = 15;

    // private final String landCode;
    /**
     * Instantiates a new booster data.
     * 
     * @param nC
     *            the n c
     * @param nU
     *            the n u
     * @param nR
     *            the n r
     * @param nS
     *            the n s
     * @param nDF
     *            the n df
     */
    public BoosterData(final String edition, final int nC, final int nU, final int nR, final int nS, final int nDF) {
        // if this booster has more that 10 cards, there must be a land in
        // 15th slot unless it's already taken
        this(edition, nC, nU, nR, nS, nDF, (nC + nR + nU + nS + nDF) > 10 ? BoosterData.CARDS_PER_BOOSTER - nC - nR - nU
                - nS - nDF : 0, 68);
    }

    /**
     * Instantiates a new booster data.
     * 
     * @param nC
     *            the n c
     * @param nU
     *            the n u
     * @param nR
     *            the n r
     * @param nS
     *            the n s
     * @param nDF
     *            the n df
     * @param nL
     *            the n l
     * @param oneFoilPer
     *            the one foil per
     */
    public BoosterData(final String edition0, final int nC, final int nU, final int nR, final int nS, final int nDF, final int nL,
            final int oneFoilPer) {
        this.nCommon = nC;
        this.nUncommon = nU;
        this.nRare = nR;
        this.nSpecial = nS;
        this.nDoubleFaced = nDF;
        this.nLand = nL > 0 ? nL : 0;
        this.foilRate = oneFoilPer;
        this.edition = edition0;
    }

    /**
     * Gets the common.
     * 
     * @return the common
     */
    public final int getCommon() {
        return this.nCommon;
    }

    public final Predicate<CardPrinted> getEditionFilter() {
        return CardPrinted.Predicates.printedInSets(edition);
    }

    /**
     * Gets the uncommon.
     * 
     * @return the uncommon
     */
    public final int getUncommon() {
        return this.nUncommon;
    }

    /**
     * Gets the rare.
     * 
     * @return the rare
     */
    public final int getRare() {
        return this.nRare;
    }

    /**
     * Gets the special.
     * 
     * @return the special
     */
    public final int getSpecial() {
        return this.nSpecial;
    }

    /**
     * Gets the double faced.
     * 
     * @return the double faced
     */
    public final int getDoubleFaced() {
        return this.nDoubleFaced;
    }

    /**
     * Gets the land.
     * 
     * @return the land
     */
    public final int getLand() {
        return this.nLand;
    }

    /**
     * Gets the total.
     * 
     * @return the total
     */
    public final int getTotal() {
        return this.nCommon + this.nUncommon + this.nRare + this.nSpecial + this.nDoubleFaced + this.nLand;
    }

    /**
     * Gets the foil chance.
     * 
     * @return the foil chance
     */
    public final int getFoilChance() {
        return this.foilRate;
    }

    public static final Lambda1<String, BoosterData> FN_GET_CODE = new Lambda1<String, BoosterData>() {

        @Override
        public String apply(BoosterData arg1) {
            return arg1.edition;
        }
    };

    public static final class Reader extends StorageReaderFile<BoosterData> {

        public Reader(String pathname) {
            super(pathname, BoosterData.FN_GET_CODE);
        }

        /* (non-Javadoc)
         * @see forge.util.StorageReaderFile#read(java.lang.String)
         */
        @Override
        protected BoosterData read(String line) {
            final FileSection section = FileSection.parse(line, ":", "|");
            int nC = section.getInt("Commons", 0);
            int nU = section.getInt("Uncommons", 0);
            int nR = section.getInt("Rares", 0);
            int nS = section.getInt("Special", 0);
            int nDf = section.getInt("DoubleFaced", 0);
            int nLand = section.getInt("BasicLands", 0);
            int nFoilRate = section.getInt("FoilRate", 68);
            return new BoosterData(section.get("Set"), nC, nU, nR, nS, nDf, nLand, nFoilRate);
        }
    }
}
