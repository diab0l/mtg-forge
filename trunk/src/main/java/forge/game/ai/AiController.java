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
package forge.game.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.CardPredicates.Presets;
import forge.Constant;
import forge.GameEntity;
import forge.card.MagicColor;
import forge.card.ability.ApiType;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.CostDiscard;
import forge.card.cost.CostPart;
import forge.card.spellability.AbilityManaPart;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellPermanent;
import forge.game.GameActionUtil;
import forge.game.Game;
import forge.game.combat.Combat;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;
import forge.game.zone.ZoneType;
import forge.util.Aggregates;
import forge.util.Expressions;
import forge.util.MyRandom;

/**
 * <p>
 * ComputerAI_General class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class AiController {

    private final Player player;
    private final Game game;
    public Game getGame()
    {
        return game;
    }

    public Player getPlayer()
    {
        return player;
    }

    /**
     * <p>
     * Constructor for ComputerAI_General.
     * </p>
     */
    public AiController(final Player computerPlayer, final Game game0) {
        player = computerPlayer;
        game = game0;
    }

    /**
     * <p>
     * getAvailableSpellAbilities.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    private List<Card> getAvailableCards() {
        List<Card> all = new ArrayList<Card>(player.getCardsIn(ZoneType.Hand));

        all.addAll(player.getCardsIn(ZoneType.Graveyard));
        all.addAll(player.getCardsIn(ZoneType.Command));
        if (!player.getCardsIn(ZoneType.Library).isEmpty()) {
            all.add(player.getCardsIn(ZoneType.Library).get(0));
        }
        for(Player p : game.getPlayers()) {
            all.addAll(p.getCardsIn(ZoneType.Exile));
            all.addAll(p.getCardsIn(ZoneType.Battlefield));
        }
        return all;
    }

    /**
     * <p>
     * getPossibleETBCounters.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    private ArrayList<SpellAbility> getPossibleETBCounters() {

        final Player opp = player.getOpponent();
        List<Card> all = new ArrayList<Card>(player.getCardsIn(ZoneType.Hand));
        all.addAll(player.getCardsIn(ZoneType.Exile));
        all.addAll(player.getCardsIn(ZoneType.Graveyard));
        if (!player.getCardsIn(ZoneType.Library).isEmpty()) {
            all.add(player.getCardsIn(ZoneType.Library).get(0));
        }
        all.addAll(opp.getCardsIn(ZoneType.Exile));

        final ArrayList<SpellAbility> spellAbilities = new ArrayList<SpellAbility>();
        for (final Card c : all) {
            for (final SpellAbility sa : c.getNonManaSpellAbilities()) {
                if (sa instanceof SpellPermanent) {
                    sa.setActivatingPlayer(player);
                    if (SpellPermanent.checkETBEffects(c, sa, ApiType.Counter, player)) {
                        spellAbilities.add(sa);
                    }
                }
            }
        }
        return spellAbilities;
    }

    private List<SpellAbility> getOriginalAndAltCostAbilities(final List<SpellAbility> originList)
    {
        final ArrayList<SpellAbility> newAbilities = new ArrayList<SpellAbility>();
        for (SpellAbility sa : originList) {
            sa.setActivatingPlayer(player);
            //add alternative costs as additional spell abilities
            newAbilities.add(sa);
            newAbilities.addAll(GameActionUtil.getAlternativeCosts(sa));
        }
    
        final List<SpellAbility> result = new ArrayList<SpellAbility>();
        for (SpellAbility sa : newAbilities) {
            sa.setActivatingPlayer(player);
            result.addAll(GameActionUtil.getOptionalCosts(sa));
        }
        return result;
    }

    /**
     * Returns the spellAbilities from the card list.
     * 
     * @param l
     *            a {@link forge.CardList} object.
     * @return an array of {@link forge.card.spellability.SpellAbility} objects.
     */
    private ArrayList<SpellAbility> getSpellAbilities(final List<Card> l) {
        final ArrayList<SpellAbility> spellAbilities = new ArrayList<SpellAbility>();
        for (final Card c : l) {
            for (final SpellAbility sa : c.getSpellAbilities()) {
                spellAbilities.add(sa);
            }
        }
        return spellAbilities;
    }

    /**
     * <p>
     * getPlayableCounters.
     * </p>
     * 
     * @param l
     *            a {@link forge.CardList} object.
     * @return a {@link java.util.ArrayList} object.
     */
    private ArrayList<SpellAbility> getPlayableCounters(final List<Card> l) {
        final ArrayList<SpellAbility> spellAbility = new ArrayList<SpellAbility>();
        for (final Card c : l) {
            for (final SpellAbility sa : c.getNonManaSpellAbilities()) {
                // Check if this AF is a Counterpsell
                if (sa.getApi() == ApiType.Counter) {
                    spellAbility.add(sa);
                }
            }
        }

        return spellAbility;
    }

    // plays a land if one is available
    /**
     * <p>
     * chooseLandsToPlay.
     * </p>
     * 
     * @return a boolean.
     */
    public List<Card> getLandsToPlay() {
    
        final List<Card> hand = new ArrayList<Card>(player.getCardsIn(ZoneType.Hand));
        hand.addAll(player.getCardsIn(ZoneType.Exile));
        List<Card> landList = CardLists.filter(hand, Presets.LANDS);
        List<Card> nonLandList = CardLists.filter(hand, Predicates.not(CardPredicates.Presets.LANDS));
        
        //filter out cards that can't be played
        landList = CardLists.filter(landList, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return player.canPlayLand(c);
            }
        });
    
        final List<Card> landsNotInHand = new ArrayList<Card>(player.getCardsIn(ZoneType.Graveyard));
        if (!player.getCardsIn(ZoneType.Library).isEmpty()) {
            landsNotInHand.add(player.getCardsIn(ZoneType.Library).get(0));
        }
        for (final Card crd : landsNotInHand) {
            if (crd.isLand() && crd.hasKeyword("May be played")) {
                landList.add(crd);
            }
        }
        if (landList.isEmpty()) {
            return null;
        }
        if (landList.size() == 1 && nonLandList.size() < 3) {
            List<Card> cardsInPlay = player.getCardsIn(ZoneType.Battlefield);
            List<Card> landsInPlay = CardLists.filter(cardsInPlay, Presets.LANDS);
            List<Card> allCards = new ArrayList<Card>(player.getCardsIn(ZoneType.Graveyard));
            allCards.addAll(player.getCardsIn(ZoneType.Command));
            allCards.addAll(cardsInPlay);
            int maxCmcInHand = Aggregates.max(hand, CardPredicates.Accessors.fnGetCmc);
            int max = Math.max(maxCmcInHand, 6);
            // consider not playing lands if there are enough already and an ability with a discard cost is present
            if (landsInPlay.size() + landList.size() > max) {
                for (Card c : allCards) {
                    for (SpellAbility sa : c.getSpellAbilities()) {
                        if (sa.getPayCosts() != null) {
                            for (CostPart part : sa.getPayCosts().getCostParts()) {
                                if (part instanceof CostDiscard) {
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        }
    
        landList = CardLists.filter(landList, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                if (c.getSVar("NeedsToPlay").length() > 0) {
                    final String needsToPlay = c.getSVar("NeedsToPlay");
                    List<Card> list = game.getCardsIn(ZoneType.Battlefield);
    
                    list = CardLists.getValidCards(list, needsToPlay.split(","), c.getController(), c);
                    if (list.isEmpty()) {
                        return false;
                    }
                }
                if (c.getSVar("NeedsToPlayVar").length() > 0) {
                    final String needsToPlay = c.getSVar("NeedsToPlayVar");
                    int x = 0;
                    int y = 0;
                    String sVar = needsToPlay.split(" ")[0];
                    String comparator = needsToPlay.split(" ")[1];
                    String compareTo = comparator.substring(2);
                    try {
                        x = Integer.parseInt(sVar);
                    } catch (final NumberFormatException e) {
                        x = CardFactoryUtil.xCount(c, c.getSVar(sVar));
                    }
                    try {
                        y = Integer.parseInt(compareTo);
                    } catch (final NumberFormatException e) {
                        y = CardFactoryUtil.xCount(c, c.getSVar(compareTo));
                    }
                    if (!Expressions.compare(x, comparator, y)) {
                        return false;
                    }
                }
                if (c.isType("Legendary") && !c.getName().equals("Flagstones of Trokair")) {
                    final List<Card> list = player.getCardsIn(ZoneType.Battlefield);
                    if (Iterables.any(list, CardPredicates.nameEquals(c.getName()))) {
                        return false;
                    }
                }
    
                // don't play the land if it has cycling and enough lands are
                // available
                final ArrayList<SpellAbility> spellAbilities = c.getSpellAbilities();
    
                final List<Card> hand = player.getCardsIn(ZoneType.Hand);
                List<Card> lands = player.getCardsIn(ZoneType.Battlefield);
                lands.addAll(hand);
                lands = CardLists.filter(lands, CardPredicates.Presets.LANDS);
                int maxCmcInHand = Aggregates.max(hand, CardPredicates.Accessors.fnGetCmc);
                for (final SpellAbility sa : spellAbilities) {
                    if (sa.isCycling()) {
                        if (lands.size() >= Math.max(maxCmcInHand, 6)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        });
    
        return landList;
    }

    public Card chooseBestLandToPlay(List<Card> landList)
    {
        if (landList.isEmpty())
            return null;
    
        //Skip reflected lands.
        List<Card> unreflectedLands = new ArrayList<Card>(landList);
        for (Card l : landList) {
            if (l.isReflectedLand()) {
                unreflectedLands.remove(l);
            }
        }

        if (!unreflectedLands.isEmpty()) {
            landList = unreflectedLands;
        }

        // Choose first land to be able to play a one drop
        if (player.getLandsInPlay().isEmpty()) {
            List<Card> oneDrops = CardLists.filter(player.getCardsIn(ZoneType.Hand), CardPredicates.hasCMC(1));
            for (int i = 0; i < MagicColor.WUBRG.length; i++) {
                byte color = MagicColor.WUBRG[i];
                if (!CardLists.filter(oneDrops, CardPredicates.isColor(color)).isEmpty()) {
                    for (Card land : landList) {
                        if (land.isType(Constant.Color.BASIC_LANDS.get(i)))
                            return land;
                        
                        for (final SpellAbility m : ComputerUtilMana.getAIPlayableMana(land)) {
                            AbilityManaPart mp = m.getManaPart();
                            if (mp.canProduce(MagicColor.toShortString(color))) {
                                return land;
                            }
                        }
                    }
                }
            }
        }

        //play basic lands that are needed the most
        if (Iterables.any(landList, CardPredicates.Presets.BASIC_LANDS)) {
            final List<Card> combined = player.getCardsIn(ZoneType.Battlefield);
    
            final ArrayList<String> basics = new ArrayList<String>();
    
            // what types can I go get?
            for (final String name : Constant.CardTypes.BASIC_TYPES) {
                if (Iterables.any(landList, CardPredicates.isType(name))) {
                    basics.add(name);
                }
            }
    
            // Which basic land is least available from hand and play, that I still
            // have in my deck
            int minSize = Integer.MAX_VALUE;
            String minType = null;
    
            for (int i = 0; i < basics.size(); i++) {
                final String b = basics.get(i);
                final int num = CardLists.getType(combined, b).size();
                if (num < minSize) {
                    minType = b;
                    minSize = num;
                }
            }
    
            if (minType != null) {
                landList = CardLists.getType(landList, minType);
            }
        }
        return landList.get(0);
    }

    // if return true, go to next phase
    /**
     * <p>
     * playCounterSpell.
     * </p>
     * 
     * @param possibleCounters
     *            a {@link java.util.ArrayList} object.
     * @return a boolean.
     */
    private SpellAbility chooseCounterSpell(final List<SpellAbility> possibleCounters) {
        if ( possibleCounters == null || possibleCounters.isEmpty())
            return null;;
        
        SpellAbility bestSA = null;
        int bestRestriction = Integer.MIN_VALUE;


        for (final SpellAbility sa : getOriginalAndAltCostAbilities(possibleCounters)) {
            SpellAbility currentSA = sa;
            sa.setActivatingPlayer(player);
            // check everything necessary
            if (canPlayAndPayFor(currentSA)) {
                if (bestSA == null) {
                    bestSA = currentSA;
                    bestRestriction = ComputerUtil.counterSpellRestriction(player, currentSA);
                } else {
                    // Compare bestSA with this SA
                    final int restrictionLevel = ComputerUtil.counterSpellRestriction(player, currentSA);
    
                    if (restrictionLevel > bestRestriction) {
                        bestRestriction = restrictionLevel;
                        bestSA = currentSA;
                    }
                }
            }
        }

        // TODO - "Look" at Targeted SA and "calculate" the threshold
        // if (bestRestriction < targetedThreshold) return false;
        return bestSA;
    } // playCounterSpell()

    // if return true, go to next phase
    /**
     * <p>
     * playSpellAbilities.
     * </p>
     * 
     * @param all
     *            an array of {@link forge.card.spellability.SpellAbility}
     *            objects.
     * @return a boolean.
     */
    private SpellAbility chooseSpellAbilyToPlay(final List<SpellAbility> all, boolean skipCounter) {
        if ( all == null || all.isEmpty() )
            return null;

        Collections.sort(all, saComparator); // put best spells first
        
        for (final SpellAbility sa : getOriginalAndAltCostAbilities(all)) {
            // Don't add Counterspells to the "normal" playcard lookups
            if (sa.getApi() == ApiType.Counter && skipCounter) {
                continue;
            }
            sa.setActivatingPlayer(player);
            
            if (!canPlayAndPayFor(sa))
                continue;
    
            return sa;
        }
        
        return null;
    } // playCards()


    // This is for playing spells regularly (no Cascade/Ripple etc.)
    private boolean canPlayAndPayFor(final SpellAbility sa) {
        if (!sa.canPlay()) {
            return false;
        }
        //System.out.printf("Ai thinks of %s @ %s >>> ", sa, sa.getActivatingPlayer().getGame().getPhaseHandler().debugPrintState());
        if (!sa.canPlayAI()) {
            return false;
        }
        //System.out.printf("wouldPlay: %s, canPay: %s%n", aiWouldPlay, canPay);
        return ComputerUtilCost.canPayCost(sa, player);
    }
    
    // not sure "playing biggest spell" matters?
    private final static Comparator<SpellAbility> saComparator = new Comparator<SpellAbility>() {
        @Override
        public int compare(final SpellAbility a, final SpellAbility b) {
            // sort from highest cost to lowest
            // we want the highest costs first
            int a1 = a.getPayCosts() == null ? 0 : a.getPayCosts().getTotalMana().getCMC();
            int b1 = b.getPayCosts() == null ? 0 : b.getPayCosts().getTotalMana().getCMC();

            // deprioritize planar die roll marked with AIRollPlanarDieParams:LowPriority$ True
            if (ApiType.RollPlanarDice == a.getApi() && a.getSourceCard().hasSVar("AIRollPlanarDieParams") && a.getSourceCard().getSVar("AIRollPlanarDieParams").toLowerCase().matches(".*lowpriority\\$\\s*true.*")) {
                return 1;
            } else if (ApiType.RollPlanarDice == b.getApi() && b.getSourceCard().hasSVar("AIRollPlanarDieParams") && b.getSourceCard().getSVar("AIRollPlanarDieParams").toLowerCase().matches(".*lowpriority\\$\\s*true.*")) {
                return -1;
            }
    
            // cast 0 mana cost spells first (might be a Mox)
            if (a1 == 0) {
                return -1;
            } else if (b1 == 0) {
                return 1;
            }

            a1 += getSpellAbilityPriority(a);
            b1 += getSpellAbilityPriority(b);

            return b1 - a1;
        }
        
        private int getSpellAbilityPriority(SpellAbility sa) {
            int p = 0;
            Card source = sa.getSourceCard();
            // puts creatures in front of spells
            if (source.isCreature()) {
                p += 1;
            }
            // don't play equipments before having any creatures
            if (source.isEquipment() && sa.getSourceCard().getController().getCreaturesInPlay().isEmpty()) {
                p -= 9;
            }
            // artifacts and enchantments with effects that do not stack
            if ("True".equals(source.getSVar("NonStackingEffect")) && source.getController().isCardInPlay(source.getName())) {
                p -= 9;
            }
            // sort planeswalker abilities for ultimate
            if (sa.getRestrictions().getPlaneswalker()) {
                if (sa.hasParam("Ultimate")) {
                    p += 9;
                }
            }
    
            if (ApiType.DestroyAll == sa.getApi()) {
                p += 4;
            }

            return p;
        }
    }; // Comparator
    
    /**
     * <p>
     * AI_discardNumType.
     * </p>
     * 
     * @param numDiscard
     *            a int.
     * @param uTypes
     *            an array of {@link java.lang.String} objects. May be null for
     *            no restrictions.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a List<Card> of discarded cards.
     */
    public List<Card> getCardsToDiscard(final int numDiscard, final String[] uTypes, final SpellAbility sa) {
        List<Card> hand = new ArrayList<Card>(player.getCardsIn(ZoneType.Hand));

    
        if ((uTypes != null) && (sa != null)) {
            hand = CardLists.getValidCards(hand, uTypes, sa.getActivatingPlayer(), sa.getSourceCard());
        }
        return getCardsToDiscard(numDiscard, numDiscard, hand, sa);
    }
    
    public List<Card> getCardsToDiscard(final int min, final int max, final List<Card> validCards, final SpellAbility sa) {
        
        if (validCards.size() < min) {
            return null;
        }

        Card sourceCard = null;
        final List<Card> discardList = new ArrayList<Card>();
        int count = 0;
        if (sa != null) {
            sourceCard = sa.getSourceCard();
        }
    
        // look for good discards
        while (count < min) {
            Card prefCard = null;
            if (sa != null && sa.getActivatingPlayer() != null && sa.getActivatingPlayer().isOpponentOf(player)) {
                for (Card c : validCards) {
                    if (c.hasKeyword("If a spell or ability an opponent controls causes you to discard CARDNAME,"
                            + " put it onto the battlefield instead of putting it into your graveyard.")
                            || !c.getSVar("DiscardMeByOpp").isEmpty()) {
                        prefCard = c;
                        break;
                    }
                }
            }
            if (prefCard == null) {
                prefCard = ComputerUtil.getCardPreference(player, sourceCard, "DiscardCost", validCards);
            }
            if (prefCard != null) {
                discardList.add(prefCard);
                validCards.remove(prefCard);
                count++;
            } else {
                break;
            }
        }
    
        final int discardsLeft = min - count;
    
        // choose rest
        for (int i = 0; i < discardsLeft; i++) {
            if (validCards.isEmpty()) {
                continue;
            }
            final int numLandsInPlay = Iterables.size(Iterables.filter(player.getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.LANDS));
            final List<Card> landsInHand = CardLists.filter(validCards, CardPredicates.Presets.LANDS);
            final int numLandsInHand = landsInHand.size();
    
            // Discard a land
            boolean canDiscardLands = numLandsInHand > 3  || (numLandsInHand > 2 && numLandsInPlay > 0)
            || (numLandsInHand > 1 && numLandsInPlay > 2) || (numLandsInHand > 0 && numLandsInPlay > 5);
    
            if (canDiscardLands) {
                discardList.add(landsInHand.get(0));
                validCards.remove(landsInHand.get(0));
            } else { // Discard other stuff
                CardLists.sortByCmcDesc(validCards);
                int numLandsAvailable = numLandsInPlay;
                if (numLandsInHand > 0) {
                    numLandsAvailable++;
                }
                //Discard unplayable card
                if (validCards.get(0).getCMC() > numLandsAvailable) {
                    discardList.add(validCards.get(0));
                    validCards.remove(validCards.get(0));
                } else { //Discard worst card
                    Card worst = ComputerUtilCard.getWorstAI(validCards);
                    discardList.add(worst);
                    validCards.remove(worst);
                }
            }
        }
    
        return discardList;
    }

    @SuppressWarnings("incomplete-switch")
    public boolean confirmAction(SpellAbility sa, PlayerActionConfirmMode mode, String message) {
        ApiType api = sa.getApi();

        // Abilities without api may also use this routine, However they should provide a unique mode value
        if ( null == api ) {
            if( mode != null ) switch (mode) {
                case BraidOfFire: return true;
                // case Ripple: return true;
            }

            String exMsg = String.format("AI confirmAction does not know what to decide about %s mode (api is null).", mode);
            throw new IllegalArgumentException(exMsg);

        } else 
            return api.getAi().confirmAction(player, sa, mode, message);
    }

    public boolean confirmStaticApplication(Card hostCard, GameEntity affected, String logic, String message) {
        if (logic.equalsIgnoreCase("ProtectFriendly")) {
            final Player controller = hostCard.getController();
            if (affected instanceof Player) {
                return !((Player) affected).isOpponentOf(controller);
            } else if (affected instanceof Card) {
                return !((Card) affected).getController().isOpponentOf(controller);
            }
        }
        return true;
    }

    /**
     * AI decides if he wants to use dredge ability and which one if many available
     * @param dredgers - contains at least single element
     * @return
     */
    public Card chooseCardToDredge(List<Card> dredgers) {
        Player ai = getPlayer();
        //don't dredge when the library is nearly empty
        if (ai.getCardsIn(ZoneType.Library).size() < 8 && !ai.isCardInPlay("Laboratory Maniac")) {
            return null;
        }
        // use dredge if there are more than one of them in your graveyard
        if (dredgers.size() > 1 || MyRandom.getRandom().nextBoolean()) {
            return Aggregates.random(dredgers);
        }
        return null;
    }

    public String getProperty(AiProps propName) {
        return AiProfileUtil.getAIProp(getPlayer().getLobbyPlayer(), propName);
    }

    public int getIntProperty(AiProps propName) {
        return Integer.parseInt(AiProfileUtil.getAIProp(getPlayer().getLobbyPlayer(), propName));
    }

    public boolean getBooleanProperty(AiProps propName) {
        return Boolean.parseBoolean(AiProfileUtil.getAIProp(getPlayer().getLobbyPlayer(), propName));
    }

    /** Returns the spell ability which has already been played - use it for reference only */ 
    public SpellAbility chooseAndPlaySa(boolean mandatory, boolean withoutPayingManaCost, final SpellAbility... list) {
        return chooseAndPlaySa(Arrays.asList(list), mandatory, withoutPayingManaCost);
    }
    /** Returns the spell ability which has already been played - use it for reference only */
    public SpellAbility chooseAndPlaySa(final List<SpellAbility> choices, boolean mandatory, boolean withoutPayingManaCost) {
        for (final SpellAbility sa : choices) {
            sa.setActivatingPlayer(player);
            //Spells
            if (sa instanceof Spell) {
                if (!((Spell) sa).canPlayFromEffectAI(mandatory, withoutPayingManaCost)) {
                    continue;
                }
            } else {
                if (sa.canPlayAI()) {
                    continue;
                }
            }
            
            if ( withoutPayingManaCost )
                ComputerUtil.playSpellAbilityWithoutPayingManaCost(player, sa, game);
            else if (!ComputerUtilCost.canPayCost(sa, player)) 
                continue;
            else
                ComputerUtil.playStack(sa, player, game);
            return sa;
        }
        return null;
    }
    
    public void onPriorityRecieved() {
        final PhaseType phase = game.getPhaseHandler().getPhase();
        switch(phase) {
            case MAIN1:
            case MAIN2:
                Log.debug("Computer " + phase.nameForUi);
                
                if (game.getStack().isEmpty())
                    playLands();
                // fall through is intended
            default:
                playSpellAbilities(game);
                break;
        }
    }

    // declares blockers for given defender in a given combat
    public void declareBlockersFor(Player defender, Combat combat) {
        AiBlockController block = new AiBlockController(defender);
        // When player != defender, AI should declare blockers for its benefit.
        block.assignBlockers(combat);
    }
    

    public Combat declareAttackers(Player attacker, Combat combat) {
        // 12/2/10(sol) the decision making here has moved to getAttackers()
        AiAttackController aiAtk = new AiAttackController(attacker); 
        aiAtk.declareAttackers(combat);

        for (final Card element : combat.getAttackers()) {
            // tapping of attackers happens after Propaganda is paid for
            final StringBuilder sb = new StringBuilder();
            sb.append("Computer just assigned ").append(element.getName()).append(" as an attacker.");
            Log.debug(sb.toString());
        }

        // ai is about to attack, cancel all phase skipping
        for (Player p : game.getPlayers()) {
            p.getController().autoPassCancel();
        }
        return combat;
    }

    private void playLands() {
        final Player player = getPlayer();
        List<Card> landsWannaPlay = getLandsToPlay();
        
        while(landsWannaPlay != null && !landsWannaPlay.isEmpty() && player.canPlayLand(null)) {
            Card land = chooseBestLandToPlay(landsWannaPlay);
            if (ComputerUtil.damageFromETB(player, land) >= player.getLife() && player.canLoseLife()) {
                break;
            }
            landsWannaPlay.remove(land);
            player.playLand(land, false);
            game.getPhaseHandler().setPriority(player);
            game.getAction().checkStateEffects();
        }
    }

    private void playSpellAbilities(final Game game)
    {
        SpellAbility sa;
        do { 
            if ( game.isGameOver() )
                return;
            sa = getSpellAbilityToPlay();
            if ( sa == null ) break;
            //System.out.println("Playing sa: " + sa);
            if (!ComputerUtil.handlePlayingSpellAbility(player, sa, game)) {
                break;
            }
        } while ( sa != null );
    }

    private final SpellAbility getSpellAbilityToPlay() {
        // if top of stack is owned by me
        if (!game.getStack().isEmpty() && game.getStack().peekAbility().getActivatingPlayer().equals(player)) {
            // probably should let my stuff resolve
            return null;
        }
        final List<Card> cards = getAvailableCards();
    
        if ( !game.getStack().isEmpty() ) {
            SpellAbility counter = chooseCounterSpell(getPlayableCounters(cards));
            if( counter != null ) return counter;
    
            SpellAbility counterETB = chooseSpellAbilyToPlay(this.getPossibleETBCounters(), false);
            if( counterETB != null )
                return counterETB;
        }
    
        return chooseSpellAbilyToPlay(getSpellAbilities(cards), true);
    }

    public List<Card> chooseCardsToDelve(int colorlessCost, List<Card> grave) {
        List<Card> toExile = new ArrayList<Card>();
        int numToExile = Math.min(grave.size(), colorlessCost);
        
        for (int i = 0; i < numToExile; i++) {
            Card chosen = null;
            for (final Card c : grave) { // Exile noncreatures first in
                // case we can revive. Might
                // wanna do some additional
                // checking here for Flashback
                // and the like.
                if (!c.isCreature()) {
                    chosen = c;
                    break;
                }
            }
            if (chosen == null) {
                chosen = ComputerUtilCard.getWorstCreatureAI(grave);
            }

            if (chosen == null) {
                // Should never get here but... You know how it is.
                chosen = grave.get(0);
            }

            toExile.add(chosen);
            grave.remove(chosen);
        }
        return toExile;
    }
    
    public List<SpellAbility> chooseSaToActivateFromOpeningHand(List<SpellAbility> usableFromOpeningHand) {
        // AI would play everything. But limits to one copy of (Leyline of Singularity) and (Gemstone Caverns)
        
        List<SpellAbility> result = new ArrayList<SpellAbility>();
        for(SpellAbility sa : usableFromOpeningHand) {
            // Is there a better way for the AI to decide this?
            if (sa.doTrigger(false, player)) {
                result.add(sa);
            }
        }
        
        boolean hasLeyline1 = false;
        SpellAbility saGemstones = null;
        
        for(int i = 0; i < result.size(); i++) {
            SpellAbility sa = result.get(i);
            
            String srcName = sa.getSourceCard().getName();
            if("Gemstone Caverns".equals(srcName)) {
                if(saGemstones == null)
                    saGemstones = sa;
                else
                    result.remove(i--);
            } else if ("Leyline of Singularity".equals(srcName)) {
                if(!hasLeyline1)
                    hasLeyline1 = true;
                else
                    result.remove(i--);
            }
        }
        
        // Play them last
        if( saGemstones != null ) {
            result.remove(saGemstones);
            result.add(saGemstones);
        }
        
        return result;
    }

    public int chooseNumber(SpellAbility sa, String title, int min, int max) {
        //TODO: AILogic
        if ("GainLife".equals(sa.getParam("AILogic"))) {
            if (player.getLife() < 5 || player.getCardsIn(ZoneType.Hand).size() >= player.getMaxHandSize()) {
                return min;
            }
        }
        if ("Min".equals(sa.getParam("AILogic"))) {
            return min;
        }
        return max;
    }
}

