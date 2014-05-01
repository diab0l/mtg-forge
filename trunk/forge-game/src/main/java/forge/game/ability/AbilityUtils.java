package forge.game.ability;

import com.google.common.collect.Iterables;

import forge.card.MagicColor;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostShard;
import forge.game.Game;
import forge.game.GameObject;
import forge.game.card.*;
import forge.game.cost.Cost;
import forge.game.mana.ManaCostBeingPaid;
import forge.game.player.Player;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityStackInstance;
import forge.game.zone.ZoneType;
import forge.util.Expressions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class AbilityUtils {

    public static CounterType getCounterType(String name, SpellAbility sa) throws Exception {
        CounterType counterType;
        if ("ReplacedCounterType".equals(name)) {
        	name = (String) sa.getReplacingObject("CounterType");
        }
        try {
            counterType = CounterType.getType(name);
        } catch (Exception e) {
            String type = sa.getSVar(name);
            if (type.equals("")) {
                type = sa.getHostCard().getSVar(name);
            }

            if (type.equals("")) {
                throw new Exception("Counter type doesn't match, nor does an SVar exist with the type name.");
            }
            counterType = CounterType.getType(type);
        }

        return counterType;
    }

    // should the three getDefined functions be merged into one? Or better to
    // have separate?
    // If we only have one, each function needs to Cast the Object to the
    // appropriate type when using
    // But then we only need update one function at a time once the casting is
    // everywhere.
    // Probably will move to One function solution sometime in the future
    /**
     * <p>
     * getDefinedCards.
     * </p>
     * 
     * @param hostCard
     *            a {@link forge.game.card.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    @SuppressWarnings("unchecked")
    public static List<Card> getDefinedCards(final Card hostCard, final String def, final SpellAbility sa) {
        final List<Card> cards = new ArrayList<Card>();
        final String defined = (def == null) ? "Self" : def; // default to Self
        final Game game = hostCard.getGame();

        Card c = null;

        if (defined.equals("Self")) {
            c = hostCard;
        }

        else if (defined.equals("OriginalHost")) {
            c = sa.getRootAbility().getOriginalHost();
        }

        else if (defined.equals("EffectSource")) {
            if (hostCard.isType("Effect")) {
                c = AbilityUtils.findEffectRoot(hostCard);
            }
        }

        else if (defined.equals("Equipped")) {
            c = hostCard.getEquippingCard();
        }

        else if (defined.equals("Enchanted")) {
            c = hostCard.getEnchantingCard();
            if ((c == null) && (sa.getRootAbility() != null)
                    && (sa.getRootAbility().getPaidList("Sacrificed") != null)
                    && !sa.getRootAbility().getPaidList("Sacrificed").isEmpty()) {
                c = sa.getRootAbility().getPaidList("Sacrificed").get(0).getEnchantingCard();
            }
        }

        else if (defined.equals("TopOfLibrary")) {
            final List<Card> lib = hostCard.getController().getCardsIn(ZoneType.Library);
            if (lib.size() > 0) {
                c = lib.get(0);
            } else {
                // we don't want this to fall through and return the "Self"
                return cards;
            }
        } else if (defined.equals("Targeted")) {
            final SpellAbility saTargeting = sa.getSATargetingCard();
            if (saTargeting != null) {
                Iterables.addAll(cards, saTargeting.getTargets().getTargetCards());
            }
        } else if (defined.equals("ThisTargetedCard")) { // do not add parent targeted
            if (sa != null && sa.getTargets() != null) {
                Iterables.addAll(cards, sa.getTargets().getTargetCards());
            }
        } else if (defined.equals("ParentTarget")) {
            final SpellAbility parent = sa.getParentTargetingCard();
            if (parent != null) {
                Iterables.addAll(cards, parent.getTargets().getTargetCards());
            }

        } else if (defined.startsWith("Triggered") && (sa != null)) {
            final SpellAbility root = sa.getRootAbility();
            if (defined.contains("LKICopy")) { //TriggeredCardLKICopy
                final Object crd = root.getTriggeringObject(defined.substring(9, 13));
                if (crd instanceof Card) {
                    c = (Card) crd;
                }
            }
            else {
                final Object crd = root.getTriggeringObject(defined.substring(9));
                if (crd instanceof Card) {
                    c = game.getCardState((Card) crd);
                } else if (crd instanceof List<?>) {
                    for (final Card cardItem : (List<Card>) crd) {
                        cards.add(cardItem);
                    }
                }
            }
        } else if (defined.startsWith("Replaced") && (sa != null)) {
            final SpellAbility root = sa.getRootAbility();
            final Object crd = root.getReplacingObject(defined.substring(8));
            if (crd instanceof Card) {
                c = game.getCardState((Card) crd);
            } else if (crd instanceof List<?>) {
                for (final Card cardItem : (List<Card>) crd) {
                    cards.add(cardItem);
                }
            }
        } else if (defined.equals("Remembered")) {
            if (hostCard.getRemembered().isEmpty()) {
                final Card newCard = game.getCardState(hostCard);
                for (final Object o : newCard.getRemembered()) {
                    if (o instanceof Card) {
                        cards.add(game.getCardState((Card) o));
                    }
                }
            }
            // game.getCardState(Card c) is not working for LKI
            for (final Object o : hostCard.getRemembered()) {
                if (o instanceof Card) {
                    cards.add(game.getCardState((Card) o));
                }
            }
        } else if (defined.equals("DirectRemembered")) {
            if (hostCard.getRemembered().isEmpty()) {
                final Card newCard = game.getCardState(hostCard);
                for (final Object o : newCard.getRemembered()) {
                    if (o instanceof Card) {
                        cards.add((Card) o);
                    }
                }
            }

            for (final Object o : hostCard.getRemembered()) {
                if (o instanceof Card) {
                    cards.add((Card) o);
                }
            }
        } else if (defined.equals("DelayTriggerRemembered")) {
            if (sa.getRootAbility().isTrigger()) {
               for (Object o : sa.getRootAbility().getTriggerRemembered()) {
                   if (o instanceof Card) {
                       cards.add(game.getCardState((Card) o));
                   }
               }
            }
        } else if (defined.equals("FirstRemembered")) {
            Object o = Iterables.getFirst(hostCard.getRemembered(), null);
            if (o != null && o instanceof Card) {
                cards.add(game.getCardState((Card) o));
            }
        } else if (defined.equals("Clones")) {
            for (final Card clone : hostCard.getClones()) {
                cards.add(game.getCardState(clone));
            }
        } else if (defined.equals("Imprinted")) {
            for (final Card imprint : hostCard.getImprinted()) {
                cards.add(game.getCardState(imprint));
            }
        } else if (defined.startsWith("ThisTurnEntered")) {
            final String[] workingCopy = defined.split("_");
            ZoneType destination, origin;
            String validFilter;

            destination = ZoneType.smartValueOf(workingCopy[1]);
            if (workingCopy[2].equals("from")) {
                origin = ZoneType.smartValueOf(workingCopy[3]);
                validFilter = workingCopy[4];
            } else {
                origin = null;
                validFilter = workingCopy[2];
            }
            for (final Card cl : CardUtil.getThisTurnEntered(destination, origin, validFilter, hostCard)) {
                cards.add(game.getCardState(cl));
            }
        } else if (defined.equals("ChosenCard")) {
            for (final Card chosen : hostCard.getChosenCard()) {
                cards.add(game.getCardState(chosen));
            }
        }
        else if (defined.startsWith("CardUID_")) {
            String idString = defined.substring(8);
            for (final Card cardByID : game.getCardsInGame()) {
                if (cardByID.getUniqueNumber() == Integer.valueOf(idString)) {
                    cards.add(game.getCardState(cardByID));
                }
            }
        } else {
            List<Card> list = null;
            if (defined.startsWith("Sacrificed")) {
                list = sa.getRootAbility().getPaidList("Sacrificed");
            }

            else if (defined.startsWith("Discarded")) {
                list = sa.getRootAbility().getPaidList("Discarded");
            }

            else if (defined.startsWith("Exiled")) {
                list = sa.getRootAbility().getPaidList("Exiled");
            }

            else if (defined.startsWith("Tapped")) {
                list = sa.getRootAbility().getPaidList("Tapped");
            }

            else if (defined.startsWith("Untapped")) {
                list = sa.getRootAbility().getPaidList("Untapped");
            }

            else if (defined.startsWith("Valid ")) {
                String validDefined = defined.substring("Valid ".length());
                list = CardLists.getValidCards(game.getCardsIn(ZoneType.Battlefield), validDefined.split(","), hostCard.getController(), hostCard);
            }

            else if (defined.startsWith("ValidHand ")) {
                String validDefined = defined.substring("ValidHand ".length());
                list = CardLists.getValidCards(game.getCardsIn(ZoneType.Hand), validDefined.split(","), hostCard.getController(), hostCard);
            }

            else if (defined.startsWith("ValidAll ")) {
                String validDefined = defined.substring("ValidAll ".length());
                list = CardLists.getValidCards(game.getCardsInGame(), validDefined.split(","), hostCard.getController(), hostCard);
            }

            else {
                return cards;
            }

            if (list != null) {
                cards.addAll(list);
            }
        }

        if (c != null) {
            cards.add(c);
        }

        return cards;
    }

    private static Card findEffectRoot(Card startCard) {

        Card cc = startCard.getEffectSource();
        if (cc != null) {

            if (cc.isType("Effect")) {
                return findEffectRoot(cc);
            }
            return cc;
        }

        return null; //If this happens there is a card in the game that is not in any zone
    }

    // Utility functions used by the AFs
    /**
     * <p>
     * calculateAmount.
     * </p>
     * 
     * @param card
     *            a {@link forge.game.card.Card} object.
     * @param amount
     *            a {@link java.lang.String} object.
     * @param ability
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a int.
     */
    public static int calculateAmount(final Card card, String amount, final SpellAbility ability) {
        // return empty strings and constants
        if (StringUtils.isBlank(amount)) { return 0; }
        final Game game = card.getController().getGame();

        // Strip and save sign for calculations
        final boolean startsWithPlus = amount.charAt(0) == '+';
        final boolean startsWithMinus = amount.charAt(0) == '-';
        if (startsWithPlus || startsWithMinus) { amount = amount.substring(1); }
        int multiplier = startsWithMinus ? -1 : 1;

        // return result soon for plain numbers
        if (StringUtils.isNumeric(amount)) { return Integer.parseInt(amount) * multiplier; }

        // Try to fetch variable, try ability first, then card.
        String svarval = null;
        if (amount.indexOf('$') > 0) { // when there is a dollar sign, it's not a reference, it's a raw value!
            svarval = amount;
        }
        else if (ability != null) {
            svarval = ability.getSVar(amount);
        }
        if (StringUtils.isBlank(svarval)) {
            if (ability != null) {
                System.err.printf("SVar '%s' not found in ability, fallback to Card (%s). Ability is (%s)%n", amount, card.getName(), ability);
            }
            svarval = card.getSVar(amount);
        }

        if (StringUtils.isBlank(svarval)) {
            // Some variables may be not chosen yet at this moment
            // So return 0 and don't issue an error.
            if (amount.equals("ChosenX")) {
                // isn't made yet
                return 0;
            }
            // cost hasn't been paid yet
            if (amount.startsWith("Cost")) {
                return 0;
            }
            // Nothing to do here if value is missing or blank
            System.err.printf("SVar '%s' not defined in Card (%s)%n", amount, card.getName());
            return 0;
        }

        // Handle numeric constant coming in svar value
        if (StringUtils.isNumeric(svarval)) {
            return multiplier * Integer.parseInt(svarval);
        }

        // Parse Object$Property string
        final String[] calcX = svarval.split("\\$", 2);

        // Incorrect parses mean zero.
        if (calcX.length == 1 || calcX[1].equals("none")) {
            return 0;
        }

        if (calcX[0].startsWith("Count")) {
            return AbilityUtils.xCount(card, calcX[1], ability) * multiplier;
        }

        if (calcX[0].startsWith("Number")) {
            return CardFactoryUtil.xCount(card, svarval) * multiplier;
        }

        if (calcX[0].startsWith("SVar")) {
            final String[] l = calcX[1].split("/");
            final String m = CardFactoryUtil.extractOperators(calcX[1]);
            return CardFactoryUtil.doXMath(AbilityUtils.calculateAmount(card, l[0], ability), m, card) * multiplier;
        }

        if (calcX[0].startsWith("PlayerCount")) {
            final String hType = calcX[0].substring(11);
            final ArrayList<Player> players = new ArrayList<Player>();
            if (hType.equals("Players") || hType.equals("")) {
                players.addAll(game.getPlayers());
                return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
            }
            else if (hType.equals("Opponents")) {
                players.addAll(card.getController().getOpponents());
                return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
            }
            else if (hType.equals("Other")) {
                players.addAll(card.getController().getAllOtherPlayers());
                return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
            }
            else if (hType.equals("Remembered")) {
                for (final Object o : card.getRemembered()) {
                    if (o instanceof Player) {
                        players.add((Player) o);
                    }
                }
                return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
            }
            else if (hType.equals("NonActive")) {
                players.addAll(game.getPlayers());
                players.remove(game.getPhaseHandler().getPlayerTurn());
                return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
            }
            else if (hType.startsWith("Property")) {
                String defined = hType.split("Property")[1];
                for (Player p : game.getPlayers()) {
                    if (p.hasProperty(defined, ability.getActivatingPlayer(), ability.getHostCard())) {
                        players.add(p);
                    }
                }
                return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
            }
            return 0;
        }

        if (calcX[0].startsWith("Remembered")) {
            // Add whole Remembered list to handlePaid
            final List<Card> list = new ArrayList<Card>();
            Card newCard = card;
            if (card.getRemembered().isEmpty()) {
                newCard = game.getCardState(card);
            }

            if (calcX[0].endsWith("LKI")) { // last known information
                for (final Object o : newCard.getRemembered()) {
                    if (o instanceof Card) {
                        list.add((Card) o);
                    }
                }
            }
            else {
                for (final Object o : newCard.getRemembered()) {
                    if (o instanceof Card) {
                        list.add(game.getCardState((Card) o));
                    }
                }
            }

            return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;
        }

        if (calcX[0].startsWith("Imprinted")) {
            // Add whole Imprinted list to handlePaid
            final List<Card> list = new ArrayList<Card>();
            Card newCard = card;
            if (card.getImprinted().isEmpty()) {
                newCard = game.getCardState(card);
            }

            if (calcX[0].endsWith("LKI")) { // last known information
                list.addAll(newCard.getImprinted());
            }
            else {
                for (final Card c : newCard.getImprinted()) {
                    list.add(game.getCardState(c));
                }
            }

            return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;
        }

        if (calcX[0].matches("Enchanted")) {
            // Add whole Enchanted list to handlePaid
            final List<Card> list = new ArrayList<Card>();
            if (card.isEnchanting()) {
                Object o = card.getEnchanting();
                if (o instanceof Card) {
                    list.add(game.getCardState((Card) o));
                }
            }
            return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;
        }

        if (ability == null) {
            return 0;
        }

        // Player attribute counting
        if (calcX[0].startsWith("TargetedPlayer")) {
            final ArrayList<Player> players = new ArrayList<Player>();
            final SpellAbility saTargeting = ability.getSATargetingPlayer();
            if (null != saTargeting) {
                Iterables.addAll(players, saTargeting.getTargets().getTargetPlayers());
            }
            return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
        }
        if (calcX[0].startsWith("ThisTargetedPlayer")) {
            final ArrayList<Player> players = new ArrayList<Player>();
            if (null != ability) {
                Iterables.addAll(players, ability.getTargets().getTargetPlayers());
            }
            return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
        }
        if (calcX[0].startsWith("TargetedObjects")) {
            final List<GameObject> objects = new ArrayList<GameObject>();
            // Make list of all targeted objects starting with the root SpellAbility
            SpellAbility loopSA = ability.getRootAbility();
            while (loopSA != null) {
                if (loopSA.getTargetRestrictions() != null) {
                    Iterables.addAll(objects, loopSA.getTargets().getTargets());
                }
                loopSA = loopSA.getSubAbility();
            }
            return CardFactoryUtil.objectXCount(objects, calcX[1], card) * multiplier;
        }
        if (calcX[0].startsWith("TargetedController")) {
            final ArrayList<Player> players = new ArrayList<Player>();
            final List<Card> list = getDefinedCards(card, "Targeted", ability);
            final List<SpellAbility> sas = AbilityUtils.getDefinedSpellAbilities(card, "Targeted", ability);

            for (final Card c : list) {
                final Player p = c.getController();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
            for (final SpellAbility s : sas) {
                final Player p = s.getHostCard().getController();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
            return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
        }
        if (calcX[0].startsWith("TargetedByTarget")) {
            final List<Card> tgtList = new ArrayList<Card>();
            final List<SpellAbility> saList = getDefinedSpellAbilities(card, "Targeted", ability);

            for (final SpellAbility s : saList) {
                tgtList.addAll(getDefinedCards(s.getHostCard(), "Targeted", s));
            }
            return CardFactoryUtil.handlePaid(tgtList, calcX[1], card) * multiplier;
        }
        if (calcX[0].startsWith("TriggeredPlayer") || calcX[0].startsWith("TriggeredTarget")) {
            final SpellAbility root = ability.getRootAbility();
            Object o = root.getTriggeringObject(calcX[0].substring(9));
            return o instanceof Player ? CardFactoryUtil.playerXProperty((Player) o, calcX[1], card) * multiplier : 0;
        }
        if (calcX[0].equals("TriggeredCardController")) {
            final ArrayList<Player> players = new ArrayList<Player>();
            players.addAll(getDefinedPlayers(card, "TriggeredCardController", ability));
            return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
        }
        if (calcX[0].equals("TriggeredSpellAbility")) {
            final SpellAbility root = ability.getRootAbility();
            SpellAbility sat = (SpellAbility) root.getTriggeringObject("SpellAbility");
            return calculateAmount(sat.getHostCard(), calcX[1], sat);
        }
        // Added on 9/30/12 (ArsenalNut) - Ended up not using but might be useful in future
        /*
        if (calcX[0].startsWith("EnchantedController")) {
            final ArrayList<Player> players = new ArrayList<Player>();
            players.addAll(AbilityFactory.getDefinedPlayers(card, "EnchantedController", ability));
            return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
        }
         */

        List<Card> list = new ArrayList<Card>();
        if (calcX[0].startsWith("Sacrificed")) {
            list = ability.getRootAbility().getPaidList("Sacrificed");
        }
        else if (calcX[0].startsWith("Discarded")) {
            final SpellAbility root = ability.getRootAbility();
            list = root.getPaidList("Discarded");
            if ((null == list) && root.isTrigger()) {
                list = root.getHostCard().getSpellPermanent().getPaidList("Discarded");
            }
        }
        else if (calcX[0].startsWith("Exiled")) {
            list = ability.getRootAbility().getPaidList("Exiled");
        }
        else if (calcX[0].startsWith("Milled")) {
            list = ability.getRootAbility().getPaidList("Milled");
        }
        else if (calcX[0].startsWith("Tapped")) {
            list = ability.getRootAbility().getPaidList("Tapped");
        }
        else if (calcX[0].startsWith("Revealed")) {
            list = ability.getRootAbility().getPaidList("Revealed");
        }
        else if (calcX[0].startsWith("Targeted")) {
            list = ability.findTargetedCards();
        }
        else if (calcX[0].startsWith("ParentTargeted")) {
            SpellAbility parent = ability.getParentTargetingCard();
            if (null != parent) {
                list = parent.findTargetedCards();
            }
        }
        else if (calcX[0].startsWith("Triggered")) {
            final SpellAbility root = ability.getRootAbility();
            list = new ArrayList<Card>();
            list.add((Card) root.getTriggeringObject(calcX[0].substring(9)));
        }
        else if (calcX[0].startsWith("TriggerCount")) {
            // TriggerCount is similar to a regular Count, but just
            // pulls Integer Values from Trigger objects
            final SpellAbility root = ability.getRootAbility();
            final String[] l = calcX[1].split("/");
            final String m = CardFactoryUtil.extractOperators(calcX[1]);
            final int count = (Integer) root.getTriggeringObject(l[0]);

            return CardFactoryUtil.doXMath(count, m, card) * multiplier;
        }
        else if (calcX[0].startsWith("Replaced")) {
            final SpellAbility root = ability.getRootAbility();
            list = new ArrayList<Card>();
            list.add((Card) root.getReplacingObject(calcX[0].substring(8)));
        }
        else if (calcX[0].startsWith("ReplaceCount")) {
            // ReplaceCount is similar to a regular Count, but just
            // pulls Integer Values from Replacement objects
            final SpellAbility root = ability.getRootAbility();
            final String[] l = calcX[1].split("/");
            final String m = CardFactoryUtil.extractOperators(calcX[1]);
            final int count = (Integer) root.getReplacingObject(l[0]);

            return CardFactoryUtil.doXMath(count, m, card) * multiplier;
        }
        else {
            return 0;
        }

        return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;
    }

    /**
     * <p>
     * getDefinedObjects.
     * </p>
     * 
     * @param card
     *            a {@link forge.game.card.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static List<GameObject> getDefinedObjects(final Card card, final String def, final SpellAbility sa) {
        final ArrayList<GameObject> objects = new ArrayList<GameObject>();
        final String defined = (def == null) ? "Self" : def;

        objects.addAll(AbilityUtils.getDefinedPlayers(card, defined, sa));
        objects.addAll(getDefinedCards(card, defined, sa));
        objects.addAll(AbilityUtils.getDefinedSpellAbilities(card, defined, sa));
        return objects;
    }

    /**
     * Filter list by type.
     * 
     * @param list
     *            a CardList
     * @param type
     *            a card type
     * @param sa
     *            a SpellAbility
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> filterListByType(final List<Card> list, String type, final SpellAbility sa) {
        if (type == null) {
            return list;
        }

        // Filter List Can send a different Source card in for things like
        // Mishra and Lobotomy

        Card source = sa.getHostCard();
        final Object o;
        if (type.startsWith("Triggered")) {
            if (type.contains("Card")) {
                o = sa.getTriggeringObject("Card");
            }
            else if (type.contains("Attacker")) {
                o = sa.getTriggeringObject("Attacker");
            }
            else if (type.contains("Blocker")) {
                o = sa.getTriggeringObject("Blocker");
            }
            else {
                o = sa.getTriggeringObject("Card");
            }

            if (!(o instanceof Card)) {
                return new ArrayList<Card>();
            }

            if (type.equals("Triggered") || (type.equals("TriggeredCard")) || (type.equals("TriggeredAttacker"))
                    || (type.equals("TriggeredBlocker"))) {
                type = "Card.Self";
            }

            source = (Card) (o);
            if (type.contains("TriggeredCard")) {
                type = type.replace("TriggeredCard", "Card");
            }
            else if (type.contains("TriggeredAttacker")) {
                type = type.replace("TriggeredAttacker", "Card");
            }
            else if (type.contains("TriggeredBlocker")) {
                type = type.replace("TriggeredBlocker", "Card");
            }
            else {
                type = type.replace("Triggered", "Card");
            }
        }
        else if (type.startsWith("Targeted")) {
            source = null;
            List<Card> tgts = sa.findTargetedCards();
            if (!tgts.isEmpty()) {
                source = tgts.get(0);
            }
            if (source == null) {
                return new ArrayList<Card>();
            }

            if (type.startsWith("TargetedCard")) {
                type = type.replace("TargetedCard", "Card");
            }
            else {
                type = type.replace("Targeted", "Card");
            }
        }
        else if (type.startsWith("Remembered")) {
            boolean hasRememberedCard = false;
            for (final Object object : source.getRemembered()) {
                if (object instanceof Card) {
                    hasRememberedCard = true;
                    source = (Card) object;
                    type = type.replace("Remembered", "Card");
                    break;
                }
            }

            if (!hasRememberedCard) {
                return new ArrayList<Card>();
            }
        }
        else if (type.equals("Card.AttachedBy")) {
            source = source.getEnchantingCard();
            type = type.replace("Card.AttachedBy", "Card.Self");
        }

        String valid = type;
        int eqIndex = valid.indexOf("EQ");
        if (eqIndex >= 0) {
            char reference = valid.charAt(eqIndex + 2); // take whatever goes after EQ
            if (Character.isLetter(reference)) {
                String varName = valid.split("EQ")[1].split("\\+")[0];
                valid = valid.replace("EQ" + varName, "EQ" + Integer.toString(calculateAmount(source, varName, sa)));
            }
        }

        return CardLists.getValidCards(list, valid.split(","), sa.getActivatingPlayer(), source);
    }

    /**
     * <p>
     * getDefinedPlayers.
     * </p>
     * 
     * @param card
     *            a {@link forge.game.card.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static List<Player> getDefinedPlayers(final Card card, final String def, final SpellAbility sa) {
        final List<Player> players = new ArrayList<Player>();
        final String defined = (def == null) ? "You" : def;
        final Game game = card == null ? null : card.getGame();

        if (defined.equals("Targeted") || defined.equals("TargetedPlayer")) {
            final SpellAbility saTargeting = sa.getSATargetingPlayer();
            if (saTargeting != null) {
                Iterables.addAll(players, saTargeting.getTargets().getTargetPlayers());
            }
        }
        else if (defined.equals("ParentTarget")) {
            final SpellAbility parent = sa.getParentTargetingPlayer();
            if (parent != null) {
                Iterables.addAll(players, parent.getTargets().getTargetPlayers());
            }
        }
        else if (defined.equals("TargetedController")) {
            final List<Card> list = getDefinedCards(card, "Targeted", sa);
            final List<SpellAbility> sas = AbilityUtils.getDefinedSpellAbilities(card, "Targeted", sa);

            for (final Card c : list) {
                final Player p = c.getController();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
            for (final SpellAbility s : sas) {
                final Player p = s.getActivatingPlayer();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
        }
        else if (defined.equals("TargetedOwner")) {
            final List<Card> list = getDefinedCards(card, "Targeted", sa);

            for (final Card c : list) {
                final Player p = c.getOwner();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
        }
        else if (defined.equals("TargetedAndYou")) {
            final SpellAbility saTargeting = sa.getSATargetingPlayer();
            if (saTargeting != null) {
                Iterables.addAll(players, saTargeting.getTargets().getTargetPlayers());
                players.add(sa.getActivatingPlayer());
            }
        }
        else if (defined.equals("ParentTargetedController")) {
            final List<Card> list = getDefinedCards(card, "ParentTarget", sa);
            final List<SpellAbility> sas = AbilityUtils.getDefinedSpellAbilities(card, "Targeted", sa);

            for (final Card c : list) {
                final Player p = c.getController();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
            for (final SpellAbility s : sas) {
                final Player p = s.getActivatingPlayer();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
        }
        else if (defined.equals("Remembered")) {
            for (final Object rem : card.getRemembered()) {
                if (rem instanceof Player) {
                    players.add((Player) rem);
                }
            }
        }
        else if (defined.equals("DelayTriggerRemembered")) {
            if (sa.isTrigger()) {
                for (Object o : sa.getRootAbility().getTriggerRemembered()) {
                    if (o instanceof Player) {
                        players.add((Player) o);
                    }
                }
            }
        }
        else if (defined.equals("RememberedOpponent")) {
            for (final Object rem : card.getRemembered()) {
                if (rem instanceof Player) {
                    players.add(((Player) rem).getOpponent());
                }
            }
        }
        else if (defined.equals("RememberedController")) {
            for (final Object rem : card.getRemembered()) {
                if (rem instanceof Card) {
                    players.add(((Card) rem).getController());
                }
            }
        }
        else if (defined.equals("RememberedOwner")) {
            for (final Object rem : card.getRemembered()) {
                if (rem instanceof Card) {
                    players.add(((Card) rem).getOwner());
                }
            }
        }
        else if (defined.equals("ImprintedController")) {
            for (final Card rem : card.getImprinted()) {
                players.add(rem.getController());
            }
        }
        else if (defined.equals("ImprintedOwner")) {
            for (final Card rem : card.getImprinted()) {
                players.add(rem.getOwner());
            }
        }
        else if (defined.startsWith("Triggered")) {
            final SpellAbility root = sa.getRootAbility();
            Object o = null;
            if (defined.endsWith("Controller")) {
                String triggeringType = defined.substring(9);
                triggeringType = triggeringType.substring(0, triggeringType.length() - 10);
                final Object c = root.getTriggeringObject(triggeringType);
                if (c instanceof Card) {
                    o = ((Card) c).getController();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getActivatingPlayer();
                }
            }
            else if (defined.endsWith("Opponent")) {
                String triggeringType = defined.substring(9);
                triggeringType = triggeringType.substring(0, triggeringType.length() - 8);
                final Object c = root.getTriggeringObject(triggeringType);
                if (c instanceof Card) {
                    o = ((Card) c).getController().getOpponents();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getActivatingPlayer().getOpponents();
                }
            }
            else if (defined.endsWith("Owner")) {
                String triggeringType = defined.substring(9);
                triggeringType = triggeringType.substring(0, triggeringType.length() - 5);
                final Object c = root.getTriggeringObject(triggeringType);
                if (c instanceof Card) {
                    o = ((Card) c).getOwner();
                }
            }
            else {
                final String triggeringType = defined.substring(9);
                o = root.getTriggeringObject(triggeringType);
            }
            if (o != null) {
                if (o instanceof Player) {
                    final Player p = (Player) o;
                    if (!players.contains(p)) {
                        players.add(p);
                    }
                }
                if (o instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    final List<Player> pList = (List<Player>) o;
                    if (!pList.isEmpty() && pList.get(0) instanceof Player) {
                        for (final Player p : pList) {
                            if (!players.contains(p)) {
                                players.add(p);
                            }
                        }
                    }
                }
            }
        }
        else if (defined.startsWith("OppNonTriggered")) {
            players.addAll(sa.getActivatingPlayer().getOpponents());
            players.removeAll(getDefinedPlayers(card, defined.substring(6), sa));

        }
        else if (defined.startsWith("Replaced")) {
            final SpellAbility root = sa.getRootAbility();
            Object o = null;
            if (defined.endsWith("Controller")) {
                String replacingType = defined.substring(8);
                replacingType = replacingType.substring(0, replacingType.length() - 10);
                final Object c = root.getReplacingObject(replacingType);
                if (c instanceof Card) {
                    o = ((Card) c).getController();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getHostCard().getController();
                }
            }
            else if (defined.endsWith("Opponent")) {
                String replacingType = defined.substring(8);
                replacingType = replacingType.substring(0, replacingType.length() - 8);
                final Object c = root.getReplacingObject(replacingType);
                if (c instanceof Card) {
                    o = ((Card) c).getController().getOpponent();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getHostCard().getController().getOpponent();
                }
            }
            else if (defined.endsWith("Owner")) {
                String replacingType = defined.substring(8);
                replacingType = replacingType.substring(0, replacingType.length() - 5);
                final Object c = root.getReplacingObject(replacingType);
                if (c instanceof Card) {
                    o = ((Card) c).getOwner();
                }
            }
            else {
                final String replacingType = defined.substring(8);
                o = root.getReplacingObject(replacingType);
            }
            if (o != null) {
                if (o instanceof Player) {
                    final Player p = (Player) o;
                    if (!players.contains(p)) {
                        players.add(p);
                    }
                }
            }
        }
        else if (defined.startsWith("Non")) {
            players.addAll(game.getPlayers());
            players.removeAll(getDefinedPlayers(card, defined.substring(3), sa));
        }
        else if (defined.equals("EnchantedController")) {
            if (card.getEnchantingCard() == null) {
                return players;
            }
            final Player p = card.getEnchantingCard().getController();
            if (!players.contains(p)) {
                players.add(p);
            }
        }
        else if (defined.equals("EnchantedOwner")) {
            if (card.getEnchantingCard() == null) {
                return players;
            }
            final Player p = card.getEnchantingCard().getOwner();
            if (!players.contains(p)) {
                players.add(p);
            }
        }
        else if (defined.equals("EnchantedPlayer")) {
            final Object o = sa.getHostCard().getEnchanting();
            if (o instanceof Player) {
                if (!players.contains(o)) {
                    players.add((Player) o);
                }
            }
        }
        else if (defined.equals("AttackingPlayer")) {
            final Player p = game.getCombat().getAttackingPlayer();
            if (!players.contains(p)) {
                players.add(p);
            }
        }
        else if (defined.equals("DefendingPlayer")) {
            players.add(game.getCombat().getDefendingPlayerRelatedTo(card));
        }
        else if (defined.equals("ChosenPlayer")) {
            final Player p = card.getChosenPlayer();
            if (!players.contains(p)) {
                players.add(p);
            }
        }
        else if (defined.equals("SourceController")) {
            final Player p = sa.getHostCard().getController();
            if (!players.contains(p)) {
                players.add(p);
            }
        }
        else if (defined.equals("CardOwner")) {
            players.add(card.getOwner());
        }
        else if (defined.startsWith("PlayerNamed_")) {
            for (Player p : game.getPlayers()) {
                System.out.println("Named player " + defined.substring(12));
                if (p.getName().equals(defined.substring(12))) {
                    players.add(p);
                }
            }
        }
        else if (defined.startsWith("Flipped")) {
            for (Player p : game.getPlayers()) {
                if (null != sa.getHostCard().getFlipResult(p)) {
                    if (sa.getHostCard().getFlipResult(p).equals(defined.substring(7))) {
                        players.add(p);
                    }
                }
            }
        }
        else if (defined.equals("ActivePlayer")) {
        	players.add(game.getPhaseHandler().getPlayerTurn());
        }
        else if (defined.equals("You")) {
            players.add(sa.getActivatingPlayer());
        }
        else if (defined.equals("Each")) {
            players.addAll(game.getPlayers());
        }
        else if (defined.equals("Opponent")) {
            players.add(sa.getActivatingPlayer().getOpponent());
        }
        else {
            for (Player p : game.getPlayers()) {
                if (p.isValid(defined, sa.getActivatingPlayer(), sa.getHostCard())) {
                    players.add(p);
                }
            }
        }
        return players;
    }

    /**
     * <p>
     * getDefinedSpellAbilities.
     * </p>
     * 
     * @param card
     *            a {@link forge.game.card.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<SpellAbility> getDefinedSpellAbilities(final Card card, final String def,
            final SpellAbility sa) {
        final ArrayList<SpellAbility> sas = new ArrayList<SpellAbility>();
        final String defined = (def == null) ? "Self" : def; // default to Self
        final Game game = sa.getActivatingPlayer().getGame();

        SpellAbility s = null;

        // TODO - this probably needs to be fleshed out a bit, but the basics
        // work
        if (defined.equals("Self")) {
            s = sa;
        }
        else if (defined.equals("Parent")) {
            s = sa.getRootAbility();
        }
        else if (defined.equals("Targeted")) {
            final SpellAbility saTargeting = sa.getSATargetingSA();
            if (saTargeting != null) {
                for (SpellAbility targetSpell : saTargeting.getTargets().getTargetSpells()) {
                    SpellAbilityStackInstance stackInstance = game.getStack().getInstanceFromSpellAbility(targetSpell);
                    if (stackInstance != null) {
                        SpellAbility instanceSA = stackInstance.getSpellAbility();
                        if (instanceSA != null) {
                            sas.add(instanceSA);
                        }
                    }
                    else {
                        sas.add(targetSpell);
                    }
                }
            }
        }
        else if (defined.startsWith("Triggered")) {
            final SpellAbility root = sa.getRootAbility();

            final String triggeringType = defined.substring(9);
            final Object o = root.getTriggeringObject(triggeringType);
            if (o instanceof SpellAbility) {
                s = (SpellAbility) o;
            }
        }
        else if (defined.equals("Remembered")) {
            for (final Object o : card.getRemembered()) {
                if (o instanceof Card) {
                    final Card rem = (Card) o;
                    sas.addAll(game.getCardState(rem).getSpellAbilities());
                }
            }
        }
        else if (defined.equals("Imprinted")) {
            for (final Card imp : card.getImprinted()) {
                sas.addAll(imp.getSpellAbilities());
            }
        }
        else if (defined.equals("EffectSource")) {
            if (card.getEffectSource() != null) {
                sas.addAll(card.getEffectSource().getSpellAbilities());
            }
        }
        else if (defined.equals("SourceFirstSpell")) {
            sas.add(card.getFirstSpellAbility());
        }

        if (s != null) {
            sas.add(s);
        }

        return sas;
    }


    /////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW ARE resove() METHOD AND ITS DEPENDANTS, CONSIDER MOVING TO DEDICATED CLASS
    //
    /////////////////////////////////////////////////////////////////////////////////////
    public static void resolve(final SpellAbility sa) {
        if (sa == null) {
            return;
        }
        final ApiType api = sa.getApi();
        if (api == null) {
            sa.resolve();
            if (sa.getSubAbility() != null) {
                resolve(sa.getSubAbility());
            }
            return;
        }

        AbilityUtils.resolveApiAbility(sa, sa.getActivatingPlayer().getGame());
    }

    private static void resolveSubAbilities(final SpellAbility sa, final Game game) {
        final AbilitySub abSub = sa.getSubAbility();
        if (abSub == null || sa.isWrapper()) {
            return;
        }

        // Needed - Equip an untapped creature with Sword of the Paruns then cast Deadshot on it. Should deal 2 more damage.
        game.getAction().checkStaticAbilities(); // this will refresh continuous abilities for players and permanents.
        AbilityUtils.resolveApiAbility(abSub, game);
    }

    private static void resolveApiAbility(final SpellAbility sa, final Game game) {
        // check conditions
        if (sa.getConditions().areMet(sa)) {
            if (sa.isWrapper() || StringUtils.isBlank(sa.getParam("UnlessCost"))) {
                sa.resolve();
            }
            else {
                handleUnlessCost(sa, game);
                return;
            }
        }
        resolveSubAbilities(sa, game);
    }

    private static void handleUnlessCost(final SpellAbility sa, final Game game) {
        final Card source = sa.getHostCard();

        // The player who has the chance to cancel the ability
        final String pays = sa.hasParam("UnlessPayer") ? sa.getParam("UnlessPayer") : "TargetedController";
        final List<Player> allPayers = getDefinedPlayers(sa.getHostCard(), pays, sa);
        final String  resolveSubs = sa.getParam("UnlessResolveSubs"); // no value means 'Always'
        final boolean execSubsWhenPaid = "WhenPaid".equals(resolveSubs) || StringUtils.isBlank(resolveSubs);
        final boolean execSubsWhenNotPaid = "WhenNotPaid".equals(resolveSubs) || StringUtils.isBlank(resolveSubs);
        final boolean isSwitched = sa.hasParam("UnlessSwitched");

        // The cost
        final Cost cost;
        String unlessCost = sa.getParam("UnlessCost").trim();
        if (unlessCost.equals("CardManaCost")) {
            cost = new Cost(source.getManaCost(), true);
        }
        else if (unlessCost.equals("ChosenManaCost")) {
        	if (source.getChosenCard().isEmpty()) {
                cost = new Cost(ManaCost.ZERO, true);
            } else {
            	cost = new Cost(source.getChosenCard().get(0).getManaCost(), true);
            }
        }
        else if (unlessCost.equals("RememberedCostMinus2")) {
            if (source.getRemembered().isEmpty() || !(source.getRemembered().get(0) instanceof Card)) {
                sa.resolve();
                resolveSubAbilities(sa, game);
            }
            Card rememberedCard = (Card) source.getRemembered().get(0);
            ManaCostBeingPaid newCost = new ManaCostBeingPaid(rememberedCard.getManaCost());
            newCost.decreaseColorlessMana(2);
            cost = new Cost(newCost.toManaCost(), true);
        }
        else if (!StringUtils.isBlank(sa.getSVar(unlessCost)) || !StringUtils.isBlank(source.getSVar(unlessCost))) {
            // check for X costs (stored in SVars
            int xCost = calculateAmount(source, sa.getParam("UnlessCost").replace(" ", ""), sa);
            //Check for XColor
            ManaCostBeingPaid toPay = new ManaCostBeingPaid(ManaCost.ZERO);
            byte xColor = MagicColor.fromName(sa.hasParam("UnlessXColor") ? sa.getParam("UnlessXColor") : "1");
            toPay.increaseShard(ManaCostShard.valueOf(xColor), xCost);
            cost = new Cost(toPay.toManaCost(), true);
        }
        else {
            cost = new Cost(unlessCost, true);
        }

        boolean alreadyPaid = false;
        for (Player payer : allPayers) {
            alreadyPaid |= payer.getController().payCostToPreventEffect(cost, sa, alreadyPaid, allPayers);
        }

        if (alreadyPaid == isSwitched) {
            sa.resolve();
        }

        if (alreadyPaid && execSubsWhenPaid || !alreadyPaid && execSubsWhenNotPaid) { // switched refers only to main ability!
            resolveSubAbilities(sa, game);
        }
    }

    /**
     * <p>
     * handleRemembering.
     * </p>
     * 
     * @param sa
     *            a SpellAbility object.
     */
    public static void handleRemembering(final SpellAbility sa) {
        Card host = sa.getHostCard();

        if (sa.hasParam("RememberTargets") && sa.getTargetRestrictions() != null) {
            if (sa.hasParam("ForgetOtherTargets")) {
                host.clearRemembered();
            }
            for (final GameObject o : sa.getTargets().getTargets()) {
                host.addRemembered(o);
            }
        }

        if (sa.hasParam("ImprintTargets") && sa.getTargetRestrictions() != null) {
            for (final Card c : sa.getTargets().getTargetCards()) {
                host.addImprinted(c);
            }
        }

        if (sa.hasParam("RememberCostMana")) {
            host.clearRemembered();
            host.getRemembered().addAll(sa.getPayingMana());
        }

        if (sa.hasParam("RememberCostCards") && !sa.getPaidHash().isEmpty()) {
            if (sa.getParam("Cost").contains("Exile")) {
                final List<Card> paidListExiled = sa.getPaidList("Exiled");
                for (final Card exiledAsCost : paidListExiled) {
                    host.addRemembered(exiledAsCost);
                }
            }
            else if (sa.getParam("Cost").contains("Sac")) {
                final List<Card> paidListSacrificed = sa.getPaidList("Sacrificed");
                for (final Card sacrificedAsCost : paidListSacrificed) {
                    host.addRemembered(sacrificedAsCost);
                }
            }
            else if (sa.getParam("Cost").contains("tapXType")) {
                final List<Card> paidListTapped = sa.getPaidList("Tapped");
                for (final Card tappedAsCost : paidListTapped) {
                    host.addRemembered(tappedAsCost);
                }
            }
            else if (sa.getParam("Cost").contains("Unattach")) {
                final List<Card> paidListUnattached = sa.getPaidList("Unattached");
                for (final Card unattachedAsCost : paidListUnattached) {
                    host.addRemembered(unattachedAsCost);
                }
            }
            else if (sa.getParam("Cost").contains("Discard")) {
                final List<Card> paidListDiscarded = sa.getPaidList("Discarded");
                for (final Card discardedAsCost : paidListDiscarded) {
                    host.addRemembered(discardedAsCost);
                }
            }
        }
    }

    /**
     * <p>
     * Parse non-mana X variables.
     * </p>
     * 
     * @param c
     *            a {@link forge.game.card.Card} object.
     * @param s
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.SpellAbility} object.
     * @return a int.
     */
    public static int xCount(final Card c, final String s, final SpellAbility sa) {

        final String[] l = s.split("/");
        final String expr = CardFactoryUtil.extractOperators(s);

        final String[] sq;
        sq = l[0].split("\\.");

        if (sa != null) {
            // Count$Kicked.<numHB>.<numNotHB>
            if (sq[0].startsWith("Kicked")) {
                if (sa.isKicked()) {
                    return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), expr, c); // Kicked
                }
                else {
                    return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), expr, c); // not Kicked
                }
            }

            //Count$SearchedLibrary.<DefinedPlayer>
            if (sq[0].contains("SearchedLibrary")) {
                int sum = 0;
                for (Player p : AbilityUtils.getDefinedPlayers(c, sq[1], sa)) {
                    sum += p.getLibrarySearched();
                }

                return sum;
            }

            // Count$Compare <int comparator value>.<True>.<False>
            if (sq[0].startsWith("Compare")) {
                final String[] compString = sq[0].split(" ");
                final int lhs = calculateAmount(c, compString[1], sa);
                final int rhs =  calculateAmount(c, compString[2].substring(2), sa);
                if (Expressions.compare(lhs, compString[2], rhs)) {
                    return CardFactoryUtil.doXMath(calculateAmount(c, sq[1], sa), expr, c);
                }
                else {
                    return CardFactoryUtil.doXMath(calculateAmount(c, sq[2], sa), expr, c);
                }
            }
        }
        return CardFactoryUtil.xCount(c, s);
    }

    public static final void applyManaColorConversion(final Player p, final Map<String, String> params) {
        String conversionType = params.get("ManaColorConversion");

        // Choices are Additives(OR) or Restrictive(AND)
        boolean additive = "Additive".equals(conversionType);

        for(String c : MagicColor.Constant.COLORS_AND_COLORLESS) {
            // Use the strings from MagicColor, since that's how the Script will be coming in as
            String key = WordUtils.capitalize(c) + "Conversion";
            if (params.containsKey(key)) {
                String convertTo = params.get(key);
                byte convertByte = 0;
                if ("All".equals(convertTo)) {
                    convertByte = MagicColor.ALL_COLORS;
                } else{
                    for(String convertColor : convertTo.split(",")) {
                        convertByte |= MagicColor.fromName(convertColor);
                    }
                }
                // AdjustColorReplacement has two different matrices handling final mana conversion under the covers
                p.getManaPool().adjustColorReplacement(MagicColor.fromName(c), convertByte, additive);
            }
        }
    }
}
