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
package forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import forge.CardPredicates.Presets;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.cost.CostDamage;
import forge.card.cost.CostDiscard;
import forge.card.cost.CostExile;
import forge.card.cost.CostPart;
import forge.card.cost.CostPayLife;
import forge.card.cost.CostMana;
import forge.card.cost.CostPutCounter;
import forge.card.cost.CostReturn;
import forge.card.cost.CostSacrifice;
import forge.card.cost.CostUtil;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityRestriction;
import forge.control.input.InputPayDiscardCost;
import forge.control.input.InputPayManaCostAbility;
import forge.control.input.InputPayReturnCost;
import forge.control.input.InputPaySacCost;
import forge.game.GameLossReason;
import forge.game.event.FlipCoinEvent;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.match.CMatchUI;
import forge.sound.SoundEffectType;
import forge.util.MyRandom;


/**
 * <p>
 * GameActionUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class GameActionUtil {

    private GameActionUtil() {
        throw new AssertionError();
    }

    /**
     * <p>
     * executePlayCardEffects.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public static void executePlayCardEffects(final SpellAbility sa) {
        // (called in MagicStack.java)

        GameActionUtil.playCardCascade(sa);
        GameActionUtil.playCardRipple(sa);
    }

    /**
     * <p>
     * playCardCascade.
     * </p>
     * 
     * @param sa
     *            a SpellAbility object.
     */
    public static void playCardCascade(final SpellAbility sa) {
        final Card c = sa.getSourceCard();
        final Player controller = sa.getActivatingPlayer();
        final Command cascade = new Command() {
            private static final long serialVersionUID = -845154812215847505L;

            @Override
            public void execute() {

                if (!c.isCopiedSpell()) {
                    final List<Card> maelstromNexii = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Maelstrom Nexus"));

                    for (final Card nexus : maelstromNexii) {
                        if (CardUtil.getThisTurnCast("Card.YouCtrl", nexus).size() == 1) {
                            this.doCascade(c, controller);
                        }
                    }
                }

                for (String keyword : c.getKeyword()) {
                    if (keyword.equals("Cascade")) {
                        this.doCascade(c, controller);
                    }
                }
            } // execute()

            void doCascade(final Card c, final Player controller) {
                final Card cascCard = c;

                final Ability ability = new Ability(c, "0") {
                    @Override
                    public void resolve() {
                        final List<Card> topOfLibrary = controller.getCardsIn(ZoneType.Library);
                        final List<Card> revealed = new ArrayList<Card>();

                        if (topOfLibrary.size() == 0) {
                            return;
                        }

                        Card cascadedCard = null;
                        Card crd;
                        int count = 0;
                        while (cascadedCard == null) {
                            crd = topOfLibrary.get(count++);
                            revealed.add(crd);
                            if ((!crd.isLand() && (crd.getManaCost().getCMC() < cascCard.getManaCost().getCMC()))) {
                                cascadedCard = crd;
                            }

                            if (count == topOfLibrary.size()) {
                                break;
                            }

                        } // while
                        GuiChoose.oneOrNone("Revealed cards:", revealed);

                        if (cascadedCard != null) {
                            Player p = cascadedCard.getController();

                            if (p.isHuman()) {
                                final StringBuilder title = new StringBuilder();
                                title.append(cascCard.getName()).append(" - Cascade Ability");
                                final StringBuilder question = new StringBuilder();
                                question.append("Cast ").append(cascadedCard.getName());
                                question.append(" without paying its mana cost?");

                                final int answer = JOptionPane.showConfirmDialog(null, question.toString(),
                                        title.toString(), JOptionPane.YES_NO_OPTION);

                                if (answer == JOptionPane.YES_OPTION) {
                                    Singletons.getModel().getGame().getAction().playCardWithoutManaCost(cascadedCard);
                                    revealed.remove(cascadedCard);
                                }
                            } else {
                                final List<SpellAbility> choices = cascadedCard.getBasicSpells();

                                for (final SpellAbility sa : choices) {
                                    //Spells
                                    if (sa instanceof Spell) {
                                        Spell spell = (Spell) sa;
                                        if (!spell.canPlayFromEffectAI(false, true)) {
                                            continue;
                                        }
                                    } else {
                                        if (!sa.canPlayAI()) {
                                            continue;
                                        }
                                    }
                                    ComputerUtil.playSpellAbilityWithoutPayingManaCost(p, sa);
                                    revealed.remove(cascadedCard);
                                    break;
                                }
                            }
                        }
                        CardLists.shuffle(revealed);
                        for (final Card bottom : revealed) {
                            Singletons.getModel().getGame().getAction().moveToBottomOfLibrary(bottom);
                        }
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append(c).append(" - Cascade.");
                ability.setStackDescription(sb.toString());
                ability.setActivatingPlayer(controller);

                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

            }
        };
        cascade.execute();
    } // end playCardCascade

    /**
     * <p>
     * playCardRipple.
     * </p>
     * 
     * @param sa
     *            a SpellAbility object.
     */
    public static void playCardRipple(final SpellAbility sa) {
        final Card c = sa.getSourceCard();
        final Player controller = sa.getActivatingPlayer();
        final Command ripple = new Command() {
            private static final long serialVersionUID = -845154812215847505L;

            @Override
            public void execute() {

                final List<Card> thrummingStones = controller.getCardsIn(ZoneType.Battlefield, "Thrumming Stone");
                for (int i = 0; i < thrummingStones.size(); i++) {
                    c.addExtrinsicKeyword("Ripple:4");
                }

                final ArrayList<String> a = c.getKeyword();
                for (int x = 0; x < a.size(); x++) {
                    if (a.get(x).toString().startsWith("Ripple")) {
                        final String parse = c.getKeyword().get(x).toString();
                        final String[] k = parse.split(":");
                        this.doRipple(c, Integer.valueOf(k[1]), controller);
                    }
                }
            } // execute()

            void doRipple(final Card c, final int rippleCount, final Player controller) {
                final Card rippleCard = c;
                boolean activateRipple = false;
                if (controller.isComputer() || GameActionUtil.showYesNoDialog(c, "Activate Ripple for " + c + "?")) {
                        activateRipple = true;
                }
                if (activateRipple) {
                    final Ability ability = new Ability(c, "0") {
                        @Override
                        public void resolve() {
                            final List<Card> topOfLibrary = controller.getCardsIn(ZoneType.Library);
                            final List<Card> revealed = new ArrayList<Card>();
                            int rippleNumber = rippleCount;
                            if (topOfLibrary.size() == 0) {
                                return;
                            }

                            // Shouldn't Have more than Ripple 10, seeing as no
                            // cards exist with a ripple greater than 4
                            final int rippleMax = 10;
                            final Card[] rippledCards = new Card[rippleMax];
                            Card crd;
                            if (topOfLibrary.size() < rippleNumber) {
                                rippleNumber = topOfLibrary.size();
                            }

                            for (int i = 0; i < rippleNumber; i++) {
                                crd = topOfLibrary.get(i);
                                revealed.add(crd);
                                if (crd.getName().equals(rippleCard.getName())) {
                                    rippledCards[i] = crd;
                                }
                            } // for
                            GuiChoose.oneOrNone("Revealed cards:", revealed);
                            for (int i = 0; i < rippleMax; i++) {
                                if (rippledCards[i] != null) {
                                    Player p = rippledCards[i].getController();

                                    if (p.isHuman()) {
                                        final Object[] possibleValues = { "Yes", "No" };
                                        final Object q = JOptionPane.showOptionDialog(null,
                                                "Cast " + rippledCards[i].getName() + "?", "Ripple",
                                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                                possibleValues, possibleValues[0]);
                                        if (q.equals(0)) {
                                            Singletons.getModel().getGame().getAction().playCardWithoutManaCost(rippledCards[i]);
                                            revealed.remove(rippledCards[i]);
                                        }
                                    } else {
                                        final List<SpellAbility> choices = rippledCards[i].getBasicSpells();

                                        for (final SpellAbility sa : choices) {
                                          //Spells
                                            if (sa instanceof Spell) {
                                                Spell spell = (Spell) sa;
                                                if (!spell.canPlayFromEffectAI(false, true)) {
                                                    continue;
                                                }
                                            } else {
                                                if (!sa.canPlayAI() && !sa.getSourceCard().isType("Legendary")) {
                                                    continue;
                                                }
                                            }
                                            ComputerUtil.playSpellAbilityWithoutPayingManaCost(p, sa);
                                            revealed.remove(rippledCards[i]);
                                            break;
                                        }
                                    }
                                }
                            }
                            CardLists.shuffle(revealed);
                            for (final Card bottom : revealed) {
                                Singletons.getModel().getGame().getAction().moveToBottomOfLibrary(bottom);
                            }
                        }
                    };
                    final StringBuilder sb = new StringBuilder();
                    sb.append(c).append(" - Ripple.");
                    ability.setStackDescription(sb.toString());
                    ability.setDescription(sb.toString());
                    ability.setActivatingPlayer(controller);

                    Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

                }
            }
        };
        ripple.execute();
    } // playCardRipple()

    /**
     * <p>
     * payManaDuringAbilityResolve.
     * </p>
     * 
     * @param message
     *            a {@link java.lang.String} object.
     * @param manaCost
     *            a {@link java.lang.String} object.
     * @param paid
     *            a {@link forge.Command} object.
     * @param unpaid
     *            a {@link forge.Command} object.
     */
    public static void payManaDuringAbilityResolve(final String message, final String manaCost, final Command paid,
            final Command unpaid) {
        // temporarily disable the Resolve flag, so the user can payMana for the
        // resolving Ability
        final boolean bResolving = Singletons.getModel().getGame().getStack().isResolving();
        Singletons.getModel().getGame().getStack().setResolving(false);
        Singletons.getModel().getMatch().getInput().setInput(new InputPayManaCostAbility(message, manaCost, paid, unpaid));
        Singletons.getModel().getGame().getStack().setResolving(bResolving);
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
    public static void payCostDuringAbilityResolve(final SpellAbility ability, final Cost cost, final Command paid,
            final Command unpaid, SpellAbility sourceAbility) {
        final Card source = ability.getSourceCard();
        final ArrayList<CostPart> parts =  cost.getCostParts();
        ArrayList<CostPart> remainingParts =  new ArrayList<CostPart>(cost.getCostParts());
        CostPart costPart = null;
        if (!parts.isEmpty()) {
            costPart = parts.get(0);
        }
        String orString = "";
        if (sourceAbility != null) {
            orString = " (or: " + sourceAbility.getStackDescription() + ")";
        }
        if (parts.isEmpty() || costPart.getAmount().equals("0")) {
            if (showYesNoDialog(source, "Do you want to pay 0?" + orString)) {
                paid.execute();
            } else {
                unpaid.execute();
            }
            return;
        }
        boolean hasPaid = true;
        //the three following costs do not need inputs
        for (CostPart part : parts) {
            if (part instanceof CostPayLife) {
                String amountString = part.getAmount();

                final int amount = amountString.matches("[0-9][0-9]?") ? Integer.parseInt(amountString)
                        : AbilityFactory.calculateAmount(source, amountString, sourceAbility);
                Player p = Singletons.getControl().getPlayer();
                if (p.canPayLife(amount) && showYesNoDialog(source, "Do you want to pay " + amount + " life?" + orString)) {
                    p.payLife(amount, null);
                } else {
                    hasPaid = false;
                    break;
                }
                remainingParts.remove(part);
            }

            else if (part instanceof CostDamage) {
                String amountString = part.getAmount();
                final int amount = amountString.matches("[0-9][0-9]?") ? Integer.parseInt(amountString)
                        : CardFactoryUtil.xCount(source, source.getSVar(amountString));
                Player p = Singletons.getControl().getPlayer();
                if (p.canPayLife(amount) && showYesNoDialog(source, "Do you want " + source + " to deal " + amount + " damage to you?")) {
                    p.addDamage(amount, source);
                } else {
                    hasPaid = false;
                    break;
                }
                remainingParts.remove(part);
            }

            else if (part instanceof CostPutCounter) {
                String amountString = part.getAmount();
                Counters counterType = ((CostPutCounter) part).getCounter();
                int amount = amountString.matches("[0-9][0-9]?") ? Integer.parseInt(amountString)
                        : CardFactoryUtil.xCount(source, source.getSVar(amountString));
                String plural = amount > 1 ? "s" : "";
                if (showYesNoDialog(source, "Do you want to put " + amount + " " + counterType.getName()
                        + " counter" + plural + " on " + source + "?")) {
                    if (source.canHaveCountersPlacedOnIt(counterType)) {
                        source.addCounterFromNonEffect(counterType, amount);
                    } else {
                        hasPaid = false;
                        Singletons.getModel().getGame().getGameLog().add("ResolveStack", "Trying to pay upkeep for " + source + " but it can't have "
                        + counterType.getName() + " counters put on it.", 2);
                        break;
                    }
                } else {
                    hasPaid = false;
                    break;
                }
                remainingParts.remove(part);
            }
            else if (part instanceof CostExile) {
                Player p = Singletons.getControl().getPlayer();
                if ("All".equals(part.getType())) {
                    if (showYesNoDialog(source, "Do you want to exile all cards in your graveyard?")) {
                        List<Card> cards = new ArrayList<Card>(p.getCardsIn(ZoneType.Graveyard));
                        for (final Card card : cards) {
                            Singletons.getModel().getGame().getAction().exile(card);
                        }
                    } else {
                        hasPaid = false;
                        break;
                    }
                    remainingParts.remove(part);
                } else {
                    CostExile costExile = (CostExile) part;
                    ZoneType from = costExile.getFrom();
                    List<Card> list = CardLists.getValidCards(p.getCardsIn(from), part.getType().split(";"), p, source);
                    final int nNeeded = AbilityFactory.calculateAmount(source, part.getAmount(), ability);
                    if (list.size() >= nNeeded) {
                        for (int i = 0; i < nNeeded; i++) {

                            final Card c = GuiChoose.oneOrNone("Exile from " + from, list);

                            if (c != null) {
                                list.remove(c);
                                Singletons.getModel().getGame().getAction().exile(c);
                            } else {
                                hasPaid = false;
                                break;
                            }
                        }
                    } else {
                        hasPaid = false;
                        break;
                    }
                }
            }
        }
        if (!hasPaid) {
            unpaid.execute();
            return;
        }
        if (remainingParts.isEmpty()) {
            paid.execute();
            return;
        }
        if (remainingParts.size() > 1) {
            throw new RuntimeException("GameActionUtil::payCostDuringAbilityResolve - Too many payment types - " + source);
        }
        costPart = remainingParts.get(0);

        //the following costs need inputs and can't be combined at the moment
        if (costPart instanceof CostSacrifice) {
            final boolean bResolving = Singletons.getModel().getGame().getStack().isResolving();
            Singletons.getModel().getGame().getStack().setResolving(false);
            Singletons.getModel().getMatch().getInput().setInput(new InputPaySacCost((CostSacrifice) costPart, ability, paid, unpaid));
            Singletons.getModel().getGame().getStack().setResolving(bResolving);
        }
        else if (costPart instanceof CostReturn) {
            final boolean bResolving = Singletons.getModel().getGame().getStack().isResolving();
            Singletons.getModel().getGame().getStack().setResolving(false);
            Singletons.getModel().getMatch().getInput().setInput(new InputPayReturnCost((CostReturn) costPart, ability, paid, unpaid));
            Singletons.getModel().getGame().getStack().setResolving(bResolving);
        }
        else if (costPart instanceof CostDiscard) {
            final boolean bResolving = Singletons.getModel().getGame().getStack().isResolving();
            Singletons.getModel().getGame().getStack().setResolving(false);
            Singletons.getModel().getMatch().getInput().setInput(new InputPayDiscardCost((CostDiscard) costPart, ability, paid, unpaid));
            Singletons.getModel().getGame().getStack().setResolving(bResolving);
        }
        else if (costPart instanceof CostMana) {
            // temporarily disable the Resolve flag, so the user can payMana for the
            // resolving Ability
            final boolean bResolving = Singletons.getModel().getGame().getStack().isResolving();
            Singletons.getModel().getGame().getStack().setResolving(false);
            Singletons.getModel().getMatch().getInput().setInput(new InputPayManaCostAbility(source + "\r\n", ability.getManaCost(), paid, unpaid));
            Singletons.getModel().getGame().getStack().setResolving(bResolving);
        }
    }

    /**
     * <p>
     * showYesNoDialog.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param question
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean showYesNoDialog(final Card c, final String question) {
        return GameActionUtil.showYesNoDialog(c, question, true);
    }

    /**
     * <p>
     * showYesNoDialog.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param question
     *            a {@link java.lang.String} object.
     * @param defaultNo
     *            true if the default option should be "No", false otherwise
     * @return a boolean.
     */
    public static boolean showYesNoDialog(final Card c, String question, final boolean defaultChoice) {
        CMatchUI.SINGLETON_INSTANCE.setCard(c);
        final StringBuilder title = new StringBuilder();
        title.append(c.getName()).append(" - Ability");

        if (!(question.length() > 0)) {
            question = "Activate card's ability?";
        }

        int answer;
        if (!defaultChoice) {
            final Object[] options = { "Yes", "No" };
            answer = JOptionPane.showOptionDialog(null, question, title.toString(), JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        } else {
            answer = JOptionPane.showConfirmDialog(null, question, title.toString(), JOptionPane.YES_NO_OPTION);
        }

        return answer == JOptionPane.YES_OPTION;
    }

    /**
     * <p>
     * showInfoDialg.
     * </p>
     * 
     * @param message
     *            a {@link java.lang.String} object.
     */
    public static void showInfoDialg(final String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * <p>
     * flipACoin.
     * </p>
     * 
     * @param caller
     *            a {@link forge.game.player.Player} object.
     * @param source
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public static boolean flipACoin(final Player caller, final Card source) {
        String choice = "";
        final String[] choices = { "heads", "tails" };

        final boolean flip = MyRandom.getRandom().nextBoolean();
        if (caller.isHuman()) {
            choice = GuiChoose.one(source.getName() + " - Call coin flip", choices);
        } else {
            choice = choices[MyRandom.getRandom().nextInt(2)];
        }

        final boolean winFlip = flip == choice.equals("heads");
        final String winMsg = winFlip ? " wins flip." : " loses flip.";

        // Play the Flip A Coin sound
        Singletons.getModel().getGame().getEvents().post(new FlipCoinEvent());

        JOptionPane.showMessageDialog(null, source.getName() + " - " + caller + winMsg, source.getName(),
                JOptionPane.PLAIN_MESSAGE);
        return winFlip;
    }

    // not restricted to combat damage, not restricted to dealing damage to
    // creatures/players
    /**
     * <p>
     * executeDamageDealingEffects.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeDamageDealingEffects(final Card source, final int damage) {

        if (damage <= 0) {
            return;
        }

        if (source.hasKeyword("Lifelink")) {
            source.getController().gainLife(damage, source);
        }

    }

    // not restricted to combat damage, restricted to dealing damage to
    // creatures
    /**
     * <p>
     * executeDamageToCreatureEffects.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     * @param affected
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeDamageToCreatureEffects(final Card source, final Card affected, final int damage) {

        if (damage <= 0) {
            return;
        }

        if (affected.hasStartOfKeyword("When CARDNAME is dealt damage, destroy it.")) {
            final Ability ability = new Ability(source, "0") {
                @Override
                public void resolve() {
                    Singletons.getModel().getGame().getAction().destroy(affected);
                }
            };

            final Ability ability2 = new Ability(source, "0") {
                @Override
                public void resolve() {
                    Singletons.getModel().getGame().getAction().destroyNoRegeneration(affected);
                }
            };

            final StringBuilder sb = new StringBuilder();
            sb.append(affected).append(" - destroy");
            ability.setStackDescription(sb.toString());
            ability2.setStackDescription(sb.toString());
            final int amount = affected
                    .getAmountOfKeyword("When CARDNAME is dealt damage, destroy it. It can't be regenerated.");

            for (int i = 0; i < amount; i++) {
                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability2);
            }
            final int amount2 = affected.getAmountOfKeyword("When CARDNAME is dealt damage, destroy it.");

            for (int i = 0; i < amount2; i++) {
                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);
            }
        }

        // Play the Damage sound
        Singletons.getControl().getSoundSystem().playSync(SoundEffectType.Damage);
    }

    // this is for cards like Sengir Vampire
    /**
     * <p>
     * executeVampiricEffects.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public static void executeVampiricEffects(final Card c) {
        final ArrayList<String> a = c.getKeyword();
        for (int i = 0; i < a.size(); i++) {
            if (c.isInPlay()
                    && a.get(i)
                            .toString()
                            .startsWith(
                                    "Whenever a creature dealt damage by CARDNAME "
                                            + "this turn is put into a graveyard, put")) {
                final Card thisCard = c;
                final String kw = a.get(i).toString();
                final Ability ability2 = new Ability(c, "0") {
                    @Override
                    public void resolve() {
                        Counters counter = Counters.P1P1;
                        if (kw.contains("+2/+2")) {
                            counter = Counters.P2P2;
                        }
                        if (thisCard.isInPlay()) {
                            thisCard.addCounter(counter, 1);
                        }
                    }
                }; // ability2

                final StringBuilder sb = new StringBuilder();
                sb.append(c.getName());
                if (kw.contains("+2/+2")) {
                    sb.append(" - gets a +2/+2 counter");
                } else {
                    sb.append(" - gets a +1/+1 counter");
                }
                ability2.setStackDescription(sb.toString());

                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability2);

            }
        }
    }

    // not restricted to just combat damage, restricted to players
    /**
     * <p>
     * executeDamageToPlayerEffects.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param c
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeDamageToPlayerEffects(final Player player, final Card c, final int damage) {
        if (damage <= 0) {
            return;
        }

        final List<Card> playerLiches = player.getCardsIn(ZoneType.Battlefield, "Lich");

        for (final Card lich : playerLiches) {
            final SpellAbility ability = new Ability(lich, "0") {
                @Override
                public void resolve() {
                    for (int i = 0; i < damage; i++) {
                        List<Card> nonTokens = CardLists.filter(player.getCardsIn(ZoneType.Battlefield), Presets.NON_TOKEN);
                        if (nonTokens.size() == 0) {
                            player.loseConditionMet(GameLossReason.SpellEffect, lich.getName());
                        } else {
                            player.sacrificePermanent("Select a permanent to sacrifice", nonTokens);
                        }
                    }
                }
            };

            final StringBuilder sb = new StringBuilder();
            sb.append(lich.getName()).append(" - ").append(lich.getController());
            sb.append(" sacrifices ").append(damage).append(" nontoken permanents.");
            ability.setStackDescription(sb.toString());

            Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

        }

        c.getDamageHistory().registerDamage(player);

        // Play the Life Loss sound
        Singletons.getControl().getSoundSystem().playSync(SoundEffectType.LifeLoss);
    }

    // restricted to combat damage, restricted to players
    /**
     * <p>
     * executeCombatDamageToPlayerEffects.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param c
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeCombatDamageToPlayerEffects(final Player player, final Card c, final int damage) {

        if (damage <= 0) {
            return;
        }

        if (c.hasStartOfKeyword("Poisonous")) {
            final int keywordPosition = c.getKeywordPosition("Poisonous");
            final String parse = c.getKeyword().get(keywordPosition).toString();
            final String[] k = parse.split(" ");
            final int poison = Integer.parseInt(k[1]);
            final Card crd = c;

            final Ability ability = new Ability(c, "0") {
                @Override
                public void resolve() {
                    final Player player = crd.getController();
                    final Player opponent = player.getOpponent();
                    opponent.addPoisonCounters(poison, c);
                }
            };

            final StringBuilder sb = new StringBuilder();
            sb.append(c);
            sb.append(" - Poisonous: ");
            sb.append(c.getController().getOpponent());
            sb.append(" gets ");
            sb.append(poison);
            sb.append(" poison counter");
            if (poison != 1) {
                sb.append("s");
            }
            sb.append(".");

            ability.setStackDescription(sb.toString());
            final ArrayList<String> keywords = c.getKeyword();

            for (int i = 0; i < keywords.size(); i++) {
                if (keywords.get(i).startsWith("Poisonous")) {
                    Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);
                }

            }
        }

        if (c.getName().equals("Scalpelexis")) {
            GameActionUtil.playerCombatDamageScalpelexis(c);
        }
        if (c.isEnchantedBy("Celestial Mantle")) {
            GameActionUtil.executeCelestialMantle(c);
        }

        c.getDamageHistory().registerCombatDamage(player);

        // Play the Life Loss sound
        Singletons.getControl().getSoundSystem().playSync(SoundEffectType.LifeLoss);
    } // executeCombatDamageToPlayerEffects

    /**
     * <p>
     * executeCelestialMantle.
     * </p>
     * 
     * @param enchanted
     *            a {@link forge.Card} object.
     */
    private static void executeCelestialMantle(final Card enchanted) {
        final ArrayList<Card> auras = enchanted.getEnchantedBy();
        for (final Card aura : auras) {
            if (aura.getName().equals("Celestial Mantle")) {
                final Ability doubleLife = new Ability(aura, "0") {
                    @Override
                    public void resolve() {
                        final int life = enchanted.getController().getLife();
                        enchanted.getController().setLife(life * 2, aura);
                    }
                };
                doubleLife.setStackDescription(aura.getName() + " - " + enchanted.getController()
                        + " doubles his or her life total.");

                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(doubleLife);

            }
        }
    }

    /**
     * <p>
     * playerCombatDamageScalpelexis.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    private static void playerCombatDamageScalpelexis(final Card c) {
        final Player player = c.getController();
        final Player opponent = player.getOpponent();

        if (c.getNetAttack() > 0) {
            final Ability ability = new Ability(c, "0") {
                @Override
                public void resolve() {

                    final List<Card> libList = opponent.getCardsIn(ZoneType.Library);
                    int count = 0;
                    int broken = 0;
                    for (int i = 0; i < libList.size(); i = i + 4) {
                        Card c1 = null;
                        Card c2 = null;
                        Card c3 = null;
                        Card c4 = null;
                        if (i < libList.size()) {
                            c1 = libList.get(i);
                        } else {
                            broken = 1;
                        }
                        if ((i + 1) < libList.size()) {
                            c2 = libList.get(i + 1);
                        } else {
                            broken = 1;
                        }
                        if ((i + 2) < libList.size()) {
                            c3 = libList.get(i + 2);
                        } else {
                            broken = 1;
                        }
                        if ((i + 3) < libList.size()) {
                            c4 = libList.get(i + 3);
                        } else {
                            broken = 1;
                        }
                        if (broken == 0) {
                            if ((c1.getName().contains(c2.getName()) || c1.getName().contains(c3.getName())
                                    || c1.getName().contains(c4.getName()) || c2.getName().contains(c3.getName())
                                    || c2.getName().contains(c4.getName()) || c3.getName().contains(c4.getName()))) {
                                count = count + 1;
                            } else {
                                broken = 1;
                            }
                        }

                    }
                    count = (count * 4) + 4;
                    int max = count;
                    if (libList.size() < count) {
                        max = libList.size();
                    }

                    for (int j = 0; j < max; j++) {
                        final Card c = libList.get(j);
                        Singletons.getModel().getGame().getAction().exile(c);
                    }
                }
            }; // ability

            final StringBuilder sb = new StringBuilder();
            sb.append("Scalpelexis - ").append(opponent);
            sb.append(" exiles the top four cards of his or her library. ");
            sb.append("If two or more of those cards have the same name, repeat this process.");
            ability.setStackDescription(sb.toString());

            Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

        }
    }

    /** Constant <code>coatOfArms</code>. */
    private static Command coatOfArms = new Command() {
        private static final long serialVersionUID = 583505612126735693L;

        private final List<Card> gloriousAnthemList = new ArrayList<Card>();

        @Override
        public void execute() {
            final List<Card> list = this.gloriousAnthemList;
            // reset all cards in list - aka "old" cards
            for (int i2 = 0; i2 < list.size(); i2++) {
                list.get(i2).addSemiPermanentAttackBoost(-1);
                list.get(i2).addSemiPermanentDefenseBoost(-1);
            }
            // add +1/+1 to cards
            list.clear();
            final int num = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Coat of Arms")).size();
            final List<Card> creatures = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.CREATURES);

            for (Card c : creatures) {
                for (Card c2 : creatures) {
                    if (!c.equals(c2) && c.sharesCreatureTypeWith(c2)) {
                        for (int j = 0; j < num; j++) {
                            c.addSemiPermanentAttackBoost(1);
                            c.addSemiPermanentDefenseBoost(1);
                            this.gloriousAnthemList.add(c);
                        }
                    }
                }
            } // for outer
        } // execute
    }; // coatOfArms

    private static Command alphaStatus = new Command() {
        private static final long serialVersionUID = -3213793711304934358L;

        private final List<Card> previouslyPumped = new ArrayList<Card>();
        private final ArrayList<Integer> previouslyPumpedValue = new ArrayList<Integer>();

        @Override
        public void execute() {
            final List<Card> alphaStatuses = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Alpha Status"));

            final List<Card> allCreatures = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), Presets.CREATURES);

            for (int i = 0; i < this.previouslyPumped.size(); i++) {
                this.previouslyPumped.get(i).addSemiPermanentAttackBoost(0 - this.previouslyPumpedValue.get(i));
                this.previouslyPumped.get(i).addSemiPermanentDefenseBoost(0 - this.previouslyPumpedValue.get(i));
            }
            this.previouslyPumped.clear();
            this.previouslyPumpedValue.clear();

            for (final Card alpha : alphaStatuses) {
                final Card enchanted = alpha.getEnchantingCard();
                int totalbuff = 0;

                for (final Card othercreat : allCreatures) {
                    if (enchanted != othercreat && othercreat.sharesCreatureTypeWith(enchanted)) {
                        totalbuff += 2;
                    }
                }

                enchanted.addSemiPermanentAttackBoost(totalbuff);
                enchanted.addSemiPermanentDefenseBoost(totalbuff);
                this.previouslyPumped.add(enchanted);
                this.previouslyPumpedValue.add(totalbuff);
            }
        }

    };

    /** stores the Command. */
    private static Command umbraStalker = new Command() {
        private static final long serialVersionUID = -3500747003228938898L;

        @Override
        public void execute() {
            // get all creatures
            final List<Card> cards = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Umbra Stalker"));
            for (final Card c : cards) {
                final Player player = c.getController();
                final List<Card> grave = player.getCardsIn(ZoneType.Graveyard);
                final int pt = CardFactoryUtil.getNumberOfManaSymbolsByColor("B", grave);
                c.setBaseAttack(pt);
                c.setBaseDefense(pt);
            }
        } // execute()
    };

    /** Constant <code>Ajani_Avatar_Token</code>. */
    private static Command ajaniAvatarToken = new Command() {
        private static final long serialVersionUID = 3027329837165436727L;

        @Override
        public void execute() {
            List<Card> list = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);

            list = CardLists.filter(list, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return c.getName().equals("Avatar") && c.getImageName().equals("W N N Avatar");
                }
            });
            for (int i = 0; i < list.size(); i++) {
                final Card card = list.get(i);
                final int n = card.getController().getLife();
                card.setBaseAttack(n);
                card.setBaseDefense(n);
            } // for
        } // execute
    }; // Ajani Avatar

    /** Constant <code>oldManOfTheSea</code>. */
    private static Command oldManOfTheSea = new Command() {
        private static final long serialVersionUID = 8076177362922156784L;

        @Override
        public void execute() {
            final List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Old Man of the Sea"));
            for (final Card oldman : list) {
                if (!oldman.getGainControlTargets().isEmpty()) {
                    if (oldman.getNetAttack() < oldman.getGainControlTargets().get(0).getNetAttack()) {
                        final ArrayList<Command> coms = oldman.getGainControlReleaseCommands();
                        for (int i = 0; i < coms.size(); i++) {
                            coms.get(i).execute();
                        }
                    }
                }
            }
        }
    }; // Old Man of the Sea

    /** Constant <code>liuBei</code>. */
    private static Command liuBei = new Command() {

        private static final long serialVersionUID = 4235093010715735727L;

        @Override
        public void execute() {
            final List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Liu Bei, Lord of Shu"));

            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {

                    final Card c = list.get(i);
                    if (this.getsBonus(c)) {
                        c.setBaseAttack(4);
                        c.setBaseDefense(6);
                    } else {
                        c.setBaseAttack(2);
                        c.setBaseDefense(4);
                    }

                }
            }
        } // execute()

        private boolean getsBonus(final Card c) {
            for (Card card : c.getController().getCardsIn(ZoneType.Battlefield)) {
                if (card.getName().equals("Guan Yu, Sainted Warrior")
                        || card.getName().equals("Zhang Fei, Fierce Warrior")) {
                    return true;
                }
            }
            return false;
        }

    }; // Liu_Bei

    /** Constant <code>soundTheCallWolf</code>. */
    private static Command soundTheCallWolf = new Command() {
        private static final long serialVersionUID = 4614281706799537283L;

        @Override
        public void execute() {
            List<Card> list = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);
            list = CardLists.filter(list, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return c.getName().equals("Wolf")
                            && c.hasKeyword("This creature gets +1/+1 for each card "
                                    + "named Sound the Call in each graveyard.");
                }
            });

            for (int i = 0; i < list.size(); i++) {
                final Card c = list.get(i);
                c.setBaseAttack(1 + this.countSoundTheCalls());
                c.setBaseDefense(c.getBaseAttack());
            }
        }

        private int countSoundTheCalls() {
            List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Graveyard), CardPredicates.nameEquals("Sound the Call"));
            return list.size();
        }

    }; // sounTheCallWolf

    /** Constant <code>Tarmogoyf</code>. */
    private static Command tarmogoyf = new Command() {
        private static final long serialVersionUID = 5895665460018262987L;

        @Override
        public void execute() {
            // get all creatures
            final List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Tarmogoyf"));

            for (int i = 0; i < list.size(); i++) {
                final Card c = list.get(i);
                c.setBaseAttack(this.countDiffTypes());
                c.setBaseDefense(c.getBaseAttack() + 1);
            }

        } // execute()

        private int countDiffTypes() {
            final List<Card> list = Singletons.getModel().getGame().getCardsIn(ZoneType.Graveyard);

            int count = 0;
            for (Card c : list) {
                if (c.isCreature()) {
                    count++;
                    break;
                }
            }
            for (Card c : list) {
                if (c.isSorcery()) {
                    count++;
                    break;
                }
            }
            for (Card c : list) {
                if (c.isInstant()) {
                    count++;
                    break;
                }
            }
            for (Card c : list) {
                if (c.isArtifact()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isEnchantment()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isLand()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isPlaneswalker()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isTribal()) {
                    count++;
                    break;
                }
            }
            return count;
        }
    };

    /** Constant <code>commands</code>. */
    private static HashMap<String, Command> commands = new HashMap<String, Command>();

    static {
        // Please add cards in alphabetical order so they are easier to find

        GameActionUtil.getCommands().put("Ajani_Avatar_Token", GameActionUtil.ajaniAvatarToken);
        GameActionUtil.getCommands().put("Alpha_Status", GameActionUtil.alphaStatus);
        GameActionUtil.getCommands().put("Coat_of_Arms", GameActionUtil.coatOfArms);

        GameActionUtil.getCommands().put("Liu_Bei", GameActionUtil.liuBei);
        GameActionUtil.getCommands().put("Old_Man_of_the_Sea", GameActionUtil.oldManOfTheSea);
        GameActionUtil.getCommands().put("Sound_the_Call_Wolf", GameActionUtil.soundTheCallWolf);
        GameActionUtil.getCommands().put("Tarmogoyf", GameActionUtil.tarmogoyf);
        GameActionUtil.getCommands().put("Umbra_Stalker", GameActionUtil.umbraStalker);

        // The commands above are in alphabetical order by cardname.
    }

    /**
     * <p>
     * doPowerSink.
     * </p>
     * 
     * @param p
     *            a {@link forge.game.player.Player} object.
     */
    public static void doPowerSink(final Player p) {
        // get all lands with mana abilities
        List<Card> lands = p.getLandsInPlay();
        lands = CardLists.filter(lands, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return c.getManaAbility().size() > 0;
            }
        });
        // tap them
        for (final Card c : lands) {
            c.tap();
        }

        // empty mana pool
        p.getManaPool().clearPool(false);
    }

    /**
     * Gets the commands.
     * 
     * @return the commands
     */
    public static HashMap<String, Command> getCommands() {
        return GameActionUtil.commands;
    }

    /**
     * Sets the commands.
     * 
     * @param commands0
     *            the commands to set
     */
    public static void setCommands(final HashMap<String, Command> commands0) {
        GameActionUtil.commands = commands0;
    }

    /**
     * Gets the st land mana abilities.
     * 
     * @return the stLandManaAbilities
     */
    public static void grantBasicLandsManaAbilities() {
        final HashMap<String, String> produces = new HashMap<String, String>();
        /*
         * for future use boolean naked =
         * AllZoneUtil.isCardInPlay("Naked Singularity"); boolean twist =
         * AllZoneUtil.isCardInPlay("Reality Twist"); //set up what they
         * produce produces.put("Forest", naked || twist ? "B" : "G");
         * produces.put("Island", naked == true ? "G" : "U"); if(naked)
         * produces.put("Mountain", "U"); else if(twist)
         * produces.put("Mountain", "W"); else produces.put("Mountain",
         * "R"); produces.put("Plains", naked || twist ? "R" : "W");
         * if(naked) produces.put("Swamp", "W"); else if(twist)
         * produces.put("Swamp", "G"); else produces.put("Swamp", "B");
         */
        produces.put("Forest", "G");
        produces.put("Island", "U");
        produces.put("Mountain", "R");
        produces.put("Plains", "W");
        produces.put("Swamp", "B");

        List<Card> lands = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);
        lands = CardLists.filter(lands, Presets.LANDS);

        // remove all abilities granted by this Command
        for (final Card land : lands) {
            List<SpellAbility> manaAbs = Lists.newArrayList(land.getManaAbility());
            // will get comodification exception without a different list
            for (final SpellAbility sa : manaAbs) {
                if (sa.getType().equals("BasicLandTypeMana")) {
                    land.removeSpellAbility(sa);
                }
            }
        }

        // add all appropriate mana abilities based on current types
        for (final Card land : lands) {
            for (String landType : Constant.Color.BASIC_LANDS) {

                if (land.isType(landType)) {
                    final AbilityFactory af = new AbilityFactory();
                    final SpellAbility sa = af.getAbility("AB$ Mana | Cost$ T | Produced$ " + produces.get(landType)
                            + " | SpellDescription$ Add " + produces.get(landType) + " to your mana pool.", land);
                    sa.setType("BasicLandTypeMana");
                    land.addSpellAbility(sa);
                }
            }
        }

    } // stLandManaAbilities

    /**
     * <p>
     * getAlternativeCosts.
     * </p>
     * 
     * @param sa
     *            a SpellAbility.
     * @return an ArrayList<SpellAbility>.
     * get alternative costs as additional spell abilities
     */
    public static final ArrayList<SpellAbility> getAlternativeCosts(SpellAbility sa) {
        ArrayList<SpellAbility> alternatives = new ArrayList<SpellAbility>();
        Card source = sa.getSourceCard();
        if (!sa.isBasicSpell()) {
            return alternatives;
        }
        for (final String keyword : source.getKeyword()) {
            if (sa.isSpell() && keyword.startsWith("Flashback")) {
                final SpellAbility flashback = sa.copy();
                flashback.setFlashBackAbility(true);
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setZone(ZoneType.Graveyard);
                flashback.setRestrictions(sar);

                // there is a flashback cost (and not the cards cost)
                if (!keyword.equals("Flashback")) {
                    final Cost fbCost = new Cost(source, keyword.substring(10), false);
                    flashback.setPayCosts(fbCost);
                }
                alternatives.add(flashback);
            }
            if (sa.isSpell() && keyword.equals("May be played without paying its mana cost")) {
                final SpellAbility newSA = sa.copy();
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setZone(null);
                newSA.setRestrictions(sar);
                final Cost cost = new Cost(source, "", false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost("");
                newSA.setDescription(sa.getDescription() + " (without paying its mana cost)");
                alternatives.add(newSA);
            }
            if (sa.isSpell() && keyword.equals("May be played by your opponent without paying its mana cost")) {
                final SpellAbility newSA = sa.copy();
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setZone(null);
                sar.setOpponentOnly(true);
                newSA.setRestrictions(sar);
                final Cost cost = new Cost(source, "", false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost("");
                newSA.setDescription(sa.getDescription() + " (without paying its mana cost)");
                alternatives.add(newSA);
            }
            if (sa.isSpell() && keyword.startsWith("May be played without paying its mana cost and as though it has flash")) {
                final SpellAbility newSA = sa.copy();
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setInstantSpeed(true);
                newSA.setRestrictions(sar);
                final Cost cost = new Cost(source, "", false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost("");
                newSA.setDescription(sa.getDescription() + " (without paying its mana cost and as though it has flash)");
                alternatives.add(newSA);
            }
            if (sa.isSpell() && keyword.startsWith("Alternative Cost")) {
                final SpellAbility newSA = sa.copy();
                final Cost cost = new Cost(source, keyword.substring(17), false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost("");
                String costString = cost.toSimpleString();
                if (costString.equals("")) {
                    costString = "0";
                }
                newSA.setDescription(sa.getDescription() + " (by paying " + costString + " instead of its mana cost)");
                alternatives.add(newSA);
            }
        }
        return alternatives;
    }

    public static Cost combineCosts(SpellAbility sa, String additionalCost) {
        final Cost newCost = new Cost(sa.getSourceCard(), additionalCost, false);
        Cost oldCost = sa.getPayCosts();
        return CostUtil.combineCosts(oldCost, newCost);
    }

    /**
     * <p>
     * getSpliceAbilities.
     * </p>
     * 
     * @param sa
     *            a SpellAbility.
     * @return an ArrayList<SpellAbility>.
     * get abilities with all Splice options
     */
    public static final ArrayList<SpellAbility> getSpliceAbilities(SpellAbility sa) {
        ArrayList<SpellAbility> newSAs = new ArrayList<SpellAbility>();
        ArrayList<SpellAbility> allSAs = new ArrayList<SpellAbility>();
        allSAs.add(sa);
        Card source = sa.getSourceCard();

        if (!sa.isSpell() || !source.isType("Arcane") || sa.getApi() == null) {
            return newSAs;
        }

        for (Card c : sa.getActivatingPlayer().getCardsIn(ZoneType.Hand)) {
            if (c.equals(source)) {
                continue;
            }
            for (String keyword : c.getKeyword()) {
                if (!keyword.startsWith("Splice")) {
                    continue;
                }
                String newSubSAString = c.getCharacteristics().getIntrinsicAbility().get(0);
                newSubSAString = newSubSAString.replace("SP", "DB");
                final AbilityFactory af = new AbilityFactory();
                final AbilitySub newSubSA = (AbilitySub) af.getAbility(newSubSAString, source);
                ArrayList<SpellAbility> addSAs = new ArrayList<SpellAbility>();
                // Add the subability to all existing variants
                for (SpellAbility s : allSAs) {
                    final SpellAbility newSA = s.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(combineCosts(newSA, keyword.substring(19)));
                    newSA.setManaCost("");
                    newSA.setDescription(s.getDescription() + " (Splicing " + c + " onto it)");
                    newSA.addSplicedCards(c);
                    SpellAbility child = newSA;
                    while (child.getSubAbility() != null) {
                        AbilitySub newChild = child.getSubAbility().getCopy();
                        child.setSubAbility(newChild);
                        newChild.setParent(child);
                        child = newChild;
                    }
                    child.setSubAbility(newSubSA);
                    newSubSA.setParent(child);
                    newSAs.add(0, newSA);
                    addSAs.add(newSA);
                }
                allSAs.addAll(addSAs);
                break;
            }
        }

        return newSAs;
    }

    /**
     * get optional additional costs.
     * 
     * @param original
     *            the original sa
     * @return an ArrayList<SpellAbility>.
     */
    public static ArrayList<SpellAbility> getOptionalAdditionalCosts(final SpellAbility original) {
        final ArrayList<SpellAbility> abilities = new ArrayList<SpellAbility>();
        final ArrayList<SpellAbility> newAbilities = new ArrayList<SpellAbility>();
        final Card source = original.getSourceCard();
        abilities.add(original);
        if (!original.isSpell()) {
            return abilities;
        }

        // Buyback, Kicker
        for (String keyword : source.getKeyword()) {
            if (keyword.startsWith("Buyback")) {
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, keyword.substring(8)));
                    newSA.setManaCost("");
                    newSA.setDescription(sa.getDescription() + " (with Buyback)");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    newSA.addOptionalAdditionalCosts("Buyback");
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                }
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            } else if (keyword.startsWith("Kicker")) {
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, keyword.substring(7)));
                    newSA.setManaCost("");
                    final Cost cost = new Cost(source, keyword.substring(7), false);
                    newSA.setDescription(sa.getDescription() + " (Kicker " + cost.toSimpleString() + ")");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    newSA.addOptionalAdditionalCosts(keyword);
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                }
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            } else if (keyword.startsWith("AlternateAdditionalCost")) {
                String costString1 = keyword.split(":")[1];
                String costString2 = keyword.split(":")[2];
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, costString1));
                    newSA.setManaCost("");
                    final Cost cost1 = new Cost(source, costString1, false);
                    newSA.setDescription(sa.getDescription() + " (Additional cost " + cost1.toSimpleString() + ")");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                    //second option
                    final SpellAbility newSA2 = sa.copy();
                    newSA2.setBasicSpell(false);
                    newSA2.setPayCosts(GameActionUtil.combineCosts(newSA2, costString2));
                    newSA2.setManaCost("");
                    final Cost cost2 = new Cost(source, costString2, false);
                    newSA2.setDescription(sa.getDescription() + " (Additional cost " + cost2.toSimpleString() + ")");
                    ArrayList<String> newoacs2 = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA2.setOptionalAdditionalCosts(newoacs2);
                    if (newSA2.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA2);
                    }
                }
                abilities.clear();
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            } else if (keyword.startsWith("Conspire")) {
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    final String conspireCost = "tapXType<2/Creature.SharesColorWith/untapped creature you control"
                            + " that shares a color with " + source.getName() + ">";
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, conspireCost));
                    newSA.setManaCost("");
                    newSA.setDescription(sa.getDescription() + " (Conspire)");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    newSA.addOptionalAdditionalCosts(keyword);
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                }
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            }
        }

        // Splice
        for (SpellAbility sa : abilities) {
            newAbilities.addAll(GameActionUtil.getSpliceAbilities(sa));
        }
        abilities.addAll(newAbilities);
        newAbilities.clear();

        return abilities;
    }
} // end class GameActionUtil
