package forge;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Function;

import forge.assets.FSkin;
import forge.assets.FSkinProp;
import forge.assets.FTextureImage;
import forge.assets.ISkinImage;
import forge.deck.Deck;
import forge.deck.FDeckViewer;
import forge.error.BugReporter;
import forge.events.UiEvent;
import forge.game.Game;
import forge.game.GameEntity;
import forge.game.GameType;
import forge.game.card.Card;
import forge.game.combat.Combat;
import forge.game.event.GameEventTurnBegan;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.player.RegisteredPlayer;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.interfaces.IButton;
import forge.interfaces.IGuiBase;
import forge.item.PaperCard;
import forge.match.input.InputQueue;
import forge.screens.match.FControl;
import forge.screens.match.views.VPhaseIndicator.PhaseLabel;
import forge.screens.match.winlose.ViewWinLose;
import forge.toolbox.FOptionPane;
import forge.toolbox.GuiChoose;
import forge.util.ThreadUtil;

public class GuiMobile implements IGuiBase {
    @Override
    public void invokeInEdtLater(Runnable proc) {
        Gdx.app.postRunnable(proc);
    }

    @Override
    public void invokeInEdtAndWait(final Runnable proc) {
        if (isGuiThread()) {
            // Just run in the current thread.
            proc.run();
        }
        else {
            //TODO
        }
    }

    @Override
    public boolean isGuiThread() {
        return !ThreadUtil.isGameThread();
    }

    @Override
    public String getInstallRoot() {
        switch (Gdx.app.getType()) {
        case Desktop:
            return "../forge-gui/";
        case Android:
            break; //TODO
        default:
            break;
        }
        return "";
    }

    @Override
    public String getAssetsDir() {
        return "res/";
    }

    @Override
    public boolean mayShowCard(Card card) {
        return FControl.mayShowCard(card);
    }

    @Override
    public void reportBug(String details) {
        BugReporter.reportBug(details);
    }

    @Override
    public void reportException(Throwable ex) {
        BugReporter.reportException(ex);
    }

    @Override
    public void reportException(Throwable ex, String message) {
        BugReporter.reportException(ex, message);
    }

    @Override
    public ISkinImage getUnskinnedIcon(String path) {
        return new FTextureImage(new Texture(path));
    }

    @Override
    public int showOptionDialog(String message, String title, FSkinProp icon, String[] options, int defaultOption) {
        return FOptionPane.showOptionDialog(message, title, icon == null ? null : FSkin.getImages().get(icon), options, defaultOption);
    }

    @Override
    public <T> T showInputDialog(String message, String title, FSkinProp icon, T initialInput, T[] inputOptions) {
        return FOptionPane.showInputDialog(message, title, icon == null ? null : FSkin.getImages().get(icon), initialInput, inputOptions);
    }

    @Override
    public <T> List<T> getChoices(final String message, final int min, final int max, final Collection<T> choices, final T selected, final Function<T, String> display) {
        return GuiChoose.getChoices(message, min, max, choices, selected, display);
    }

    @Override
    public <T> List<T> order(final String title, final String top, final int remainingObjectsMin, final int remainingObjectsMax,
            final List<T> sourceChoices, final List<T> destChoices, final Card referenceCard, final boolean sideboardingMode) {
        return GuiChoose.order(title, top, remainingObjectsMin, remainingObjectsMax, sourceChoices, destChoices, referenceCard, sideboardingMode);
    }

    @Override
    public void showCardList(final String title, final String message, final List<PaperCard> list) {
        Deck deck = new Deck(title + " - " + message);
        deck.getMain().addAllFlat(list);
        FDeckViewer.show(deck);
    }

    @Override
    public IButton getBtnOK() {
        return FControl.getView().getPrompt().getBtnOk();
    }

    @Override
    public IButton getBtnCancel() {
        return FControl.getView().getPrompt().getBtnCancel();
    }

    @Override
    public void focusButton(final IButton button) {
        //not needed for mobile game
    }

