package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates.Presets;
import forge.card.CardType;
import forge.card.ability.SpellAbilityEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.Aggregates;

public class ChooseCardEffect extends SpellAbilityEffect {
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        for (final Player p : getTargetPlayers(sa)) {
            sb.append(p).append(" ");
        }
        sb.append("chooses a card.");

        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final Card host = sa.getSourceCard();
        final GameState game = sa.getActivatingPlayer().getGame();
        final ArrayList<Card> chosen = new ArrayList<Card>();

        final Target tgt = sa.getTarget();
        final List<Player> tgtPlayers = getTargetPlayers(sa);

        ZoneType choiceZone = ZoneType.Battlefield;
        if (sa.hasParam("ChoiceZone")) {
            choiceZone = ZoneType.smartValueOf(sa.getParam("ChoiceZone"));
        }
        List<Card> choices = game.getCardsIn(choiceZone);
        if (sa.hasParam("Choices")) {
            choices = CardLists.getValidCards(choices, sa.getParam("Choices"), host.getController(), host);
        }
        if (sa.hasParam("TargetControls")) {
            choices = CardLists.filterControlledBy(choices, tgtPlayers.get(0));
        }

        final String numericAmount = sa.getParamOrDefault("Amount", "1");
        final int validAmount = StringUtils.isNumeric(numericAmount) ? Integer.parseInt(numericAmount) : CardFactoryUtil.xCount(host, host.getSVar(numericAmount));

        for (final Player p : tgtPlayers) {
            if (sa.hasParam("EachBasicType")) {
                // Get all lands, 
                List<Card> land = CardLists.filter(game.getCardsIn(ZoneType.Battlefield), Presets.LANDS);
                String eachBasic = sa.getParam("EachBasicType");
                if (eachBasic.equals("Controlled")) {
                    land = CardLists.filterControlledBy(land, p);
                }
                
                // Choose one of each BasicLand given special place
                for (final String type : CardType.getBasicTypes()) {
                    final List<Card> cl = CardLists.getType(land, type);
                    if (!cl.isEmpty()) {
                        final String prompt = "Choose a" + (type.equals("Island") ? "n " : " ") + type;
                        Card c = p.getController().chooseSingleCardForEffect(cl, sa, prompt, true);
                        
                        if (null != c) {
                            chosen.add(c);
                        }
                    }
                }
            } else if ((tgt == null) || p.canBeTargetedBy(sa)) {
                for (int i = 0; i < validAmount; i++) {
                    
                    Card c;
                    if (sa.hasParam("AtRandom")) {
                        c = Aggregates.random(choices);
                    } else {
                        c = p.getController().chooseSingleCardForEffect(choices, sa, sa.hasParam("ChoiceTitle") ? sa.getParam("ChoiceTitle") : "Choose a card ", !sa.hasParam("Mandatory"));
                    }
                
                    if (c != null) {
                        chosen.add(c);
                        choices.remove(c);
                    } else {
                        break;
                    }
                }
            }
        }
        host.setChosenCard(chosen);
        if (sa.hasParam("RememberChosen")) {
            for (final Card rem : chosen) {
                host.addRemembered(rem);
            }
        }
        if (sa.hasParam("ForgetChosen")) {
            for (final Card rem : chosen) {
                host.removeRemembered(rem);
            }
        }
    }

}
