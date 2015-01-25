package forge.interfaces;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;

import forge.LobbyPlayer;
import forge.assets.FSkinProp;
import forge.assets.ISkinImage;
import forge.deck.CardPool;
import forge.download.GuiDownloadService;
import forge.game.GameEntity;
import forge.game.GameEntityView;
import forge.game.card.CardView;
import forge.game.player.DelayedReveal;
import forge.game.player.IHasIcon;
import forge.item.PaperCard;
import forge.player.PlayerControllerHuman;
import forge.sound.IAudioClip;
import forge.sound.IAudioMusic;
import forge.util.Callback;
import forge.util.FCollectionView;

public interface IGuiBase {
    boolean isRunningOnDesktop();
    String getCurrentVersion();
    String getAssetsDir();
    void invokeInEdtLater(Runnable runnable);
    void invokeInEdtAndWait(final Runnable proc);
    boolean isGuiThread();
    IGuiTimer createGuiTimer(Runnable proc, int interval);
    ISkinImage getSkinIcon(FSkinProp skinProp);
    ISkinImage getUnskinnedIcon(String path);
    ISkinImage getCardArt(PaperCard card);
    ISkinImage createLayeredImage(FSkinProp background, String overlayFilename, float opacity);
    void showBugReportDialog(String title, String text, boolean showExitAppBtn);
    void showImageDialog(ISkinImage image, String message, String title);
    int showOptionDialog(String message, String title, FSkinProp icon, String[] options, int defaultOption);
    int showCardOptionDialog(CardView card, String message, String title, FSkinProp icon, String[] options, int defaultOption);
    String showInputDialog(String message, String title, FSkinProp icon, String initialInput, String[] inputOptions);
    <T> List<T> getChoices(final String message, final int min, final int max, final Collection<T> choices, final T selected, final Function<T, String> display);
    <T> List<T> order(final String title, final String top, final int remainingObjectsMin, final int remainingObjectsMax,
            final List<T> sourceChoices, final List<T> destChoices, final CardView referenceCard, final boolean sideboardingMode);
    List<PaperCard> sideboard(CardPool sideboard, CardPool main);
    GameEntityView chooseSingleEntityForEffect(String title, FCollectionView<? extends GameEntity> optionList, DelayedReveal delayedReveal, boolean isOptional, PlayerControllerHuman controller);
    String showFileDialog(String title, String defaultDir);
    File getSaveFile(File defaultFile);
    void download(GuiDownloadService service, Callback<Boolean> callback);
    void showCardList(final String title, final String message, final List<PaperCard> list);
    boolean showBoxedProduct(final String title, final String message, final List<PaperCard> list);
    void setCard(CardView card);
    int getAvatarCount();
    void copyToClipboard(String text);
    void browseToUrl(String url) throws Exception;
    IAudioClip createAudioClip(String filename);
    IAudioMusic createAudioMusic(String filename);
    void startAltSoundSystem(String filename, boolean isSynchronized);
    void clearImageCache();
    void showSpellShop();
    void showBazaar();
    void setPlayerAvatar(LobbyPlayer player, IHasIcon ihi);
}