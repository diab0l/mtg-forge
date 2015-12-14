package forge.planarconquest;

import java.util.Set;

import forge.deck.Deck;
import forge.game.GameType;
import forge.planarconquest.ConquestController.GameRunner;

public abstract class ConquestEvent {
    private final ConquestLocation location;
    private Deck opponentDeck;

    public ConquestEvent(ConquestLocation location0) {
        location = location0;
    }

    public ConquestLocation getLocation() {
        return location;
    }

    public Deck getOpponentDeck() {
        if (opponentDeck == null) {
            opponentDeck = buildOpponentDeck();
        }
        return opponentDeck;
    }

    protected abstract Deck buildOpponentDeck();
    public abstract void addVariants(Set<GameType> variants);
    public abstract String getOpponentName();
    public abstract String getAvatarImageKey();

    public static interface IConquestEventLauncher {
        void startGame(GameRunner gameRunner);
    }
}
