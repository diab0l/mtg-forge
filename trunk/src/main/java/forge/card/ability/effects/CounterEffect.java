package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import forge.Card;
import forge.Singletons;
import forge.card.ability.SpellAbilityEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.game.player.AIPlayer;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityStackInstance;
import forge.card.spellability.SpellPermanent;
import forge.card.trigger.TriggerType;
import forge.gui.GuiChoose;

public class CounterEffect extends SpellAbilityEffect {
    @Override
    protected String getStackDescription(SpellAbility sa) {

        final StringBuilder sb = new StringBuilder();
        final List<SpellAbility> sas;

        if (sa.hasParam("AllType")) {
            sas = new ArrayList<SpellAbility>();
            for (int i = 0; i < Singletons.getModel().getGame().getStack().size(); i++) {
                SpellAbility spell = Singletons.getModel().getGame().getStack().peekAbility(i);
                if (sa.getParam("AllType").equals("Spell") && !spell.isSpell()) {
                    continue;
                }
                if (sa.hasParam("AllValid")) {
                    if (!spell.getSourceCard().isValid(sa.getParam("AllValid"), sa.getActivatingPlayer(), sa.getSourceCard())) {
                        continue;
                    }
                }
                sas.add(spell);
            }
        } else {
            sas = getTargetSpellAbilities(sa);
        }

        sb.append("countering");

        boolean isAbility = false;
        for (final SpellAbility tgtSA : sas) {
            sb.append(" ");
            sb.append(tgtSA.getSourceCard());
            isAbility = tgtSA.isAbility();
            if (isAbility) {
                sb.append("'s ability");
            }
        }

        if (isAbility && sa.hasParam("DestroyPermanent")) {
            sb.append(" and destroy it");
        }

        sb.append(".");
        return sb.toString();
    } // end counterStackDescription

    @Override
    public void resolve(SpellAbility sa) {
        // TODO Before this resolves we should see if any of our targets are
        // still on the stack
        final List<SpellAbility> sas;

        if (sa.hasParam("AllType")) {
            sas = new ArrayList<SpellAbility>();
            for (int i = 0; i < Singletons.getModel().getGame().getStack().size(); i++) {
                SpellAbility spell = Singletons.getModel().getGame().getStack().peekAbility(i);
                if (sa.getParam("AllType").equals("Spell") && !spell.isSpell()) {
                    continue;
                }
                if (sa.hasParam("AllValid")) {
                    if (!spell.getSourceCard().isValid(sa.getParam("AllValid"), sa.getActivatingPlayer(), sa.getSourceCard())) {
                        continue;
                    }
                }
                sas.add(spell);
            }
        } else {
            sas = getTargetSpellAbilities(sa);
        }

        if (sa.hasParam("ForgetOtherTargets")) {
            if (sa.getParam("ForgetOtherTargets").equals("True")) {
                sa.getSourceCard().clearRemembered();
            }
        }

        for (final SpellAbility tgtSA : sas) {
            final Card tgtSACard = tgtSA.getSourceCard();

            if (tgtSA.isSpell() && !CardFactoryUtil.isCounterableBy(tgtSACard, sa)) {
                continue;
            }

            final SpellAbilityStackInstance si = Singletons.getModel().getGame().getStack().getInstanceFromSpellAbility(tgtSA);
            if (si == null) {
                continue;
            }

            this.removeFromStack(tgtSA, sa, si);

            // Destroy Permanent may be able to be turned into a SubAbility
            if (tgtSA.isAbility() && sa.hasParam("DestroyPermanent")) {
                Singletons.getModel().getGame().getAction().destroy(tgtSACard, sa);
            }

            if (sa.hasParam("RememberCountered")) {
                if (sa.getParam("RememberCountered").equals("True")) {
                    sa.getSourceCard().addRemembered(tgtSACard);
                }
            }
        }
    } // end counterResolve

    /**
     * <p>
     * removeFromStack.
     * </p>
     * 
     * @param tgtSA
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param srcSA
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param si
     *            a {@link forge.card.spellability.SpellAbilityStackInstance}
     *            object.
     * @param sa
     */
    private void removeFromStack(final SpellAbility tgtSA, final SpellAbility srcSA, final SpellAbilityStackInstance si) {
        Singletons.getModel().getGame().getStack().remove(si);

        String destination =  srcSA.hasParam("Destination") ? srcSA.getParam("Destination") : "Graveyard";
        if (srcSA.hasParam("DestinationChoice")) {//Hinder
            final String[] pos = srcSA.getParam("DestinationChoice").split(",");
            if (srcSA.getActivatingPlayer() instanceof AIPlayer) {
                destination = pos[0];
            } else {
                final String prompt = "Select a destination to remove";
                destination = GuiChoose.one(prompt, pos);
            }
        }
        if (tgtSA.isAbility()) {
            // For Ability-targeted counterspells - do not move it anywhere,
            // even if Destination$ is specified.
        } else if (tgtSA.isFlashBackAbility())  {
            Singletons.getModel().getGame().getAction().exile(tgtSA.getSourceCard());
        } else if (destination.equals("Graveyard")) {
            Singletons.getModel().getGame().getAction().moveToGraveyard(tgtSA.getSourceCard());
        } else if (destination.equals("Exile")) {
            Singletons.getModel().getGame().getAction().exile(tgtSA.getSourceCard());
        } else if (destination.equals("TopOfLibrary")) {
            Singletons.getModel().getGame().getAction().moveToLibrary(tgtSA.getSourceCard());
        } else if (destination.equals("Hand")) {
            Singletons.getModel().getGame().getAction().moveToHand(tgtSA.getSourceCard());
        } else if (destination.equals("Battlefield")) {
            if (tgtSA instanceof SpellPermanent) {
                Card c = tgtSA.getSourceCard();
                System.out.println(c + " is SpellPermanent");
                c.setController(srcSA.getActivatingPlayer(), 0);
                Singletons.getModel().getGame().getAction().moveToPlay(c, srcSA.getActivatingPlayer());
            } else {
                Card c = Singletons.getModel().getGame().getAction().moveToPlay(tgtSA.getSourceCard(), srcSA.getActivatingPlayer());
                c.setController(srcSA.getActivatingPlayer(), 0);
            }
        } else if (destination.equals("BottomOfLibrary")) {
            Singletons.getModel().getGame().getAction().moveToBottomOfLibrary(tgtSA.getSourceCard());
        } else if (destination.equals("ShuffleIntoLibrary")) {
            Singletons.getModel().getGame().getAction().moveToBottomOfLibrary(tgtSA.getSourceCard());
            tgtSA.getSourceCard().getController().shuffle();
        } else {
            throw new IllegalArgumentException("AbilityFactory_CounterMagic: Invalid Destination argument for card "
                    + srcSA.getSourceCard().getName());
        }
        // Run triggers
        final HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Player", tgtSA.getActivatingPlayer());
        runParams.put("Card", tgtSA.getSourceCard());
        runParams.put("Cause", srcSA.getSourceCard());
        srcSA.getActivatingPlayer().getGame().getTriggerHandler().runTrigger(TriggerType.Countered, runParams, false);
        

        if (!tgtSA.isAbility()) {
            System.out.println("Send countered spell to " + destination);
        }
    }

}
