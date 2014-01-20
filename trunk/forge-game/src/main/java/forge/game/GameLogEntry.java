package forge.game;

public class GameLogEntry {
    public final String message;
    public final GameLogEntryType type;
    // might add here date and some other fields

    GameLogEntry(final GameLogEntryType type0, final String messageIn) {
        type = type0;
        message = messageIn;
    }
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return type.getCaption() + ": " + message;
    }
}