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
package forge.game.mana;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import forge.card.MagicColor;
import forge.card.mana.ManaCostShard;
import forge.game.GlobalRuleChange;
import forge.game.event.EventValueChangeType;
import forge.game.event.GameEventManaPool;
import forge.game.event.GameEventZone;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.AbilityManaPart;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

/**
 * <p>
 * ManaPool class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class ManaPool {

    private final Multimap<Byte, Mana> floatingMana = ArrayListMultimap.create();

    /** Constant <code>map</code>. */
    private final Player owner;

    /**
     * <p>
     * Constructor for ManaPool.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     */
    public ManaPool(final Player player) {
        owner = player;
        restoreColorReplacements();
    }

    public final int getAmountOfColor(final byte color) {
        Collection<Mana> ofColor = floatingMana.get(color);
        return ofColor == null ? 0 : ofColor.size();
    }

    private void addMana(final Mana mana) {
        floatingMana.put(mana.getColorCode(), mana);
        owner.getGame().fireEvent(new GameEventManaPool(owner, EventValueChangeType.Added, mana));
    }

    /**
     * <p>
     * addManaToFloating.
     * </p>
     * 
     * @param manaList
     *           a {@link java.util.ArrayList} object.
     */
    public final void add(final Iterable<Mana> manaList) {
        for (final Mana m : manaList) {
            this.addMana(m);
        }

        // check state effects replaced by checkStaticAbilities
        //owner.getGame().getAction().checkStaticAbilities();
    }

    /**
     * <p>
     * clearPool.
     * 
     * @return - the amount of mana removed this way
     * </p>
     */
    public final int clearPool(boolean isEndOfPhase) {
        // isEndOfPhase parameter: true = end of phase, false = mana drain effect
        if (this.floatingMana.isEmpty()) { return 0; }

        if (isEndOfPhase && owner.getGame().getStaticEffects().getGlobalRuleChange(GlobalRuleChange.manapoolsDontEmpty)) {
            return 0;
        }

        int numRemoved = 0;
        boolean keepGreenMana = isEndOfPhase && this.owner.hasKeyword("Green mana doesn't empty from your mana pool as steps and phases end.");

        List<Byte> keys = Lists.newArrayList(floatingMana.keySet());
        if (keepGreenMana) {
            keys.remove(Byte.valueOf(MagicColor.GREEN));
        }

        for (Byte b : keys) {
            if (isEndOfPhase && !owner.getGame().getPhaseHandler().is(PhaseType.CLEANUP)) {
                final List<Mana> pMana = new ArrayList<Mana>();
                for (final Mana mana : this.floatingMana.get(b)) {
                    if (mana.getManaAbility()!= null && mana.getManaAbility().isPersistentMana()) {
                        pMana.add(mana);
                    }
                }
                numRemoved += floatingMana.get(b).size() - pMana.size();
                floatingMana.get(b).clear();
                floatingMana.putAll(b, pMana);
            }
            else {
                numRemoved += floatingMana.get(b).size();
                floatingMana.get(b).clear();
            }
        }
        owner.getGame().fireEvent(new GameEventManaPool(owner, EventValueChangeType.Cleared, null));
        return numRemoved;
    }

    /**
     * <p>
     * getManaFrom.
     * </p>
     * 
     * @param pool
     *            a {@link java.util.ArrayList} object.
     * @param manaStr
     *            a {@link java.lang.String} object.
     * @param saBeingPaidFor
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a {@link forge.game.mana.Mana} object.
     */
    private Mana getMana(final ManaCostShard shard, final SpellAbility saBeingPaidFor, String restriction) {
        final List<Pair<Mana, Integer>> weightedOptions = selectManaToPayFor(shard, saBeingPaidFor, restriction);

        // Exclude border case
        if (weightedOptions.isEmpty()) {
            return null; // There is no matching mana in the pool
        }

        // select equal weight possibilities
        List<Mana> manaChoices = new ArrayList<Mana>();
        int bestWeight = Integer.MIN_VALUE;
        for (Pair<Mana, Integer> option : weightedOptions) {
            int thisWeight = option.getRight();
            Mana thisMana = option.getLeft();

            if (thisWeight > bestWeight) {
                manaChoices.clear();
                bestWeight = thisWeight;
            }

            if (thisWeight == bestWeight) {
                // add only distinct Mana-s
                boolean haveDuplicate = false;
                for (Mana m : manaChoices) {
                    if (m.equals(thisMana)) {
                        haveDuplicate = true;
                        break;
                    }
                }
                if (!haveDuplicate) {
                    manaChoices.add(thisMana);
                }
            }
        }

        // got an only one best option?
        if (manaChoices.size() == 1) {
            return manaChoices.get(0);
        }

        // Let them choose then
        return owner.getController().chooseManaFromPool(manaChoices);
    }

    private List<Pair<Mana, Integer>> selectManaToPayFor(final ManaCostShard shard, final SpellAbility saBeingPaidFor, String restriction) {
        final List<Pair<Mana, Integer>> weightedOptions = new ArrayList<Pair<Mana, Integer>>();
        for (final Byte manaKey : this.floatingMana.keySet()) {
            if (!canPayForShardWithColor(shard, manaKey.byteValue())) {
                continue;
            }

            for (final Mana thisMana : this.floatingMana.get(manaKey)) {
                if (thisMana.getManaAbility() != null && !thisMana.getManaAbility().meetsManaRestrictions(saBeingPaidFor)) {
                    continue;
                }

                boolean canPay = canPayForShardWithColor(shard, thisMana.getColorCode());
                if (!canPay || (shard.isSnow() && !thisMana.isSnow())) {
                    continue;
                }

                if (StringUtils.isNotBlank(restriction) && !thisMana.getSourceCard().isType(restriction)) {
                    continue;
                }

                // prefer colorless mana to spend
                int weight = thisMana.isColorless() ? 5 : 0;

                // prefer restricted mana to spend
                if (thisMana.isRestricted()) {
                    weight += 2;
                }

                // Spend non-snow mana first
                if (!thisMana.isSnow()) {
                    weight += 1;
                }

                weightedOptions.add(Pair.of(thisMana, weight));
            }
        }
        return weightedOptions;
    }

    /**
     * <p>
     * removeManaFrom.
     * </p>
     * 
     * @param pool
     *            a {@link java.util.ArrayList} object.
     * @param choice
     *            a {@link forge.game.mana.Mana} object.
     */
    private void removeMana(final Mana mana) {
        Collection<Mana> cm = floatingMana.get(mana.getColorCode());
        if (cm.remove(mana)) {
            owner.getGame().fireEvent(new GameEventManaPool(owner, EventValueChangeType.Removed, mana));
        }
    }

    public final void payManaFromPool(final SpellAbility saBeingPaidFor, final ManaCostBeingPaid manaCost, final ManaCostShard manaShard) {
        if (manaCost.isPaid()) {
            return;
        }

        // get a mana of this type from floating, bail if none available
        final Mana mana = this.getMana(manaShard, saBeingPaidFor, manaCost.getSourceRestriction());
        if (mana == null) {
            return; // no matching mana in the pool
        }
        else {
            tryPayCostWithMana(saBeingPaidFor, manaCost, mana);
        }
    }

    /**
     * <p>
     * subtractManaFromAbility.
     * </p>
     * 
     * @param saPaidFor
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param manaCost
     *            a {@link forge.game.mana.ManaCostBeingPaid} object.
     * @param saPayment
     *            a {@link forge.card.spellability.AbilityMana} object.
     * @return a {@link forge.game.mana.ManaCostBeingPaid} object.
     */
    public final void payManaFromAbility(final SpellAbility saPaidFor, ManaCostBeingPaid manaCost, final SpellAbility saPayment) {
        // Mana restriction must be checked before this method is called

        final List<SpellAbility> paidAbs = saPaidFor.getPayingManaAbilities();
        AbilityManaPart abManaPart = saPayment.getManaPartRecursive();

        paidAbs.add(saPayment); // assumes some part on the mana produced by the ability will get used
        for (final Mana mana : abManaPart.getLastManaProduced()) {
            tryPayCostWithMana(saPaidFor, manaCost, mana);
        }
    }

    private void tryPayCostWithMana(final SpellAbility sa, ManaCostBeingPaid manaCost, final Mana mana) {
        if (manaCost.isNeeded(mana, this)) {
            manaCost.payMana(mana, this);
            sa.getPayingMana().add(mana);
            this.removeMana(mana);
            if (mana.addsNoCounterMagic(sa) && sa.getSourceCard() != null) {
                sa.getSourceCard().setCanCounter(false);
            }
            if (sa.isSpell() && sa.getSourceCard() != null) {
                if (sa.getSourceCard().isCreature() && mana.addsKeywords(sa)) {
                    final long timestamp = sa.getSourceCard().getGame().getNextTimestamp();
                    sa.getSourceCard().addChangedCardKeywords(Arrays.asList(mana.getAddedKeywords().split(" & ")), new ArrayList<String>(), false, timestamp);
                }
                if (mana.addsCounters(sa)) {
                    mana.getManaAbility().createETBCounters(sa.getSourceCard());
                }
            }
        }
    }

    /**
     * <p>
     * totalMana.
     * </p>
     * 
     * @return a int.
     */
    public final int totalMana() {
        return floatingMana.values().size();
    }

    /**
     * <p>
     * clearPay.
     * </p>
     * 
     * @param ability
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param refund
     *            a boolean.
     */
    public final void clearManaPaid(final SpellAbility ability, final boolean refund) {
        final List<Mana> manaPaid = ability.getPayingMana();
        ability.getPayingManaAbilities().clear();
        // move non-undoable paying mana back to floating
        if (refund) {
            if (ability.getSourceCard() != null) {
                ability.getSourceCard().setCanCounter(true);
            }
            for (final Mana m : manaPaid) {
                this.addMana(m);
            }
        }
        manaPaid.clear();
    }

    //Account for mana part of ability when undoing it
    public boolean accountFor(final AbilityManaPart ma) {
    	if (ma == null) {
    		return false;
    	}
        if (this.floatingMana.isEmpty()) {
            return false;
        }

        final ArrayList<Mana> removeFloating = new ArrayList<Mana>();

        boolean manaNotAccountedFor = false;
        // loop over mana produced by mana ability
        for (Mana mana : ma.getLastManaProduced()) {
            Collection<Mana> poolLane = this.floatingMana.get(mana.getColorCode());

            if (poolLane != null && poolLane.contains(mana)) {
                removeFloating.add(mana);
            }
            else {
                manaNotAccountedFor = true;
                break;
            }
        }

        // When is it legitimate for all the mana not to be accountable?
        // Does this condition really indicate an bug in Forge?
        if (manaNotAccountedFor) {
            return false;
        }

        for (int k = 0; k < removeFloating.size(); k++) {
            this.removeMana(removeFloating.get(k));
        }
        return true;
    }

    public final void refundManaPaid(final SpellAbility sa) {
        // Send all mana back to your mana pool, before accounting for it.
        final List<Mana> manaPaid = sa.getPayingMana();

        // move non-undoable paying mana back to floating
        if (sa.getSourceCard() != null) {
            sa.getSourceCard().setCanCounter(true);
        }
        for (final Mana m : manaPaid) {
            this.addMana(m);
        }
        manaPaid.clear();

        List<SpellAbility> payingAbilities = sa.getPayingManaAbilities();
        for (final SpellAbility am : payingAbilities) {
            // undo paying abilities if we can
            am.undo();
        }

        for (final SpellAbility am : payingAbilities) {
            // Recursively refund abilities that were used.
            this.refundManaPaid(am);
        }

        payingAbilities.clear();

        // update battlefield of activating player - to redraw cards used to pay mana as untapped
        Player p = sa.getActivatingPlayer();
        p.getGame().fireEvent(new GameEventZone(ZoneType.Battlefield, p, EventValueChangeType.ComplexUpdate, null));
    }
    
    
    private final byte[] colorConversionMatrix = new byte[6];
    private static final byte[] identityMatrix = { MagicColor.WHITE, MagicColor.BLUE, MagicColor.BLACK, MagicColor.RED, MagicColor.GREEN, 0 };

    public void addColorReplacement(byte originalColor, byte replacementColor) {
        int rowIdx = MagicColor.getIndexOfFirstColor(originalColor);
        colorConversionMatrix[rowIdx] |= replacementColor;
    }
    
    public void restoreColorReplacements() {
        for(int i = 0; i < colorConversionMatrix.length; i++)
            colorConversionMatrix[i] = identityMatrix[i];
    }

    public byte getPossibleColorUses(byte color) {
        int rowIdx = MagicColor.getIndexOfFirstColor(color);
        return colorConversionMatrix[rowIdx < 0 ? identityMatrix.length - 1 : rowIdx];
    }

    public boolean canPayForShardWithColor(ManaCostShard shard, byte color) {
        byte line = getPossibleColorUses(color);
        for(int i = 0; i < MagicColor.NUMBER_OR_COLORS; i++) {
            byte outColor = MagicColor.WUBRG[i];
            if (( line & outColor) != 0  && shard.canBePaidWithManaOfColor(outColor))
                return true;
        }
        return shard.canBePaidWithManaOfColor((byte)0);
    }
}
