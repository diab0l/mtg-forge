package forge.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import forge.card.CardType;
import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.card.CounterType;
import forge.game.card.CardPredicates.Presets;
import forge.game.cost.CostAddMana;
import forge.game.cost.CostChooseCreatureType;
import forge.game.cost.CostDamage;
import forge.game.cost.CostDecisionMakerBase;
import forge.game.cost.CostDiscard;
import forge.game.cost.CostDraw;
import forge.game.cost.CostExile;
import forge.game.cost.CostExiledMoveToGrave;
import forge.game.cost.CostFlipCoin;
import forge.game.cost.CostGainControl;
import forge.game.cost.CostGainLife;
import forge.game.cost.CostMill;
import forge.game.cost.CostPartMana;
import forge.game.cost.CostPayLife;
import forge.game.cost.CostPutCardToLib;
import forge.game.cost.CostPutCounter;
import forge.game.cost.CostRemoveAnyCounter;
import forge.game.cost.CostRemoveCounter;
import forge.game.cost.CostReturn;
import forge.game.cost.CostReveal;
import forge.game.cost.CostSacrifice;
import forge.game.cost.CostTap;
import forge.game.cost.CostTapType;
import forge.game.cost.CostUnattach;
import forge.game.cost.CostUntap;
import forge.game.cost.CostUntapType;
import forge.game.cost.PaymentDecision;
import forge.game.cost.ICostVisitor;
import forge.game.player.Player;
import forge.game.player.PlayerControllerAi;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

public class AiCostDecision extends CostDecisionMakerBase implements ICostVisitor<PaymentDecision> {


    private final SpellAbility ability;
    private final Card source;
    
    public AiCostDecision(Player ai0, SpellAbility sa) {
        super(ai0);
        ability = sa;
        source = ability.getSourceCard();
    }

    @Override
    public PaymentDecision visit(CostAddMana cost) {
        Integer c = cost.convertAmount();
    
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }
    
