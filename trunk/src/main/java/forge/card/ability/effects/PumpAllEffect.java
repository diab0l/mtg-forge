package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import forge.Card;
import forge.Command;
import forge.Singletons;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.SpellAbility;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class PumpAllEffect extends SpellAbilityEffect {
    private void applyPumpAll(final SpellAbility sa, final List<Card> list, final int a, 
            final int d, final List<String> keywords, final ArrayList<ZoneType> affectedZones) {
        
        for (final Card tgtC : list) {

            // only pump things in the affected zones.
            boolean found = false;
            for (final ZoneType z : affectedZones) {
                if (tgtC.isInZone(z)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                continue;
            }

            tgtC.addTempAttackBoost(a);
            tgtC.addTempDefenseBoost(d);

            for (int i = 0; i < keywords.size(); i++) {
                tgtC.addExtrinsicKeyword(keywords.get(i));
                if (keywords.get(i).equals("Suspend")) {
                    tgtC.setSuspend(true);
                }
            }

            if (sa.hasParam("RememberAllPumped")) {
                sa.getSourceCard().addRemembered(tgtC);
            }
        
            if (!sa.hasParam("Permanent")) {
                // If not Permanent, remove Pumped at EOT
                final Command untilEOT = new Command() {
                    private static final long serialVersionUID = 5415795460189457660L;

                    @Override
                    public void run() {
                        tgtC.addTempAttackBoost(-1 * a);
                        tgtC.addTempDefenseBoost(-1 * d);

                        if (keywords.size() > 0) {
                            for (int i = 0; i < keywords.size(); i++) {
                                tgtC.removeExtrinsicKeyword(keywords.get(i));
                            }
                        }
                    }
                };
                if (sa.hasParam("UntilUntaps")) {
                    sa.getSourceCard().addUntapCommand(untilEOT);
                } else if (sa.hasParam("UntilEndOfCombat")) {
                    Singletons.getModel().getGame().getEndOfCombat().addUntil(untilEOT);
                } else {
                    Singletons.getModel().getGame().getEndOfTurn().addUntil(untilEOT);
                }
            }
        }
    }
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        String desc = "";
        if (sa.hasParam("PumpAllDescription")) {
            desc = sa.getParam("PumpAllDescription");
        } else if (sa.hasParam("SpellDescription")) {
            desc = sa.getParam("SpellDescription").replace("CARDNAME", sa.getSourceCard().getName());
        }

        sb.append(desc);

        return sb.toString();
    } // pumpAllStackDescription()

    @Override
    public void resolve(SpellAbility sa) {
        List<Card> list;
        final List<Player> tgtPlayers = getTargetPlayersEmptyAsDefault(sa);
        final ArrayList<ZoneType> affectedZones = new ArrayList<ZoneType>();

        if (sa.hasParam("PumpZone")) {
            for (final String zone : sa.getParam("PumpZone").split(",")) {
                affectedZones.add(ZoneType.valueOf(zone));
            }
        } else {
            affectedZones.add(ZoneType.Battlefield);
        }

        list = new ArrayList<Card>();
        if (tgtPlayers.isEmpty()) {
            for (final ZoneType zone : affectedZones) {
                list.addAll(Singletons.getModel().getGame().getCardsIn(zone));
            }

        } else {
            for (final ZoneType zone : affectedZones) {
                for (final Player p : tgtPlayers) {
                    list.addAll(p.getCardsIn(zone));
                }
            }
        }

        String valid = "";
        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }

        list = AbilityUtils.filterListByType(list, valid, sa);

        List<String> keywords = sa.hasParam("KW") ? Arrays.asList(sa.getParam("KW").split(" & ")) : new ArrayList<String>();
        final int a = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumAtt"), sa);
        final int d = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumDef"), sa);
        
        if (sa.hasParam("SharedKeywordsZone")) {
            List<ZoneType> zones = ZoneType.listValueOf(sa.getParam("SharedKeywordsZone"));
            String[] restrictions = sa.hasParam("SharedRestrictions") ? sa.getParam("SharedRestrictions").split(",") : new String[] {"Card"};
            keywords = CardFactoryUtil.sharedKeywords(sa.getParam("KW").split(" & "), restrictions, zones, sa.getSourceCard());
        }
        this.applyPumpAll(sa, list, a, d, keywords, affectedZones);
    } // pumpAllResolve()

}
