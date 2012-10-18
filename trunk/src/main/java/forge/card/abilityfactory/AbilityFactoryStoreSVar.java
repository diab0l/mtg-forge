/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.card.abilityfactory;

import java.util.HashMap;

import forge.Card;
import forge.Singletons;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.cost.Cost;
import forge.card.spellability.Target;
import forge.game.phase.CombatUtil;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;

/**
 * <p>
 * AbilityFactoryStoreSVar class.
 * </p>
 * 
 * @author Forge
 * @version $Id: AbilityFactoryStoreSVar.java 15090 2012-04-07 12:50:31Z Max mtg $
 */
public class AbilityFactoryStoreSVar {
    /**
     * <p>
     * createAbilityStoreSVar.
     * </p>
     * 
     * @param abilityFactory
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityStoreSVar(final AbilityFactory abilityFactory) {
        class AbilityStoreSVar extends AbilityActivated {
            public AbilityStoreSVar(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityStoreSVar(getSourceCard(),
                        getPayCosts(), getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                return res;
            }

            private static final long serialVersionUID = -7299561150243337080L;
            private final AbilityFactory af = abilityFactory;

            @Override
            public String getStackDescription() {
                return AbilityFactoryStoreSVar.storeSVarStackDescription(this.af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryStoreSVar.storeSVarCanPlayAI(this.af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryStoreSVar.storeSVarResolve(this.af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryStoreSVar.storeSVarDoTriggerAI(getActivatingPlayer(), this.af, this, mandatory);
            }
        }
        final SpellAbility abStoreSVar = new AbilityStoreSVar(abilityFactory.getHostCard(), abilityFactory.getAbCost(),
                abilityFactory.getAbTgt());
        return abStoreSVar;
    }

    /**
     * <p>
     * createSpellStoreSVar.
     * </p>
     * 
     * @param abilityFactory
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellStoreSVar(final AbilityFactory abilityFactory) {
        final SpellAbility spStoreSVar = new Spell(abilityFactory.getHostCard(), abilityFactory.getAbCost(),
                abilityFactory.getAbTgt()) {
            private static final long serialVersionUID = 6631124959690157874L;

            private final AbilityFactory af = abilityFactory;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is
                // happening
                return AbilityFactoryStoreSVar.storeSVarStackDescription(this.af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryStoreSVar.storeSVarCanPlayAI(this.af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryStoreSVar.storeSVarResolve(this.af, this);
            }

        };
        return spStoreSVar;
    }

    /**
     * <p>
     * createDrawbackStoreSVar.
     * </p>
     * 
     * @param abilityFactory
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackStoreSVar(final AbilityFactory abilityFactory) {
        class DrawbackStoreSVar extends AbilitySub {
            public DrawbackStoreSVar(final Card ca, final Target t) {
                super(ca, t);
            }

            @Override
            public AbilitySub getCopy() {
                AbilitySub res = new DrawbackStoreSVar(getSourceCard(),
                        getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                return res;
            }

            private static final long serialVersionUID = 6631124959690157874L;

            private final AbilityFactory af = abilityFactory;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is
                // happening
                return AbilityFactoryStoreSVar.storeSVarStackDescription(this.af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryStoreSVar.storeSVarCanPlayAI(this.af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryStoreSVar.storeSVarResolve(this.af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                boolean randomReturn = true;
                final AbilitySub subAb = this.getSubAbility();
                if (subAb != null) {
                    randomReturn &= subAb.chkAIDrawback();
                }

                return randomReturn;
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryStoreSVar.storeSVarDoTriggerAI(getActivatingPlayer(), this.af, this, mandatory);
            }
        }
        final SpellAbility dbStoreSVar = new DrawbackStoreSVar(abilityFactory.getHostCard(), abilityFactory.getAbTgt());

        return dbStoreSVar;
    }

    /**
     * <p>
     * storeSVarStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    public static String storeSVarStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();

        final StringBuilder sb = new StringBuilder();

        if (sa instanceof AbilitySub) {
            sb.append(" ");
        } else {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        }

        if (params.containsKey("StackDescription")) {
            sb.append(params.get("StackDescription").replaceAll("CARDNAME", sa.getSourceCard().getName()));
            return sb.toString();
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
     * storeSVarCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public static boolean storeSVarCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        //Tree of Redemption
        final Player ai = sa.getActivatingPlayer();
        final Card source = sa.getSourceCard();
        if (AbilityFactory.waitForBlocking(sa) || ai.getLife() + 1 >= source.getNetDefense() 
                || (ai.getLife() > 5 && !CombatUtil.lifeInSeriousDanger(ai, Singletons.getModel().getGameState().getCombat()))) {
            return false;
        }
        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null && !subAb.chkAIDrawback()) {
            return false;
        }

        return true;
    }

    /**
     * <p>
     * storeSVarDoTriggerAI.
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
    public static boolean storeSVarDoTriggerAI(final Player ai, final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa, ai) && !mandatory) {
            // payment it's usually
            // not mandatory
            return false;
        }

        // check SubAbilities DoTrigger?
        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            return abSub.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * <p>
     * storeSVarResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public static void storeSVarResolve(final AbilityFactory af, final SpellAbility sa) {
        //SVar$ OldToughness | Type$ Count | Expression$ CardToughness
        final HashMap<String, String> params = af.getMapParams();
        Card source = sa.getSourceCard();

        String key = null;
        String type = null;
        String expr = null;

        if (params.containsKey("SVar")) {
            key = params.get("SVar");
        }

        if (params.containsKey("Type")) {
            type = params.get("Type");
        }

        if (params.containsKey("Expression")) {
            expr = params.get("Expression");
        }

        if (key == null || type == null || expr == null) {
            System.out.println("SVar, Type and Expression paramaters required for StoreSVar. They are missing for " + source.getName());
            return;
        }

        int value = 0;

        if (type.equals("Count")) {
            value = CardFactoryUtil.xCount(source, expr);
        }
        else if (type.equals("Number")) {
            value = Integer.valueOf(expr);
        }
        else if (type.equals("CountSVar")) {
            value = CardFactoryUtil.xCount(source, "SVar$" + expr);
        }
        //TODO For other types call a different function

        StringBuilder numBuilder = new StringBuilder();
        numBuilder.append("Number$");
        numBuilder.append(value);

        source.setSVar(key, numBuilder.toString());
        
        SpellAbility root = sa.getRootSpellAbility();
        while(root != null) {
            root.setSVar(key, numBuilder.toString());
            root = root.getSubAbility();
        }
    }

} // end class AbilityFactorystoreSVar
