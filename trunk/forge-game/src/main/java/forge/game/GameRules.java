package forge.game;

public class GameRules {
    private GameType gameType;
    private boolean manaBurn;
    private int poisonCountersToLose = 10; // is commonly 10, but turns into 15 for 2HG
    private int gamesPerMatch = 3;
    private int gamesToWinMatch = 2;
    private boolean playForAnte = false;
    
    public GameRules(GameType type) {
        this.gameType = type;
    }

    public GameType getGameType() {
        return gameType;
    }

    /**
     * @return the manaBurn
     */
    public boolean hasManaBurn() {
        return manaBurn;
    }
    /**
     * @param manaBurn the manaBurn to set
     */
    public void setManaBurn(boolean manaBurn) {
        this.manaBurn = manaBurn;
    }
    /**
     * @return the poisonCountersToLose
     */
    public int getPoisonCountersToLose() {
        return poisonCountersToLose;
    }
    /**
     * @param poisonCountersToLose the poisonCountersToLose to set
     */
    public void setPoisonCountersToLose(int amount) {
        this.poisonCountersToLose = amount;
    }

    public int getGamesPerMatch() {
        return gamesPerMatch;
    }

    public void setGamesPerMatch(int gamesPerMatch) {
        this.gamesPerMatch = gamesPerMatch;
        this.gamesToWinMatch = (int)Math.ceil((gamesPerMatch+1)/2);
    }

    public boolean useAnte() {
        return playForAnte;
    }

    public void setPlayForAnte(boolean useAnte) {
        this.playForAnte = useAnte;
    }

    public int getGamesToWinMatch() {
        return gamesToWinMatch;
    }

    // it's a preference, not rule... but I could hardly find a better place for it
    public boolean canCloneUseTargetsImage;
}
