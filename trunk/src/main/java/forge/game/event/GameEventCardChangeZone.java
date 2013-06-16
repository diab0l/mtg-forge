package forge.game.event;

import forge.Card;
import forge.game.zone.Zone;

public class GameEventCardChangeZone extends GameEvent {
    

     public final Card card;
     public final Zone from;
     public final Zone to;
     
    public GameEventCardChangeZone(Card c, Zone zoneFrom, Zone zoneTo) {
        card = c;
        from = zoneFrom;
        to = zoneTo;
    }

    @Override
    public <T> T visit(IGameEventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

