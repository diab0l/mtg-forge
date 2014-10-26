package forge.player;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.card.CardType;
import forge.game.Game;
import forge.game.GameEntity;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardCollectionView;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.card.CardPredicates.Presets;
import forge.game.card.CardView;
import forge.game.card.CounterType;
import forge.game.cost.*;
import forge.game.player.Player;
import forge.game.player.PlayerView;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityStackInstance;
import forge.game.zone.ZoneType;
import forge.match.input.InputSelectCardsFromList;
import forge.match.input.InputSelectManyBase;
import forge.util.Aggregates;
import forge.util.FCollectionView;
import forge.util.ITriggerEvent;
import forge.util.Lang;
import forge.util.gui.SGuiChoose;
import forge.util.gui.SGuiDialog;

import java.util.*;
import java.util.Map.Entry;

public class HumanCostDecision extends CostDecisionMakerBase {
    private final PlayerControllerHuman controller;
    private final SpellAbility ability;
    private final Card source;

    public HumanCostDecision(final PlayerControllerHuman controller, final Player p, final SpellAbility sa, final Card source) {
        super(p);
        this.controller = controller;
        ability = sa;
        this.source = source;
    }

    protected int chooseXValue(final int maxValue) {
        /*final String chosen = sa.getSVar("ChosenX");
        if (chosen.length() > 0) {
            return AbilityFactory.calculateAmount(card, "ChosenX", null);
        }*/

        int chosenX = player.getController().chooseNumber(ability, source.toString() + " - Choose a Value for X", 0, maxValue);
        ability.setSVar("ChosenX", Integer.toString(chosenX));
        source.setSVar("ChosenX", Integer.toString(chosenX));
        return chosenX;
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
        String choice = controller.chooseSomeType("Creature", ability, new ArrayList<String>(CardType.Constant.CREATURE_TYPES), new ArrayList<String>(), true);
        if (null == choice)
            return null;
        return PaymentDecision.type(choice);
    }

    @Override
    public PaymentDecision visit(CostDiscard cost) {
        CardCollectionView hand = player.getCardsIn(ZoneType.Hand);
        String discardType = cost.getType();
        final String amount = cost.getAmount();

        if (cost.payCostFromSource()) {
            return hand.contains(source) ? PaymentDecision.card(source) : null;
        }

        if (discardType.equals("Hand")) {
            return PaymentDecision.card(hand);
        }

        if (discardType.equals("LastDrawn")) {
            final Card lastDrawn = player.getLastDrawnCard();
            return hand.contains(lastDrawn) ? PaymentDecision.card(lastDrawn) : null;
        }

        Integer c = cost.convertAmount();

        if (discardType.equals("Random")) {
            if (c == null) {
                final String sVar = ability.getSVar(amount);
                // Generalize this
                if (sVar.equals("XChoice")) {
                    c = chooseXValue(hand.size());
                }
                else {
                    c = AbilityUtils.calculateAmount(source, amount, ability);
                }
            }

            return PaymentDecision.card(Aggregates.random(hand, c, new CardCollection()));
        }
        if (discardType.contains("+WithSameName")) {
            String type = discardType.replace("+WithSameName", "");
            hand = CardLists.getValidCards(hand, type.split(";"), player, source);
            final CardCollectionView landList2 = hand;
            hand = CardLists.filter(hand, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    for (Card card : landList2) {
                        if (!card.equals(c) && card.getName().equals(c.getName())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (c == 0) {
                return PaymentDecision.card(new CardCollection());
            }
            CardCollection discarded = new CardCollection();
            while (c > 0) {
                InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, 1, 1, hand);
                inp.setMessage("Select one of the cards with the same name to discard. Already chosen: " + discarded);
                inp.setCancelAllowed(true);
                inp.showAndWait();
                if (inp.hasCancelled()) {
                    return null;
                }
                final Card first = inp.getFirstSelected();
                discarded.add(first);
                CardCollection filteredHand = CardLists.filter(hand, CardPredicates.nameEquals(first.getName()));
                filteredHand.remove(first);
                hand = filteredHand;
                c--;
            }
            return PaymentDecision.card(discarded);
        }
        
        String type = new String(discardType);
        final String[] validType = type.split(";");
        hand = CardLists.getValidCards(hand, validType, player, source);

        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(hand.size());
            }
            else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, c, hand);
        inp.setMessage("Select %d more " + cost.getDescriptiveType() + " to discard.");
        inp.setCancelAllowed(true);
        inp.showAndWait();
        if (inp.hasCancelled() || inp.getSelected().size() != c) {
            return null;
        }
        return PaymentDecision.card(inp.getSelected());
    }

