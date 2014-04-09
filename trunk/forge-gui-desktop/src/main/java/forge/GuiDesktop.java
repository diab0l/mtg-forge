package forge;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;

import forge.assets.FSkinProp;
import forge.assets.ISkinImage;
import forge.control.FControl;
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
import forge.gui.CardListViewer;
import forge.gui.FNetOverlay;
import forge.gui.GuiChoose;
import forge.gui.GuiUtils;
import forge.gui.SOverlayUtils;
import forge.gui.framework.SDisplayUtil;
import forge.gui.framework.SLayoutIO;
import forge.interfaces.IButton;
import forge.interfaces.IGuiBase;
import forge.item.PaperCard;
import forge.match.input.InputQueue;
import forge.net.FServer;
import forge.screens.match.CMatchUI;
import forge.screens.match.VMatchUI;
import forge.screens.match.ViewWinLose;
import forge.screens.match.controllers.CPrompt;
import forge.screens.match.controllers.CStack;
import forge.screens.match.views.VField;
import forge.screens.match.views.VHand;
import forge.screens.match.views.VPrompt;
import forge.toolbox.FButton;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinImage;
import forge.toolbox.special.PhaseLabel;
import forge.util.BuildInfo;

public class GuiDesktop implements IGuiBase {
    public void invokeInEdtLater(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }

    public void invokeInEdtAndWait(final Runnable proc) {
        if (SwingUtilities.isEventDispatchThread()) {
            // Just run in the current thread.
            proc.run();
        }
        else {
            try {
                SwingUtilities.invokeAndWait(proc);
            }
            catch (final InterruptedException exn) {
                throw new RuntimeException(exn);
            }
            catch (final InvocationTargetException exn) {
                throw new RuntimeException(exn);
            }
        }
    }

    public boolean isGuiThread() {
        return SwingUtilities.isEventDispatchThread();
    }

    public String getAssetsRoot() {
        return StringUtils.containsIgnoreCase(BuildInfo.getVersionString(), "svn") ?
                "../forge-gui/res/" : "res/";
    }

    public boolean mayShowCard(Card card) {
        return Singletons.getControl().mayShowCard(card);
    }

    public void reportBug(String details) {
        BugReporter.reportBug(details);
    }
    public void reportException(Throwable ex) {
        BugReporter.reportException(ex);
    }
    public void reportException(Throwable ex, String message) {
        BugReporter.reportException(ex, message);
    }

    public boolean showConfirmDialog(String message) {
        return FOptionPane.showConfirmDialog(message);
    }

    public ISkinImage getUnskinnedIcon(String path) {
        return new FSkin.UnskinnedIcon(path);
    }
    
    public int showOptionDialog(String message, String title, ISkinImage icon, String[] options, int defaultOption) {
        return FOptionPane.showOptionDialog(message, title, (SkinImage)icon, options, defaultOption);
    }

    public <T> T showInputDialog(String message, String title, ISkinImage icon, T initialInput, T[] inputOptions) {
        return FOptionPane.showInputDialog(message, title, (SkinImage)icon, initialInput, inputOptions);
    }

    public <T> List<T> getChoices(final String message, final int min, final int max, final Collection<T> choices, final T selected, final Function<T, String> display) {
        return GuiChoose.getChoices(message, min, max, choices, selected, display);
    }

    public <T> List<T> order(final String title, final String top, final int remainingObjectsMin, final int remainingObjectsMax,
            final List<T> sourceChoices, final List<T> destChoices, final Card referenceCard, final boolean sideboardingMode) {
        return GuiChoose.order(title, top, remainingObjectsMin, remainingObjectsMax, sourceChoices, destChoices, referenceCard, sideboardingMode);
    }

    public void showCardList(final String title, final String message, final List<PaperCard> list) {
        final CardListViewer cardView = new CardListViewer(title, message, list);
        cardView.setVisible(true);
        cardView.dispose();
    }

