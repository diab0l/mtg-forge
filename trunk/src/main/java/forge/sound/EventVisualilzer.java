package forge.sound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import forge.Card;
import forge.card.spellability.SpellAbility;
import forge.game.event.Event;
import forge.game.event.LandPlayedEvent;
import forge.game.event.PoisonCounterEvent;
import forge.game.event.SpellResolvedEvent;

/** 
 * This class is in charge of converting any forge.game.event.Event to a SoundEffectType
 *
 */
public class EventVisualilzer {

    final static Map<Class<?>, SoundEffectType> matchTable = new HashMap<Class<?>, SoundEffectType>();
    
    public EventVisualilzer() { 
        matchTable.put(PoisonCounterEvent.class, SoundEffectType.Poison);
    }
    
    
    public final SoundEffectType getSoundForEvent(Event evt) {
        SoundEffectType fromMap = matchTable.get(evt);

        // call methods copied from Utils here
        if( evt instanceof SpellResolvedEvent ) {
            return getSoundEffectForResolve(((SpellResolvedEvent) evt).Source, ((SpellResolvedEvent) evt).Spell);
        }
        if ( evt instanceof LandPlayedEvent )
            return getSoundEffectForLand(((LandPlayedEvent) evt).Land);

        return fromMap;
    }
    
    /**
     * Plays the sound corresponding to the card type/color when the card
     * ability resolves on the stack.
     *
     * @param source the card to play the sound for.
     * @param sa the spell ability that was resolving.
     */
    public SoundEffectType getSoundEffectForResolve(final Card source, final SpellAbility sa) {
        if (sa == null || source == null) {
            return null;
        }

        if (sa.isSpell()) {
            // if there's a specific effect for this particular card, play it and
            // we're done.
            SoundEffectType specialEffect = getSpecificCardEffect(source);
            if( specialEffect != null ) return specialEffect;

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
     * Plays the sound corresponding to the land type when the land is played.
     *
     * @param land the land card that was played
     */
    public static SoundEffectType getSoundEffectForLand(final Card land) {
        if (land == null) {
            return null;
        }

        // if there's a specific effect for this particular card, play it and
        // we're done.
        SoundEffectType specialEffect = getSpecificCardEffect(land);
        if( specialEffect != null ) return specialEffect;


        final List<SpellAbility> manaProduced = land.getManaAbility();

        for (SpellAbility sa : manaProduced) {
            String manaColors = sa.getManaPart().getManaProduced();

            if (manaColors.contains("B")) {
                return SoundEffectType.BlackLand;
            }
            if (manaColors.contains("U")) {
                return SoundEffectType.BlueLand;
            }
            if (manaColors.contains("G")) {
                return SoundEffectType.GreenLand;
            }
            if (manaColors.contains("R")) {
                return SoundEffectType.RedLand;
            }
            if (manaColors.contains("W")) {
                return SoundEffectType.WhiteLand;
            }
        }

        // play a generic land sound if no other sound corresponded to it.
        return SoundEffectType.OtherLand;
    }

    /**
     * Play a specific sound effect based on card's name.
     *
     * @param c the card to play the sound effect for.
     * @return true if the special effect was found and played, otherwise
     *         false (in which case the type-based FX will be played, if
     *         applicable).
     */
    private static SoundEffectType getSpecificCardEffect(final Card c) {
        // Implement sound effects for specific cards here, if necessary.
        return null;
    }


    /**
     * TODO: Choose is the special type of event produces a single or lot of overlapping sounds (?)
     */
    public boolean isSyncSound(Event evt) {

        return true;
    }    

}
