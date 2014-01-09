package forge.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import forge.Singletons;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.SkinColor;
import forge.gui.toolbox.FSkin.SkinnedDialog;
import forge.util.OperatingSystem;

@SuppressWarnings("serial")
public class FDialog extends SkinnedDialog implements ITitleBarOwner, KeyEventDispatcher {
    private static final SkinColor borderColor = FSkin.getColor(FSkin.Colors.CLR_BORDERS);
    private static final int cornerDiameter = 20;
    private static final boolean isSetShapeSupported;
    private static final boolean antiAliasBorder;

    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        isSetShapeSupported = gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSPARENT);

        //only apply anti-aliasing to border on Windows, as it creates issues on Linux
        antiAliasBorder = OperatingSystem.isWindows();
    }

    private Point locBeforeMove;
    private Point mouseDownLoc;
    private final FTitleBar titleBar;
    private final FPanel innerPanel;
    private JComponent defaultFocus;

    public FDialog() {
        this(true);
    }

    public FDialog(boolean modal0) {
        super(JOptionPane.getRootFrame(), modal0);
        this.setUndecorated(true);
        this.setIconImage(FSkin.getIcon(FSkin.InterfaceIcons.ICO_FAVICON)); //use Forge icon by default

        this.innerPanel = new FPanel(new MigLayout("insets dialog, gap 0, center, fill"));
        this.innerPanel.setBackgroundTexture(FSkin.getIcon(FSkin.Backgrounds.BG_TEXTURE));
        this.innerPanel.setBackgroundTextureOverlay(FSkin.getColor(FSkin.Colors.CLR_THEME)); //use theme color as overlay to reduce background texture opacity
        this.innerPanel.setBorderToggle(false);
        this.innerPanel.setOpaque(false);
        super.setContentPane(this.innerPanel);

        this.titleBar = new FTitleBar(this);
        this.titleBar.setVisible(true);
        addMoveSupport();

        this.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(final WindowEvent e) {
                if (FDialog.this.defaultFocus != null) {
                    FDialog.this.defaultFocus.grabFocus();
                    FDialog.this.defaultFocus = null; //reset default focused component so it doesn't receive focus if the dialog later loses then regains focus
                }
            }

            @Override
            public void windowLostFocus(final WindowEvent e) {
            }
        });

        if (isSetShapeSupported) { //if possible, set rounded rectangle shape for dialog
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(final ComponentEvent e) { //must update shape whenever dialog is resized
                    int arc = cornerDiameter - 4; //leave room for border aliasing
                    FDialog.this.setShape(new RoundRectangle2D.Float(0, 0, FDialog.this.getWidth(), FDialog.this.getHeight(), arc, arc));
                }
            });
        }
    }

    //Make Escape key close dialog if allowed
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
                return true;
            }
        }
        return false;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        //draw rounded border
        final Graphics2D g2d = (Graphics2D) g.create();
        if (antiAliasBorder) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        FSkin.setGraphicsColor(g2d, borderColor);
        if (isSetShapeSupported) {
            g2d.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, cornerDiameter, cornerDiameter);
        }
        else { //draw non-rounded border instead if setShape isn't supported
            g2d.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        }
        g2d.dispose();
    }

    @Override
    public void dispose() {
        setVisible(false); //ensure overlay hidden when disposing
        super.dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.isVisible() == visible) { return; }

        if (visible) {
            setLocationRelativeTo(JOptionPane.getRootFrame());
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this); //support handling keyboard shortcuts in dialog
            if (isModal()) {
                backdropPanel.setVisible(true);
                Singletons.getView().getNavigationBar().setMenuShortcutsEnabled(false);
            }
        }
        else {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
            if (isModal()) {
                backdropPanel.setVisible(false);
                Singletons.getView().getNavigationBar().setMenuShortcutsEnabled(true);
            }
        }
        super.setVisible(visible);
    }

    public void setDefaultFocus(JComponent comp) {
        this.defaultFocus = comp;
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        if (this.titleBar != null) {
            this.titleBar.setTitle(title);
        }
    }

    @Override
    public void setIconImage(Image image) {
        super.setIconImage(image);
        if (this.titleBar != null) {
            this.titleBar.setIconImage(image);
        }
    }

    //relay certain methods to the inner panel if it has been initialized
    @Override
    public void setContentPane(Container contentPane) {
        if (innerPanel != null) {
            innerPanel.add(contentPane, "w 100%!, h 100%!");
        }
        super.setContentPane(contentPane);
    }

    @Override
    public Component add(Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(comp);
        }
        return super.add(comp);
    }
    
    @Override
    public void add(PopupMenu popup) {
        if (innerPanel != null) {
            innerPanel.add(popup);
            return;
        }
        super.add(popup);
    }
    
    @Override
    public void add(Component comp, Object constraints) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints);
            return;
        }
        super.add(comp, constraints);
    }
    
    @Override
    public Component add(Component comp, int index) {
        if (innerPanel != null) {
            return innerPanel.add(comp, index);
        }
        return super.add(comp, index);
    }
    
    @Override
    public void add(Component comp, Object constraints, int index) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints, index);
            return;
        }
        super.add(comp, constraints, index);
    }
    
    @Override
    public Component add(String name, Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(name, comp);
        }
        return super.add(name, comp);
    }

    private void addMoveSupport() {
        this.titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 1) {
                        locBeforeMove = getLocation();
                        mouseDownLoc = e.getLocationOnScreen();
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    locBeforeMove = null;
                    mouseDownLoc = null;
                }
            }
        });
        this.titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseDownLoc != null) {
                    final Point loc = e.getLocationOnScreen();
                    final int dx = loc.x - mouseDownLoc.x;
                    final int dy = loc.y - mouseDownLoc.y;
                    setLocation(locBeforeMove.x + dx, locBeforeMove.y + dy);
                }
            }
        });
    }

    @Override
    public boolean isMinimized() {
        return false;
    }

    @Override
    public void setMinimized(boolean b) {
    }

    @Override
    public boolean isMaximized() {
        return false;
    }

    @Override
    public void setMaximized(boolean b) {
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void setFullScreen(boolean b) {
    }

    @Override
    public boolean getLockTitleBar() {
        return false;
    }

    @Override
    public void setLockTitleBar(boolean b) {
    }

    @Override
    public Image getIconImage() {
        return getIconImages().isEmpty() ? null : getIconImages().get(0);
    }

    private static final BackdropPanel backdropPanel = new BackdropPanel();

    public static JPanel getBackdropPanel() {
        return backdropPanel;
    }

    private static class BackdropPanel extends JPanel {
        private static final SkinColor backColor = FSkin.getColor(FSkin.Colors.CLR_OVERLAY).alphaColor(120);

        private BackdropPanel() {
            setOpaque(false);
            setVisible(false);
            setFocusable(false);
        }

        @Override
        public void paintComponent(final Graphics g) {
            super.paintComponent(g);
            FSkin.setGraphicsColor(g, backColor);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }
}