package forge.ai;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import forge.card.MagicColor;
import forge.card.mana.ManaAtom;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostShard;
import forge.game.Game;
import forge.game.GameActionUtil;
import forge.game.ability.AbilityUtils;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardUtil;
import forge.game.cost.Cost;
import forge.game.cost.CostPayment;
import forge.game.mana.Mana;
import forge.game.mana.ManaCostBeingPaid;
import forge.game.mana.ManaPool;
import forge.game.player.Player;
import forge.game.replacement.ReplacementEffect;
import forge.game.spellability.AbilityManaPart;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.util.CollectionSuppliers;
import forge.util.TextUtil;
import forge.util.maps.EnumMapOfLists;
import forge.util.maps.MapOfLists;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class ComputerUtilMana {
    private final static boolean DEBUG_MANA_PAYMENT = false;

    public static boolean canPayManaCost(ManaCostBeingPaid cost, final SpellAbility sa, final Player ai) {
        cost = new ManaCostBeingPaid(cost); //check copy of cost so it doesn't modify the exist cost being paid
        return payManaCost(cost, sa, ai, true, 0, true);
    }

    public static boolean payManaCost(ManaCostBeingPaid cost, final SpellAbility sa, final Player ai) {
        return payManaCost(cost, sa, ai, false, 0, true);
    }

    public static boolean canPayManaCost(final SpellAbility sa, final Player ai, final int extraMana) {
        return payManaCost(sa, ai, true, extraMana, true);
    }

    // Does not check if mana sources can be used right now, just checks for potential chance.
    public static boolean hasEnoughManaSourcesToCast(final SpellAbility sa, final Player ai) {
        sa.setActivatingPlayer(ai);
        return payManaCost(sa, ai, true, 0, false);
    }

    public static boolean payManaCost(final Player ai, final SpellAbility sa) {
        return payManaCost(sa, ai, false, 0, true);
    }

    private static void refundMana(List<Mana> manaSpent, Player ai, SpellAbility sa) {
        if (sa.getHostCard() != null) {
            sa.getHostCard().setCanCounter(true);
        }
        for (final Mana m : manaSpent) {
            ai.getManaPool().addMana(m);
        }
        manaSpent.clear();
    }
    

    /**
     * <p>
     * payManaCost.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param ai
     *            a {@link forge.game.player.Player} object.
     * @param test
     *            (is for canPayCost, if true does not change the game state)
     * @param extraMana
     *            a int.
     * @param checkPlayable
     *            should we check if playable? use for hypothetical "can AI play this"
     * @return a boolean.
     * @since 1.0.15
     */
    private static boolean payManaCost(final SpellAbility sa, final Player ai, final boolean test, final int extraMana, boolean checkPlayable) {
        ManaCostBeingPaid cost = ComputerUtilMana.calculateManaCost(sa, test, extraMana);
        return payManaCost(cost, sa, ai, test, extraMana, checkPlayable);
    }

    private static boolean payManaCost(final ManaCostBeingPaid cost, final SpellAbility sa, final Player ai, final boolean test, final int extraMana, boolean checkPlayable) {
        adjustManaCostToAvoidNegEffects(cost, sa.getHostCard(), ai);
        List<Mana> manaSpentToPay = test ? new ArrayList<Mana>() : sa.getPayingMana();
        
        final ManaPool manapool = ai.getManaPool();
        List<ManaCostShard> unpaidShards = cost.getUnpaidShards();
        Collections.sort(unpaidShards); // most difficult shards must come first
        for (ManaCostShard part : unpaidShards) {
            if (part != ManaCostShard.X) {
                if (cost.isPaid()) {
                    continue;
                }

                // get a mana of this type from floating, bail if none available
                final Mana mana = getMana(ai, part, sa, cost.getSourceRestriction());
                if (mana == null) {
                    continue; // no matching mana in the pool
                }
                else {
                    if (ai.getManaPool().tryPayCostWithMana(sa, cost, mana)) {
                        manaSpentToPay.add(mana);
                    }
                }
            }
        }

        if (cost.isPaid()) {
            // refund any mana taken from mana pool when test
            if(test)
                refundMana(manaSpentToPay, ai, sa);

            handleOfferingsAI(sa, test, cost.isPaid());
            return true;
        }

        // arrange all mana abilities by color produced.
        final Multimap<Integer, SpellAbility> manaAbilityMap = ComputerUtilMana.groupSourcesByManaColor(ai, checkPlayable);
        if (manaAbilityMap.isEmpty()) {
            refundMana(manaSpentToPay, ai, sa);

            handleOfferingsAI(sa, test, cost.isPaid());
            return false;
        }

        if (DEBUG_MANA_PAYMENT) {
            System.out.println("DEBUG_MANA_PAYMENT: manaAbilityMap = " + manaAbilityMap);
        }

        // select which abilities may be used for each shard
        MapOfLists<ManaCostShard, SpellAbility> sourcesForShards = ComputerUtilMana.groupAndOrderToPayShards(ai, manaAbilityMap, cost);

//        if (DEBUG_MANA_PAYMENT) {
//            System.out.println((test ? "test -- " : "PROD -- ") + FThreads.debugGetStackTraceItem(5, true));
//            for (Entry<ManaCostShard, Collection<SpellAbility>> src : sourcesForShards.entrySet()) {
//                System.out.println("\t" +src.getKey() + " : " + src.getValue().size() + " source(s)");
//                for (SpellAbility sss : src.getValue()) {
//                    System.out.printf("\t\t%s - %s%n", sss.getHostCard(), sss);
//                }
//            }
//        }

        List<String> paymentPlan = new ArrayList<String>();

        ManaCostShard toPay = null;
        // Loop over mana needed
        while (!cost.isPaid()) {
            toPay = getNextShardToPay(cost, sourcesForShards);

            Collection<SpellAbility> saList = sourcesForShards.get(toPay);
            SpellAbility saPayment = null;
            if (saList != null) {
                for (final SpellAbility ma : saList) {
                    if (ma.getHostCard() == sa.getHostCard()) {
                        continue;
                    }

                    final String typeRes = cost.getSourceRestriction();
                    if (StringUtils.isNotBlank(typeRes) && !ma.getHostCard().isType(typeRes)) {
                        continue;
                    }

                    if (canPayShardWithSpellAbility(toPay, ai, ma, sa, checkPlayable || !test)) {
                        saPayment = ma;
                        break;
                    }
                }
            } else {
            	break;
            }

            if (DEBUG_MANA_PAYMENT) {
                paymentPlan.add(String.format("%s : (%s) %s", toPay, saPayment == null ? "LIFE" : saPayment.getHostCard(), saPayment));
            }

            if (saPayment == null) {
                if (!toPay.isPhyrexian() || !ai.canPayLife(2)) {
                    break; // cannot pay
                }

                cost.payPhyrexian();
                if (!test) {
                    ai.payLife(2, sa.getHostCard());
                }
                continue;
            }

            setExpressColorChoice(sa, ai, cost, toPay, saPayment);

            if (test) {
                String manaProduced = toPay.isSnow() ? "S" : GameActionUtil.generatedMana(saPayment);
                manaProduced = AbilityManaPart.applyManaReplacement(saPayment, manaProduced);
                //System.out.println(manaProduced);
                payMultipleMana(cost, manaProduced, ai);

                // remove from available lists
                for (Collection<SpellAbility> kv : sourcesForShards.values()) {
                    Iterator<SpellAbility> itSa = kv.iterator();
                    while (itSa.hasNext()) {
                        SpellAbility srcSa = itSa.next();
                        if (srcSa.getHostCard().equals(saPayment.getHostCard())) {
                            itSa.remove();
                        }
                    }
                }
            }
            else {
                if (saPayment.getPayCosts() != null) {
                    final CostPayment pay = new CostPayment(saPayment.getPayCosts(), saPayment);
                    if (!pay.payComputerCosts(new AiCostDecision(ai, saPayment))) {
                    	saList.remove(saPayment);
                        continue;
                    }
                }
                else {
                    System.err.println("Ability " + saPayment + " from " + saPayment.getHostCard() + "  had NULL as payCost");
                    saPayment.getHostCard().tap();
                }

                ai.getGame().getStack().addAndUnfreeze(saPayment);
                // subtract mana from mana pool
                manapool.payManaFromAbility(sa, cost, saPayment);

                // no need to remove abilities from resource map,
                // once their costs are paid and consume resources, they can not be used again
            }
        }

        handleOfferingsAI(sa, test, cost.isPaid());

//        if (DEBUG_MANA_PAYMENT) {
//            System.err.printf("%s > [%s] payment has %s (%s +%d) for (%s) %s:%n\t%s%n%n",
//                    FThreads.debugGetCurrThreadId(), test ? "test" : "PROD", cost.isPaid() ? "*PAID*" : "failed", originalCost,
//                    extraMana, sa.getHostCard(), sa.toUnsuppressedString(), StringUtils.join(paymentPlan, "\n\t"));
//        }

        if (!cost.isPaid()) {
            refundMana(manaSpentToPay, ai, sa);
            if (test) {
                return false;
            }
            else {
                System.out.println("ComputerUtil : payManaCost() cost was not paid for " + sa.getHostCard().getName() + ". Didn't find what to pay for " + toPay);
                return false;
            }
        }

        if (test)
            refundMana(manaSpentToPay, ai, sa);
        
        sa.getHostCard().setColorsPaid(cost.getColorsPaid());
        // if (sa instanceof Spell_Permanent) // should probably add this
        sa.getHostCard().setSunburstValue(cost.getSunburst());
        return true;
    } // payManaCost()

    /**
     * <p>
     * getManaFrom.
     * </p>
     *
     * @param saBeingPaidFor
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a {@link forge.game.mana.Mana} object.
     */
    private static Mana getMana(final Player ai, final ManaCostShard shard, final SpellAbility saBeingPaidFor, String restriction) {
        final List<Pair<Mana, Integer>> weightedOptions = selectManaToPayFor(ai.getManaPool(), shard, saBeingPaidFor, restriction);

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
        return ai.getController().chooseManaFromPool(manaChoices);
    }

    private static List<Pair<Mana, Integer>> selectManaToPayFor(final ManaPool manapool, final ManaCostShard shard, final SpellAbility saBeingPaidFor, String restriction) {
        final List<Pair<Mana, Integer>> weightedOptions = new ArrayList<Pair<Mana, Integer>>();
        for (final Mana thisMana : manapool) {
            if (!manapool.canPayForShardWithColor(shard, thisMana.getColor())) {
                continue;
            }

            if (thisMana.getManaAbility() != null && !thisMana.getManaAbility().meetsManaRestrictions(saBeingPaidFor)) {
                continue;
            }

            boolean canPay = manapool.canPayForShardWithColor(shard, thisMana.getColor());
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
        return weightedOptions;
    }
    
    private static void setExpressColorChoice(final SpellAbility sa, final Player ai, ManaCostBeingPaid cost,
            ManaCostShard toPay, SpellAbility saPayment) {

        AbilityManaPart m = saPayment.getManaPart();
        if (m.isComboMana())
            getComboManaChoice(ai, saPayment, sa, cost);
        else if (saPayment.getApi() == ApiType.ManaReflected) {
            System.out.println("Evaluate reflected mana of: " + saPayment.getHostCard());
            Set<String> reflected = CardUtil.getReflectableManaColors(saPayment);

            for (byte c : MagicColor.WUBRG) {
                if (ai.getManaPool().canPayForShardWithColor(toPay, c) && reflected.contains(MagicColor.toLongString(c))) {
                    m.setExpressChoice(MagicColor.toShortString(c));
                    return;
                }
            }
        } else if (m.isAnyMana()) {
            byte colorChoice = 0;
            if (toPay.isOr2Colorless())
                colorChoice = toPay.getColorMask();
            else {
                for (byte c : MagicColor.WUBRG) {
                    if (ai.getManaPool().canPayForShardWithColor(toPay, c)) {
                        colorChoice = c;
                        break;
                    }
                }
            }
            m.setExpressChoice(MagicColor.toShortString(colorChoice));
        }
    }

    private static boolean canPayShardWithSpellAbility(ManaCostShard toPay, Player ai, SpellAbility ma, SpellAbility sa, boolean checkCosts) {
        final Card sourceCard = ma.getHostCard();

        if (toPay.isSnow() && !sourceCard.isSnow()) { return false; }

        AbilityManaPart m = ma.getManaPart();
        if (!m.meetsManaRestrictions(sa)) {
            return false;
        }

        if (checkCosts) {
            // Check if AI can still play this mana ability
            ma.setActivatingPlayer(ai);
            if (ma.getPayCosts() != null) { // if the AI can't pay the additional costs skip the mana ability
                if (!CostPayment.canPayAdditionalCosts(ma.getPayCosts(), ma)) {
                    return false;
                }
            } else if (sourceCard.isTapped()) {
                return false;
            }
        }

        if (m.isComboMana()) {
            for (String s : m.getComboColors().split(" ")) {
                if ("Any".equals(s) || ai.getManaPool().canPayForShardWithColor(toPay, MagicColor.fromName(s)))
                    return true;
            }
            return false;

        } else if (ma.getApi() == ApiType.ManaReflected) {
            Set<String> reflected = CardUtil.getReflectableManaColors(ma);

            for (byte c : MagicColor.WUBRG) {
                if (ai.getManaPool().canPayForShardWithColor(toPay, c) && reflected.contains(MagicColor.toLongString(c))) {
                    m.setExpressChoice(MagicColor.toShortString(c));
                    return true;
                }
            }
            return false;
        }
        return true;
    }


    private static ManaCostShard getNextShardToPay(ManaCostBeingPaid cost, Map<ManaCostShard, Collection<SpellAbility>> sourcesForShards) {
        // mind the priorities
        // * Pay mono-colored first,
        // * Pay 2/C with matching colors
        // * pay hybrids
        // * pay phyrexian, keep mana for colorless
        // * pay colorless

        for (ManaCostShard s : cost.getDistinctShards()) { // should check in which order EnumMap enumerates keys. If it's same as enum member declaration, nothing else needs to be done.
            return s;
        }
        return null;
    }

    private static void adjustManaCostToAvoidNegEffects(ManaCostBeingPaid cost, final Card card, Player ai) {
        // Make mana needed to avoid negative effect a mandatory cost for the AI
        for (String manaPart : card.getSVar("ManaNeededToAvoidNegativeEffect").split(",")) {
            // convert long color strings to short color strings
            byte mask = MagicColor.fromName(manaPart);

            // make mana mandatory for AI
            if (!cost.needsColor(mask, ai.getManaPool()) && cost.getColorlessManaAmount() > 0) {
                ManaCostShard shard = ManaCostShard.valueOf(mask);
                cost.increaseShard(shard, 1);
                cost.decreaseColorlessMana(1);
            }
        }
    }

    /**
     * <p>
     * getComboManaChoice.
     * </p>
     * 
     * @param abMana
     *            a {@link forge.card.spellability.AbilityMana} object.
     * @param saRoot
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param cost
     *            a {@link forge.game.mana.ManaCostBeingPaid} object.
     * @return String
     */
    private static void getComboManaChoice(final Player ai, final SpellAbility manaAb, final SpellAbility saRoot, final ManaCostBeingPaid cost) {
        final StringBuilder choiceString = new StringBuilder();
        final Card source = manaAb.getHostCard();
        final AbilityManaPart abMana = manaAb.getManaPart();

        if (abMana.isComboMana()) {
            int amount = manaAb.hasParam("Amount") ? AbilityUtils.calculateAmount(source, manaAb.getParam("Amount"), saRoot) : 1;
            final ManaCostBeingPaid testCost = new ManaCostBeingPaid(cost);
            final String[] comboColors = abMana.getComboColors().split(" ");
            for (int nMana = 1; nMana <= amount; nMana++) {
                String choice = "";
                // Use expressChoice first
                if (!abMana.getExpressChoice().isEmpty()) {
                    choice = abMana.getExpressChoice();
                    abMana.clearExpressChoice();
                    byte colorMask = MagicColor.fromName(choice);
                    if (abMana.canProduce(choice, manaAb) && testCost.isAnyPartPayableWith(colorMask, ai.getManaPool())) {
                        choiceString.append(choice);
                        payMultipleMana(testCost, choice, ai);
                        continue;
                    }
                }
                // check colors needed for cost
                if (!testCost.isPaid()) {
                    // Loop over combo colors
                    for (String color : comboColors) {
                        if (testCost.isAnyPartPayableWith(MagicColor.fromName(color), ai.getManaPool())) {
                            payMultipleMana(testCost, color, ai);
                            if (nMana != 1) {
                                choiceString.append(" ");
                            }
                            choiceString.append(color);
                            choice = color;
                            break;
                        }
                    }
                    if (!choice.isEmpty()) {
                        continue;
                    }
                }
                // check if combo mana can produce most common color in hand
                String commonColor = ComputerUtilCard.getMostProminentColor(ai.getCardsIn(
                        ZoneType.Hand));
                if (!commonColor.isEmpty() && abMana.getComboColors().contains(MagicColor.toShortString(commonColor))) {
                    choice = MagicColor.toShortString(commonColor);
                }
                else {
                    // default to first color
                    choice = comboColors[0];
                }
                if (nMana != 1) {
                    choiceString.append(" ");
                }
                choiceString.append(choice);
            }
        }
        if (choiceString.toString().isEmpty()) {
            choiceString.append("0");
        }

        abMana.setExpressChoice(choiceString.toString());
    }

    /**
     * <p>
     * payMultipleMana.
     * </p>
     * @param testCost 
     * 
     * @param mana
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    private final static String payMultipleMana(ManaCostBeingPaid testCost, String mana, final Player p) {
        List<String> unused = new ArrayList<String>(4);
        for (String manaPart : TextUtil.split(mana, ' ')) {
            if (StringUtils.isNumeric(manaPart)) {
                for (int i = Integer.parseInt(manaPart); i > 0; i--) {
                    boolean wasNeeded = testCost.ai_payMana("1", p.getManaPool());
                    if (!wasNeeded) {
                        unused.add(Integer.toString(i));
                        break;
                    }
                }
            }
            else {
                String color = MagicColor.toShortString(manaPart);
                boolean wasNeeded = testCost.ai_payMana(color, p.getManaPool());
                if (!wasNeeded) {
                    unused.add(color);
                }
            }
        }
        return unused.isEmpty() ? null : StringUtils.join(unused, ' ');
    }
    
    /**
     * Find all mana sources.
     * @param manaAbilityMap
     * @param partSources
     * @param partPriority
     * @param costParts
     * @param foundAllSources
     * @return Were all mana sources found?
     */
    private static MapOfLists<ManaCostShard, SpellAbility> groupAndOrderToPayShards(final Player ai, final Multimap<Integer, SpellAbility> manaAbilityMap,
            final ManaCostBeingPaid cost) {
        MapOfLists<ManaCostShard, SpellAbility> res = new EnumMapOfLists<ManaCostShard, SpellAbility>(ManaCostShard.class, CollectionSuppliers.<SpellAbility>arrayLists());

        // loop over cost parts
        for (ManaCostShard shard : cost.getDistinctShards()) {
            if (shard == ManaCostShard.S) {
                res.put(shard, manaAbilityMap.get(ManaAtom.IS_SNOW));
                continue;
            }

            if (shard.isOr2Colorless()) {
                Integer colorKey = Integer.valueOf(shard.getColorMask());
                if (manaAbilityMap.containsKey(colorKey))
                    res.addAll(shard, manaAbilityMap.get(colorKey));
                if (manaAbilityMap.containsKey(ManaAtom.COLORLESS))
                    res.addAll(shard, manaAbilityMap.get(ManaAtom.COLORLESS));
                continue;
            }

            for (Entry<Integer, SpellAbility> kv : manaAbilityMap.entries()) {
                // apply mana color change matrix here
                if (ai.getManaPool().canPayForShardWithColor(shard, kv.getKey().byteValue())) {
                    res.add(shard, kv.getValue());
                }
            }
        }

        if (cost.getColorlessManaAmount() > 0 && manaAbilityMap.containsKey(ManaAtom.COLORLESS)) {
            res.addAll(ManaCostShard.COLORLESS, manaAbilityMap.get(ManaAtom.COLORLESS));
        }

        return res;
    }

    /**
     * Calculate the ManaCost for the given SpellAbility.
     * @param sa
     * @param test
     * @param extraMana
     * @return ManaCost
     */
    private static ManaCostBeingPaid calculateManaCost(final SpellAbility sa, final boolean test, final int extraMana) {
        ZoneType castFromBackup = null;
        if (test && sa.isSpell()) {
            castFromBackup = sa.getHostCard().getCastFrom();
            sa.getHostCard().setCastFrom(sa.getHostCard().getZone().getZoneType());
        }

        Cost payCosts = sa.getPayCosts();
        final ManaCost mana = payCosts != null ? payCosts.getTotalMana() : ManaCost.NO_COST;

        String restriction = null;
        if (payCosts != null && payCosts.getCostMana() != null) {
            restriction = payCosts.getCostMana().getRestiction();
        }
        ManaCostBeingPaid cost = new ManaCostBeingPaid(mana, restriction);
        cost.applySpellCostChange(sa, test);

        final Card card = sa.getHostCard();
        // Tack xMana Payments into mana here if X is a set value
        if ((sa.getPayCosts() != null) && (cost.getXcounter() > 0 || extraMana > 0)) {
            int manaToAdd = 0;
            if (test && extraMana > 0) {
                final int multiplicator = Math.max(cost.getXcounter(), 1);
                manaToAdd = extraMana * multiplicator;
            } else {
                // For Count$xPaid set PayX in the AFs then use that here
                // Else calculate it as appropriate.
                final String xSvar = card.getSVar("X").startsWith("Count$xPaid") ? "PayX" : "X";
                if (!card.getSVar(xSvar).equals("")) {
                    if (xSvar.equals("PayX")) {
                        manaToAdd = Integer.parseInt(card.getSVar(xSvar)) * cost.getXcounter(); // X
                    } else {
                        manaToAdd = AbilityUtils.calculateAmount(card, xSvar, sa) * cost.getXcounter();
                    }
                }
            }

            String manaXColor = sa.getParam("XColor");
            ManaCostShard shardToGrow = ManaCostShard.parseNonGeneric(manaXColor == null ? "1" : manaXColor);
            cost.increaseShard(shardToGrow, manaToAdd);

            if (!test) {
                card.setXManaCostPaid(manaToAdd / cost.getXcounter());
            }
        }

        if (test && sa.isSpell()) {
            sa.getHostCard().setCastFrom(castFromBackup);
        }

        return cost;
    }

    //This method is currently used by AI to estimate available mana
    public static List<Card> getAvailableMana(final Player ai, final boolean checkPlayable) {
        final List<Card> list = ai.getCardsIn(ZoneType.Battlefield);
        list.addAll(ai.getCardsIn(ZoneType.Hand));
        final List<Card> manaSources = CardLists.filter(list, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                for (final SpellAbility am : getAIPlayableMana(c)) {
                    am.setActivatingPlayer(ai);
                    if (!checkPlayable || am.canPlay()) {
                        return true;
                    }
                }
                return false;
            }
        }); // CardListFilter

        final List<Card> sortedManaSources = new ArrayList<Card>();
        final List<Card> otherManaSources = new ArrayList<Card>();
        final List<Card> colorlessManaSources = new ArrayList<Card>();
        final List<Card> oneManaSources = new ArrayList<Card>();
        final List<Card> twoManaSources = new ArrayList<Card>();
        final List<Card> threeManaSources = new ArrayList<Card>();
        final List<Card> fourManaSources = new ArrayList<Card>();
        final List<Card> fiveManaSources = new ArrayList<Card>();
        final List<Card> anyColorManaSources = new ArrayList<Card>();

        // Sort mana sources
        // 1. Use lands that can only produce colorless mana without
        // drawback/cost first
        // 2. Search for mana sources that have a certain number of abilities
        // 3. Use lands that produce any color many
        // 4. all other sources (creature, costs, drawback, etc.)
        for (Card card : manaSources) {
            if (card.isCreature() || card.isEnchanted()) {
                otherManaSources.add(card);
                continue; // don't use creatures before other permanents
            }

            int usableManaAbilities = 0;
            boolean needsLimitedResources = false;
            boolean producesAnyColor = false;
            final ArrayList<SpellAbility> manaAbilities = getAIPlayableMana(card);

            for (final SpellAbility m : manaAbilities) {
                if (m.getManaPart().isAnyMana()) {
                    producesAnyColor = true;
                }

                final Cost cost = m.getPayCosts();
                if (cost != null) {
                    needsLimitedResources |= !cost.isReusuableResource();
                }

                // if the AI can't pay the additional costs skip the mana ability
                if (cost != null) {
                    m.setActivatingPlayer(ai);
                    if (!CostPayment.canPayAdditionalCosts(m.getPayCosts(), m)) {
                        continue;
                    }
                }

                // don't use abilities with dangerous drawbacks
                AbilitySub sub = m.getSubAbility();
                if (sub != null && !card.getName().equals("Pristine Talisman") && !card.getName().equals("Zhur-Taa Druid")) {
                    if (!SpellApiToAi.Converter.get(sub.getApi()).chkDrawbackWithSubs(ai, sub)) {
                        continue;
                    }
                    needsLimitedResources = true; // TODO: check for good drawbacks (gainLife)
                }
                usableManaAbilities++;
            }

            if (needsLimitedResources) {
                otherManaSources.add(card);
            } else if (producesAnyColor) {
                anyColorManaSources.add(card);
            } else if (usableManaAbilities == 1) {
                if (manaAbilities.get(0).getManaPart().mana().equals("1")) {
                    colorlessManaSources.add(card);
                } else {
                    oneManaSources.add(card);
                }
            } else if (usableManaAbilities == 2) {
                twoManaSources.add(card);
            } else if (usableManaAbilities == 3) {
                threeManaSources.add(card);
            } else if (usableManaAbilities == 4) {
                fourManaSources.add(card);
            } else {
                fiveManaSources.add(card);
            }
        }
        sortedManaSources.addAll(colorlessManaSources);
        sortedManaSources.addAll(oneManaSources);
        sortedManaSources.addAll(twoManaSources);
        sortedManaSources.addAll(threeManaSources);
        sortedManaSources.addAll(fourManaSources);
        sortedManaSources.addAll(fiveManaSources);
        sortedManaSources.addAll(anyColorManaSources);
        //use better creatures later
        ComputerUtilCard.sortByEvaluateCreature(otherManaSources);
        Collections.reverse(otherManaSources);
        sortedManaSources.addAll(otherManaSources);
        return sortedManaSources;
    } // getAvailableMana()

    //This method is currently used by AI to estimate mana available
    private static Multimap<Integer, SpellAbility> groupSourcesByManaColor(final Player ai, boolean checkPlayable) {
        final Multimap<Integer, SpellAbility> manaMap = ArrayListMultimap.create();
        final Game game = ai.getGame();

        // Loop over all current available mana sources
        for (final Card sourceCard : getAvailableMana(ai, checkPlayable)) {
            for (final SpellAbility m : getAIPlayableMana(sourceCard)) {
                m.setActivatingPlayer(ai);
                if (checkPlayable && !m.canPlay()) {
                    continue;
                }

                // don't use abilities with dangerous drawbacks
                AbilitySub sub = m.getSubAbility();
                if (sub != null) {
                    if (!SpellApiToAi.Converter.get(sub.getApi()).chkDrawbackWithSubs(ai, sub)) {
                        continue;
                    }
                }

                manaMap.put(ManaAtom.COLORLESS, m); // add to colorless source list
                AbilityManaPart mp = m.getManaPart();

                // setup produce mana replacement effects
                final HashMap<String, Object> repParams = new HashMap<String, Object>();
                repParams.put("Event", "ProduceMana");
                repParams.put("Mana", mp.getOrigProduced());
                repParams.put("Affected", sourceCard);
                repParams.put("Player", ai);
                repParams.put("AbilityMana", m);

                for (final Player p : game.getPlayers()) {
                    for (final Card crd : p.getAllCards()) {
                        for (final ReplacementEffect replacementEffect : crd.getReplacementEffects()) {
                            if (replacementEffect.requirementsCheck(game)
                                    && replacementEffect.canReplace(repParams)
                                    && replacementEffect.getMapParams().containsKey("ManaReplacement")
                                    && replacementEffect.zonesCheck(game.getZoneOf(crd))) {
                                String repType = crd.getSVar(replacementEffect.getMapParams().get("ManaReplacement"));
                                if (repType.contains("Chosen")) {
                                    repType = repType.replace("Chosen", MagicColor.toShortString(crd.getChosenColor().get(0)));
                                }
                                mp.setManaReplaceType(repType);
                            }
                        }
                    }
                }

                Set<String> reflectedColors = CardUtil.getReflectableManaColors(m);
                // find possible colors
                if (mp.canProduce("W", m) || reflectedColors.contains(MagicColor.Constant.WHITE)) {
                    manaMap.put(ManaAtom.WHITE, m);
                }
                if (mp.canProduce("U", m) || reflectedColors.contains(MagicColor.Constant.BLUE)) {
                    manaMap.put(ManaAtom.BLUE, m);
                }
                if (mp.canProduce("B", m) || reflectedColors.contains(MagicColor.Constant.BLACK)) {
                    manaMap.put(ManaAtom.BLACK, m);
                }
                if (mp.canProduce("R", m) || reflectedColors.contains(MagicColor.Constant.RED)) {
                    manaMap.put(ManaAtom.RED, m);
                }
                if (mp.canProduce("G", m) || reflectedColors.contains(MagicColor.Constant.GREEN)) {
                    manaMap.put(ManaAtom.GREEN, m);
                }
                if (mp.isSnow()) {
                    manaMap.put(ManaAtom.IS_SNOW, m);
                }
            } // end of mana abilities loop
        } // end of mana sources loop

        return manaMap;
    }

    /**
     * <p>
     * determineLeftoverMana.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @return a int.
     * @since 1.0.15
     */
    public static int determineLeftoverMana(final SpellAbility sa, final Player player) {
        for (int i = 1; i < 100; i++)
            if (!canPayManaCost(sa, player, i))
                return i - 1;

        return 99;
    }

    // Returns basic mana abilities plus "reflected mana" abilities
    /**
     * <p>
     * getAIPlayableMana.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public static final ArrayList<SpellAbility> getAIPlayableMana(Card c) {
        final ArrayList<SpellAbility> res = new ArrayList<SpellAbility>();
        for (final SpellAbility a : c.getManaAbility()) {
            // if a mana ability has a mana cost the AI will miscalculate
            // if there is a parent ability the AI can't use it
            final Cost cost = a.getPayCosts();
            if (!cost.hasNoManaCost() || (a.getApi() != ApiType.Mana && a.getApi() != ApiType.ManaReflected)) {
                continue;
            }

            if (!res.contains(a)) {
                res.add(a);
            }
        }
        return res;
    }

    private static void handleOfferingsAI(final SpellAbility sa, boolean test, boolean costIsPaid) {
        if (sa.isOffering() && sa.getSacrificedAsOffering() != null) {
            final Card offering = sa.getSacrificedAsOffering();
            offering.setUsedToPay(false);
            if (costIsPaid && !test) {
                sa.getHostCard().getController().getGame().getAction().sacrifice(offering, sa);
            }
            sa.resetSacrificedAsOffering();
        }
    }
}
