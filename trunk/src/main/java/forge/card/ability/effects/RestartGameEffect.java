package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.card.trigger.TriggerHandler;
import forge.card.trigger.TriggerType;
import forge.game.GameAction;
import forge.game.GameAge;
import forge.game.GameNew;
import forge.game.Game;
import forge.game.RegisteredPlayer;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;

public class RestartGameEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.AbilityFactoryAlterLife.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        final Player activator = sa.getActivatingPlayer();
        final Game game = activator.getGame();
        List<Player> players = game.getPlayers();
        Map<Player, List<Card>> playerLibraries = new HashMap<Player, List<Card>>();

        // Don't grab Ante Zones
        List<ZoneType> restartZones = new ArrayList<ZoneType>(Arrays.asList(ZoneType.Battlefield,
                ZoneType.Library, ZoneType.Graveyard, ZoneType.Hand, ZoneType.Exile, ZoneType.Command));

        ZoneType leaveZone = ZoneType.smartValueOf(sa.hasParam("RestrictFromZone") ? sa.getParam("RestrictFromZone") : null);
        restartZones.remove(leaveZone);
        String leaveRestriction = sa.hasParam("RestrictFromValid") ? sa.getParam("RestrictFromValid") : "Card";

        for (Player p : players) {
            List<Card> newLibrary = new ArrayList<Card>(p.getCardsIn(restartZones));
            List<Card> filteredCards = null;
            if (leaveZone != null) {
                filteredCards = CardLists.filter(p.getCardsIn(leaveZone),
                        CardPredicates.restriction(leaveRestriction.split(","), p, sa.getSourceCard()));
            }

            newLibrary.addAll(filteredCards);
            playerLibraries.put(p, newLibrary);
        }
        
        //Card.resetUniqueNumber();
        // need this code here, otherwise observables fail
        forge.card.trigger.Trigger.resetIDs();
        TriggerHandler trigHandler = game.getTriggerHandler();
        trigHandler.clearDelayedTrigger();
        trigHandler.cleanUpTemporaryTriggers();
        trigHandler.suppressMode(TriggerType.ChangesZone);

        game.getStack().reset();
        GameAction action = game.getAction();
    
        List<Player> gamePlayers = game.getRegisteredPlayers();
        for( int i = 0; i < gamePlayers.size(); i++ ) {

            final Player player = gamePlayers.get(i);
            if( player.hasLost()) continue;
            
            RegisteredPlayer psc = game.getMatch().getPlayers().get(i);
            
            player.setStartingLife(psc.getStartingLife());
            player.setPoisonCounters(0, sa.getSourceCard());
            player.setNumLandsPlayed(0);
            GameNew.putCardsOnBattlefield(player, psc.getCardsOnBattlefield(player));
    
            List<Card> newLibrary = playerLibraries.get(player);
            for (Card c : newLibrary) {
                action.moveToLibrary(c, 0);
            }
    
            player.shuffle();
        }
    
        trigHandler.clearSuppression(TriggerType.ChangesZone);
    
        game.setAge(GameAge.RestartedByKarn);
        // Do not need this because ability will resolve only during that player's turn
        //game.getPhaseHandler().setPlayerTurn(sa.getActivatingPlayer());
        
        // Set turn number?
        
        // The rest is handled by phaseHandler 
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.AbilityFactoryAlterLife.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public String getStackDescription(SpellAbility sa) {
        String desc = sa.getParam("SpellDescription");

        if (desc == null) {
            desc = "Restart the game.";
        }

        return desc.replace("CARDNAME", sa.getSourceCard().getName());
    }
}

