package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import forge.Command;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.util.Lang;

public class ProtectAllEffect extends SpellAbilityEffect {

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        final List<Card> tgtCards = getTargetCards(sa);

        if (tgtCards.size() > 0) {
            sb.append("Valid card gain protection");
            if (!sa.hasParam("Permanent")) {
                sb.append(" until end of turn");
            }
            sb.append(".");
        }

        return sb.toString();
    } // protectStackDescription()

    @Override
    public void resolve(SpellAbility sa) {
        final Card host = sa.getSourceCard();
        final Game game = sa.getActivatingPlayer().getGame();

        final boolean isChoice = sa.getParam("Gains").contains("Choice");
        final List<String> choices = ProtectEffect.getProtectionList(sa);
        final List<String> gains = new ArrayList<String>();
        if (isChoice) {
            Player choser = sa.getActivatingPlayer();
            if (choser.isHuman()) {
                final String choice = GuiChoose.one("Choose a protection", choices);
                if (null == choice) {
                    return;
                }
                gains.add(choice);
            } else {
                // TODO - needs improvement
                final String choice = choices.get(0);
                gains.add(choice);
            }
            game.getAction().nofityOfValue(sa, choser, Lang.joinHomogenous(gains), choser);
        } else {
            if (sa.getParam("Gains").equals("ChosenColor")) {
                for (final String color : host.getChosenColor()) {
                    gains.add(color.toLowerCase());
                }
            } else if (sa.getParam("Gains").equals("TargetedCardColor")) {
                for (final Card c : sa.getSATargetingCard().getTargets().getTargetCards()) {
                    ColorSet cs = CardUtil.getColors(c);
                    for(byte col : MagicColor.WUBRG) {
                        if (cs.hasAnyColor(col))
                            gains.add(MagicColor.toLongString(col).toLowerCase());
                    }
                }
            } else {
                gains.addAll(choices);
            }
        }

        // Deal with permanents
        String valid = "";
        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }
        if (!valid.equals("")) {
            List<Card> list = game.getCardsIn(ZoneType.Battlefield);
            list = CardLists.getValidCards(list, valid, sa.getActivatingPlayer(), host);

            for (final Card tgtC : list) {
                if (tgtC.isInPlay()) {
                    for (final String gain : gains) {
                        tgtC.addExtrinsicKeyword("Protection from " + gain);
                    }

                    if (!sa.hasParam("Permanent")) {
                        // If not Permanent, remove protection at EOT
                        final Command untilEOT = new Command() {
                            private static final long serialVersionUID = -6573962672873853565L;

                            @Override
                            public void run() {
                                if (tgtC.isInPlay()) {
                                    for (final String gain : gains) {
                                        tgtC.removeExtrinsicKeyword("Protection from " + gain);
                                    }
                                }
                            }
                        };
                        if (sa.hasParam("UntilEndOfCombat")) {
                            game.getEndOfCombat().addUntil(untilEOT);
                        } else {
                            game.getEndOfTurn().addUntil(untilEOT);
                        }
                    }
                }
            }
        }

        // Deal with Players
        String players = "";
        if (sa.hasParam("ValidPlayers")) {
            players = sa.getParam("ValidPlayers");
        }
        if (!players.equals("")) {
            final List<Player> playerList = AbilityUtils.getDefinedPlayers(host, players, sa);
            for (final Player player : playerList) {
                for (final String gain : gains) {
                    player.addKeyword("Protection from " + gain);
                }

                if (!sa.hasParam("Permanent")) {
                    // If not Permanent, remove protection at EOT
                    final Command untilEOT = new Command() {
                        private static final long serialVersionUID = -6573962672873853565L;

                        @Override
                        public void run() {
                            for (final String gain : gains) {
                                player.removeKeyword("Protection from " + gain);
                            }
                        }
                    };
                    if (sa.hasParam("UntilEndOfCombat")) {
                        game.getEndOfCombat().addUntil(untilEOT);
                    } else {
                        game.getEndOfTurn().addUntil(untilEOT);
                    }
                }
            }
        }

    } // protectAllResolve()

}
