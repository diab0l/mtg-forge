package forge.screens.home.online;

import forge.UiCommand;
import forge.gui.framework.ICDoc;
import forge.screens.home.CLobby;
import forge.screens.home.VLobby;

public enum COnlineLobby implements ICDoc {
    SINGLETON_INSTANCE;

    private CLobby lobby;

    void setLobby(VLobby lobbyView) {
        lobby = new CLobby(lobbyView);
        initialize();
    }

    @Override
    public void register() {
    }

    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @Override
    public void update() {
        if (lobby != null) {
            lobby.update();
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @Override
    public void initialize() {
        if (lobby != null) {
            lobby.initialize();
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }

}