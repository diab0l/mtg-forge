package forge.card.abilityFactory;

import forge.*;
import forge.card.spellability.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * <p>AbilityFactory_AlterLife class.</p>
 *
 * @author Forge
 * @version $Id: $
 */
public class AbilityFactory_AlterLife {
    // An AbilityFactory subclass for Gaining, Losing, or Setting Life totals.

    // *************************************************************************
    // ************************* GAIN LIFE *************************************
    // *************************************************************************

    /**
     * <p>createAbilityGainLife.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityGainLife(final AbilityFactory AF) {

        final SpellAbility abGainLife = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
            private static final long serialVersionUID = 8869422603616247307L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return gainLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return gainLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                gainLifeResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return gainLifeDoTriggerAI(af, this, mandatory);
            }

        };
        return abGainLife;
    }

    /**
     * <p>createSpellGainLife.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellGainLife(final AbilityFactory AF) {
        final SpellAbility spGainLife = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
            private static final long serialVersionUID = 6631124959690157874L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return gainLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return gainLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                gainLifeResolve(af, this);
            }

        };
        return spGainLife;
    }

    /**
     * <p>createDrawbackGainLife.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackGainLife(final AbilityFactory AF) {
        final SpellAbility dbGainLife = new Ability_Sub(AF.getHostCard(), AF.getAbTgt()) {
            private static final long serialVersionUID = 6631124959690157874L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return gainLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return gainLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                gainLifeResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return true;
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return gainLifeDoTriggerAI(af, this, mandatory);
            }

        };
        return dbGainLife;
    }

    /**
     * <p>gainLifeStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    public static String gainLifeStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("LifeAmount"), sa);

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard().getName()).append(" - ");
        else
            sb.append(" ");

        String conditionDesc = params.get("ConditionDescription");
        if (conditionDesc != null)
            sb.append(conditionDesc).append(" ");

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (Player player : tgtPlayers)
            sb.append(player).append(" ");

        sb.append("gains ").append(amount).append(" life.");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>gainLifeCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public static boolean gainLifeCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        Random r = MyRandom.random;
        HashMap<String, String> params = af.getMapParams();
        Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        int life = AllZone.getComputerPlayer().getLife();
        int lifeAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("LifeAmount"), sa);
        String amountStr = params.get("LifeAmount");

        //don't use it if no life to gain
        if (lifeAmount <= 0) return false;

        if (abCost != null) {
            if (abCost.getSacCost() && life > 4) {
                if (abCost.getSacThis() && life > 6)
                    return false;
                else {
                    //only sacrifice something that's supposed to be sacrificed
                    String type = abCost.getSacType();
                    CardList typeList = AllZoneUtil.getPlayerCardsInPlay(AllZone.getComputerPlayer());
                    typeList = typeList.getValidCards(type.split(","), source.getController(), source);
                    if (ComputerUtil.getCardPreference(source, "SacCost", typeList) == null)
                        return false;
                }
            }
            if (abCost.getLifeCost() && life > 5) return false;
            if (abCost.getDiscardCost() && life > 5) return false;

            if (abCost.getSubCounter()) {
                //non +1/+1 counters should be used
                if (abCost.getCounterType().equals(Counters.P1P1)) {
                    // A card has a 25% chance per counter to be able to pass through here
                    // 4+ counters will always pass. 0 counters will never
                    int currentNum = source.getCounters(abCost.getCounterType());
                    double percent = .25 * (currentNum / abCost.getCounterNum());
                    if (percent <= r.nextFloat())
                        return false;
                }
            }
        }

        if (!AllZone.getComputerPlayer().canGainLife())
            return false;

        //Don't use lifegain before main 2 if possible
        if (AllZone.getPhase().isBefore(Constant.Phase.Main2) && !params.containsKey("ActivatingPhases"))
            return false;

        //Don't tap creatures that may be able to block
        if (AbilityFactory.waitForBlocking(sa))
            return false;

        // TODO handle proper calculation of X values based on Cost and what would be paid
        //final int amount = calculateAmount(af.getHostCard(), amountStr, sa);

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.resetTargets();
            if (tgt.canOnlyTgtOpponent())
                tgt.addTarget(AllZone.getHumanPlayer());
            else
                tgt.addTarget(AllZone.getComputerPlayer());
        }

        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            int xPay = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(xPay));
        }

        boolean randomReturn = r.nextFloat() <= .6667;
        if (AbilityFactory.playReusable(sa))
            randomReturn = true;

        return (randomReturn && chance);
    }

    /**
     * <p>gainLifeDoTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    public static boolean gainLifeDoTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa) && !mandatory)    // If there is a cost payment it's usually not mandatory
            return false;

        HashMap<String, String> params = af.getMapParams();

        // If the Target is gaining life, target self.
        // if the Target is modifying how much life is gained, this needs to be handled better
        Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.resetTargets();
            if (tgt.canOnlyTgtOpponent())
                tgt.addTarget(AllZone.getHumanPlayer());
            else
                tgt.addTarget(AllZone.getComputerPlayer());
        }

        Card source = sa.getSourceCard();
        String amountStr = params.get("LifeAmount");
        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            int xPay = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(xPay));
        }

        // check SubAbilities DoTrigger?
        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            return abSub.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * <p>gainLifeResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    public static void gainLifeResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        int lifeAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("LifeAmount"), sa);
        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null && !params.containsKey("Defined"))
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (Player p : tgtPlayers)
            if (tgt == null || p.canTarget(sa))
                p.gainLife(lifeAmount, sa.getSourceCard());
    }

    // *************************************************************************
    // ************************* LOSE LIFE *************************************
    // *************************************************************************

    /**
     * <p>createAbilityLoseLife.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityLoseLife(final AbilityFactory AF) {
        final SpellAbility abLoseLife = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
            private static final long serialVersionUID = 1129762905315395160L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return loseLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return loseLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                loseLifeResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return loseLifeDoTriggerAI(af, this, mandatory);
            }
        };
        return abLoseLife;
    }

    /**
     * <p>createSpellLoseLife.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellLoseLife(final AbilityFactory AF) {
        final SpellAbility spLoseLife = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
            private static final long serialVersionUID = -2966932725306192437L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return loseLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return loseLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                loseLifeResolve(af, this);
            }
        };
        return spLoseLife;
    }

    /**
     * <p>createDrawbackLoseLife.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackLoseLife(final AbilityFactory AF) {
        final SpellAbility dbLoseLife = new Ability_Sub(AF.getHostCard(), AF.getAbTgt()) {
            private static final long serialVersionUID = -2966932725306192437L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return loseLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return loseLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                loseLifeResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return true;
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return loseLifeDoTriggerAI(af, this, mandatory);
            }
        };
        return dbLoseLife;
    }

    /**
     * <p>loseLifeStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    static String loseLifeStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("LifeAmount"), sa);

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard().getName()).append(" - ");
        else
            sb.append(" ");

        String conditionDesc = params.get("ConditionDescription");
        if (conditionDesc != null)
            sb.append(conditionDesc).append(" ");

        ArrayList<Player> tgtPlayers;
        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (Player player : tgtPlayers)
            sb.append(player).append(" ");

        sb.append("loses ").append(amount).append(" life.");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>loseLifeCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public static boolean loseLifeCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        Random r = MyRandom.random;
        Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        HashMap<String, String> params = af.getMapParams();
        int humanLife = AllZone.getHumanPlayer().getLife();
        int aiLife = AllZone.getComputerPlayer().getLife();

        String amountStr = params.get("LifeAmount");

        // TODO handle proper calculation of X values based on Cost and what would be paid
        final int amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);

        if (abCost != null) {
            // AI currently disabled for these costs
            if (abCost.getSacCost()) {
                if (amountStr.contains("X"))
                    return false;
                if (!abCost.getSacThis()) {
                    //only sacrifice something that's supposed to be sacrificed
                    String type = abCost.getSacType();
                    CardList typeList = AllZoneUtil.getPlayerCardsInPlay(AllZone.getComputerPlayer());
                    typeList = typeList.getValidCards(type.split(","), source.getController(), source);
                    if (ComputerUtil.getCardPreference(source, "SacCost", typeList) == null)
                        return false;
                }
            }
            if (abCost.getLifeCost() && aiLife - abCost.getLifeAmount() < humanLife - amount) return false;
            if (abCost.getDiscardCost()) return false;

            if (abCost.getSubCounter()) {
                // A card has a 25% chance per counter to be able to pass through here
                // 4+ counters will always pass. 0 counters will never
                int currentNum = source.getCounters(abCost.getCounterType());
                double percent = .25 * (currentNum / abCost.getCounterNum());
                if (percent <= r.nextFloat())
                    return false;
            }
        }

        if (!AllZone.getHumanPlayer().canLoseLife())
            return false;

        //Don't use loselife before main 2 if possible
        if (AllZone.getPhase().isBefore(Constant.Phase.Main2) && !params.containsKey("ActivatingPhases"))
            return false;

        //Don't tap creatures that may be able to block
        if (AbilityFactory.waitForBlocking(sa))
            return false;

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        Target tgt = sa.getTarget();

        if (sa.getTarget() != null) {
            tgt.resetTargets();
            sa.getTarget().addTarget(AllZone.getHumanPlayer());
        }

        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            int xPay = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(xPay));
        }

        boolean randomReturn = r.nextFloat() <= .6667;
        if (AbilityFactory.playReusable(sa))
            randomReturn = true;

        return (randomReturn && chance);
    }

    /**
     * <p>loseLifeDoTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    public static boolean loseLifeDoTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa) && !mandatory)    // If there is a cost payment it's usually not mandatory
            return false;

        HashMap<String, String> params = af.getMapParams();

        Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.addTarget(AllZone.getHumanPlayer());
        }

        Card source = sa.getSourceCard();
        String amountStr = params.get("LifeAmount");
        int amount = 0;
        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            int xPay = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(xPay));
            amount = xPay;
        } else
            amount = AbilityFactory.calculateAmount(source, amountStr, sa);

        ArrayList<Player> tgtPlayers;
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);


        if (tgtPlayers.contains(AllZone.getComputerPlayer())) {
            // For cards like Foul Imp, ETB you lose life
            if (amount + 3 > AllZone.getComputerPlayer().getLife())
                return false;
        }

        // check SubAbilities DoTrigger?
        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            return abSub.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * <p>loseLifeResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    public static void loseLifeResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        int lifeAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("LifeAmount"), sa);

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (Player p : tgtPlayers)
            if (tgt == null || p.canTarget(sa))
                p.loseLife(lifeAmount, sa.getSourceCard());

    }

    // *************************************************************************
    // ************************** Poison Counters ******************************
    // *************************************************************************
    //
    // Made more sense here than in AF_Counters since it affects players and their health

    /**
     * <p>createAbilityPoison.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityPoison(final AbilityFactory af) {

        final SpellAbility abPoison = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 6598936088284756268L;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return poisonStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return poisonCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                poisonResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return poisonDoTriggerAI(af, this, mandatory);
            }

        };
        return abPoison;
    }

    /**
     * <p>createSpellPoison.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellPoison(final AbilityFactory af) {
        final SpellAbility spPoison = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -1495708415138457833L;

            @Override
            public String getStackDescription() {
                return poisonStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return poisonCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                poisonResolve(af, this);
            }

        };
        return spPoison;
    }

    /**
     * <p>createDrawbackPoison.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackPoison(final AbilityFactory af) {
        final SpellAbility dbPoison = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -1173479041548558016L;

            @Override
            public String getStackDescription() {
                return poisonStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return poisonCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                poisonResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return true;
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return poisonDoTriggerAI(af, this, mandatory);
            }

        };
        return dbPoison;
    }

    /**
     * <p>poisonDoTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean poisonDoTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa) && !mandatory)    // If there is a cost payment it's usually not mandatory
            return false;

        HashMap<String, String> params = af.getMapParams();

        Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.addTarget(AllZone.getHumanPlayer());
        } else {
            ArrayList<Player> players = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
            for (Player p : players)
                if (!mandatory && p.isComputer() && p.getPoisonCounters() > p.getOpponent().getPoisonCounters())
                    return false;
        }

        // check SubAbilities DoTrigger?
        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            return abSub.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * <p>poisonResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void poisonResolve(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("Num"), sa);

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (Player p : tgtPlayers)
            if (tgt == null || p.canTarget(sa))
                p.addPoisonCounters(amount);
    }

    /**
     * <p>poisonStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String poisonStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("Num"), sa);

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard()).append(" - ");
        else
            sb.append(" ");

        String conditionDesc = params.get("ConditionDescription");
        if (conditionDesc != null)
            sb.append(conditionDesc).append(" ");

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        if (tgtPlayers.size() > 0) {
            Iterator<Player> it = tgtPlayers.iterator();
            while (it.hasNext()) {
                Player p = it.next();
                sb.append(p);
                if (it.hasNext()) sb.append(", ");
                else sb.append(" ");
            }
        }

        sb.append("get");
        if (tgtPlayers.size() < 2) sb.append("s");
        sb.append(" ").append(amount).append(" poison counter");
        if (amount != 1) sb.append("s.");
        else sb.append(".");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>poisonCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean poisonCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        Cost abCost = sa.getPayCosts();
        final Card source = af.getHostCard();
        HashMap<String, String> params = af.getMapParams();
        //int humanPoison = AllZone.getHumanPlayer().getPoisonCounters();
        //int humanLife = AllZone.getHumanPlayer().getLife();
        //int aiPoison = AllZone.getComputerPlayer().getPoisonCounters();
        int aiLife = AllZone.getComputerPlayer().getLife();
        String amountStr = params.get("Num");

        // TODO handle proper calculation of X values based on Cost and what would be paid
        //final int amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);

        if (abCost != null) {
            // AI currently disabled for these costs
            if (abCost.getSacCost()) {
                if (amountStr.contains("X"))
                    return false;
                if (!abCost.getSacThis()) {
                    //only sacrifice something that's supposed to be sacrificed
                    String type = abCost.getSacType();
                    CardList typeList = AllZoneUtil.getPlayerCardsInPlay(AllZone.getComputerPlayer());
                    typeList = typeList.getValidCards(type.split(","), source.getController(), source);
                    if (ComputerUtil.getCardPreference(source, "SacCost", typeList) == null)
                        return false;
                }
            }
            if (abCost.getLifeCost() && aiLife - abCost.getLifeAmount() <= 0) return false;
        }

        //Don't use poison before main 2 if possible
        if (AllZone.getPhase().isBefore(Constant.Phase.Main2) && !params.containsKey("ActivatingPhases"))
            return false;

        //Don't tap creatures that may be able to block
        if (AbilityFactory.waitForBlocking(sa))
            return false;

        Target tgt = sa.getTarget();

        if (sa.getTarget() != null) {
            tgt.resetTargets();
            sa.getTarget().addTarget(AllZone.getHumanPlayer());
        }

        return true;
    }

    // *************************************************************************
    // ************************** SET LIFE *************************************
    // *************************************************************************

    /**
     * <p>createAbilitySetLife.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilitySetLife(final AbilityFactory af) {
        final SpellAbility abSetLife = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -7375434097541097668L;

            @Override
            public String getStackDescription() {
                return setLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return setLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                setLifeResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return setLifeDoTriggerAI(af, this, mandatory);
            }

        };
        return abSetLife;
    }

    /**
     * <p>createSpellSetLife.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellSetLife(final AbilityFactory af) {
        final SpellAbility spSetLife = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -94657822256270222L;

            @Override
            public String getStackDescription() {
                return setLifeStackDescription(af, this);
            }

            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return setLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                setLifeResolve(af, this);
            }

        };
        return spSetLife;
    }

    /**
     * <p>createDrawbackSetLife.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackSetLife(final AbilityFactory af) {
        final SpellAbility dbSetLife = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -7634729949893534023L;

            @Override
            public String getStackDescription() {
                return setLifeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return setLifeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                setLifeResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return true;
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return setLifeDoTriggerAI(af, this, mandatory);
            }

        };
        return dbSetLife;
    }

    /**
     * <p>setLifeStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String setLifeStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("LifeAmount"), sa);

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard()).append(" -");
        else
            sb.append(" ");

        String conditionDesc = params.get("ConditionDescription");
        if (conditionDesc != null)
            sb.append(conditionDesc).append(" ");

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (Player player : tgtPlayers)
            sb.append(player).append(" ");

        sb.append("life total becomes ").append(amount).append(".");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>setLifeCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean setLifeCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        Random r = MyRandom.random;
        //Ability_Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        int life = AllZone.getComputerPlayer().getLife();
        int hlife = AllZone.getHumanPlayer().getLife();
        HashMap<String, String> params = af.getMapParams();
        String amountStr = params.get("LifeAmount");

        if (!AllZone.getComputerPlayer().canGainLife())
            return false;

        //Don't use setLife before main 2 if possible
        if (AllZone.getPhase().isBefore(Constant.Phase.Main2) && !params.containsKey("ActivatingPhases"))
            return false;

        // TODO handle proper calculation of X values based on Cost and what would be paid
        int amount;
        //we shouldn't have to worry too much about PayX for SetLife
        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            int xPay = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(xPay));
            amount = xPay;
        } else
            amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.resetTargets();
            if (tgt.canOnlyTgtOpponent()) {
                tgt.addTarget(AllZone.getHumanPlayer());
                //if we can only target the human, and the Human's life would go up, don't play it.
                //possibly add a combo here for Magister Sphinx and Higedetsu's (sp?) Second Rite
                if (amount > hlife || !AllZone.getHumanPlayer().canLoseLife()) return false;
            } else {
                if (amount > life && life <= 10) tgt.addTarget(AllZone.getComputerPlayer());
                else if (hlife > amount) tgt.addTarget(AllZone.getHumanPlayer());
                else if (amount > life) tgt.addTarget(AllZone.getComputerPlayer());
                else return false;
            }
        } else {
            if (params.containsKey("Each") && params.get("Defined").equals("Each")) {
                if (amount == 0) return false;
                else if (life > amount) { //will decrease computer's life
                    if (life < 5 || ((life - amount) > (hlife - amount))) return false;
                }
            }
            if (amount < life) return false;
        }

        //if life is in danger, always activate
        if (life < 3 && amount > life) return true;

        return ((r.nextFloat() < .6667) && chance);
    }

    /**
     * <p>setLifeDoTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean setLifeDoTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        int life = AllZone.getComputerPlayer().getLife();
        int hlife = AllZone.getHumanPlayer().getLife();
        Card source = sa.getSourceCard();
        HashMap<String, String> params = af.getMapParams();
        String amountStr = params.get("LifeAmount");
        if (!ComputerUtil.canPayCost(sa) && !mandatory)   // If there is a cost payment it's usually not mandatory
            return false;

        int amount;
        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            int xPay = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(xPay));
            amount = xPay;
        } else
            amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);

        if (source.getName().equals("Eternity Vessel") &&
                (AllZoneUtil.isCardInPlay("Vampire Hexmage", AllZone.getHumanPlayer()) || (source.getCounters(Counters.CHARGE) == 0)))
            return false;

        // If the Target is gaining life, target self.
        // if the Target is modifying how much life is gained, this needs to be handled better
        Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.resetTargets();
            if (tgt.canOnlyTgtOpponent())
                tgt.addTarget(AllZone.getHumanPlayer());
            else {
                if (amount > life && life <= 10) tgt.addTarget(AllZone.getComputerPlayer());
                else if (hlife > amount) tgt.addTarget(AllZone.getHumanPlayer());
                else if (amount > life) tgt.addTarget(AllZone.getComputerPlayer());
                else return false;
            }
        }

        // check SubAbilities DoTrigger?
        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            return abSub.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * <p>setLifeResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void setLifeResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        int lifeAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("LifeAmount"), sa);
        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null && !params.containsKey("Defined"))
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (Player p : tgtPlayers)
            if (tgt == null || p.canTarget(sa))
                p.setLife(lifeAmount, sa.getSourceCard());
    }

}//end class AbilityFactory_AlterLife
