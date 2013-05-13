package forge.game.player;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates.Presets;
import forge.CounterType;
import forge.FThreads;
import forge.card.ability.AbilityUtils;
import forge.card.ability.ApiType;
import forge.card.ability.effects.CharmEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.cost.CostDamage;
import forge.card.cost.CostDiscard;
import forge.card.cost.CostExile;
import forge.card.cost.CostMill;
import forge.card.cost.CostPart;
import forge.card.cost.CostPartMana;
import forge.card.cost.CostPartWithList;
import forge.card.cost.CostPayLife;
import forge.card.cost.CostPayment;
import forge.card.cost.CostPutCounter;
import forge.card.cost.CostRemoveCounter;
import forge.card.cost.CostReturn;
import forge.card.cost.CostReveal;
import forge.card.cost.CostSacrifice;
import forge.card.cost.CostTapType;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostBeingPaid;
import forge.card.mana.ManaCostShard;
import forge.card.spellability.Ability;
import forge.card.spellability.HumanPlaySpellAbility;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.control.input.InputPayManaBase;
import forge.control.input.InputPayManaExecuteCommands;
import forge.control.input.InputPayManaSimple;
import forge.control.input.InputPayment;
import forge.control.input.InputSelectCards;
import forge.control.input.InputSelectCardsFromList;
import forge.game.GameActionUtil;
import forge.game.GameState;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.GuiDialog;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class HumanPlay {

    /**
     * TODO: Write javadoc for Constructor.
     */
    public HumanPlay() {
        // TODO Auto-generated constructor stub
    }

    /**
     * TODO: Write javadoc for this method.
     * @param card
     * @param ab
     */
    public static void playSpellAbility(Player p, Card c, SpellAbility ab) {
        if (ab == Ability.PLAY_LAND_SURROGATE)
            p.playLand(c);
        else {
            HumanPlay.playSpellAbility(p, ab);
        }
        p.getGame().getPhaseHandler().setPriority(p);
    }

    /**
     * <p>
     * playSpellAbility.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public final static void playSpellAbility(Player p, SpellAbility sa) {
        FThreads.assertExecutedByEdt(false);
        sa.setActivatingPlayer(p);
    
        final Card source = sa.getSourceCard();
        
        source.setSplitStateToPlayAbility(sa);
    
        if (sa.getApi() == ApiType.Charm && !sa.isWrapper()) {
            CharmEffect.makeChoices(sa);
        }
    
        sa = chooseOptionalAdditionalCosts(p, sa);
    
        if (sa == null) {
            return;
        }
    
        // Need to check PayCosts, and Ability + All SubAbilities for Target
        boolean newAbility = sa.getPayCosts() != null;
        SpellAbility ability = sa;
        while ((ability != null) && !newAbility) {
            final Target tgt = ability.getTarget();
    
            newAbility |= tgt != null;
            ability = ability.getSubAbility();
        }
    
        // System.out.println("Playing:" + sa.getDescription() + " of " + sa.getSourceCard() +  " new = " + newAbility);
        if (newAbility) {
            CostPayment payment = null;
            if (sa.getPayCosts() == null) {
                payment = new CostPayment(new Cost("0", sa.isAbility()), sa);
            } else {
                payment = new CostPayment(sa.getPayCosts(), sa);
            }
    
            final HumanPlaySpellAbility req = new HumanPlaySpellAbility(sa, payment);
            req.fillRequirements(false, false, false);
        } else {
            if (payManaCostIfNeeded(p, sa)) {
                if (sa.isSpell() && !source.isCopiedSpell()) {
                    sa.setSourceCard(p.getGame().getAction().moveToStack(source));
                }
                p.getGame().getStack().add(sa);
            } 
        }
    }

    /**
     * choose optional additional costs. For HUMAN only
     * @param activator 
     * 
     * @param original
     *            the original sa
     * @return an ArrayList<SpellAbility>.
     */
    static SpellAbility chooseOptionalAdditionalCosts(Player p, final SpellAbility original) {
        //final HashMap<String, SpellAbility> map = new HashMap<String, SpellAbility>();
        final List<SpellAbility> abilities = GameActionUtil.getOptionalCosts(original);
        
        if (!original.isSpell()) {
            return original;
        }
    
        return p.getController().getAbilityToPlay(abilities);
    }

    private static boolean payManaCostIfNeeded(final Player p, final SpellAbility sa) {
        final ManaCostBeingPaid manaCost; 
        if (sa.getSourceCard().isCopiedSpell() && sa.isSpell()) {
            manaCost = new ManaCostBeingPaid(ManaCost.ZERO);
        } else {
            manaCost = new ManaCostBeingPaid(sa.getPayCosts().getTotalMana());
            manaCost.applySpellCostChange(sa);
        }
    
        boolean isPaid = manaCost.isPaid();
    
        if( !isPaid ) {
            InputPayManaBase inputPay = new InputPayManaSimple(p.getGame(), sa, manaCost);
            FThreads.setInputAndWait(inputPay);
            isPaid = inputPay.isPaid();
        }
        return isPaid;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param humanPlayer
     * @param c
     */
    public static final void playCardWithoutPayingManaCost(Player player, Card c) {
        final List<SpellAbility> choices = c.getBasicSpells();
        // TODO add Buyback, Kicker, ... , spells here
    
        SpellAbility sa = player.getController().getAbilityToPlay(choices);
    
        if (sa != null) {
            sa.setActivatingPlayer(player);
            playSaWithoutPayingManaCost(player, sa);
        }
    }

    /**
     * <p>
     * playSpellAbilityForFree.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public static final void playSaWithoutPayingManaCost(final Player player, final SpellAbility sa) {
        FThreads.assertExecutedByEdt(false);
        final Card source = sa.getSourceCard();
        
        source.setSplitStateToPlayAbility(sa);
    
        if (sa.getPayCosts() != null) {
            if (sa.getApi() == ApiType.Charm && !sa.isWrapper()) {
                CharmEffect.makeChoices(sa);
            }
            final CostPayment payment = new CostPayment(sa.getPayCosts(), sa);
    
            final HumanPlaySpellAbility req = new HumanPlaySpellAbility(sa, payment);
            req.fillRequirements(false, true, false);
        } else {
            if (sa.isSpell()) {
                final Card c = sa.getSourceCard();
                if (!c.isCopiedSpell()) {
                    sa.setSourceCard(player.getGame().getAction().moveToStack(c));
                }
            }
            boolean x = sa.getSourceCard().getManaCost().getShardCount(ManaCostShard.X) > 0;
    
            player.getGame().getStack().add(sa, x);
        }
    }

    /**
     * <p>
     * playSpellAbility_NoStack.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param skipTargeting
     *            a boolean.
     */
    public final static void playSpellAbilityNoStack(final Player player, final SpellAbility sa) {
        playSpellAbilityNoStack(player, sa, false);
    }

    public final static void playSpellAbilityNoStack(final Player player, final SpellAbility sa, boolean useOldTargets) {
        sa.setActivatingPlayer(player);
    
        if (sa.getPayCosts() != null) {
            final HumanPlaySpellAbility req = new HumanPlaySpellAbility(sa, new CostPayment(sa.getPayCosts(), sa));
            
            req.fillRequirements(useOldTargets, false, true);
        } else {
            if (payManaCostIfNeeded(player, sa)) {
                AbilityUtils.resolve(sa, false);
            }
    
        }
    }

    // ------------------------------------------------------------------------
    
    private static int getAmountFromPart(CostPart part, Card source, SpellAbility sourceAbility) {
        String amountString = part.getAmount();
        return StringUtils.isNumeric(amountString) ? Integer.parseInt(amountString) : AbilityUtils.calculateAmount(source, amountString, sourceAbility);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param part
     * @param source
     * @param sourceAbility
     * @return
     */
    private static int getAmountFromPartX(CostPart part, Card source, SpellAbility sourceAbility) {
        String amountString = part.getAmount();
        return StringUtils.isNumeric(amountString) ? Integer.parseInt(amountString) : CardFactoryUtil.xCount(source, source.getSVar(amountString));
    }

    /**
     * <p>
     * payCostDuringAbilityResolve.
     * </p>
     * 
     * @param ability
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param cost
     *            a {@link forge.card.cost.Cost} object.
     * @param paid
     *            a {@link forge.Command} object.
     * @param unpaid
     *            a {@link forge.Command} object.
     * @param sourceAbility TODO
     */
    public static boolean payCostDuringAbilityResolve(final SpellAbility ability, final Cost cost, SpellAbility sourceAbility, final GameState game) {
        
        // Only human player pays this way
        final Player p = ability.getActivatingPlayer();
        final Card source = ability.getSourceCard();
        Card current = null; // Used in spells with RepeatEach effect to distinguish cards, Cut the Tethers
        if (!source.getRemembered().isEmpty()) {
            if (source.getRemembered().get(0) instanceof Card) { 
                current = (Card) source.getRemembered().get(0);
            }
        }
        if (!source.getImprinted().isEmpty()) {
            current = source.getImprinted().get(0);
        }
        
        final List<CostPart> parts =  cost.getCostParts();
        ArrayList<CostPart> remainingParts =  new ArrayList<CostPart>(cost.getCostParts());
        CostPart costPart = null;
        if (!parts.isEmpty()) {
            costPart = parts.get(0);
        }
        final String orString = sourceAbility == null ? "" : " (or: " + sourceAbility.getStackDescription() + ")";
        
        if (parts.isEmpty() || costPart.getAmount().equals("0")) {
            return GuiDialog.confirm(source, "Do you want to pay 0?" + orString);
        }
        
        //the following costs do not need inputs
        for (CostPart part : parts) {
            boolean mayRemovePart = true;
            
            if (part instanceof CostPayLife) {
                final int amount = getAmountFromPart(part, source, sourceAbility);
                if (!p.canPayLife(amount))
                    return false;
    
                if (false == GuiDialog.confirm(source, "Do you want to pay " + amount + " life?" + orString))
                    return false;
    
                p.payLife(amount, null);
            }
    
            else if (part instanceof CostMill) {
                final int amount = getAmountFromPart(part, source, sourceAbility);
                final List<Card> list = p.getCardsIn(ZoneType.Library);
                if (list.size() < amount) return false;
                if (!GuiDialog.confirm(source, "Do you want to mill " + amount + " card(s)?" + orString))
                    return false;
                List<Card> listmill = p.getCardsIn(ZoneType.Library, amount);
                ((CostMill) part).executePayment(sourceAbility, listmill);
            }
    
            else if (part instanceof CostDamage) {
                int amount = getAmountFromPartX(part, source, sourceAbility);
                if (!p.canPayLife(amount))
                    return false;
    
                if (false == GuiDialog.confirm(source, "Do you want " + source + " to deal " + amount + " damage to you?"))
                    return false;
                
                p.addDamage(amount, source);
            }
    
            else if (part instanceof CostPutCounter) {
                CounterType counterType = ((CostPutCounter) part).getCounter();
                int amount = getAmountFromPartX(part, source, sourceAbility);
                
                if (false == source.canReceiveCounters(counterType)) {
                    String message = String.format("Won't be able to pay upkeep for %s but it can't have %s counters put on it.", source, counterType.getName());
                    p.getGame().getGameLog().add("ResolveStack", message, 2);
                    return false;
                }
                
                String plural = amount > 1 ? "s" : "";
                if (false == GuiDialog.confirm(source, "Do you want to put " + amount + " " + counterType.getName() + " counter" + plural + " on " + source + "?")) 
                    return false;
                
                source.addCounter(counterType, amount, false);
            }
    
            else if (part instanceof CostRemoveCounter) {
                CounterType counterType = ((CostRemoveCounter) part).getCounter();
                int amount = getAmountFromPartX(part, source, sourceAbility);
                String plural = amount > 1 ? "s" : "";
                
                if (!part.canPay(sourceAbility))
                    return false;
    
                if ( false == GuiDialog.confirm(source, "Do you want to remove " + amount + " " + counterType.getName() + " counter" + plural + " from " + source + "?"))
                    return false;
    
                source.subtractCounter(counterType, amount);
            }
    
            else if (part instanceof CostExile) {
                if ("All".equals(part.getType())) {
                    if (false == GuiDialog.confirm(source, "Do you want to exile all cards in your graveyard?"))
                        return false;
                        
                    List<Card> cards = new ArrayList<Card>(p.getCardsIn(ZoneType.Graveyard));
                    for (final Card card : cards) {
                        p.getGame().getAction().exile(card);
                    }
                } else {
                    CostExile costExile = (CostExile) part;
                    ZoneType from = costExile.getFrom();
                    List<Card> list = CardLists.getValidCards(p.getCardsIn(from), part.getType().split(";"), p, source);
                    final int nNeeded = AbilityUtils.calculateAmount(source, part.getAmount(), ability);
                    if (list.size() < nNeeded)
                        return false;
    
                    // replace this with input
                    for (int i = 0; i < nNeeded; i++) {
                        final Card c = GuiChoose.oneOrNone("Exile from " + from, list);
                        if (c == null)
                            return false;
                            
                        list.remove(c);
                        p.getGame().getAction().exile(c);
                    }
                }
            }
    
            else if (part instanceof CostSacrifice) {
                int amount = Integer.parseInt(((CostSacrifice)part).getAmount());
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Battlefield), part.getType(), p, source);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "sacrifice." + orString);
                if(!hasPaid) return false;
            } else if (part instanceof CostReturn) {
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Battlefield), part.getType(), p, source);
                int amount = getAmountFromPartX(part, source, sourceAbility);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "return to hand." + orString);
                if(!hasPaid) return false;
            } else if (part instanceof CostDiscard) {
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Hand), part.getType(), p, source);
                int amount = getAmountFromPartX(part, source, sourceAbility);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "discard." + orString);
                if(!hasPaid) return false;
            } else if (part instanceof CostReveal) {
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Hand), part.getType(), p, source);
                int amount = getAmountFromPartX(part, source, sourceAbility);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "reveal." + orString);
                if(!hasPaid) return false;
            } else if (part instanceof CostTapType) {
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Battlefield), part.getType(), p, source);
                list = CardLists.filter(list, Presets.UNTAPPED);
                int amount = getAmountFromPartX(part, source, sourceAbility);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "tap." + orString);
                if(!hasPaid) return false;
            }
            
            else if (part instanceof CostPartMana ) {
                if (!((CostPartMana) part).getManaToPay().isZero()) // non-zero costs require input
                    mayRemovePart = false; 
            } else
                throw new RuntimeException("GameActionUtil.payCostDuringAbilityResolve - An unhandled type of cost was met: " + part.getClass());
    
            if( mayRemovePart )
                remainingParts.remove(part);
        }
    
    
        if (remainingParts.isEmpty()) {
            return true;
        }
        if (remainingParts.size() > 1) {
            throw new RuntimeException("GameActionUtil.payCostDuringAbilityResolve - Too many payment types - " + source);
        }
        costPart = remainingParts.get(0);
        // check this is a mana cost
        if (!(costPart instanceof CostPartMana ))
            throw new RuntimeException("GameActionUtil.payCostDuringAbilityResolve - The remaining payment type is not Mana.");
    
        InputPayment toSet = current == null 
                ? new InputPayManaExecuteCommands(p, source + "\r\n", cost.getCostMana().getManaToPay())
                : new InputPayManaExecuteCommands(p, source + "\r\n" + "Current Card: " + current + "\r\n" , cost.getCostMana().getManaToPay());
        FThreads.setInputAndWait(toSet);
        return toSet.isPaid();
    }

    private static boolean payCostPart(SpellAbility sourceAbility, CostPartWithList cpl, int amount, List<Card> list, String actionName) {
        if (list.size() < amount) return false;                     // unable to pay (not enough cards)
    
        InputSelectCards inp = new InputSelectCardsFromList(amount, amount, list);
        inp.setMessage("Select %d " + cpl.getDescriptiveType() + " card(s) to " + actionName);
        inp.setCancelAllowed(true);
        
        FThreads.setInputAndWait(inp);
        if( inp.hasCancelled() || inp.getSelected().size() != amount)
            return false;
    
        for(Card c : inp.getSelected()) {
            cpl.executePayment(sourceAbility, c);
        }
        if (sourceAbility != null) {
            cpl.reportPaidCardsTo(sourceAbility);
        }
        return true;
    }

}