    @Override
    public void flashIncorrectAction() {
        //SDisplayUtil.remind(VPrompt.SINGLETON_INSTANCE); //TODO
    }

    @Override
    public void updatePhase() {
        PhaseHandler pH = FControl.getGame().getPhaseHandler();
        Player p = pH.getPlayerTurn();
        PhaseType ph = pH.getPhase();

        PhaseLabel lbl = FControl.getPlayerPanel(p).getPhaseIndicator().getLabel(ph);

        FControl.resetAllPhaseButtons();
        if (lbl != null) {
            lbl.setActive(true);
        }
    }

    @Override
    public void updateTurn(final GameEventTurnBegan event, final Game game) {
        //VField nextField = FControl.getFieldViewFor(event.turnOwner);
        //SDisplayUtil.showTab(nextField);
        FControl.getView().getPrompt().updateText(game);
    }

    @Override
    public void updatePlayerControl() {
        //TODO
    }

    @Override
    public void finishGame() {
        new ViewWinLose(FControl.getGame()).setVisible(true);
    }

    @Override
    public void updateStack() {
        FControl.getView().getStack().update();
    }

    @Override
    public void startMatch(GameType gameType, List<RegisteredPlayer> players) {
        FControl.startMatch(gameType, players);
    }

    @Override
    public void setPanelSelection(Card c) {
        //GuiUtils.setPanelSelection(c); //TODO
    }

    @Override
    public SpellAbility getAbilityToPlay(List<SpellAbility> abilities, Object triggerEvent) {
        if (abilities.isEmpty()) {
            return null;
        }
        if (abilities.size() == 1) {
            return abilities.get(0);
        }
        return GuiChoose.oneOrNone("Choose ability to play", abilities);
    }

    @Override
    public void hear(LobbyPlayer player, String message) {
        //FNetOverlay.SINGLETON_INSTANCE.addMessage(player.getName(), message); //TODO
    }

    @Override
    public int getAvatarCount() {
        if (FSkin.isLoaded()) {
            return FSkin.getAvatars().size();
        }
        return 0;
    }

    @Override
    public void fireEvent(UiEvent e) {
        FControl.fireEvent(e);
    }

    @Override
    public void setCard(Card card) {
        FControl.setCard(card);
    }

    @Override
    public void showCombat(Combat combat) {
        FControl.showCombat(combat);
    }

    @Override
    public void setUsedToPay(Card card, boolean b) {
        FControl.setUsedToPay(card, b);
    }

    @Override
    public void setHighlighted(Player player, boolean b) {
        FControl.setHighlighted(player, b);
    }

    @Override
    public void showPromptMessage(String message) {
        FControl.showMessage(message);
    }

    @Override
    public boolean stopAtPhase(Player playerTurn, PhaseType phase) {
        return FControl.stopAtPhase(playerTurn, phase);
    }

    @Override
    public InputQueue getInputQueue() {
        return FControl.getInputQueue();
    }

    @Override
    public Game getGame() {
        return FControl.getGame();
    }

    @Override
    public void updateZones(List<Pair<Player, ZoneType>> zonesToUpdate) {
        FControl.updateZones(zonesToUpdate);
    }

    @Override
    public void updateCards(Set<Card> cardsToUpdate) {
        FControl.updateCards(cardsToUpdate);
    }

    @Override
    public void updateManaPool(List<Player> manaPoolUpdate) {
        FControl.updateManaPool(manaPoolUpdate);
    }

    @Override
    public void updateLives(List<Player> livesUpdate) {
        FControl.updateLives(livesUpdate);
    }

    @Override
    public void endCurrentGame() {
        FControl.endCurrentGame();
    }

    @Override
    public Map<Card, Integer> getDamageToAssign(Card attacker, List<Card> blockers,
            int damageDealt, GameEntity defender, boolean overrideOrder) {
        return FControl.getDamageToAssign(attacker, blockers,
                damageDealt, defender, overrideOrder);
    }
}
