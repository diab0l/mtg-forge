package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import forge.Card;
import forge.CardLists;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.util.Aggregates;

public class DiscardEffect extends SpellAbilityEffect {
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final String mode = sa.getParam("Mode");
        final StringBuilder sb = new StringBuilder();

        final List<Player> tgtPlayers = getTargetPlayers(sa);

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
            if (sa.hasParam("NumCards")) {
                numCards = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumCards"), sa);
            }

            if (mode.equals("Hand")) {
                sb.append("his or her hand");
            } else if (mode.equals("RevealDiscardAll")) {
                sb.append("All");
            } else if (sa.hasParam("AnyNumber")) {
                sb.append("any number");
            } else if (sa.hasParam("NumCards") && sa.getParam("NumCards").equals("X")
                    && sa.getSVar("X").equals("Remembered$Amount")) {
                sb.append("that many");
            } else {
                sb.append(numCards);
            }

            sb.append(")");

            if (mode.equals("RevealYouChoose")) {
                sb.append(" to discard");
            } else if (mode.equals("RevealDiscardAll")) {
                String valid = sa.getParam("DiscardValid");
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

    @Override
    public void resolve(SpellAbility sa) {
        final Card source = sa.getSourceCard();
        final String mode = sa.getParam("Mode");
        //final boolean anyNumber = sa.hasParam("AnyNumber");

        final TargetRestrictions tgt = sa.getTargetRestrictions();

        final List<Card> discarded = new ArrayList<Card>();

        for (final Player p : getTargetPlayers(sa)) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                final int numCardsInHand = p.getCardsIn(ZoneType.Hand).size(); 
                if (mode.equals("Defined")) {
                    final List<Card> toDiscard = AbilityUtils.getDefinedCards(source, sa.getParam("DefinedCards"), sa);
                    for (final Card c : toDiscard) {
                        boolean hasDiscarded = p.discard(c, sa);
                        if(hasDiscarded)
                            discarded.add(c);
                    }
                    if (sa.hasParam("RememberDiscarded")) {
                        for (final Card c : discarded) {
                            source.addRemembered(c);
                        }
                    }
                    continue;
                }

                if (mode.equals("Hand")) {
                    boolean shouldRemember = sa.hasParam("RememberDiscarded");
                    for(Card c : Lists.newArrayList(p.getCardsIn(ZoneType.Hand))) { // without copying will get concurrent modification exception
                        boolean hasDiscarded = p.discard(c, sa);
                        if( hasDiscarded && shouldRemember )
                            source.addRemembered(c);
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
                if (sa.hasParam("NumCards")) {
                    numCards = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumCards"), sa);
                    if (p.getCardsIn(ZoneType.Hand).size() > 0
                            && p.getCardsIn(ZoneType.Hand).size() < numCards) {
                        // System.out.println("Scale down discard from " + numCards + " to " + p.getCardsIn(ZoneType.Hand).size());
                        numCards = p.getCardsIn(ZoneType.Hand).size();
                    }
                }

                if (mode.equals("Random")) {
                    String message = "Would you like to discard " + numCards + " random card(s)?";
                    boolean runDiscard = !sa.hasParam("Optional") || p.getController().confirmAction(sa, PlayerActionConfirmMode.Random, message);

                    if (runDiscard) {
                        final String valid = sa.hasParam("DiscardValid") ? sa.getParam("DiscardValid") : "Card";
                        final List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Hand), valid, sa.getSourceCard().getController(), sa.getSourceCard());
                        for (int i = 0; i < numCards; i++) {
                            if (list.isEmpty())
                                break;
                                
                            final Card disc = Aggregates.random(list);
                            if ( p.discard(disc, sa) ) 
                                discarded.add(disc);
                            list.remove(disc);
                        }
                    }
                } else if (mode.equals("TgtChoose") && sa.hasParam("UnlessType")) {
                    if( numCardsInHand > 0 ) {
                        final List<Card> hand = p.getCardsIn(ZoneType.Hand);
                        List<Card> toDiscard = p.getController().chooseCardsToDiscardUnlessType(Math.min(numCards, numCardsInHand), hand, sa.getParam("UnlessType"), sa);
                        for(Card c : toDiscard)
                            c.getController().discard(c, sa);
                    }
                } else if (mode.equals("RevealDiscardAll")) {
                    // Reveal
                    final List<Card> dPHand = p.getCardsIn(ZoneType.Hand);

                    p.getOpponent().getController().reveal("Reveal " + p + " hand" , dPHand, ZoneType.Hand, p);

                    String valid = sa.hasParam("DiscardValid") ? sa.getParam("DiscardValid") : "Card";

                    if (valid.contains("X")) {
                        valid = valid.replace("X", Integer.toString(AbilityUtils.calculateAmount(source, "X", sa)));
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
                    if (dPHand.isEmpty())
                        continue; // for loop over players

                    if (sa.hasParam("RevealNumber")) {
                        String amountString = sa.getParam("RevealNumber");
                        int amount = StringUtils.isNumeric(amountString) ? Integer.parseInt(amountString) : CardFactoryUtil.xCount(source, source.getSVar(amountString));
                        dPHand = p.getController().chooseCardsToRevealFromHand(amount, amount, dPHand);
                    }
                    final String valid = sa.hasParam("DiscardValid") ? sa.getParam("DiscardValid") : "Card";
                    String[] dValid = valid.split(",");
                    List<Card> validCards = CardLists.getValidCards(dPHand, dValid, source.getController(), source);

                    Player chooser = p;
                    if (mode.equals("RevealYouChoose")) {
                        chooser = source.getController();
                    } else if (mode.equals("RevealOppChoose")) {
                        chooser = source.getController().getOpponent();
                    }

                    if (mode.startsWith("Reveal") && p != chooser)
                        chooser.getGame().getAction().reveal(dPHand, p);
                    
                    int min = sa.hasParam("AnyNumber") || sa.hasParam("Optional") ? 0 : Math.min(validCards.size(), numCards);
                    int max = sa.hasParam("AnyNumber") ? validCards.size() : Math.min(validCards.size(), numCards);

                    List<Card> toBeDiscarded = validCards.isEmpty() ? CardLists.emptyList : chooser.getController().chooseCardsToDiscardFrom(p, sa, validCards, min, max);

                    if (mode.startsWith("Reveal") ) {
                        p.getController().reveal(chooser + " has chosen", toBeDiscarded, ZoneType.Hand, p);
                    }

                    if (toBeDiscarded != null) {
                        for (Card card : toBeDiscarded) {
                            if ( null == card ) continue;
                            p.discard(card, sa);
                            discarded.add(card);
                        }
                    }
                }
            }
        }

        if (sa.hasParam("RememberDiscarded")) {
            for (final Card c : discarded) {
                source.addRemembered(c);
            }
        }

    } // discardResolve()
}
