package forge.card.abilityfactory.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.CardUtil;
import forge.GameActionUtil;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.AbilityFactoryReveal;
import forge.card.abilityfactory.SpellEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;

public class DiscardEffect extends SpellEffect {
    /**
     * <p>
     * discardStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    protected String getStackDescription(java.util.Map<String,String> params, SpellAbility sa) {
        final String mode = params.get("Mode");
        final StringBuilder sb = new StringBuilder();
    
        ArrayList<Player> tgtPlayers;
    
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }
    
        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        } else {
            sb.append(" ");
        }
    
        final String conditionDesc = params.get("ConditionDescription");
        if (conditionDesc != null) {
            sb.append(conditionDesc).append(" ");
        }
    
        if (tgtPlayers.size() > 0) {
    
            for (final Player p : tgtPlayers) {
                sb.append(p.toString()).append(" ");
            }
    
            if (mode.equals("RevealYouChoose")) {
                sb.append("reveals his or her hand.").append("  You choose (");
            } else if (mode.equals("RevealDiscardAll")) {
                sb.append("reveals his or her hand. Discard (");
            } else {
                sb.append("discards (");
            }
    
            int numCards = 1;
            if (params.containsKey("NumCards")) {
                numCards = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumCards"), sa);
            }
    
            if (mode.equals("Hand")) {
                sb.append("his or her hand");
            } else if (mode.equals("RevealDiscardAll")) {
                sb.append("All");
            } else {
                sb.append(numCards);
            }
    
            sb.append(")");
    
            if (mode.equals("RevealYouChoose")) {
                sb.append(" to discard");
            } else if (mode.equals("RevealDiscardAll")) {
                String valid = params.get("DiscardValid");
                if (valid == null) {
                    valid = "Card";
                }
                sb.append(" of type: ").append(valid);
            }
    
            if (mode.equals("Defined")) {
                sb.append(" defined cards");
            }
    
            if (mode.equals("Random")) {
                sb.append(" at random.");
            } else {
                sb.append(".");
            }
        }
        return sb.toString();
    } // discardStackDescription()

    /**
     * <p>
     * discardResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    @Override
    public void resolve(java.util.Map<String,String> params, SpellAbility sa) {
        final Card source = sa.getSourceCard();
        final String mode = params.get("Mode");
    
        ArrayList<Player> tgtPlayers;
    
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }
    
        final List<Card> discarded = new ArrayList<Card>();
    
        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                if (mode.equals("Defined")) {
                    final ArrayList<Card> toDiscard = AbilityFactory.getDefinedCards(source, params.get("DefinedCards"),
                            sa);
                    for (final Card c : toDiscard) {
                        discarded.addAll(p.discard(c, sa));
                    }
                    if (params.containsKey("RememberDiscarded")) {
                        for (final Card c : discarded) {
                            source.addRemembered(c);
                        }
                    }
                    continue;
                }
    
                if (mode.equals("Hand")) {
                    final List<Card> list = p.discardHand(sa);
                    if (params.containsKey("RememberDiscarded")) {
                        for (final Card c : list) {
                            source.addRemembered(c);
                        }
                    }
                    continue;
                }
    
                if (mode.equals("NotRemembered")) {
                    final List<Card> dPHand = 
                            CardLists.getValidCards(p.getCardsIn(ZoneType.Hand), "Card.IsNotRemembered", source.getController(), source);
                    for (final Card c : dPHand) {
                        p.discard(c, sa);
                        discarded.add(c);
                    }
                }
    
                int numCards = 1;
                if (params.containsKey("NumCards")) {
                    numCards = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumCards"), sa);
                    if (p.getCardsIn(ZoneType.Hand).size() > 0
                            && p.getCardsIn(ZoneType.Hand).size() < numCards) {
                        // System.out.println("Scale down discard from " + numCards + " to " + p.getCardsIn(ZoneType.Hand).size());
                        numCards = p.getCardsIn(ZoneType.Hand).size();
                    }
                }
    
                if (mode.equals("Random")) {
                    boolean runDiscard = true;
                    if (params.containsKey("Optional")) {
                       if (p.isHuman()) {
                           // TODO Ask if Human would like to discard a card at Random
                           StringBuilder sb = new StringBuilder("Would you like to discard ");
                           sb.append(numCards).append(" random card(s)?");
                           runDiscard = GameActionUtil.showYesNoDialog(source, sb.toString());
                       }
                       else {
                           // TODO For now AI will always discard Random used currently with:
                           // Balduvian Horde and similar cards
                       }
                    }
                    
                    if (runDiscard) {
                        final String valid = params.containsKey("DiscardValid") ? params.get("DiscardValid") : "Card";
                        discarded.addAll(p.discardRandom(numCards, sa, valid));
                    }
                } else if (mode.equals("TgtChoose") && params.containsKey("UnlessType")) {
                    p.discardUnless(numCards, params.get("UnlessType"), sa);
                } else if (mode.equals("RevealDiscardAll")) {
                    // Reveal
                    final List<Card> dPHand = p.getCardsIn(ZoneType.Hand);
    
                    if (p.isHuman()) {
                        // "reveal to computer" for information gathering
                    } else {
                        GuiChoose.oneOrNone("Revealed computer hand", dPHand);
                    }
    
                    String valid = params.get("DiscardValid");
                    if (valid == null) {
                        valid = "Card";
                    }
    
                    if (valid.contains("X")) {
                        valid = valid.replace("X", Integer.toString(AbilityFactory.calculateAmount(source, "X", sa)));
                    }
    
                    final List<Card> dPChHand = CardLists.getValidCards(dPHand, valid.split(","), source.getController(), source);
                    // Reveal cards that will be discarded?
                    for (final Card c : dPChHand) {
                        p.discard(c, sa);
                        discarded.add(c);
                    }
                } else if (mode.equals("RevealYouChoose") || mode.equals("RevealOppChoose") || mode.equals("TgtChoose")) {
                    // Is Reveal you choose right? I think the wrong player is
                    // being used?
                    List<Card> dPHand = new ArrayList<Card>(p.getCardsIn(ZoneType.Hand));
                    if (dPHand.size() != 0) {
                        if (params.containsKey("RevealNumber")) {
                            String amountString = params.get("RevealNumber");
                            int amount = amountString.matches("[0-9][0-9]?") ? Integer.parseInt(amountString)
                                    : CardFactoryUtil.xCount(source, source.getSVar(amountString));
                            dPHand = AbilityFactoryReveal.getRevealedList(p, dPHand, amount, false);
                        }
                        List<Card> dPChHand = new ArrayList<Card>(dPHand);
                        String[] dValid = null;
                        if (params.containsKey("DiscardValid")) { // Restrict card choices
                            dValid = params.get("DiscardValid").split(",");
                            dPChHand = CardLists.getValidCards(dPHand, dValid, source.getController(), source);
                        }
                        Player chooser = p;
                        if (mode.equals("RevealYouChoose")) {
                            chooser = source.getController();
                        } else if (mode.equals("RevealOppChoose")) {
                            chooser = source.getController().getOpponent();
                        }
    
                        if (chooser.isComputer()) {
                            // AI
                            if (p.isComputer()) { // discard AI cards
                                int max = chooser.getCardsIn(ZoneType.Hand).size();
                                max = Math.min(max, numCards);
                                List<Card> list = ComputerUtil.discardNumTypeAI(p, max, dValid, sa);
                                if (mode.startsWith("Reveal")) {
                                    GuiChoose.oneOrNone("Computer has chosen", list);
                                }
                                discarded.addAll(list);
                                for (Card card : list) {
                                    p.discard(card, sa);
                                }
                                continue;
                            }
                            // discard human cards
                            for (int i = 0; i < numCards; i++) {
                                if (dPChHand.size() > 0) {
                                    List<Card> goodChoices = CardLists.filter(dPChHand, new Predicate<Card>() {
                                        @Override
                                        public boolean apply(final Card c) {
                                            if (c.hasKeyword("If a spell or ability an opponent controls causes you to discard CARDNAME," +
                                                    " put it onto the battlefield instead of putting it into your graveyard.")
                                                    || !c.getSVar("DiscardMe").equals("")) {
                                                return false;
                                            }
                                            return true;
                                        }
                                    });
                                    if (goodChoices.isEmpty()) {
                                        goodChoices = dPChHand;
                                    }
                                    final List<Card> dChoices = new ArrayList<Card>();
                                    if (params.containsKey("DiscardValid")) {
                                        final String validString = params.get("DiscardValid");
                                        if (validString.contains("Creature") && !validString.contains("nonCreature")) {
                                            final Card c = CardFactoryUtil.getBestCreatureAI(goodChoices);
                                            if (c != null) {
                                                dChoices.add(CardFactoryUtil.getBestCreatureAI(goodChoices));
                                            }
                                        }
                                    }
    
                                    Collections.sort(goodChoices, CardLists.TextLenReverseComparator);
    
                                    CardLists.sortCMC(goodChoices);
                                    dChoices.add(goodChoices.get(0));
    
                                    final Card dC = goodChoices.get(CardUtil.getRandomIndex(goodChoices));
                                    dPChHand.remove(dC);
    
                                    if (mode.startsWith("Reveal")) {
                                        final List<Card> dCs = new ArrayList<Card>();
                                        dCs.add(dC);
                                        GuiChoose.oneOrNone("Computer has chosen", dCs);
                                    }
                                    discarded.add(dC);
                                    p.discard(dC, sa);
                                }
                            }
                        } else {
                            // human
                            if (mode.startsWith("Reveal")) {
                                GuiChoose.oneOrNone("Revealed " + p + "  hand", dPHand);
                            }
    
                            for (int i = 0; i < numCards; i++) {
                                if (dPChHand.size() > 0) {
                                    Card dC = null;
                                    if (params.containsKey("Optional")) {
                                        dC = GuiChoose.oneOrNone("Choose a card to be discarded", dPChHand);
                                    } else {
                                        dC = GuiChoose.one("Choose a card to be discarded", dPChHand);
                                    } if (dC != null) {
                                        dPChHand.remove(dC);
                                        discarded.add(dC);
                                        p.discard(dC, sa);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    
        if (params.containsKey("RememberDiscarded")) {
            for (final Card c : discarded) {
                source.addRemembered(c);
            }
        }
    
    } // discardResolve()

}