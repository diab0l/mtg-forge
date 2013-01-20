package forge.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import forge.Card;
import forge.deck.Deck;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.item.CardPrinted;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class MatchStartHelper {
    private final Map<LobbyPlayer, PlayerStartConditions> players = new HashMap<LobbyPlayer, PlayerStartConditions>();

    public void addPlayer(final LobbyPlayer player, final PlayerStartConditions c) {
        players.put(player, c);
    }

    public void addPlayer(final LobbyPlayer player, final Deck deck) {
        PlayerStartConditions start = new PlayerStartConditions(deck);
        players.put(player, start);
    }

    public void addVanguardPlayer(final LobbyPlayer player, final Deck deck, final CardPrinted avatar) {

        PlayerStartConditions start = new PlayerStartConditions(deck);

        start.setStartingLife(start.getStartingLife() + avatar.getCard().getLife());
        start.setStartingHand(start.getStartingHand() + avatar.getCard().getHand());

        start.setCardsInCommand(new Function<Player, Iterable<Card>>() {

            @Override
            public Iterable<Card> apply(Player p) {
                List<Card> res = new ArrayList<Card>();
                res.add(avatar.toForgeCard(p));
                return res;
            }

        });

        players.put(player, start);
    }

    public void addArchenemy(final LobbyPlayer player, final Deck deck, final Iterable<CardPrinted> schemes) {
        PlayerStartConditions start = new PlayerStartConditions(deck);

        start.setSchemes(new Function<Player, Iterable<Card>>() {

            @Override
            public Iterable<Card> apply(Player p) {
                List<Card> res = new ArrayList<Card>();
                for (CardPrinted cp : schemes) {
                    res.add(cp.toForgeCard(p));
                }

                return res;
            }

        });

        players.put(player, start);
    }
    
    public void addPlanechasePlayer(final LobbyPlayer player, final Deck deck, final Iterable<CardPrinted> planes) {
        PlayerStartConditions start = new PlayerStartConditions(deck);

        start.setPlanes(new Function<Player, Iterable<Card>>() {

            @Override
            public Iterable<Card> apply(Player p) {
                List<Card> res = new ArrayList<Card>();
                for (CardPrinted cp : planes) {
                    res.add(cp.toForgeCard(p));
                }

                return res;
            }

        });

        players.put(player, start);
    }

    public Map<LobbyPlayer, PlayerStartConditions> getPlayerMap() {

        return players;
    }

}