    @Override
    public PaymentDecision visit(CostDamage cost) {
        final String amount = cost.getAmount();
        final int life = player.getLife();

        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(life);
            }
            else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (player.canPayLife(c) && player.getController().confirmPayment(cost, "Pay " + c + " Life?")) {
            return PaymentDecision.number(c);
        }
        return null;
    }

    @Override
    public PaymentDecision visit(CostDraw cost) {
        final String amount = cost.getAmount();

        Integer c = cost.convertAmount();
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, amount, ability);
        }

        if (!player.getController().confirmPayment(cost, "Draw " + c + " Card" + (c == 1 ? "" : "s"))) {
            return null;
        }

        return PaymentDecision.number(c);
    }

    @Override
    public PaymentDecision visit(CostExile cost) {
        final String amount = cost.getAmount();
        final Game game = player.getGame(); 

        Integer c = cost.convertAmount();
        String type = cost.getType();
        boolean fromTopGrave = false;
        if (type.contains("FromTopGrave")) {
            type = type.replace("FromTopGrave", "");
            fromTopGrave = true;
        }

        CardCollection list;
        if (cost.getFrom().equals(ZoneType.Stack)) {
            list = new CardCollection();
            for (SpellAbilityStackInstance si : game.getStack()) {
                list.add(si.getSourceCard());
            }
        }
        else if (cost.sameZone) {
            list = new CardCollection(game.getCardsIn(cost.from));
        }
        else {
            list = new CardCollection(player.getCardsIn(cost.from));
        }

        if (cost.payCostFromSource()) {
            return source.getZone() == player.getZone(cost.from) && player.getController().confirmPayment(cost, "Exile " + source.getName() + "?") ? PaymentDecision.card(source) : null;
        }

        if (type.equals("All")) {
            return PaymentDecision.card(list);
        }
        list = CardLists.getValidCards(list, type.split(";"), player, source);
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(list.size());
            }
            else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (cost.from == ZoneType.Battlefield || cost.from == ZoneType.Hand) {
            InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, c, list);
            inp.setMessage("Exile %d card(s) from your" + cost.from);
            inp.setCancelAllowed(true);
            inp.showAndWait();
            return inp.hasCancelled() ? null : PaymentDecision.card(inp.getSelected());
        }

        if (cost.from == ZoneType.Library) { return exileFromTop(cost, ability, player, c); }
        if (fromTopGrave) { return exileFromTopGraveType(ability, c, list); }
        if (!cost.sameZone) { return exileFromMiscZone(cost, ability, c, list); }

        FCollectionView<Player> players = game.getPlayers();
        List<Player> payableZone = new ArrayList<Player>();
        for (Player p : players) {
            CardCollection enoughType = CardLists.filter(list, CardPredicates.isOwner(p));
            if (enoughType.size() < c) {
                list.removeAll((CardCollectionView)enoughType);
            }
            else {
                payableZone.add(p);
            }
        }
        return exileFromSame(cost, list, c, payableZone);
    }
    


    // Inputs

    // Exile<Num/Type{/TypeDescription}>
    // ExileFromHand<Num/Type{/TypeDescription}>
    // ExileFromGrave<Num/Type{/TypeDescription}>
    // ExileFromTop<Num/Type{/TypeDescription}> (of library)
    // ExileSameGrave<Num/Type{/TypeDescription}>

    private PaymentDecision exileFromSame(CostExile cost, CardCollectionView list, int nNeeded, List<Player> payableZone) {
        if (nNeeded == 0) {
            return PaymentDecision.number(0);
        }
        final Player p = Player.get(SGuiChoose.oneOrNone(String.format("Exile from whose %s?", cost.getFrom()), PlayerView.getCollection(payableZone)));
        if (p == null) {
            return null;
        }

        CardCollection typeList = CardLists.filter(list, CardPredicates.isOwner(p));
        int count = typeList.size();
        if (count < nNeeded) {
            return null;
        }

        CardCollection toExile = Card.getList(SGuiChoose.many("Exile from " + cost.getFrom(), "To be exiled", nNeeded, CardView.getCollection(typeList), null));
        return PaymentDecision.card(toExile);
    }
    
    @Override
    public PaymentDecision visit(CostExileFromStack cost) {
        final String amount = cost.getAmount();
        final Game game = player.getGame(); 

        Integer c = cost.convertAmount();
        String type = cost.getType();
        List<SpellAbility> saList = new ArrayList<SpellAbility>();
        ArrayList<String> descList = new ArrayList<String>();

        for (SpellAbilityStackInstance si : game.getStack()) {
            final Card stC = si.getSourceCard();
            final SpellAbility stSA = si.getSpellAbility(true).getRootAbility();
            if (stC.isValid(cost.getType().split(";"), ability.getActivatingPlayer(), source) && stSA.isSpell()) {
                saList.add(stSA);
                if (stC.isCopiedSpell()) {
                    descList.add(stSA.getStackDescription() + " (Copied Spell)");
                } else {
                    descList.add(stSA.getStackDescription());
                }
            }
        }

        if (type.equals("All")) {
            return PaymentDecision.spellabilities(saList);
        }
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(saList.size());
            }
            else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (saList.size() < c) {
            return null;
        }
        
        List<SpellAbility> exiled = new ArrayList<SpellAbility>();
        for (int i = 0; i < c; i++) {
            //Have to use the stack descriptions here because some copied spells have no description otherwise
            final String o = SGuiChoose.oneOrNone("Exile from Stack", descList);

            if (o != null) {
                final SpellAbility toExile = saList.get(descList.indexOf(o));

                saList.remove(toExile);
                descList.remove(o);
                
                exiled.add(toExile);
            } else {
                return null;
            }
        }
        return PaymentDecision.spellabilities(exiled);
    }

    private PaymentDecision exileFromTop(final CostExile cost, final SpellAbility sa, final Player player, final int nNeeded) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Exile ").append(nNeeded).append(" cards from the top of your library?");
        final CardCollectionView list = player.getCardsIn(ZoneType.Library, nNeeded);

        if (list.size() > nNeeded || !player.getController().confirmPayment(cost, "Exile " + Lang.nounWithAmount(nNeeded, "card") + " from the top of your library?")) {
            return null;
        }
        return PaymentDecision.card(list);
    }

    private PaymentDecision exileFromMiscZone(CostExile cost, SpellAbility sa, int nNeeded, CardCollection typeList) {
        if (typeList.size() < nNeeded) { return null; }

        CardCollection exiled = new CardCollection();
        for (int i = 0; i < nNeeded; i++) {
            final Card c = Card.get(SGuiChoose.oneOrNone("Exile from " + cost.getFrom(), CardView.getCollection(typeList)));
            if (c == null) { return null; }

            typeList.remove(c);
            exiled.add(c);
        }
        return PaymentDecision.card(exiled);
    }

    private PaymentDecision exileFromTopGraveType(SpellAbility sa, int nNeeded, CardCollection typeList) {
        if (typeList.size() < nNeeded) { return null; }

        Collections.reverse(typeList);
        return PaymentDecision.card(Iterables.limit(typeList, nNeeded));
    }    

    @Override
    public PaymentDecision visit(CostExiledMoveToGrave cost) {
        Integer c = cost.convertAmount();
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        final Player activator = ability.getActivatingPlayer();
        CardCollection list = CardLists.getValidCards(activator.getGame().getCardsIn(ZoneType.Exile), cost.getType().split(";"), activator, source);

        if (list.size() < c) {
            return null;
        }
        final CardCollection choice = Card.getList(SGuiChoose.many("Choose an exiled card to put into graveyard", "To graveyard", c, CardView.getCollection(list), CardView.get(source)));
        return PaymentDecision.card(choice);
    }

    @Override
    public PaymentDecision visit(CostFlipCoin cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();

        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(cost.getLKIList().size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
        return PaymentDecision.number(c);
    }

    @Override
    public PaymentDecision visit(CostGainControl cost) {
        final String amount = cost.getAmount();

        Integer c = cost.convertAmount();
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, amount, ability);
        }
        final CardCollectionView list = player.getCardsIn(ZoneType.Battlefield);
        CardCollectionView validCards = CardLists.getValidCards(list, cost.getType().split(";"), player, source);

        InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, validCards);
        final String desc = cost.getTypeDescription() == null ? cost.getType() : cost.getTypeDescription();
        inp.setMessage("Gain control of %d " + desc);
        inp.showAndWait();
        if (inp.hasCancelled()) {
            return null;
        }
        return PaymentDecision.card(inp.getSelected());
    }

    @Override
    public PaymentDecision visit(CostGainLife cost) {
        final String amount = cost.getAmount();

        final int life = player.getLife();

        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(life);
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        final List<Player> oppsThatCanGainLife = new ArrayList<Player>();
        for (final Player opp : cost.getPotentialTargets(player, source)) {
            if (opp.canGainLife()) {
                oppsThatCanGainLife.add(opp);
            }
        }

        if (cost.getCntPlayers() == Integer.MAX_VALUE) // applied to all players who can gain
            return PaymentDecision.players(oppsThatCanGainLife);

        final StringBuilder sb = new StringBuilder();
        sb.append(source.getName()).append(" - Choose an opponent to gain ").append(c).append(" life:");

        final Player chosenToGain = Player.get(SGuiChoose.oneOrNone(sb.toString(), PlayerView.getCollection(oppsThatCanGainLife)));
        if (chosenToGain == null) {
            return null;
        }
        return PaymentDecision.players(Lists.newArrayList(chosenToGain));
    }

    @Override
    public PaymentDecision visit(CostMill cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();

        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(cost.getLKIList().size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (!player.getController().confirmPayment(cost, "Mill " + c + " card" + (c == 1 ? "" : "s") + " from your library?")) {
            return null;
        }
        return PaymentDecision.card(player.getCardsIn(ZoneType.Library, c));
    }

    @Override
    public PaymentDecision visit(CostPayLife cost) {
        final String amount = cost.getAmount();
        final int life = player.getLife();

        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.startsWith("XChoice")) {
                int limit = life;
                if (sVar.contains("LimitMax")) {
                    limit = AbilityUtils.calculateAmount(source, sVar.split("LimitMax.")[1], ability);
                }
                int maxLifePayment = limit < life ? limit : life;
                c = chooseXValue(maxLifePayment);
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (player.canPayLife(c) && player.getController().confirmPayment(cost, "Pay " + c + " Life?")) {
            return PaymentDecision.number(c);
        }
        return null;
    }

    @Override
    public PaymentDecision visit(CostPartMana cost) {
        // only interactive payment possible for now =(
        return new PaymentDecision(0);
    }

    @Override
    public PaymentDecision visit(CostPutCardToLib cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();

        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(cost.getLKIList().size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        CardCollection list = CardLists.getValidCards(cost.sameZone ? player.getGame().getCardsIn(cost.getFrom()) : player.getCardsIn(cost.getFrom()), cost.getType().split(";"), player, source);

        if (cost.from == ZoneType.Hand) {
            InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, c, list);
            inp.setMessage("Put %d card(s) from your " + cost.from);
            inp.setCancelAllowed(true);
            inp.showAndWait();
            return inp.hasCancelled() ? null : PaymentDecision.card(inp.getSelected());
        }
        
        if (cost.sameZone){
            FCollectionView<Player> players = player.getGame().getPlayers();
            List<Player> payableZone = new ArrayList<Player>();
            for (Player p : players) {
                CardCollectionView enoughType = CardLists.filter(list, CardPredicates.isOwner(p));
                if (enoughType.size() < c) {
                    list.removeAll(enoughType);
                } else {
                    payableZone.add(p);
                }
            }
            return putFromSame(list, c.intValue(), payableZone, cost.from);
        } else {//Graveyard
            return putFromMiscZone(ability, c.intValue(), list, cost.from);
        }
    }

    private PaymentDecision putFromMiscZone(SpellAbility sa, int nNeeded, CardCollection typeList, ZoneType fromZone) {
        if (typeList.size() < nNeeded) {
            return null;
        }

        CardCollection chosen = new CardCollection();
        for (int i = 0; i < nNeeded; i++) {
            final Card c = Card.get(SGuiChoose.oneOrNone("Put from " + fromZone + " to library", CardView.getCollection(typeList)));
            if (c == null) {
                return null;
            }
            typeList.remove(c);
            chosen.add(c);
        }
        return PaymentDecision.card(chosen);
    }

    private PaymentDecision putFromSame(CardCollectionView list, int nNeeded, List<Player> payableZone, ZoneType fromZone) {
        if (nNeeded == 0) {
            return PaymentDecision.number(0);
        }

        final Player p = Player.get(SGuiChoose.oneOrNone(String.format("Put cards from whose %s?", fromZone), PlayerView.getCollection(payableZone)));
        if (p == null) {
            return null;
        }
    
        CardCollection typeList = CardLists.filter(list, CardPredicates.isOwner(p));
        if (typeList.size() < nNeeded) {
            return null;
        }

        CardCollection chosen = new CardCollection();
        for (int i = 0; i < nNeeded; i++) {
            final Card c = Card.get(SGuiChoose.oneOrNone("Put cards from " + fromZone + " to Library", CardView.getCollection(typeList)));
            if (c == null) {
                return null;
            }
            typeList.remove(c);
            chosen.add(c);
        }
        return PaymentDecision.card(chosen);
    }
    
    @Override
    public PaymentDecision visit(CostPutCounter cost) {
        Integer c = cost.getNumberOfCounters(ability);

        if (cost.payCostFromSource()) {
            cost.setLastPaidAmount(c);
            return PaymentDecision.number(c);
        } 

        // Cards to use this branch: Scarscale Ritual, Wandering Mage - each adds only one counter 
        CardCollectionView typeList = CardLists.getValidCards(player.getCardsIn(ZoneType.Battlefield), cost.getType().split(";"), player, ability.getHostCard());
        
        InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, 1, 1, typeList);
        inp.setMessage("Put " + Lang.nounWithAmount(c, cost.getCounter().getName() + " counter") + " on " + cost.getDescriptiveType());
        inp.setCancelAllowed(true);
        inp.showAndWait();

        if (inp.hasCancelled()) {
            return null;
        }
        return PaymentDecision.card(inp.getSelected());
    }

    @Override
    public PaymentDecision visit(CostReturn cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();

        final CardCollectionView list = player.getCardsIn(ZoneType.Battlefield);
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(list.size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
        if (cost.payCostFromSource()) {
            final Card card = ability.getHostCard();
            if (card.getController() == player && card.isInPlay()) {
                final CardView view = CardView.get(card);
                return player.getController().confirmPayment(cost, "Return " + view + " to hand?") ? PaymentDecision.card(card) : null;
            }
        }
        else {
            CardCollectionView validCards = CardLists.getValidCards(ability.getActivatingPlayer().getCardsIn(ZoneType.Battlefield), cost.getType().split(";"), ability.getActivatingPlayer(), ability.getHostCard());

            InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, c, validCards);
            inp.setCancelAllowed(true);
            inp.setMessage("Return %d " + cost.getType() + " " + cost.getType() + " card(s) to hand");
            inp.showAndWait();
            if (inp.hasCancelled()) {
                return null;
            }
            return PaymentDecision.card(inp.getSelected());
       }
       return null;
    }

    @Override
    public PaymentDecision visit(CostReveal cost) {
        final String amount = cost.getAmount();

        if (cost.payCostFromSource()) {
            return PaymentDecision.card(source);
        }
        if (cost.getType().equals("Hand")) {
            return PaymentDecision.card(player.getCardsIn(ZoneType.Hand));
        }
        InputSelectCardsFromList inp = null;
        if (cost.getType().equals("SameColor")) {
            Integer num = cost.convertAmount();
            CardCollectionView hand = player.getCardsIn(ZoneType.Hand);
            final CardCollectionView hand2 = hand;
            hand = CardLists.filter(hand, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    for (Card card : hand2) {
                        if (!card.equals(c) && card.sharesColorWith(c)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (num == 0) {
                return PaymentDecision.number(0);
            }
            inp = new InputSelectCardsFromList(controller, num, hand) {
                private static final long serialVersionUID = 8338626212893374798L;

                @Override
                protected boolean onCardSelected(Card c, ITriggerEvent triggerEvent) {
                    Card firstCard = Iterables.getFirst(this.selected, null);
                    if (firstCard != null && !CardPredicates.sharesColorWith(firstCard).apply(c)) {
                        return false;
                    }
                    return super.onCardSelected(c, triggerEvent);
                }
            };
            inp.setMessage("Select " + Lang.nounWithAmount(num, "card") + " of same color to reveal.");
        }
        else {
            Integer num = cost.convertAmount();

            CardCollectionView hand = player.getCardsIn(ZoneType.Hand);
            hand = CardLists.getValidCards(hand, cost.getType().split(";"), player, ability.getHostCard());

            if (num == null) {
                final String sVar = ability.getSVar(amount);
                if (sVar.equals("XChoice")) {
                    num = chooseXValue(hand.size());
                } else {
                    num = AbilityUtils.calculateAmount(source, amount, ability);
                }
            }
            if (num == 0)
                return PaymentDecision.number(0);;
                
            inp = new InputSelectCardsFromList(controller, num, num, hand);
            inp.setMessage("Select %d more " + cost.getDescriptiveType() + " card(s) to reveal.");
        }
        inp.setCancelAllowed(true);
        inp.showAndWait();
        if (inp.hasCancelled()) {
            return null;
        }
        return PaymentDecision.card(inp.getSelected());
    }

    @Override
    public PaymentDecision visit(CostRemoveAnyCounter cost) {
        Integer c = cost.convertAmount();
        final String type = cost.getType();

        if (c == null) {
            c = AbilityUtils.calculateAmount(source, cost.getAmount(), ability);
        }

        CardCollectionView list = new CardCollection(player.getCardsIn(ZoneType.Battlefield));
        list = CardLists.getValidCards(list, type.split(";"), player, source);


        list = CardLists.filter(list, new Predicate<Card>() {
            @Override
            public boolean apply(final Card card) {
                return card.hasCounters();
            }
        });
        InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, 1, 1, list);
        inp.setMessage("Select " + cost.getDescriptiveType() + " to remove a counter");
        inp.setCancelAllowed(false);
        inp.showAndWait();
        Card selected = inp.getFirstSelected();
        final Map<CounterType, Integer> tgtCounters = selected.getCounters();
        final ArrayList<CounterType> typeChoices = new ArrayList<CounterType>();
        for (CounterType key : tgtCounters.keySet()) {
            if (tgtCounters.get(key) > 0) {
                typeChoices.add(key);
            }
        }

        String prompt = "Select type counters to remove";
        cost.setCounterType(SGuiChoose.one(prompt, typeChoices));
        
        return PaymentDecision.card(selected, cost.getCounter());
    }

    public static final class InputSelectCardToRemoveCounter extends InputSelectManyBase<Card> {
        private static final long serialVersionUID = 2685832214519141903L;

        private final Map<Card,Integer> cardsChosen;
        private final CounterType counterType;
        private final CardCollectionView validChoices;

        public InputSelectCardToRemoveCounter(final PlayerControllerHuman controller, int cntCounters, CounterType cType, CardCollectionView validCards) {
            super(controller, cntCounters, cntCounters);
            this.validChoices = validCards;
            counterType = cType;
            cardsChosen = cntCounters > 0 ? new HashMap<Card, Integer>() : null; 
        }

        @Override
        protected boolean onCardSelected(Card c, ITriggerEvent triggerEvent) {
            if (!isValidChoice(c) || c.getCounters(counterType) <= getTimesSelected(c)) {
                return false;
            }

            int tc = getTimesSelected(c);
            cardsChosen.put(c, tc + 1);

            onSelectStateChanged(c, true);
            refresh();
            return true;
        };

        @Override
        protected boolean hasEnoughTargets() {
            return hasAllTargets();
        }

        @Override
        protected boolean hasAllTargets() {
            int sum = getDistibutedCounters();
            return sum >= max;
        }

        protected String getMessage() {
            return max == Integer.MAX_VALUE
                ? String.format(message, getDistibutedCounters())
                : String.format(message, max - getDistibutedCounters());
        }

        private int getDistibutedCounters() {
            int sum = 0;
            for (Entry<Card, Integer> kv : cardsChosen.entrySet()) {
                sum += kv.getValue().intValue();
            }
            return sum;
        }
        
        protected final boolean isValidChoice(GameEntity choice) {
            return validChoices.contains(choice);
        }

        public int getTimesSelected(Card c) {
            return cardsChosen.containsKey(c) ? cardsChosen.get(c).intValue() : 0;
        }

        @Override
        public Collection<Card> getSelected() {
            return cardsChosen.keySet();
        }
    }
    
    @Override
    public PaymentDecision visit(CostRemoveCounter cost) {
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();
        final String type = cost.getType();

        String sVarAmount = ability.getSVar(amount);
        int cntRemoved = 1;
        if (c != null)  
            cntRemoved = c.intValue();
        else if (!"XChoice".equals(sVarAmount)) {
            cntRemoved = AbilityUtils.calculateAmount(source, amount, ability);
        }

        if (cost.payCostFromSource()) {
            int maxCounters = source.getCounters(cost.counter);
            if (amount.equals("All")) {
                final CardView view = CardView.get(ability.getHostCard());
                if (!SGuiDialog.confirm(view, "Remove all counters?")) {
                    return null;
                }
                cntRemoved = maxCounters;
            }
            else if (c == null && "XChoice".equals(sVarAmount)) { 
                cntRemoved = chooseXValue(maxCounters);
            }

            if (maxCounters < cntRemoved) 
                return null;
            return PaymentDecision.card(source, cntRemoved >= 0 ? cntRemoved : maxCounters);
            
        } else if (type.equals("OriginalHost")) {
            int maxCounters = ability.getOriginalHost().getCounters(cost.counter);
            if (amount.equals("All")) {
                cntRemoved = maxCounters;
            }
            if (maxCounters < cntRemoved) 
                return null;

            return PaymentDecision.card(ability.getOriginalHost(), cntRemoved >= 0 ? cntRemoved : maxCounters);
        }

        CardCollectionView validCards = CardLists.getValidCards(player.getCardsIn(cost.zone), type.split(";"), player, source);
        if (cost.zone.equals(ZoneType.Battlefield)) {
            final InputSelectCardToRemoveCounter inp = new InputSelectCardToRemoveCounter(controller, cntRemoved, cost.counter, validCards);
            inp.setMessage("Remove %d " + cost.counter.getName() + " counters from " + cost.getDescriptiveType());
            inp.setCancelAllowed(true);
            inp.showAndWait();
            if (inp.hasCancelled()) {
                return null;
            }

            // Have to hack here: remove all counters minus one, without firing any triggers,
            // triggers will fire when last is removed by executePayment.
            // They don't care how many were removed anyway
            // int sum = 0;
            for (Card crd : inp.getSelected()) {
                int removed = inp.getTimesSelected(crd);
               // sum += removed;
                if (removed < 2) continue;
                int oldVal = crd.getCounters().get(cost.counter).intValue();
                crd.getCounters().put(cost.counter, Integer.valueOf(oldVal - removed + 1));
            }
            return PaymentDecision.card(inp.getSelected(), 1);
        } 

        // Rift Elemental only - always removes 1 counter, so there will be no code for N counters.
        List<CardView> suspended = Lists.newArrayList();
        for (final Card crd : validCards) {
            if (crd.getCounters(cost.counter) > 0) {
                suspended.add(CardView.get(crd));
            }
        }

        final Card card = Card.get(SGuiChoose.oneOrNone("Remove counter(s) from a card in " + cost.zone, suspended));
        return null == card ? null : PaymentDecision.card(card, c);
    }

    @Override
    public PaymentDecision visit(CostSacrifice cost) {
        final String amount = cost.getAmount();
        final String type = cost.getType();

        CardCollectionView list = player.getCardsIn(ZoneType.Battlefield);
        list = CardLists.getValidCards(list, type.split(";"), player, source);
        if (player.hasKeyword("You can't sacrifice creatures to cast spells or activate abilities.")) {
            list = CardLists.getNotType(list, "Creature");
        }

        if (cost.payCostFromSource()) {
            if (source.getController() == ability.getActivatingPlayer() && source.isInPlay()) {
                return player.getController().confirmPayment(cost, "Sacrifice " + source.getName() + "?") ? PaymentDecision.card(source) : null;
            }
            else {
                return null;
            }
        }

        if (amount.equals("All")) {
            return PaymentDecision.card(list);
        }      

        Integer c = cost.convertAmount();
        if (c == null) {
            // Generalize this
            if (ability.getSVar(amount).equals("XChoice")) {
                c = chooseXValue(list.size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
        if (0 == c.intValue()) {
            return PaymentDecision.number(0);
        }
        if (list.size() < c) {
            return null;
        }
        InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, c, list);
        inp.setMessage("Select a " + cost.getDescriptiveType() + " to sacrifice (%d left)");
        inp.setCancelAllowed(true);
        inp.showAndWait();
        if (inp.hasCancelled())
            return null;

        return PaymentDecision.card(inp.getSelected());

    }

    @Override
    public PaymentDecision visit(CostTap cost) {
        // if (!canPay(ability, source, ability.getActivatingPlayer(),
        // payment.getCost()))
        // return false;
        return PaymentDecision.number(1);
    }

    @Override
    public PaymentDecision visit(CostTapType cost) {
        String type = cost.getType();
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();

        boolean sameType = false;
        if (type.contains(".sharesCreatureTypeWith")) {
            sameType = true;
            type = type.replace(".sharesCreatureTypeWith", "");
        }

        boolean totalPower = false;
        String totalP = "";
        if (type.contains("+withTotalPowerGE")) {
            totalPower = true;
            totalP = type.split("withTotalPowerGE")[1];
            type = type.replace("+withTotalPowerGE" + totalP, "");
        }

        CardCollection typeList = CardLists.getValidCards(player.getCardsIn(ZoneType.Battlefield), type.split(";"), player, ability.getHostCard());
        typeList = CardLists.filter(typeList, Presets.UNTAPPED);
        if (c == null && !amount.equals("Any")) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(typeList.size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (sameType) {
            final CardCollection list2 = typeList;
            typeList = CardLists.filter(typeList, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    for (Card card : list2) {
                        if (!card.equals(c) && card.sharesCreatureTypeWith(c)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (c == 0) return PaymentDecision.number(0);
            CardCollection tapped = new CardCollection();
            while (c > 0) {
                InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, 1, 1, typeList);
                inp.setMessage("Select one of the cards to tap. Already chosen: " + tapped);
                inp.setCancelAllowed(true);
                inp.showAndWait();
                if (inp.hasCancelled()) {
                    return null;
                }
                final Card first = inp.getFirstSelected();
                tapped.add(first);
                typeList = CardLists.filter(typeList, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.sharesCreatureTypeWith(first);
                    }
                });
                typeList.remove(first);
                c--;
            }
            return PaymentDecision.card(tapped);
        }       

        if (totalPower) {
            int i = Integer.parseInt(totalP);
            InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, 0, typeList.size(), typeList);
            inp.setMessage("Select a card to tap.");
            inp.setCancelAllowed(true);
            inp.showAndWait();

            if (inp.hasCancelled() || CardLists.getTotalPower(inp.getSelected()) < i) {
                return null;
            }
            return PaymentDecision.card(inp.getSelected());
        }

        InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, c, typeList);
        inp.setCancelAllowed(true);
        inp.setMessage("Select a " + cost.getDescriptiveType() + " to tap (%d left)");
        inp.showAndWait();
        if (inp.hasCancelled()) {
            return null;
        }
        return PaymentDecision.card(inp.getSelected());
    }

    @Override
    public PaymentDecision visit(CostUntapType cost) {
        CardCollection typeList = CardLists.getValidCards(player.getGame().getCardsIn(ZoneType.Battlefield), cost.getType().split(";"),
                player, ability.getHostCard());
        typeList = CardLists.filter(typeList, Presets.TAPPED);
        if (!cost.canUntapSource) {
            typeList.remove(source);
        }
        final String amount = cost.getAmount();
        Integer c = cost.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(typeList.size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
        InputSelectCardsFromList inp = new InputSelectCardsFromList(controller, c, c, typeList);
        inp.setCancelAllowed(true);
        inp.setMessage("Select a " + cost.getDescriptiveType() + " to untap (%d left)");
        inp.showAndWait();
        if (inp.hasCancelled() || inp.getSelected().size() != c) {
            return null;
        }
        return PaymentDecision.card(inp.getSelected());
    }

    @Override
    public PaymentDecision visit(CostUntap cost) {
        return PaymentDecision.number(1);
    }

    @Override
    public PaymentDecision visit(CostUnattach cost) {
        final Card source = ability.getHostCard();
        
        Card cardToUnattach = cost.findCardToUnattach(source, player, ability);
        if (cardToUnattach != null && player.getController().confirmPayment(cost, "Unattach " + cardToUnattach.getName() + "?")) {
            return PaymentDecision.card(cardToUnattach);
        }
        return null;
    }

    @Override
    public boolean paysRightAfterDecision() {
        return true;
    }
}
