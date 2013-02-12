package forge.card.ability.ai;


import forge.Singletons;
import forge.card.ability.SpellAiLogic;
import forge.card.spellability.SpellAbility;
import forge.game.ai.ComputerUtilCombat;
import forge.game.phase.PhaseType;
import forge.game.player.AIPlayer;

public class FogAi extends SpellAiLogic {

    /* (non-Javadoc)
         * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
         */
    @Override
    protected boolean canPlayAI(AIPlayer ai, SpellAbility sa) {
        // AI should only activate this during Human's Declare Blockers phase
        if (Singletons.getModel().getGame().getPhaseHandler().isPlayerTurn(sa.getActivatingPlayer())) {
            return false;
        }
        if (!Singletons.getModel().getGame().getPhaseHandler().is(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)) {
            return false;
        }

        // Only cast when Stack is empty, so Human uses spells/abilities first
        if (!Singletons.getModel().getGame().getStack().isEmpty()) {
            return false;
        }

        // Don't cast it, if the effect is already in place
        if (Singletons.getModel().getGame().getPhaseHandler().isPreventCombatDamageThisTurn()) {
            return false;
        }

        // Cast it if life is in danger
        return ComputerUtilCombat.lifeInDanger(ai, Singletons.getModel().getGame().getCombat());
    }

    @Override
    public boolean chkAIDrawback(SpellAbility sa, AIPlayer ai) {
        // AI should only activate this during Human's turn
        boolean chance;

        // should really check if other player is attacking this player
        if (ai.isOpponentOf(Singletons.getModel().getGame().getPhaseHandler().getPlayerTurn())) {
            chance = Singletons.getModel().getGame().getPhaseHandler().getPhase().isBefore(PhaseType.COMBAT_FIRST_STRIKE_DAMAGE);
        } else {
            chance = Singletons.getModel().getGame().getPhaseHandler().getPhase().isAfter(PhaseType.COMBAT_DAMAGE);
        }

        return chance;
    }

    @Override
    protected boolean doTriggerAINoCost(AIPlayer aiPlayer, SpellAbility sa, boolean mandatory) {
        boolean chance;
        if (Singletons.getModel().getGame().getPhaseHandler().isPlayerTurn(sa.getActivatingPlayer().getOpponent())) {
            chance = Singletons.getModel().getGame().getPhaseHandler().getPhase().isBefore(PhaseType.COMBAT_FIRST_STRIKE_DAMAGE);
        } else {
            chance = Singletons.getModel().getGame().getPhaseHandler().getPhase().isAfter(PhaseType.COMBAT_DAMAGE);
        }

        return chance;
    }
}
