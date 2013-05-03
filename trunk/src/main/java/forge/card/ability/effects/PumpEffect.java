package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import forge.Card;
import forge.CardUtil;
import forge.Command;
import forge.GameEntity;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiDialog;
import forge.util.Aggregates;

public class PumpEffect extends SpellAbilityEffect {

    private void applyPump(final SpellAbility sa, final Card applyTo, final int a, final int d, final List<String> keywords) {
        //if host is not on the battlefield don't apply
        if (sa.hasParam("UntilLoseControlOfHost")
                && !sa.getSourceCard().isInPlay()) {
            return;
        }
        final GameState game = sa.getActivatingPlayer().getGame();
        final long timestamp = game.getNextTimestamp();
        final ArrayList<String> kws = new ArrayList<String>();
        
        for (String kw : keywords) {
            if (kw.startsWith("HIDDEN")) {
                applyTo.addHiddenExtrinsicKeyword(kw);
            } else {
                kws.add(kw);
                if (kw.equals("Suspend")) {
                    applyTo.setSuspend(true);
                }
            }
        }

        applyTo.addTempAttackBoost(a);
        applyTo.addTempDefenseBoost(d);
        applyTo.addChangedCardKeywords(kws, null, false, timestamp);

        if (!sa.hasParam("Permanent")) {
            // If not Permanent, remove Pumped at EOT
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = -42244224L;

                @Override
                public void run() {
                    applyTo.addTempAttackBoost(-1 * a);
                    applyTo.addTempDefenseBoost(-1 * d);

                    if (keywords.size() > 0) {
                        for (String kw : keywords) {
                            if (kw.startsWith("HIDDEN")) {
                                applyTo.removeHiddenExtrinsicKeyword(kw);
                            }
                        }
                        applyTo.removeChangedCardKeywords(timestamp);
                    }
                }
            };
            if (sa.hasParam("UntilEndOfCombat")) {
                game.getEndOfCombat().addUntil(untilEOT);
            } else if (sa.hasParam("UntilYourNextUpkeep")) {
                game.getUpkeep().addUntil(sa.getActivatingPlayer(), untilEOT);
            } else if (sa.hasParam("UntilHostLeavesPlay")) {
                sa.getSourceCard().addLeavesPlayCommand(untilEOT);
            } else if (sa.hasParam("UntilLoseControlOfHost")) {
                sa.getSourceCard().addLeavesPlayCommand(untilEOT);
                sa.getSourceCard().addChangeControllerCommand(untilEOT);
            } else if (sa.hasParam("UntilYourNextTurn")) {
                game.getCleanup().addUntil(sa.getActivatingPlayer(), untilEOT);
            } else if (sa.hasParam("UntilUntaps")) {
                sa.getSourceCard().addUntapCommand(untilEOT);
            } else {
                game.getEndOfTurn().addUntil(untilEOT);
            }
        }
    }

    private void applyPump(final SpellAbility sa, final Player p, final List<String> keywords) {
        final GameState game = p.getGame();
        for (int i = 0; i < keywords.size(); i++) {
            p.addKeyword(keywords.get(i));
        }

        if (!sa.hasParam("Permanent")) {
            // If not Permanent, remove Pumped at EOT
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = -32453460L;

                @Override
                public void run() {

                    if (keywords.size() > 0) {
                        for (int i = 0; i < keywords.size(); i++) {
                            p.removeKeyword(keywords.get(i));
                        }
                    }
                }
            };
            if (sa.hasParam("UntilEndOfCombat")) {
                game.getEndOfCombat().addUntil(untilEOT);
            } else if (sa.hasParam("UntilYourNextUpkeep")) {
                game.getUpkeep().addUntil(sa.getActivatingPlayer(), untilEOT);
            } else {
                game.getEndOfTurn().addUntil(untilEOT);
            }
        }
    }

    @Override
    protected String getStackDescription(SpellAbility sa) {

        final StringBuilder sb = new StringBuilder();
        ArrayList<GameEntity> tgts = new ArrayList<GameEntity>();

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgts.addAll(tgt.getTargetCards());
            tgts.addAll(tgt.getTargetPlayers());
        } else {
            if (sa.hasParam("Defined")) {
                tgts.addAll(AbilityUtils.getDefinedPlayers(sa.getSourceCard(), sa.getParam("Defined"), sa));
            }
            if (tgts.isEmpty()) {
                tgts.addAll(AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa));
            }
        }

        if (tgts.size() > 0) {

            for (final GameEntity c : tgts) {
                sb.append(c).append(" ");
            }

            if (sa.hasParam("Radiance")) {
                sb.append(" and each other ").append(sa.getParam("ValidTgts"))
                        .append(" that shares a color with ");
                if (tgts.size() > 1) {
                    sb.append("them ");
                } else {
                    sb.append("it ");
                }
            }

            final List<String> keywords = sa.hasParam("KW") ? Arrays.asList(sa.getParam("KW").split(" & ")) : new ArrayList<String>();
            final int atk = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumAtt"), sa);
            final int def = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumDef"), sa);

            sb.append("gains ");
            if ((atk != 0) || (def != 0)) {
                if (atk >= 0) {
                    sb.append("+");
                }
                sb.append(atk);
                sb.append("/");
                if (def >= 0) {
                   sb.append("+");
                }
                sb.append(def);
                sb.append(" ");
            }

            for (int i = 0; i < keywords.size(); i++) {
                sb.append(keywords.get(i)).append(" ");
            }

            if (!sa.hasParam("Permanent")) {
                sb.append("until end of turn.");
            }

        }

        return sb.toString();
    } // pumpStackDescription()

    @Override
    public void resolve(SpellAbility sa) {
        List<Card> tgtCards = new ArrayList<Card>();
        final ArrayList<Card> untargetedCards = new ArrayList<Card>();
        final Target tgt = sa.getTarget();
        final GameState game = sa.getActivatingPlayer().getGame();
        List<Player> tgtPlayers = new ArrayList<Player>();
        String pumpRemembered = null;
        String pumpForget = null;
        String pumpImprint = null;
        
        List<String> keywords = sa.hasParam("KW") ? Arrays.asList(sa.getParam("KW").split(" & ")) : new ArrayList<String>();
        final int a = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumAtt"), sa);
        final int d = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumDef"), sa);

        if (tgt != null) {
            tgtCards = tgt.getTargetCards();
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            if (sa.hasParam("Defined")) {
                tgtPlayers = AbilityUtils.getDefinedPlayers(sa.getSourceCard(), sa.getParam("Defined"), sa);
            }
            if (tgtPlayers.isEmpty()) {
                tgtCards = AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa);
            }
        }
        if (sa.hasParam("DefinedChosenKW")) {
            if (sa.getParam("DefinedChosenKW").equals("Type")) {
                final String t = sa.getSourceCard().getChosenType();
                for (int i = 0; i < keywords.size(); i++) {
                    keywords.set(i, keywords.get(i).replaceAll("ChosenType", t));
                }
            }
        }
        if (sa.hasParam("RandomKeyword")) {
            final String num = sa.hasParam("RandomKWNum") ? sa.getParam("RandomKWNum") : "1";
            final int numkw = AbilityUtils.calculateAmount(sa.getSourceCard(), num, sa);
            List<String> choice = new ArrayList<String>();
            List<String> total = new ArrayList<String>(keywords);
            if (sa.hasParam("NoRepetition")) {
                final List<String> tgtCardskws = tgtCards.get(0).getKeyword();
                for (String kws : tgtCardskws) {
                    if (total.contains(kws)) {
                        total.remove(kws);
                    }
                }
            }
            final int min = Math.min(total.size(), numkw);
            for (int i = 0; i < min; i++) {
                final String random = Aggregates.random(total);
                choice.add(random);
                total.remove(random);
            }
            keywords = choice;
        }
        
        if (sa.hasParam("Optional")) {
            if (sa.getActivatingPlayer().isHuman()) {
                final StringBuilder targets = new StringBuilder();
                for (final Card tc : tgtCards) {
                    targets.append(tc);
                }
                final StringBuilder sb = new StringBuilder();
                final String descBasic = "Apply pump to " + targets + "?";
                final String pumpDesc = sa.hasParam("OptionQuestion")
                        ? sa.getParam("OptionQuestion").replace("TARGETS", targets) : descBasic;
                sb.append(pumpDesc);
                if (!GuiDialog.confirm(sa.getSourceCard(), sb.toString())) {
                   return;
                }
            } else { //Computer player
                //TODO Add logic here if necessary but I think the AI won't cast
                //the spell in the first place if it would curse its own creature
                //and the pump isn't mandatory
            }
        }

        if (sa.hasParam("RememberObjects")) {
            pumpRemembered = sa.getParam("RememberObjects");
        }

        if (pumpRemembered != null) {
            for (final Object o : AbilityUtils.getDefinedObjects(sa.getSourceCard(), pumpRemembered, sa)) {
                if (!sa.getSourceCard().getRemembered().contains(o)) {
                    sa.getSourceCard().addRemembered(o);
                }
            }
        }

        if (sa.hasParam("ForgetObjects")) {
            pumpForget = sa.getParam("ForgetObjects");
        }

        if (pumpForget != null) {
            for (final Object o : AbilityUtils.getDefinedObjects(sa.getSourceCard(), pumpForget, sa)) {
                if (sa.getSourceCard().getRemembered().contains(o)) {
                    sa.getSourceCard().removeRemembered(o);
                }
            }
        }
        if (sa.hasParam("ImprintCards")) {
            pumpImprint = sa.getParam("ImprintCards");
        }

        if (pumpImprint != null) {
            for (final Card c : AbilityUtils.getDefinedCards(sa.getSourceCard(), pumpImprint, sa)) {
                if (!sa.getSourceCard().getImprinted().contains(c)) {
                    sa.getSourceCard().addImprinted(c);
                }
            }
        }
        
        if (sa.hasParam("ForgetLastImprinted")) {
            final int size = sa.getSourceCard().getImprinted().size();
            sa.getSourceCard().getImprinted().remove(size - 1);
        }   // Used in a SubAbility to clear the root imprinted card (Archery Training)
        
        if (sa.hasParam("Radiance")) {
            for (final Card c : CardUtil.getRadiance(sa.getSourceCard(), tgtCards.get(0), sa.getParam("ValidTgts")
                    .split(","))) {
                untargetedCards.add(c);
            }
        }

        final ZoneType pumpZone = sa.hasParam("PumpZone") ? ZoneType.smartValueOf(sa.getParam("PumpZone"))
                : ZoneType.Battlefield;

        final int size = tgtCards.size();
        for (int j = 0; j < size; j++) {
            final Card tgtC = tgtCards.get(j);

            // only pump things in PumpZone
            if (!game.getCardsIn(pumpZone).contains(tgtC)) {
                continue;
            }

            // if pump is a target, make sure we can still target now
            if ((tgt != null) && !tgtC.canBeTargetedBy(sa)) {
                continue;
            }

            this.applyPump(sa, tgtC, a, d, keywords);
        }

        for (int i = 0; i < untargetedCards.size(); i++) {
            final Card tgtC = untargetedCards.get(i);
            // only pump things in PumpZone
            if (!game.getCardsIn(pumpZone).contains(tgtC)) {
                continue;
            }

            this.applyPump(sa, tgtC, a, d, keywords);
        }

        for (Player p : tgtPlayers) {
            if (!p.canBeTargetedBy(sa)) {
                continue;
            }

            this.applyPump(sa, p, keywords);
        }
    } // pumpResolve()
}
