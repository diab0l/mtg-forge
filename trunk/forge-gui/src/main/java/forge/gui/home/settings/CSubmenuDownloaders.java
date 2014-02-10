package forge.gui.home.settings;

import forge.UiCommand;
import forge.error.BugReporter;
import forge.gui.ImportDialog;
import forge.gui.download.GuiDownloadPicturesLQ;
import forge.gui.download.GuiDownloadPrices;
import forge.gui.download.GuiDownloadQuestImages;
import forge.gui.download.GuiDownloadSetPicturesLQ;
import forge.gui.framework.ICDoc;

import javax.swing.*;

/** 
 * Controls the utilities submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
@SuppressWarnings("serial")
public enum CSubmenuDownloaders implements ICDoc {
    SINGLETON_INSTANCE;

    private final UiCommand cmdLicensing = new UiCommand() { @Override
        public void run() { VSubmenuDownloaders.SINGLETON_INSTANCE.showLicensing(); } };
    private final UiCommand cmdPicDownload  = new UiCommand() { @Override
        public void run() { new GuiDownloadPicturesLQ(); } };
    private final UiCommand cmdSetDownload = new UiCommand() { @Override
        public void run() { new GuiDownloadSetPicturesLQ(); } };
    private final UiCommand cmdQuestImages = new UiCommand() { @Override
        public void run() { new GuiDownloadQuestImages(); } };
    private final UiCommand cmdDownloadPrices = new UiCommand() { @Override
        public void run() { new GuiDownloadPrices(); } };
    private final UiCommand cmdHowToPlay = new UiCommand() { @Override
        public void run() { VSubmenuDownloaders.SINGLETON_INSTANCE.showHowToPlay(); } };
    private final UiCommand cmdImportPictures = new UiCommand() { @Override
        public void run() { new ImportDialog(null, null); } };
    private final UiCommand cmdReportBug = new UiCommand() { @Override
        public void run() { BugReporter.reportBug(null); }
    };

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void initialize() {
        final VSubmenuDownloaders view = VSubmenuDownloaders.SINGLETON_INSTANCE;
        view.setDownloadPicsCommand(cmdPicDownload);
        view.setDownloadSetPicsCommand(cmdSetDownload);
        view.setDownloadQuestImagesCommand(cmdQuestImages);
        view.setReportBugCommand(cmdReportBug);
        view.setImportPicturesCommand(cmdImportPictures);
        view.setHowToPlayCommand(cmdHowToPlay);
        view.setDownloadPricesCommand(cmdDownloadPrices);
        view.setLicensingCommand(cmdLicensing);
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void update() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VSubmenuDownloaders.SINGLETON_INSTANCE.focusTopButton();
            }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }
}