    public IButton getBtnOK() {
        return VMatchUI.SINGLETON_INSTANCE.getBtnOK();
    }

    public IButton getBtnCancel() {
        return VMatchUI.SINGLETON_INSTANCE.getBtnCancel();
    }

    public void focusButton(final IButton button) {
        // ensure we don't steal focus from an overlay
        if (!SOverlayUtils.overlayHasFocus()) {
            FThreads.invokeInEdtLater(new Runnable() {
                @Override
                public void run() {
                    ((FButton)button).requestFocusInWindow();
                }
            });
        }
    }

    public void flashIncorrectAction() {
        SDisplayUtil.remind(VPrompt.SINGLETON_INSTANCE);
    }

    public void updatePhase() {
        PhaseHandler pH = Singletons.getControl().getObservedGame().getPhaseHandler();
        Player p = pH.getPlayerTurn();
        PhaseType ph = pH.getPhase();

        final CMatchUI matchUi = CMatchUI.SINGLETON_INSTANCE;
        PhaseLabel lbl = matchUi.getFieldViewFor(p).getPhaseIndicator().getLabelFor(ph);

        matchUi.resetAllPhaseButtons();
        if (lbl != null) lbl.setActive(true);
    }

    public void updateTurn(final GameEventTurnBegan event, final Game game) {
        VField nextField = CMatchUI.SINGLETON_INSTANCE.getFieldViewFor(event.turnOwner);
        SDisplayUtil.showTab(nextField);
        CPrompt.SINGLETON_INSTANCE.updateText(game);
    }

    public void updatePlayerControl() {
        CMatchUI.SINGLETON_INSTANCE.initHandViews(FServer.getLobby().getGuiPlayer());
        SLayoutIO.loadLayout(null);
        VMatchUI.SINGLETON_INSTANCE.populate();
        for (VHand h : VMatchUI.SINGLETON_INSTANCE.getHands()) {
            h.getLayoutControl().updateHand();
        }
    }

    public void finishGame() {
        new ViewWinLose(Singletons.getControl().getObservedGame());
        SOverlayUtils.showOverlay();
    }

    public void updateStack() {
        CStack.SINGLETON_INSTANCE.update();
    }

    public void startMatch(GameType gameType, List<RegisteredPlayer> players) {
        FControl.instance.startMatch(gameType, players);
    }

    public void setPanelSelection(Card c) {
        GuiUtils.setPanelSelection(c);
    }

    public SpellAbility getAbilityToPlay(List<SpellAbility> abilities, Object triggerEvent) {
        if (triggerEvent == null) {
            if (abilities.isEmpty()) {
                return null;
            }
            if (abilities.size() == 1) {
                return abilities.get(0);
            }
            return GuiChoose.oneOrNone("Choose ability to play", abilities);
        }

        if (abilities.isEmpty()) {
            return null;
        }
        if (abilities.size() == 1 && !abilities.get(0).promptIfOnlyPossibleAbility()) {
            if (abilities.get(0).canPlay()) {
                return abilities.get(0); //only return ability if it's playable, otherwise return null
            }
            return null;
        }

        //show menu if mouse was trigger for ability
        final JPopupMenu menu = new JPopupMenu("Abilities");

        boolean enabled;
        boolean hasEnabled = false;
        int shortcut = KeyEvent.VK_1; //use number keys as shortcuts for abilities 1-9
        for (final SpellAbility ab : abilities) {
            enabled = ab.canPlay();
            if (enabled) {
                hasEnabled = true;
            }
            GuiUtils.addMenuItem(menu, FSkin.encodeSymbols(ab.toString(), true),
                    shortcut > 0 ? KeyStroke.getKeyStroke(shortcut, 0) : null,
                    new Runnable() {
                        @Override
                        public void run() {
                            CPrompt.SINGLETON_INSTANCE.getInputControl().selectAbility(ab);
                        }
                    }, enabled);
            if (shortcut > 0) {
                shortcut++;
                if (shortcut > KeyEvent.VK_9) {
                    shortcut = 0; //stop adding shortcuts after 9
                }
            }
        }
        if (hasEnabled) { //only show menu if at least one ability can be played
            SwingUtilities.invokeLater(new Runnable() { //use invoke later to ensure first ability selected by default
                public void run() {
                    MenuSelectionManager.defaultManager().setSelectedPath(new MenuElement[]{menu, menu.getSubElements()[0]});
                }
            });
            MouseEvent mouseEvent = (MouseEvent) triggerEvent;
            menu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }

        return null; //delay ability until choice made
    }

