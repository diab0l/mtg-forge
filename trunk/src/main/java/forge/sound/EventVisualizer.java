package forge.sound;

import forge.Card;
import forge.Singletons;
import forge.card.spellability.SpellAbility;
import forge.game.event.GameEventAnteCardsSelected;
import forge.game.event.GameEventBlockerAssigned;
import forge.game.event.GameEventCardDamaged;
import forge.game.event.GameEventCardDestroyed;
import forge.game.event.GameEventCardDiscarded;
import forge.game.event.GameEventCardEquipped;
import forge.game.event.GameEventCardRegenerated;
import forge.game.event.GameEventCardSacrificed;
import forge.game.event.GameEventCounterAdded;
import forge.game.event.GameEventCounterRemoved;
import forge.game.event.GameEventDrawCard;
import forge.game.event.GameEventDuelFinished;
import forge.game.event.GameEventDuelOutcome;
import forge.game.event.GameEventEndOfTurn;
import forge.game.event.GameEvent;
import forge.game.event.GameEventFlipCoin;
import forge.game.event.GameEventGameRestarted;
import forge.game.event.GameEventLandPlayed;
import forge.game.event.GameEventLifeLoss;
import forge.game.event.GameEventManaBurn;
import forge.game.event.GameEventMulligan;
import forge.game.event.GameEventPlayerControl;
import forge.game.event.GameEventPoisonCounter;
import forge.game.event.GameEventCardTapped;
import forge.game.event.GameEventShuffle;
import forge.game.event.GameEventSpellResolved;
import forge.game.event.GameEventTokenCreated;
import forge.game.event.GameEventTurnPhase;
import forge.game.event.IGameEventVisitor;

/** 
 * This class is in charge of converting any forge.game.event.Event to a SoundEffectType.
 *
 */
public class EventVisualizer implements IGameEventVisitor<SoundEffectType> {


    public SoundEffectType visit(GameEventBlockerAssigned event) { return SoundEffectType.Block; }
    public SoundEffectType visit(GameEventCardDamaged event) { return SoundEffectType.Damage; }
    public SoundEffectType visit(GameEventCardDestroyed event) { return SoundEffectType.Destroy; }
    public SoundEffectType visit(GameEventCardDiscarded event) { return SoundEffectType.Discard; }
    public SoundEffectType visit(GameEventCardEquipped event) { return SoundEffectType.Equip; }
    public SoundEffectType visit(GameEventCardRegenerated event) { return SoundEffectType.Regen; }
    public SoundEffectType visit(GameEventCardSacrificed event) { return SoundEffectType.Sacrifice; }
    public SoundEffectType visit(GameEventCounterAdded event) { return event.Amount > 0 ? SoundEffectType.AddCounter : null; }
    public SoundEffectType visit(GameEventCounterRemoved event) { return event.Amount > 0 ? SoundEffectType.RemoveCounter : null; }
    public SoundEffectType visit(GameEventDrawCard event) { return SoundEffectType.Draw; }
    public SoundEffectType visit(GameEventEndOfTurn event) { return SoundEffectType.EndOfTurn; }
    public SoundEffectType visit(GameEventFlipCoin event) { return SoundEffectType.FlipCoin; }
    public SoundEffectType visit(GameEventLifeLoss event) { return SoundEffectType.LifeLoss; }
    public SoundEffectType visit(GameEventPoisonCounter event) { return SoundEffectType.Poison; }
    public SoundEffectType visit(GameEventShuffle event) { return SoundEffectType.Shuffle; }
    public SoundEffectType visit(GameEventTokenCreated event) { return SoundEffectType.Token; }

    /**
     * Plays the sound corresponding to the outcome of the duel.
     */
    public SoundEffectType visit(GameEventDuelOutcome event) {
        boolean humanWonTheDuel = event.result.getWinner() == Singletons.getControl().getLobby().getGuiPlayer();
        return humanWonTheDuel ? SoundEffectType.WinDuel : SoundEffectType.LoseDuel;
    }

