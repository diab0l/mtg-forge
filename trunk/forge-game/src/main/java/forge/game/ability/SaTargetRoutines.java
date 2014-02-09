package forge.game.ability;

import com.google.common.collect.Lists;
import forge.game.GameObject;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;

import java.util.List;

// Class contains all that methods that are used by both effects and AI to fetch their targets.
// {SA}Effect and {SA}Ai now inherit from this class to use these routines, though they should not.

public class SaTargetRoutines {

    // Cards
    protected List<Card> getTargetCards(SpellAbility sa) {                                  return getCards(false, "Defined", sa); }
    protected List<Card> getTargetCards(SpellAbility sa, String definedParam) {             return getCards(false, definedParam, sa); }
    protected List<Card> getDefinedCardsOrTargeted(SpellAbility sa) {                       return getCards(true, "Defined", sa); }
    protected List<Card> getDefinedCardsOrTargeted(SpellAbility sa, String definedParam) {  return getCards(true, definedParam, sa); }

    private List<Card> getCards(boolean definedFirst, String definedParam, SpellAbility sa) {
        boolean useTargets = sa.usesTargeting() && (!definedFirst || !sa.hasParam(definedParam))
                && sa.getTargets() != null && (sa.getTargets().isTargetingAnyCard() || sa.getTargets().getTargets().isEmpty());
        return useTargets ? Lists.newArrayList(sa.getTargets().getTargetCards()) 
                : AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam(definedParam), sa);
    }

    // Players
    protected List<Player> getTargetPlayers(SpellAbility sa) {                                  return getPlayers(false, "Defined", sa); }
    protected List<Player> getTargetPlayers(SpellAbility sa, String definedParam) {             return getPlayers(false, definedParam, sa); }
    protected List<Player> getDefinedPlayersOrTargeted(SpellAbility sa ) {                      return getPlayers(true, "Defined", sa); }
    protected List<Player> getDefinedPlayersOrTargeted(SpellAbility sa, String definedParam) {  return getPlayers(true, definedParam, sa); }

    private List<Player> getPlayers(boolean definedFirst, String definedParam, SpellAbility sa) {
        boolean useTargets = sa.usesTargeting() && (!definedFirst || !sa.hasParam(definedParam));
        return useTargets ? Lists.newArrayList(sa.getTargets().getTargetPlayers()) 
                : AbilityUtils.getDefinedPlayers(sa.getSourceCard(), sa.getParam(definedParam), sa);
    }

    // Spells
    protected List<SpellAbility> getTargetSpells(SpellAbility sa) {                                  return getSpells(false, "Defined", sa); }
    protected List<SpellAbility> getTargetSpells(SpellAbility sa, String definedParam) {             return getSpells(false, definedParam, sa); }
    protected List<SpellAbility> getDefinedSpellsOrTargeted(SpellAbility sa, String definedParam) {  return getSpells(true, definedParam, sa); }
    
    private List<SpellAbility> getSpells(boolean definedFirst, String definedParam, SpellAbility sa) {
        boolean useTargets = sa.usesTargeting() && (!definedFirst || !sa.hasParam(definedParam));
        return useTargets ? Lists.newArrayList(sa.getTargets().getTargetSpells()) 
                : AbilityUtils.getDefinedSpellAbilities(sa.getSourceCard(), sa.getParam(definedParam), sa);
    }

    // Targets of unspecified type
    protected List<GameObject> getTargets(SpellAbility sa) {                                   return getTargetables(false, "Defined", sa); }
    protected List<GameObject> getTargets(SpellAbility sa, String definedParam) {              return getTargetables(false, definedParam, sa); }
    protected List<GameObject> getDefinedOrTargeteded(SpellAbility sa, String definedParam) {  return getTargetables(true, definedParam, sa); }

    private List<GameObject> getTargetables(boolean definedFirst, String definedParam, SpellAbility sa) {
        boolean useTargets = sa.usesTargeting() && (!definedFirst || !sa.hasParam(definedParam));
        return useTargets ? Lists.newArrayList(sa.getTargets().getTargets()) 
                : AbilityUtils.getDefinedObjects(sa.getSourceCard(), sa.getParam(definedParam), sa);
    }
}