    public void hear(LobbyPlayer player, String message) {
        FNetOverlay.SINGLETON_INSTANCE.addMessage(player.getName(), message);
    }

    public int getAvatarCount() {
        if (FSkin.isLoaded()) {
            FSkin.getAvatars().size();
        }
        return 0;
    }

    @Override
    public int showOptionDialog(String message, String title, FSkinProp icon, String[] options, int defaultOption) {
        return FOptionPane.showOptionDialog(message, title, FSkin.getImage(icon), options, defaultOption);
    }

    @Override
    public <T> T showInputDialog(String message, String title, FSkinProp icon, T initialInput, T[] inputOptions) {
        return FOptionPane.showInputDialog(message, title, FSkin.getImage(icon), initialInput, inputOptions);
    }

    @Override
    public void fireEvent(UiEvent e) {
        CMatchUI.SINGLETON_INSTANCE.fireEvent(e);
    }

    @Override
    public void setCard(Card card) {
        CMatchUI.SINGLETON_INSTANCE.setCard(card);
    }

    @Override
    public void showCombat(Combat combat) {
        CMatchUI.SINGLETON_INSTANCE.showCombat(combat);
    }

    @Override
    public void setUsedToPay(Card card, boolean b) {
        CMatchUI.SINGLETON_INSTANCE.setUsedToPay(card, b);
    }

    @Override
    public void setHighlighted(Player player, boolean b) {
        CMatchUI.SINGLETON_INSTANCE.setHighlighted(player, b);
    }

    @Override
    public void showPromptMessage(String message) {
        CMatchUI.SINGLETON_INSTANCE.showMessage(message);
    }

    @Override
    public boolean stopAtPhase(Player playerTurn, PhaseType phase) {
        return CMatchUI.SINGLETON_INSTANCE.stopAtPhase(playerTurn, phase);
    }

    @Override
    public InputQueue getInputQueue() {
        return FControl.instance.getInputQueue();
    }

    @Override
    public Game getGame() {
        return FControl.instance.getObservedGame();
    }

    @Override
    public void updateZones(List<Pair<Player, ZoneType>> zonesToUpdate) {
        CMatchUI.SINGLETON_INSTANCE.updateZones(zonesToUpdate);
    }

    @Override
    public void updateCards(Set<Card> cardsToUpdate) {
        CMatchUI.SINGLETON_INSTANCE.updateCards(cardsToUpdate);
    }

    @Override
    public void updateManaPool(List<Player> manaPoolUpdate) {
        CMatchUI.SINGLETON_INSTANCE.updateManaPool(manaPoolUpdate);
    }

    @Override
    public void updateLives(List<Player> livesUpdate) {
        CMatchUI.SINGLETON_INSTANCE.updateLives(livesUpdate);
    }

    @Override
    public void endCurrentGame() {
        FControl.instance.endCurrentGame();
    }

    @Override
    public Map<Card, Integer> getDamageToAssign(Card attacker, List<Card> blockers,
            int damageDealt, GameEntity defender, boolean overrideOrder) {
        return CMatchUI.SINGLETON_INSTANCE.getDamageToAssign(attacker, blockers,
                damageDealt, defender, overrideOrder);
    }
}
