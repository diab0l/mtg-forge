package forge.card.mana;

import forge.gui.input.Input_PayManaCostUtil;

//handles mana costs like 2/R or 2/B
//for cards like Flame Javelin (Shadowmoor)
/**
 * <p>
 * Mana_PartSplit class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Mana_PartSplit extends Mana_Part {
    private Mana_Part manaPart = null;
    private String originalCost = "";

    /**
     * <p>
     * Constructor for Mana_PartSplit.
     * </p>
     * 
     * @param manaCost
     *            a {@link java.lang.String} object.
     */
    public Mana_PartSplit(final String manaCost) {
        // is mana cost like "2/R"
        if (manaCost.length() != 3) {
            throw new RuntimeException("Mana_PartSplit : constructor() error, bad mana cost parameter - " + manaCost);
        }

        this.originalCost = manaCost;
    }

    /**
     * <p>
     * isFirstTime.
     * </p>
     * 
     * @return a boolean.
     */
    private boolean isFirstTime() {
        return this.manaPart == null;
    }

    /**
     * <p>
     * setup.
     * </p>
     * 
     * @param manaToPay
     *            a {@link java.lang.String} object.
     */
    private void setup(final String manaToPay) {
        // get R out of "2/R"
        final String color = this.originalCost.substring(2, 3);

        // is manaToPay the one color we want or do we
        // treat it like colorless?
        // if originalCost is 2/R and is color W (treated like colorless)
        // or R? if W use Mana_PartColorless, if R use Mana_PartColor
        // does manaToPay contain color?
        if (0 <= manaToPay.indexOf(color)) {
            this.manaPart = new Mana_PartColor(color);
        } else {
            // get 2 out of "2/R"
            this.manaPart = new Mana_PartColorless(this.originalCost.substring(0, 1));
        }
    } // setup()

    /** {@inheritDoc} */
    @Override
    public final void reduce(final String mana) {
        if (this.isFirstTime()) {
            this.setup(mana);
        }

        this.manaPart.reduce(mana);
    }

    /** {@inheritDoc} */
    @Override
    public final void reduce(final Mana mana) {
        if (this.isFirstTime()) {
            this.setup(Input_PayManaCostUtil.getShortColorString(mana.getColor()));
        }

        this.manaPart.reduce(mana);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isNeeded(final String mana) {
        if (this.isFirstTime()) {
            // always true because any mana can pay the colorless part of 2/G
            return true;
        }

        return this.manaPart.isNeeded(mana);
    } // isNeeded()

    /** {@inheritDoc} */
    @Override
    public final boolean isNeeded(final Mana mana) {
        if (this.isFirstTime()) {
            // always true because any mana can pay the colorless part of 2/G
            return true;
        }

        return this.manaPart.isNeeded(mana);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isColor(final String mana) {
        // ManaPart method
        final String mp = this.toString();
        return mp.indexOf(mana) != -1;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isColor(final Mana mana) {
        final String color = Input_PayManaCostUtil.getShortColorString(mana.getColor());
        final String mp = this.toString();
        return mp.indexOf(color) != -1;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEasierToPay(final Mana_Part mp) {
        if (mp instanceof Mana_PartColorless) {
            return false;
        }
        if (!this.isFirstTime()) {
            return true;
        }
        return this.toString().length() >= mp.toString().length();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        if (this.isFirstTime()) {
            return this.originalCost;
        }

        return this.manaPart.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isPaid() {
        if (this.isFirstTime()) {
            return false;
        }

        return this.manaPart.isPaid();
    }

    /** {@inheritDoc} */
    @Override
    public final int getConvertedManaCost() {
        // grab the colorless portion of the split cost (usually 2, but possibly
        // more later)
        return Integer.parseInt(this.originalCost.substring(0, 1));
    }
}
