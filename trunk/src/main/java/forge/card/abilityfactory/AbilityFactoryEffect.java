package forge.card.abilityfactory;

import java.util.HashMap;
import java.util.Random;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.CombatUtil;
import forge.Command;
import forge.ComputerUtil;
import forge.Constant;
import forge.Constant.Zone;
import forge.MyRandom;
import forge.Phase;
import forge.Player;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;

/**
 * <p>
 * AbilityFactory_Effect class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class AbilityFactoryEffect {
    /**
     * <p>
     * createAbilityEffect.
     * </p>
     * 
     * @param abilityFactory
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityEffect(final AbilityFactory abilityFactory) {

        final SpellAbility abEffect = new AbilityActivated(abilityFactory.getHostCard(), abilityFactory.getAbCost(), abilityFactory.getAbTgt()) {
            private static final long serialVersionUID = 8869422603616247307L;

            private final AbilityFactory af = abilityFactory;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is
                // happening
                return AbilityFactoryEffect.effectStackDescription(this.af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryEffect.effectCanPlayAI(this.af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryEffect.effectResolve(this.af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryEffect.effectDoTriggerAI(this.af, this, mandatory);
            }

        };
        return abEffect;
    }

    /**
     * <p>
     * createSpellEffect.
     * </p>
     * 
     * @param abilityFactory
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellEffect(final AbilityFactory abilityFactory) {
        final SpellAbility spEffect = new Spell(abilityFactory.getHostCard(), abilityFactory.getAbCost(), abilityFactory.getAbTgt()) {
            private static final long serialVersionUID = 6631124959690157874L;

            private final AbilityFactory af = abilityFactory;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is
                // happening
                return AbilityFactoryEffect.effectStackDescription(this.af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryEffect.effectCanPlayAI(this.af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryEffect.effectResolve(this.af, this);
            }

        };
        return spEffect;
    }

    /**
     * <p>
     * createDrawbackEffect.
     * </p>
     * 
     * @param abilityFactory
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackEffect(final AbilityFactory abilityFactory) {
        final SpellAbility dbEffect = new AbilitySub(abilityFactory.getHostCard(), abilityFactory.getAbTgt()) {
            private static final long serialVersionUID = 6631124959690157874L;

            private final AbilityFactory af = abilityFactory;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is
                // happening
                return AbilityFactoryEffect.effectStackDescription(this.af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryEffect.effectCanPlayAI(this.af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryEffect.effectResolve(this.af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                return true;
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryEffect.effectDoTriggerAI(this.af, this, mandatory);
            }

        };
        return dbEffect;
    }

    /**
     * <p>
     * effectStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    public static String effectStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        if (sa instanceof AbilitySub) {
            sb.append(" ");
        } else {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        }

        sb.append(sa.getDescription());

        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>
     * effectCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public static boolean effectCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        final Random r = MyRandom.getRandom();
        final HashMap<String, String> params = af.getMapParams();
        boolean randomReturn = r.nextFloat() <= .6667;
        String logic = "";

        if (params.containsKey("AILogic")) {
            logic = params.get("AILogic");
            final Phase phase = AllZone.getPhase();
            if (logic.equals("BeginningOfOppTurn")) {
                if (phase.isPlayerTurn(AllZone.getComputerPlayer())
                        || phase.isAfter(Constant.Phase.DRAW)) {
                    return false;
                }
                randomReturn = true;
            } else if (logic.equals("Fog")) {
                if (AllZone.getPhase().isPlayerTurn(sa.getActivatingPlayer())) {
                    return false;
                }
                if (!AllZone.getPhase().is(Constant.Phase.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)) {
                    return false;
                }
                if (AllZone.getStack().size() != 0) {
                    return false;
                }
                if (AllZone.getPhase().isPreventCombatDamageThisTurn()) {
                    return false;
                }
                randomReturn = CombatUtil.lifeInDanger(AllZone.getCombat());
            }
        }

        final String stackable = params.get("Stackable");

        if ((stackable != null) && stackable.equals("False")) {
            String name = params.get("Name");
            if (name == null) {
                name = sa.getSourceCard().getName() + "'s Effect";
            }
            final CardList list = sa.getActivatingPlayer().getCardsIn(Zone.Battlefield, name);
            if (list.size() != 0) {
                return false;
            }
        }

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.resetTargets();
            if (tgt.canOnlyTgtOpponent() || logic.equals("BeginningOfOppTurn")) {
                tgt.addTarget(AllZone.getHumanPlayer());
            } else {
                tgt.addTarget(AllZone.getComputerPlayer());
            }
        }

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null) {
            randomReturn &= subAb.chkAIDrawback();
        }

        return randomReturn;
    }

    /**
     * <p>
     * effectDoTriggerAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    public static boolean effectDoTriggerAI(final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa) && !mandatory) {
            // payment it's usually
            // not mandatory
            return false;
        }

        // TODO: Add targeting effects

        // check SubAbilities DoTrigger?
        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            return abSub.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * <p>
     * effectResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public static void effectResolve(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final Card card = af.getHostCard();

        String[] effectAbilities = null;
        String[] effectTriggers = null;
        String[] effectSVars = null;
        String[] effectKeywords = null;
        String[] effectStaticAbilities = null;
        String effectRemembered = null;

        if (params.containsKey("Abilities")) {
            effectAbilities = params.get("Abilities").split(",");
        }

        if (params.containsKey("Triggers")) {
            effectTriggers = params.get("Triggers").split(",");
        }

        if (params.containsKey("StaticAbilities")) {
            effectStaticAbilities = params.get("StaticAbilities").split(",");
        }

        if (params.containsKey("SVars")) {
            effectSVars = params.get("SVars").split(",");
        }

        if (params.containsKey("Keywords")) {
            effectKeywords = params.get("Keywords").split(",");
        }

        if (params.containsKey("RememberObjects")) {
            effectRemembered = params.get("RememberObjects");
        }

        // Effect eff = new Effect();
        String name = params.get("Name");
        if (name == null) {
            name = sa.getSourceCard().getName() + "'s Effect";
        }

        // Unique Effects shouldn't be duplicated
        if (params.containsKey("Unique") && AllZoneUtil.isCardInPlay(name)) {
            return;
        }

        final Player controller = sa.getActivatingPlayer();
        final Card eff = new Card();
        eff.setName(name);
        eff.addType("Effect"); // Or Emblem
        eff.setToken(true); // Set token to true, so when leaving play it gets
                            // nuked
        eff.addController(controller);
        eff.setOwner(controller);
        eff.setImageName(card.getImageName());
        eff.setColor(card.getColor());
        eff.setImmutable(true);

        // Effects should be Orange or something probably

        final Card e = eff;

        // Abilities, triggers and SVars work the same as they do for Token
        // Grant abilities
        if (effectAbilities != null) {
            for (final String s : effectAbilities) {
                final AbilityFactory abFactory = new AbilityFactory();
                final String actualAbility = af.getHostCard().getSVar(s);

                final SpellAbility grantedAbility = abFactory.getAbility(actualAbility, eff);
                eff.addSpellAbility(grantedAbility);
            }
        }

        // Grant triggers
        if (effectTriggers != null) {
            for (final String s : effectTriggers) {
                final String actualTrigger = af.getHostCard().getSVar(s);

                final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, eff, true);
                eff.addTrigger(parsedTrigger);
            }
        }

        // Grant static abilities
        if (effectStaticAbilities != null) {
            for (final String s : effectStaticAbilities) {
                eff.addStaticAbility(af.getHostCard().getSVar(s));
            }
        }

        // Grant SVars
        if (effectSVars != null) {
            for (final String s : effectSVars) {
                final String actualSVar = af.getHostCard().getSVar(s);
                eff.setSVar(s, actualSVar);
            }
        }

        // Grant Keywords
        if (effectKeywords != null) {
            for (final String s : effectKeywords) {
                final String actualKeyword = af.getHostCard().getSVar(s);
                eff.addIntrinsicKeyword(actualKeyword);
            }
        }

        // Set Remembered
        if (effectRemembered != null) {
            for (final Object o : AbilityFactory.getDefinedObjects(card, effectRemembered, sa)) {
                eff.addRemembered(o);
            }
        }

        // Set Chosen Color(s)
        if (!card.getChosenColor().isEmpty()) {
            eff.setChosenColor(card.getChosenColor());
        }

        // Duration
        final String duration = params.get("Duration");
        if ((duration == null) || !duration.equals("Permanent")) {
            final Command endEffect = new Command() {
                private static final long serialVersionUID = -5861759814760561373L;

                @Override
                public void execute() {
                    AllZone.getGameAction().exile(e);
                }
            };

            if ((duration == null) || duration.equals("EndOfTurn")) {
                AllZone.getEndOfTurn().addUntil(endEffect);
            }
        }

        // TODO: Add targeting to the effect so it knows who it's dealing with
        AllZone.getTriggerHandler().suppressMode("ChangesZone");
        AllZone.getGameAction().moveToPlay(eff);
        AllZone.getTriggerHandler().clearSuppression("ChangesZone");
    }
}
