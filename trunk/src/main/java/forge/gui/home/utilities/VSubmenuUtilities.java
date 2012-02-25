package forge.gui.home.utilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;
import forge.gui.GuiUtils;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.properties.ForgeProps;
import forge.properties.NewConstants.Lang;
import forge.view.toolbox.FButton;
import forge.view.toolbox.FLabel;
import forge.view.toolbox.FOverlay;
import forge.view.toolbox.FPanel;
import forge.view.toolbox.FScrollPane;
import forge.view.toolbox.FSkin;

/** 
 * Singleton instance of "Colors" submenu in "Constructed" group.
 *
 */
public enum VSubmenuUtilities implements IVSubmenu {
    /** */
    SINGLETON_INSTANCE;

    /** */
    private final JPanel pnl = new JPanel();

    private final FLabel btnDownloadSetPics = new FLabel.Builder().opaque(true).hoverable(true)
            .text("Download LQ Set Pictures").fontScaleFactor(0.5).build();
    private final FLabel btnDownloadPics = new FLabel.Builder().opaque(true).hoverable(true)
            .text("Download LQ Card Pictures").fontScaleFactor(0.5).build();
    private final FLabel btnDownloadQuestImages = new FLabel.Builder().opaque(true).hoverable(true)
            .text("Download Quest Images").fontScaleFactor(0.5).build();
    private final FLabel btnReportBug = new FLabel.Builder().opaque(true).hoverable(true)
            .text("Report a Bug").fontScaleFactor(0.5).build();
    private final FLabel btnImportPictures = new FLabel.Builder().opaque(true).hoverable(true)
            .text("Import Pictures").fontScaleFactor(0.5).build();
    private final FLabel btnHowToPlay = new FLabel.Builder().opaque(true)
            .hoverable(true).text("How To Play").build();
    private final FLabel btnDownloadPrices = new FLabel.Builder().opaque(true).hoverable(true)
            .text("Download Card Prices").fontScaleFactor(0.5).build();
    private final FLabel btnLicensing = new FLabel.Builder().opaque(true)
            .hoverable(true).text("License Details").build();

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#populate()
     */
    @Override
    public void populate() {
        final String constraintsLBL = "w 90%!, h 20px!, gap 5% 0 3px 8px";
        final String constraintsBTN = "h 30px!, w 50%!, gap 25% 0 0 0";

        final JPanel pnlContent = new JPanel();
        pnlContent.setOpaque(false);
        pnlContent.setLayout(new MigLayout("insets 0, gap 0, wrap, ay center"));

        pnlContent.add(btnDownloadPics, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("Download default card picture for each card.")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        pnlContent.add(btnDownloadSetPics, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("Download full card picture sets for all cards from legacy releases of MTG.")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        pnlContent.add(btnDownloadQuestImages, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("Download tokens and icons used in Quest mode.")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        pnlContent.add(btnDownloadPrices, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("Download up-to-date price list for in-game card shops.")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        pnlContent.add(btnImportPictures, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("Import card pictures from a local version of Forge.")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        pnlContent.add(btnReportBug, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("Something broken?")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        pnlContent.add(btnHowToPlay, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("Rules of ")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        pnlContent.add(btnLicensing, constraintsBTN);
        pnlContent.add(new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text("About Forge")
                .fontStyle(Font.ITALIC).build(), constraintsLBL);

        final FScrollPane scr = new FScrollPane(pnlContent);
        scr.setBorder(null);

        pnl.removeAll();
        pnl.setOpaque(false);
        pnl.setLayout(new MigLayout("insets 0"));
        pnl.add(scr, "w 100%!, h 100%!");
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getGroup()
     */
    @Override
    public EMenuGroup getGroup() {
        return EMenuGroup.UTILITIES;
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getPanel()
     */
    @Override
    public JPanel getPanel() {
        return pnl;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnDownloadPics() {
        return btnDownloadPics;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnDownloadSetPics() {
        return btnDownloadSetPics;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnDownloadQuestImages() {
        return btnDownloadQuestImages;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnReportBug() {
        return btnReportBug;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnImportPictures() {
        return btnImportPictures;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnHowToPlay() {
        return btnHowToPlay;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnDownloadPrices() {
        return btnDownloadPrices;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnLicensing() {
        return btnLicensing;
    }

    /** */
    public void showLicensing() {
        final FOverlay overlay = GuiUtils.genericOverlay();
        final int w = overlay.getWidth();

        final String license = "Forge License Information" + "\r\n\r\n"
                + "This program is free software : you can redistribute it and/or modify "
                + "it under the terms of the GNU General Public License as published by "
                + "the Free Software Foundation, either version 3 of the License, or "
                + "(at your option) any later version." + "\r\n\r\n"
                + "This program is distributed in the hope that it will be useful, "
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
                + "GNU General Public License for more details." + "\r\n\r\n"
                + "You should have received a copy of the GNU General Public License "
                + "along with this program.  If not, see <http://www.gnu.org/licenses/>.";

        // Init directions text pane
        final JTextPane tpnDirections = new JTextPane();
        tpnDirections.setOpaque(false);
        tpnDirections.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        tpnDirections.setFont(FSkin.getFont(15));
        tpnDirections.setAlignmentX(SwingConstants.CENTER);
        tpnDirections.setFocusable(false);
        tpnDirections.setEditable(false);
        tpnDirections.setBorder(null);
        tpnDirections.setText(license);

        final StyledDocument doc = tpnDirections.getStyledDocument();
        final SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        final JButton btnCloseBig = new FButton("OK");
        btnCloseBig.setBounds(new Rectangle((w / 2 - 100), 510, 200, 30));
        btnCloseBig.addActionListener(new ActionListener() { @Override
            public void actionPerformed(final ActionEvent arg0) { overlay.hideOverlay(); } });

        final FPanel pnl = new FPanel();
        pnl.setCornerDiameter(0);
        pnl.setBackgroundTexture(FSkin.getIcon(FSkin.Backgrounds.BG_TEXTURE));
        pnl.setLayout(new MigLayout("insets 0, gap 0"));
        pnl.add(tpnDirections, "w 90%!, h 90%!, gap 5% 0 5% 0");
        pnl.setBounds(new Rectangle((w / 2 - 250), 80, 500, 400));

        overlay.setLayout(null);
        overlay.add(btnCloseBig);
        overlay.add(pnl);
        overlay.showOverlay();
    }

    /** */
    public void showHowToPlay() {
        final FOverlay overlay = GuiUtils.genericOverlay();
        final int w = overlay.getWidth();

        final String directions = ForgeProps.getLocalized(Lang.HowTo.MESSAGE);

        // Init directions text pane
        final JTextPane tpnDirections = new JTextPane();
        tpnDirections.setOpaque(true);
        tpnDirections.setBackground(Color.white);
        tpnDirections.setForeground(Color.black);
        tpnDirections.setFont(FSkin.getFont(15));
        tpnDirections.setAlignmentX(SwingConstants.CENTER);
        tpnDirections.setFocusable(false);
        tpnDirections.setEditable(false);
        tpnDirections.setBorder(new EmptyBorder(10, 20, 10, 20));
        tpnDirections.setText(directions);

        final StyledDocument doc = tpnDirections.getStyledDocument();
        final SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        final JButton btnCloseBig = new FButton("OK");
        btnCloseBig.setBounds(new Rectangle((w / 2 - 100), 510, 200, 30));
        btnCloseBig.addActionListener(new ActionListener() { @Override
            public void actionPerformed(final ActionEvent arg0) { overlay.hideOverlay(); } });

        final FScrollPane scr = new FScrollPane(tpnDirections, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scr.setBorder(new LineBorder(Color.black, 1));
        scr.setBounds(new Rectangle((w / 2 - 250), 80, 500, 400));

        overlay.setLayout(null);
        overlay.add(btnCloseBig);
        overlay.add(scr);
        overlay.showOverlay();
    }
}