    /**
     * Plays the sound corresponding to the card type/color when the card
     * ability resolves on the stack.
     */
    public SoundEffectType visit(GameEventSpellResolved evt) { 
        Card source = evt.Source;
        SpellAbility sa = evt.Spell;
    
        if (sa == null || source == null) {
            return null;
        }

        if (sa.isSpell()) {
            // if there's a specific effect for this particular card, play it and
            // we're done.
            if (hasSpecificCardEffect(source)) {
                return SoundEffectType.ScriptedEffect;
            }

            if (source.isCreature() && source.isArtifact()) {
                return SoundEffectType.ArtifactCreature;
            } else if (source.isCreature()) {
                return SoundEffectType.Creature;
            } else if (source.isArtifact()) {
                return SoundEffectType.Artifact;
            } else if (source.isInstant()) {
                return SoundEffectType.Instant;
            } else if (source.isPlaneswalker()) {
                return SoundEffectType.Planeswalker;
            } else if (source.isSorcery()) {
                return SoundEffectType.Sorcery;
            } else if (source.isEnchantment()) {
                return SoundEffectType.Enchantment;
            }
        }
        return null;
    }

    /**
     * Plays the sound corresponding to the change of the card's tapped state
     * (when a card is tapped or untapped).
     * 
     * @param tapped_state if true, the "tap" sound is played; otherwise, the
     * "untap" sound is played
     * @return the sound effect type
     */
    public SoundEffectType visit(GameEventCardTapped event) {
        return event.tapped ? SoundEffectType.Tap : SoundEffectType.Untap;
    }

    /**
     * Plays the sound corresponding to the land type when the land is played.
     *
     * @param land the land card that was played
     * @return the sound effect type
     */
    public SoundEffectType visit(GameEventLandPlayed event) { 
        Card land = event.Land;
        if (land == null) {
            return null;
        }

        // if there's a specific effect for this particular card, play it and
        // we're done.
        if (hasSpecificCardEffect(land)) {
            return SoundEffectType.ScriptedEffect;
        }

        for (SpellAbility sa : land.getManaAbility()) {
            String manaColors = sa.getManaPartRecursive().getOrigProduced();

            if (manaColors.contains("B")) return SoundEffectType.BlackLand;
            if (manaColors.contains("U")) return SoundEffectType.BlueLand;
            if (manaColors.contains("G")) return SoundEffectType.GreenLand;
            if (manaColors.contains("R")) return SoundEffectType.RedLand;
            if (manaColors.contains("W")) return SoundEffectType.WhiteLand;
        }

        // play a generic land sound if no other sound corresponded to it.
        return SoundEffectType.OtherLand;
    }

    /**
     * Play a specific sound effect based on card's name.
     *
     * @param c the card to play the sound effect for.
     * @return the sound effect type
     */
    private static boolean hasSpecificCardEffect(final Card c) {
        // Implement sound effects for specific cards here, if necessary.
        String effect = "";
        if (null != c) {
            effect = c.getSVar("SoundEffect");
        }
        return effect.isEmpty() ? false : true;
    }


    /**
     * Returns the value of the SoundEffect SVar of the card that triggered
     * the event, otherwise returns an empty string.
     * 
     * @param evt the event which is the source of the sound effect
     * @return a string containing the SoundEffect SVar, or empty string if
     * SVar:SoundEffect does not exist.
     */
    public String getScriptedSoundEffectName(GameEvent evt) {
        Card c = null;

        if (evt instanceof GameEventSpellResolved) {
            c = ((GameEventSpellResolved) evt).Source;
        } else if (evt instanceof GameEventLandPlayed) {
            c = ((GameEventLandPlayed) evt).Land;
        }

        return c != null ? c.getSVar("SoundEffect") : "";
    }

    // These are not used by sound system
    public SoundEffectType visit(GameEventGameRestarted event) { return null; }
    public SoundEffectType visit(GameEventDuelFinished event) { return null; }
    public SoundEffectType visit(GameEventAnteCardsSelected event) { return null; }
    public SoundEffectType visit(GameEventManaBurn event) { return null; }
    public SoundEffectType visit(GameEventMulligan event) { return null; }
    public SoundEffectType visit(GameEventPlayerControl event) { return null; }
    public SoundEffectType visit(GameEventTurnPhase event) { return null; }

}
