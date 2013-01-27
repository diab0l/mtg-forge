package forge.game.player;

import java.util.List;

import forge.Card;
import forge.Singletons;
import forge.card.spellability.SpellAbility;
import forge.control.input.Input;
import forge.game.GameState;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.gui.match.CMatchUI;


/** 
 * A prototype for player controller class
 * 
 * Handles phase skips for now.
 */
public abstract class PlayerController {

    protected final Player player;
    protected final GameState game;
    
    private PhaseType autoPassUntil = null;


    public PlayerController(GameState game0, Player p) {
        game = game0;
        player = p;
    }
    public abstract Input getDefaultInput();
    public abstract Input getBlockInput();
    public abstract Input getCleanupInput();


    /**
     * TODO: Write javadoc for this method.
     * @param cleanup
     */
    public void autoPassTo(PhaseType cleanup) {
        autoPassUntil = cleanup;
    }
    public void autoPassCancel() {
        autoPassUntil = null;
    }


    public boolean mayAutoPass(PhaseType phase) {

        return phase.isBefore(autoPassUntil);
    }


    public boolean isUiSetToSkipPhase(final Player turn, final PhaseType phase) {
        boolean isLocalPlayer = player.equals(Singletons.getControl().getPlayer());
        return isLocalPlayer && !CMatchUI.SINGLETON_INSTANCE.stopAtPhase(turn, phase);
    }

    /**
     * Uses GUI to learn which spell the player (human in our case) would like to play
     */
    public abstract SpellAbility getAbilityToPlay(List<SpellAbility> abilities);

    /**
     * TODO: Write javadoc for this method.
     */
    public void passPriority() {
        PhaseHandler handler = game.getPhaseHandler();
        // may pass only priority is has
        if ( handler.getPriorityPlayer() == player )
            game.getPhaseHandler().passPriority();
    }

    /**
     * TODO: Write javadoc for this method.
     * @param c
     */
    public abstract void playFromSuspend(Card c);
    public abstract boolean playCascade(Card cascadedCard, Card sourceCard);
    public abstract void mayPlaySpellAbilityForFree(SpellAbility copySA);



}