        return PaymentDecision.number(c);
    }


    @Override
    public PaymentDecision visit(CostChooseCreatureType cost) {
        String choice = player.getController().chooseSomeType("Creature", ability, new ArrayList<String>(CardType.getCreatureTypes()), new ArrayList<String>());
        return PaymentDecision.type(choice);
    }

    @Override
    public PaymentDecision visit(CostDiscard cost) {
        final String type = cost.getType();

        final List<Card> hand = player.getCardsIn(ZoneType.Hand);
        if (type.equals("LastDrawn")) {
            if (!hand.contains(player.getLastDrawnCard())) {
                return null;
            }
            return PaymentDecision.card(player.getLastDrawnCard());
        }
        else if (cost.payCostFromSource()) {
            if (!hand.contains(source)) {
                return null;
            }

            return PaymentDecision.card(source);
        }
        else if (type.equals("Hand")) {
            return PaymentDecision.card(hand);
        }

        if (type.contains("WithSameName")) {
            return null;
        }
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            if (sVar.equals("XChoice")) {
                return null;
            }
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        if (type.equals("Random")) {
            return PaymentDecision.card(CardLists.getRandomSubList(hand, c));
        }
        else {
            final AiController aic = ((PlayerControllerAi)player.getController()).getAi();
            return PaymentDecision.card(aic.getCardsToDiscard(c, type.split(";"), ability));
        }
    }

    @Override
    public PaymentDecision visit(CostDamage cost) {
        Integer c = cost.convertAmount();

        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            // Generalize cost
            if (sVar.equals("XChoice")) {
                return null; // cannot pay
            } else {
                c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
            }
        }

        return PaymentDecision.number(c);
    }

    @Override
    public PaymentDecision visit(CostDraw cost) {
        Integer c = cost.convertAmount();

        if (c == null) {
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        return PaymentDecision.number(c);
    }

    @Override
    public PaymentDecision visit(CostExile cost) {
        if (cost.payCostFromSource()) {
            return PaymentDecision.card(source);
        }

        if (cost.getType().equals("All")) {
            return PaymentDecision.card(player.getCardsIn(cost.getFrom()));
        }
        else if (cost.getType().contains("FromTopGrave")) {
            return null;
        }

        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            // Generalize cost
            if (sVar.equals("XChoice")) {
                return null;
            }
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        if (cost.getFrom().equals(ZoneType.Library)) {
            return PaymentDecision.card(player.getCardsIn(ZoneType.Library, c));
        }
        else if (cost.sameZone) {
            // TODO Determine exile from same zone for AI
            return null;
        }
        else {
            List<Card> chosen = ComputerUtil.chooseExileFrom(player, cost.getFrom(), cost.getType(), source, ability.getTargetCard(), c);
            return null == chosen ? null : PaymentDecision.card(chosen);
        }
    }


    @Override
    public PaymentDecision visit(CostExiledMoveToGrave cost) {
        Integer c = cost.convertAmount();
        List<Card> chosen = new ArrayList<Card>();

        if (c == null) {
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        List<Card> typeList = player.getGame().getCardsIn(ZoneType.Exile);

        typeList = CardLists.getValidCards(typeList, cost.getType().split(";"), player, source);

        if (typeList.size() < c) {
            return null;
        }

        CardLists.sortByPowerAsc(typeList);
        Collections.reverse(typeList);

        for (int i = 0; i < c; i++) {
            chosen.add(typeList.get(i));
        }

        return chosen.isEmpty() ? null : PaymentDecision.card(chosen);
    }

    @Override
    public PaymentDecision visit(CostFlipCoin cost) {
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            // Generalize cost
            if (sVar.equals("XChoice")) {
                return null;
            }
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }
        return PaymentDecision.number(c);
    }

    @Override
    public PaymentDecision visit(CostGainControl cost) {
        if (cost.payCostFromSource())
            return PaymentDecision.card(source);
        
        Integer c = cost.convertAmount();
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }
    
        final List<Card> typeList = CardLists.getValidCards(player.getGame().getCardsIn(ZoneType.Battlefield), cost.getType().split(";"), player, source);
    
    
        if (typeList.size() < c) {
            return null;
        }
    
        CardLists.sortByPowerAsc(typeList);
        final List<Card> res = new ArrayList<Card>();
    
        for (int i = 0; i < c; i++) {
            res.add(typeList.get(i));
        }
        return res.isEmpty() ? null : PaymentDecision.card(res);
    }


    @Override
    public PaymentDecision visit(CostGainLife cost) {
        final List<Player> oppsThatCanGainLife = new ArrayList<Player>();
        
        for (final Player opp : cost.getPotentialTargets(player, source)) {
            if (opp.canGainLife()) {
                oppsThatCanGainLife.add(opp);
            }
        }
    
        if (oppsThatCanGainLife.size() == 0) {
            return null;
        }
    
        return PaymentDecision.players(oppsThatCanGainLife);
    }


    @Override
    public PaymentDecision visit(CostMill cost) {
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            // Generalize cost
            if (sVar.equals("XChoice")) {
                return null;
            }

            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        List<Card> topLib = player.getCardsIn(ZoneType.Library, c);
        return topLib.size() < c ? null : PaymentDecision.card(topLib);
    }

    @Override
    public PaymentDecision visit(CostPartMana cost) {
        return PaymentDecision.number(0);
    }

    @Override
    public PaymentDecision visit(CostPayLife cost) {
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            // Generalize cost
            if (sVar.equals("XChoice")) {
                return null;
            } else {
                c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
            }
        }
        if (!player.canPayLife(c)) {
            return null;
        }
        // activator.payLife(c, null);
        return PaymentDecision.number(c);
    }


    @Override
    public PaymentDecision visit(CostPutCardToLib cost) {
        Integer c = cost.convertAmount();
        final Game game = player.getGame();
        List<Card> chosen = new ArrayList<Card>();
        List<Card> list;

        if (cost.isSameZone()) {
            list = new ArrayList<Card>(game.getCardsIn(cost.getFrom()));
        } else {
            list = new ArrayList<Card>(player.getCardsIn(cost.getFrom()));
        }

        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            // Generalize cost
            if (sVar.equals("XChoice")) {
                return null;
            }
    
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        list = CardLists.getValidCards(list, cost.getType().split(";"), player, source);

        if (cost.isSameZone()) {
            // Jotun Grunt
            // TODO: improve AI
            final List<Player> players = game.getPlayers();
            for (Player p : players) {
                List<Card> enoughType = CardLists.filter(list, CardPredicates.isOwner(p));
                if (enoughType.size() >= c) {
                    chosen.addAll(enoughType);
                    break;
                }
            }
            chosen = chosen.subList(0, c);
        } else {
            chosen = ComputerUtil.choosePutToLibraryFrom(player, cost.getFrom(), cost.getType(), source, ability.getTargetCard(), c);
        }
        return chosen.isEmpty() ? null : PaymentDecision.card(chosen);
    }

    @Override
    public PaymentDecision visit(CostPutCounter cost) {

        if (cost.payCostFromSource()) {
            return PaymentDecision.card(source);

        }

        final List<Card> typeList = CardLists.getValidCards(player.getGame().getCardsIn(ZoneType.Battlefield), cost.getType().split(";"), player, source);

        Card card = null;
        if (cost.getType().equals("Creature.YouCtrl")) {
            card = ComputerUtilCard.getWorstCreatureAI(typeList);
        } else {
            card = ComputerUtilCard.getWorstPermanentAI(typeList, false, false, false, false);
        }
        return PaymentDecision.card(card);
    }


    @Override
    public PaymentDecision visit(CostTap cost) {
        return PaymentDecision.number(0);
    }

    @Override
    public PaymentDecision visit(CostTapType cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            if (sVar.equals("XChoice")) {
                List<Card> typeList =
                        CardLists.getValidCards(player.getCardsIn(ZoneType.Battlefield), cost.getType().split(";"), ability.getActivatingPlayer(), ability.getSourceCard());
                typeList = CardLists.filter(typeList, Presets.UNTAPPED);
                c = typeList.size();
                source.setSVar("ChosenX", "Number$" + Integer.toString(c));
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
        if (cost.getType().contains("sharesCreatureTypeWith") || cost.getType().contains("withTotalPowerGE")) {
            return null;
        }

        List<Card> totap = ComputerUtil.chooseTapType(player, cost.getType(), source, !cost.canTapSource, c);


        if (totap == null) {
            System.out.println("Couldn't find a valid card to tap for: " + source.getName());
            return null;
        }

        return PaymentDecision.card(totap);
    }


    @Override
    public PaymentDecision visit(CostSacrifice cost) {
        if (cost.payCostFromSource()) {
            return PaymentDecision.card(source);
        }
        if (cost.getAmount().equals("All")) {
            /*List<Card> typeList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Battlefield));
            typeList = CardLists.getValidCards(typeList, cost.getType().split(";"), activator, source);
            if (activator.hasKeyword("You can't sacrifice creatures to cast spells or activate abilities.")) {
                typeList = CardLists.getNotType(typeList, "Creature");
            }*/
            // Does the AI want to use Sacrifice All?
            return null;
        }

        Integer c = cost.convertAmount();
        if (c == null) {
            if (ability.getSVar(cost.getAmount()).equals("XChoice")) {
                return null;
            }

            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }
        List<Card> list = ComputerUtil.chooseSacrificeType(player, cost.getType(), source, ability.getTargetCard(), c);
        return PaymentDecision.card(list);
    }

    @Override
    public PaymentDecision visit(CostReturn cost) {
        if (cost.payCostFromSource())
            return PaymentDecision.card(source);
        
        Integer c = cost.convertAmount();
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        List<Card> res = ComputerUtil.chooseReturnType(player, cost.getType(), source, ability.getTargetCard(), c);
        return res.isEmpty() ? null : PaymentDecision.card(res);
    }

    @Override
    public PaymentDecision visit(CostReveal cost) {

        final String type = cost.getType();
        List<Card> hand = new ArrayList<Card>(player.getCardsIn(ZoneType.Hand));

        if (cost.payCostFromSource()) {
            if (!hand.contains(source)) {
                return null;
            }
            return PaymentDecision.card(source);
        }

        if (cost.getType().equals("Hand"))
            return PaymentDecision.card(player.getCardsIn(ZoneType.Hand));

        if (cost.getType().equals("SameColor")) {
            return null;
        }
            
        hand = CardLists.getValidCards(hand, type.split(";"), player, source);
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(cost.getAmount());
            if (sVar.equals("XChoice")) {
                c = hand.size();
            } else {
                c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
            }
        }

        final AiController aic = ((PlayerControllerAi)player.getController()).getAi();
        return PaymentDecision.card(aic.getCardsToDiscard(c, type.split(";"), ability));
    }

    @Override
    public PaymentDecision visit(CostRemoveAnyCounter cost) {
        final String amount = cost.getAmount();
        final int c = AbilityUtils.calculateAmount(source, amount, ability);
        final String type = cost.getType();

        List<Card> typeList = CardLists.getValidCards(player.getCardsIn(ZoneType.Battlefield), type.split(";"), player, source);
        List<Card> hperms = CardLists.filter(typeList, new Predicate<Card>() {
            @Override
            public boolean apply(final Card crd) {
                for (final CounterType c1 : CounterType.values()) {
                    if (crd.getCounters(c1) >= c  && ComputerUtil.isNegativeCounter(c1, crd)) {
                        return true;
                    }
                }
                return false;
            }
        });
        // Only find cards with enough negative counters
        // TODO: add ai for Chisei, Heart of Oceans
        return hperms.isEmpty() ? null : PaymentDecision.card(hperms);
    }

    @Override
    public PaymentDecision visit(CostRemoveCounter cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();
        final String type = cost.getType();

        if (c == null) {
            final String sVar = ability.getSVar(amount);
            if (sVar.equals("XChoice")) {
                return null;
            }
            if (amount.equals("All")) {
                c = source.getCounters(cost.counter);
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (!cost.payCostFromSource()) {
            List<Card> typeList;
            if (type.equals("OriginalHost")) {
                typeList = Lists.newArrayList(ability.getOriginalHost());
            } else {
                typeList = CardLists.getValidCards(player.getCardsIn(cost.zone), type.split(";"), player, source);
            }
            for (Card card : typeList) {
                if (card.getCounters(cost.counter) >= c) {
                    return PaymentDecision.card(card, c);
                }
            }
            return null;
        }

        if (c > source.getCounters(cost.counter)) {
            System.out.println("Not enough " + cost.counter + " on " + source.getName());
            return null;
        }

        return PaymentDecision.card(source, c);
    }

    @Override
    public PaymentDecision visit(CostUntapType cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            if (sVar.equals("XChoice")) {
                List<Card> typeList = player.getGame().getCardsIn(ZoneType.Battlefield);
                typeList = CardLists.getValidCards(typeList, cost.getType().split(";"), player, ability.getSourceCard());
                if (!cost.canUntapSource) {
                    typeList.remove(source);
                }
                typeList = CardLists.filter(typeList, Presets.TAPPED);
                c = typeList.size();
                source.setSVar("ChosenX", "Number$" + Integer.toString(c));
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
    
        List<Card> list = ComputerUtil.chooseUntapType(player, cost.getType(), source, cost.canUntapSource, c);
    
        if (list == null) {
            System.out.println("Couldn't find a valid card to untap for: " + source.getName());
            return null;
        }
    
        return PaymentDecision.card(list);
    }

    @Override
    public PaymentDecision visit(CostUntap cost) {
        return PaymentDecision.number(0);
    }

    @Override
    public PaymentDecision visit(CostUnattach cost) {
        Card cardToUnattach = cost.findCardToUnattach(source, (Player) player, ability);
        if (cardToUnattach == null) {
            // We really shouldn't be able to get here if there's nothing to unattach
            return null;
        }
        return PaymentDecision.card(cardToUnattach);
    }

    @Override
    public boolean paysRightAfterDecision() {
        return false;
    }
}

