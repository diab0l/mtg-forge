package forge.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;
import forge.AllZone;
import forge.Singletons;
import forge.control.FControl;
import forge.gui.deckeditor.VDeckEditorUI;
import forge.gui.framework.DragCell;
import forge.gui.framework.EDocID;
import forge.gui.framework.SLayoutConstants;
import forge.gui.home.CMainMenu;
import forge.gui.home.VHomeUI;
import forge.gui.match.VMatchUI;
import forge.gui.toolbox.FOverlay;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FSkin;

/** */
public enum FView {
    /** */
    SINGLETON_INSTANCE;
    private final List<DragCell> allCells = new ArrayList<DragCell>();
    private SplashFrame splash;

    // Non-singleton instances (deprecated, but not updated yet)
    private ViewBazaarUI bazaar = null;

    // Top-level UI components; all have getters.
    private final JFrame frmDocument = new JFrame();
    private final JPanel pnlContent = new JPanel();
    private final FPanel pnlInsets = new FPanel(new BorderLayout());
    private final JPanel pnlPreview = new PreviewPanel();
    private final JPanel pnlTabOverflow = new JPanel(new MigLayout("insets 0, gap 0, wrap"));
    private final JLayeredPane lpnDocument = new JLayeredPane();

    //
    private FView() {
        splash = new SplashFrame();
    }

    /** */
    public void initialize() {
        // Frame styling
        frmDocument.setMinimumSize(new Dimension(800, 600));
        frmDocument.setLocationRelativeTo(null);
        frmDocument.setExtendedState(frmDocument.getExtendedState() | Frame.MAXIMIZED_BOTH);
        frmDocument.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmDocument.setIconImage(FSkin.getIcon(FSkin.InterfaceIcons.ICO_FAVICON).getImage());
        frmDocument.setTitle("Forge: " + Singletons.getModel().getBuildInfo().getVersion());

        // Frame components
        frmDocument.setContentPane(lpnDocument);
        lpnDocument.add(pnlInsets, (Integer) 1);
        lpnDocument.add(pnlPreview, (Integer) 2);
        lpnDocument.add(pnlTabOverflow, (Integer) 3);
        lpnDocument.add(FOverlay.SINGLETON_INSTANCE.getPanel(), JLayeredPane.MODAL_LAYER);

        pnlInsets.add(pnlContent, BorderLayout.CENTER);
        pnlInsets.setBackgroundTexture(FSkin.getIcon(FSkin.Backgrounds.BG_TEXTURE));
        pnlInsets.setForegroundImage(FSkin.getIcon(FSkin.Backgrounds.BG_MATCH));
        pnlInsets.setBorder(new EmptyBorder(
                SLayoutConstants.BORDER_T, SLayoutConstants.BORDER_T, 0, 0));

        pnlContent.setOpaque(false);
        pnlContent.setLayout(null);

        FOverlay.SINGLETON_INSTANCE.getPanel().setBackground(FSkin.getColor(FSkin.Colors.CLR_OVERLAY));

        // Populate all drag tab components.
        this.cacheUIStates();

        // Initialize actions on all drag tab components (which should
        // be realized / populated already).
        for (EDocID doc : EDocID.values()) {
            doc.getDoc().getLayoutControl().initialize();
        }

        // All is ready to go - fire up home screen and discard splash frame.
        Singletons.getControl().changeState(FControl.HOME_SCREEN);
        CMainMenu.SINGLETON_INSTANCE.selectPrevious();

        FView.this.splash.dispose();
        FView.this.splash = null;

        frmDocument.setVisible(true);
    }

    /** @return {@link javax.swing.JFrame} */
    public JFrame getFrame() {
        return frmDocument;
    }

    /** @return {@link javax.swing.JLayeredPane} */
    public JLayeredPane getLpnDocument() {
        return lpnDocument;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlInsets() {
        return pnlInsets;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlContent() {
        return pnlContent;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlPreview() {
        return pnlPreview;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlTabOverflow() {
        return pnlTabOverflow;
    }

    /** @return {@link java.util.List}<{@link forge.gui.framework.DragCell}> */
    public List<DragCell> getDragCells() {
        final List<DragCell> clone = new ArrayList<DragCell>();
        clone.addAll(allCells);
        return clone;
    }

    /** @param pnl0 &emsp; {@link forge.gui.framework.DragCell} */
    public void addDragCell(final DragCell pnl0) {
        allCells.add(pnl0);
        pnlContent.add(pnl0);
    }

    /** @param pnl0 &emsp; {@link forge.gui.framework.DragCell} */
    public void removeDragCell(final DragCell pnl0) {
        allCells.remove(pnl0);
        pnlContent.remove(pnl0);
    }

    /** */
    public void removeAllDragCells() {
        allCells.clear();
        pnlContent.removeAll();
    }

    /** PreviewPanel shows where a dragged component could
     * come to rest when the mouse is released.<br>
     * This class is an unfortunate necessity to overcome
     * translucency issues for preview panel. */
    @SuppressWarnings("serial")
    class PreviewPanel extends JPanel {
        /** PreviewPanel shows where a dragged component could
         * come to rest when the mouse is released. */
        public PreviewPanel() {
            super();
            setOpaque(false);
            setVisible(false);
            setBorder(new LineBorder(Color.DARK_GRAY, 2));
        }

        @Override
        public void paintComponent(final Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /** @return {@link forge.view.ViewBazaarUI} */
    public ViewBazaarUI getViewBazaar() {
        if (Singletons.getControl().getState() != FControl.QUEST_BAZAAR) {
            throw new IllegalArgumentException("FView$getViewBazaar\n"
                    + "may only be called while the bazaar UI is showing.");
        }
        return FView.this.bazaar;
    }

    /** */
    private void cacheUIStates() {
        FView.this.bazaar = new ViewBazaarUI(AllZone.getQuest().getBazaar());
        VMatchUI.SINGLETON_INSTANCE.instantiate();
        VHomeUI.SINGLETON_INSTANCE.instantiate();
        VDeckEditorUI.SINGLETON_INSTANCE.instantiate();
    }
}
