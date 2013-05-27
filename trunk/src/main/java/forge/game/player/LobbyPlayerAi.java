package forge.game.player;

import forge.Singletons;
import forge.game.GameState;
import forge.game.ai.AiProfileUtil;
import forge.properties.ForgePreferences.FPref;

public class LobbyPlayerAi extends LobbyPlayer {
    public LobbyPlayerAi(String name) {
        super(name);
    }

    private String aiProfile = "";
    
    public void setAiProfile(String profileName) {
        aiProfile = profileName;
    }

    public String getAiProfile() {
        return aiProfile;
    }

    @Override
    public PlayerType getType() {
        return PlayerType.COMPUTER;
    }

    @Override
    public Player getPlayer(GameState game) {
        Player ai = new Player(this, game);
        ai.setController(new PlayerControllerAi(game, ai, this));

        String currentAiProfile = Singletons.getModel().getPreferences().getPref(FPref.UI_CURRENT_AI_PROFILE);
        String lastProfileChosen = game.getMatch().getPlayedGames().isEmpty() ? currentAiProfile : getAiProfile();

        // TODO: implement specific AI profiles for quest mode.
        boolean wantRandomProfile = currentAiProfile.equals(AiProfileUtil.AI_PROFILE_RANDOM_DUEL) 
             || game.getMatch().getPlayedGames().isEmpty() && currentAiProfile.equals(AiProfileUtil.AI_PROFILE_RANDOM_MATCH); 

        setAiProfile(wantRandomProfile ? AiProfileUtil.getRandomProfile() : lastProfileChosen);
        System.out.println(String.format("AI profile %s was chosen for the lobby player %s.", getAiProfile(), getName()));
        return ai;
    }

    @Override
    public void hear(LobbyPlayer player, String message) { /* Local AI is deaf. */ }
}